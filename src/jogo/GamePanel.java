package jogo;

import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import menus.menuPause;

public class GamePanel extends JPanel implements Runnable {

    // =========================================================
    // CONFIGURAÇÕES GLOBAIS (Teclas e Sombras)
    // =========================================================
    public static class GameConfig {
        public static boolean sombraAtivada = false; // Desativada por padrão

        public static int teclaCima     = KeyEvent.VK_W;
        public static int teclaBaixo    = KeyEvent.VK_S;
        public static int teclaEsquerda = KeyEvent.VK_A;
        public static int teclaDireita  = KeyEvent.VK_D;
        public static int teclaPause    = KeyEvent.VK_ESCAPE;
        public static int teclaSombra   = KeyEvent.VK_M; // Tecla para o toggle de luz

        public static final int teclaCima2     = KeyEvent.VK_UP;
        public static final int teclaBaixo2    = KeyEvent.VK_DOWN;
        public static final int teclaEsquerda2 = KeyEvent.VK_LEFT;
        public static final int teclaDireita2  = KeyEvent.VK_RIGHT;

        public static final String[] NOMES_ACOES = {
            "Mover: Cima", "Mover: Baixo", "Mover: Esquerda", "Mover: Direita", "Pause", "Luz/Sombra"
        };

        public static int[] getTeclasPrimarias() {
            return new int[]{ teclaCima, teclaBaixo, teclaEsquerda, teclaDireita, teclaPause, teclaSombra };
        }

        public static void setTecla(int indice, int keyCode) {
            switch (indice) {
                case 0 -> teclaCima     = keyCode;
                case 1 -> teclaBaixo    = keyCode;
                case 2 -> teclaEsquerda = keyCode;
                case 3 -> teclaDireita  = keyCode;
                case 4 -> teclaPause    = keyCode;
                case 5 -> teclaSombra   = keyCode;
            }
        }

        public static void resetarTeclas() {
            teclaCima     = KeyEvent.VK_W;
            teclaBaixo    = KeyEvent.VK_S;
            teclaEsquerda = KeyEvent.VK_A;
            teclaDireita  = KeyEvent.VK_D;
            teclaPause    = KeyEvent.VK_ESCAPE;
            teclaSombra   = KeyEvent.VK_M;
            sombraAtivada = false;
        }
    }
    // =========================================================

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
        
        this.instancia_menu_pause = new menuPause(this); 
    }
    
    public void pausar() {
        pausado = true;
        //instancia_menu_pause.atualizarAtalhoPause(); 
        instancia_menu_pause.setVisible(true);
        instancia_menu_pause.toFront();
        instancia_menu_pause.requestFocus();
    }
    
    public void despausar() {
        pausado = false;
        instancia_menu_pause.setVisible(false);
        this.requestFocus(); 
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

            if (Input.foiPressionadoPause()) {
                if (pausado) despausar();
                else pausar();
            }

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

        // O Mundo agora desenha tudo: Chão, Entidades e as Sombras Poligonais!
        if (mundo != null) {
            mundo.draw(g2v);
        }

        g2v.setColor(Color.WHITE);
        g2v.drawString("FPS: " + Time.fps, 10, 20);
        g2v.drawString("Sombras [M]: " + (GameConfig.sombraAtivada ? "ON" : "OFF"), 10, 40);

        if (pausado) {
            g2v.setColor(new Color(0, 0, 0, 150)); 
            g2v.fillRect(0, 0, Config.LARGURA_VIRTUAL, Config.ALTURA_VIRTUAL);

            g2v.setColor(Color.WHITE);
            g2v.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 50));
            String texto = "PAUSADO";
            
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

// ======================================================================
// MOTOR DE SHADOW CASTING (RAYCASTING GEOMÉTRICO 2D)
// ======================================================================
class ShadowCaster {

    static class PontoAngulo {
        float x, y, angulo;
        PontoAngulo(float x, float y, float angulo) {
            this.x = x; this.y = y; this.angulo = angulo;
        }
    }

    public static GeneralPath calcularPoligonoVisibilidade(float luzX, float luzY, List<Line2D.Float> paredes) {
        List<PontoAngulo> pontosDeIntersecao = new ArrayList<>();

        // Coleta todos os pontos únicos (quinas) do mapa
        List<Point2D.Float> cantos = new ArrayList<>();
        for (Line2D.Float parede : paredes) {
            cantos.add(new Point2D.Float(parede.x1, parede.y1));
            cantos.add(new Point2D.Float(parede.x2, parede.y2));
        }

        // Lança 3 raios para cada canto: Exato, Exato-0.0001, Exato+0.0001
        for (Point2D.Float canto : cantos) {
            float anguloBase = (float) Math.atan2(canto.y - luzY, canto.x - luzX);
            
            float[] angulos = { anguloBase - 0.0001f, anguloBase, anguloBase + 0.0001f };

            for (float angulo : angulos) {
                float dx = (float) Math.cos(angulo);
                float dy = (float) Math.sin(angulo);
                float raioMax = 3000f; 

                Point2D.Float pIntersecao = null;
                float distMinima = Float.MAX_VALUE;

                for (Line2D.Float parede : paredes) {
                    Point2D.Float cruzamento = getIntersecao(
                        luzX, luzY, luzX + dx * raioMax, luzY + dy * raioMax,
                        parede.x1, parede.y1, parede.x2, parede.y2
                    );

                    if (cruzamento != null) {
                        float dist = (float) cruzamento.distanceSq(luzX, luzY);
                        if (dist < distMinima) {
                            distMinima = dist;
                            pIntersecao = cruzamento;
                        }
                    }
                }

                if (pIntersecao != null) {
                    pontosDeIntersecao.add(new PontoAngulo(pIntersecao.x, pIntersecao.y, angulo));
                }
            }
        }

        Collections.sort(pontosDeIntersecao, Comparator.comparingDouble(p -> p.angulo));

        GeneralPath poly = new GeneralPath();
        if (!pontosDeIntersecao.isEmpty()) {
            poly.moveTo(pontosDeIntersecao.get(0).x, pontosDeIntersecao.get(0).y);
            for (int i = 1; i < pontosDeIntersecao.size(); i++) {
                poly.lineTo(pontosDeIntersecao.get(i).x, pontosDeIntersecao.get(i).y);
            }
            poly.closePath();
        }

        return poly;
    }

