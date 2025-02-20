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
    private final int totalFrames = 4;  // Solo los frames correspondientes a la dirección

    private int frameWidth, frameHeight;
    private int columnas = 12;  // 8 frames en total
    private int filas = 1;     // Solo una fila

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
            frameActual = (frameActual + 1) % totalFrames;  // Ciclo entre los 4 frames hacia la derecha
        } else if (moviendoIzquierda) {
            x -= velocidadX;
            frameActual = (frameActual + 1) % totalFrames + 4;  // Ciclo entre los 4 frames hacia la izquierda (los frames de la derecha son de 0 a 3, los de izquierda de 4 a 7)
        }

        // Cambiar frame solo si se está moviendo
        if (!moviendoDerecha && !moviendoIzquierda) {
            frameActual = 11; // Si está quieto, mostrar el primer frame
        }
    }

    public void draw(Canvas canvas) {
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