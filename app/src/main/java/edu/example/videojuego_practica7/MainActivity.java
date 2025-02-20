package edu.example.videojuego_practica7;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Establece el layout primero
        EdgeToEdge.enable(this);
        AnimarBoton();

        Button b = findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ActividadJuego.class);
                startActivity(i);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void AnimarBoton(){
        AnimatorSet animadorBton = new AnimatorSet();
        Button b=findViewById(R.id.button);

        //1ª animación, trasladar desde la izquierda (800 pixeles menos hasta la posición
        //inicial (0)
        ObjectAnimator trasladar= ObjectAnimator.ofFloat(b,"translationX",-800,0);
        trasladar.setDuration(5000);//duración 5 segundos

        //2ª Animación fade in de 8 segundos
        ObjectAnimator fade = ObjectAnimator.ofFloat(b, "alpha", 0f, 1f);
        fade.setDuration(8000);

        //3ª Animación
        ObjectAnimator rotar=ObjectAnimator.ofFloat(b,"rotationY",0,360);
        rotar.setDuration(5000);

        //4ª animación
        ObjectAnimator color=ObjectAnimator.ofArgb(b,"backgroundColor",
                Color.argb(128,255,0,0),Color.argb(128,0,0,255));
        color.setDuration(5000);

        //5ª animación
        ObjectAnimator trasladarY=ObjectAnimator.ofFloat(b,"translationY",1000,0);
        trasladarY.setDuration(5000);

        animadorBton.play(trasladar).with(fade).with(rotar).with(color).with(trasladarY);
        animadorBton.start();
    };
}