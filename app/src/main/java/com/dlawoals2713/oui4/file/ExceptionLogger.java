package com.dlawoals2713.oui4.file;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExceptionLogger {

    public static void log(Exception exception, String className) {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;

        try {
            File dir = new File(FileUtil.getPublicDir(Environment.DIRECTORY_DOCUMENTS) + "/OUI_FileManager/");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e("ExceptionLogger", "dir.mkdirs ERROR");
                }
            }
            File file = new File(dir, "exception.txt");

            String currentTime = new SimpleDateFormat("yyyy-MM-dd a h:mm:ss", Locale.getDefault()).format(new Date());
            String logMessage = "[" + currentTime + "] Error in: " + className + " → " + exception.getMessage() + "\n";

            fos = new FileOutputStream(file, true);
            osw = new OutputStreamWriter(fos);
            osw.write(logMessage);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (osw != null) osw.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void log(Throwable throwable, String className) {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;

        try {
            File dir = new File(FileUtil.getPublicDir(Environment.DIRECTORY_DOCUMENTS) + "/OUI_FileManager/");
            if (!dir.exists() && !dir.mkdirs()) {
                Log.e("ExceptionLogger", "dir.mkdirs ERROR");
            }
            File file = new File(dir, "exception.txt");

            String currentTime = new SimpleDateFormat("yyyy-MM-dd a h:mm:ss", Locale.getDefault()).format(new Date());
            String logMessage = "[" + currentTime + "] Error in: " + className + " → " + throwable.toString() + "\n";

            for (StackTraceElement element : throwable.getStackTrace()) {
                logMessage += "    at " + element.toString() + "\n";
            }

            fos = new FileOutputStream(file, true);
            osw = new OutputStreamWriter(fos);
            osw.write(logMessage);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (osw != null) osw.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void log(String content, String className) {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;

        try {
            File dir = new File(FileUtil.getPublicDir(Environment.DIRECTORY_DOCUMENTS) + "/OUI_FileManager/");
            if (!dir.exists() && !dir.mkdirs()) {
                Log.e("ExceptionLogger", "dir.mkdirs ERROR");
            }
            File file = new File(dir, "exception.txt");

            String currentTime = new SimpleDateFormat("yyyy-MM-dd a h:mm:ss", Locale.getDefault()).format(new Date());
            String logMessage = "[" + currentTime + "] Error in: " + className + " → " + content + "\n";

            fos = new FileOutputStream(file, true);
            osw = new OutputStreamWriter(fos);
            osw.write(logMessage);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (osw != null) osw.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}