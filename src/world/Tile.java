package world;

import java.awt.image.BufferedImage;

public class Tile {
    
    // A textura/imagem real deste bloco (ex: grama.png, parede.png)
    public BufferedImage imagem;
    
    // Se for 'true', o motor físico vai impedir qualquer entidade de passar por aqui
    public boolean colidivel = false;
}