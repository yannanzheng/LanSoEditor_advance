package com.example.custom.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;


import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;


public class ActivityUtils {

    public static boolean isLandscape(Context activity) {
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point windowSize = new Point();
        display.getSize(windowSize);
        //return activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        return windowSize.x > windowSize.y;
    }

    public static boolean isPortrait(Context activity) {
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point windowSize = new Point();
        display.getSize(windowSize);
        //return activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        return windowSize.x < windowSize.y;
//        return activity.getResources().getConfiguration().orientation == Configuration
//                .ORIENTATION_PORTRAIT;
    }

    public static String getRunningActivityName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context
                .ACTIVITY_SERVICE);
        String runningActivity;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            runningActivity = activityManager.getRunningAppProcesses().get(0).processName;
        } else {
            runningActivity = activityManager.getRunningTasks(1).get(0).topActivity
                    .getClassName();
        }
        return runningActivity;
    }


    public static void changeLanguage(Context context, Locale locale) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            context = context.createConfigurationContext(configuration);
        } else {
            context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
        }
    }


    public static void notifyMediaScannerScanFile(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
        MediaScannerConnection.scanFile(
                context.getApplicationContext(),
                new String[]{file.getAbsolutePath()},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {

                    }
                });
    }

    /**
     * @param view activity contentView
     */
    public static int setFullScreen(View view) {
        if (view == null) return 0;
        int lastFlag = view.getSystemUiVisibility();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        return lastFlag;
    }

    public static int setFullScreen(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        int lastFlag = decorView.getSystemUiVisibility();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        return lastFlag;
    }

    /**
     * @param view activity contentView
     */
    public static void setVisibleStatusBar(View view) {
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    /**
     * 隐藏虚拟键
     *
     * @param view activity contentView
     */
    public static void setHideNavigation(View view) {
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View
                .SYSTEM_UI_FLAG_IMMERSIVE);
    }

    public static void setTransparentStatusBar(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

    }


    /**
     * 检查是否存在 虚拟键
     *
     * @param context
     * @return
     */
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }

        return hasNavigationBar;

    }

    //获取NavigationBar的高度：
    public static int getNavigationBarHeight(Context context) {
        int navigationBarHeight = 0;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0 && checkDeviceHasNavigationBar(context)) {
            navigationBarHeight = rs.getDimensionPixelSize(id);
        }
        return navigationBarHeight;
    }

    public static int setStatusBarDarkMode(Activity activity) {
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (MIUISetStatusBarLightMode(activity.getWindow(), false)) {
                result = 1;
            } else if (FlymeSetStatusBarLightMode(activity.getWindow(), false)) {
                result = 2;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                result = 3;
            }
        }
        return result;
    }


    private static boolean isMIUIStatusBar(Activity activity) {
        boolean result = false;
        Window window = activity.getWindow();
        if (window != null) {
            try {
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                result = field != null;
            } catch (Exception ignored) {

            }
        }
        return result;
    }

    public static boolean isFlymeStatusBar() {

        try {
            Field meizuFlags = WindowManager.LayoutParams.class
                    .getDeclaredField("meizuFlags");
            Field darkFlag = WindowManager.LayoutParams.class
                    .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            return meizuFlags != null && darkFlag != null;
        } catch (NoSuchFieldException ignored) {
        }
        return false;
    }

    public static void setCompatToolbarPadding(Object object, @NonNull Toolbar toolbar) {
        int paddingEnd = 0;
        int paddingTop = toolbar.getPaddingTop();
        if (object instanceof Activity) {
            Activity activity = (Activity) object;
            if (isLandscape(activity)) {
                paddingEnd = getNavigationBarHeight(activity);
            }
            if (isMIUIStatusBar(activity) && isMIUIStatusBarLightMode(activity.getWindow())) {
                paddingTop = 0;
            }
            if (isFlymeStatusBar() && isFlymeStatusBarLightMode(activity.getWindow())) {
                /*paddingTop = 0;*/
            }
        } else if (object instanceof DialogFragment) {
            paddingTop = 0;
            /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            }*/
        } else if (object instanceof Fragment) {
            Activity activity = ((Fragment) object).getActivity();
            if (isLandscape(activity)) {
                paddingEnd = getNavigationBarHeight(activity);
            }
            if (isMIUIStatusBar(activity)) {
                paddingTop = 0;
            }
            if (isFlymeStatusBar()) {
                /*paddingTop = 0;*/
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                paddingTop = 0;
            }
        }

        toolbar.setPadding(0, paddingTop, paddingEnd, 0);
    }

    private static boolean MIUISetStatusBarLightMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            Class clazz = window.getClass();
            try {
                int darkModeFlag;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
                }
                result = true;
            } catch (Exception ignored) {

            }
        }
        return result;
    }

    private static boolean isMIUIStatusBarLightMode(Window window) {
        if (window != null) {
            try {
                int darkModeFlag;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                return (window.getAttributes().getClass().getField("extraFlags").getInt(window.getAttributes()) & darkModeFlag) == darkModeFlag;
            } catch (Exception ignored) {

            }
        }
        return false;
    }

    private static boolean isFlymeStatusBarLightMode(Window window) {
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                return (value & bit) == bit;
            } catch (Exception ignored) {

            }
        }
        return false;
    }


    public static boolean FlymeSetStatusBarLightMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception ignored) {

            }
        }
        return result;
    }

    public static void addWindowTranslucentFlag(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = activity.getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
    }

    public static void removeWindowTranslucentFlag(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = activity.getWindow().getAttributes();
            localLayoutParams.flags = (localLayoutParams.flags ^ WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    public static boolean isNavigationBarShow(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y != size.y || realSize.x != size.x;
        } else {
            boolean menu = ViewConfiguration.get(activity).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if (menu || back) {
                return false;
            } else {
                return true;
            }
        }
    }

    public static int getNavigationBarHeight(Activity activity) {
        int height = 0;
        if (!isNavigationBarShow(activity)) {
            return height;
        }
//        Resources resources = activity.getResources();
//        int resourceId = resources.getIdentifier("navigation_bar_height",
//                "dimen", "android");
//        //获取NavigationBar的高度
//        int height = resources.getDimensionPixelSize(resourceId);
//        L.i("onLayout: height = "+height);
//
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        Point realSize = new Point();
        display.getSize(size);
        display.getRealSize(realSize);
        if (ActivityUtils.isLandscape(activity)) {
            height = realSize.x - size.x;
        } else {
            height = realSize.y - size.y;
        }
        return height;
    }

    public static int[] getNavigationNarHeightByShowOrNot(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Point realSize = new Point();
        display.getRealSize(realSize);
        int tempPaddingEnd = realSize.x - size.x;
        int tempPaddingBottom = realSize.y - size.y;
        if (Build.MODEL.endsWith("SM-G950F")) {
            //三星手机没办法通过代码拉起虚拟键
            tempPaddingEnd = 0;
            tempPaddingBottom = 0;
        }
        return new int[]{tempPaddingEnd, tempPaddingBottom};
    }

    public static void setCompactNavigationBarViewPaddingBottom(Activity activity, View view) {
        view.setPadding(0, 0, 0, getNavigationBarHeight(activity) + view.getPaddingBottom());
    }

    public static void setCompactNavigationBarViewPaddingEnd(Activity activity, View view) {
        view.setPadding(0, 0, getNavigationBarHeight(activity) + view.getPaddingEnd(), 0);
    }

    public static void setCompactNavigationBarViewPaddingEnd(Activity activity, View view, int paddingLeft) {
        view.setPadding(paddingLeft, 0, getNavigationBarHeight(activity) + view.getPaddingEnd(), 0);
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            result = context.getResources().getDimensionPixelOffset(resId);
        }

        return result;
    }
}
