package jogo;

import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import menus.menuPause;

public class GamePanel extends JPanel implements Runnable {

    private Thread gameThread;
    private volatile boolean rodando = false; 
    private World mundo;
    private boolean pausado = false;
    
    private menuPause instancia_menu_pause;

    public GamePanel() {
        this.setPreferredSize(new Dimension(Config.LARGURA_VIRTUAL, Config.ALTURA_VIRTUAL));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        
        // CORREÇÃO 1: Inicializar o menu para ele não ser nulo
        this.instancia_menu_pause = new menuPause(this); 
    }
    
    public void pausar() {
        pausado = true;
        instancia_menu_pause.setVisible(true);
        instancia_menu_pause.toFront();
        instancia_menu_pause.requestFocus();
    }
    
    public void despausar() {
        pausado = false;
        instancia_menu_pause.setVisible(false);
    }
    
    public boolean estaPausado() {
        return pausado;
    }

    public void inicializarJogo() {
        Input.inicializar(this);
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
        Time.update();

        while (rodando) {
            Time.update();

            // LÓGICA DE PAUSE: Verifica se a tecla foi pressionada uma única vez
            if (Input.foiPressionadoPause()) {
                if (pausado) {
                    despausar();
                } else {
                    pausar();
                }
            }

            // O mundo só atualiza a física/movimento se NÃO estiver pausado
            if (!pausado && mundo != null) {
                mundo.update();
            }
            
            repaint();

            Time.sync();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2v = (Graphics2D) g;

        if (mundo != null) {
            mundo.draw(g2v);
        }

        g2v.setColor(Color.WHITE);
        g2v.drawString("FPS: " + Time.fps, 10, 20);

        // DESENHO DO PAUSE: Renderiza uma tela semi-transparente por cima de tudo
        if (pausado) {
            // Cria um retângulo preto com 150 de transparência (Alpha de 0 a 255)
            g2v.setColor(new Color(0, 0, 0, 150)); 
            g2v.fillRect(0, 0, Config.LARGURA_VIRTUAL, Config.ALTURA_VIRTUAL);

            // Desenha o texto "PAUSADO" bem no centro da tela
            g2v.setColor(Color.WHITE);
            g2v.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 50));
            String texto = "PAUSADO";
            
            // Calcula a largura do texto para centralizá-lo matematicamente
            int larguraTexto = g2v.getFontMetrics().stringWidth(texto);
            int x = (Config.LARGURA_VIRTUAL - larguraTexto) / 2;
            int y = Config.ALTURA_VIRTUAL / 2;
            
            g2v.drawString(texto, x, y);
        }

        java.awt.Toolkit.getDefaultToolkit().sync(); 
    }

    public static void main(String[] args) {

        JFrame janela = new JFrame();
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setResizable(false);
        janela.setTitle(Config.TITULO);

        GamePanel gamePanel = new GamePanel();
        janela.add(gamePanel);
        
        janela.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                if (gamePanel.estaPausado()) {
                    gamePanel.despausar();
                }
            }
        });

        janela.pack();
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);

        gamePanel.inicializarJogo();
        gamePanel.start();
    }
}


class Config {
    static final int TODOS = 120;
    static final int LARGURA_VIRTUAL = 800;
    static final int ALTURA_VIRTUAL = 600;
    static final int FPS = TODOS;
    static final String TITULO = "Apenas um Jogo";
    static final int TAMANHO_TILE = 48; // Tamanho padrão dos blocos do mapa (48x48 pixels)
}


/** Delta Time, FPS*/
class Time {

    static double deltaTime;
    static int fps;

    private static long lastTime = System.nanoTime();
    private static long fpsTimer = System.currentTimeMillis();
    private static int frameCount = 0;

    static void update() {
        long currentTime = System.nanoTime();
        deltaTime = (currentTime - lastTime) / 1_000_000_000.0;
        lastTime = currentTime;

        // Limita o Delta Time máximo para evitar saltos grandes (stuttering)
        if (deltaTime > 0.1) {
            deltaTime = 0.1;
        }

        frameCount++;
        if (System.currentTimeMillis() - fpsTimer > 1000) {
            fps = frameCount;
            frameCount = 0;
            fpsTimer += 1000;
        }
    }

