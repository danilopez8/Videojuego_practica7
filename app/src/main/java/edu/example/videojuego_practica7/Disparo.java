package edu.example.videojuego_practica7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class Disparo {
    private Bitmap imagen;
    private float x, y;
    private float velocidadY = -20;  // Velocidad del disparo (hacia arriba)
    private EboraJuego juego;
    private int ancho, alto;

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
        y += velocidadY;
        if (y + alto < 0) {
           juego.eliminarDisparo(this);
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(imagen, x - ancho / 2, y, null);
    }

    public boolean colisionaCon(Enemigo pompa) {
        return (x >= pompa.getX() && x <= pompa.getX() + pompa.getAncho() &&
                y >= pompa.getY() && y <= pompa.getY() + pompa.getAlto());
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