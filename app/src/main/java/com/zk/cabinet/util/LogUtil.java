package com.zk.cabinet.util;

import android.util.Log;

/**
 * Created by ZK on 2017/8/8.
 */

public class LogUtil {

    private volatile static LogUtil instance = null;
    private static final String LOG_TAG = "TEST";
    private static final String LOG_MESSAGE = "MESSAGE";

    private static boolean logSwitch = true;

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

    public void setLogSwitch(boolean logSwitch) {
        this.logSwitch = logSwitch;
    }

    /**
     * debug级别日志
     *
     * @param msg 信息
     */
    public void d(String msg) {
        d(null, msg, false);
    }

    /**
     * debug级别日志
     *
     * @param tag 标签
     * @param msg 信息
     */
    public void d(String tag, String msg) {
        d(tag, msg, false);
    }

    /**
     * debug级别日志
     *
     * @param msg 信息
     */
    public void d(byte[] msg, int msgLength) {
        if (logSwitch) {
            StringBuilder buffers = new StringBuilder();
            for (int i = 0; i < msgLength; i++) {
                buffers.append(Integer.toHexString((msg[i] & 0xff)));
                buffers.append(" ");
            }

            d(null, buffers.toString(), false);
        }
    }

    /**
     * debug级别日志
     *
     * @param tag 标签
     * @param msg 信息
     */
    public void d(String tag, byte[] msg, int msgLength) {
        if (logSwitch) {
            StringBuilder buffers = new StringBuilder();
            for (int i = 0; i < msgLength; i++) {
                buffers.append(Integer.toHexString((msg[i] & 0xff)));
                buffers.append(" ");
            }

            d(tag, buffers.toString(), false);
        }
    }

    /**
     * debug级别日志
     *
     * @param tag 标签
     * @param msg 信息
     */
    public void d(String tag, byte[] msg, int msgLength, boolean recordLocal) {
        if (logSwitch) {
            StringBuilder buffers = new StringBuilder();
            for (int i = 0; i < msgLength; i++) {
                buffers.append(Integer.toHexString((msg[i] & 0xff)));
                buffers.append(" ");
            }

            d(tag, buffers.toString(), recordLocal);
        }
    }

    /**
     * debug级别日志
     *
     * @param tag         标签
     * @param msg         信息
     * @param recordLocal 是否本地日志保存
     */
    public void d(String tag, String msg, boolean recordLocal) {
        if (logSwitch) {
            Log.d(tag == null ? LOG_TAG : LOG_TAG + "  " + tag, getTargetStackTraceElementMessage() + (msg == null ? LOG_MESSAGE : msg));

            if (recordLocal) {
                // 本地日志保存
            }
        }
    }

    private String getTargetStackTraceElementMessage() {
        return getTargetStackTraceElementMessage(getTargetStackTraceElement());
    }

    private String getTargetStackTraceElementMessage(StackTraceElement stackTraceElement) {
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

}