    static void sync() {
        double targetTime = 1_000_000_000.0 / Config.FPS;
        long visualTime = lastTime + (long) targetTime;
        long now = System.nanoTime();
        long sleepTime = visualTime - now;

        try {
            if (sleepTime > 0) {
                // Converte nanossegundos para milissegundos e nanossegundos restantes
                Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
            } else {

                // Isto impede que o loop asfixie a EDT (a thread que pinta o ecrã)
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

   

/** A "janela" que mostra parte do mundo na tela. */
class Camera {

    int x;
    int y;

    private final int larguraTela;
    private final int alturaTela;

    Camera(int larguraTela, int alturaTela) {
        this.larguraTela = larguraTela;
        this.alturaTela = alturaTela;
    }

    /** Centraliza a câmera no alvo e trava nas bordas do mapa. */
    void focarNoAlvo(int alvoX, int alvoY, int alvoLargura, int alvoAltura,
                      int larguraMaximaMapa, int alturaMaximaMapa) {

        this.x = alvoX + (alvoLargura / 2) - (larguraTela / 2);
        this.y = alvoY + (alvoAltura / 2) - (alturaTela / 2);

        // Usando a API Math do Java em vez do método limitar()
        /* temporariamente desabilitado
        this.x = Math.max(0, Math.min(this.x, larguraMaximaMapa - larguraTela));
        this.y = Math.max(0, Math.min(this.y, alturaMaximaMapa - alturaTela));
        */
    }
}

/** Fachada estática de input*/
class Input {

    private static KeyHandler teclado;
    private static MouseHandler mouse;

    static void inicializar(GamePanel gamePanel) {
        teclado = new KeyHandler();
        mouse = new MouseHandler();

        gamePanel.addKeyListener(teclado);
        gamePanel.addMouseListener(mouse);
        gamePanel.addMouseMotionListener(mouse);
    }

    static boolean paraCima() { return teclado.upPressed; }
    static boolean paraBaixo() { return teclado.downPressed; }
    static boolean paraEsquerda() { return teclado.leftPressed; }
    static boolean paraDireita() { return teclado.rightPressed; }
    
    static boolean foiPressionadoPause() {
        if (teclado.pausePressed && !teclado.pauseTratado) {
            teclado.pauseTratado = true; // Trava a tecla
            return true;
        }
        return false;
    }
    
    static int getMouseX() { return MouseHandler.mouseX; }
    // ...
    static int getMouseY() { return MouseHandler.mouseY; }
    static boolean esquerdoClicado() { return MouseHandler.cliqueEsquerdoPressed; }

    private static class KeyHandler implements KeyListener {

        boolean upPressed, downPressed, leftPressed, rightPressed;
        boolean confirmPressed; 
        boolean cancelPressed;  
        boolean menuPressed;    
        boolean pausePressed;
        
        // NOVA VARIÁVEL: A trava do botão
        boolean pauseTratado = false;   

        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e) {
            atualizarTeclas(e.getKeyCode(), true);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            atualizarTeclas(e.getKeyCode(), false);
        }

        private void atualizarTeclas(int code, boolean pressionado) {
            switch (code) {
                case KeyEvent.VK_UP, KeyEvent.VK_W -> upPressed = pressionado;
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> downPressed = pressionado;
                case KeyEvent.VK_LEFT, KeyEvent.VK_A -> leftPressed = pressionado;
                case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> rightPressed = pressionado;
                case KeyEvent.VK_Z, KeyEvent.VK_ENTER -> confirmPressed = pressionado;
                case KeyEvent.VK_X, KeyEvent.VK_SHIFT -> cancelPressed = pressionado;
                case KeyEvent.VK_C, KeyEvent.VK_CONTROL -> menuPressed = pressionado;
                case KeyEvent.VK_ESCAPE -> {
                    pausePressed = pressionado;
                    // Se o jogador soltou a tecla, destrava para o próximo clique
                    if (!pressionado) {
                        pauseTratado = false;
                    }
                }
            }
        }
    }

    private static class MouseHandler implements MouseListener, MouseMotionListener {

        static int mouseX;
        static int mouseY;
        static boolean cliqueEsquerdoPressed;
        static boolean cliqueDireitoPressed;

        @Override
        public void mouseMoved(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            atualizarMouse(e.getButton(), true);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            atualizarMouse(e.getButton(), false);
        }
        
        private void atualizarMouse(int button, boolean pressionado) {
            if (button == MouseEvent.BUTTON1) cliqueEsquerdoPressed = pressionado;
            else if (button == MouseEvent.BUTTON3) cliqueDireitoPressed = pressionado;
        }

        @Override public void mouseDragged(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
    }
}
