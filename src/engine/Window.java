package engine;

import javax.swing.JFrame;

public class Window {
    
    public static void main(String[] args) {
        // cria janela
        JFrame janela = new JFrame();
        
        // pucha config das config
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setResizable(false);
        janela.setTitle(Config.TITULO); // pucha titulo

        // painel do jogo pa tacar na janela para conseguir ficar em tela cheia, ou n
        GamePanel gamePanel = new GamePanel();
        janela.add(gamePanel);
        
        // Ajusta o tamanho da janela externa para cobrir exatamente os 800x600 do GamePanel
        janela.pack(); 

        // Centralizar a janela no meio da tela do seu monitor
        janela.setLocationRelativeTo(null); 
        
        // janela n ser do ge
        janela.setVisible(true);

        // inicia o mundo/inputs e liga o Game Loop!
        gamePanel.inicializarJogo();
        gamePanel.start();
    }
}