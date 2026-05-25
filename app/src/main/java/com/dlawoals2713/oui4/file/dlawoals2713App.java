package com.dlawoals2713.oui4.file;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.material.color.DynamicColors;

public class dlawoals2713App extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private Thread.UncaughtExceptionHandler mExceptionHandler;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
        mContext = this;
        this.mExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logExceptionToFile(throwable);
            Intent intent = new Intent(mContext, DebugActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("error", Log.getStackTraceString(throwable));
            mContext.startActivity(intent);
            mExceptionHandler.uncaughtException(thread, throwable);
        });
    }

    private void logExceptionToFile(Throwable throwable) {
        ExceptionLogger.log(throwable, getClass().getSimpleName());
    }
}