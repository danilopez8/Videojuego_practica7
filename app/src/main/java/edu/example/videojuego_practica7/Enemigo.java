package edu.example.videojuego_practica7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import java.util.Random;

public class Enemigo {
    private Bitmap spriteSheet;
    private float x, y;
    private float velocidadX, velocidadY;
    private int frameWidth, frameHeight;
    private int sizeLevel; // Tamaño de la pompa: 3 = grande, 2 = mediana, 1 = pequeña
    private EboraJuego juego;
    private static final int GRAVEDAD = 1;
    private static final int MAX_SIZE_LEVEL = 3; // Nivel máximo
    private Random rand;

    public Enemigo(Context context, EboraJuego juego, int sizeLevel, float startX, float startY) {
        this.juego = juego;
        this.sizeLevel = sizeLevel;
        rand = new Random();
        x = startX;
        y = startY;
        velocidadX = rand.nextFloat() * 5 + 2;
        velocidadY = 0;
        // Cargar la imagen según el nivel de tamaño
        spriteSheet = BitmapFactory.decodeResource(context.getResources(), getResourceForSizeLevel(sizeLevel));
        // Se asume que la imagen se usa completa (sin recorte)
        frameWidth = spriteSheet.getWidth();
        frameHeight = spriteSheet.getHeight();
    }

    private int getResourceForSizeLevel(int level) {
        if (level == 3) {
            return R.drawable.bolas4;  // Pompa grande
        } else if (level == 2) {
            return R.drawable.bolas3;  // Pompa mediana
        } else {
            return R.drawable.bolas2;  // Pompa pequeña
        }
    }

    public void update() {
        velocidadY += GRAVEDAD;
        y += velocidadY;
        x += velocidadX;
        if (x <= 0 || x + frameWidth >= juego.getWidth()) {
            velocidadX *= -1;
        }
        if (y + frameHeight >= juego.getHeight()) {
            y = juego.getHeight() - frameHeight;
            velocidadY = -velocidadY * 0.8f;
        }
        if (y <= 0) {
            y = 0;
            velocidadY *= -1;
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(spriteSheet, x, y, null);
    }

    public boolean colisionaCon(float px, float py) {
        return px >= x && px <= x + frameWidth && py >= y && py <= y + frameHeight;
    }

    public Enemigo[] dividir() {
        if (sizeLevel > 1) {
            Enemigo[] nuevas = new Enemigo[2];
            float offset = frameWidth / 4f;
            nuevas[0] = new Enemigo(juego.getContext(), juego, sizeLevel - 1, x - offset, y);
            nuevas[1] = new Enemigo(juego.getContext(), juego, sizeLevel - 1, x + offset, y);
            return nuevas;
        }
        return null;
    }

    // Getters para colisiones
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getAncho() {
        return frameWidth;
    }

    public int getAlto() {
        return frameHeight;
    }

    public int getSizeLevel() {
        return sizeLevel;
    }
}