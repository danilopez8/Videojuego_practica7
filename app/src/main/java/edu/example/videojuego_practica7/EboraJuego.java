package edu.example.videojuego_practica7;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;




    public class EboraJuego extends SurfaceView implements SurfaceHolder.Callback, Runnable {

        private SurfaceHolder surfaceHolder;
        private Thread gameThread;
        private Player player;  // Instancia del personaje
        private Control derecha, izquierda, disparo, salto;  // Controles
        private boolean isRunning = false;

        public EboraJuego(Context context) {
            super(context);
            surfaceHolder = getHolder();
            surfaceHolder.addCallback(this);  // Establece el listener
            setFocusable(true);  // Permite recibir eventos de teclado o pantalla táctil

            // Inicializar los controles
            derecha = new Control(context, 50, 500);  // Ajusta las coordenadas de cada control
            derecha.cargarImagen(R.drawable.alante);  // Carga la imagen de "Derecha"
            izquierda = new Control(context, 150, 500);
            izquierda.cargarImagen(R.drawable.atras);  // Carga la imagen de "Izquierda"
            disparo = new Control(context, 300, 500);
            disparo.cargarImagen(R.drawable.disparo);  // Carga la imagen de "Disparo"
            salto = new Control(context, 450, 500);
            salto.cargarImagen(R.drawable.salto);  // Carga la imagen de "Salto"


        }

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            player = new Player(getContext());  // Crea el personaje

            // Inicia el hilo del juego
            gameThread = new Thread(this);
            isRunning = true;
            gameThread.start();
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            isRunning = false;
            try {
                gameThread.join();  // Espera a que el hilo del juego termine
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (isRunning) {
                Canvas canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas();
                    synchronized (surfaceHolder) {
                        // Lógica de actualización del juego
                        updateGame();
                        // Dibuja el juego
                        drawGame(canvas);
                    }
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);  // Publica el lienzo
                    }
                }
            }
        }

        // Método para actualizar el estado del juego (movimiento, etc.)
        private void updateGame() {
            player.update();  // Actualiza la posición del personaje

            // Verificar si el jugador presionó los controles
            if (derecha.pulsado) {
                player.moveRight();
            }
            if (izquierda.pulsado) {
                player.moveLeft();
            }
            if (disparo.pulsado) {
                player.shoot();
            }
            if (salto.pulsado) {
                player.jump();
            }
        }

        // Método para dibujar el juego (personaje, fondo, etc.)
        private void drawGame(Canvas canvas) {
            canvas.drawColor(Color.WHITE);  // Limpiar la pantalla con un color blanco
            player.draw(canvas);  // Dibuja al personaje

            // Dibujar los controles
            Paint paint = new Paint();
            derecha.dibujar(canvas, paint);
            izquierda.dibujar(canvas, paint);
            disparo.dibujar(canvas, paint);
            salto.dibujar(canvas, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int action = event.getAction();
            int x = (int) event.getX();
            int y = (int) event.getY();

            // Verificar qué control fue presionado
            derecha.comprueba_pulsado(x, y);
            izquierda.comprueba_pulsado(x, y);
            disparo.comprueba_pulsado(x, y);
            salto.comprueba_pulsado(x, y);

            // Al soltar el toque, comprobar si se soltaron los controles
            if (action == MotionEvent.ACTION_UP) {
                derecha.comprueba_soltado(null);
                izquierda.comprueba_soltado(null);
                disparo.comprueba_soltado(null);
                salto.comprueba_soltado(null);
            }

            return true;
        }
}
