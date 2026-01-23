package com.dlawoals2713.oui4.file;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import com.dlawoals2713.oui4.file.base.BaseThemeActivity;

import de.dlyt.yanndroid.oneui.layout.AboutPage;

public class AboutActivity extends BaseThemeActivity {
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            mUseAltTheme = true;
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_about);

            AboutPage about_page = findViewById(R.id.about_page);

            findViewById(R.id.about_btn1).setOnClickListener(v -> about_page.setUpdateState(AboutPage.LOADING));
        
            findViewById(de.dlyt.yanndroid.oneui.R.id.update_button).setOnClickListener(v -> Toast.makeText(this, "what?", Toast.LENGTH_LONG).show());
            findViewById(de.dlyt.yanndroid.oneui.R.id.retry_button).setOnClickListener(v -> retry());

            about_page.setUpdateState(AboutPage.NOT_UPDATEABLE);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void retry() {
        recreate();
	}

    private void handleException(Exception e) {
        showToast(e.getMessage());
        ExceptionLogger.log(e, getClass().getSimpleName());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}