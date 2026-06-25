package jogo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Scanner;
/**
 * Junta o mapa de tiles com o jogador: cria os dois, atualiza a lógica e
 * desenha tudo na ordem certa (chão -> jogador).
 */
class World {

    private TileManager tileM;
    private Player jogador;
    private Camera camera;

    private int larguraMaximaMundo;
    private int alturaMaximaMundo;

    World() {
        int colunas = 20;
        int linhas = 15;
        tileM = new TileManager(colunas, linhas);

        // Tamanho total do mapa em pixels (20 * 48 = 960px | 15 * 48 = 720px)
        larguraMaximaMundo = colunas * Config.TAMANHO_TILE;
        alturaMaximaMundo = linhas * Config.TAMANHO_TILE;

        camera = new Camera(Config.LARGURA_VIRTUAL, Config.ALTURA_VIRTUAL);

        // spawn mais ao centro
        jogador = new Player(200, 200, tileM);
    }

    void update() {
        if (jogador != null) {
            jogador.update();

            camera.focarNoAlvo(
                (int) jogador.x,
                (int) jogador.y,
                jogador.largura,
                jogador.altura,
                larguraMaximaMundo,
                alturaMaximaMundo
            );
        }
    }

    void draw(Graphics2D g2v) {
        tileM.draw(g2v, camera);

        if (jogador != null) {
            // Guardamos a posição original da "lente" do Graphics2D
            java.awt.geom.AffineTransform transformacaoOriginal = g2v.getTransform();
            
            // Movemos todo o contexto gráfico na direção oposta à câmara.
            // Isto permite desenhar o jogador nas suas coordenadas REAIS do mundo!
            g2v.translate(-camera.x, -camera.y);
            
            jogador.draw(g2v);
            
            // Restauramos a "lente" para que a UI (como o texto do FPS) 
            // não seja desenhada fora do lugar
            g2v.setTransform(transformacaoOriginal);
        }
    }
}

/** Dono da matriz do mapa e dos tipos de bloco; desenha o cenário com scroll da câmera. */
class TileManager {

    Tile[] tiposDeTile;
    int[][] mapaMatriz;
    int maxMundoCol;
    int maxMundoLin;

    TileManager(int maxCol, int maxLin) {
        this.maxMundoCol = maxCol;
        this.maxMundoLin = maxLin;

        tiposDeTile = new Tile[10]; // Suporta até 10 tipos de blocos diferentes

        carregarImagensDosTiles();

        // IMPORTANTE: confira se a pasta de mapas é "/maps/" ou "/res/maps/" no seu projeto
        try {
            this.mapaMatriz = MapLoader.carregarMapa("src/maps/map1tst.txt");
            this.maxMundoCol = this.mapaMatriz.length;
            this.maxMundoLin = this.mapaMatriz[0].length;
        } catch (Exception e) {
            System.out.println("ERRO AO CARREGAR MAPA, UTILIZANDO MAPA DE EMERGÊNCIA");
            this.mapaMatriz = MapLoader.gerarMapaEmergencia(maxCol, maxLin);
            this.maxMundoCol = maxCol;
            this.maxMundoLin = maxLin;
        }
        
    }

    private void carregarImagensDosTiles() {
        tiposDeTile[0] = new Tile();
        tiposDeTile[0].colidivel = false; // Grama (atravessável)

        tiposDeTile[1] = new Tile();
        tiposDeTile[1].colidivel = true; // Parede (sólida)
        
        // Placeholders
        tiposDeTile[2] = new Tile();
        tiposDeTile[2].colidivel = true;
        
        tiposDeTile[3] = new Tile();
        tiposDeTile[3].colidivel = true;
        
        tiposDeTile[4] = new Tile();
        tiposDeTile[4].colidivel = true;
        
        tiposDeTile[5] = new Tile();
        tiposDeTile[5].colidivel = true;
        
        tiposDeTile[6] = new Tile();
        tiposDeTile[6].colidivel = true;
        
        tiposDeTile[7] = new Tile();
        tiposDeTile[7].colidivel = true;
        
        tiposDeTile[8] = new Tile();
        tiposDeTile[8].colidivel = true;
        
        tiposDeTile[9] = new Tile();
        tiposDeTile[9].colidivel = true;
    }

    void draw(Graphics2D g2v, Camera camera) {
        int col = 0;
        int lin = 0;

        while (col < maxMundoCol && lin < maxMundoLin) {
            int tileNum = mapaMatriz[col][lin];

            int mundoX = col * Config.TAMANHO_TILE;
            int mundoY = lin * Config.TAMANHO_TILE;

            int telaX = mundoX - camera.x;
            int telaY = mundoY - camera.y;

            if (tileNum == 1) {
                g2v.setColor(Color.GRAY); // Parede
            } else {
                g2v.setColor(new Color(34, 139, 34)); // Chão
            }
            g2v.fillRect(telaX, telaY, Config.TAMANHO_TILE, Config.TAMANHO_TILE);

            col++;
            if (col == maxMundoCol) {
                col = 0;
                lin++;
            }
        }
    }
}

/** Um tipo de bloco do mapa (ex: grama, parede). */
class Tile {
    BufferedImage imagem;
    boolean colidivel = false;
}

/** Lê a matriz do mapa de um .txt em /maps/; se falhar, gera uma arena de emergência. */
class MapLoader {

