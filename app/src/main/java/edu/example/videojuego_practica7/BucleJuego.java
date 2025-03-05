package edu.example.videojuego_practica7;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.graphics.Canvas;

import android.view.SurfaceHolder;

/**
 * Bucle principal del juego.
 */
public class BucleJuego extends Thread{

    // Referencia al juego y al SurfaceHolder
    private EboraJuego juego;
    private SurfaceHolder surfaceHolder;
    private boolean juegoEnEjecucion = true; // Indica si el juego está en ejecución

    // Constantes para el control de FPS
    public static final int MAX_FPS = 30;
    public static final int TIEMPO_FRAME = 1000 / MAX_FPS; // ~33ms
    public static final int MAX_FRAMES_SALTADOS = 5;

    /**
     * Constructor del bucle principal.
     * @param sh SurfaceHolder del juego
     * @param s Juego
     */
    public BucleJuego(SurfaceHolder sh, EboraJuego s) {
        juego = s;
        surfaceHolder = sh;
    }

    /**
     * Bucle principal del juego.
     */
    public void run() {
        Canvas canvas; // Canvas para dibujar
        long tiempoComienzo; // Tiempo de inicio del ciclo
        long tiempoDiferencia; // Diferencia de tiempo entre el ciclo actual y el anterior
        int tiempoDormir; // Tiempo a dormir para mantener el FPS
        int framesAsaltar; // Número de frames saltados

        // Bucle principal del juego
        while (juegoEnEjecucion) {
            canvas = null; // Inicializa el canvas en null

            try { // Intenta dibujar
                canvas = surfaceHolder.lockCanvas(); // Intenta obtener el canvas
                synchronized (surfaceHolder) { // Sincroniza el acceso al canvas
                    tiempoComienzo = System.currentTimeMillis(); // Obtiene el tiempo actual
                    framesAsaltar = 0; // Restablece el contador de frames saltados

                    // 1) Actualiza la lógica del juego
                    juego.actualizar();

                    // 2) Comprueba si no hay enemigos => pasar de nivel
                    if (juego.sinEnemigos()) {
                        juego.pasarAlSiguienteNivel();
                    }

                    // 3) Renderiza en el canvas
                    juego.renderizar(canvas);

                    // 4) Control de FPS (sleep)
                    tiempoDiferencia = System.currentTimeMillis() - tiempoComienzo;
                    tiempoDormir = (int) (TIEMPO_FRAME - tiempoDiferencia);

                    if (tiempoDormir > 0) { // Si hay tiempo para dormir, espera
                        try { // Intenta dormir
                            Thread.sleep(tiempoDormir); // Espera el tiempo necesario
                        } catch (InterruptedException e) {} // Maneja la excepción
                    }

                    // Si el tiempo de dormir es negativo, salta un frame
                    while (tiempoDormir < 0 && framesAsaltar < MAX_FRAMES_SALTADOS) {
                        juego.actualizar(); // Actualiza el juego
                        tiempoDormir += TIEMPO_FRAME; // Aumenta el tiempo de dormir
                        framesAsaltar++; // Incrementa el contador de frames saltados
                    }
                }
            } finally { // Intenta liberar el canvas
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas); // Libera el canvas y muestra los cambios
                }
            }
        }
    }

    /**
     * Termina el juego.
     */
    public void fin() {
        juegoEnEjecucion = false; // Establece el juego como terminado
    }
}

