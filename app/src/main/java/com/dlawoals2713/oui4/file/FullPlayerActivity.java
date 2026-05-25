package com.dlawoals2713.oui4.file;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dlawoals2713.oui4.file.base.BaseThemeActivity;

import java.util.Locale;

import de.dlyt.yanndroid.oneui.widget.SeekBar;

public class FullPlayerActivity extends BaseThemeActivity {
    private FrameLayout fullPlayerLayout;
    private float initialY, dY;
    private boolean isDragging = false;
    private boolean advanced = false;

    private ImageView full_album_art;
    private ImageButton full_play_pause;
    private ImageButton full_close;
    private TextView full_song_title;
    private TextView full_artist;
    private SeekBar full_seekbar;
    private TextView full_current_position;
    private TextView full_total_duration;
    private TextView text_speed_label;
    private TextView text_pitch_label;
    private ImageButton full_sp_reset;
    private ImageButton full_manage;
    private LinearLayout group_manage;
    private FrameLayout full_player;

    private MusicService musicService;
    private boolean isServiceBound = false;
    private ImageButton full_loop;
    private boolean isLooping = false;

    private Handler handler = new Handler();
    private Runnable updateSeekBarRunnable;
    private SeekBar seekbar_speed;
    private SeekBar seekbar_pitch;

    private ImageButton full_sv;
    private LinearLayout group_sv; // 기존 멤버 변수
    private SoundVisualizerView soundVisualizerView; // 새로운 멤버 변수

    @SuppressLint({"ClickableViewAccessibility", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_player);

        overridePendingTransition(R.anim.bottom_enter, R.anim.none);

        fullPlayerLayout = findViewById(R.id.full_player);
        full_play_pause = findViewById(R.id.full_play_pause);
        full_close = findViewById(R.id.full_close);
        full_song_title = findViewById(R.id.full_song_title);
        full_artist = findViewById(R.id.full_artist);
        full_seekbar = findViewById(R.id.full_seekbar);
        full_album_art = findViewById(R.id.full_album_art);
        full_loop = findViewById(R.id.full_loop);
        full_current_position = findViewById(R.id.full_current_position);
        full_total_duration = findViewById(R.id.full_total_duration);
        seekbar_speed = findViewById(R.id.seekbar_speed);
        seekbar_pitch = findViewById(R.id.seekbar_pitch);
        text_speed_label = findViewById(R.id.text_speed_label);
        text_pitch_label = findViewById(R.id.text_pitch_label);
        full_sp_reset = findViewById(R.id.full_sp_reset);
        full_manage = findViewById(R.id.full_manage);
        group_manage = findViewById(R.id.group_manage);
        full_player = findViewById(R.id.full_player);
        group_sv = findViewById(R.id.group_sv);
        full_sv = findViewById(R.id.full_sv);
        soundVisualizerView = findViewById(R.id.sound_visualizer_view);

        // MusicService와 바인딩
        Intent serviceIntent = new Intent(this, MusicService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // 터치 이벤트 처리
        fullPlayerLayout.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialY = fullPlayerLayout.getY();
                    dY = event.getRawY() - initialY;
                    isDragging = true;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (isDragging) {
                        float newY = event.getRawY() - dY;
                        if (newY >= 0) { // 위로 이동 제한
                            fullPlayerLayout.setY(newY);
                        }
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    isDragging = false;
                    float finalY = fullPlayerLayout.getY();
                    float threshold = fullPlayerLayout.getHeight() * 0.25f; // 25% 이상 내려가면 닫기

                    if (finalY > threshold) {
                        animateClose();
                    } else {
                        animateRestore(); // 원래 위치로 복귀
                    }
                    return true;
            }
            return false;
        });

        // 재생/일시정지 버튼 클릭 리스너
        full_play_pause.setOnClickListener(v -> {
            if (isServiceBound) {
                musicService.togglePlayPause();
                updatePlayPauseButton(musicService.isPlaying());
            }
        });

        full_close.setOnClickListener(v -> animateClose());

