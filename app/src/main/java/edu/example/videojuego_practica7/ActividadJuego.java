package edu.example.videojuego_practica7;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Actividad principal del juego.
 */
public class ActividadJuego extends AppCompatActivity {

    EboraJuego j; // Referencia al juego

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        j = new EboraJuego(this); // Crea el juego
        setContentView(j);// Establece el juego como contenido de la actividad
        hideSystemUI(); // Oculta la barra de estado y la barra de navegación
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // Restricción de orientación
    }

    /**
     * Oculta la barra de estado y la barra de navegación.
     */
    private void hideSystemUI(){
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.HONEYCOMB){
            j.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE //diseño estable sin cambio al ocultar la barras
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION //permite que la aplicacion de dibuje detras de la barra de navegacion
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // que se dibuje detras de la barra de estado
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar (botones virtuales)
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar (barra de estado hora y bateria..)
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY); //matiene el modo inmersivo incluso si el usuario toca la pantalla


            j.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    hideSystemUI();
                }
            });
        }
    }


}