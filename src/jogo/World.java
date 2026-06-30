package jogo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Junta o mapa de tiles com o jogador e outras entidades: cria as instâncias,
 * atualiza a lógica de cada um e desenha tudo na ordem certa.
 *
 * Ordem de desenho:
 * 1. Chão / tiles
 * 2. Entidades (dentro do translate da câmera)
 * 3. laboratoriozinho de tst
 */
class World {

    private TileManager tileM;
    private Player      jogador;
    private Follower    seguidor;    // IA inteligente (A*)      — verde
    private Perseguidor perseguidor; // IA burra (greedy)        — laranja
    private Camera      camera;

    private int larguraMaximaMundo;
    private int alturaMaximaMundo;

    World() {
        int colunas = 20;
        int linhas  = 15;
        tileM = new TileManager(colunas, linhas);

        larguraMaximaMundo = colunas * Config.TAMANHO_TILE;
        alturaMaximaMundo  = linhas  * Config.TAMANHO_TILE;

        camera = new Camera(Config.LARGURA_VIRTUAL, Config.ALTURA_VIRTUAL);

        jogador     = new Player(200, 200, tileM);
        seguidor    = new Follower(400, 300, tileM, jogador);
        perseguidor = new Perseguidor(100, 400, tileM, jogador); // posição diferente para variar
    }

    void update() {
        if (jogador != null) {
            jogador.update();

            camera.focarNoAlvo(
                (int) jogador.x, (int) jogador.y,
                jogador.largura, jogador.altura,
                larguraMaximaMundo, alturaMaximaMundo
            );
        }

        if (seguidor    != null) seguidor.update();
        if (perseguidor != null) perseguidor.update();
    }

    void draw(Graphics2D g2v) {
        // Chão
        tileM.draw(g2v, camera);

        // Entidades (em coordenadas de mundo via translate da câmera)
        if (jogador != null) {
            java.awt.geom.AffineTransform transformacaoOriginal = g2v.getTransform();
            g2v.translate(-camera.x, -camera.y);

            if (seguidor    != null) seguidor.draw(g2v);
            if (perseguidor != null) perseguidor.draw(g2v);

            jogador.draw(g2v); // player por cima dos inimigos

            g2v.setTransform(transformacaoOriginal);
        }


        // lab1
        if (GamePanel.GameConfig.sombraAtivada && jogador != null) {
            desenharSombraPoligono(g2v);
        }
    }
    
