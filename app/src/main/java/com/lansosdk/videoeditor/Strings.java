package com.lansosdk.videoeditor;

import android.text.TextUtils;


import java.text.DecimalFormat;
import java.util.List;

public class Strings {
    public final static String TAG = "Strings";

    public static String stripTrailingSlash(String s) {
        if( s.endsWith("/") && s.length() > 1 )
            return s.substring(0, s.length() - 1);
        return s;
    }

    static boolean startsWith(String[] array, String text) {
        for (String item : array)
            if (text.startsWith(item))
                return true;
        return false;
    }

    static int containsName(List<String> array, String text) {
        for (int i = array.size()-1 ; i >= 0 ; --i)
            if (array.get(i).endsWith(text))
                return i;
        return -1;
    }

    /**
     * Get the formatted current playback speed in the form of 1.00x
     */
    public static String formatRateString(float rate) {
        return String.format(java.util.Locale.US, "%.2fx", rate);
    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KiB", "MiB", "GiB", "TiB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String readableSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1000));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1000, digitGroups)) + " " + units[digitGroups];
    }

    public static String removeFileProtocole(String path){
        if (path == null)
            return null;
        if (path.startsWith("file://"))
            return path.substring(7);
        else
            return path;
    }

    public static boolean stringArrayContains(String[] array, String string) {
        for (int i = 0 ; i < array.length ; ++i)
            if (TextUtils.equals(string, array[i]))
                return true;
        return false;
    }
}
