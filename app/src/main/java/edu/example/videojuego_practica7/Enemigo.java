package edu.example.videojuego_practica7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import java.util.Random;

public class Enemigo {

    // Imagen original (spriteSheet) que contiene 3 variantes en línea
    private Bitmap spriteSheet;
    // Sprite recortado: la bola que se usará (se extrae la variante central)
    private Bitmap bolaIndividual;

    // Posición y velocidad
    private float x, y;
    private float velocidadX, velocidadY;

    // Dimensiones de la bola recortada
    private int ancho, alto;

    // Tamaño de la bola: 3 = grande, 2 = mediana, 1 = pequeña
    private int sizeLevel;

    // Referencia al juego para obtener ancho y alto
    private EboraJuego juego;

    // Factor de velocidad (ajustable según el nivel)
    private float speedFactor;

    // Generador de números aleatorios
    private Random rand;

    public Enemigo(Context context, EboraJuego juego, int sizeLevel, float startX, float startY, float speedFactor) {
        this.juego = juego;
        this.sizeLevel = sizeLevel;
        this.x = startX;
        this.y = startY;
        // Inicializa el Random usando el tiempo actual
        this.rand = new Random(System.currentTimeMillis());
        this.speedFactor = speedFactor;

        // Cargar la imagen adecuada según el tamaño
        spriteSheet = BitmapFactory.decodeResource(context.getResources(), getResourceForSizeLevel(sizeLevel));

        // Suponemos que la imagen contiene 3 variantes horizontales
        int totalBolas = 3;
        int spriteAncho = spriteSheet.getWidth() / totalBolas;
        int spriteAlto = spriteSheet.getHeight();
        // Selecciona aleatoriamente un índice (0, 1 o 2)
        int bolaIndex = rand.nextInt(3);
        // Opcional: verifica el valor con Log (requiere importar android.util.Log)
        // Log.d("Enemigo", "bolaIndex: " + bolaIndex);
        int srcX = bolaIndex * spriteAncho;
        Rect src = new Rect(srcX, 0, srcX + spriteAncho, spriteAlto);
        bolaIndividual = Bitmap.createBitmap(spriteSheet, src.left, src.top, src.width(), src.height());

        ancho = bolaIndividual.getWidth();
        alto = bolaIndividual.getHeight();

        velocidadX = (rand.nextFloat() * 6 - 3) * speedFactor;
        velocidadY = (rand.nextFloat() * 6 - 3) * speedFactor;
        if (Math.abs(velocidadX) < 1) {
            velocidadX = (velocidadX < 0) ? -2 : 2;
            velocidadX *= speedFactor;
        }
        if (Math.abs(velocidadY) < 1) {
            velocidadY = (velocidadY < 0) ? -2 : 2;
            velocidadY *= speedFactor;
        }
    }


    // Constructor por defecto, speedFactor = 1.0f
    public Enemigo(Context context, EboraJuego juego, int sizeLevel, float startX, float startY) {
        this(context, juego, sizeLevel, startX, startY, 1.0f);
    }

    // Selecciona el recurso de imagen según el tamaño
    private int getResourceForSizeLevel(int level) {
        if (level == 3) {
            return R.drawable.bolas2;  // Imagen para bola grande (debe tener 3 variantes en línea)
        } else if (level == 2) {
            return R.drawable.bolas3;  // Imagen para bola mediana
        } else {
            return R.drawable.bolas4;  // Imagen para bola pequeña
        }
    }

    public void update() {
        x += velocidadX;
        y += velocidadY;

        // Rebote en los bordes horizontal
        if (x < 0) {
            x = 0;
            velocidadX *= -1;
        } else if (x + ancho > juego.getWidth()) {
            x = juego.getWidth() - ancho;
            velocidadX *= -1;
        }

        // Rebote en los bordes vertical
        if (y < 0) {
            y = 0;
            velocidadY *= -1;
        } else {
            // Rebote inferior con margen de 200 px
            int limiteInferiorMapa = juego.getHeight() - 200;  // Ajusta el 200 según necesites
            if (y + alto > limiteInferiorMapa) {
                y = limiteInferiorMapa - alto;
                velocidadY *= -1;
            }
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bolaIndividual, x, y, null);
    }

    // Comprueba si (px, py) está dentro del área de la bola
    public boolean colisionaCon(float px, float py) {
        return (px >= x && px <= x + ancho && py >= y && py <= y + alto);
    }

    // Divide la bola:
    // - Si es grande (sizeLevel == 3): se generan 2 bolas medianas.
    // - Si es mediana (sizeLevel == 2): se generan 3 bolas pequeñas (posicionadas para no superponerse).
    // - Si es pequeña (sizeLevel == 1): retorna null (se elimina).
    public Enemigo[] dividir() {
        if (sizeLevel > 1) {
            if (sizeLevel == 3) {
                Enemigo[] nuevas = new Enemigo[2];
                float offsetX = ancho / 3f;
                nuevas[0] = new Enemigo(juego.getContext(), juego, sizeLevel - 1, x - offsetX, y, speedFactor);
                nuevas[1] = new Enemigo(juego.getContext(), juego, sizeLevel - 1, x + offsetX, y, speedFactor);
                return nuevas;
            } else if (sizeLevel == 2) {
                Enemigo[] nuevas = new Enemigo[3];
                float offsetX = ancho / 4f;
                float offsetY = alto / 4f;
                nuevas[0] = new Enemigo(juego.getContext(), juego, sizeLevel - 1, x - offsetX, y - offsetY, speedFactor);
                nuevas[1] = new Enemigo(juego.getContext(), juego, sizeLevel - 1, x, y - offsetY * 1.2f, speedFactor);
                nuevas[2] = new Enemigo(juego.getContext(), juego, sizeLevel - 1, x + offsetX, y - offsetY, speedFactor);
                return nuevas;
            }
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
