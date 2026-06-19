package engine.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    // VARIAVEIS DE MOVIMENTcÇAO
    public boolean upPressed, downPressed, leftPressed, rightPressed;
    
    // VARIAVEIS DE AÇAO
    public boolean confirmPressed; // Z ou Enter (Interagir / Confirmar)
    public boolean cancelPressed;  // X ou Shift (Cancelar / Passar dialogo rapido)
    public boolean menuPressed;    // C ou Ctrl  (Abrir Inventario / Menu)
    
    // Pause
    public boolean pausePressed;   // ESC (Pausar o jogo) (n fiz ainda nessa nova versao

    @Override
    public void keyTyped(KeyEvent e) {
        // sla porra, so sei que precisa
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // Movimento
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W)    upPressed = true;
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S)  downPressed = true;
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A)  leftPressed = true;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) rightPressed = true;

        // acao
        if (code == KeyEvent.VK_Z || code == KeyEvent.VK_ENTER) confirmPressed = true;
        if (code == KeyEvent.VK_X || code == KeyEvent.VK_SHIFT) cancelPressed = true;
        if (code == KeyEvent.VK_C || code == KeyEvent.VK_CONTROL) menuPressed = true;

        // forcar fechamento
        if (code == KeyEvent.VK_END) pausePressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        // Reseta as teclaaaaa
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W)    upPressed = false;
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S)  downPressed = false;
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A)  leftPressed = false;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) rightPressed = false;

        if (code == KeyEvent.VK_Z || code == KeyEvent.VK_ENTER) confirmPressed = false;
        if (code == KeyEvent.VK_X || code == KeyEvent.VK_SHIFT) cancelPressed = false;
        if (code == KeyEvent.VK_C || code == KeyEvent.VK_CONTROL) menuPressed = false;
        
        if (code == KeyEvent.VK_ESCAPE) pausePressed = false;
    }
}