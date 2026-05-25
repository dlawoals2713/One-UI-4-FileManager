package com.dlawoals2713.oui4.file;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.PictureInPictureParams;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.dlawoals2713.oui4.file.base.BaseThemeActivity;
import com.dlawoals2713.oui4.file.databinding.FilemanagerVideoBinding;

public class FilemanagerVideoActivity extends BaseThemeActivity {
	private FilemanagerVideoBinding binding;
	private static final int UI_OPTIONS = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

	private void hideSystemUI() {
		ActionBar actionBar = getActionBar();
		if (actionBar != null) actionBar.hide();
		getWindow().getDecorView().setSystemUiVisibility(UI_OPTIONS);
	}

	private String url = "";

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = FilemanagerVideoBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initializeLogic();
	}

	@SuppressLint("MissingSuperCall")
    @Override
	public void onBackPressed() {
		finishAndRemoveTask();
	}

	private void initializeLogic() {
		_Video_Player(getIntent().getStringExtra("path"));
		getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		SketchwareUtil.showMessage(getApplicationContext(), "PIP 모드를 실행하려면 영상을 길게 누르세요");
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
		}
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		getWindow().setStatusBarColor(Color.TRANSPARENT);
		// this lines ensure only the status-bar to become transparent without affecting the nav-bar
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		hideSystemUI();

		getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                hideSystemUI();
            }
        });
	}

	public void _Video_Player(final String _url) {
		url = _url;
		final VideoView vd = new VideoView(FilemanagerVideoActivity.this);
		vd.setLayoutParams(new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.MATCH_PARENT));
		binding.linear1.addView(vd);
		vd.setVideoURI(Uri.parse(url));
		vd.setMediaController(new MediaController(this));
		vd.requestFocus();
		vd.start();
		vd.setOnLongClickListener(_view -> {
            if (Build.VERSION.SDK_INT >= 26) {
                //Trigger PiP mode
                try {
                    Rational rational = new
                            Rational(vd.getWidth(),
                            vd.getHeight());
                    PictureInPictureParams mParams =
                            new PictureInPictureParams.Builder()
                                    .setAspectRatio(rational)
                                    .build();
                    enterPictureInPictureMode(mParams);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(FilemanagerVideoActivity.this, "API 버전이 26보다 낮아서 PiP를 실행할 수 없습니다", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
	}
}