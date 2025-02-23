package edu.example.videojuego_practica7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
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
    private final int totalFrames = 4;
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
    private Bitmap iconoVida;  // Imagen del fondo
    private float pos_inicial_mapa = 0;  // Posición inicial del fondo

    // Lista de disparos
    private ArrayList<Disparo> listaDisparos = new ArrayList<>();
    private final int FRAMES_ENTRE_DISPAROS = 20; // Cooldown entre disparos
    private int framesDesdeUltimoDisparo = 0;

    // Lista de enemigos (pompas)
    private ArrayList<Enemigo> listaPompas = new ArrayList<>();

    // Límites del movimiento del jugador
    private int limiteIzquierdo, limiteDerecho, limiteSuperior, limiteInferior;

    // Número de vidas del jugador
    private int vidas = 3;
    private boolean jugadorGolpeado = false;
    private int contadorGolpe = 0;    // Para temporizar cuánto dura el frame de golpe
    private final int TIEMPO_GOLPE = 180; // Frames que dura mostrando el golpe


    // Indica si el jugador está en el aire
    private boolean enElAire = false;
    // Velocidad vertical del jugador (hacia arriba negativa, hacia abajo positiva)
    private float velocidadY = 0f;
    // Gravedad que se aplicará en cada frame
    private final float GRAVEDAD = 1.5f;
    // Fuerza de salto inicial (negativa para saltar hacia arriba)
    private final float FUERZA_SALTO = -30f;
    // Suelo (coincide con el borde inferior donde los pies deben apoyarse)
    private float sueloY;

    // Control de niveles
    private int nivel = 1;          // Nivel actual (1, 2, 3, etc.)
    private int enemigosPorNivel = 5;   // Número de enemigos a spawnear en el primer nivel
    private int enemigosEliminados = 0; // Contador de enemigos eliminados

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

        // Indica el suelo como la parte inferior de la pantalla
        sueloY = pantallaAlto - frameHeight;
        y = sueloY; // El jugador inicia en el suelo

        nivel = 1;
        enemigosPorNivel = 5;  // o el número que quieras para el primer nivel
        enemigosEliminados = 0;
        fondoActual = 0;

        // Límites de movimiento
        limiteIzquierdo = 40;
        limiteDerecho = pantallaAncho - 40;
        limiteSuperior = 50;
        limiteInferior = pantallaAlto - 50;

        // (2) Cargar ícono de vida
        iconoVida = BitmapFactory.decodeResource(getResources(), R.drawable.vidas);

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
        disparo = new Control(getContext(), aux, pantallaAlto - 200);
        disparo.cargarImagen(R.drawable.disparo);
        disparo.nombre = "DISPARO";

        // Botón de salto
        aux = pantallaAncho - 250;  // Coloca el botón de salto justo a la izquierda de disparo
        salto = new Control(getContext(), aux, pantallaAlto - 200);
        salto.cargarImagen(R.drawable.salto);
        salto.nombre = "SALTO";
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) { }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        isRunning = false;
        try {
            gameThread.join();
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
            int srcX = fondoFrameAncho * fondoActual;
            int srcY = 0;
            Rect srcFondo = new Rect(srcX, srcY, srcX + fondoFrameAncho, srcY + fondoFrameAlto);

            Rect dstFondo = new Rect(0, 0, pantallaAncho, pantallaAlto);
            canvas.drawBitmap(fondoSprite, srcFondo, dstFondo, null);

            // DIBUJAR EL JUGADOR
            int srcXjug = frameWidth * frameActual;
            int srcYjug = 0;

            Rect srcJugador = new Rect(srcXjug, srcYjug, srcXjug + frameWidth, srcYjug + frameHeight);
            Rect dstJugador = new Rect((int) x, (int) (y - frameHeight), (int) (x + frameWidth), (int) y);
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

        // === SALTO: Igual que antes ===
        if (salto.pulsado && !enElAire) {
            enElAire = true;
            velocidadY = FUERZA_SALTO;
        }
        if (enElAire) {
            y += velocidadY;
            velocidadY += GRAVEDAD;
            if (y >= sueloY) {
                y = sueloY;
                enElAire = false;
                velocidadY = 0;
            }
        }

        // === MOVIMIENTO HORIZONTAL (SIEMPRE PERMITIDO) ===
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

        // === ELECCIÓN DE FRAME SEGÚN DIRECCIÓN (BASE FRAME) ===
        int baseFrame;
        if (moviendoDerecha) {
            x += velocidadX;       // Mover derecha
            baseFrame = (frameActual + 1) % totalFrames; // frames 0..3
        } else if (moviendoIzquierda) {
            x -= velocidadX;       // Mover izquierda
            baseFrame = ((frameActual + 1) % totalFrames) + 4; // frames 4..7
        } else {
            baseFrame = 11;        // quieto
        }

        // === LÍMITES ===
        if (x < limiteIzquierdo) {
            x = limiteIzquierdo;
        }
        if (x + frameWidth > limiteDerecho) {
            x = limiteDerecho - frameWidth;
        }
        // Límites verticales, etc.
        if (y - frameHeight < limiteSuperior) {
            y = limiteSuperior + frameHeight;
        }
        if (y > limiteInferior) {
            y = limiteInferior;
        }

        // === DISPAROS ===
        framesDesdeUltimoDisparo++;
        if (disparo.pulsado && framesDesdeUltimoDisparo >= FRAMES_ENTRE_DISPAROS) {
            crearDisparo();
            framesDesdeUltimoDisparo = 0;
        }

        // === SI ESTÁ GOLPEADO, PARPADEO E IGNORA COLISIONES ===
        if (jugadorGolpeado) {
            contadorGolpe++;

            // A cada 10 frames alternamos visible/invisible
            int fase = (contadorGolpe / 10) % 2;
            if (fase == 0) {
                // Encedemos sprite => Usamos el frame baseFrame que calculamos al mover
                frameActual = baseFrame;
            } else {
                // Apagamos sprite => -1
                frameActual = -1;
            }

            // Tras 6 seg (180 frames), salir de golpeado
            if (contadorGolpe >= TIEMPO_GOLPE) {
                terminarParpadeo();
            }

            // NO verificar colisiones => inmune
            // NO return aquí => deja que el enemigo y disparos se actualicen
        } else {
            // === NO ESTÁ GOLPEADO => actualizamos frame con el baseFrame normal ===
            frameActual = baseFrame;

            // === Verificar colision con jugador (ya no es inmune) ===
            verificarColisionJugador();
        }

        // Actualizar disparos
        Iterator<Disparo> itDisparo = listaDisparos.iterator();
        while (itDisparo.hasNext()) {
            Disparo d = itDisparo.next();
            d.update();
            if (d.fueraDePantalla()) {
                itDisparo.remove();
            }
        }

        // Actualizar pompas
        for (Enemigo pompa : listaPompas) {
            pompa.update();
        }
        // Verificar colisiones de disparos
        verificarColisionDisparos();
        verificarColisionJugador();
    }

    private void drawGame(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        // Dibuja el fondo
        int srcX = fondoFrameAncho * fondoActual;
        int srcY = 0;
        Rect srcFondo = new Rect(srcX, srcY, srcX + fondoFrameAncho, srcY + fondoFrameAlto);
        Rect dstFondo = new Rect(0, 0, pantallaAncho, pantallaAlto);
        canvas.drawBitmap(fondoSprite, srcFondo, dstFondo, null);

        /* Jugador
        int srcXjug = frameWidth * frameActual;
        int srcYjug = 0;
        Rect srcJugador = new Rect(srcXjug, srcYjug, srcXjug + frameWidth, srcYjug + frameHeight);
        Rect dstJugador = new Rect((int) x, (int) (y - frameHeight), (int) (x + frameWidth), (int) y);
        canvas.drawBitmap(spriteSheet, srcJugador, dstJugador, null);
*/

        if (frameActual != -1) {
            int srcXjug = frameWidth * frameActual;
            int srcYjug = 0;
            Rect srcJugador = new Rect(srcXjug, srcYjug, srcXjug + frameWidth, srcYjug + frameHeight);
            Rect dstJugador = new Rect((int) x, (int) (y - frameHeight),
                    (int) (x + frameWidth), (int) y);
            canvas.drawBitmap(spriteSheet, srcJugador, dstJugador, null);
        }

        // Disparos
        for (Disparo d : listaDisparos) {
            d.draw(canvas);
        }

        // Pompas
        for (Enemigo pompa : listaPompas) {
            pompa.draw(canvas);
        }

        // Controles
        Paint paint = new Paint();
        derecha.dibujar(canvas, paint);
        izquierda.dibujar(canvas, paint);
        disparo.dibujar(canvas, paint);
        salto.dibujar(canvas, paint);

        //  Dibuja el HUD de vidas
        dibujarVidas(canvas);
    }

    private void terminarParpadeo() {
        jugadorGolpeado = false;
        contadorGolpe = 0;
        // Pones un frame neutro, p.e. 11 (quieto)
        frameActual = 11;
    }

    private void dibujarVidas(Canvas canvas) {
        if (iconoVida == null) return;

        // Posición donde empieza a dibujar las vidas
        int offsetX = 20;
        int offsetY = 20;
        // Espacio entre íconos
        int separacion = 10;
        // Ancho del icono
        int anchoVida = iconoVida.getWidth();

        for (int i = 0; i < vidas; i++) {
            int xPos = offsetX + i * (anchoVida + separacion);
            int yPos = offsetY;
            canvas.drawBitmap(iconoVida, xPos, yPos, null);
        }
    }

    private void crearDisparo() {
        float disparoX = x + (frameWidth / 2);
        float disparoY = y - frameHeight;
        listaDisparos.add(new Disparo(getContext(), this, disparoX, disparoY));

        // Reproducir sonido "disparo.mp3" (colocado en res/raw)
        MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.disparo);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mp.start();
    }


    // Nueva función
    private void verificarColisionJugador() {
        if (jugadorGolpeado) {
            // Está parpadeando/inmune, no colisiona
            return;
        }

        // JugadorRect
        Rect jugadorRect = new Rect(
                (int)x,
                (int)(y - frameHeight),
                (int)(x + frameWidth),
                (int)y
        );

        // Recorre pompas
        for (Enemigo pompa : listaPompas) {
            Rect pompaRect = new Rect(
                    (int)pompa.getX(),
                    (int)pompa.getY(),
                    (int)(pompa.getX() + pompa.getAncho()),
                    (int)(pompa.getY() + pompa.getAlto())
            );

            if (Colision.rectsOverlap(jugadorRect, pompaRect)) {
                quitarVida();
                // Podrías hacer rebotar la pompa o lo que desees.
                break; // Con break evita quitar más de una vida en un frame
            }
        }
    }

    // **** ARREGLO con listas temporales para evitar ConcurrentModificationException ****
    private void verificarColisionDisparos() {
        ArrayList<Enemigo> pompasNuevas = new ArrayList<>();
        ArrayList<Enemigo> pompasEliminar = new ArrayList<>();
        ArrayList<Disparo> disparosEliminar = new ArrayList<>();

        for (Disparo d : listaDisparos) {
            for (Enemigo pompa : listaPompas) {
                if (d.colisionaCon(pompa)) {
                    // Reproducir el sonido de "burbuja_pop.mp3" al dividir la pompa
                    MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.burbuja_pop);
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                        }
                    });
                    mp.start();

                    // Dividir la pompa
                    Enemigo[] splitted = pompa.dividir();
                    if (splitted != null) {
                        for (Enemigo e : splitted) {
                            pompasNuevas.add(e);
                        }
                    }
                    pompasEliminar.add(pompa);
                    disparosEliminar.add(d);
                    // Rompe el bucle de pompas para este disparo
                    // Incrementamos el contador de enemigos eliminados

                    break;
                }
            }
        }
        // Eliminamos las pompas y disparos marcados
        listaPompas.removeAll(pompasEliminar);
        listaDisparos.removeAll(disparosEliminar);
        // Añadimos las nuevas pompas resultantes de dividir
        listaPompas.addAll(pompasNuevas);

        // Si ya no quedan enemigos en pantalla, pasar al siguiente nivel
        if (listaPompas.isEmpty()) {
            pasarAlSiguienteNivel();
        }
    }

    private void pasarAlSiguienteNivel() {
        nivel++;
        // Cambia el fondo: asumiendo que el sprite de fondo tiene sus frames en orden,
        // el nivel 2 utilizará el segundo frame (índice 1), etc.
        fondoActual = nivel - 1;  // Por ejemplo: nivel 2 -> fondoActual=1

        // Incrementa la cantidad de enemigos (puedes ajustar la fórmula)
        enemigosPorNivel += 5;
        // Resetea el contador de enemigos eliminados
        enemigosEliminados = 0;

        // Opcional: limpia la lista de pompas (si deseas reiniciar la acción)
        listaPompas.clear();

        // Spawnea los nuevos enemigos para el nuevo nivel
        for (int i = 0; i < enemigosPorNivel; i++) {
            spawnPompa();
        }
    }


    private void quitarVida() {
        if (!jugadorGolpeado && vidas > 0) {
            // Reducir vida ahora
            vidas--;

            // Si llega a 0, fin
            if (vidas <= 0) {
                finDelJuego();
                return;
            }

            // Activar el modo golpe
            jugadorGolpeado = true;
            contadorGolpe = 0;
            // Fijar el frame #10 de inmediato
            frameActual = 11;
            // También detendremos la animación normal por 3 seg en 'actualizar()'
        }
    }

    // Fin de juego: pausar hilo o mostrar "Game Over"
    private void finDelJuego() {
        isRunning = false;
        // Podrías pasar a otra Activity, mostrar un dialog, etc.
    }


    private void spawnPompa() {
        int screenWidth = getWidth();
        float posX = (float) (Math.random() * (screenWidth - 100)) + 50;
        float posY = 0;
        int sizeLevel = 3; // Pompa grande

        // Factor de velocidad según el nivel, p.ej. un 10% extra por nivel
        float velocidadExtra = 1.0f + (0.1f * (nivel - 1));

        Enemigo nuevaPompa = new Enemigo(getContext(), this, sizeLevel, posX, posY, velocidadExtra);
        listaPompas.add(nuevaPompa);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int ex = (int) event.getX();
        int ey = (int) event.getY();

        derecha.comprueba_pulsado(ex, ey);
        izquierda.comprueba_pulsado(ex, ey);
        disparo.comprueba_pulsado(ex, ey);
        salto.comprueba_pulsado(ex, ey);

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