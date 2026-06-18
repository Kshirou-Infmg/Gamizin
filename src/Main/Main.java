
package Main;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        // Cria a janela
        JFrame janela = new JFrame();
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setResizable(false);
        janela.setTitle("Meu Jogo, so meu");

        // Instancia o painel do jogo
        GamePanel gamePanel = new GamePanel();
        janela.add(gamePanel);
        janela.pack(); // Ajusta a janela ao tamanho do painel

        janela.setLocationRelativeTo(null); // Centraliza na tela
        janela.setVisible(true);

        // Inicia o relógio/loop do jogo
        gamePanel.startGameThread();
    }
}