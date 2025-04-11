package com.example.frameratemonitor;

import android.os.Bundle;
import android.view.Choreographer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements Choreographer.FrameCallback {

    private TextView frameRateTextView;
    private Button toggleButton;
    private Choreographer choreographer;
    private final LinkedList<Long> frameTimes = new LinkedList<>();
    private static final long MONITORING_INTERVAL = 1000L; // Calculate FPS over 1 second
    private boolean isShowingFPS = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameRateTextView = findViewById(R.id.frameRateTextView);
        toggleButton = findViewById(R.id.toggleButton);
        choreographer = Choreographer.getInstance();

        toggleButton.setOnClickListener(v -> {
            isShowingFPS = !isShowingFPS;
            frameRateTextView.setVisibility(isShowingFPS ? View.VISIBLE : View.GONE);
            if (isShowingFPS) {
                choreographer.postFrameCallback(MainActivity.this);
            } else {
                choreographer.removeFrameCallback(MainActivity.this);
                frameRateTextView.setText("FPS: --");
                frameTimes.clear();
            }
        });
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        frameTimes.addLast(frameTimeNanos);
        long currentTimeNanos = System.nanoTime();

        while (frameTimes.peekFirst() != null && (currentTimeNanos - frameTimes.peekFirst()) >= MONITORING_INTERVAL * 1_000_000) {
            frameTimes.removeFirst();
        }

        if (frameTimes.size() > 1) {
            double fps = (double) (frameTimes.size() - 1) * 1_000_000_000.0 / (frameTimes.getLast() - frameTimes.getFirst());
            runOnUiThread(() -> frameRateTextView.setText(String.format("FPS: %.2f", fps)));
        }

        if (isShowingFPS) {
            choreographer.postFrameCallback(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isShowingFPS) {
            choreographer.postFrameCallback(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        choreographer.removeFrameCallback(this);
        frameTimes.clear();
    }
}