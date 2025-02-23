package edu.example.videojuego_practica7;

/**
 * Clase para representar un toque en la pantalla.
 */
public class Toque {

    public int id;   // Identificador del toque (por ejemplo, para múltiples toques simultáneos)
    public float x, y; // Coordenadas del toque en la pantalla

    public Toque(int id, float x, float y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }
}