        // SeekBar 업데이트
        updateSeekBarRunnable = new Runnable() {
            private long lastUpdateTime = 0;
            private static final int UPDATE_INTERVAL_PLAYING = 16; // 재생 중: 약 60fps (16ms)
            private static final int UPDATE_INTERVAL_PAUSED = 500; // 일시정지: 0.5초

            @Override
            public void run() {
                if (!isServiceBound || isFinishing()) {
                    return;
                }

                long currentTime = System.currentTimeMillis();
                boolean isPlaying = musicService.isPlaying();

                // 재생 상태에 따라 업데이트 주기 결정
                int updateInterval = isPlaying ? UPDATE_INTERVAL_PLAYING : UPDATE_INTERVAL_PAUSED;

                // 최소 업데이트 간격을 유지하면서 불필요한 업데이트 방지
                if (currentTime - lastUpdateTime >= updateInterval) {
                    lastUpdateTime = currentTime;

                    int currentPosition = musicService.getCurrentPosition();
                    int duration = musicService.getDuration();

                    // SeekBar 업데이트
                    full_seekbar.setMax(duration);
                    full_seekbar.setProgress(currentPosition);
                    full_current_position.setText(formatTime(currentPosition));
                    full_total_duration.setText(formatTime(duration));
                }

                // 다음 업데이트 예약 (재생 상태에 따라 다른 주기 사용)
                handler.postDelayed(this, isPlaying ? UPDATE_INTERVAL_PLAYING : UPDATE_INTERVAL_PAUSED);
            }
        };

