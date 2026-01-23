package com.dlawoals2713.oui4.file;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.dlawoals2713.oui4.file.base.BaseThemeActivity;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dlyt.yanndroid.oneui.widget.ProgressBar;
import de.dlyt.yanndroid.oneui.widget.SeekBar;

public class FilemanagerVLCActivity extends BaseThemeActivity {

    private static final int UI_OPTIONS = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(UI_OPTIONS);
    }

    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;

    private LinearLayout overlayControls;
    private ImageButton btnPlayPause;
    private ImageButton btnLoop;

    private boolean isPlaying = true;
    private boolean isLooping = false;

    private SeekBar seekBar;
    private boolean isUserSeeking = false;
    private TextView tvCurrentTime, tvTotalTime;
    private ProgressBar play_loading;
    private ProgressBar pip_loading;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filemanager_vlc);

        VLCVideoLayout videoLayout = findViewById(R.id.video_layout);
        overlayControls = findViewById(R.id.overlay_controls);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        ImageButton btnRewind = findViewById(R.id.btn_rewind);
        ImageButton btnForward = findViewById(R.id.btn_forward);
        btnLoop = findViewById(R.id.btn_loop);
        ImageButton btnPip = findViewById(R.id.btn_pip);
        View videoRootView = findViewById(R.id.root_layout);
        seekBar = findViewById(R.id.seek_bar);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        play_loading = findViewById(R.id.play_loading);
        pip_loading = findViewById(R.id.pip_loading);

        libVLC = new LibVLC(this);
        mediaPlayer = new MediaPlayer(libVLC);
        mediaPlayer.attachViews(videoLayout, null, false, false);

        String videoPath = getIntent().getStringExtra("path");
        boolean isExternal = videoPath.startsWith("http://") || videoPath.startsWith("https://") || videoPath.startsWith("rtsp://") || videoPath.startsWith("rtmp://") || videoPath.startsWith("udp://") || videoPath.startsWith("mms://");

        mediaPlayer.setEventListener(event -> {
            switch (event.type) {
                case MediaPlayer.Event.Playing:
                    if (play_loading.getVisibility() == View.VISIBLE) {
                        play_loading.setVisibility(View.GONE);
                        btnPlayPause.setVisibility(View.VISIBLE);
                    }
                    break;
                case MediaPlayer.Event.Buffering:
                case MediaPlayer.Event.ESAdded:
                    break;
                case MediaPlayer.Event.EndReached:
                    runOnUiThread(() -> {
                        if (isLooping) {
                            mediaPlayer.setTime(0);
                            mediaPlayer.play();
                            if (play_loading.getVisibility() == View.GONE) {
                                play_loading.setVisibility(View.VISIBLE);
                                btnPlayPause.setVisibility(View.GONE);
                            }
                        } else {
                            mediaPlayer.pause();
                            btnPlayPause.setImageResource(R.drawable.ic_samsung_play_dark);
                            isPlaying = false;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                updatePipActions();
                            }
                        }
                    });
                    break;
            }
        });

        try {
            Media media;
            if (isExternal) {
                // 외부 URL인 경우 Uri 객체를 사용합니다.
                media = new Media(libVLC, Uri.parse(videoPath));
                SketchwareUtil.showMessage(this, "영상 불러오는 중...");
                play_loading.setVisibility(View.VISIBLE);
                btnPlayPause.setVisibility(View.GONE);
            } else {
                // 로컬 파일 경로인 경우 String 객체를 사용합니다.
                media = new Media(libVLC, videoPath);
            }

            // 💡 VLC 캐싱 옵션들
            media.addOption(":network-caching=" + Setting.getFileVLCNetworkBuffer(this));
            media.addOption(":file-caching=3000");
            media.addOption(":live-caching=3000");
            media.addOption(":codec=avcodec");

            media.parseAsync(); // 미디어 메타데이터 비동기 파싱

            media.setEventListener(event -> {
                if (event.type == Media.Event.ParsedChanged) {
                    mediaPlayer.setMedia(media);
                    media.release();
                    mediaPlayer.play();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "미디어 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_PLAY_PAUSE");
        filter.addAction("ACTION_LOOP_TOGGLE");
        registerReceiver(pipActionReceiver, filter);

        btnPip.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                // 로딩 UI 표시
                if (pip_loading.getVisibility() == View.GONE && isExternal) {
                    pip_loading.setVisibility(View.VISIBLE);
                    btnPip.setVisibility(View.GONE);
                }

                // 백그라운드 스레드에서 작업 실행
                executorService.execute(() -> {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    Rational aspectRatio = null;
                    try {
                        // 이 부분이 백그라운드 스레드에서 실행됩니다.
                        retriever.setDataSource(videoPath);
                        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                        int videoWidth = Integer.parseInt(width);
                        int videoHeight = Integer.parseInt(height);
                        aspectRatio = new Rational(videoWidth, videoHeight);

                        // UI 업데이트는 메인 스레드에서 실행
                        Rational finalAspectRatio = aspectRatio;
                        mainHandler.post(() -> {
                            if (isFinishing()) {
                                return;
                            }
                            // PIP 파라미터 빌드 및 PIP 모드 진입
                            // 이 부분은 기존 코드와 동일
                            int pendingIntentFlags;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
                            } else {
                                pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
                            }

                            // 재생/정지 액션
                            Icon playPauseIcon = Icon.createWithResource(
                                    this,
                                    isPlaying ? R.drawable.ic_samsung_pause_dark : R.drawable.ic_samsung_play_dark
                            );
                            PendingIntent playPauseIntent = PendingIntent.getBroadcast(
                                    this,
                                    0,
                                    new Intent("ACTION_PLAY_PAUSE"),
                                    pendingIntentFlags
                            );
                            RemoteAction playPauseAction = new RemoteAction(
                                    playPauseIcon,
                                    isPlaying ? "일시정지" : "재생",
                                    isPlaying ? "일시정지" : "재생",
                                    playPauseIntent
                            );

                            // 루프 액션
                            Icon loopIcon = Icon.createWithResource(
                                    this,
                                    isLooping ? R.drawable.ic_samsung_repeat_dark : R.drawable.ic_samsung_move_dark
                            );
                            PendingIntent loopIntent = PendingIntent.getBroadcast(
                                    this,
                                    1,
                                    new Intent("ACTION_LOOP_TOGGLE"),
                                    pendingIntentFlags
                            );
                            RemoteAction loopAction = new RemoteAction(
                                    loopIcon,
                                    isLooping ? "루프 해제" : "루프 설정",
                                    isLooping ? "루프 해제" : "루프 설정",
                                    loopIntent
                            );

                            // PIP 파라미터 빌드
                            ArrayList<RemoteAction> actions = new ArrayList<>();
                            actions.add(playPauseAction);
                            actions.add(loopAction);

                            PictureInPictureParams params = new PictureInPictureParams.Builder()
                                    .setAspectRatio(finalAspectRatio)
                                    .setActions(actions)
                                    .build();

                            enterPictureInPictureMode(params);
                            overlayControls.setVisibility(View.GONE);

                            // 로딩 UI 숨기기
                            if (pip_loading.getVisibility() == View.VISIBLE && isExternal) {
                                pip_loading.setVisibility(View.GONE);
                                btnPip.setVisibility(View.VISIBLE);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        // 오류 처리도 메인 스레드에서
                        mainHandler.post(() -> {
                            Toast.makeText(
                                    FilemanagerVLCActivity.this,
                                    "PIP 실행 오류: " + e.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();

                            // 로딩 UI 숨기기
                            if (pip_loading.getVisibility() == View.VISIBLE && isExternal) {
                                pip_loading.setVisibility(View.GONE);
                                btnPip.setVisibility(View.VISIBLE);
                            }
                        });
                    } finally {
                        // 리소스 정리
                        try {
                            retriever.release();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                Toast.makeText(
                        FilemanagerVLCActivity.this,
                        "이 기기는 PIP를 지원하지 않아요",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Controls
        btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                mediaPlayer.pause();
                btnPlayPause.setImageResource(R.drawable.ic_samsung_play_dark);
            } else {
                mediaPlayer.play();
                btnPlayPause.setImageResource(R.drawable.ic_samsung_pause_dark);
            }
            isPlaying = !isPlaying;
        });

        btnRewind.setOnClickListener(v -> {
            long newTime = mediaPlayer.getTime() - 5000;
            mediaPlayer.setTime(Math.max(newTime, 0));
        });

        btnForward.setOnClickListener(v -> {
            long newTime = mediaPlayer.getTime() + 5000;
            mediaPlayer.setTime(newTime);
        });

        btnLoop.setOnClickListener(v -> {
            isLooping = !isLooping;
            btnLoop.setImageResource(isLooping ? R.drawable.ic_samsung_repeat_dark : R.drawable.ic_samsung_move_dark);
        });

        // Show/hide overlay on video tap
        videoRootView.setOnClickListener(v -> {
            if (overlayControls.getVisibility() == View.VISIBLE) {
                overlayControls.setVisibility(View.GONE);
            } else {
                overlayControls.setVisibility(View.VISIBLE);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                long duration = mediaPlayer.getLength();
                long newTime = (duration * seekBar.getProgress()) / 100;
                mediaPlayer.setTime(newTime);
                isUserSeeking = false;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 사용자가 조작 중일 때 별도 처리하고 싶으면 여기에 표시용 코드만 작성
            }
        });

        handler.post(updateSeekBarRunnable);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }

            // 배경 투명 및 시스템 바 관련 플래그 설정
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);

            hideSystemUI();

            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    hideSystemUI();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(updateSeekBarRunnable);

        if (mediaPlayer != null) {
            // ⭐ 추가: 뷰와의 연결을 먼저 해제
            mediaPlayer.detachViews();
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (libVLC != null) {
            libVLC.release();
            libVLC = null;
        }

        try {
            unregisterReceiver(pipActionReceiver);
        } catch (Exception e) {
            // 이미 해제된 경우 무시
        }

        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!isInPictureInPictureMode()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("ACTION_PLAY_PAUSE");
            filter.addAction("ACTION_LOOP_TOGGLE");
            registerReceiver(pipActionReceiver, filter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isInPictureInPictureMode()) {
            unregisterReceiver(pipActionReceiver);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        finishAndRemoveTask(); // 액티비티 종료 + 태스크(최근 앱)에서 제거
    }

    private final android.os.Handler handler = new android.os.Handler();

    private final Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying() && !isUserSeeking) {
                long current = mediaPlayer.getTime();
                long total = mediaPlayer.getLength();
                if (total > 0) {
                    int progress = (int) (100 * current / total);
                    seekBar.setProgress(progress);

                    // 시간 업데이트
                    tvCurrentTime.setText(formatTime(current));
                    tvTotalTime.setText(formatTime(total));
                }
            }
            handler.postDelayed(this, 100);
        }
    };

    private String formatTime(long milliseconds) {
        int totalSeconds = (int) (milliseconds / 1000);

        if (totalSeconds >= 3600) {
            // 1시간 이상인 경우 (HH:MM:SS)
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            // 1시간 미만인 경우 (MM:SS)
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    // BroadcastReceiver 로그 추가
    private final BroadcastReceiver pipActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mediaPlayer == null || libVLC == null) {
                Log.e("PIP", "Player not initialized");
                return;
            }

            runOnUiThread(() -> {
                try {
                    switch (intent.getAction()) {
                        case "ACTION_PLAY_PAUSE":
                            if (isPlaying) {
                                mediaPlayer.pause();
                            } else {
                                mediaPlayer.play();
                            }
                            isPlaying = !isPlaying;
                            break;
                        case "ACTION_LOOP_TOGGLE":
                            isLooping = !isLooping;
                            break;
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isInPictureInPictureMode()) {
                        updatePipActions();
                    }
                } catch (Exception e) {
                    Log.e("PIP", "Action error", e);
                }
            });
        }
    };

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // PIP 모드 진입 시 리시버를 해제하지 않도록 변경
    }

    // PIP 액션 업데이트 메서드 추가
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updatePipActions() {
        if (!isInPictureInPictureMode()) return;

        int pendingIntentFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;

        Icon playPauseIcon = Icon.createWithResource(this,
                isPlaying ? R.drawable.ic_samsung_pause_dark : R.drawable.ic_samsung_play_dark);
        PendingIntent playPauseIntent = PendingIntent.getBroadcast(this, 0, new Intent("ACTION_PLAY_PAUSE"), pendingIntentFlags);
        RemoteAction playPauseAction = new RemoteAction(playPauseIcon,
                isPlaying ? "일시정지" : "재생", isPlaying ? "일시정지" : "재생", playPauseIntent);

        Icon loopIcon = Icon.createWithResource(this,
                isLooping ? R.drawable.ic_samsung_repeat_dark : R.drawable.ic_samsung_move_dark);
        PendingIntent loopIntent = PendingIntent.getBroadcast(this, 1, new Intent("ACTION_LOOP_TOGGLE"), pendingIntentFlags);
        RemoteAction loopAction = new RemoteAction(loopIcon,
                isLooping ? "루프 해제" : "루프 설정", isLooping ? "루프 해제" : "루프 설정", loopIntent);

        PictureInPictureParams newParams = new PictureInPictureParams.Builder()
                .setActions(Arrays.asList(playPauseAction, loopAction))
                .build();
        setPictureInPictureParams(newParams);
    }
}