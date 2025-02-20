package edu.example.videojuego_practica7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.Random;

public class Enemigo {
    private Bitmap spriteSheet;
    private float x, y;
    private float velocidadX, velocidadY;
    private int frameWidth, frameHeight;
    private int sizeLevel; // Tamaño de la pompa (de grande a pequeña)
    private boolean reboteArriba = false; // Controla la dirección del rebote
    private EboraJuego juego;

    private static final int GRAVEDAD = 1;
    private static final int MAX_SIZE_LEVEL = 3; // Nivel máximo (más grande)

    public Enemigo(Context context, EboraJuego juego, int sizeLevel, float startX, float startY) {
        this.juego = juego;
        this.sizeLevel = sizeLevel;

        // Cargar sprite de pompa
        spriteSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.pompas);

        // Definir el tamaño de cada pompa según su nivel
        int totalFrames = 4; // Supongamos que hay 4 tamaños de pompa en la imagen
        frameWidth = spriteSheet.getWidth() / totalFrames;
        frameHeight = spriteSheet.getHeight();

        // Posición inicial
        x = startX;
        y = startY;

        // Velocidades aleatorias
        Random rand = new Random();
        velocidadX = rand.nextFloat() * 5 + 2; // Velocidad horizontal aleatoria
        velocidadY = 0; // Comienza cayendo
    }

    public void update() {
        // Movimiento de caída
        velocidadY += GRAVEDAD; // Aumentar la velocidad por gravedad
        y += velocidadY;

        // Movimiento horizontal
        x += velocidadX;

        // Rebote en los bordes
        if (x <= 0 || x + frameWidth >= juego.getWidth()) {
            velocidadX *= -1; // Invertir dirección
        }

        // Rebote en el suelo (simulación)
        if (y + frameHeight >= juego.getHeight()) {
            y = juego.getHeight() - frameHeight;
            velocidadY = -velocidadY * 0.8f; // Rebote con reducción de velocidad
        }
    }

    public void draw(Canvas canvas) {
        int srcX = frameWidth * (MAX_SIZE_LEVEL - sizeLevel); // Ajusta el frame según el tamaño
        Rect src = new Rect(srcX, 0, srcX + frameWidth, frameHeight);
        Rect dst = new Rect((int) x, (int) y, (int) (x + frameWidth), (int) (y + frameHeight));

        canvas.drawBitmap(spriteSheet, src, dst, null);
    }

    public boolean colisionaCon(float px, float py) {
        return px >= x && px <= x + frameWidth && py >= y && py <= y + frameHeight;
    }

    public Enemigo[] dividir() {
        if (sizeLevel > 1) {
            return new Enemigo[]{
                    new Enemigo(juego.getContext(), juego, sizeLevel - 1, x - frameWidth / 2, y),
                    new Enemigo(juego.getContext(), juego, sizeLevel - 1, x + frameWidth / 2, y)
            };
        }
        return null;
    }

    public int getSizeLevel() {
        return sizeLevel;
    }
}
