package edu.example.videojuego_practica7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

public class Control {

    public boolean pulsado = false;
    public float coordenada_x, coordenada_y;
    private Bitmap imagen;
    private Context mContexto;
    public String nombre;

    public Control(Context c, float x, float y){
        coordenada_x = x;
        coordenada_y = y;
        mContexto = c;
    }

    // Cargar la imagen del control
    public void cargarImagen(int recurso){
        imagen = BitmapFactory.decodeResource(mContexto.getResources(), recurso);
    }

    // Dibujar el control en un canvas
    public void dibujar(Canvas c, Paint p){
        c.drawBitmap(imagen, coordenada_x, coordenada_y, p);
    }

    // Comprobar si se ha tocado el control
    public void comprueba_pulsado(int x, int y){
        if (x > coordenada_x && x < coordenada_x + Ancho() &&
                y > coordenada_y && y < coordenada_y + Alto()){
            pulsado = true;
        }
    }

    public int Ancho (){
        return imagen.getWidth();
    }

    public int Alto (){
        return imagen.getHeight();
    }

    // Comprobar si se ha soltado el control
    public void comprueba_soltado(ArrayList<Toque> lista){
        // Si la lista es null o está vacía, consideramos que no hay toques y se suelta el botón.
        if (lista == null || lista.isEmpty()){
            pulsado = false;
            return;
        }
        boolean aux = false;
        for(Toque t: lista){
            if(t.x > coordenada_x && t.x < coordenada_x + Ancho() &&
                    t.y > coordenada_y && t.y < coordenada_y + Alto()){
                aux = true;
            }
        }
        if(!aux){
            pulsado = false;
        }
    }

}
