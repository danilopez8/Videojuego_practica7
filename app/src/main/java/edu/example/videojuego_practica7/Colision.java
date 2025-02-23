package edu.example.videojuego_practica7;

import android.graphics.Rect;

public class Colision {

    // Verifica si dos rectángulos se solapan
    public static boolean rectsOverlap(Rect a, Rect b) {
        return Rect.intersects(a, b);
    }

    // O con coordenadas
    public static boolean rectsOverlap(float x1, float y1, float w1, float h1,
                                       float x2, float y2, float w2, float h2) {
        return (x1 < x2 + w2 && x1 + w1 > x2 &&
                y1 < y2 + h2 && y1 + h1 > y2);
    }
}

