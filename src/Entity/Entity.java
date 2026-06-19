package Entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;

public abstract class Entity {

    // eo seguinte, do usando double para usar o deltatime
    public double x, y;
    public int largura, altura;
    public int velocidade;
    public String direcao = "parado"; 
    public boolean colisaoLigada = false; 

    public Rectangle hitbox;

    public Entity(int x, int y, int largura, int altura, int velocidade) {
        // Faz o cast automarico de int(fireball) para double na inicializacao
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.velocidade = velocidade;
        
        this.hitbox = new Rectangle(0, 0, largura, altura);
    }

    public abstract void update();
    public abstract void draw(Graphics2D g2v);
}