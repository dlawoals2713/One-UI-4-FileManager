package com.dlawoals2713.oui4.file;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dlawoals2713.oui4.file.base.BaseThemeActivity;

import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;


public class UpdatelogappActivity extends BaseThemeActivity {
	private ToolbarLayout toolbarLayout;
	private TextView textview1;
	private TextView textview5;
	
	private Intent intent = new Intent();
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.updatelogapp);
		initialize(_savedInstanceState);
		
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
		} else {
			initializeLogic();
		}
	}

	@SuppressLint("MissingSuperCall")
	@Override
	public void onBackPressed() {
		intent.setClass(getApplicationContext(), SettingActivity.class);
		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
		finishAffinity();
	}

	@SuppressLint("UnspecifiedRegisterReceiverFlag")
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(backReceiver, new IntentFilter("com.dlawoals2713.HOME_BACK_PRESSED"));
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(backReceiver);
	}

	private final BroadcastReceiver backReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			onBackPressed();
		}
	};

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		textview1 = findViewById(R.id.textview1);
		textview5 = findViewById(R.id.textview5);
		
		textview1.setOnClickListener(_view -> {
            intent.setClass(getApplicationContext(), UpdatelogActivity.class);
            intent.putExtra("app", "0");
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        });
		
		textview5.setOnClickListener(_view -> {
            intent.setClass(getApplicationContext(), UpdatelogActivity.class);
            intent.putExtra("app", "1");
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        });
	}
	
	private void initializeLogic() {
		toolbarLayout = findViewById(R.id.toolbar_view);
		toolbarLayout.setNavigationButtonOnClickListener(view -> onBackPressed());
		if (!FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/Smart all in one/data/ex_app/camone_updatelog.txt"))) {
			textview5.setVisibility(View.GONE);
		}
		textview1.setBackground(new GradientDrawable() {
			public GradientDrawable getIns(int a, int b) {
				this.setCornerRadius(a);
				this.setColor(b);
				return this;
			}
		}.getIns((int)60, 0xFFFCFCFC));
		textview5.setBackground(new GradientDrawable() {
			public GradientDrawable getIns(int a, int b) {
				this.setCornerRadius(a);
				this.setColor(b);
				return this;
			}
		}.getIns((int)60, 0xFFFCFCFC));
		_DarkMode();
	}
	
	public void _DarkMode() {
		int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
		if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
			textview1.setBackground(new GradientDrawable() {
				public GradientDrawable getIns(int a, int b) {
					this.setCornerRadius(a);
					this.setColor(b);
					return this;
				}
			}.getIns((int)60, 0xFF171717));
			textview5.setBackground(new GradientDrawable() {
				public GradientDrawable getIns(int a, int b) {
					this.setCornerRadius(a);
					this.setColor(b);
					return this;
				}
			}.getIns((int)60, 0xFF171717));
		}
	}
}
