package edu.example.videojuego_practica7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import java.util.Random;

/**
 * Clase que representa un enemigo en el juego.
 */
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

    /**
     * Constructor de la clase Enemigo.
     * @param context Contexto de la aplicación
     * @param juego Referencia al juego
     * @param sizeLevel Tamaño de la bola: 3 = grande, 2 = mediana, 1 = pequeña
     * @param startX Posición inicial en el eje X
     * @param startY Posición inicial en el eje Y
     * @param speedFactor Factor de velocidad (ajustable según el nivel)
     */
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

        int totalBolas = 3;
        int spriteAncho = spriteSheet.getWidth() / totalBolas;
        int spriteAlto = spriteSheet.getHeight();

        // Selecciona aleatoriamente un índice (0, 1 o 2)
        int bolaIndex = rand.nextInt(3);

        int srcX = bolaIndex * spriteAncho; // Posición en el spriteSheet
        Rect src = new Rect(srcX, 0, srcX + spriteAncho, spriteAlto); // Rectángulo de recorte
        bolaIndividual = Bitmap.createBitmap(spriteSheet, src.left, src.top, src.width(), src.height()); // Recorta la bola

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

    /**
     * Constructor de la clase Enemigo.
     * @param context Contexto de la aplicación
     * @param juego Referencia al juego
     * @param sizeLevel Tamaño de la bola: 3 = grande, 2 = mediana, 1 = pequeña
     * @param startX Posición inicial en el eje X
     * @param startY Posición inicial en el eje Y
     */
    public Enemigo(Context context, EboraJuego juego, int sizeLevel, float startX, float startY) {
        this(context, juego, sizeLevel, startX, startY, 1.0f);
    }

    /**
     * Obtiene la imagen adecuada según el tamaño de la bola.
     * @param level Tamaño de la bola: 3 = grande, 2 = mediana, 1 = pequeña
     * @return Imagen correspondiente
     */
    private int getResourceForSizeLevel(int level) {
        if (level == 3) {
            return R.drawable.bolas2;  // Imagen para bola grande (debe tener 3 variantes en línea)
        } else if (level == 2) {
            return R.drawable.bolas3;  // Imagen para bola mediana
        } else {
            return R.drawable.bolas4;  // Imagen para bola pequeña
        }
    }

    /**
     * Actualiza la posición de la bola.
     */
    public void update() {
        x += velocidadX;
        y += velocidadY;

        // Rebote en el borde izquierdo/derecho usando limiteIzquierdo/limiteDerecho
        if (x < juego.limiteIzquierdo) {
            x = juego.limiteIzquierdo;
            velocidadX *= -1;
        } else if (x + ancho > juego.limiteDerecho) {
            x = juego.limiteDerecho - ancho;
            velocidadX *= -1;
        }

        // Rebote en el borde superior/inferior usando limiteSuperior/limiteInferior
        if (y < juego.limiteSuperior) {
            y = juego.limiteSuperior;
            velocidadY *= -1;
        } else if (y + alto > juego.limiteInferior) {
            y = juego.limiteInferior - alto;
            velocidadY *= -1;
        }
    }

    /**
     * Dibuja la bola en el canvas.
     * @param canvas lienzo
     */
    public void draw(Canvas canvas) {
        canvas.drawBitmap(bolaIndividual, x, y, null);
    }

    /**
     * Divide la bola en dos partes.
     * @return Arreglo de bolas divididas o null si no se puede dividir
     */
    public Enemigo[] dividir() {
        if (sizeLevel > 1) { // Si la bola no es pequeña
            if (sizeLevel == 3) { // Si es grande
                Enemigo[] nuevas = new Enemigo[2];
                float offsetX = ancho / 3f;
                nuevas[0] = new Enemigo(juego.getContext(), juego, sizeLevel - 1, x - offsetX, y, speedFactor);
                nuevas[1] = new Enemigo(juego.getContext(), juego, sizeLevel - 1, x + offsetX, y, speedFactor);
                return nuevas;
            } else if (sizeLevel == 2) { // Si es mediana
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
