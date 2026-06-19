package world;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;
import engine.Config;
import engine.Camera;

public class TileManager {

    // Array que guarda os modelos de blocos disponíveis (Ex: [0]=Grama, [1]=Parede)
    public Tile[] tiposDeTile;
    
    // A matriz do mapa real (guarda os índices dos blocos)
    public int[][] mapaMatriz;

    // Tamanho do mapa em blocos
    public int maxMundoCol;
    public int maxMundoLin;

    public TileManager(int maxCol, int maxLin) {
        this.maxMundoCol = maxCol;
        this.maxMundoLin = maxLin;
        
        tiposDeTile = new Tile[10]; // Suporta até 10 tipos de blocos diferentes

        // 1. Primeiro carregamos as configurações dos tipos de bloco (IDs, colisões, etc)
        carregarImagensDosTiles();

        // 2. CORREÇÃO: Carrega a matriz do mapa a partir do ficheiro .txt usando o MapLoader
        // IMPORTANTE: Verifica se a tua pasta se chama "/maps/" ou "/res/maps/" no NetBeans
        this.mapaMatriz = MapLoader.carregarMapa("/maps/map1tst.txt", maxMundoCol, maxMundoLin);
    }

    private void carregarImagensDosTiles() {
        try {
            // Bloco 0: Grama (Atravessável)
            tiposDeTile[0] = new Tile();
            tiposDeTile[0].colidivel = false;

            // Bloco 1: Parede/Obstáculo (Sólido)
            tiposDeTile[1] = new Tile();
            tiposDeTile[1].colidivel = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Desenha os blocos na tela aplicando o scroll relativo da Câmera.
     */
    public void draw(Graphics2D g2v, Camera camera) {
        int col = 0;
        int lin = 0;

        while (col < maxMundoCol && lin < maxMundoLin) {
            // sla
            int tileNum = mapaMatriz[col][lin];

            // Calcula a posição absoluta dele no mundo (px)
            int mundoX = col * Config.TAMANHO_TILE;
            int mundoY = lin * Config.TAMANHO_TILE;

            // Subtrai a posição da câmara para saber onde desenhar na janela do PC
            int telaX = mundoX - camera.x;
            int telaY = mundoY - camera.y;

            // RENDERIZADOR
            if (tileNum == 1) {
                g2v.setColor(java.awt.Color.GRAY); // Parede 
            } else {
                g2v.setColor(new java.awt.Color(34, 139, 34)); // Chão 
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