    // meu laboratorio de testes
    private void desenharSombraPoligono(Graphics2D g2v) {
        
        // Pega o centro exato do Player NA TELA
        float luzX = (float) (jogador.x + jogador.largura / 2.0 - camera.x);
        float luzY = (float) (jogador.y + jogador.altura  / 2.0 - camera.y);

        List<Line2D.Float> paredes = new ArrayList<>();

        //Bordas da tela 
        paredes.add(new Line2D.Float(0, 0, Config.LARGURA_VIRTUAL, 0));
        paredes.add(new Line2D.Float(Config.LARGURA_VIRTUAL, 0, Config.LARGURA_VIRTUAL, Config.ALTURA_VIRTUAL));
        paredes.add(new Line2D.Float(Config.LARGURA_VIRTUAL, Config.ALTURA_VIRTUAL, 0, Config.ALTURA_VIRTUAL));
        paredes.add(new Line2D.Float(0, Config.ALTURA_VIRTUAL, 0, 0));

        int cols = tileM.maxMundoCol;
        int lins = tileM.maxMundoLin;

        //Varre o mapa em busca de blocos sólidos para formar as paredes
        for (int col = 0; col < cols; col++) {
            for (int lin = 0; lin < lins; lin++) {
                int tileNum = tileM.mapaMatriz[col][lin];
                
                // Se o bloco for colidível, vamos checar seus vizinhos!
                if (tileM.tiposDeTile[tileNum].colidivel) {
                    float px = col * Config.TAMANHO_TILE - camera.x;
                    float py = lin * Config.TAMANHO_TILE - camera.y;

                    // Otimização: ignora blocos que estão muito longe fora da tela
                    if (px + Config.TAMANHO_TILE < -100 || px > Config.LARGURA_VIRTUAL + 100) continue;
                    if (py + Config.TAMANHO_TILE < -100 || py > Config.ALTURA_VIRTUAL + 100)  continue;


                    // Checa CIMA
                    if (lin == 0 || !tileM.tiposDeTile[tileM.mapaMatriz[col][lin-1]].colidivel) {
                        paredes.add(new Line2D.Float(px, py, px + Config.TAMANHO_TILE, py));
                    }
                    // Checa BAIXO
                    if (lin == lins - 1 || !tileM.tiposDeTile[tileM.mapaMatriz[col][lin+1]].colidivel) {
                        paredes.add(new Line2D.Float(px, py + Config.TAMANHO_TILE, px + Config.TAMANHO_TILE, py + Config.TAMANHO_TILE));
                    }
                    // ChecaESQUERDA
                    if (col == 0 || !tileM.tiposDeTile[tileM.mapaMatriz[col-1][lin]].colidivel) {
                        paredes.add(new Line2D.Float(px, py, px, py + Config.TAMANHO_TILE));
                    }
                    // Checa DIREITA
                    if (col == cols - 1 || !tileM.tiposDeTile[tileM.mapaMatriz[col+1][lin]].colidivel) {
                        paredes.add(new Line2D.Float(px + Config.TAMANHO_TILE, py, px + Config.TAMANHO_TILE, py + Config.TAMANHO_TILE));
                    }
                }
            }
        }

        GeneralPath poligonoLuz = ShadowCaster.calcularPoligonoVisibilidade(luzX, luzY, paredes);

        Area escuridao = new Area(new java.awt.Rectangle(0, 0, Config.LARGURA_VIRTUAL, Config.ALTURA_VIRTUAL));

        if (poligonoLuz != null) {
            escuridao.subtract(new Area(poligonoLuz));
        }
        g2v.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2v.setColor(new Color(0, 0, 0, 240)); // 240 = Muito escuro (quase breu)
        g2v.fill(escuridao);

        g2v.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
}

// TILE MANAGER
class TileManager {

    Tile[] tiposDeTile;
    int[][] mapaMatriz;
    int maxMundoCol;
    int maxMundoLin;

    TileManager(int maxCol, int maxLin) {
        this.maxMundoCol = maxCol;
        this.maxMundoLin = maxLin;

        tiposDeTile = new Tile[10];
        carregarImagensDosTiles();

        try {
            this.mapaMatriz  = MapLoader.carregarMapa("src/maps/map1tst.txt");
            this.maxMundoCol = this.mapaMatriz.length;
            this.maxMundoLin = this.mapaMatriz[0].length;
        } catch (Exception e) {
            System.out.println("ERRO AO CARREGAR MAPA, UTILIZANDO MAPA DE EMERGÊNCIA");
            this.mapaMatriz  = MapLoader.gerarMapaEmergencia(maxCol, maxLin);
            this.maxMundoCol = maxCol;
            this.maxMundoLin = maxLin;
        }
    }

    private void carregarImagensDosTiles() {
        tiposDeTile[0] = new Tile(); tiposDeTile[0].colidivel = false; // Grama
        tiposDeTile[1] = new Tile(); tiposDeTile[1].colidivel = true;  // Parede
        tiposDeTile[2] = new Tile(); tiposDeTile[2].colidivel = true;
        tiposDeTile[3] = new Tile(); tiposDeTile[3].colidivel = true;
        tiposDeTile[4] = new Tile(); tiposDeTile[4].colidivel = true;
        tiposDeTile[5] = new Tile(); tiposDeTile[5].colidivel = true;
        tiposDeTile[6] = new Tile(); tiposDeTile[6].colidivel = true;
        tiposDeTile[7] = new Tile(); tiposDeTile[7].colidivel = true;
        tiposDeTile[8] = new Tile(); tiposDeTile[8].colidivel = true;
        tiposDeTile[9] = new Tile(); tiposDeTile[9].colidivel = true;
    }

    void draw(Graphics2D g2v, Camera camera) {
        int col = 0, lin = 0;
        while (col < maxMundoCol && lin < maxMundoLin) {
            int tileNum = mapaMatriz[col][lin];
            int telaX   = col * Config.TAMANHO_TILE - camera.x;
            int telaY   = lin * Config.TAMANHO_TILE - camera.y;

            g2v.setColor(tileNum == 1 ? Color.GRAY : new Color(34, 139, 34));
            g2v.fillRect(telaX, telaY, Config.TAMANHO_TILE, Config.TAMANHO_TILE);

            col++;
            if (col == maxMundoCol) { col = 0; lin++; }
        }
    }
}


// TILE / MAP LOADER / COLLISION
class Tile {
    BufferedImage imagem;
    boolean colidivel = false;
}

class MapLoader {

