package com.dlawoals2713.oui4.file;

import android.content.Context;
import java.io.File;

public class CacheCleaner {

    /**
     * 앱의 내부 캐시를 모두 삭제합니다.
     *
     * @param context Context 객체
     */
    public static void clearInternalCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteDir(cacheDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 지정된 디렉터리와 그 안의 모든 파일 및 하위 디렉터리를 재귀적으로 삭제합니다.
     *
     * @param dir 삭제할 디렉터리
     * @return 삭제 성공 여부
     */
    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }
}
