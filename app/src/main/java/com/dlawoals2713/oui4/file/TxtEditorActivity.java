package com.dlawoals2713.oui4.file;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dlawoals2713.oui4.file.base.BaseThemeActivity;
import com.dlawoals2713.oui4.file.databinding.TxtEditorBinding;
import com.google.android.material.snackbar.Snackbar;

public class TxtEditorActivity extends BaseThemeActivity {
	private TxtEditorBinding binding;
	private String load = "";
	
	private AlertDialog.Builder dialog;

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = TxtEditorBinding.inflate(getLayoutInflater());
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
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		dialog = new AlertDialog.Builder(this);
		
		binding.button1.setOnClickListener(_view -> {
            if (binding.contents.getText().toString().isEmpty()) {
                SketchwareUtil.showMessage(this, "내용을 입력하세요");
            } else if (binding.filepath.getText().toString().isEmpty()) {
				SketchwareUtil.showMessage(this, "파일 경로를 입력하세요");
			} else if (FileUtil.isExistFile(binding.filepath.getText().toString())) {
				dialog.setTitle("이미 존재하는 파일");
				dialog.setMessage("이미 존재하는 파일에 저장합니다. 계속 하시겠습니까?");
				dialog.setPositiveButton("계속", (_dialog, _which) -> {
                    FileUtil.writeFile(binding.filepath.getText().toString(), binding.contents.getText().toString());
                    _snackbar("저장 되었습니다.", "확인");
                });
				dialog.setNegativeButton("취소", (_dialog, _which) -> {});
				dialog.create().show();
			} else {
				FileUtil.writeFile(binding.filepath.getText().toString(), binding.contents.getText().toString());
				_snackbar("저장 되었습니다.", "확인");
			}
        });
	}
	
	private void initializeLogic() {
		new SomeTask().execute();
	}
	
	private class SomeTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			binding.progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			load = getIntent().getStringExtra("load");
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			binding.progressBar.setVisibility(View.GONE);
			if (!load.isEmpty()) {
				binding.filepath.setText(load);
				binding.contents.setText(FileUtil.readFile(load));
			}
		}
	}

	public void _snackbar(final String _text, final String _dismiss) {
		Snackbar sb = Snackbar.make(binding.linear1, _text, Snackbar.LENGTH_LONG).setDuration(8000);
		sb.setAction(_dismiss, v -> {});
		sb.show();
	}
}
