package com.dlawoals2713.oui4.file;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dlawoals2713.oui4.file.base.BaseThemeActivity;

public class DebugActivity extends BaseThemeActivity {
    private Context context;
    private void handleException(Exception e) {
        showToast(e.getMessage());
        ExceptionLogger.log(e, getClass().getSimpleName());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private String string = "";
    private TextView home;
    private TextView explain_1;
    private TextView restart;
    private TextView explain_2;
    private TextView shutdown;
    private TextView explain_3;
    private TextView kill_process;
    private TextView explain_4;
    private TextView copy;
    private TextView textview8;

    private AlertDialog.Builder dialog;
    private Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        try {
            setContentView(R.layout.debug);
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
        home = findViewById(R.id.home);
        explain_1 = findViewById(R.id.explain_1);
        restart = findViewById(R.id.restart);
        explain_2 = findViewById(R.id.explain_2);
        shutdown = findViewById(R.id.shutdown);
        explain_3 = findViewById(R.id.explain_3);
        kill_process = findViewById(R.id.kill_process);
        explain_4 = findViewById(R.id.explain_4);
        copy = findViewById(R.id.copy);
        textview8 = findViewById(R.id.textview8);
        dialog = new AlertDialog.Builder(this);

        home.setOnClickListener(_view -> {
            intent.setClass(getApplicationContext(), FilemanagerActivity.class);
            intent.putExtra("a", "lock");
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        });

        shutdown.setOnClickListener(_view -> {
            finishAffinity();
        });

        kill_process.setOnClickListener(_view -> {
            System.exit(1);
        });

        copy.setOnClickListener(_view -> {
            SketchwareUtil.showMessage(getApplicationContext(), "클립보드에 복사되었어요");
            ((ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", textview8.getText().toString()));
        });
    }

    private void initializeLogic() {
        context = getApplicationContext();
        home.setBackground(new GradientDrawable() {
            public GradientDrawable getIns(int a, int b) {
                this.setCornerRadius(a);
                this.setColor(b);
                return this;
            }
        }.getIns((int)60, ContextCompat.getColor(getApplicationContext(), R.color.groupColor)));
        restart.setBackground(new GradientDrawable() {
            public GradientDrawable getIns(int a, int b) {
                this.setCornerRadius(a);
                this.setColor(b);
                return this;
            }
        }.getIns((int)60, ContextCompat.getColor(getApplicationContext(), R.color.groupColor)));
        shutdown.setBackground(new GradientDrawable() {
            public GradientDrawable getIns(int a, int b) {
                this.setCornerRadius(a);
                this.setColor(b);
                return this;
            }
        }.getIns((int)60, ContextCompat.getColor(getApplicationContext(), R.color.groupColor)));
        kill_process.setBackground(new GradientDrawable() {
            public GradientDrawable getIns(int a, int b) {
                this.setCornerRadius(a);
                this.setColor(b);
                return this;
            }
        }.getIns((int)60, ContextCompat.getColor(getApplicationContext(), R.color.groupColor)));
        copy.setBackground(new GradientDrawable() {
            public GradientDrawable getIns(int a, int b) {
                this.setCornerRadius(a);
                this.setColor(b);
                return this;
            }
        }.getIns((int)60, ContextCompat.getColor(getApplicationContext(), R.color.groupColor)));
        string = getIntent().getStringExtra("error");
        textview8.setText(string);
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
            home.setVisibility(View.GONE);
            explain_1.setVisibility(View.GONE);
        }
        if (string.equals("java.lang.IllegalArgumentException: The user has invaded an activity that is not permitted by Smart All in one. [0~3] (too stupid)\n	at com.dlawoals2713.internal.util.saio.it.is.not.over(final verdict)")) {
            explain_3.setText("앱을 비̷͘͢정̸̀͡상̨͟͠적̸͏҉͏̨͠으̸͏̀҉̢̛͠로̨͠͝҉͏̷̢҉ 종료합니다");
            explain_1.setVisibility(View.GONE);
            explain_2.setVisibility(View.GONE);
            explain_4.setVisibility(View.GONE);
            kill_process.setVisibility(View.GONE);
            home.setVisibility(View.GONE);
            restart.setVisibility(View.GONE);
            copy.setVisibility(View.GONE);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }
}