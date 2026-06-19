package engine.input;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MouseHandler implements MouseListener, MouseMotionListener {

    public static int mouseX;
    public static int mouseY;
    public static boolean cliqueEsquerdoPressed;
    public static boolean cliqueDireitoPressed;

    @Override
    public void mouseMoved(MouseEvent e) {
        // Atualiza a posicao do cursor na tela a cada movimento (sei la para que, so sei que sim)
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) cliqueEsquerdoPressed = true;
        if (e.getButton() == MouseEvent.BUTTON3) cliqueDireitoPressed = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) cliqueEsquerdoPressed = false;
        if (e.getButton() == MouseEvent.BUTTON3) cliqueDireitoPressed = false;
    }

    // Metodos obrigatorios da interface que podemos deixar vazios por enquanto, 
    @Override public void mouseDragged(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}