package engine;

import engine.input.KeyHandler;
import java.awt.Graphics2D;

public abstract class GameState {
    
    protected GameStateManager gsm;
    
    public GameState(GameStateManager gsm) {
        this.gsm = gsm;
    }
    
    // Toda tela do jogo e obrigada a ter esses 3 metodos:
    public abstract void inicializar();
    public abstract void update(KeyHandler teclado);
    public abstract void draw(Graphics2D g2v);
}