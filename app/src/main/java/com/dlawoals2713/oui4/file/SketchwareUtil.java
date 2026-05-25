package com.dlawoals2713.oui4.file;

import android.content.Context;
import android.widget.Toast;

public class SketchwareUtil {
    public static void showMessage(Context _context, String _s) {
        Toast.makeText(_context, _s, Toast.LENGTH_SHORT).show();
    }

    public static int getDisplayWidthPixels(Context _context) {
        return _context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getDisplayHeightPixels(Context _context) {
        return _context.getResources().getDisplayMetrics().heightPixels;
    }
}
