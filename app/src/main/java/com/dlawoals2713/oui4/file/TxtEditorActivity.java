package com.dlawoals2713.oui4.file;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dlawoals2713.oui4.file.base.BaseThemeActivity;

public class TxtEditorActivity extends BaseThemeActivity {
	private String load = "";
	private LinearLayout linear1;
	private ProgressBar progressBar;
	private EditText filepath;
	private Button button1;
	private EditText contents;
	
	private AlertDialog.Builder dialog;

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.txt_editor);
		initialize(_savedInstanceState);
		
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
		|| ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
		} else {
			initializeLogic();
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		linear1 = findViewById(R.id.linear1);
		progressBar = findViewById(R.id.progressBar);
		filepath = findViewById(R.id.filepath);
		button1 = findViewById(R.id.button1);
		contents = findViewById(R.id.contents);
		dialog = new AlertDialog.Builder(this);
		
		button1.setOnClickListener(_view -> {
            if (contents.getText().toString().isEmpty()) {
                SketchwareUtil.showMessage(this, "내용을 입력하세요");
            } else if (filepath.getText().toString().isEmpty()) {
				SketchwareUtil.showMessage(this, "파일 경로를 입력하세요");
			} else if (FileUtil.isExistFile(filepath.getText().toString())) {
				dialog.setTitle("이미 존재하는 파일");
				dialog.setMessage("이미 존재하는 파일에 저장합니다. 계속 하시겠습니까?");
				dialog.setPositiveButton("계속", (_dialog, _which) -> {
                    FileUtil.writeFile(filepath.getText().toString(), contents.getText().toString());
                    _snackbar("저장 되었습니다.", "확인");
                });
				dialog.setNegativeButton("취소", (_dialog, _which) -> {});
				dialog.create().show();
			} else {
				FileUtil.writeFile(filepath.getText().toString(), contents.getText().toString());
				_snackbar("저장 되었습니다.", "확인");
			}
        });
	}
	
	private void initializeLogic() {
		progressBar = findViewById(R.id.progressBar);

		// 다음은 렉이 걸릴 때 로딩 화면을 표시할 작업을 수행하는 코드입니다.
		new SomeTask().execute();
	}
	
	private class SomeTask extends AsyncTask<Void, Void, Void> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// 작업을 시작하기 전에 로딩 화면을 표시합니다.
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
		load = getIntent().getStringExtra("load");

					// 여기서 작업을 수행합니다. 렉이 걸리는 작업을 시뮬레이션하면 됩니다.
					// 예를 들어, 네트워크 호출 또는 데이터베이스 작업을 시뮬레이션할 수 있습니다.

					// 결과를 반환하지 않고, UI 업데이트는 onPostExecute()에서 수행합니다.
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// 작업이 완료되면 로딩 화면을 숨깁니다.
			progressBar.setVisibility(View.GONE);
			_language();
			_DarkMode();
			if (!load.isEmpty()) {
				filepath.setText(load);
				contents.setText(FileUtil.readFile(load));
			}
		}
	}
	
	public void _Custom_Dialog(final AlertDialog.Builder _dialog, final String _view_id) {
		int resID = getResources().getIdentifier(_view_id, "layout", getPackageName());
		View layout = getLayoutInflater().inflate(resID, null);
		_dialog.setView(layout);
	}
	
	
	public void _snackbar(final String _text, final String _dismiss) {
		com.google.android.material.snackbar.Snackbar sb = com.google.android.material.snackbar.Snackbar.make(linear1, _text, com.google.android.material.snackbar.Snackbar.LENGTH_LONG).setDuration(8000);
		
		sb.setAction(_dismiss, v -> {

            //paste the code that you want to perform

        });
		
		sb.show();
	}

	public void _DarkMode() {
		int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
		if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
			filepath.setTextColor(0xFFFFFFFF);
			contents.setTextColor(0xFFFFFFFF);
		}
	}
	
	
	public void _language() {
		button1.setText(getString(R.string.txt_save));
		filepath.setHint(getString(R.string.txt_path));
		contents.setHint(getString(R.string.txt_contents));
	}
}
