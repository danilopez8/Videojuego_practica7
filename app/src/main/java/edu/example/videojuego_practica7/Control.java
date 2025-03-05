package edu.example.videojuego_practica7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Clase que representa un control en el juego.
 */
public class Control {

    // Datos del control
    public boolean pulsado = false;
    public float coordenada_x, coordenada_y;
    private Bitmap imagen;
    private Context mContexto;
    public String nombre;

    /**
     * Constructor del control.
     * @param c Contexto de la aplicación
     * @param x Coordenada X
     * @param y Coordenada Y
     */
    public Control(Context c, float x, float y){
        coordenada_x = x;
        coordenada_y = y;
        mContexto = c;
    }

    /**
     * Carga una imagen para el control.
     * @param recurso Recurso de la imagen
     */
    public void cargarImagen(int recurso){
        imagen = BitmapFactory.decodeResource(mContexto.getResources(), recurso);
    }

    /**
     * Dibuja el control en el lienzo.
     * @param c lienzo
     * @param p pintura
     */
    public void dibujar(Canvas c, Paint p){
        c.drawBitmap(imagen, coordenada_x, coordenada_y, p);
    }

    /**
     * Comprueba si el control ha sido pulsado.
     * @param x Coordenada X del toque
     * @param y Coordenada Y del toque
     */
    public void comprueba_pulsado(int x, int y){
        if (x > coordenada_x && x < coordenada_x + Ancho() &&
                y > coordenada_y && y < coordenada_y + Alto()){
            pulsado = true;
        }
    }

    /**
     * Obtiene el ancho del control.
     * @return Ancho del control
     */
    public int Ancho (){
        return imagen.getWidth();
    }

    /**
     * Obtiene el alto del control.
     * @return Alto del control
     */
    public int Alto (){
        return imagen.getHeight();
    }

    /**
     * Comprueba si el control ha sido soltado.
     * @param lista Lista de toques
     */
    public void comprueba_soltado(ArrayList<Toque> lista){
        // Si la lista es null o está vacía, consideramos que no hay toques y se suelta el botón.
        if (lista == null || lista.isEmpty()){
            pulsado = false;
            return;
        }

        boolean aux = false; // Indica si el control ha sido soltado
        for(Toque t: lista){ // Recorre la lista de toques

            if(t.x > coordenada_x && t.x < coordenada_x + Ancho() &&
                    t.y > coordenada_y && t.y < coordenada_y + Alto()){
                aux = true;
            }
        }
        // Si no se ha soltado, se mantiene pulsado
        if(!aux){
            pulsado = false;
        }
    }

}
