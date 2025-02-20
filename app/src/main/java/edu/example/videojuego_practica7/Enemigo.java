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
    private EboraJuego juego;

    private static final int GRAVEDAD = 1;
    private static final int MAX_SIZE_LEVEL = 3; // Nivel máximo

    public Enemigo(Context context, EboraJuego juego, int sizeLevel, float startX, float startY) {
        this.juego = juego;
        this.sizeLevel = sizeLevel;

        // Cargar sprite de pompas (asegúrate de que R.drawable.pompas es la imagen correcta)
        spriteSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.bolas2);

        // Se asume que la imagen tiene 4 frames de diferentes tamaños
        int totalFrames = 4;
        frameWidth = spriteSheet.getWidth() / totalFrames;
        frameHeight = spriteSheet.getHeight();

        // Posición inicial
        x = startX;
        y = startY;

        // Velocidades aleatorias
        Random rand = new Random();
        velocidadX = rand.nextFloat() * 5 + 2;
        velocidadY = 0;
    }

    public void update() {
        // Movimiento vertical (caída)
        velocidadY += GRAVEDAD;
        y += velocidadY;

        // Movimiento horizontal
        x += velocidadX;

        // Rebote en los bordes
        if (x <= 0 || x + frameWidth >= juego.getWidth()) {
            velocidadX *= -1;
        }

        // Rebote en el suelo
        if (y + frameHeight >= juego.getHeight()) {
            y = juego.getHeight() - frameHeight;
            velocidadY = -velocidadY * 0.8f;
        }
    }

    public void draw(Canvas canvas) {
        // Seleccionar el frame según el tamaño: si sizeLevel = 3 (grande), se usará frame 0; si 2, frame 1; si 1, frame 2
        int frameIndex = MAX_SIZE_LEVEL - sizeLevel;
        int srcX = frameWidth * frameIndex;
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
