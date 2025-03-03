package edu.example.videojuego_practica7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Clase que representa un disparo en el juego.
 */
public class Disparo {

    // Datos del disparo
    private Bitmap imagen;
    private float x, y;
    private float velocidadY = -15;  // Velocidad del disparo (hacia arriba)
    private EboraJuego juego;
    private int ancho, alto;

    private int delayFrames = 3; // Número de frames de retardo antes de verificar colisión
    private int frameCounter = 0;

    /**
     * Constructor del disparo.
     * @param context Contexto de la aplicación
     * @param juego Juego
     * @param startX Coordenada X de inicio
     * @param startY Coordenada Y de inicio
     */
    public Disparo(Context context, EboraJuego juego, float startX, float startY) {
        this.juego = juego;
        this.x = startX;
        this.y = startY;

        imagen = BitmapFactory.decodeResource(context.getResources(), R.drawable.tiro); // Carga la imagen del disparo
        this.ancho = imagen.getWidth(); // Obtiene el ancho de la imagen
        this.alto = imagen.getHeight(); // Obtiene el alto de la imagen
    }

    /**
     * Actualiza el estado del disparo.
     */
    public void update() {
        frameCounter++; // Incrementa el contador de frames
        y += velocidadY; // Mueve el disparo hacia arriba
        if (y + alto < 0) { // Si el disparo sale de la pantalla superior, lo elimina
           juego.eliminarDisparo(this); // Elimina el disparo de la lista de disparos
        }
    }

    /**
     * Dibuja el disparo en el lienzo.
     * @param canvas lienzo
     */
    public void draw(Canvas canvas) {
        canvas.drawBitmap(imagen, x - ancho / 2, y-alto, null); // Dibuja el disparo en el lienzo
    }

    /**
     * Verifica si el disparo colisiona con un enemigo.
     * @param pompa Enemigo
     * @return true si colisiona, false en caso contrario
     */
    public boolean colisionaCon(Enemigo pompa) {
        if (frameCounter < delayFrames) { // Retardo antes de verificar colisión
            return false; // No colisiona
        }

        // Rect del disparo
        int left   = (int) (x - ancho / 2);
        int top    = (int) (y - alto);
        int right  = (int) (x + ancho / 2);
        int bottom = (int) y;
        Rect disparoRect = new Rect(left, top, right, bottom); // Crea el rectángulo del disparo

        // Rect del enemigo
        int pompaLeft   = (int) pompa.getX();
        int pompaTop    = (int) pompa.getY();
        int pompaRight  = (int) (pompa.getX() + pompa.getAncho());
        int pompaBottom = (int) (pompa.getY() + pompa.getAlto());
        Rect pompaRect = new Rect(pompaLeft, pompaTop, pompaRight, pompaBottom); // Crea el rectángulo del enemigo

        // Retorna true si los rectángulos se solapan
        return Rect.intersects(disparoRect, pompaRect);
    }

    /**
     * Obtiene la coordenada X del disparo.
     * @return Coordenada X del disparo
     */
    public float getX() {
        return x;
    }

    /**
     * Obtiene la coordenada Y del disparo.
     * @return Coordenada Y del disparo
     */
    public float getY() {
        return y;
    }

    /**
     * Obtiene el ancho del disparo.
     * @return Ancho del disparo
     */
    public boolean fueraDePantalla() {
        return y < 0; // Si el disparo sale de la pantalla superior
    }
}