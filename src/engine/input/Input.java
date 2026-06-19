package engine.input;

import engine.GamePanel;

public class Input {

    private static KeyHandler teclado;
    private static MouseHandler mouse;

    //Vincula o sistema de inputs ao GamePanel para escutar a janela.
     
    public static void inicializar(GamePanel gamePanel) {
        teclado = new KeyHandler();
        mouse = new MouseHandler();

        // Conecta os ouvintes nativos do Java ao nosso painel
        gamePanel.addKeyListener(teclado);
        gamePanel.addMouseListener(mouse);
        gamePanel.addMouseMotionListener(mouse);
    }

    // atalhos, so sim
    public static boolean paraCima() { return teclado.upPressed; }
    public static boolean paraBaixo() { return teclado.downPressed; }
    public static boolean paraEsquerda() { return teclado.leftPressed; }
    public static boolean paraDireita() { return teclado.rightPressed; }
    
    public static int getMouseX() { return MouseHandler.mouseX; }
    public static int getMouseY() { return MouseHandler.mouseY; }
    public static boolean esquerdoClicado() { return MouseHandler.cliqueEsquerdoPressed; }
}