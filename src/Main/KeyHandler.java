package Main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    
    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean jogoPausado = false; 
    
    // VARIÁVEIS DAS TECLAS padrao WASDefghijk...
    public int teclaCima = KeyEvent.VK_W;
    public int teclaBaixo = KeyEvent.VK_S;
    public int teclaEsquerda = KeyEvent.VK_A;
    public int teclaDireita = KeyEvent.VK_D;

    // Controle do Menu de Config
    public int opcaoSelecionada = 0; // 0=Cima, 1=Baixo, 2=Esquerda, 3=Direita
    public boolean esperandoNovaTecla = false;

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // Tecla global para Pausar/Despausar
        if (code == KeyEvent.VK_ESCAPE) {
            if (esperandoNovaTecla) {
                esperandoNovaTecla = false; // Cancela se o cara amarelar no meio da troca, esse bananao
            } else {
                jogoPausado = !jogoPausado;
            }
            return;
        }

        // SE O JOGO ESTIVER PAUSADO PAUSA NE, obvio
        if (jogoPausado) {
            if (esperandoNovaTecla) {
                // Sobrescreve a tecla antiga 
                if (opcaoSelecionada == 0) teclaCima = code;
                if (opcaoSelecionada == 1) teclaBaixo = code;
                if (opcaoSelecionada == 2) teclaEsquerda = code;
                if (opcaoSelecionada == 3) teclaDireita = code;
                
                esperandoNovaTecla = false; // Fecha o modo de captura
            } else {
                // anda no menu
                if (code == KeyEvent.VK_UP) {
                    opcaoSelecionada--;
                    if (opcaoSelecionada < 0) opcaoSelecionada = 3;
                }
                if (code == KeyEvent.VK_DOWN) {
                    opcaoSelecionada++;
                    if (opcaoSelecionada > 3) opcaoSelecionada = 0;
                }
                // funçao para mudar a funçao das tecla
                if (code == KeyEvent.VK_ENTER) {
                    esperandoNovaTecla = true;
                }
                if (code == KeyEvent.VK_DELETE) {
                    System.exit(0);
                }
            }
        } 
        // Movimentação aNormal
        else {
            if (code == teclaCima) upPressed = true;
            if (code == teclaBaixo) downPressed = true;  
            if (code == teclaEsquerda) leftPressed = true;
            if (code == teclaDireita) rightPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == teclaCima) upPressed = false;
        if (code == teclaBaixo) downPressed = false;  
        if (code == teclaEsquerda) leftPressed = false;
        if (code == teclaDireita) rightPressed = false;
    }
}