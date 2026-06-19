package engine;

public class Time {

    public static double deltaTime;
    public static int fps;
    
    private static long m_LastTime = System.nanoTime();
    private static long m_FPSTimer = System.currentTimeMillis();
    private static int m_Frames = 0;

     // Atualiza o fps
    public static void update() {
        long currentTime = System.nanoTime();
        // Calcula o tempo que passou desde o último frame em segundos, parte chata
        deltaTime = (currentTime - m_LastTime) / 1000000000.0;
        m_LastTime = currentTime;

        // Limita o Delta Time máximo para evitar saltos gigantes caso o jogo engasgue
        if (deltaTime > 0.1) {
            deltaTime = 0.1;
        }

        // Contador de FPS interno
        m_Frames++;
        if (System.currentTimeMillis() - m_FPSTimer > 1000) {
            fps = m_Frames;
            m_Frames = 0;
            m_FPSTimer += 1000;
        }
    }

    // serve para sincronizat o loop para manter a taxa de quadros estável baseada no Config.
 
    public static void sync() {
        double targetTime = 1000000000.0 / Config.FPS;
        long visualTime = m_LastTime + (long) targetTime;
        
        while (System.nanoTime() < visualTime) {
            try {
                // Da um pequeno descanso para o processador do PC não fritar, meu notebook agradece
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}