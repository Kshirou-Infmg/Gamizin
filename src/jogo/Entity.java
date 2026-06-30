package jogo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** Tudo que pode existir e se mover dentro do mundo (jogador, inimigos, etc.). */
abstract class Entity {

    protected double x, y;
    protected int largura, altura;
    protected int velocidade;
    protected char direcao = 'p';
    protected boolean colisaoLigada = false;

    protected Rectangle hitbox;

    public Entity(int x, int y, int largura, int altura, int velocidade) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.velocidade = velocidade;
        this.hitbox = new Rectangle(0, 0, largura, altura);
    }

    abstract void update();
    abstract void draw(Graphics2D g2v);
}


// PLAYER
class Player extends Entity {

    private TileManager tileM;

    Player(int x, int y, TileManager tileM) {
        super(x, y, 48, 48, 250);
        this.tileM = tileM;
        this.hitbox = new Rectangle(2, 2, largura - 4, altura - 4);
    }

    @Override
    void update() {
        double moveX = 0;
        double moveY = 0;

        if (Input.paraCima())     { direcao = 'c'; moveY -= 1; }
        if (Input.paraBaixo())    { direcao = 'b'; moveY += 1; }
        if (Input.paraEsquerda()) { direcao = 'e'; moveX -= 1; }
        if (Input.paraDireita())  { direcao = 'd'; moveX += 1; }

        if (!Input.paraCima() && !Input.paraBaixo() && !Input.paraEsquerda() && !Input.paraDireita()) {
            direcao = 'p';
        }

        double comprimento = Math.sqrt(moveX * moveX + moveY * moveY);
        if (comprimento > 1) { moveX /= comprimento; moveY /= comprimento; }

        if (moveY != 0) {
            direcao = moveY < 0 ? 'c' : 'b';
            Collision.checarTile(this, tileM);
            if (!colisaoLigada) y += moveY * velocidade * Time.deltaTime;
        }
        if (moveX != 0) {
            direcao = moveX < 0 ? 'e' : 'd';
            Collision.checarTile(this, tileM);
            if (!colisaoLigada) x += moveX * velocidade * Time.deltaTime;
        }
    }

    @Override
    void draw(Graphics2D g2v) {
        g2v.setColor(new Color(205, 50, 50));
        g2v.fillRect((int) x, (int) y, largura, altura);
    }
}

// PERSEGUIDOR — Simplesmente vai em direção ao player
class Perseguidor extends Entity {

    private Player alvo;
    private TileManager tileM;

    public Perseguidor(int x, int y, TileManager tileM, Player alvo) {
        super(x, y, 48, 48, 105);
        this.tileM = tileM;
        this.alvo  = alvo;
        this.hitbox = new Rectangle(6, 6, largura - 12, altura - 12);
    }

    @Override
    void update() {
        double dx   = alvo.x - this.x;
        double dy   = alvo.y - this.y;
        double dist = Math.hypot(dx, dy);
        if (dist <= 12) { direcao = 'p'; return; }

        double moveX = dx / dist;
        double moveY = dy / dist;
        
        // Tenta mover no eixo Y
        if (moveY != 0) {
            this.direcao       = moveY < 0 ? 'c' : 'b';
            this.colisaoLigada = false;
            Collision.checarTile(this, tileM);
            if (!colisaoLigada) {
                y += moveY * velocidade * Time.deltaTime;
            }
        }

        // Tenta mover no eixo X 
        if (moveX != 0) {
            this.direcao       = moveX < 0 ? 'e' : 'd';
            this.colisaoLigada = false;
            Collision.checarTile(this, tileM);
            if (!colisaoLigada) {
                x += moveX * velocidade * Time.deltaTime;
            }
        }
        
        direcao = (Math.abs(dx) > Math.abs(dy)) ? (dx < 0 ? 'e' : 'd') : (dy < 0 ? 'c' : 'b');
    }

    @Override
    void draw(Graphics2D g2v) {
        // Laranja-avermelhado para diferenciar dos outros dois
        g2v.setColor(new Color(220, 120, 30));
        g2v.fillRect((int) x, (int) y, largura, altura);
    }
}

// A* PATHFINDING
class AStar {

