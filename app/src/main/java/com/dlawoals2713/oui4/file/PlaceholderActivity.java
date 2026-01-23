package com.dlawoals2713.oui4.file;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dlawoals2713.oui4.file.base.BaseThemeActivity;

public class PlaceholderActivity extends BaseThemeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
            (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        systemBars.bottom);
                return insets;
            });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // 뒤로가기 눌렀을 때 HomeActivity에 신호 전달
        Intent intent = new Intent("com.dlawoals2713.HOME_BACK_PRESSED");
        sendBroadcast(intent);
    }
}