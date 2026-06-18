package Main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage; // IMPORTANTE: Onde o jogo será renderizado
import javax.swing.JPanel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel implements Runnable, MouseListener, MouseMotionListener {
    
    // 1. RESOLUÇÃO VIRTUAL (A lógica da Engine roda sempre aqui)
    public final int LARGURA_VIRTUAL = 800;
    public final int ALTURA_VIRTUAL = 600;
    
    // Canvas interno onde a Engine vai desenhar antes de mandar pra tela
    private BufferedImage telaVirtual;
    
    // Variáveis para controlar o redimensionamento dinâmico
    private int larguraRender = 800;
    private int alturaRender = 600;
    private int offsetX = 0;
    private int offsetY = 0;

    // Configurações de Loop e FPS
    final int FPS = 60;
    private int fpsAtual = 0;
    private int contadorDeFrames = 0;
    private long ultimoTempoContador = System.currentTimeMillis();
    
    // Mouse e Teclado
    private int mouseVirtualX = 0;
    private int mouseVirtualY = 0;
    KeyHandler teclado = new KeyHandler();
    Thread gameThread;
    
    // Entidades do Motor
    java.util.ArrayList<Entidade> entidades = new java.util.ArrayList<>();
    Jogador jogador;
    int salaAtual = 0; 
    Image imagemTeste;

    public GamePanel() {
        // A janela inicial abre em 800x600, mas agora o usuário pode esticar!
        this.setPreferredSize(new Dimension(LARGURA_VIRTUAL, ALTURA_VIRTUAL));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(teclado);
        this.addMouseListener(this); 
        this.addMouseMotionListener(this); 
        this.setFocusable(true);
        
        // Inicializa a nossa tela interna de desenho
        telaVirtual = new BufferedImage(LARGURA_VIRTUAL, ALTURA_VIRTUAL, BufferedImage.TYPE_INT_RGB);
        
        carregarImagens();
        
        // Inicializa o jogador através do motor
        jogador = new Jogador(100, 100);
        entidades.add(jogador);
    }
    
    public void carregarImagens() {
        try {
            imagemTeste = ImageIO.read(getClass().getResourceAsStream("/Main/res/imagemTeste.jpg"));
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Erro ao carregar imagem de teste.");
        }
    }
    
    public void startGameThread(){
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    @Override
    public void run() {
        double intervaloDesenho = 1000000000.0 / FPS;
        long ultimoTempo = System.nanoTime();
        long tempoAtual;
        
        while(gameThread != null) {
            tempoAtual = System.nanoTime();
            
            // Calcula dinamicamente a escala caso a janela tenha mudado de tamanho
            calcularEscalaTela();
            
            update();
            repaint();
            
            contadorDeFrames++;
            if (System.currentTimeMillis() - ultimoTempoContador >= 1000) {
                fpsAtual = contadorDeFrames;
                contadorDeFrames = 0;
                ultimoTempoContador = System.currentTimeMillis();
            }
            
            java.awt.Toolkit.getDefaultToolkit().sync();
            
            try {
                long tempoProcessado = System.nanoTime() - tempoAtual;
                double tempoRestanteNano = intervaloDesenho - tempoProcessado;
                long tempoRestanteMili = (long)(tempoRestanteNano / 1000000);
                if (tempoRestanteMili > 0) Thread.sleep(tempoRestanteMili);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }   
    }
    
    /**
     * A MÁGICA DA ADAPTABILIDADE: Calcula como encaixar os 800x600 na janela atual
     */
    private void calcularEscalaTela() {
        int larguraJanela = getWidth();
        int alturaJanela = getHeight();
        
        // Descobre a proporção ideal (Aspect Ratio)
        double proporcaoVirtual = (double) LARGURA_VIRTUAL / ALTURA_VIRTUAL;
        double proporcaoJanela = (double) larguraJanela / alturaJanela;
        
        if (proporcaoJanela > proporcaoVirtual) {
            // Janela é mais larga (Pillarbox - barras nas laterais)
            alturaRender = alturaJanela;
            larguraRender = (int) (alturaJanela * proporcaoVirtual);
        } else {
            // Janela é mais alta (Letterbox - barras no topo e base)
            larguraRender = larguraJanela;
            alturaRender = (int) (larguraJanela / proporcaoVirtual);
        }
        
        // Centraliza o jogo na tela
        offsetX = (larguraJanela - larguraRender) / 2;
        offsetY = (alturaJanela - alturaRender) / 2;
    }

    public void update() {
        if (!teclado.jogoPausado) {
            for (Entidade e : entidades) {
                e.update(teclado);
            }
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 1. O MOTOR DESENHA TUDO DENTRO DA IMAGEM VIRTUAL PRIMEIRO
        Graphics2D g2Virtual = telaVirtual.createGraphics();
        
        // Limpa a tela interna com preto
        g2Virtual.setColor(Color.BLACK);
        g2Virtual.fillRect(0, 0, LARGURA_VIRTUAL, ALTURA_VIRTUAL);
        
        // Desenha o cenário de teste (sempre em 800x600 na cabeça do Java)
        if (salaAtual == 0 && imagemTeste != null) {
            g2Virtual.drawImage(imagemTeste, 0, 0, LARGURA_VIRTUAL, ALTURA_VIRTUAL, null);
        }
        
        // Renderiza as entidades do motor
        for (Entidade e : entidades) {
            // Aqui passamos o mouse virtualizado para o jogador olhar pro pão corretamente
            if (e instanceof Jogador) {
                ((Jogador) e).setPosicaoMouse(mouseVirtualX, mouseVirtualY);
            }
            e.draw(g2Virtual);
        }
        
        // Interface e FPS na resolução interna
        g2Virtual.setColor(Color.GREEN);
        g2Virtual.drawString("FPS: " + fpsAtual, 15, 25);
        
        g2Virtual.dispose();
        
        // 2. O MOTOR PEGA A TELA VIRTUAL E ENCAIXA NA JANELA REAL DO USUÁRIO
        Graphics2D g2Real = (Graphics2D) g;
        
        // Ativa interpolação bilinear para o jogo não ficar borrado/serrilhado ao esticar
        g2Real.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // Desenha a imagem do jogo escalada e centralizada
        g2Real.drawImage(telaVirtual, offsetX, offsetY, larguraRender, alturaRender, null);
        
        g2Real.dispose();
    }

    // =========================================================================
    // MATEMÁTICA DO MOUSE: Traduz a posição real do clique para o mundo do jogo
    // =========================================================================
    private void traduzirCoordenadasMouse(MouseEvent e) {
        // Subtrai as barras pretas e calcula a proporção matemática
        mouseVirtualX = (int) (((double)(e.getX() - offsetX) / larguraRender) * LARGURA_VIRTUAL);
        mouseVirtualY = (int) (((double)(e.getY() - offsetY) / alturaRender) * ALTURA_VIRTUAL);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        traduzirCoordenadasMouse(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        traduzirCoordenadasMouse(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        traduzirCoordenadasMouse(e);
        // O clique do menu de pausa agora funciona em qualquer resolução!
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}