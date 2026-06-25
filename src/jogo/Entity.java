        package jogo;

        import java.awt.Color;
        import java.awt.Graphics2D;
        import java.awt.Rectangle;

        /** Tudo que pode existir e se mover dentro do mundo (jogador, inimigos, etc.). */
        abstract class Entity {

            double x, y;
            int largura, altura;
            int velocidade;
            String direcao = "parado";
            boolean colisaoLigada = false;

            Rectangle hitbox;

            Entity(int x, int y, int largura, int altura, int velocidade) {
                this.x = x;
                this.y = y;
                this.largura = largura;
                this.altura = altura;
                this.velocidade = velocidade;

                this.hitbox = new Rectangle(0, 0, largura, altura);
            }

            abstract void update();
            abstract void draw(Graphics2D g2v);
        }

        /** O personagem controlado pelo jogador. */
        class Player extends Entity {

            private TileManager tileM;

            Player(int x, int y, TileManager tileM) {
                super(x, y, 48, 48, 250); // Velocidade de 250 pixels por segundo
                this.tileM = tileM;

                // Ajuste de hitbox do player (um pouco menor que o sprite)
                // menor o caralho
                this.hitbox = new Rectangle(0, 0, largura, altura);
            }

            @Override
            void update() {
                double moveX = 0;
                double moveY = 0;

                if (Input.paraCima())     { direcao = "cima";     moveY -= 1; }
                if (Input.paraBaixo())    { direcao = "baixo";    moveY += 1; }
                if (Input.paraEsquerda()) { direcao = "esquerda"; moveX -= 1; }
                if (Input.paraDireita())  { direcao = "direita";  moveX += 1; }

                // Se nenhuma tecla for pressionada, fica parado
                if (!Input.paraCima() && !Input.paraBaixo() && !Input.paraEsquerda() && !Input.paraDireita()) {
                    direcao = "parado";
                }

                // Normaliza o vetor de movimento, pra não andar mais rápido na diagonal
                double comprimento = Math.sqrt(moveX * moveX + moveY * moveY);
                if (comprimento > 1) {
                    moveX /= comprimento;
                    moveY /= comprimento;
                }

                if (moveY != 0) {
                    direcao = moveY < 0 ? "cima" : "baixo";
                    Collision.checarTile(this, tileM);
                    if (!colisaoLigada) {
                        y += moveY * velocidade * Time.deltaTime;
                    }
                }

                if (moveX != 0) {
                    direcao = moveX < 0 ? "esquerda" : "direita";
                    Collision.checarTile(this, tileM);
                    if (!colisaoLigada) {
                        x += moveX * velocidade * Time.deltaTime;
                    }
                }
            }

            @Override
            void draw(Graphics2D g2v) {
                // Cubo de teste no lugar do sprite
                g2v.setColor(new Color(205, 50, 50));
                g2v.fillRect((int) x, (int) y, largura, altura);

                // Olhos
                /*
                g2v.setColor(Color.WHITE);
                g2v.fillRect((int) x + 10, (int) y + 12, 8, 12);
                g2v.fillRect((int) x + 28, (int) y + 12, 8, 12);

                g2v.setColor(Color.BLACK);
                g2v.fillRect((int) x + 12, (int) y + 16, 5, 6);
                g2v.fillRect((int) x + 30, (int) y + 16, 5, 6);
                *///não gosto dos olhos dele
            }
        }