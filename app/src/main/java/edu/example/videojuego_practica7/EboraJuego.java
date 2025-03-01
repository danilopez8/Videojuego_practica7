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
    private float velocidadX = 15f;
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
    private Bitmap nivelesImagen;
    private boolean mostrandoNivel = false;
    private int contadorTransicion = 0;
    private final int DURACION_TRANSICION = 120; // 2 segundos a 60 FPS
    private int alphaNivel = 0;  // Opacidad inicial (0 = invisible)
    private boolean fadeIn = true;  // Indica si estamos en la fase de aparición
    private final int FADE_STEP = 5;  // Cantidad de opacidad que aumenta/disminuye por frame


    // Tiempo máximo en segundos para cada nivel (ajusta a tu gusto)
    private int tiempoRestanteSegundos = 90;

    // Cálculo de frames totales (usando la misma tasa de FPS que BucleJuego)
    private int framesRestantes;


    private BucleJuego bucleJuego;
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

        // Cargar la imagen de niveles
        nivelesImagen = BitmapFactory.decodeResource(getResources(), R.drawable.niveles_fondo);
// Iniciar la transición desde el inicio del juego
        mostrandoNivel = true;
        contadorTransicion = 0;
        alphaNivel = 0;
        fadeIn = true;

        framesRestantes = tiempoRestanteSegundos * BucleJuego.MAX_FPS;

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

        nivel = 0;
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

        bucleJuego = new BucleJuego(getHolder(), this);
        bucleJuego.start();

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
        if (gameThread != null) {
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
                    renderizar(canvas);
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

            // Limpiar la pantalla
            canvas.drawColor(Color.BLACK);

            // Dibujar el fondo
            int srcX = fondoFrameAncho * fondoActual;
            int srcY = 0;
            int margenInferior = 200; // Espacio en píxeles que quieres dejar en la parte inferior
            Rect srcFondo = new Rect(srcX, srcY, srcX + fondoFrameAncho, srcY + fondoFrameAlto);
            Rect dstFondo = new Rect(0, 0, pantallaAncho, pantallaAlto - margenInferior);
            canvas.drawBitmap(fondoSprite, srcFondo, dstFondo, null);


            // Dibujar al jugador
            int srcXjug = frameWidth * frameActual;
            int srcYjug = 0;
            Rect srcJugador = new Rect(srcXjug, srcYjug, srcXjug + frameWidth, srcYjug + frameHeight);
            Rect dstJugador = new Rect((int) x, (int) (y - frameHeight), (int) (x + frameWidth), (int) y);
            canvas.drawBitmap(spriteSheet, srcJugador, dstJugador, null);

            // Dibujar los disparos
            for (Disparo d : listaDisparos) {
                d.draw(canvas);
            }

            // Dibujar los enemigos (pompas)
            for (Enemigo pompa : listaPompas) {
                pompa.draw(canvas);
            }

            // Dibujar controles
            Paint paint = new Paint();
            derecha.dibujar(canvas, paint);
            izquierda.dibujar(canvas, paint);
            disparo.dibujar(canvas, paint);
            salto.dibujar(canvas, paint);

            // Dibujar HUD de vidas
            dibujarVidas(canvas);

            if (mostrandoNivel && nivelesImagen != null) {
                // Calcular qué parte de la imagen mostrar
                int seccionAncho = nivelesImagen.getWidth() / 3;  // 3 niveles en una sola imagen
                int srcXNiv = (nivel - 1) * seccionAncho;
                Rect src = new Rect(srcXNiv, 0, srcXNiv + seccionAncho, nivelesImagen.getHeight());
                Rect dst = new Rect(0, 0, pantallaAncho, pantallaAlto);

                // Crear un Paint con la opacidad ajustada
                Paint paintNivel = new Paint();
                paintNivel.setAlpha(alphaNivel);

                // Dibujar la parte correspondiente de la imagen con transparencia
                canvas.drawBitmap(nivelesImagen, src, dst, paint);

                // Manejar la animación fade-in y fade-out
                if (fadeIn) {
                    alphaNivel += FADE_STEP;
                    if (alphaNivel >= 255) {  // Cuando llega al máximo, cambia a fade-out
                        alphaNivel = 255;
                        fadeIn = false;
                    }
                } else {
                    alphaNivel -= FADE_STEP;
                    if (alphaNivel <= 0) {  // Cuando llega a 0, termina la transición
                        mostrandoNivel = false;

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

        // Decrementar el contador de frames del temporizador
        framesRestantes--;
        if (framesRestantes <= 0) {
            // Se acabó el tiempo, fin del juego
            finDelJuego();
            return; // Para que no siga actualizando este frame
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
/*
    private void drawGame(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        // Dibuja el fondo
        int srcX = fondoFrameAncho * fondoActual;
        int srcY = 0;
        Rect srcFondo = new Rect(srcX, srcY, srcX + fondoFrameAncho, srcY + fondoFrameAlto);
        Rect dstFondo = new Rect(0, 0, pantallaAncho, pantallaAlto);
        canvas.drawBitmap(fondoSprite, srcFondo, dstFondo, null);

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
*/
    private void terminarParpadeo() {
        jugadorGolpeado = false;
        contadorGolpe = 0;
        // Pones un frame neutro, p.e. 11 (quieto)
        frameActual = 11;
    }

    private void dibujarVidas(Canvas canvas) {
        if (iconoVida == null) return;

        int separacion = 10;
        int anchoVida = iconoVida.getWidth();
        int offsetX = 600;
        // Usamos el mismo margen que en renderizar:
        int margenInferior = 200;
        // Colocamos las vidas en la parte inferior, con un pequeño margen adicional, por ejemplo 20 píxeles dentro del área inferior:
        int offsetY = pantallaAlto - margenInferior + 20;

        for (int i = 0; i < vidas; i++) {
            int xPos = offsetX + i * (anchoVida + separacion);
            int yPos = offsetY;
            canvas.drawBitmap(iconoVida, xPos, yPos, null);

            // A continuación, dibujamos el temporizador
            // Convertimos frames restantes a segundos
            int segundosRestantes = framesRestantes / BucleJuego.MAX_FPS;

            // Ajusta un poco la posición para que no se superponga a las vidas
            int xTimer = offsetX + (vidas * (anchoVida + separacion)) + 50;
            int yTimer = offsetY + 30;  // Un poco más abajo o al mismo nivel

            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(40);  // Ajusta el tamaño de fuente

            canvas.drawText("Tiempo: " + segundosRestantes, xTimer, yTimer, paint);
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
                    // Rompe el bucle de pompas para este disparo
                    break;
                }
            }
        }
        listaPompas.removeAll(pompasEliminar);
        listaDisparos.removeAll(disparosEliminar);
        listaPompas.addAll(pompasNuevas);


    }


    public boolean sinEnemigos() {
        return listaPompas.isEmpty();
    }

    public void pasarAlSiguienteNivel() {
        nivel++;
        if (nivel > 3) {  // Si ya completó 3 niveles
            ganar();
            return;
        }

        mostrandoNivel = true;
        contadorTransicion = 0;
        alphaNivel = 0;
        fadeIn = true;

        // Reiniciamos el temporizador a 30 segundos (o lo que quieras)
        framesRestantes = tiempoRestanteSegundos * BucleJuego.MAX_FPS;

        fondoActual = nivel - 1;
        enemigosPorNivel = (nivel * 1) + 0;
        enemigosEliminados = 0;
        listaPompas.clear();
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

  /*  // Fin de juego: pausar hilo o mostrar "Game Over"
    private void finDelJuego() {
        isRunning = false;
        // Podrías pasar a otra Activity, mostrar un dialog, etc.
    }
*/

    private void spawnPompa() {
        int screenWidth = getWidth();
        float posX = (float) (Math.random() * (screenWidth - 100)) + 50;
        float posY = 0; // Aparece en la parte superior
        int sizeLevel = 3; // Pompa grande
        // Factor de velocidad según el nivel; en el nivel 1 es 1.0, aumenta un 10% por nivel adicional
        float velocidadExtra = 1.0f + (0.2f * (nivel - 1));
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

                final AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setView(dialogView)
                        .setCancelable(false)
                        .setPositiveButton("Reiniciar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                // Despedir el diálogo antes de reiniciar
                                d.dismiss();
                                reiniciarPartida();
                            }
                        })
                        .setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                d.dismiss();
                                ((Activity) getContext()).finish();
                            }
                        })
                        .create();
                dialog.show();
            }
        });
    }


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

    private void reiniciarPartida() {
        // Reinicia la actividad que contiene el juego
        Context context = getContext();
        Intent intent = new Intent(context, ActividadJuego.class);
        context.startActivity(intent);
        // Finaliza la actividad actual para evitar solapamientos
        ((Activity) context).finish();
    }


}