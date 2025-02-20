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
    private Player player;  // Usará la nueva versión de Player
    private Control derecha, izquierda, disparo, salto;  // Controles
    private boolean isRunning = false;
    private int pantallaAncho, pantallaAlto;


    public EboraJuego(Context context) {
        super(context);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);  // Establece el listener
        setFocusable(true);  // Permite recibir eventos de teclado o pantalla táctil

        // Inicializar los controles (ajusta las posiciones según sea necesario)
        derecha = new Control(context, 0, 0);
        derecha.cargarImagen(R.drawable.alante);
        izquierda = new Control(context, 0, 0);
        izquierda.cargarImagen(R.drawable.atras);
        disparo = new Control(context, 0, 0);
        disparo.cargarImagen(R.drawable.disparo);
        salto = new Control(context, 0, 0);
        salto.cargarImagen(R.drawable.salto);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        player = new Player(getContext());  // Crea el personaje con la nueva lógica

        // Obtén el tamaño de la pantalla
        pantallaAncho = getWidth();
        pantallaAlto = getHeight();

        // Ajustar la posición de los controles según el tamaño de la pantalla
        ajustarControles();

        // Inicia el hilo del juego
        gameThread = new Thread(this);
        isRunning = true;
        gameThread.start();
    }

    private void ajustarControles() {
        float aux;

        //flecha_izda
        izquierda = new Control(getContext(), 50, pantallaAlto - 200);  // Posición en la parte inferior izquierda
        izquierda.cargarImagen(R.drawable.atras);  // Imagen de flecha izquierda
        izquierda.nombre = "IZQUIERDA";  // Nombre del control

        //flecha_derecha
        derecha = new Control(getContext(), 50 + izquierda.Ancho(), pantallaAlto - 200);  // Colocamos la flecha derecha justo al lado de la izquierda
        derecha.cargarImagen(R.drawable.alante);  // Imagen de flecha derecha
        derecha.nombre = "DERECHA";  // Nombre del control

        //disparo (colocamos en el 5/7 del ancho)
        aux = pantallaAncho - 460;  // Coloca el botón de disparo en la parte inferior derecha, ajustado
        disparo = new Control(getContext(), aux, pantallaAlto - 200);  // Colocamos el botón de disparo
        disparo.cargarImagen(R.drawable.disparo);
        disparo.nombre = "DISPARO";  // Nombre del control

        //salto (colocamos en el 4/7 del ancho)
        aux = pantallaAncho - 250;  // Coloca el botón de salto justo a la izquierda de disparo
        salto = new Control(getContext(), aux, pantallaAlto - 200);  // Colocamos el botón de salto
        salto.cargarImagen(R.drawable.salto);
        salto.nombre = "SALTO";  // Nombre del control
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) { }

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
                    updateGame();
                    drawGame(canvas);
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    private void updateGame() {
        // Mueve al jugador según los controles
        if (derecha.pulsado) {
            player.moveRight();
        } else if (izquierda.pulsado) {
            player.moveLeft();
        } else {
            player.stopMoving();
        }
        // Actualiza la animación del jugador
        player.update();
    }

    private void drawGame(Canvas canvas) {
        canvas.drawColor(Color.WHITE);  // Limpia la pantalla
        player.draw(canvas);             // Dibuja el jugador

        // Dibuja los controles
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

        // Verifica qué control fue presionado
        derecha.comprueba_pulsado(x, y);
        izquierda.comprueba_pulsado(x, y);
        disparo.comprueba_pulsado(x, y);
        salto.comprueba_pulsado(x, y);

        // Al soltar, se comprueba que se hayan soltado los controles
        if (action == MotionEvent.ACTION_UP) {
            derecha.comprueba_soltado(null);
            izquierda.comprueba_soltado(null);
            disparo.comprueba_soltado(null);
            salto.comprueba_soltado(null);
        }
        return true;
    }
}
