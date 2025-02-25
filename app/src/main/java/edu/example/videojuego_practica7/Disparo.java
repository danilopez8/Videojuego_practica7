package edu.example.videojuego_practica7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Disparo {
    private Bitmap imagen;
    private float x, y;
    private float velocidadY = -15;  // Velocidad del disparo (hacia arriba)
    private EboraJuego juego;
    private int ancho, alto;

    private int delayFrames = 3; // Número de frames de retardo antes de verificar colisión
    private int frameCounter = 0;

    public Disparo(Context context, EboraJuego juego, float startX, float startY) {
        this.juego = juego;
        this.x = startX;
        this.y = startY;
        // Cargar imagen del disparo (se asume que tiro.png es el recurso sin fondo)
        imagen = BitmapFactory.decodeResource(context.getResources(), R.drawable.tiro);
        this.ancho = imagen.getWidth();
        this.alto = imagen.getHeight();
    }

    public void update() {
        frameCounter++;

        y += velocidadY;
        if (y + alto < 0) {
           juego.eliminarDisparo(this);
        }
    }

    public void draw(Canvas canvas) {

        canvas.drawBitmap(imagen, x - ancho / 2, y-alto, null);
    }

    public boolean colisionaCon(Enemigo pompa) {
        // Si el disparo tiene menos de delayFrames, no se verifica la colisión
        if (frameCounter < delayFrames) {
            return false;
        }

        // Rect completo del disparo (parte inferior: y, superior: y - alto)
        int left   = (int) (x - ancho / 2);
        int top    = (int) (y - alto);
        int right  = (int) (x + ancho / 2);
        int bottom = (int) y;
        Rect disparoRect = new Rect(left, top, right, bottom);

        // Rect del enemigo
        int pompaLeft   = (int) pompa.getX();
        int pompaTop    = (int) pompa.getY();
        int pompaRight  = (int) (pompa.getX() + pompa.getAncho());
        int pompaBottom = (int) (pompa.getY() + pompa.getAlto());
        Rect pompaRect = new Rect(pompaLeft, pompaTop, pompaRight, pompaBottom);

        // Retorna true si los rectángulos se solapan
        return Rect.intersects(disparoRect, pompaRect);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean fueraDePantalla() {
        return y < 0; // Si el disparo sale de la pantalla superior
    }
}