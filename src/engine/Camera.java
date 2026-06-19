package engine;

public class Camera {
    
    // Posição atual da camera no mundo absolute pao
    public int x;
    public int y;
    
    // O tamanho do painel
    private final int larguraTela;
    private final int alturaTela;
    
    public Camera(int larguraTela, int alturaTela) {
        this.larguraTela = larguraTela;
        this.alturaTela = alturaTela;
        this.x = 0;
        this.y = 0;
    }
    
    
     /* Atualiza a posição da câmera para focar perfeitamente(que nem as bolas que o ibere faz) no centro dp Jogador.
     Tambem impede que a camera mostre o void fora das bordas do mapa atual.*/
    public void focarNoAlvo(int alvoX, int alvoY, int alvoLargura, int alvoAltura, int larguraMaximaMapa, int alturaMaximaMapa) {
        
        // OBMAP
        this.x = alvoX + (alvoLargura / 2) - (larguraTela / 2);
        this.y = alvoY + (alvoAltura / 2) - (alturaTela / 2);
        
        // Impede a cam de passar dos limites do mundo
        // Trava Esquerda / Topo
        if (this.x < 0) this.x = 0;
        if (this.y < 0) this.y = 0;
        
        // Trava Direita / Fundo
        if (this.x > larguraMaximaMapa - larguraTela) {
            this.x = larguraMaximaMapa - larguraTela;
        }
        if (this.y > alturaMaximaMapa - alturaTela) {
            this.y = alturaMaximaMapa - alturaTela;
        }
    }
}