package edu.example.videojuego_practica7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class EboraJuego extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder surfaceHolder;
    private Thread gameThread;
    private Control derecha, izquierda, disparo, salto;  // Controles
    private boolean isRunning = false;
    private int pantallaAncho, pantallaAlto;

    // Datos del jugador (antes en Player)
    private Bitmap spriteSheet;
    private float x, y;
    private int frameActual = 0;
    private int contadorFrames = 0;
    private final int totalFrames = 4;  // Solo los frames correspondientes a la dirección
    private int frameWidth, frameHeight;
    private int columnas = 12;  // 12 frames en total (6 hacia la derecha y 6 hacia la izquierda)
    private int filas = 1;     // Solo una fila

    private float velocidadX = 10f;
    private boolean moviendoDerecha = false;
    private boolean moviendoIzquierda = false;

    // Datos del fondo
    private Bitmap fondo;  // Imagen del fondo
    private float pos_inicial_mapa = 0;  // Posición inicial del fondo

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
        // Inicializamos el sprite del jugador y su posición
        spriteSheet = BitmapFactory.decodeResource(getResources(), R.drawable.player);  // Carga el sprite del jugador

        // Calcular tamaño de cada frame correctamente
        frameWidth = spriteSheet.getWidth() / columnas;
        frameHeight = spriteSheet.getHeight() / filas;

        // Posición inicial del personaje en pantalla
        x = 100;
        y = 300;

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
                    actualizar();
                    drawGame(canvas);
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    public void renderizar(Canvas canvas) {
        if (canvas != null) {
            Paint mypaint = new Paint();
            mypaint.setStyle(Paint.Style.STROKE);

            // Limpiar la pantalla con un color de fondo (negro en este caso)
            canvas.drawColor(Color.BLACK);

            // Dibujar el fondo
            canvas.drawBitmap(fondo, 0, 0, null);  // Dibujamos el fondo

            // Dibujar al jugador
            int srcX = frameWidth * frameActual;
            int srcY = 0; // Solo hay una fila

            Rect src = new Rect(srcX, srcY, srcX + frameWidth, srcY + frameHeight);
            Rect dst = new Rect((int) x, (int) (y - frameHeight), (int) (x + frameWidth), (int) y);
            canvas.drawBitmap(spriteSheet, src, dst, null);  // Dibuja el jugador

            // Dibuja los controles
            Paint paint = new Paint();
            derecha.dibujar(canvas, paint);
            izquierda.dibujar(canvas, paint);
            disparo.dibujar(canvas, paint);
            salto.dibujar(canvas, paint);
        }
    }

    // Actualiza el estado del juego
    public void actualizar() {
        // Mueve al jugador según los controles
        if (derecha.pulsado) {
            moviendoDerecha = true;
            moviendoIzquierda = false;
        } else if (izquierda.pulsado) {
            moviendoIzquierda = true;
            moviendoDerecha = false;
        } else {
            moviendoDerecha = false;
            moviendoIzquierda = false;
        }

        // Actualiza la animación del jugador
        if (moviendoDerecha) {
            x += velocidadX;
            frameActual = (frameActual + 1) % totalFrames;  // Ciclo entre los 4 frames hacia la derecha
        } else if (moviendoIzquierda) {
            x -= velocidadX;
            frameActual = (frameActual + 1) % totalFrames + 4;  // Ciclo entre los 4 frames hacia la izquierda (los frames de la derecha son de 0 a 3, los de izquierda de 4 a 7)
        }

        if (!moviendoDerecha && !moviendoIzquierda) {
            frameActual = 11; // Si está quieto, mostrar el último frame de la fila
        }
    }

    private void drawGame(Canvas canvas) {
        canvas.drawColor(Color.WHITE);  // Limpia la pantalla

        // Dibujar el jugador
        int srcX = frameWidth * frameActual;
        int srcY = 0; // Solo hay una fila

        Rect src = new Rect(srcX, srcY, srcX + frameWidth, srcY + frameHeight);
        Rect dst = new Rect((int) x, (int) (y - frameHeight), (int) (x + frameWidth), (int) y);
        canvas.drawBitmap(spriteSheet, src, dst, null);  // Dibuja el jugador

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
