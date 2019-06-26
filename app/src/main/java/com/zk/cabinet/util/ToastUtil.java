package com.zk.cabinet.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by ZK on 2017/12/22.
 */

public class ToastUtil {

    private ToastUtil() {}

    // Toast对象
    private static Toast toast = null;

    /**
     * 显示Toast
     */
    public static void showText(Context context, String text) {
        if (toast == null) {
            toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        }
        toast.setText(text);
        toast.show();
    }
}
