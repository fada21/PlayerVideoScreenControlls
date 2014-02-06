package com.example.player_control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    GestureDetector gest;

    public enum PlayerState {
        PREPARING, PAUSED, PLAYING, STOPPED;
    }

    private PlayerState              state;

    private View                     lines;
    private ImageView                progressSpinner;
    private AnimationDrawable        progressSpinnerDrawable;

    private ScheduledExecutorService scheduledExecutorService;

    private CircularProgressView     countDownProgress;

    private View                     playBtn;

    private View                     stopBtn;

    private TextView                 counter;

    private TextView                 playPauseUpperText;

    private TextView                 playPauseLowerText;

    private int                      width;

    public class PlayerControlsGestureDetector extends SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            float x = e.getX();
            onPlayerPlayPause(x > width / 2);
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float x = e.getX();
            onPlayerStop(x < width / 2);
            return super.onDoubleTap(e);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        gest.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lines = findViewById(R.id.player_lines);
        lines.setAlpha(0.5f);

        playBtn = findViewById(R.id.play_pause_btn);
        stopBtn = findViewById(R.id.stop_btn);

        playPauseUpperText = (TextView) findViewById(R.id.play_pause_text1);
        playPauseLowerText = (TextView) findViewById(R.id.play_pause_text2);

        progressSpinner = (ImageView) findViewById(R.id.player_buffering_progress);
        progressSpinnerDrawable = (AnimationDrawable) progressSpinner.getDrawable();

        countDownProgress = (CircularProgressView) findViewById(R.id.player_countdown_progress);
        counter = (TextView) findViewById(R.id.player_counter);

    }

    @Override
    protected void onResume() {
        gest = new GestureDetector(this, new PlayerControlsGestureDetector());
        width = this.getResources().getDisplayMetrics().widthPixels;
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        onPlayerLoading();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        scheduledExecutorService.shutdown();
        super.onDestroy();

    }

    private void onPlayerLoading() {
        state = PlayerState.PREPARING;
        updatePlayPauseText();
        AsyncTask<Integer, Void, Void> task = new AsyncTask<Integer, Void, Void>() {

            @Override
            protected Void doInBackground(Integer... params) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.d("error", "eroor");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                progressSpinnerDrawable.stop();
                ViewPropertyAnimator animator = progressSpinner.animate().alpha(0);
                animator.start();
                animator.setListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressSpinner.setVisibility(View.INVISIBLE);
                        onPlayerCountDown();
                    }

                });
                super.onPostExecute(result);
            }

        };

        progressSpinner.setVisibility(View.VISIBLE);
        progressSpinnerDrawable.start();
        task.execute(0);
    }

    private void updatePlayPauseText() {
        if (state == PlayerState.PLAYING || state == PlayerState.PREPARING) {
            playPauseUpperText.setText("PAUZA");
            playPauseLowerText.setText("Stuknij, aby zatrzymać trening");
        } else if (state == PlayerState.PAUSED) {
            playPauseUpperText.setText("WZNÓW");
            playPauseLowerText.setText("Stuknij, aby wznowić trening");
        }
    }

    // countdown section start ==============================================
    public class PlayerDownCounter implements Runnable {
        private final long startTime;
        private final long countDownFrom = 3000;
        private final long countEvery    = 1000;
        long               countNext     = 0;

        public PlayerDownCounter(long startTime) {
            this.startTime = startTime;

        }

        @Override
        public void run() {
            long now = System.currentTimeMillis();

            long elapsed = now - startTime;

            if (elapsed > countNext) {
                int curSec = (int) ((countDownFrom - countNext) / 1000);
                countNext += countEvery;
                updateSecondsText(curSec);
            }

            if (elapsed > countDownFrom) {
                scheduledExecutorService.shutdown();
                onPlayerCountDownComplete();
            }

            updateProgressCircle((float) elapsed / countDownFrom);

        }

    }

    private void onPlayerCountDown() {
        List<View> views = Arrays.asList(lines, playBtn, stopBtn);
        AnimatorSet animationSet = getFadeOutAnimatorSet(views, 3000);

        countDownProgress.setVisibility(View.VISIBLE);
        counter.setVisibility(View.VISIBLE);
        PlayerDownCounter dc = new PlayerDownCounter(System.currentTimeMillis());
        scheduledExecutorService.scheduleAtFixedRate(dc, 10, 10, TimeUnit.MILLISECONDS);

        animationSet.start();
    }

    private AnimatorSet getFadeOutAnimatorSet(List<View> views, long duration) {
        AnimatorSet animationSet = new AnimatorSet();
        List<Animator> animations = new ArrayList<Animator>();
        for (View view : views) {
            ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0);
            alpha.setDuration(duration);
            animations.add(alpha);
        }
        animationSet.playTogether(animations);
        return animationSet;
    }

    // countdown section end ==============================================

    synchronized private void updateProgressCircle(final float percentage) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                countDownProgress.setProgress(percentage);
            }
        });
    }

    private void updateSecondsText(final int second) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                counter.setAlpha(1f);
                counter.setText("" + second);
                if (second > 0) {
                    ViewPropertyAnimator animator = counter.animate().alpha(0f).setDuration(900);
                    animator.start();
                }
            }
        });
    }

    // countdown section end ==============================================

    private void onPlayerCountDownComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state = PlayerState.PLAYING;
                counter.setAlpha(1f);
                counter.setText("" + 0);
                Toast.makeText(MainActivity.this, "Count down complete", Toast.LENGTH_SHORT).show();
                AnimatorSet animatorSet = getFadeOutAnimatorSet(Arrays.asList(counter, countDownProgress), 2000);
                animatorSet.start();
            }
        });
    }

    public void onPlayerPlayPause(boolean showOnly) {
        if (state != PlayerState.PREPARING && state != PlayerState.STOPPED) {

            if (state == PlayerState.PAUSED) {
                state = PlayerState.PLAYING;
                onPlayerPlay();
            } else {
                if (showOnly) {
                    onShowControlls();
                    onHideControlls();
                } else {
                    state = PlayerState.PAUSED;
                    onPlayerPause();
                }
            }
            Toast.makeText(this, "Player w stanie: " + state, Toast.LENGTH_SHORT).show();
        }

    }

    public void onPlayerPlay() {
        onHideControlls();
    }

    public void onPlayerPause() {
        onShowControlls();
    }

    public void onShowControlls() {
        updatePlayPauseText();
        playBtn.setAlpha(1f);
        stopBtn.setAlpha(1f);
    }

    public void onHideControlls() {
        List<View> views = Arrays.asList(playBtn, stopBtn);
        AnimatorSet animationSet = getFadeOutAnimatorSet(views, 2000);
        animationSet.start();
    }

    public void onPlayerStop(boolean showOnly) {
        if (showOnly) {
            onShowControlls();
            onHideControlls();
        } else {
            state = PlayerState.STOPPED;
            Toast.makeText(this, "Player w stanie: " + state, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
