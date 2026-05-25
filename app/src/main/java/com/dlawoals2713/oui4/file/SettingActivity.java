package com.dlawoals2713.oui4.file;

import static com.dlawoals2713.oui4.file.Setting.getFileCache;
import static com.dlawoals2713.oui4.file.Setting.getFileImage;
import static com.dlawoals2713.oui4.file.Setting.getFileSVBar;
import static com.dlawoals2713.oui4.file.Setting.getFileSort;
import static com.dlawoals2713.oui4.file.Setting.getFileTitle;
import static com.dlawoals2713.oui4.file.Setting.getFileVLCNetworkBuffer;
import static com.dlawoals2713.oui4.file.Setting.getFileVideo;
import static com.dlawoals2713.oui4.file.Setting.setFileCache;
import static com.dlawoals2713.oui4.file.Setting.setFileImage;
import static com.dlawoals2713.oui4.file.Setting.setFileSVBar;
import static com.dlawoals2713.oui4.file.Setting.setFileSort;
import static com.dlawoals2713.oui4.file.Setting.setFileTitle;
import static com.dlawoals2713.oui4.file.Setting.setFileVLCNetworkBuffer;
import static com.dlawoals2713.oui4.file.Setting.setFileVideo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dlawoals2713.oui4.file.base.BaseThemeActivity;
import com.dlawoals2713.oui4.file.databinding.SettingBinding;

import de.dlyt.yanndroid.oneui.dialog.AlertDialog;
import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;

