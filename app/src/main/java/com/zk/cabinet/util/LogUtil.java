package com.zk.cabinet.util;

import android.os.Environment;
import android.util.Log;

import java.io.FileOutputStream;
import java.util.Calendar;

/**
 * Created by ZK on 2017/8/8.
 */

public class LogUtil {
    public static final String ANDROID_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    public static final String LOG_PATH = ANDROID_PATH + "/ToolCabinetTestLog.txt";
    private volatile static LogUtil instance = null;
    private static final String LOG_TAG = "TEST";
    private static final String LOG_MESSAGE = "MESSAGE";

    private static final boolean logSwitch = true;

    private LogUtil() {
    }

    public static LogUtil getInstance() {
        if (instance == null) {
            synchronized (LogUtil.class) {
                if (instance == null) instance = new LogUtil();
            }
        }
        return instance;
    }

    /**
     * debug级别日志
     * @param tag 标签
     * @param msg 信息
     */
    public void d (String tag, String msg) {
        d(tag, msg, false);
    }

    /**
     * debug级别日志
     * @param tag 标签
     * @param msg 信息
     * @param recordLocal 是否本地日志保存
     */
    public void d (String tag, String msg, boolean recordLocal) {
        if (logSwitch) {
            Log.d(tag == null ? LOG_TAG : tag, getTargetStackTraceElementMessage() + (msg == null ? LOG_MESSAGE : msg));

            if (recordLocal) {
                // 本地日志保存
            }
        }
    }

    /**
     * debug级别日志
     * @param msg 信息
     */
    public void d (String msg) {
        d(null, msg, false);
    }

    private String getTargetStackTraceElementMessage () {
        return getTargetStackTraceElementMessage(getTargetStackTraceElement());
    }

    private String getTargetStackTraceElementMessage (StackTraceElement stackTraceElement) {
        return "(" + stackTraceElement.getFileName() + ":"
                + stackTraceElement.getLineNumber() + ")  ------  ";
    }

    private StackTraceElement getTargetStackTraceElement() {
        // find the target invoked method
        StackTraceElement targetStackTrace = null;
        boolean shouldTrace = false;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            boolean isLogMethod = stackTraceElement.getClassName().equals(LogUtil.class.getName());
            if (shouldTrace && !isLogMethod) {
                targetStackTrace = stackTraceElement;
                break;
            }
            shouldTrace = isLogMethod;
        }
        return targetStackTrace;
    }

    public static void LogPrint(String message) {
//        File file = new File("/sdcard/Log");
//        if (!file.exists()) {
//            try {
//                // 按照指定的路径创建文件夹
//                file.mkdirs();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        try {
            byte[] c = new byte[2];
            c[0] = 0x0d;
            c[1] = 0x0a;// 用于输入换行符的字节码
            String t = new String(c);// 将该字节码转化为字符串类型
            FileOutputStream fout = new FileOutputStream(LOG_PATH, true);
            byte[] bytes = (getTime() + message).getBytes();
            fout.write(bytes);
            fout.write(t.getBytes());
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getTime() {
        Calendar c = Calendar.getInstance();
        String year, month, day, hour, minute, second;
        year = String.valueOf(c.get(Calendar.YEAR));
        month = String.valueOf(c.get(Calendar.MONTH) + 1);
        day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
        minute = String.valueOf(c.get(Calendar.MINUTE));
        second = String.valueOf(c.get(Calendar.SECOND));
        return "[" + year + "/" + month + "/" + day + " " + hour + ":" + minute
                + ":" + second + "]";
    }

}
