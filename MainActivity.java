package com.example.samsung_project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView recordTextView;
    private TextView lastDistanceTextView;
    private GameView gameView;
    private View startScreen;
    private View failScreen;
    private Button startButton;
    private Button restartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.gameView);
        startScreen = findViewById(R.id.startScreen);
        failScreen = findViewById(R.id.failScreen);
        startButton = findViewById(R.id.startButton);
        restartButton = findViewById(R.id.restartButton);

        startButton.setOnClickListener(v -> {
            startScreen.setVisibility(View.GONE);
            gameView.setVisibility(View.VISIBLE);
            gameView.startGame();
        });

        restartButton.setOnClickListener(v -> {
            failScreen.setVisibility(View.GONE);
            startScreen.setVisibility(View.VISIBLE); // Возвращаем на стартовый экран
            gameView.resetGame(); // Сбрасываем состояние игры
        });

        gameView.setGameOverListener(() -> {
            gameView.setVisibility(View.GONE);
            failScreen.setVisibility(View.VISIBLE);
        });

        recordTextView = findViewById(R.id.recordTextView);
        lastDistanceTextView = findViewById(R.id.lastDistanceTextView);

        updateRecordViews();

        restartButton.setOnClickListener(v -> {
            failScreen.setVisibility(View.GONE);
            startScreen.setVisibility(View.VISIBLE);
            updateRecordViews(); // Обновляем отображение рекордов
            gameView.resetGame();
        });

        gameView.setGameOverListener(() -> {
            lastDistanceTextView.setText("Last run: " +
                    String.format("%.1f", gameView.getCurrentDistance()) + " m");
            gameView.setVisibility(View.GONE);
            failScreen.setVisibility(View.VISIBLE);
        });
    }

    private void updateRecordViews() {
        recordTextView.setText("Record: " +
                String.format("%.1f", gameView.getRecordDistance()) + " m");
    }
}