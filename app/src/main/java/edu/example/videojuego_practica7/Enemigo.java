package edu.example.videojuego_practica7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import java.util.Random;

public class Enemigo {

    // Imagen de la bola
    private Bitmap spriteSheet;

    // Posición y velocidad
    private float x, y;
    private float velocidadX, velocidadY;

    // Dimensiones de la imagen
    private int ancho, alto;

    // Tamaño de la bola: 3 = grande, 2 = mediana, 1 = pequeña
    private int sizeLevel;

    // Referencia al juego para getWidth(), getHeight(), etc.
    private EboraJuego juego;

    // Generador de velocidades aleatorias
    private Random rand;

    public Enemigo(Context context, EboraJuego juego, int sizeLevel, float startX, float startY) {
        this.juego = juego;
        this.sizeLevel = sizeLevel;
        this.x = startX;
        this.y = startY;
        this.rand = new Random();

        // Cargar la imagen adecuada al tamaño
        spriteSheet = BitmapFactory.decodeResource(context.getResources(), getResourceForSizeLevel(sizeLevel));

        // Tomamos su ancho y alto
        ancho = spriteSheet.getWidth();
        alto = spriteSheet.getHeight();

        // Asignar velocidades aleatorias en X e Y (rango -4..4) para que rebote
        velocidadX = rand.nextFloat() * 8 - 4;
        velocidadY = rand.nextFloat() * 8 - 4;

        // Evitar velocidades demasiado pequeñas
        if (Math.abs(velocidadX) < 1) {
            velocidadX = (velocidadX < 0) ? -2 : 2;
        }
        if (Math.abs(velocidadY) < 1) {
            velocidadY = (velocidadY < 0) ? -2 : 2;
        }
    }

    // Elige la imagen según sizeLevel (tres archivos distintos)
    private int getResourceForSizeLevel(int level) {
        if (level == 3) {
            return R.drawable.bolas4;  // Bola grande (un solo color / imagen)
        } else if (level == 2) {
            return R.drawable.bolas3;  // Bola mediana
        } else {
            return R.drawable.bolas2;  // Bola pequeña
        }
    }

    public void update() {
        // Mover la bola
        x += velocidadX;
        y += velocidadY;

        // Rebote en bordes izquierdo / derecho
        if (x < 0) {
            x = 0;
            velocidadX *= -1;
        } else if (x + ancho > juego.getWidth()) {
            x = juego.getWidth() - ancho;
            velocidadX *= -1;
        }

        // Rebote en bordes superior / inferior
        if (y < 0) {
            y = 0;
            velocidadY *= -1;
        } else if (y + alto > juego.getHeight()) {
            y = juego.getHeight() - alto;
            velocidadY *= -1;
        }
    }

    public void draw(Canvas canvas) {
        // Dibuja la bola sin recortes
        canvas.drawBitmap(spriteSheet, x, y, null);
    }

    // Comprueba si (px, py) está dentro de la bola
    public boolean colisionaCon(float px, float py) {
        return px >= x && px <= x + ancho &&
                py >= y && py <= y + alto;
    }

    // Dividir la bola en dos más pequeñas (si no es la más pequeña)
    public Enemigo[] dividir() {
        if (sizeLevel > 1) {
            Enemigo[] nuevas = new Enemigo[2];
            // Offset horizontal para separarlas un poco
            float offset = ancho / 4f;

            nuevas[0] = new Enemigo(juego.getContext(), juego, sizeLevel - 1, x - offset, y);
            nuevas[1] = new Enemigo(juego.getContext(), juego, sizeLevel - 1, x + offset, y);
            return nuevas;
        }
        return null;  // Si era la más pequeña (sizeLevel=1), no se divide más
    }

    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }
    public int getSizeLevel() { return sizeLevel; }
}
