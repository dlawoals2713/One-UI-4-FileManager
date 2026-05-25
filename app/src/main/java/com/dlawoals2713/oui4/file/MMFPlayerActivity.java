package com.dlawoals2713.oui4.file;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.Choreographer;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.media.session.MediaButtonReceiver;

import com.dlawoals2713.oui4.file.base.BaseThemeActivity;
import com.dlawoals2713.oui4.file.databinding.ActivityMmfPlayerBinding;
import com.yamaha.smafsynth.m7.emu.DataParsers;
import com.yamaha.smafsynth.m7.emu.EmuSmw7;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MMFPlayerActivity extends BaseThemeActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, EmuSmw7.ChannelDataListener {

    private ActivityMmfPlayerBinding binding;
    private static final int PICK_MMF_FILE = 1;

    private EmuSmw7 emuSmw7;
    private int sampleRate = 22050;
    private byte[] mmfData;
    private String currentFileName;
    public DataParsers dataParser;

    private SharedPreferences sp;

    private Choreographer.FrameCallback frameCallback;
    private boolean isUpdating = false;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Intent intent = new Intent();
    private MediaSessionCompat mediaSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMmfPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                1);

        sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
        currentFileName = getString(R.string.mmf_file_unselected);
        initUI();
        emuSmw7 = new EmuSmw7();
        emuSmw7.EmuSmw7Init(); // EmuSmw7 초기화
        emuSmw7.setChannelDataListener(this); // 채널 데이터 리스너 설정

        ComponentName mediaButtonReceiver = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
        mediaSession = new MediaSessionCompat(this, "MMFPlayer", mediaButtonReceiver, null);
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // MediaSession 콜백 설정 (재생, 정지 명령 처리)
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                // 재생 버튼 클릭 시의 로직
                if (mmfData != null && emuSmw7.getPlaybackStatus() != 2) {
                    emuSmw7.startPlayback(mmfData, 100L, -1L, 15L, 32, 32);
                    startPlaybackUpdates();
                    updateButtonStates();
                    updateMediaSessionState();
                }
            }

            @Override
            public void onPause() {
                super.onPause();
                // 정지/일시정지 버튼 클릭 시의 로직
                if (emuSmw7.getPlaybackStatus() == 2) {
                    emuSmw7.stopPlayback();
                    stopPlaybackUpdates();
                    updateButtonStates();
                    updateMediaSessionState();
                }
            }
        });

        mediaSession.setActive(true);

        try {
            MediaControllerCompat mediaController = new MediaControllerCompat(this, mediaSession.getSessionToken());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        Uri fileUri = getIntent().getData();
        if (fileUri != null) {
            try {
                currentFileName = getFileNameFromUri(fileUri);
                binding.textFileName.setText(currentFileName);

                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                if (inputStream != null) {
                    mmfData = new byte[inputStream.available()];
                    inputStream.read(mmfData);
                    inputStream.close();

                    parseAndDisplayMetadata();

                    Toast.makeText(this, getString(R.string.mmf_file_loaded, currentFileName), Toast.LENGTH_SHORT).show();
                    updateButtonStates();
                    Log.d("MMFPlayer", "파일 URI: " + fileUri.toString());
                }
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.mmf_file_loadError, e.getMessage()), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Log.d("MMFPlayer", "Error: File URI is null");
        }
    }

    private void initUI() {
        LinearLayout content = findViewById(R.id.content);
        content.setClipChildren(false);
        content.setClipToPadding(false);

        sampleRate = Integer.parseInt(sp.getString("sr", "22050"));

        binding.textFileName.setText(currentFileName);

        binding.buttonInit.setOnClickListener(this);
        binding.buttonPlay.setOnClickListener(this);
        binding.buttonStop.setOnClickListener(this);
        binding.buttonRelease.setOnClickListener(this);

        binding.seekbarProgress.setEnabled(false);
        binding.seekbarVolume.setMax(127);
        binding.seekbarVolume.setProgress(100);
        binding.seekbarVolume.setOnSeekBarChangeListener(this);

        binding.toolbarView.inflateToolbarMenu(R.menu.player);
        binding.toolbarView.getToolbarMenu().findItem(R.id.settings).setTitle(getString(R.string.mmf_setting));
        binding.toolbarView.setOnToolbarMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.settings) {
                setting();
            } else if (itemId == R.id.open) {
                open();
            }

            return true;
        });
        updateButtonStates();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_init) {
            String rateText = String.valueOf(sampleRate).trim();
            sp.edit().putString("sr", rateText).apply();

            try {
                sampleRate = Integer.parseInt(rateText);
                if (sampleRate < 11025) sampleRate = 11025;
                if (sampleRate > 48000) sampleRate = 48000;

                // 기존 리소스 정리
                if (emuSmw7 != null) {
                    emuSmw7.releaseAudioResources();
                }

                long result = emuSmw7.startAudio(sampleRate);

                runOnUiThread(() -> {
                    if (result >= 0) {
                        updateButtonStates();
                        Toast.makeText(MMFPlayerActivity.this, getString(R.string.mmf_init_success), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MMFPlayerActivity.this, getString(R.string.mmf_init_fail), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(this, getString(R.string.mmf_init_error), Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.button_play) {
            if (mmfData == null) {
                Toast.makeText(this, getString(R.string.mmf_play_unselected), Toast.LENGTH_SHORT).show();
                return;
            }

            parseAndDisplayMetadata();
            emuSmw7.startPlayback(mmfData, 100L, -1L, 15L, 32, 32);
            emuSmw7.setPlaybackVolume(binding.seekbarVolume.getProgress());

            startPlaybackUpdates();
            updateButtonStates();
            updateMediaSessionMetadata();
            updateMediaSessionState();
        } else if (id == R.id.button_stop) {
            emuSmw7.stopPlayback();
            stopPlaybackUpdates();
            updateButtonStates();
            updateMediaSessionState();
        } else if (id == R.id.button_release) {
            emuSmw7.releaseAudioResources();
            stopPlaybackUpdates();
            updateButtonStates();
            updateMediaSessionState();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_MMF_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    if (inputStream != null) {
                        mmfData = new byte[inputStream.available()];
                        inputStream.read(mmfData);
                        inputStream.close();

                        currentFileName = getFileName(uri);
                        binding.textFileName.setText(currentFileName);

                        parseAndDisplayMetadata();

                        Toast.makeText(this, getString(R.string.mmf_file_loaded, currentFileName), Toast.LENGTH_SHORT).show();
                        updateButtonStates();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, getString(R.string.mmf_file_notFound), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, getString(R.string.mmf_file_error), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void parseAndDisplayMetadata() {
        if (mmfData != null) {
            try {
                dataParser = new DataParsers(mmfData);
                updateMetadataDisplay();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, getString(R.string.mmf_parse_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateMetadataDisplay() {
        if (dataParser != null) {
            binding.textTitle.setText(getString(R.string.mmf_title, (dataParser.getTitle().isEmpty() ? getString(R.string.mmf_data_unknown) : dataParser.getTitle())));
            binding.textArtist.setText(getString(R.string.mmf_artist, (dataParser.getArtistName().isEmpty() ? getString(R.string.mmf_data_unknown) : dataParser.getArtistName())));
            binding.textCopyright.setText(getString(R.string.mmf_copyright, (dataParser.getCopyrightInfo().isEmpty() ? getString(R.string.mmf_data_unknown) : dataParser.getCopyrightInfo())));
            binding.textGenre.setText(getString(R.string.mmf_genre, (dataParser.getGenre().isEmpty() ? getString(R.string.mmf_data_unknown) : dataParser.getGenre())));
            binding.textMisc.setText(getString(R.string.mmf_etc, (dataParser.getMiscInfo().isEmpty() ? getString(R.string.mmf_data_unknown) : dataParser.getMiscInfo())));
        } else {
            binding.textTitle.setText(getString(R.string.mmf_title, getString(R.string.mmf_data_unknown)));
            binding.textArtist.setText(getString(R.string.mmf_artist, getString(R.string.mmf_data_unknown)));
            binding.textCopyright.setText(getString(R.string.mmf_copyright, getString(R.string.mmf_data_unknown)));
            binding.textGenre.setText(getString(R.string.mmf_genre, getString(R.string.mmf_data_unknown)));
            binding.textMisc.setText(getString(R.string.mmf_etc, getString(R.string.mmf_data_unknown)));
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void updateButtonStates() {
        if (emuSmw7 == null) {
            int cardBackground = getResources().getColor(R.color.mmf_cardBackground);
            int primaryColor = getResources().getColor(R.color.mmf_primaryColor);
            resetButtons(cardBackground);
            binding.buttonInit.setEnabled(true);
            binding.buttonInit.setImageTintList(ColorStateList.valueOf(primaryColor));
            return;
        }

        long state = emuSmw7.getPlaybackStatus();

        int primaryColor = getResources().getColor(R.color.mmf_primaryColor);
        int cardBackground = getResources().getColor(R.color.mmf_cardBackground);
        int errorColor = getResources().getColor(R.color.mmf_errorColor);

        resetButtons(cardBackground);

        switch ((int) state) {
            case 0: // 초기화되지 않음
                binding.buttonInit.setEnabled(true);
                binding.buttonInit.setImageTintList(ColorStateList.valueOf(primaryColor));
                break;
            case 1: // 초기화됨 (재생 준비 상태)
                if (mmfData != null) {
                    binding.buttonPlay.setEnabled(true);
                    binding.buttonPlay.setImageTintList(ColorStateList.valueOf(primaryColor));
                }
                binding.buttonRelease.setEnabled(true);
                binding.buttonRelease.setImageTintList(ColorStateList.valueOf(errorColor));
                break;
            case 2: // 재생 중
                binding.buttonStop.setEnabled(true);
                binding.buttonStop.setImageTintList(ColorStateList.valueOf(primaryColor));
                binding.buttonRelease.setEnabled(true);
                binding.buttonRelease.setImageTintList(ColorStateList.valueOf(errorColor));
                break;
        }
    }

    private void resetButtons(int defaultColor) {
        ColorStateList defaultTint = ColorStateList.valueOf(defaultColor);

        binding.buttonInit.setEnabled(false);
        binding.buttonInit.setImageTintList(defaultTint);

        binding.buttonPlay.setEnabled(false);
        binding.buttonPlay.setImageTintList(defaultTint);

        binding.buttonStop.setEnabled(false);
        binding.buttonStop.setImageTintList(defaultTint);

        binding.buttonRelease.setEnabled(false);
        binding.buttonRelease.setImageTintList(defaultTint);
    }

    private void startPlaybackUpdates() {
        stopPlaybackUpdates();
        isUpdating = true;

        frameCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (emuSmw7 != null && isUpdating) {
                    long position = emuSmw7.getCurrentPlaybackPosition();
                    long length = emuSmw7.getAudioLength();

                    if (length > 0) {
                        binding.seekbarProgress.setMax((int) length);
                        binding.seekbarProgress.setProgress((int) position);

                        binding.textTime.setText(formatTime(position) + " / " + formatTime(length));
                    }
                }

                if (isUpdating) {
                    Choreographer.getInstance().postFrameCallback(this);
                }
            }
        };

        Choreographer.getInstance().postFrameCallback(frameCallback);
    }

    private void stopPlaybackUpdates() {
        isUpdating = false;
        if (frameCallback != null) {
            Choreographer.getInstance().removeFrameCallback(frameCallback);
            frameCallback = null;
        }
    }

    private String formatTime(long milliseconds) {
        int seconds = (int) (milliseconds / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == binding.seekbarVolume && emuSmw7 != null) {
            emuSmw7.setPlaybackVolume(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (emuSmw7 != null) {
            emuSmw7.stopPlayback();
            emuSmw7.releaseAudioResources();
        }
        stopPlaybackUpdates();
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    private void setting() {
        intent.setClass(getApplicationContext(), SettingActivity.class);
        startActivity(intent);
    }

    private void open() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_MMF_FILE);
    }

    @Override
    public void onResume() {
        super.onResume();
        sampleRate = Integer.parseInt(sp.getString("sr", "22050"));
    }

    private String getFileNameFromUri(Uri uri) {
        String displayName = "";
        try (Cursor cursor = getContentResolver().query(
                uri,
                new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME},
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                displayName = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            displayName = uri.getLastPathSegment();
        }
        return displayName;
    }

    @Override
    public void onChannelDataReady(int leftChannelValue, int rightChannelValue) {
        // 여기에 오디오 채널 데이터를 처리하는 코드를 추가할 수 있습니다.
        // 예: 시각화 업데이트 등
        // Log.d("ChannelData", "Left: " + leftChannelValue + ", Right: " + rightChannelValue);
    }

    private void updateMediaSessionState() {
        int state = (int) emuSmw7.getPlaybackStatus();
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_STOP
                        // 여기에 필요한 액션들 (다음 곡, 이전 곡 등) 추가
                );

        switch (state) {
            case 2: // 재생 중
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, emuSmw7.getCurrentPlaybackPosition(), 1.0f);
                break;
            case 1: // 초기화됨 (일시정지 상태로 간주)
                stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, emuSmw7.getCurrentPlaybackPosition(), 1.0f);
                break;
            case 0: // 초기화되지 않음 (정지 상태로 간주)
            default:
                stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f);
                break;
        }

        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private void updateMediaSessionMetadata() {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, dataParser != null && !dataParser.getTitle().isEmpty() ? dataParser.getTitle() : currentFileName)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, dataParser != null && !dataParser.getArtistName().isEmpty() ? dataParser.getArtistName() : getString(R.string.mmf_data_unknown))
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, emuSmw7 != null ? emuSmw7.getAudioLength() : 0);

        mediaSession.setMetadata(metadataBuilder.build());
    }
}