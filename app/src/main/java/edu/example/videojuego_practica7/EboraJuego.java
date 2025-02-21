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

import java.util.ArrayList;
import java.util.Iterator;

public class EboraJuego extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder surfaceHolder;
    private Thread gameThread;
    private Control derecha, izquierda, disparo, salto;  // Controles
    private boolean isRunning = false;
    private int pantallaAncho, pantallaAlto;

    // Datos del jugador (antes en Player)
    private Bitmap spriteSheet, fondoSprite;
    private float x, y;
    private int frameActual = 0;
    private final int totalFrames = 4;  // Solo los frames correspondientes a la dirección
    private int frameWidth, frameHeight;
    private int columnas = 12;  // 12 frames en total (6 hacia la derecha y 6 hacia la izquierda)
    private int filas = 1;     // Solo una fila
    private float velocidadX = 10f;
    private boolean moviendoDerecha = false;
    private boolean moviendoIzquierda = false;

    // Para el fondo
    private int fondoFrameAncho, fondoFrameAlto; // Dimensiones de cada fondo
    private int fondoColumnas = 3;  // Tienes 3 fondos en horizontal
    private int fondoFilas = 1;     // Solo 1 fila
    private int fondoActual = 0;    // Índice del fondo que se va a dibujar (0, 1 o 2)

    // Datos del fondo
    private Bitmap fondo;  // Imagen del fondo
    private float pos_inicial_mapa = 0;  // Posición inicial del fondo

    // Lista de disparos
    private ArrayList<Disparo> listaDisparos = new ArrayList<>();
    private final int FRAMES_ENTRE_DISPAROS = 20; // Cooldown entre disparos
    private int framesDesdeUltimoDisparo = 0;

    // Lista de enemigos (pompas)
    private ArrayList<Enemigo> listaPompas = new ArrayList<>();

    public EboraJuego(Context context) {
        super(context);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);  // Establece el listener
        setFocusable(true);  // Permite recibir eventos de teclado o pantalla táctil

        // Inicializar los controles
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
        // Obtener el tamaño de la pantalla
        pantallaAncho = getWidth();
        pantallaAlto = getHeight();

        // Inicializamos el sprite del jugador y su posición
        spriteSheet = BitmapFactory.decodeResource(getResources(), R.drawable.player);  // Carga el sprite del jugador

        // Escalar el bitmap
        float escala = 2.0f;
        int newWidth = (int) (spriteSheet.getWidth() * escala);
        int newHeight = (int) (spriteSheet.getHeight() * escala);
        spriteSheet = Bitmap.createScaledBitmap(spriteSheet, newWidth, newHeight, false);

        // Calcular tamaño de cada frame correctamente
        frameWidth = spriteSheet.getWidth() / columnas;
        frameHeight = spriteSheet.getHeight() / filas;

        // Posición inicial del personaje en pantalla
        x = 50;
        y = pantallaAlto - frameHeight;

        // Cargar el sprite con los 3 fondos
        fondoSprite = BitmapFactory.decodeResource(getResources(), R.drawable.fondo);

        // Calcular el ancho/alto de cada fondo
        fondoFrameAncho = fondoSprite.getWidth() / fondoColumnas;  // 3 columnas
        fondoFrameAlto = fondoSprite.getHeight() / fondoFilas;     // 1 fila

        // Ajustar la posición de los controles según el tamaño de la pantalla
        ajustarControles();

        // Inicia el hilo del juego
        gameThread = new Thread(this);
        isRunning = true;
        gameThread.start();

        // Spawnea una pompa grande al inicio
        spawnPompa();
    }

    private void ajustarControles() {
        float aux;

        // Flecha izquierda
        izquierda = new Control(getContext(), 50, pantallaAlto - 200);  // Posición en la parte inferior izquierda
        izquierda.cargarImagen(R.drawable.atras);  // Imagen de flecha izquierda
        izquierda.nombre = "IZQUIERDA";  // Nombre del control

        // Flecha derecha
        derecha = new Control(getContext(), 50 + izquierda.Ancho(), pantallaAlto - 200);  // Colocamos la flecha derecha justo al lado de la izquierda
        derecha.cargarImagen(R.drawable.alante);  // Imagen de flecha derecha
        derecha.nombre = "DERECHA";  // Nombre del control

        // Botón de disparo
        aux = pantallaAncho - 460;  // Coloca el botón de disparo en la parte inferior derecha, ajustado
        disparo = new Control(getContext(), aux, pantallaAlto - 200);  // Colocamos el botón de disparo
        disparo.cargarImagen(R.drawable.disparo);
        disparo.nombre = "DISPARO";  // Nombre del control

        // Botón de salto
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

            // Limpiar la pantalla con un color de fondo (negro)
            canvas.drawColor(Color.BLACK);

            // DIBUJAR EL FONDO
            int srcX = fondoFrameAncho * fondoActual;  // inicio en x
            int srcY = 0;                              // una sola fila
            Rect srcFondo = new Rect(srcX, srcY,
                    srcX + fondoFrameAncho,    // fin en x
                    srcY + fondoFrameAlto);    // fin en y

            // Escalamos el fondo a todo el ancho/alto de la pantalla
            Rect dstFondo = new Rect(0, 0, pantallaAncho, pantallaAlto);
            canvas.drawBitmap(fondoSprite, srcFondo, dstFondo, null);

            // DIBUJAR EL JUGADOR
            int srcXjug = frameWidth * frameActual;
            int srcYjug = 0;

            Rect srcJugador = new Rect(srcXjug, srcYjug,
                    srcXjug + frameWidth,
                    srcYjug + frameHeight);

            Rect dstJugador = new Rect((int) x, (int) (y - frameHeight),
                    (int) (x + frameWidth), (int) y);

            canvas.drawBitmap(spriteSheet, srcJugador, dstJugador, null);

            // DIBUJAR CONTROLES
            Paint paint = new Paint();
            derecha.dibujar(canvas, paint);
            izquierda.dibujar(canvas, paint);
            disparo.dibujar(canvas, paint);
            salto.dibujar(canvas, paint);
        }
    }

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
            frameActual = (frameActual + 1) % totalFrames + 4;  // Ciclo entre los 4 frames hacia la izquierda
        }
        if (!moviendoDerecha && !moviendoIzquierda) {
            frameActual = 11; // Si está quieto, mostrar el último frame de la fila
        }

        // Maneja los disparos
        framesDesdeUltimoDisparo++;
        if (disparo.pulsado && framesDesdeUltimoDisparo >= FRAMES_ENTRE_DISPAROS) {
            crearDisparo();
            framesDesdeUltimoDisparo = 0;
        }

        // Actualiza los disparos
        Iterator<Disparo> itDisparo = listaDisparos.iterator();
        while (itDisparo.hasNext()) {
            Disparo d = itDisparo.next();
            d.update();
            if (d.fueraDePantalla()) {
                itDisparo.remove();
            }
        }

        // Actualiza las pompas
        for (Enemigo pompa : listaPompas) {
            pompa.update();
        }

        // Verifica colisiones entre disparos y pompas
        verificarColisionDisparos();
    }

    private void drawGame(Canvas canvas) {
        canvas.drawColor(Color.WHITE);  // Limpia la pantalla

        // Dibuja el fondo
        int srcX = fondoFrameAncho * fondoActual;  // inicio en x
        int srcY = 0;                              // una sola fila
        Rect srcFondo = new Rect(srcX, srcY,
                srcX + fondoFrameAncho,    // fin en x
                srcY + fondoFrameAlto);    // fin en y

        // Escalamos el fondo a todo el ancho/alto de la pantalla
        Rect dstFondo = new Rect(0, 0, pantallaAncho, pantallaAlto);
        canvas.drawBitmap(fondoSprite, srcFondo, dstFondo, null);

        // Dibuja al jugador
        int srcXjug = frameWidth * frameActual;
        int srcYjug = 0;

        Rect srcJugador = new Rect(srcXjug, srcYjug,
                srcXjug + frameWidth,
                srcYjug + frameHeight);

        Rect dstJugador = new Rect((int) x, (int) (y - frameHeight),
                (int) (x + frameWidth), (int) y);

        canvas.drawBitmap(spriteSheet, srcJugador, dstJugador, null);

        // Dibuja los disparos
        for (Disparo d : listaDisparos) {
            d.draw(canvas);
        }

        // Dibuja las pompas
        for (Enemigo pompa : listaPompas) {
            pompa.draw(canvas);
        }

        // Dibuja los controles
        Paint paint = new Paint();
        derecha.dibujar(canvas, paint);
        izquierda.dibujar(canvas, paint);
        disparo.dibujar(canvas, paint);
        salto.dibujar(canvas, paint);
    }

    private void crearDisparo() {
        float disparoX = x + frameWidth / 2;
        float disparoY = y;
        listaDisparos.add(new Disparo(getContext(), this, disparoX, disparoY));
    }

    private void verificarColisionDisparos() {
        Iterator<Disparo> itDisparo = listaDisparos.iterator();
        while (itDisparo.hasNext()) {
            Disparo d = itDisparo.next();
            Iterator<Enemigo> itPompa = listaPompas.iterator();
            while (itPompa.hasNext()) {
                Enemigo pompa = itPompa.next();
                if (d.colisionaCon(pompa)) {
                    Enemigo[] nuevasPompas = pompa.dividir();
                    if (nuevasPompas != null) {
                        for (Enemigo p : nuevasPompas) {
                            listaPompas.add(p);
                        }
                    }
                    itPompa.remove();
                    itDisparo.remove();
                    break;
                }
            }
        }
    }

    private void spawnPompa() {
        int screenWidth = getWidth();
        float posX = (float) (Math.random() * (screenWidth - 100)) + 50;
        float posY = 0;
        int sizeLevel = 3; // Pompa grande
        Enemigo nuevaPompa = new Enemigo(getContext(), this, sizeLevel, posX, posY);
        listaPompas.add(nuevaPompa);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int ex = (int) event.getX();
        int ey = (int) event.getY();

        // Verifica qué control fue presionado
        derecha.comprueba_pulsado(ex, ey);
        izquierda.comprueba_pulsado(ex, ey);
        disparo.comprueba_pulsado(ex, ey);
        salto.comprueba_pulsado(ex, ey);

        // Al soltar, se comprueba que se hayan soltado los controles
        if (action == MotionEvent.ACTION_UP) {
            derecha.comprueba_soltado(null);
            izquierda.comprueba_soltado(null);
            disparo.comprueba_soltado(null);
            salto.comprueba_soltado(null);
        }
        return true;
    }

    public void eliminarDisparo(Disparo d) {
        listaDisparos.remove(d);
    }
}