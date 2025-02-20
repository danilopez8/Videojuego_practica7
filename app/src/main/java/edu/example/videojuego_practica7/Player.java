package edu.example.videojuego_practica7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Player {
    private Bitmap spriteSheet;
    private float x, y;
    private int frameActual = 0;
    private int contadorFrames = 0;
    private final int totalFrames = 8;  // Ahora hay 8 frames en una fila

    private int frameWidth, frameHeight;
    private int columnas = 8; // Número de columnas en el sprite sheet
    private int filas = 1; // Ahora solo hay una fila

    private float velocidadX = 10f;
    private boolean moviendoDerecha = false;
    private boolean moviendoIzquierda = false;

    public Player(Context context) {
        spriteSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);

        // Calcular tamaño de cada frame correctamente
        frameWidth = spriteSheet.getWidth() / columnas;
        frameHeight = spriteSheet.getHeight() / filas;

        // Posición inicial del personaje en pantalla
        x = 100;
        y = 300;
    }

    public void update() {
        if (moviendoDerecha) {
            x += velocidadX;
        } else if (moviendoIzquierda) {
            x -= velocidadX;
        }

        // Cambiar frame solo si se está moviendo
        if (moviendoDerecha || moviendoIzquierda) {
            contadorFrames++;
            if (contadorFrames % 6 == 0) {
                frameActual = (frameActual + 1) % totalFrames;
            }
        } else {
            frameActual = 0; // Si está quieto, mostrar el primer frame
        }
    }

    public void draw(Canvas canvas) {
        // **Corrección del recorte**
        int srcX = frameWidth * frameActual;
        int srcY = 0; // Solo hay una fila

        Rect src = new Rect(srcX, srcY, srcX + frameWidth, srcY + frameHeight);
        Rect dst = new Rect((int) x, (int) (y - frameHeight), (int) (x + frameWidth), (int) y);

        canvas.drawBitmap(spriteSheet, src, dst, null);
    }

    public void moveRight() {
        moviendoDerecha = true;
        moviendoIzquierda = false;
    }

    public void moveLeft() {
        moviendoIzquierda = true;
        moviendoDerecha = false;
    }

    public void stopMoving() {
        moviendoDerecha = false;
        moviendoIzquierda = false;
    }
}
