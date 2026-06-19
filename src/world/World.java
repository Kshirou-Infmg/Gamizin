package world;

import java.awt.Graphics2D;
import Entity.Player;
import engine.Camera;
import engine.Config;

public class World {

    private TileManager tileM;
    private Player jogador;
    private Camera camera;

    //tamanho mapa
    private int larguraMaximaMundo;
    private int alturaMaximaMundo;

    public World() {
        // tamanho do mapa
        int colunas = 20;
        int linhas = 15;
        tileM = new TileManager(colunas, linhas);

        // Calcula o tamanho total do mapa em pixels (20 * 48 = 960px | 15 * 48 = 720px)
        larguraMaximaMundo = colunas * Config.TAMANHO_TILE;
        alturaMaximaMundo = linhas * Config.TAMANHO_TILE;

        // Inicializa a câmera do jogo usando o tamanho virtual do Config
        camera = new Camera(Config.LARGURA_VIRTUAL, Config.ALTURA_VIRTUAL);

        // spawn mais ao centro
        jogador = new Player(200, 200, tileM);
    }

    public void update() {
        if (jogador != null) {
            // logica
            jogador.update();
            
            // camera seguir alvo
            camera.focarNoAlvo(
                (int)jogador.x, 
                (int)jogador.y, 
                jogador.largura, 
                jogador.altura, 
                larguraMaximaMundo, 
                alturaMaximaMundo
            );
        }
    }

    public void draw(Graphics2D g2v) {
        //  Desenhar primeiro o chão e as paredes
        tileM.draw(g2v, camera);

        // Desenhar o jogador por cima do cenário 
        if (jogador != null) {
            // coisinha provavelmente temporaria (ou n)
            int telaX = (int) (jogador.x - camera.x);
            int telaY = (int) (jogador.y - camera.y);
            
            // posição real do mundo
            double xReal = jogador.x;
            double yReal = jogador.y;
            
            // Movendo o player temporariamente para a posição da tela apenas para desenhar
            jogador.x = telaX;
            jogador.y = telaY;
            
            jogador.draw(g2v);
            
            // Devolvendo a posição real do mundo para não quebrar a lógica física
            jogador.x = xReal;
            jogador.y = yReal;
        }
    }
}