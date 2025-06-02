package com.example.samsung_project;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GameView extends View {

    private long gameStartTime; // Время начала игры
    private float distance = 0;
    private float recordDistance = 0;
    private Paint textPaint = new Paint();
    private int viewWidth;
    private int viewHeight;
    private Bitmap background;
    private Bitmap startBackground;
    private boolean start_flag = false;
    private int health = 10;
    private Fighter player;
    private final int timerInterval = 30;
    private Ball[] balls = new Ball[3];
    private GameOverListener gameOverListener;

    public interface GameOverListener {
        void onGameOver();
    }

    public void setGameOverListener(GameOverListener listener) {
        this.gameOverListener = listener;
    }

    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Загружаем фоновые изображения
        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        startBackground = BitmapFactory.decodeResource(getResources(), R.drawable.start_background);

        // Загружаем изображение корабля с масштабированием
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fighter);
        // Уменьшаем размер в 2 раза (можно изменить коэффициент)
        float scaleFactor = 0.8f; // 0.5 = 50% от исходного размера
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                (int)(originalBitmap.getWidth() * scaleFactor),
                (int)(originalBitmap.getHeight() * scaleFactor),
                true
        );
        Rect firstFrame = new Rect(0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
        player = new Fighter(500, 1900, 0, 0, firstFrame, scaledBitmap);

        // Инициализируем шары
        Bitmap ballBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
        for (int i = 0; i < balls.length; i++) {
            balls[i] = new Ball(0, -ballBitmap.getHeight(), 20, ballBitmap);
        }

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(55.0f);
        textPaint.setAntiAlias(true);

        // Загрузка рекорда из SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
        recordDistance = prefs.getFloat("recordDistance", 0);

        new Timer().start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;

        // После получения размеров View, размещаем шары
        if (balls[0] != null) {
            for (int i = 0; i < balls.length; i++) {
                balls[i].reset(viewWidth, balls, i);
            }
        }
    }

    protected void update() {
        if (start_flag && health > 0) {
            long currentTime = System.currentTimeMillis();
            distance = (currentTime - gameStartTime) / 10f; // Делаем число красивым

            // Обновляем рекорд
            if (distance > recordDistance) {
                recordDistance = distance;
                getContext().getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
                        .edit()
                        .putFloat("recordDistance", recordDistance)
                        .apply();
            }
        }

        if (!start_flag) return;

        player.update(timerInterval);

        // Ограничение движения игрока по горизонтали
        if (player.getX() + player.getFrameWidth() > viewWidth) {
            player.setX(viewWidth - player.getFrameWidth());
            player.setVx(0);
        } else if (player.getX() < 0) {
            player.setX(0);
            player.setVx(0);
        }

        // Обновление шаров
        for (int i = 0; i < balls.length; i++) {
            // Проверка столкновения с игроком
            if (((balls[i].getX() > player.getX() - balls[i].getWidth()) &&
                    (balls[i].getX() < player.getX() + player.getFrameWidth())) &&
                    (balls[i].getY() + balls[i].getHeigth() >= player.getY())) {
                balls[i].reset(viewWidth, balls, i);
                health--;
            }

            // Если шар ушел за нижнюю границу
            if (balls[i].getY() > viewHeight) {
                balls[i].reset(viewWidth, balls, i);
            }

            balls[i].move();
        }

        // Проверка окончания игры
        if (health <= 0 && gameOverListener != null) {
            gameOverListener.onGameOver();
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint p = new Paint();
        p.setAntiAlias(true);

        if (start_flag) {
            // Рисуем игровой экран
            canvas.drawBitmap(background, null, new Rect(0, 0, viewWidth, viewHeight), p);

            // Рисуем игрока и шары
            player.draw(canvas);
            for (Ball ball : balls) {
                if (ball != null) {
                    ball.draw(canvas);
                }
            }

            // Рисуем здоровье
            p.setTextSize(55.0f);
            p.setColor(Color.WHITE);
            canvas.drawText("Health: " + health + "hp", 50, 100, p);
        } else {
            // Рисуем стартовый экран
            canvas.drawBitmap(startBackground, null, new Rect(0, 0, viewWidth, viewHeight), p);
        }

        if (start_flag && health > 0) {
            // Отображаем текущее расстояние
            canvas.drawText("Distance: " + String.format("%.1f", distance) + "m", 10, 160, textPaint);
        }
    }

    class Timer extends CountDownTimer {
        public Timer() {
            super(Integer.MAX_VALUE, timerInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            update();
        }

        @Override
        public void onFinish() {
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!start_flag) return true;

        int eventAction = event.getAction();
        if (eventAction == MotionEvent.ACTION_DOWN) {
            if (event.getX() < player.getBoundingBoxRect().left) {
                player.setVx(-500);
            } else if (event.getX() > player.getBoundingBoxRect().right) {
                player.setVx(500);
            }
        }
        return true;
    }

    public void startGame() {
        start_flag = true;
        health = 10;
        gameStartTime = System.currentTimeMillis(); // Фиксируем время начала
        invalidate();
    }

    public void resetGame() {
        distance = 0;
        start_flag = false;
        health = 10;
        player.setX(viewWidth / 2f); // Центрируем игрока
        player.setVx(0);

        // Сбрасываем позиции шаров
        for (int i = 0; i < balls.length; i++) {
            balls[i].reset(viewWidth, balls, i);
        }

        invalidate();
    }

    public float getCurrentDistance() {
        return distance;
    }

    public float getRecordDistance() {
        return recordDistance;
    }
}