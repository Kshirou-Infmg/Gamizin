package world;

import engine.Config;
import Entity.Entity;

public class Collision {

    public static void checarTile(Entity entidade, TileManager tileM) {
        
        double entidadeMundoEsqX = entidade.x + entidade.hitbox.x;
        double entidadeMundoDirX = entidade.x + entidade.hitbox.x + entidade.hitbox.width;
        double entidadeMundoTopoY = entidade.y + entidade.hitbox.y;
        double entidadeMundoBaseY = entidade.y + entidade.hitbox.y + entidade.hitbox.height;

        int entidadeEsqCol = (int) (entidadeMundoEsqX / Config.TAMANHO_TILE);
        int entidadeDirCol = (int) (entidadeMundoDirX / Config.TAMANHO_TILE);
        int entidadeTopoLin = (int) (entidadeMundoTopoY / Config.TAMANHO_TILE);
        int entidadeBaseLin = (int) (entidadeMundoBaseY / Config.TAMANHO_TILE);


        // Impede que os índices fiquem negativos ou ultrapassem o tamanho da matriz do mapa
        if (entidadeEsqCol < 0) entidadeEsqCol = 0;
        if (entidadeDirCol >= tileM.maxMundoCol) entidadeDirCol = tileM.maxMundoCol - 1;
        if (entidadeTopoLin < 0) entidadeTopoLin = 0;
        if (entidadeBaseLin >= tileM.maxMundoLin) entidadeBaseLin = tileM.maxMundoLin - 1;

        int tileNum1, tileNum2;
        entidade.colisaoLigada = false;

        switch (entidade.direcao) {
            case "cima":
                entidadeTopoLin = (int) ((entidadeMundoTopoY - (entidade.velocidade * engine.Time.deltaTime)) / Config.TAMANHO_TILE);
                // Nova trava para a previsão de cima
                if (entidadeTopoLin < 0) {
                    entidade.colisaoLigada = true; 
                    break;
                }
                
                tileNum1 = tileM.mapaMatriz[entidadeEsqCol][entidadeTopoLin];
                tileNum2 = tileM.mapaMatriz[entidadeDirCol][entidadeTopoLin];
                
                if (tileM.tiposDeTile[tileNum1].colidivel || tileM.tiposDeTile[tileNum2].colidivel) {
                    entidade.colisaoLigada = true;
                }
                break;

            case "baixo":
                entidadeBaseLin = (int) ((entidadeMundoBaseY + (entidade.velocidade * engine.Time.deltaTime)) / Config.TAMANHO_TILE);

                if (entidadeBaseLin >= tileM.maxMundoLin) {
                    entidade.colisaoLigada = true;
                    break;
                }
                
                tileNum1 = tileM.mapaMatriz[entidadeEsqCol][entidadeBaseLin];
                tileNum2 = tileM.mapaMatriz[entidadeDirCol][entidadeBaseLin];
                
                if (tileM.tiposDeTile[tileNum1].colidivel || tileM.tiposDeTile[tileNum2].colidivel) {
                    entidade.colisaoLigada = true;
                }
                break;

            case "esquerda":
                entidadeEsqCol = (int) ((entidadeMundoEsqX - (entidade.velocidade * engine.Time.deltaTime)) / Config.TAMANHO_TILE);
                // Nova trava para a previsão da esquerda
                if (entidadeEsqCol < 0) {
                    entidade.colisaoLigada = true;
                    break;
                }
                
                tileNum1 = tileM.mapaMatriz[entidadeEsqCol][entidadeTopoLin];
                tileNum2 = tileM.mapaMatriz[entidadeEsqCol][entidadeBaseLin];
                
                if (tileM.tiposDeTile[tileNum1].colidivel || tileM.tiposDeTile[tileNum2].colidivel) {
                    entidade.colisaoLigada = true;
                }
                break;

            case "direita":
                entidadeDirCol = (int) ((entidadeMundoDirX + (entidade.velocidade * engine.Time.deltaTime)) / Config.TAMANHO_TILE);
                // Nova trava para a previsão da direita
                if (entidadeDirCol >= tileM.maxMundoCol) {
                    entidade.colisaoLigada = true;
                    break;
                }
                
                tileNum1 = tileM.mapaMatriz[entidadeDirCol][entidadeTopoLin];
                tileNum2 = tileM.mapaMatriz[entidadeDirCol][entidadeBaseLin];
                
                if (tileM.tiposDeTile[tileNum1].colidivel || tileM.tiposDeTile[tileNum2].colidivel) {
                    entidade.colisaoLigada = true;
                }
                break;
        }
    }
}