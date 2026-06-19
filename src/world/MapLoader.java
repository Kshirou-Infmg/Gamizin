package world;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MapLoader {

    public static int[][] carregarMapa(String caminho, int maxCol, int maxLin) {
        int[][] matrizGerada = new int[maxCol][maxLin];

        try {
            // Tenta carregar o arquivo de texto do sistema de recursos do projeto
            InputStream is = MapLoader.class.getResourceAsStream(caminho);
            
            if (is == null) {
                System.out.println("[ERRO CRÍTICO] Arquivo de mapa não encontrado em: " + caminho);
                System.out.println("-> Criando mapa de emergência automático para o jogo não bugar!");
                return gerarMapaEmergencia(maxCol, maxLin);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int lin = 0;

            while (col < maxCol && lin < maxLin) {
                String linhaTexto = br.readLine();

                if (linhaTexto == null) break;

                linhaTexto = linhaTexto.trim();
                if (linhaTexto.isEmpty()) continue;
                
                String[] numeros = linhaTexto.split("\\s+");

                while (col < maxCol && col < numeros.length) {
                    int num = Integer.parseInt(numeros[col]);
                    matrizGerada[col][lin] = num;
                    col++;
                }

                if (col == maxCol || col >= numeros.length) {
                    col = 0;
                    lin++;
                }
            }
            br.close();
            System.out.println("[SUCESSO] Mapa '" + caminho + "' carregado com sucesso!");

        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao processar a leitura da matriz. Gerando arena padrão.");
            return gerarMapaEmergencia(maxCol, maxLin);
        }

        return matrizGerada;
    }

    // Cria uma arena 8x4 para sair na porrada
    private static int[][] gerarMapaEmergencia(int maxCol, int maxLin) {
        int[][] emergencia = new int[maxCol][maxLin];
        for (int r = 0; r < maxLin; r++) {
            for (int c = 0; c < maxCol; c++) {
                if (r == 0 || r == maxLin - 1 || c == 0 || c == maxCol - 1) {
                    emergencia[c][r] = 1; // Paredes nas bordas
                } else {
                    emergencia[c][r] = 0; // Chao no chao
                }
            }
        }
        return emergencia;
    }
}