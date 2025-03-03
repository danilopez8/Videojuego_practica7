package edu.example.videojuego_practica7;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Clase principal del juego.
 */
public class EboraJuego extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder surfaceHolder; // Holder de la superficie
    private Thread gameThread; // Hilo del juego
    private Control derecha, izquierda, disparo, salto;  // Controles
    private boolean isRunning = false; // Indica si el juego está en ejecución
    private int pantallaAncho, pantallaAlto; // Dimensiones de la pantalla

    // Datos del jugador (antes en Player)
    private Bitmap spriteSheet, fondoSprite; // Sprites del jugador y fondo
    private float x, y; // Coordenadas del jugador
    private int frameActual = 0; // Frame actual del sprite
    private final int totalFrames = 4; // Total de frames en el sprite
    private int frameWidth, frameHeight; // Ancho y alto de cada frame
    private int columnas = 12;  // 12 frames en total (6 hacia la derecha y 6 hacia la izquierda)
    private int filas = 1;     // Solo una fila
    private float velocidadX = 15f; // Velocidad de movimiento horizontal
    private boolean moviendoDerecha = false; // Indica si el jugador está moviéndose a la derecha
    private boolean moviendoIzquierda = false; // Indica si el jugador está moviéndose a la izquierda

    // Para el fondo
    private int fondoFrameAncho, fondoFrameAlto; // Dimensiones de cada fondo
    private int fondoColumnas = 3;  // Tienes 3 fondos en horizontal
    private int fondoFilas = 1;     // Solo 1 fila
    private int fondoActual = 0;    // Índice del fondo que se va a dibujar (0, 1 o 2)

    // Datos del fondo
    private Bitmap iconoVida;  // Imagen del fondo
    private float pos_inicial_mapa = 0;  // Posición inicial del fondo

    // Lista de disparos
    private ArrayList<Disparo> listaDisparos = new ArrayList<>(); // Lista de disparos
    private final int FRAMES_ENTRE_DISPAROS = 20; // Cooldown entre disparos
    private int framesDesdeUltimoDisparo = 0; // Frames desde el último disparo

    // Lista de enemigos (pompas)
    private ArrayList<Enemigo> listaPompas = new ArrayList<>(); // Lista de enemigos (pompas)

    // Límites del movimiento del jugador
    int limiteIzquierdo;
    int limiteDerecho;
    int limiteSuperior;
    int limiteInferior;

    // Número de vidas del jugador
    private int vidas = 3;
    private boolean jugadorGolpeado = false;    // Indica si el jugador está golpeado
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
    private int enemigosPorNivel = 8;   // Número de enemigos a spawnear en el primer nivel
    private int enemigosEliminados = 0; // Contador de enemigos eliminados
    private Bitmap nivelesImagen;
    private boolean mostrandoNivel = false;
    private int contadorTransicion = 0;
    private final int DURACION_TRANSICION = 120; // 2 segundos a 60 FPS
    private int alphaNivel = 0;  // Opacidad inicial (0 = invisible)
    private boolean fadeIn = true;  // Indica si estamos en la fase de aparición
    private final int FADE_STEP = 5;  // Cantidad de opacidad que aumenta/disminuye por frame
    private boolean puedeMoverse = false;

    // Tiempo máximo en segundos para cada nivel (ajusta a tu gusto)
    private int tiempoRestanteSegundos = 90;

    // Cálculo de frames totales (usando la misma tasa de FPS que BucleJuego)
    private int framesRestantes;

    private BucleJuego bucleJuego; // Hilo principal del juego

    /**
     * Constructor del juego.
     *
     * @param context Contexto de la aplicación
     */
    public EboraJuego(Context context) {
        super(context);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);  // Establece el listener
        setFocusable(true);  // Permite recibir eventos de teclado o pantalla táctil

        // Inicializar los controles
        derecha = new Control(context, 0, 0);
        derecha.cargarImagen(R.drawable.delante);
        izquierda = new Control(context, 0, 0);
        izquierda.cargarImagen(R.drawable.atras);
        disparo = new Control(context, 0, 0);
        disparo.cargarImagen(R.drawable.disparo);
        salto = new Control(context, 0, 0);
        salto.cargarImagen(R.drawable.salto);
    }

    /**
     * Callback cuando la superficie se crea.
     */
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        // Obtener el tamaño de la pantalla
        pantallaAncho = getWidth();
        pantallaAlto = getHeight();

        // Cargar la imagen de niveles
        nivelesImagen = BitmapFactory.decodeResource(getResources(), R.drawable.niveles_fondo);

        // Iniciar la transición desde el inicio del juego
        mostrandoNivel = true;
        contadorTransicion = 0;
        alphaNivel = 0;
        fadeIn = true;

        puedeMoverse = false; // Bloqueamos el movimiento del jugador

        framesRestantes = tiempoRestanteSegundos * BucleJuego.MAX_FPS; // Calculamos los frames totales

        // Definir el margen inferior que no usará el mapa (por ejemplo, 200 píxeles)
        int margenInferior = 200;

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

        // Posición inicial del jugador: sus pies deben estar en el "suelo" definido por el margen
        x = 50;
        sueloY = pantallaAlto - margenInferior;
        y = sueloY;  // El jugador comienza con sus pies en el suelo

        // (1) Definir el nivel
        nivel = 1;
        enemigosPorNivel = 8;
        enemigosEliminados = 0;
        fondoActual = 0;

        // Límites de movimiento
        limiteIzquierdo = 40;
        limiteDerecho = pantallaAncho - 40;
        limiteSuperior = 50;
        limiteInferior = pantallaAlto - 50;

        // (2) Cargar ícono de vida
        iconoVida = BitmapFactory.decodeResource(getResources(), R.drawable.vidas);
        iconoVida = Bitmap.createScaledBitmap(iconoVida, 80, 80, false);

        // Cargar el sprite con los 3 fondos
        fondoSprite = BitmapFactory.decodeResource(getResources(), R.drawable.fondo);

        // Calcular el ancho/alto de cada fondo
        fondoFrameAncho = fondoSprite.getWidth() / fondoColumnas;  // 3 columnas
        fondoFrameAlto = fondoSprite.getHeight() / fondoFilas;     // 1 fila

        // Ajustar la posición de los controles según el tamaño de la pantalla
        ajustarControles();

        // Iniciar el bucle principal
        bucleJuego = new BucleJuego(getHolder(), this);
        bucleJuego.start();

        // Spawnea una pompa grande al inicio
        spawnPompa();
    }

    /**
     * Ajusta la posición de los controles según el tamaño de la pantalla.
     */
    private void ajustarControles() {
        float aux;

        // Flecha izquierda
        izquierda = new Control(getContext(), 50, pantallaAlto - 200);  // Posición en la parte inferior izquierda
        izquierda.cargarImagen(R.drawable.atras);  // Imagen de flecha izquierda
        izquierda.nombre = "IZQUIERDA";  // Nombre del control

        // Flecha derecha
        derecha = new Control(getContext(), 50 + izquierda.Ancho(), pantallaAlto - 200);  // Colocamos la flecha derecha justo al lado de la izquierda
        derecha.cargarImagen(R.drawable.delante);  // Imagen de flecha derecha
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
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    /**
     * Callback cuando la superficie se destruye.
     */
    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        isRunning = false; // Indica que el juego no está en ejecución
        if (gameThread != null) { // Si el hilo existe
            try {
                gameThread.join(); // Espera a que termine
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Bucle principal del juego.
     */
    @Override
    public void run() {
        while (isRunning) { // Mientras el juego esté en ejecución
            Canvas canvas = null; // Canvas para dibujar
            try {
                canvas = surfaceHolder.lockCanvas(); // Intenta obtener el canvas
                synchronized (surfaceHolder) { // Sincroniza el acceso al canvas
                    actualizar(); // Actualiza el juego
                    renderizar(canvas); // Renderiza en el canvas
                }
            } finally { // Intenta liberar el canvas
                if (canvas != null) { // Si el canvas no es nulo
                    surfaceHolder.unlockCanvasAndPost(canvas); // Libera el canvas y muestra los cambios
                }
            }
        }
    }

    /**
     * Renderiza el juego en el lienzo.
     * @param canvas lienzo
     */
    public void renderizar(Canvas canvas) {
        // Actualiza el estado del juego
        if (canvas != null) {
            Paint mypaint = new Paint(); // Crea un objeto Paint para dibujar
            mypaint.setStyle(Paint.Style.STROKE); // Establece el estilo de dibujo como STROKE (líneas)

            // 1) Limpiar la pantalla
            canvas.drawColor(Color.BLACK);

            // 2) Dibujar el fondo
            int srcX = fondoFrameAncho * fondoActual;
            int srcY = 0;
            Rect srcFondo = new Rect(srcX, srcY, srcX + fondoFrameAncho, srcY + fondoFrameAlto);
            Rect dstFondo = new Rect(0, 0, pantallaAncho, pantallaAlto);
            canvas.drawBitmap(fondoSprite, srcFondo, dstFondo, null);


            // 4) Dibujar al jugador y las bolas (solo si 'puedeMoverse' es true)
            if (puedeMoverse) {
                // (a) Dibujar al jugador:
                int srcXjug = frameWidth * frameActual;
                int srcYjug = 0;
                Rect srcJugador = new Rect(srcXjug, srcYjug, srcXjug + frameWidth, srcYjug + frameHeight);
                // Si el jugador está en el aire, usamos su 'y' actual; de lo contrario, fijamos su base en el límite inferior
                int playerBottom = enElAire ? (int) y : limiteInferior;
                Rect dstJugador = new Rect((int) x, playerBottom - frameHeight, (int) (x + frameWidth), playerBottom);
                canvas.drawBitmap(spriteSheet, srcJugador, dstJugador, null);

                // (b) Dibujar disparos
                for (Disparo d : listaDisparos) {
                    d.draw(canvas);
                }

                // (c) Dibujar enemigos (bolas)
                for (Enemigo pompa : listaPompas) {
                    pompa.draw(canvas);
                }
            }

            // 5) Dibujar controles con transparencia (50% opacidad)
            Paint controlPaint = new Paint();
            controlPaint.setAlpha(128);
            derecha.dibujar(canvas, controlPaint);
            izquierda.dibujar(canvas, controlPaint);
            disparo.dibujar(canvas, controlPaint);
            salto.dibujar(canvas, controlPaint);

            // 6) Dibujar HUD de vidas
            dibujarVidas(canvas);

            // 7) Dibujar transición de niveles
            if (mostrandoNivel && nivelesImagen != null) {
                int seccionAncho = nivelesImagen.getWidth() / 3;
                int srcXNiv = (nivel - 1) * seccionAncho;
                Rect src = new Rect(srcXNiv, 0, srcXNiv + seccionAncho, nivelesImagen.getHeight());
                Rect dst = new Rect(0, 0, pantallaAncho, pantallaAlto);

                Paint paintNivel = new Paint();
                paintNivel.setAlpha(alphaNivel);

                canvas.drawBitmap(nivelesImagen, src, dst, paintNivel);

                if (fadeIn) { // Animación de aparición
                    alphaNivel += FADE_STEP;
                    if (alphaNivel >= 255) {
                        alphaNivel = 255;
                        fadeIn = false;
                    }
                } else { // Animación de desaparecimiento
                    alphaNivel -= FADE_STEP;
                    if (alphaNivel <= 0) {
                        mostrandoNivel = false;
                        puedeMoverse = true;

                        // Ahora iniciamos el nivel normalmente
                        framesRestantes = tiempoRestanteSegundos * BucleJuego.MAX_FPS;
                        fondoActual = nivel - 1;
                        enemigosPorNivel = nivel;
                        enemigosEliminados = 0;
                        listaPompas.clear();
                        for (int i = 0; i < enemigosPorNivel; i++) {
                            spawnPompa();
                        }
                    }
                }
            }
        }
    }

    /**
     * Actualiza el estado del juego.
     */
    public void actualizar() {

        if (salto.pulsado && !enElAire) { // Si el salto está pulsado y no está en el aire
            enElAire = true;
            velocidadY = FUERZA_SALTO;
        }

        if (enElAire) { // Si está en el aire
            y += velocidadY;
            velocidadY += GRAVEDAD;
            if (y >= sueloY) { // Si llega al suelo
                y = sueloY;
                enElAire = false;
                velocidadY = 0;
            }
        }

        framesRestantes--; // Decrementar el contador de frames del temporizador
        if (framesRestantes <= 0) {
            finDelJuego();// Se acabó el tiempo, fin del juego
            return; // Para que no siga actualizando este frame
        }

        //CONTROLES
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

        // ACTUALIZAR SPRITE
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

        //LÍMITES
        if (x < limiteIzquierdo) {
            x = limiteIzquierdo;
        }
        if (x + frameWidth > limiteDerecho) {
            x = limiteDerecho - frameWidth;
        }
        if (y - frameHeight < limiteSuperior) {
            y = limiteSuperior + frameHeight;
        }
        if (y > limiteInferior) {
            y = limiteInferior;
        }

        // DISPAROS
        framesDesdeUltimoDisparo++;
        if (disparo.pulsado && framesDesdeUltimoDisparo >= FRAMES_ENTRE_DISPAROS) {
            crearDisparo();
            framesDesdeUltimoDisparo = 0;
        }

        // SI ESTÁ GOLPEADO, PARPADEO E IGNORA COLISIONES
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
            // NO ESTÁ GOLPEADO => actualizamos frame con el baseFrame normal
            frameActual = baseFrame;

            // Verificar colision con jugador (ya no es inmune)
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

    /**
     * Termina el parpadeo del jugador.
     */
    private void terminarParpadeo() {
        jugadorGolpeado = false;
        contadorGolpe = 0;
        // Pones un frame neutro, p.e. 11 (quieto)
        frameActual = 11;
    }

    /**
     * Dibuja las vidas en el lienzo.
     * @param canvas lienzo
     */
    private void dibujarVidas(Canvas canvas) {
        if (iconoVida == null) return;

        int separacion = 10; // Espacio entre iconos de vida
        int anchoVida = iconoVida.getWidth(); // Ancho de cada icono de vida
        int offsetX = 800; // Posición horizontal inicial
        int offsetY = 100; // Posición vertical inicial

        for (int i = 0; i < vidas; i++) { // Recorre cada vida

            int xPos = offsetX + i * (anchoVida + separacion); // Posición horizontal
            int yPos = offsetY; // Posición vertical
            canvas.drawBitmap(iconoVida, xPos, yPos, null); // Dibuja el icono de vida

            // Convertir frames restantes a segundos para el temporizador
            int segundosRestantes = framesRestantes / BucleJuego.MAX_FPS;

            // Posicionar el temporizador a la derecha de los iconos de vida
            int xTimer = offsetX + (vidas * (anchoVida + separacion)) + 50;
            int yTimer = offsetY + 70;  // Un poco más abajo de los iconos

            // Dibujar el temporizador
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(60);

            canvas.drawText("Tiempo: " + segundosRestantes, xTimer, yTimer, paint);
        }
    }

    /**
     * Crea un nuevo disparo.
     */
    private void crearDisparo() {
        float disparoX = x + (frameWidth / 2f); // Centro del jugador
        float playerBottom = enElAire ? y : limiteInferior; // Si está en el aire, usamos el suelo; de lo contrario, usamos el límite inferior
        float disparoY = playerBottom - (frameHeight * 0.8f); // Disparo ligeramente arriba del jugador
        listaDisparos.add(new Disparo(getContext(), this, disparoX, disparoY)); // Añade el disparo a la lista

        // Reproducir sonido "disparo.mp3"
        MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.disparo);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release(); // Libera el recurso de audio
            }
        });
        mp.start();
    }

    /**
     * Verifica colisiones con el jugador.
     */
    private void verificarColisionJugador() {
        if (jugadorGolpeado) {
            // Si el jugador está en modo inmune, no se detectan colisiones.
            return;
        }

        // Usamos la misma lógica que en renderizar para definir el rectángulo del jugador.
        int playerBottom = enElAire ? (int) y : limiteInferior;
        Rect jugadorRect = new Rect(
                (int) x,
                (int) (playerBottom - frameHeight),
                (int) (x + frameWidth),
                playerBottom
        );

        // Recorre cada bola (enemigo)
        for (Enemigo pompa : listaPompas) {
            Rect pompaRect = new Rect(
                    (int) pompa.getX(),
                    (int) pompa.getY(),
                    (int) (pompa.getX() + pompa.getAncho()),
                    (int) (pompa.getY() + pompa.getAlto())
            );
            if (Rect.intersects(jugadorRect, pompaRect)) {
                quitarVida();
                // Salir del bucle para evitar quitar más de una vida en el mismo frame.
                break;
            }
        }
    }

    /**
     * Verifica colisiones de disparos.
     */
    private void verificarColisionDisparos() {
        ArrayList<Enemigo> pompasNuevas = new ArrayList<>(); // Lista para almacenar nuevas pompas
        ArrayList<Enemigo> pompasEliminar = new ArrayList<>(); // Lista para almacenar pompas a eliminar
        ArrayList<Disparo> disparosEliminar = new ArrayList<>(); // Lista para almacenar disparos a eliminar

        for (Disparo d : listaDisparos) {
            for (Enemigo pompa : listaPompas) {
                if (d.colisionaCon(pompa)) {
                    // Reproducir sonido de "burbuja_pop.mp3" al dividir la pompa
                    MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.burbuja_pop);
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                        }
                    });
                    mp.start();

                    // Dividir la pompa
                    Enemigo[] nuevasPompas = pompa.dividir();
                    if (nuevasPompas != null) {
                        for (Enemigo e : nuevasPompas) {
                            pompasNuevas.add(e);
                        }
                    }

                    pompasEliminar.add(pompa);
                    disparosEliminar.add(d);

                    break;// Rompe el bucle de pompas para este disparo
                }
            }
        }
        listaPompas.removeAll(pompasEliminar); // Elimina las pompas eliminadas
        listaDisparos.removeAll(disparosEliminar); // Elimina los disparos eliminados
        listaPompas.addAll(pompasNuevas); // Añade las nuevas pompas a la lista

    }

    /**
     * Verifica si no quedan enemigos.
     * @return true si no quedan enemigos, false en caso contrario
     */
    public boolean sinEnemigos() {
        return listaPompas.isEmpty();
    }

    /**
     * Pasa al siguiente nivel.
     */
    public void pasarAlSiguienteNivel() {
        nivel++;
        if (nivel > 3) {  // Si ya completó 3 niveles
            ganar(); // Fin del juego
            return;
        }

        // Animación de transición de niveles
        mostrandoNivel = true;
        contadorTransicion = 0;
        alphaNivel = 0;
        fadeIn = true;
        puedeMoverse = false;

        // Reiniciamos el temporizador
        framesRestantes = tiempoRestanteSegundos * BucleJuego.MAX_FPS;

        // Reiniciamos el fondo
        fondoActual = nivel - 1;
        enemigosPorNivel = (nivel * 1) + 0;
        enemigosEliminados = 0;
        listaPompas.clear();
        for (int i = 0; i < enemigosPorNivel; i++) {
            spawnPompa();
        }
    }

    /**
     * Quita una vida al jugador.
     */
    private void quitarVida() {
        if (!jugadorGolpeado && vidas > 0) {
            vidas--; // Quitar una vida

            if (vidas <= 0) { // Si no quedan vidas
                finDelJuego(); // Fin del juego
                return;
            }

            // Animación de golpe
            jugadorGolpeado = true;
            contadorGolpe = 0;
            frameActual = 11;
        }
    }

    /**
     * Crea una nueva bomba.
     */
    private void spawnPompa() {
        int screenWidth = getWidth(); // Ancho de la pantalla
        float posX = (float) (Math.random() * (screenWidth - 100)) + 50;// Posición aleatoria en el eje X
        float posY = 0; // Posición inicial en el eje Y
        int sizeLevel = 3; // Tamaño de la bomba según el nivel
        float velocidadExtra = 1.5f + (0.3f * (nivel - 1)); // Velocidad extra según el nivel
        Enemigo nuevaPompa = new Enemigo(getContext(), this, sizeLevel, posX, posY, velocidadExtra); // Crea una nueva pompa
        listaPompas.add(nuevaPompa); // Añade la nueva bomba a la lista
    }


    /**
     * Maneja los eventos de toque en la pantalla.
     * @return true si se procesó el evento, false en caso contrario
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // Manejar los eventos de toque
        int action = event.getAction();
        int ex = (int) event.getX();
        int ey = (int) event.getY();

        // Actualizar los estados de los controles
        derecha.comprueba_pulsado(ex, ey);
        izquierda.comprueba_pulsado(ex, ey);
        disparo.comprueba_pulsado(ex, ey);
        salto.comprueba_pulsado(ex, ey);

        if (action == MotionEvent.ACTION_UP) { // Si se soltó un toque
            derecha.comprueba_soltado(null);
            izquierda.comprueba_soltado(null);
            disparo.comprueba_soltado(null);
            salto.comprueba_soltado(null);
        }
        return true;
    }

    /**
     * Elimina un disparo de la lista.
     * @param d disparo a eliminar
     */
    public void eliminarDisparo(Disparo d) {
        listaDisparos.remove(d);
    }

    /**
     * Fin del juego.
     */
    private void ganar() {
        // 1) Detén el hilo secundario
        if (bucleJuego != null) {
            bucleJuego.fin();
        }

        // 2) Desactiva toques
        setOnTouchListener(null);
        setClickable(false);

        post(new Runnable() {
            @Override
            public void run() {
                // Inflar el layout personalizado para victoria (dialog_victoria.xml)
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View dialogView = inflater.inflate(R.layout.dialog_victoria, null);

                final AlertDialog dialog = new AlertDialog.Builder(getContext()) // Construir el diálogo
                        .setView(dialogView)
                        .setCancelable(false)
                        .setPositiveButton("Reiniciar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                d.dismiss(); // Cerrar el diálogo
                                reiniciarPartida(); // Reiniciar la partida
                            }
                        })
                        .setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                d.dismiss(); // Cerrar el diálogo
                                ((Activity) getContext()).finish(); // Finalizar la actividad actual
                            }
                        })
                        .create();
                dialog.show(); // Mostrar el diálogo
            }
        });
    }


    /**
     * Fin del juego.
     */
    private void finDelJuego() {
        // 1) Indica a BucleJuego que se pare
        if (bucleJuego != null) {
            bucleJuego.fin();
        }

        // Deshabilitar toques del SurfaceView para que el AlertDialog reciba los eventos
        this.setOnTouchListener(null);
        this.setClickable(false);

        // Mostrar el diálogo en el hilo principal
        post(new Runnable() {
            @Override
            public void run() {
                Activity activity = (Activity) getContext();
                if (activity == null || activity.isFinishing()) return;

                // Inflar el layout y crear el diálogo
                View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_derrota, null);
                AlertDialog dialog = new AlertDialog.Builder(activity)
                        .setView(dialogView)
                        .setCancelable(false)
                        .setPositiveButton("Reiniciar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                d.dismiss();
                                reiniciarPartida();
                            }
                        })
                        .setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                d.dismiss();
                                activity.finish();
                            }
                        })
                        .create();

                dialog.show();
            }
        });
    }

    /**
     * Reinicia la partida.
     */
    private void reiniciarPartida() {
        // Reinicia la actividad que contiene el juego
        Context context = getContext();
        Intent intent = new Intent(context, ActividadJuego.class);
        context.startActivity(intent);
        // Finaliza la actividad actual para evitar solapamientos
        ((Activity) context).finish();
    }


}