package com.dlawoals2713.oui4.file.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import de.dlyt.yanndroid.oneui.utils.ThemeUtil;

public class BaseThemeActivity extends AppCompatActivity {
    private static String SP_NAME = "com.dlawoals2713.oui4.file" + "_preferences";
    private SharedPreferences sp;
    public boolean mUseAltTheme;
    public boolean mUseOUI4Theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sp = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        mUseOUI4Theme = sp.getBoolean("use_oui4_theme", true);

        int normalTheme = mUseOUI4Theme ? de.dlyt.yanndroid.oneui.R.style.OneUI4Theme : de.dlyt.yanndroid.oneui.R.style.OneUI3Theme;
        int altTheme = mUseOUI4Theme ? de.dlyt.yanndroid.oneui.R.style.OneUI4AboutTheme : de.dlyt.yanndroid.oneui.R.style.OneUI3AboutTheme;
        setTheme(mUseAltTheme ? altTheme : normalTheme);
        new ThemeUtil(this);

        super.onCreate(savedInstanceState);

        int normalThemeNavBar = mUseOUI4Theme ? de.dlyt.yanndroid.oneui.R.color.sesl4_round_and_bgcolor : de.dlyt.yanndroid.oneui.R.color.sesl_round_and_bgcolor;
        int altThemeNavBar = de.dlyt.yanndroid.oneui.R.color.splash_background;
        getWindow().setNavigationBarColor(getResources().getColor(mUseAltTheme ? altThemeNavBar : normalThemeNavBar));
    }

    protected void switchOUITheme() {
        sp.edit().putBoolean("use_oui4_theme", !mUseOUI4Theme).apply();
        recreate();
    }
}
