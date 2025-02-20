package edu.example.videojuego_practica7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;



    public class Player {

        private Context context;
        private Bitmap sprite;  // Imagen del personaje
        private float x, y;  // Posición en la pantalla
        private float speed = 10;  // Velocidad del personaje
        private int width, height;
        private boolean isMovingRight = false;
        private boolean isMovingLeft = false;
        private boolean isJumping = false;
        private boolean isShooting = false;

        public Player(Context context) {
            this.context = context;
            sprite = BitmapFactory.decodeResource(context.getResources(), R.drawable.personajes);  // Cargar el sprite
            width = sprite.getWidth();
            height = sprite.getHeight();
            x = 100;  // Posición inicial en X
            y = 300;  // Posición inicial en Y
        }

        // Método para actualizar la posición del personaje
        public void update() {
            if (isMovingRight) {
                x += speed;  // Mueve a la derecha
            }
            if (isMovingLeft) {
                x -= speed;  // Mueve a la izquierda
            }
            if (isJumping) {
                // Lógica de salto
                y -= speed;
            }
        }

        // Método para mover al personaje hacia la derecha
        public void moveRight() {
            isMovingRight = true;
            isMovingLeft = false;
        }

        // Método para mover al personaje hacia la izquierda
        public void moveLeft() {
            isMovingLeft = true;
            isMovingRight = false;
        }

        // Método para disparar
        public void shoot() {
            isShooting = true;
            // Aquí puedes agregar la lógica para disparar
        }

        // Método para saltar
        public void jump() {
            isJumping = true;
            // Aquí puedes agregar la lógica para el salto (como velocidad de salto, gravedad, etc.)
        }

        // Método para dibujar el personaje en la pantalla
        public void draw(Canvas canvas) {
            canvas.drawBitmap(sprite, x, y, null);  // Dibuja el sprite en la posición actual
        }

        // Métodos para detener las acciones
        public void stopMoving() {
            isMovingLeft = false;
            isMovingRight = false;
        }

        public void stopJumping() {
            isJumping = false;
        }

        public void stopShooting() {
            isShooting = false;
        }
}
