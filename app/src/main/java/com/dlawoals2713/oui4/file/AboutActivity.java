package com.dlawoals2713.oui4.file;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import com.dlawoals2713.oui4.file.base.BaseThemeActivity;
import com.dlawoals2713.oui4.file.databinding.ActivityAboutBinding;

import de.dlyt.yanndroid.oneui.layout.AboutPage;

public class AboutActivity extends BaseThemeActivity {
    private ActivityAboutBinding binding;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            mUseAltTheme = true;
            super.onCreate(savedInstanceState);
            binding = ActivityAboutBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            binding.aboutBtn1.setOnClickListener(v -> binding.aboutPage.setUpdateState(AboutPage.LOADING));
        
            findViewById(de.dlyt.yanndroid.oneui.R.id.update_button).setOnClickListener(v -> Toast.makeText(this, "what?", Toast.LENGTH_LONG).show());
            findViewById(de.dlyt.yanndroid.oneui.R.id.retry_button).setOnClickListener(v -> retry());

            binding.aboutPage.setUpdateState(AboutPage.NOT_UPDATEABLE);
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