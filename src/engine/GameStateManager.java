package engine;

import engine.input.KeyHandler;
import java.awt.Graphics2D;
import java.util.HashMap;

public class GameStateManager {
    
    // Ia me falou que e assim, pq n entendi porra nenhuma, alguem me explicaaaaa
    private HashMap<String, GameState> estados = new HashMap<>();
    private GameState estadoAtivo;
    
    public void adicionarEstado(String chave, GameState estado) {
        estados.put(chave, estado);
    }
    
    public void definirEstado(String chave) {
        estadoAtivo = estados.get(chave);
        if (estadoAtivo != null) {
            estadoAtivo.inicializar(); // Carrega os dados da tela ao entrar nela
        }
    }
    
    public void update(KeyHandler teclado) {
        if (estadoAtivo != null) {
            estadoAtivo.update(teclado);
        }
    }
    
    public void draw(Graphics2D g2v) {
        if (estadoAtivo != null) {
            estadoAtivo.draw(g2v);
        }
    }
}