    private static Point2D.Float getIntersecao(float ax, float ay, float bx, float by, float cx, float cy, float dx, float dy) {
        float divisor = (ax - bx) * (cy - dy) - (ay - by) * (cx - dx);
        if (divisor == 0) return null; 

        float t = ((ax - cx) * (cy - dy) - (ay - cy) * (cx - dx)) / divisor;
        float u = -((ax - bx) * (ay - cy) - (ay - by) * (ax - cx)) / divisor;

        if (t >= 0 && t <= 1 && u >= 0 && u <= 1) {
            float pX = ax + t * (bx - ax);
            float pY = ay + t * (by - ay);
            return new Point2D.Float(pX, pY);
        }
        return null;
    }
}
// ======================================================================

class Config {
    static final int TODOS = 60;
    static final int LARGURA_VIRTUAL = 800;
    static final int ALTURA_VIRTUAL = 600;
    static final int FPS = TODOS;
    static final String TITULO = "Apenas um Jogo";
    static final int TAMANHO_TILE = 48; 
}

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

        if (deltaTime > 0.1) deltaTime = 0.1;

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
            if (sleepTime > 0) Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
            else Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Camera {
    int x, y;
    private final int larguraTela, alturaTela;

    Camera(int larguraTela, int alturaTela) {
        this.larguraTela = larguraTela;
        this.alturaTela = alturaTela;
    }

    void focarNoAlvo(int alvoX, int alvoY, int alvoLargura, int alvoAltura, int larguraMaximaMapa, int alturaMaximaMapa) {
        this.x = alvoX + (alvoLargura / 2) - (larguraTela / 2);
        this.y = alvoY + (alvoAltura / 2) - (alturaTela / 2);
    }
}

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
            teclado.pauseTratado = true;
            return true;
        }
        return false;
    }
    
    static int getMouseX() { return MouseHandler.mouseX; }
    static int getMouseY() { return MouseHandler.mouseY; }
    static boolean esquerdoClicado() { return MouseHandler.cliqueEsquerdoPressed; }

    private static class KeyHandler implements KeyListener {
        boolean upPressed, downPressed, leftPressed, rightPressed;
        boolean confirmPressed, cancelPressed, menuPressed;
        
        boolean pausePressed;
        boolean pauseTratado = false;   
        
        boolean sombraTratado = false; // Lida com o toggle do 'M'

        @Override public void keyTyped(KeyEvent e) {}

        @Override public void keyPressed(KeyEvent e) { atualizarTeclas(e.getKeyCode(), true); }
        @Override public void keyReleased(KeyEvent e) { atualizarTeclas(e.getKeyCode(), false); }

        private void atualizarTeclas(int code, boolean pressionado) {
            if (code == GamePanel.GameConfig.teclaCima || code == GamePanel.GameConfig.teclaCima2) {
                upPressed = pressionado;
            } else if (code == GamePanel.GameConfig.teclaBaixo || code == GamePanel.GameConfig.teclaBaixo2) {
                downPressed = pressionado;
            } else if (code == GamePanel.GameConfig.teclaEsquerda || code == GamePanel.GameConfig.teclaEsquerda2) {
                leftPressed = pressionado;
            } else if (code == GamePanel.GameConfig.teclaDireita || code == GamePanel.GameConfig.teclaDireita2) {
                rightPressed = pressionado;
            } else if (code == GamePanel.GameConfig.teclaPause) {
                pausePressed = pressionado;
                if (!pressionado) pauseTratado = false;
            
            // TRATAMENTO DA TECLA DE SOMBRA (Toggle)
            } else if (code == GamePanel.GameConfig.teclaSombra) {
                if (pressionado && !sombraTratado) {
                    GamePanel.GameConfig.sombraAtivada = !GamePanel.GameConfig.sombraAtivada;
                    sombraTratado = true;
                } else if (!pressionado) {
                    sombraTratado = false;
                }
            } else {
                switch (code) {
                    case KeyEvent.VK_Z, KeyEvent.VK_ENTER -> confirmPressed = pressionado;
                    case KeyEvent.VK_X, KeyEvent.VK_SHIFT -> cancelPressed = pressionado;
                    case KeyEvent.VK_C, KeyEvent.VK_CONTROL -> menuPressed = pressionado;
                }
            }
        }
    }

    private static class MouseHandler implements MouseListener, MouseMotionListener {
        static int mouseX, mouseY;
        static boolean cliqueEsquerdoPressed, cliqueDireitoPressed;

        @Override public void mouseMoved(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
        @Override public void mousePressed(MouseEvent e) { atualizarMouse(e.getButton(), true); }
        @Override public void mouseReleased(MouseEvent e) { atualizarMouse(e.getButton(), false); }
        
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