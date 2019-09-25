package com.zk.cabinet.util;

public class JKSDolphinUtil {
    private volatile static JKSDolphinUtil instance = null;

    private JKSDolphinUtil() {
    }

    public static JKSDolphinUtil getInstance() {
        if (instance == null) {
            synchronized (JKSDolphinUtil.class) {
                if (instance == null) instance = new JKSDolphinUtil();
            }
        }
        return instance;
    }

    public boolean hideNavigation(){
        boolean isHide;
        try
        {
            String command;
            command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib service call activity 42 s16 com.android.systemui";
            Process proc = Runtime.getRuntime().exec(new String[] { "su",
                    "-c", command });
            proc.waitFor();
            isHide = true;
        }
        catch(Exception ex) {
            isHide = false;
        }
        return isHide;
    }

    public boolean showNavigation(){
        boolean isShow;
        try
        {
            String command;
            command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib am startservice -n com.android.systemui/.SystemUIService";
            Process proc = Runtime.getRuntime().exec(new String[] { "su",
                    "-c", command });
            proc.waitFor();
            isShow = true;
        }
        catch (Exception e)
        {
            isShow = false;
            e.printStackTrace();
        }
        return isShow;
    }
}
