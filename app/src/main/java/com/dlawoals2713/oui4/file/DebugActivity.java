package com.dlawoals2713.oui4.file;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dlawoals2713.oui4.file.base.BaseThemeActivity;
import com.dlawoals2713.oui4.file.databinding.DebugBinding;

public class DebugActivity extends BaseThemeActivity {
    private DebugBinding binding;
    private void handleException(Exception e) {
        showToast(e.getMessage());
        ExceptionLogger.log(e, getClass().getSimpleName());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private String string = "";
    private AlertDialog.Builder dialog;
    private Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        try {
            binding = DebugBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            initialize(_savedInstanceState);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
            } else {
                initializeLogic();
            }
        } catch(Exception e) {
            handleException(e);
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
        dialog = new AlertDialog.Builder(this);

        binding.home.setOnClickListener(_view -> {
            intent.setClass(getApplicationContext(), FilemanagerActivity.class);
            intent.putExtra("a", "lock");
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        });

        binding.shutdown.setOnClickListener(_view -> finishAffinity());
        binding.killProcess.setOnClickListener(_view -> System.exit(1));

        binding.copy.setOnClickListener(_view -> {
            SketchwareUtil.showMessage(getApplicationContext(), "클립보드에 복사되었어요");
            ((ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", binding.textview8.getText().toString()));
        });
    }

    private void initializeLogic() {
        binding.home.setBackground(new GradientDrawable() {
            public GradientDrawable getIns(int a, int b) {
                this.setCornerRadius(a);
                this.setColor(b);
                return this;
            }
        }.getIns((int)60, ContextCompat.getColor(getApplicationContext(), R.color.groupColor)));
        binding.restart.setBackground(new GradientDrawable() {
            public GradientDrawable getIns(int a, int b) {
                this.setCornerRadius(a);
                this.setColor(b);
                return this;
            }
        }.getIns((int)60, ContextCompat.getColor(getApplicationContext(), R.color.groupColor)));
        binding.shutdown.setBackground(new GradientDrawable() {
            public GradientDrawable getIns(int a, int b) {
                this.setCornerRadius(a);
                this.setColor(b);
                return this;
            }
        }.getIns((int)60, ContextCompat.getColor(getApplicationContext(), R.color.groupColor)));
        binding.killProcess.setBackground(new GradientDrawable() {
            public GradientDrawable getIns(int a, int b) {
                this.setCornerRadius(a);
                this.setColor(b);
                return this;
            }
        }.getIns((int)60, ContextCompat.getColor(getApplicationContext(), R.color.groupColor)));
        binding.copy.setBackground(new GradientDrawable() {
            public GradientDrawable getIns(int a, int b) {
                this.setCornerRadius(a);
                this.setColor(b);
                return this;
            }
        }.getIns((int)60, ContextCompat.getColor(getApplicationContext(), R.color.groupColor)));
        string = getIntent().getStringExtra("error");
        binding.textview8.setText(string);
        FileUtil.writeFile(FileUtil.getExternalStorageDir().concat("/Smart all in one/data/crash.txt"), string);
        dialog.setTitle("오류가 발생하였습니다!");
        dialog.setMessage(string);
        dialog.setPositiveButton("확인", (_dialog, _which) -> {
            if (string.contains("java.lang.OutOfMemoryError")) {
                SketchwareUtil.showMessage(getApplicationContext(), "OutOfMemoryError 오류 감지됨");
            }
        });
        dialog.create().show();
        if (!(FileUtil.isExistFile(FileUtil.getExternalStorageDir().concat("/Smart all in one/data/data.txt")) || FileUtil.isExistFile(FileUtil.getExternalStorageDir().concat("/Smart all in one/data/login method.txt")))) {
            binding.home.setVisibility(View.GONE);
            binding.explain1.setVisibility(View.GONE);
        }
        if (string.equals("java.lang.IllegalArgumentException: The user has invaded an activity that is not permitted by Smart All in one. [0~3] (too stupid)\n	at com.dlawoals2713.internal.util.saio.it.is.not.over(final verdict)")) {
            binding.explain3.setText("앱을 비̷͘͢정̸̀͡상̨͟͠적̸͏҉͏̨͠으̸͏̀҉̢̛͠로̨͠͝҉͏̷̢҉ 종료합니다");
            binding.explain1.setVisibility(View.GONE);
            binding.explain2.setVisibility(View.GONE);
            binding.explain4.setVisibility(View.GONE);
            binding.killProcess.setVisibility(View.GONE);
            binding.home.setVisibility(View.GONE);
            binding.restart.setVisibility(View.GONE);
            binding.copy.setVisibility(View.GONE);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {}
}