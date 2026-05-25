package com.dlawoals2713.oui4.file;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.audiofx.Visualizer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;


public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private final IBinder binder = new LocalBinder();
    private Handler handler = new Handler();
    private Runnable updateProgressRunnable;

    // 현재 재생 중인 곡 정보
    private String currentTitle = "";
    private String currentArtist = "";
    private String currentAlbumArt = "";

    // 재생 상태
    private boolean isPlaying = false;
    private boolean isLooping = false;

    private float currentSpeed = 1.0f;
    private float currentPitch = 1.0f;

    // 인터페이스로 데이터 전송
    private MusicServiceListener listener;

    private static final String CHANNEL_ID = "music_playback_channel";
    private static final int NOTIFICATION_ID = 1;

    private Visualizer visualizer;
    private MusicServiceVisualizerListener visualizerListener;

    public interface MusicServiceVisualizerListener {
        void onFftDataCapture(byte[] fft);
    }

    public void setVisualizerListener(MusicServiceVisualizerListener listener) {
        this.visualizerListener = listener;
    }

    public interface MusicServiceListener {
        void onMusicStateChanged(boolean isPlaying);
        void onMusicInfoChanged(String title, String artist, String albumArt);
        void onProgressUpdated(int currentPosition);
    }

    public void setListener(MusicServiceListener listener) {
        this.listener = listener;
    }

    public class LocalBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(mp -> {
            isPlaying = false;
            if (listener != null) {
                listener.onMusicStateChanged(false); // 재생 완료 시 상태 업데이트
                stopForegroundNotification(); // 일시정지 또는 종료 시 알림 제거
            }
        });

        updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    if (listener != null) {
                        listener.onProgressUpdated(currentPosition);
                    }
                    handler.postDelayed(this, 500); // 0.5초마다 업데이트
                }
            }
        };
    }

    private void setupVisualizer() {
        if (visualizer != null) {
            visualizer.release();
        }
        try{
            visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

            visualizer.setDataCaptureListener(
                    new Visualizer.OnDataCaptureListener() {
                        @Override
                        public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {}
                        @Override
                        public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                            if (visualizerListener != null) {
                                visualizerListener.onFftDataCapture(fft);
                            }
                        }
                    },
                    Visualizer.getMaxCaptureRate(), // 캡처 속도 (Hz)
                    false, // 파형 데이터 캡처 비활성화
                    true   // FFT 데이터 캡처 활성화
            );
            visualizer.setEnabled(true);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        // Visualizer 리소스 해제
        if (visualizer != null) {
            visualizer.release();
            visualizer = null;
        }
        handler.removeCallbacks(updateProgressRunnable);
    }

    // 음악 재생
    public void playMusic(String filePath) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(filePath);
                mediaPlayer.prepare();

                setupVisualizer();

                setLooping(isLooping);
                setPlaybackSpeed(currentSpeed);
                setPlaybackPitch(currentPitch);
                mediaPlayer.start();
                isPlaying = true;

                updateMusicInfo(filePath);

                // 재생 상태 전송
                if (listener != null) {
                    listener.onMusicStateChanged(true);
                }

                // 재생 위치 업데이트 시작
                handler.post(updateProgressRunnable);
                startForegroundWithNotification(); // 재생 시작 시 알림 표시
            }
        } catch (IOException e) {
            Log.e("MusicService", "Error playing music", e);
        }
    }

    // 음악 정지
    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            stopForegroundNotification(); // 일시정지 또는 종료 시 알림 제거
            isPlaying = false;
            if (listener != null) {
                listener.onMusicStateChanged(false);
            }
        }
    }

    // 음악 재생/정지 토글
    public void togglePlayPause() {
        if (isPlaying) {
            pauseMusic();
        } else {
            if (mediaPlayer != null) {
                mediaPlayer.start();
                isPlaying = true;
                if (listener != null) {
                    listener.onMusicStateChanged(true);
                }
                handler.post(updateProgressRunnable);
            }
        }
    }

    // 곡 정보 업데이트
    private void updateMusicInfo(String filePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            // 파일 경로를 기반으로 메타데이터 추출
            retriever.setDataSource(filePath);

            // 곡 제목 추출
            currentTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (currentTitle == null || currentTitle.isEmpty()) {
                // 타이틀이 없는 경우 파일 이름을 사용
                File file = new File(filePath);
                currentTitle = file.getName();
                // 파일 확장자 제거 (선택 사항)
                int lastDotIndex = currentTitle.lastIndexOf('.');
                if (lastDotIndex > 0) {
                    currentTitle = currentTitle.substring(0, lastDotIndex);
                }
            }

            // 아티스트 추출
            currentArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if (currentArtist == null || currentArtist.isEmpty()) {
                currentArtist = "알 수 없음"; // 아티스트가 없는 경우 기본값 설정
            }

            // 앨범 아트 추출 (비트맵 형태)
            byte[] albumArtBytes = retriever.getEmbeddedPicture();
            if (albumArtBytes != null) {
                // 비트맵을 Base64로 인코딩하여 문자열로 저장
                currentAlbumArt = Base64.encodeToString(albumArtBytes, Base64.DEFAULT);
            } else {
                currentAlbumArt = ""; // 앨범 아트가 없는 경우 빈 문자열
            }

            // 리스너를 통해 정보 전송
            if (listener != null) {
                listener.onMusicInfoChanged(currentTitle, currentArtist, currentAlbumArt);
            }
        } catch (Exception e) {
            Log.e("MusicService", "Error extracting metadata", e);
        } finally {
            try {
                retriever.release(); // 리소스 해제
            } catch (IOException e) {
                Log.e("MusicService", "Error releasing MediaMetadataRetriever", e);
            }
        }
    }

    // 현재 재생 위치 반환
    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    // 곡의 총 길이 반환
    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    // 재생 상태 반환
    public boolean isPlaying() {
        return isPlaying;
    }

    // 현재 곡 제목 반환
    public String getCurrentTitle() {
        return currentTitle;
    }

    // 현재 재생 중인 곡의 앨범 아트 반환
    public String getCurrentAlbumArt() {
        return currentAlbumArt;
    }

    // 현재 아티스트 반환
    public String getCurrentArtist() {
        return currentArtist;
    }

    // 재생 위치 이동
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public void setLooping(boolean looping) {
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(looping);
        }
        isLooping = looping;
    }

    public boolean isLooping() {
        return isLooping;
    }

    public void setPlaybackSpeed(float speed) {
        if (mediaPlayer != null) {
            currentSpeed = speed;
            try {
                PlaybackParams params = mediaPlayer.getPlaybackParams();
                params.setSpeed(speed);
                params.setPitch(currentPitch); // 기존 피치 유지
                mediaPlayer.setPlaybackParams(params);
                if (!isPlaying) {
                    pauseMusic();
                }
            } catch (Exception e) {
                Log.e("MusicService", "Error setting playback speed", e);
            }
        }
    }

    public void setPlaybackPitch(float pitch) {
        if (mediaPlayer != null) {
            currentPitch = pitch;
            try {
                PlaybackParams params = mediaPlayer.getPlaybackParams();
                params.setPitch(pitch);
                params.setSpeed(currentSpeed); // 기존 속도 유지
                mediaPlayer.setPlaybackParams(params);
                if (!isPlaying) {
                    pauseMusic();
                }
            } catch (Exception e) {
                Log.e("MusicService", "Error setting playback pitch", e);
            }
        }
    }

    public float getPlaybackSpeed() {
        return currentSpeed;
    }

    public float getPlaybackPitch() {
        return currentPitch;
    }

    private void startForegroundWithNotification() {
        createNotificationChannel();

        Intent intent = new Intent(this, FullPlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(currentTitle)
                .setContentText(currentArtist)
                .setSmallIcon(R.drawable.ic_music_icon)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void stopForegroundNotification() {
        stopForeground(true);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "음악 재생 알림",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}