package com.dlawoals2713.oui4.file;

import android.content.Context;

public class Setting {
    // 유닛 - 파일 매니저
    public static void setFileTitle(Context context, String option) { // 기본 타이틀 표시 방식
        String path = FileUtil.getPackageDataDir(context).concat("/Setting/file_title.txt");
        FileUtil.writeFile(path, option);
    }

    public static void setFileSort(Context context, String option) { // 기본 타이틀 표시 방식
        String path = FileUtil.getPackageDataDir(context).concat("/Setting/file_sort.txt");
        FileUtil.writeFile(path, option);
    }

    public static void setFileCache(Context context, String option) { // 기본 타이틀 표시 방식
        String path = FileUtil.getPackageDataDir(context).concat("/Setting/file_cache.txt");
        FileUtil.writeFile(path, option);
    }

    public static void setFileImage(Context context, String option) { // 기본 타이틀 표시 방식
        String path = FileUtil.getPackageDataDir(context).concat("/Setting/file_image.txt");
        FileUtil.writeFile(path, option);
    }

    public static void setFileVideo(Context context, String option) { // 기본 타이틀 표시 방식
        String path = FileUtil.getPackageDataDir(context).concat("/Setting/file_video.txt");
        FileUtil.writeFile(path, option);
    }

    public static void setFileVLCNetworkBuffer(Context context, String option) { // 기본 타이틀 표시 방식
        String path = FileUtil.getPackageDataDir(context).concat("/Setting/file_vlc_network_buffer.txt");
        FileUtil.writeFile(path, option);
    }

    public static void setFileSVBar(Context context, String option) { // 기본 타이틀 표시 방식
        String path = FileUtil.getPackageDataDir(context).concat("/Setting/file_sv_bar.txt");
        FileUtil.writeFile(path, option);
    }


    /// ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///
    /// ////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static int getFileTitle(Context context) {
        String path = FileUtil.getPackageDataDir(context) + "/Setting/file_title.txt";

        if (!FileUtil.isExistFile(path)) return 0;

        try {
            return Integer.parseInt(FileUtil.readFile(path).trim());
        } catch (NumberFormatException e) {
            return 0; // 오류
        }
    }

    public static int getFileSort(Context context) {
        String path = FileUtil.getPackageDataDir(context) + "/Setting/file_sort.txt";

        if (!FileUtil.isExistFile(path)) return 0;

        try {
            return Integer.parseInt(FileUtil.readFile(path).trim());
        } catch (NumberFormatException e) {
            return 0; // 오류
        }
    }

    public static int getFileCache(Context context) {
        String path = FileUtil.getPackageDataDir(context) + "/Setting/file_cache.txt";

        if (!FileUtil.isExistFile(path)) return 0;

        try {
            return Integer.parseInt(FileUtil.readFile(path).trim());
        } catch (NumberFormatException e) {
            return 0; // 오류
        }
    }

    public static int getFileImage(Context context) {
        String path = FileUtil.getPackageDataDir(context) + "/Setting/file_image.txt";

        if (!FileUtil.isExistFile(path)) return 0;

        try {
            return Integer.parseInt(FileUtil.readFile(path).trim());
        } catch (NumberFormatException e) {
            return 0; // 오류
        }
    }

    public static int getFileVideo(Context context) {
        String path = FileUtil.getPackageDataDir(context) + "/Setting/file_video.txt";

        if (!FileUtil.isExistFile(path)) return 0;

        try {
            return Integer.parseInt(FileUtil.readFile(path).trim());
        } catch (NumberFormatException e) {
            return 0; // 오류
        }
    }

    public static String getFileVLCNetworkBuffer(Context context) {
        String path = FileUtil.getPackageDataDir(context) + "/Setting/file_vlc_network_buffer.txt";

        if (!FileUtil.isExistFile(path)) return "3000";

        try {
            return FileUtil.readFile(path).trim();
        } catch (Exception e) {
            return "3000"; // 오류
        }
    }

    public static String getFileSVBar(Context context) {
        String path = FileUtil.getPackageDataDir(context) + "/Setting/file_sv_bar.txt";

        if (!FileUtil.isExistFile(path)) return "200";

        try {
            return Integer.parseInt(FileUtil.readFile(path).trim()) < 512 ? FileUtil.readFile(path) : "511";
        } catch (Exception e) {
            return "200"; // 오류
        }
    }

    public static boolean getUnitSplit(Context context) {
        if (FileUtil.isExistFile(FileUtil.getPackageDataDir(context).concat("/Setting/unit_split.txt"))) {
            return FileUtil.readFile(FileUtil.getPackageDataDir(context).concat("/Setting/unit_split.txt")).equals("true");
        } else {
            return true;
        }
    }
}