    static int[][] carregarMapa(String caminho) throws Exception {
        Scanner sc = new Scanner(new File(caminho));
        int maxLin = sc.nextInt();
        int maxCol = sc.nextInt();
        int[][] mat = new int[maxCol][maxLin];
        for (int lin = 0; lin < maxLin; lin++)
            for (int col = 0; col < maxCol; col++)
                mat[col][lin] = sc.nextInt();
        sc.close();
        return mat;
    }

    static int[][] gerarMapaEmergencia(int maxCol, int maxLin) {
        int[][] em = new int[maxCol][maxLin];
        for (int r = 0; r < maxLin; r++)
            for (int c = 0; c < maxCol; c++)
                em[c][r] = (r == 0 || r == maxLin - 1 || c == 0 || c == maxCol - 1) ? 1 : 0;
        return em;
    }
}

class Collision {

    static void checarTile(Entity entidade, TileManager tileM) {
        double esqX  = entidade.x + entidade.hitbox.x;
        double dirX  = entidade.x + entidade.hitbox.x + entidade.hitbox.width;
        double topoY = entidade.y + entidade.hitbox.y;
        double baseY = entidade.y + entidade.hitbox.y + entidade.hitbox.height;

        int esqCol  = Math.max(0, (int)(esqX  / Config.TAMANHO_TILE));
        int dirCol  = Math.min(tileM.maxMundoCol - 1, (int)(dirX  / Config.TAMANHO_TILE));
        int topoLin = Math.max(0, (int)(topoY / Config.TAMANHO_TILE));
        int baseLin = Math.min(tileM.maxMundoLin - 1, (int)(baseY / Config.TAMANHO_TILE));

        entidade.colisaoLigada = false;
        double dt = entidade.velocidade * Time.deltaTime;

        int tileNum1, tileNum2;

        switch (entidade.direcao) {
            case 'c':
                int tL = (int)((topoY - dt) / Config.TAMANHO_TILE);
                if (tL < 0) { entidade.colisaoLigada = true; break; }
                tileNum1 = tileM.mapaMatriz[esqCol][tL];
                tileNum2 = tileM.mapaMatriz[dirCol][tL];
                if (tileM.tiposDeTile[tileNum1].colidivel || tileM.tiposDeTile[tileNum2].colidivel)
                    entidade.colisaoLigada = true;
                break;

            case 'b':
                int bL = (int)((baseY + dt) / Config.TAMANHO_TILE);
                if (bL >= tileM.maxMundoLin) { entidade.colisaoLigada = true; break; }
                tileNum1 = tileM.mapaMatriz[esqCol][bL];
                tileNum2 = tileM.mapaMatriz[dirCol][bL];
                if (tileM.tiposDeTile[tileNum1].colidivel || tileM.tiposDeTile[tileNum2].colidivel)
                    entidade.colisaoLigada = true;
                break;

            case 'e':
                int eC = (int)((esqX - dt) / Config.TAMANHO_TILE);
                if (eC < 0) { entidade.colisaoLigada = true; break; }
                tileNum1 = tileM.mapaMatriz[eC][topoLin];
                tileNum2 = tileM.mapaMatriz[eC][baseLin];
                if (tileM.tiposDeTile[tileNum1].colidivel || tileM.tiposDeTile[tileNum2].colidivel)
                    entidade.colisaoLigada = true;
                break;

            case 'd':
                int dC = (int)((dirX + dt) / Config.TAMANHO_TILE);
                if (dC >= tileM.maxMundoCol) { entidade.colisaoLigada = true; break; }
                tileNum1 = tileM.mapaMatriz[dC][topoLin];
                tileNum2 = tileM.mapaMatriz[dC][baseLin];
                if (tileM.tiposDeTile[tileNum1].colidivel || tileM.tiposDeTile[tileNum2].colidivel)
                    entidade.colisaoLigada = true;
                break;
        }
    }
}