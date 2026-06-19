package Entity; 

import java.awt.Color;
import java.awt.Graphics2D;
import engine.Time;
import engine.input.Input;
import world.Collision; // Importa o nosso sistema de colisão

public class Player extends Entity {

    // referencia temporaria (ou n)
    private world.TileManager tileM;

    public Player(int x, int y, world.TileManager tileM) {
        super(x, y, 48, 48, 250); // Velocidade absurda de 250 pixels por segundo wooooooool 
        this.tileM = tileM;
        
        // ajuste de hibox do player
        this.hitbox = new java.awt.Rectangle(8, 16, 32, 32); 
    }

    @Override
    public void update() { 
        double moveX = 0;
        double moveY = 0;

        // Capturar intencao de movimento
        if (Input.paraCima()) {
            direcao = "cima";
            moveY -= 1;
        }
        if (Input.paraBaixo()) {
            direcao = "baixo";
            moveY += 1;
        }
        if (Input.paraEsquerda()) {
            direcao = "esquerda";
            moveX -= 1;
        }
        if (Input.paraDireita()) {
            direcao = "direita";
            moveX += 1;
        }

        // Se nenhuma tecla for pressionada, Inercia
        if (!Input.paraCima() && !Input.paraBaixo() && !Input.paraEsquerda() && !Input.paraDireita()) {
            direcao = "parado";
        }

        // Aqui seu bosta, aprendi fazer essa coisa
        double comprimento = Math.sqrt(moveX * moveX + moveY * moveY);
        if (comprimento > 1) {
            moveX /= comprimento;
            moveY /= comprimento;
        }


        // movimento vertical
        if (moveY != 0) {
            direcao = moveY < 0 ? "cima" : "baixo";
            Collision.checarTile(this, tileM);
            if (!colisaoLigada) {
                y += moveY * velocidade * Time.deltaTime;
            }
        }

        // movimento horizonte me pede para ir, tao longe...
        if (moveX != 0) {
            direcao = moveX < 0 ? "esquerda" : "direita";
            Collision.checarTile(this, tileM);
            if (!colisaoLigada) {
                x += moveX * velocidade * Time.deltaTime;
            }
        }
    }

    @Override
    public void draw(Graphics2D g2v) {
        // Renderiza o cubu de teste(temo que adiciona-lo como referencia pq n sei fazer coisa melhor a n ser escrever comentarios inuteis)
        g2v.setColor(new Color(50, 205, 50));
        g2v.fillRect((int)x, (int)y, largura, altura);

        // oio
        g2v.setColor(Color.WHITE);
        g2v.fillRect((int)x + 10, (int)y + 12, 8, 12);
        g2v.fillRect((int)x + 28, (int)y + 12, 8, 12);
        
        g2v.setColor(Color.BLACK);
        g2v.fillRect((int)x + 12, (int)y + 16, 5, 6);
        g2v.fillRect((int)x + 30, (int)y + 16, 5, 6);
    }
}