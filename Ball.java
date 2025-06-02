package com.example.samsung_project;

import static android.opengl.ETC1.getWidth;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Ball {
    private float x, y, velocityY;
    private Bitmap bitmap;
    public Ball(float x, float y, float velocityY, Bitmap bitmap){
        this.x = x;
        this.y = y;
        this.bitmap = bitmap;
        this.velocityY = velocityY;
    }

    public float getX(){
        return x;
    }
    public float getY(){
        return y;
    }
    public void setX(float x){
        this.x = x;
    }
    public void setY(float y){
        this.y = y;
    }
    public void setVy(float Vy){
        this.velocityY = Vy;
    }
    public float getWidth(){
        return this.bitmap.getWidth();
    }
    public float getHeigth(){
        return this.bitmap.getHeight();
    }

    public void reset(double b){
        double v = Math.random() * ((b - this.bitmap.getWidth()) - 0) + 0;
        this.x = (float) v;
        this.y = 0;
    }

    public void move(){
        this.y += this.velocityY;
    }

    public void draw (Canvas canvas) {
        Paint p = new Paint();
        canvas.drawBitmap(bitmap, this.x, this.y, p);
    }

    public void reset(double screenWidth, Ball[] otherBalls, int currentIndex) {
        boolean positionValid;
        float newX;

        do {
            positionValid = true;
            newX = (float) (Math.random() * (screenWidth - this.bitmap.getWidth()));

            // Проверяем, не пересекается ли новый шар с другими
            for (int i = 0; i < otherBalls.length; i++) {
                if (i != currentIndex && otherBalls[i] != null) {
                    if (Math.abs(newX - otherBalls[i].getX()) < this.bitmap.getWidth()) {
                        positionValid = false;
                        break;
                    }
                }
            }
        } while (!positionValid);

        this.x = newX;
        this.y = -this.bitmap.getHeight(); // Начинаем выше экрана
    }
}
