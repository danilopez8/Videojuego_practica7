package edu.example.videojuego_practica7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.Random;
public class Enemigo {

    private Bitmap spriteSheet;          // Imagen de la bola (contiene las 3 bolas en línea)
    private Bitmap bolaIndividual;       // Sprite recortado de una sola bola
    private float x, y;
    private float velocidadX, velocidadY;
    private int ancho, alto;
    private int sizeLevel;
    private EboraJuego juego;
    private Random rand;
    private float speedFactor;           // Factor de velocidad

    // Constructor con speedFactor
    public Enemigo(Context context, EboraJuego juego, int sizeLevel, float startX, float startY, float speedFactor) {
        this.juego = juego;
        this.sizeLevel = sizeLevel;
        this.x = startX;
        this.y = startY;
        this.rand = new Random();
        this.speedFactor = speedFactor;  // Asignamos correctamente el parámetro

        // Cargar la imagen completa de bolas (contiene las 3 bolas en una fila)
        spriteSheet = BitmapFactory.decodeResource(context.getResources(), getResourceForSizeLevel(sizeLevel));

        // Extraer solo una bola de la imagen (dividiendo en 3 secciones)
        int totalBolas = 3; // La imagen contiene 3 bolas
        int frameWidth = spriteSheet.getWidth() / totalBolas;
        int frameHeight = spriteSheet.getHeight();

        // Seleccionamos aleatoriamente un color de bola (0, 1 o 2)
        int bolaIndex = rand.nextInt(3);
        int srcX = bolaIndex * frameWidth;
        Rect src = new Rect(srcX, 0, srcX + frameWidth, frameHeight);
        bolaIndividual = Bitmap.createBitmap(spriteSheet, src.left, src.top, src.width(), src.height());

        // Establecer el ancho y alto según el recorte de una bola
        ancho = bolaIndividual.getWidth();
        alto = bolaIndividual.getHeight();

        // Asignar velocidades aleatorias en X e Y, multiplicadas por speedFactor
        velocidadX = (rand.nextFloat() * 6 - 3) * speedFactor;
        velocidadY = (rand.nextFloat() * 6 - 3) * speedFactor;

        // Evitar velocidades demasiado pequeñas
        if (Math.abs(velocidadX) < 1) {
            velocidadX = (velocidadX < 0) ? -2 : 2;
            velocidadX *= speedFactor;
        }
        if (Math.abs(velocidadY) < 1) {
            velocidadY = (velocidadY < 0) ? -2 : 2;
            velocidadY *= speedFactor;
        }
    }

    // Constructor por defecto, con speedFactor = 1.0f
    public Enemigo(Context context, EboraJuego juego, int sizeLevel, float startX, float startY) {
        this(context, juego, sizeLevel, startX, startY, 1.0f);
    }

    // Devuelve la imagen de bolas según su tamaño
    private int getResourceForSizeLevel(int level) {
        if (level == 3) {
            return R.drawable.bolas2;  // Imagen con las bolas grandes
        } else if (level == 2) {
            return R.drawable.bolas3;  // Imagen con las bolas medianas
        } else {
            return R.drawable.bolas4;  // Imagen con las bolas pequeñas
        }
    }

    public void update() {
        // Movimiento horizontal y vertical
        x += velocidadX;
        y += velocidadY;

        // Rebote en bordes izquierdo/derecho
        if (x < 0) {
            x = 0;
            velocidadX *= -1;
        } else if (x + ancho > juego.getWidth()) {
            x = juego.getWidth() - ancho;
            velocidadX *= -1;
        }

        // Rebote en bordes superior/inferior
        if (y < 0) {
            y = 0;
            velocidadY *= -1;
        } else if (y + alto > juego.getHeight()) {
            y = juego.getHeight() - alto;
            velocidadY *= -1;
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bolaIndividual, x, y, null);
    }

    // Comprueba si (px, py) está dentro de la bola
    public boolean colisionaCon(float px, float py) {
        return (px >= x && px <= x + ancho &&
                py >= y && py <= y + alto);
    }

    // Divide la bola en más pequeñas si no es la más pequeña
    public Enemigo[] dividir() {
        if (sizeLevel > 1) {
            Enemigo[] nuevas = new Enemigo[2];

            // Separación entre las nuevas bolas
            float offsetX = ancho / 2f;
            float offsetY = alto / 4f;

            nuevas[0] = new Enemigo(juego.getContext(), juego, sizeLevel - 1, x - offsetX, y - offsetY, speedFactor);
            nuevas[1] = new Enemigo(juego.getContext(), juego, sizeLevel - 1, x + offsetX, y - offsetY, speedFactor);
            return nuevas;
        }
        return null;
    }

    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }
    public int getSizeLevel() { return sizeLevel; }
}
