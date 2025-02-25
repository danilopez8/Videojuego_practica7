package edu.example.videojuego_practica7;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.graphics.Canvas;

import android.view.SurfaceHolder;

public class BucleJuego extends Thread{
    private EboraJuego juego;
    private SurfaceHolder surfaceHolder;
    private boolean juegoEnEjecucion = true;

    public static final int MAX_FPS = 30;
    public static final int TIEMPO_FRAME = 1000 / MAX_FPS; // ~33ms
    public static final int MAX_FRAMES_SALTADOS = 5;

    public BucleJuego(SurfaceHolder sh, EboraJuego s) {
        juego = s;
        surfaceHolder = sh;
    }


    public void run() {
        Canvas canvas;
        long tiempoComienzo;
        long tiempoDiferencia;
        int tiempoDormir;
        int framesAsaltar;

        while (juegoEnEjecucion) {
            canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    tiempoComienzo = System.currentTimeMillis();
                    framesAsaltar = 0;

                    // 1) Actualiza la lógica del juego
                    juego.actualizar();

                    // 2) Comprueba si no hay enemigos => pasar de nivel
                    if (juego.sinEnemigos()) {
                        juego.pasarAlSiguienteNivel();
                    }

                    // 3) Renderiza en el canvas
                    juego.renderizar(canvas);

                    // 4) Control de FPS
                    tiempoDiferencia = System.currentTimeMillis() - tiempoComienzo;
                    tiempoDormir = (int) (TIEMPO_FRAME - tiempoDiferencia);

                    if (tiempoDormir > 0) {
                        try {
                            Thread.sleep(tiempoDormir);
                        } catch (InterruptedException e) {}
                    }

                    while (tiempoDormir < 0 && framesAsaltar < MAX_FRAMES_SALTADOS) {
                        juego.actualizar();
                        tiempoDormir += TIEMPO_FRAME;
                        framesAsaltar++;
                    }
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    public void fin() {
        juegoEnEjecucion = false;
    }
}