        // SeekBar 변경 리스너
        full_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isServiceBound) {
                    musicService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        full_loop.setOnClickListener(v -> {
            if (isServiceBound) {
                isLooping = !isLooping;
                musicService.setLooping(isLooping);
                updateLoopButton();
            }
        });

        full_manage.setOnClickListener(v -> {
            advanced = !advanced;
            tm();
            if (advanced) {
                group_manage.setVisibility(View.VISIBLE);
            } else {
                group_manage.setVisibility(View.GONE);
            }
        });

        full_sp_reset.setOnClickListener(v -> {
            float speed = 1.0f; // 0.0 ~ 2.0 범위로 변환 가정
            float pitch = 1.0f; // 0.0 ~ 2.0 범위로 변환 가정
            musicService.setPlaybackSpeed(speed);
            musicService.setPlaybackPitch(pitch);
            text_speed_label.setText("속도: " + String.format(Locale.getDefault(), "%.2f", speed));
            text_pitch_label.setText("피치: " + String.format(Locale.getDefault(), "%.2f", pitch));
            seekbar_speed.setProgress(100);
            seekbar_pitch.setProgress(100);
        });

        seekbar_speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isServiceBound && fromUser) {
                    float speed = progress / 100.0f; // 0.0 ~ 2.0 범위로 변환 가정
                    musicService.setPlaybackSpeed(speed);
                    text_speed_label.setText("속도: " + String.format(Locale.getDefault(), "%.2f", speed));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekbar_pitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isServiceBound && fromUser) {
                    float pitch = progress / 100.0f; // 0.0 ~ 2.0 범위로 변환 가정
                    musicService.setPlaybackPitch(pitch);
                    text_pitch_label.setText("피치: " + String.format(Locale.getDefault(), "%.2f", pitch));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        full_sv.setOnClickListener(v -> {
            tm();
            if (group_sv.getVisibility() == View.GONE) {
                group_sv.setVisibility(View.VISIBLE);
            } else {
                group_sv.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 액티비티가 다시 포커스될 때 SeekBar 업데이트 시작
        if (isServiceBound) {
            updateSeekBarState();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 액티비티가 백그라운드로 갈 때 SeekBar 업데이트 중지
        handler.removeCallbacks(updateSeekBarRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        handler.removeCallbacks(updateSeekBarRunnable);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        animateClose();
    }

    // MusicService와의 연결을 관리하는 ServiceConnection
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            isServiceBound = true;

            // 초기 상태 업데이트
            updatePlayPauseButton(musicService.isPlaying());
            updateSongInfo();
            isLooping = musicService.isLooping(); // 루프 상태 동기화
            updateLoopButton(); // 루프 버튼 업데이트

            float speed = musicService.getPlaybackSpeed();
            float pitch = musicService.getPlaybackPitch();
            text_speed_label.setText("속도: " + String.format(Locale.getDefault(), "%.2f", speed));
            text_pitch_label.setText("피치: " + String.format(Locale.getDefault(), "%.2f", pitch));
            seekbar_speed.setProgress((int) (speed * 100));
            seekbar_pitch.setProgress((int) (pitch * 100));

            handler.post(() -> {
                full_seekbar.setMax(musicService.getDuration());
                full_seekbar.setProgress(musicService.getCurrentPosition());
            });

            musicService.setListener(new MusicService.MusicServiceListener() {
                @Override
                public void onMusicStateChanged(boolean isPlaying) {
                    runOnUiThread(() -> {
                        updatePlayPauseButton(isPlaying);
                        if (isPlaying) {
                            // 재생 시작 시 SeekBar 업데이트 재개
                            handler.removeCallbacks(updateSeekBarRunnable);
                            handler.post(updateSeekBarRunnable);
                        } else {
                            // 일시정지 시 업데이트 중지
                            handler.removeCallbacks(updateSeekBarRunnable);
                        }
                    });
                }

                @Override
                public void onMusicInfoChanged(String title, String artist, String albumArt) {
                    runOnUiThread(() -> {
                        updateSongInfo();
                        // 새로운 곡 정보가 로드되면 SeekBar 리셋
                        full_seekbar.setMax(musicService.getDuration());
                        full_seekbar.setProgress(0);
                        // 재생 중이면 SeekBar 업데이트 재개
                        if (musicService.isPlaying()) {
                            handler.removeCallbacks(updateSeekBarRunnable);
                            handler.post(updateSeekBarRunnable);
                        }
                    });
                }

                @Override
                public void onProgressUpdated(int currentPosition) {
                    runOnUiThread(() -> {
                        if (!full_seekbar.isPressed()) { // 사용자가 터치 중이 아닐 때만 업데이트
                            full_seekbar.setProgress(currentPosition);
                        }
                    });
                }
            });

            if (soundVisualizerView != null) {
                soundVisualizerView.setContext(FullPlayerActivity.this);
            }

            musicService.setVisualizerListener(fft -> runOnUiThread(() -> {
                // soundVisualizerView가 null이 아닌지 확인
                if (soundVisualizerView != null) {
                    // UI 스레드에서 View 갱신
                    soundVisualizerView.updateVisualizer(fft);
                }
            }));

            // 현재 재생 상태에 따라 SeekBar 업데이트 시작/중지
            if (musicService.isPlaying()) {
                handler.post(updateSeekBarRunnable);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };

    // 재생/일시정지 버튼 업데이트
    private void updatePlayPauseButton(boolean isPlaying) {
        if (isPlaying) {
            full_play_pause.setImageResource(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_pause); // 재생 중일 때 아이콘
        } else {
            full_play_pause.setImageResource(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_play); // 정지 상태일 때 아이콘
        }
    }

    // 곡 정보 업데이트
    private void updateSongInfo() {
        if (isServiceBound) {
            full_song_title.setText(musicService.getCurrentTitle());
            full_artist.setText(musicService.getCurrentArtist());

            // 앨범 아트 업데이트
            String albumArt = musicService.getCurrentAlbumArt();
            if (albumArt != null && !albumArt.isEmpty()) {
                // Base64로 인코딩된 앨범 아트를 디코딩하여 표시
                Bitmap bitmap = getAlbumArtFromBase64(albumArt);
                if (bitmap != null) {
                    full_album_art.setImageBitmap(bitmap);
                } else {
                    full_album_art.setImageResource(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_file_type_audio); // 기본 앨범 아트
                }
            } else {
                full_album_art.setImageResource(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_file_type_audio); // 기본 앨범 아트
            }
        }
    }

    // Base64로 인코딩된 문자열을 비트맵으로 변환
    private Bitmap getAlbumArtFromBase64(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e("FullPlayerActivity", "Error decoding album art", e);
            return null;
        }
    }

    // 원래 위치로 복귀하는 애니메이션
    private void animateRestore() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(fullPlayerLayout, "translationY", fullPlayerLayout.getY(), 0);
        animator.setDuration(200);
        animator.start();
    }

    // 아래로 닫히는 애니메이션
    private void animateClose() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(fullPlayerLayout, "translationY", fullPlayerLayout.getY(), fullPlayerLayout.getHeight());
        animator.setDuration(300);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finish();
            }
        });
    }

    private void updateLoopButton() {
        if (isLooping) {
            full_loop.setImageResource(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_repeat); // 루프 활성화 아이콘
        } else {
            full_loop.setImageResource(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_move); // 루프 비활성화 아이콘
        }
    }

    private void updateSeekBarState() {
        if (!isServiceBound) return;

        int currentPosition = musicService.getCurrentPosition();
        int duration = musicService.getDuration();
        full_seekbar.setMax(duration);
        full_seekbar.setProgress(currentPosition);

        if (musicService.isPlaying()) {
            handler.post(updateSeekBarRunnable);
        }
    }

    private String formatTime(int millis) {
        int totalSeconds = millis / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private void tm() {
        android.transition.AutoTransition autoTransition = new android.transition.AutoTransition();
        autoTransition.setDuration(200);
        android.transition.TransitionManager.beginDelayedTransition(full_player, autoTransition);
    }
}