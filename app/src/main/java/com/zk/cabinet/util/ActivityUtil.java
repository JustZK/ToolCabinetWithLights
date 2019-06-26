package com.zk.cabinet.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import static android.content.Context.ACTIVITY_SERVICE;

public class ActivityUtil {

    public static final String TAG = "ActivityUtil";

    public static boolean isTopActivity (Context context, String activityName) {

        LogUtil.getInstance().d(TAG, "isTopActivity", false);

        boolean result = false;

        try {
            ActivityManager am = (ActivityManager) (context.getSystemService(ACTIVITY_SERVICE));

            if (am != null) {
                ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                LogUtil.getInstance().d(TAG, "para:" + activityName, false);
                LogUtil.getInstance().d(TAG, "TopActivity:" + cn.getClassName(), false);

                result = cn.getClassName().equals(activityName);
            } else {
                LogUtil.getInstance().d(TAG, "context.getSystemService(ACTIVITY_SERVICE) == null", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.getInstance().d(TAG, e.getMessage(), false);
        }

        return result;
    }

    /**
     * 获取正在运行桌面包名
     */
    public static String getLauncherPackageName(Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo == null) {
            // should not happen. A home is always installed, isn't it?
            return null;
        }
        return res.activityInfo.packageName;
    }

    /**
     * 卸载 APK。
     *
     * @param packageName
     *         应用的包名
     */
    public static void uninstallApk(Context context, String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
        context.startActivity(intent);
    }
}
