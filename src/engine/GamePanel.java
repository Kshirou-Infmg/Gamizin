package engine;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import engine.input.Input;
import world.World;

public class GamePanel extends JPanel implements Runnable {

    private Thread gameThread;
    private boolean rodando = false;
    
    // Instancia do nosso mundo estruturado
    private World mundo;

    public GamePanel() {
        // Define o tamanho da tela usando as constantes do Config
        this.setPreferredSize(new Dimension(Config.LARGURA_VIRTUAL, Config.ALTURA_VIRTUAL));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
    }
    
    // Inicializar os componentes do motor e o mundo do jogo.
    public void inicializarJogo() {
        // Inicializa os inputs
        Input.inicializar(this);
        
        // Cria O MUNDO (sim isso foi uma referencia ao anime que n gosto)
        mundo = new World();
    }

    public synchronized void start() {
        if (rodando) return;
        rodando = true;
        gameThread = new Thread(this, "GameLoopThread");
        gameThread.start();
    }

    @Override
    public void run() {
        // Garante que o tempo inicialize corretamente antes do primeiro frame
        Time.update(); 

        while (rodando) {
            // Atualiza o Delta Time e o medidor de FPH (frames per hora)
            Time.update();

            // Atualiza a lógica do jogin
            update();

            //redesenhar a tela (pede para o swing (impora))
            repaint();

            // Controla a estabilidade do loop
            Time.sync();
        }
    }

    private void update() {
        if (mundo != null) {
            mundo.update();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2v = (Graphics2D) g;

        /* Se você estiver usando Pixel Art,
         pode aplicar o fator de escala aqui. Como configuramos a tela direto, desenhamos 1:1:,
        valeu duke ai, n tava entendendo pq tava bugando */
        if (mundo != null) {
            mundo.draw(g2v);
        }

        // Desenha o contador de FPS no topo esquerdo para monitoramento
        g2v.setColor(Color.WHITE);
        g2v.drawString("FPS: " + Time.fps, 10, 20);

        g2v.dispose();
    }
}