    static List<int[]> encontrarCaminho(TileManager tileM, int startX, int startY, int endX, int endY) {
        int cols = tileM.maxMundoCol;
        int lins = tileM.maxMundoLin;

        startX = Math.max(0, Math.min(startX, cols - 1));
        startY = Math.max(0, Math.min(startY, lins - 1));
        endX   = Math.max(0, Math.min(endX,   cols - 1));
        endY   = Math.max(0, Math.min(endY,   lins - 1));

        if (tileM.mapaMatriz[endX][endY] == 1) {
            int[] v = encontrarVizinhoLivre(tileM, endX, endY);
            if (v == null) return new ArrayList<>();
            endX = v[0]; endY = v[1];
        }
        if (startX == endX && startY == endY) return new ArrayList<>();

        double[][] gCost  = new double[cols][lins];
        int[][]    parentX = new int[cols][lins];
        int[][]    parentY = new int[cols][lins];
        boolean[][] closed = new boolean[cols][lins];

        for (double[] row : gCost)   Arrays.fill(row, Double.MAX_VALUE);
        for (int[]    row : parentX) Arrays.fill(row, -1);
        for (int[]    row : parentY) Arrays.fill(row, -1);

        gCost[startX][startY] = 0;
        PriorityQueue<double[]> abertos = new PriorityQueue<>(Comparator.comparingDouble(a -> a[0]));
        abertos.add(new double[]{ heuristica(startX, startY, endX, endY), startX, startY });

        int[] dCol = { 0, 0, -1, 1 };
        int[] dLin = { -1, 1,  0, 0 };

        while (!abertos.isEmpty()) {
            double[] atual = abertos.poll();
            int cx = (int) atual[1];
            int cy = (int) atual[2];
            if (closed[cx][cy]) continue;
            closed[cx][cy] = true;

            if (cx == endX && cy == endY) {
                List<int[]> cam = new ArrayList<>();
                int rx = cx, ry = cy;
                while (!(rx == startX && ry == startY)) {
                    cam.add(0, new int[]{ rx, ry });
                    int px = parentX[rx][ry], py = parentY[rx][ry];
                    rx = px; ry = py;
                }
                return cam;
            }

            for (int i = 0; i < 4; i++) {
                int nx = cx + dCol[i];
                int ny = cy + dLin[i];
                if (nx < 0 || nx >= cols || ny < 0 || ny >= lins) continue;
                if (closed[nx][ny] || tileM.mapaMatriz[nx][ny] == 1) continue;
                double novoG = gCost[cx][cy] + 1;
                if (novoG < gCost[nx][ny]) {
                    gCost[nx][ny]   = novoG;
                    parentX[nx][ny] = cx;
                    parentY[nx][ny] = cy;
                    abertos.add(new double[]{ novoG + heuristica(nx, ny, endX, endY), nx, ny });
                }
            }
        }
        return new ArrayList<>();
    }

    private static double heuristica(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private static int[] encontrarVizinhoLivre(TileManager tileM, int col, int lin) {
        int[] dC = { 0, 0, -1, 1 };
        int[] dL = { -1, 1, 0, 0 };
        for (int i = 0; i < 4; i++) {
            int nc = col + dC[i], nl = lin + dL[i];
            if (nc >= 0 && nc < tileM.maxMundoCol && nl >= 0 && nl < tileM.maxMundoLin)
                if (tileM.mapaMatriz[nc][nl] != 1) return new int[]{ nc, nl };
        }
        return null;
    }
}


//algoritimo
class LineOfSight {

    
     // Retorna uma matriz [col][lin] com true onde o player enxerga.
    static boolean[][] calcular(TileManager tileM, double origemX, double origemY) {
        int cols = tileM.maxMundoCol;
        int lins = tileM.maxMundoLin;
        boolean[][] visivel = new boolean[cols][lins];

        int oCol = Math.max(0, Math.min((int)(origemX / Config.TAMANHO_TILE), cols - 1));
        int oLin = Math.max(0, Math.min((int)(origemY / Config.TAMANHO_TILE), lins - 1));

        for (int col = 0; col < cols; col++) {
            for (int lin = 0; lin < lins; lin++) {
                visivel[col][lin] = linhaLivre(tileM, oCol, oLin, col, lin);
            }
        }

        return visivel;
    }

    static boolean linhaLivre(TileManager tileM, int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        int cx = x0, cy = y0;

        while (true) {
            if (cx == x1 && cy == y1) return true;

            // Tile intermediário (não é a origem) — bloqueia se for parede
            if (!(cx == x0 && cy == y0) && tileM.mapaMatriz[cx][cy] == 1) return false;

            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; cx += sx; }
            if (e2 <  dx) { err += dx; cy += sy; }
        }
    }
}