public class SettingActivity extends BaseThemeActivity {
	private SettingBinding binding;
	private Intent intent = new Intent();

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = SettingBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
		|| ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
		} else {
			initializeLogic();
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				startActivity(new Intent(SettingActivity.this, FilemanagerActivity.class));
			}
		});

		binding.themeToggle.setText((mUseOUI4Theme ? "OneUI 3" : "OneUI 4") + " 테마로 변경");
		binding.themeToggle.setOnClickListener(_view -> {
			switchOUITheme();
		});

		binding.textview12.setOnClickListener(_view -> {
			final String[] methods = {"유닛 이름", "폴더 이름"};
			String savedMethod = String.valueOf(getFileTitle(SettingActivity.this));
			final int[] checkedItem = {savedMethod.equals("0") ? 0 : 1};

			AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
			builder.setTitle("기본 타이틀 표시 방식")
					.setSingleChoiceItems(methods, checkedItem[0], (dialog, which) -> checkedItem[0] = which)
					.setPositiveButton("확인", (dialog, which) -> {
						setFileTitle(this, String.valueOf(checkedItem[0]));
						binding.textview12.setText("기본 타이틀 표시 방식: " + methods[checkedItem[0]]);
					})
					.setNegativeButton("취소", (dialog, which) -> {})
					.setCancelable(false)
					.create()
					.show();
		});

		binding.textview13.setOnClickListener(_view -> {
			final String[] methods = {"기본 정렬", "폴더 우선 정렬"};
			String savedMethod = String.valueOf(getFileSort(SettingActivity.this));
			final int[] checkedItem = {savedMethod.equals("0") ? 0 : 1};

			AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
			builder.setTitle("기본 정렬 방식")
					.setSingleChoiceItems(methods, checkedItem[0], (dialog, which) -> checkedItem[0] = which)
					.setPositiveButton("확인", (dialog, which) -> {
						setFileSort(this, String.valueOf(checkedItem[0]));
						binding.textview13.setText("기본 정렬 방식: " + methods[checkedItem[0]]);
					})
					.setNegativeButton("취소", (dialog, which) -> {})
					.setCancelable(false)
					.create()
					.show();
		});

		binding.textview14.setOnClickListener(_view -> {
			final String[] methods = {"폴더 이동 시", "위로 당길 때만"};
			String savedMethod = String.valueOf(getFileCache(SettingActivity.this));
			final int[] checkedItem = {savedMethod.equals("0") ? 0 : 1};

			AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
			builder.setTitle("기본 캐시 정리 방식")
					.setSingleChoiceItems(methods, checkedItem[0], (dialog, which) -> checkedItem[0] = which)
					.setPositiveButton("확인", (dialog, which) -> {
						setFileCache(this, String.valueOf(checkedItem[0]));
						binding.textview14.setText("기본 캐시 정리 방식: " + methods[checkedItem[0]]);
					})
					.setNegativeButton("취소", (dialog, which) -> {})
					.setCancelable(false)
					.create()
					.show();
		});

		binding.textview15.setOnClickListener(_view -> {
			final String[] methods = {"한 이미지만 보기", "여러 이미지 같이 보기", "항상 묻기"};
			String savedMethod = String.valueOf(getFileImage(SettingActivity.this));
			final int[] checkedItem = {savedMethod.equals("0") ? 0 : savedMethod.equals("1") ? 1 : 2};

			AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
			builder.setTitle("기본 이미지 표시 방식")
					.setSingleChoiceItems(methods, checkedItem[0], (dialog, which) -> checkedItem[0] = which)
					.setPositiveButton("확인", (dialog, which) -> {
						setFileImage(this, String.valueOf(checkedItem[0]));
						binding.textview15.setText("기본 이미지 표시 방식: " + methods[checkedItem[0]]);
					})
					.setNegativeButton("취소", (dialog, which) -> {})
					.setCancelable(false)
					.create()
					.show();
		});

		binding.textview16.setOnClickListener(_view -> {
			final String[] methods = {"VLC 플레이어", "기본 플레이어", "항상 묻기"};
			String savedMethod = String.valueOf(getFileVideo(SettingActivity.this));
			final int[] checkedItem = {savedMethod.equals("0") ? 0 : savedMethod.equals("1") ? 1 : 2};

			AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
			builder.setTitle("기본 동영상 표시 방식")
					.setSingleChoiceItems(methods, checkedItem[0], (dialog, which) -> checkedItem[0] = which)
					.setPositiveButton("확인", (dialog, which) -> {
						setFileVideo(this, String.valueOf(checkedItem[0]));
						binding.textview16.setText("기본 동영상 표시 방식: " + methods[checkedItem[0]]);
					})
					.setNegativeButton("취소", (dialog, which) -> {})
					.setCancelable(false)
					.create()
					.show();
		});

		binding.textview17.setOnClickListener(_view -> {
			String currentValue = getFileVLCNetworkBuffer(SettingActivity.this);

			final EditText input = new EditText(SettingActivity.this);
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			input.setText(currentValue);
			input.setHint("예: 3000 (100ms 이상)");
			input.setGravity(Gravity.CENTER_HORIZONTAL);
			input.setPadding(50, 30, 50, 30);

			AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
			builder.setTitle("VLC 버퍼 크기 (ms)")
					.setView(input)
					.setNegativeButton("취소", (dialog, which) -> {})
					.setCancelable(false);

			AlertDialog dialog = builder.create();

			dialog.setOnShowListener(dlg -> {
				Button confirmButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				if (confirmButton == null) {
					confirmButton = new Button(SettingActivity.this);
					confirmButton.setText("확인");
					dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setLayoutParams(
							new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1)
					);
					((LinearLayout) dialog.getButton(AlertDialog.BUTTON_NEGATIVE).getParent())
							.addView(confirmButton);
				}

				confirmButton.setOnClickListener(v -> {
					try {
						int newValue = Integer.parseInt(input.getText().toString().trim());

						if (newValue < 100) {
							SketchwareUtil.showMessage(SettingActivity.this, "100ms 이상 입력해주세요!");
							return;
						}

						setFileVLCNetworkBuffer(SettingActivity.this, String.valueOf(newValue));
						binding.textview17.setText("VLC 네트워크 버퍼 크기: " + newValue + "ms");
						dialog.dismiss(); // 성공 시 다이얼로그 닫기
					} catch (NumberFormatException e) {
						SketchwareUtil.showMessage(SettingActivity.this, "올바른 숫자를 입력해주세요.");
					}
				});
			});

			// 버튼을 먼저 설정해야 하므로 create 후에 버튼 추가
			dialog.setButton(AlertDialog.BUTTON_POSITIVE, "확인", (DialogInterface.OnClickListener) null);
			dialog.show();
		});

		binding.textview18.setOnClickListener(_view -> {
			String currentValue = getFileSVBar(SettingActivity.this);

			final EditText input = new EditText(SettingActivity.this);
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			input.setText(currentValue);
			input.setHint("예: 200 (최대 511개)");
			input.setGravity(Gravity.CENTER_HORIZONTAL);
			input.setPadding(50, 30, 50, 30);

			AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
			builder.setTitle("사운드 비주얼라이저 막대 개수")
					.setMessage("너무 많은 막대를 둘 경우 고음 음역대에 의미 없는 막대가 생길 수 있습니다.\n511번째 막대를 둘 경우 22050Hz까지의 고음을 표시하게 됩니다.")
					.setView(input)
					.setNegativeButton("취소", (dialog, which) -> {})
					.setCancelable(false);

			AlertDialog dialog = builder.create();

			dialog.setOnShowListener(dlg -> {
				Button confirmButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				if (confirmButton == null) {
					confirmButton = new Button(SettingActivity.this);
					confirmButton.setText("확인");
					dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setLayoutParams(
							new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1)
					);
					((LinearLayout) dialog.getButton(AlertDialog.BUTTON_NEGATIVE).getParent())
							.addView(confirmButton);
				}

				confirmButton.setOnClickListener(v -> {
					try {
						int newValue = Integer.parseInt(input.getText().toString().trim());

						if (newValue > 511) {
							SketchwareUtil.showMessage(SettingActivity.this, "511개가 최대입니다");
							return;
						}

						setFileSVBar(SettingActivity.this, String.valueOf(newValue));
						binding.textview18.setText("사운드 비주얼라이저 막대 개수: " + newValue + "개");
						dialog.dismiss(); // 성공 시 다이얼로그 닫기
					} catch (NumberFormatException e) {
						SketchwareUtil.showMessage(SettingActivity.this, "올바른 숫자를 입력해주세요.");
					}
				});
			});

			// 버튼을 먼저 설정해야 하므로 create 후에 버튼 추가
			dialog.setButton(AlertDialog.BUTTON_POSITIVE, "확인", (DialogInterface.OnClickListener) null);
			dialog.show();
		});

		binding.updatelog.setOnClickListener(_view -> {
            intent.setClass(getApplicationContext(), UpdatelogappActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.slide_out_right);
			finishAffinity();
        });
	}
	
	private void initializeLogic() {
        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_view);
		toolbarLayout.setNavigationButtonOnClickListener(view -> onBackPressed());
		_load_setting();
	}

	public void _load_setting() {
		if (getFileTitle(this) == 1) binding.textview12.setText("기본 타이틀 표시 방식: 폴더 이름");
		else binding.textview12.setText("기본 타이틀 표시 방식: 유닛 이름");

		if (getFileSort(this) == 1) binding.textview13.setText("기본 정렬 방식: 폴더 우선 정렬");
		else binding.textview13.setText("기본 정렬 방식: 기본 정렬");

		if (getFileCache(this) == 1) binding.textview14.setText("기본 캐시 정리 방식: 위로 당길 때만");
		else binding.textview14.setText("기본 캐시 정리 방식: 폴더 이동 시");

		switch (getFileImage(this)) {
            case 1:
				binding.textview15.setText("기본 이미지 표시 방식: 여러 이미지 같이 보기");
				break;
			case 2:
				binding.textview15.setText("기본 이미지 표시 방식: 항상 묻기");
				break;
			default:
				binding.textview15.setText("기본 이미지 표시 방식: 한 이미지만 보기");
				break;
		}

		switch (getFileVideo(this)) {
			case 1:
				binding.textview16.setText("기본 동영상 표시 방식: 기본 플레이어");
				break;
			case 2:
				binding.textview16.setText("기본 동영상 표시 방식: 항상 묻기");
				break;
			default:
				binding.textview16.setText("기본 동영상 표시 방식: VLC 플레이어");
				break;
		}

		binding.textview17.setText("VLC 네트워크 버퍼 크기: " + getFileVLCNetworkBuffer(this) + "ms");
		binding.textview18.setText("사운드 비주얼라이저 막대 개수: " + getFileSVBar(this) + "개");
	}
}
