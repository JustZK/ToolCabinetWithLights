package com.zk.cabinet.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ZK on 2016/12/14.
 */

public class RegularExpressionUtil {

    //IP正则表达式
    public static boolean isIp(String IP_PORT) {
        String str = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(IP_PORT);
        return m.matches();
    }

    //子网掩码正则表达式
    public static boolean isSubnetMask(String subnetMask) {
        String str = "^((128|192)|2(24|4[08]|5[245]))(\\.(0|(128|192)|2((24)|(4[08])|(5[245])))){3}$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(subnetMask);
        return m.matches();
    }

    //数字
    public static boolean isNumber(String number) {
        String str = "^[0-9]*$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(number);
        return m.matches();
    }

    //MAC地址的正则表达式
    public static boolean stringIsMac(String val) {
        String trueMacAddress = "([0-9a-fA-F]{2})(([/\\\\s:-][0-9a-fA-F]{2}){5})";
        if (val.matches(trueMacAddress)) {
            return true;
        } else {
            return false;
        }
    }
}