    static int[][] carregarMapa(String caminho) throws Exception {
        Scanner sc = new Scanner(new File(caminho));
        int maxLin = sc.nextInt();
        int maxCol = sc.nextInt();
        
        int[][] matrizGerada = new int[maxCol][maxLin];

        for (int lin = 0; lin < maxLin; lin++) {
            for (int col = 0; col < maxCol; col++) {
                matrizGerada[col][lin] = sc.nextInt();
            }
        }
        
        sc.close();

        return matrizGerada;
    }

    // Cria uma arena com bordas sólidas para sair na porrada
    static int[][] gerarMapaEmergencia(int maxCol, int maxLin) {
        int[][] emergencia = new int[maxCol][maxLin];
        for (int r = 0; r < maxLin; r++) {
            for (int c = 0; c < maxCol; c++) {
                emergencia[c][r] = (r == 0 || r == maxLin - 1 || c == 0 || c == maxCol - 1) ? 1 : 0;
            }
        }
        return emergencia;
    }
}

/** Verifica colisão entre uma entidade e os tiles sólidos do mapa, na direção do movimento. */
class Collision {

    static void checarTile(Entity entidade, TileManager tileM) {

        double entidadeMundoEsqX = entidade.x + entidade.hitbox.x;
        double entidadeMundoDirX = entidade.x + entidade.hitbox.x + entidade.hitbox.width;
        double entidadeMundoTopoY = entidade.y + entidade.hitbox.y;
        double entidadeMundoBaseY = entidade.y + entidade.hitbox.y + entidade.hitbox.height;

        int entidadeEsqCol = (int) (entidadeMundoEsqX / Config.TAMANHO_TILE);
        int entidadeDirCol = (int) (entidadeMundoDirX / Config.TAMANHO_TILE);
        int entidadeTopoLin = (int) (entidadeMundoTopoY / Config.TAMANHO_TILE);
        int entidadeBaseLin = (int) (entidadeMundoBaseY / Config.TAMANHO_TILE);

        // Impede que os índices fiquem negativos ou ultrapassem o tamanho da matriz do mapa
        if (entidadeEsqCol < 0) entidadeEsqCol = 0;
        if (entidadeDirCol >= tileM.maxMundoCol) entidadeDirCol = tileM.maxMundoCol - 1;
        if (entidadeTopoLin < 0) entidadeTopoLin = 0;
        if (entidadeBaseLin >= tileM.maxMundoLin) entidadeBaseLin = tileM.maxMundoLin - 1;

        int tileNum1, tileNum2;
        entidade.colisaoLigada = false;

        switch (entidade.direcao) {
            case 'c':
                entidadeTopoLin = (int) ((entidadeMundoTopoY - (entidade.velocidade * Time.deltaTime)) / Config.TAMANHO_TILE);
                if (entidadeTopoLin < 0) {
                    entidade.colisaoLigada = true;
                    break;
                }
                tileNum1 = tileM.mapaMatriz[entidadeEsqCol][entidadeTopoLin];
                tileNum2 = tileM.mapaMatriz[entidadeDirCol][entidadeTopoLin];
                if (tileM.tiposDeTile[tileNum1].colidivel || tileM.tiposDeTile[tileNum2].colidivel) {
                    entidade.colisaoLigada = true;
                }
                break;

            case 'b':
                entidadeBaseLin = (int) ((entidadeMundoBaseY + (entidade.velocidade * Time.deltaTime)) / Config.TAMANHO_TILE);
                if (entidadeBaseLin >= tileM.maxMundoLin) {
                    entidade.colisaoLigada = true;
                    break;
                }
                tileNum1 = tileM.mapaMatriz[entidadeEsqCol][entidadeBaseLin];
                tileNum2 = tileM.mapaMatriz[entidadeDirCol][entidadeBaseLin];
                if (tileM.tiposDeTile[tileNum1].colidivel || tileM.tiposDeTile[tileNum2].colidivel) {
                    entidade.colisaoLigada = true;
                }
                break;

            case 'e':
                entidadeEsqCol = (int) ((entidadeMundoEsqX - (entidade.velocidade * Time.deltaTime)) / Config.TAMANHO_TILE);
                if (entidadeEsqCol < 0) {
                    entidade.colisaoLigada = true;
                    break;
                }
                tileNum1 = tileM.mapaMatriz[entidadeEsqCol][entidadeTopoLin];
                tileNum2 = tileM.mapaMatriz[entidadeEsqCol][entidadeBaseLin];
                if (tileM.tiposDeTile[tileNum1].colidivel || tileM.tiposDeTile[tileNum2].colidivel) {
                    entidade.colisaoLigada = true;
                }
                break;

            case 'd':
                entidadeDirCol = (int) ((entidadeMundoDirX + (entidade.velocidade * Time.deltaTime)) / Config.TAMANHO_TILE);
                if (entidadeDirCol >= tileM.maxMundoCol) {
                    entidade.colisaoLigada = true;
                    break;
                }
                tileNum1 = tileM.mapaMatriz[entidadeDirCol][entidadeTopoLin];
                tileNum2 = tileM.mapaMatriz[entidadeDirCol][entidadeBaseLin];
                if (tileM.tiposDeTile[tileNum1].colidivel || tileM.tiposDeTile[tileNum2].colidivel) {
                    entidade.colisaoLigada = true;
                }
                break;
        }
    }
}