package com.zk.cabinet.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class UpdateUtil {

    /**
     * 对比版本号，是否需要升级
     * @param context 上下文
     * @param newVersionName 版本名称，例如：V1.0.1.2 or 1.0.1 or V1-0-1 or 1-0-1-2  (不用区分v的大小写，和-或.的区分)
     * @param newVersionCode 若没有code则传入-1
     * @return 是否需要升级
     */
    public static boolean checkNeedUpdate(Context context, String newVersionName, int newVersionCode){
        String localPackage = context.getPackageName();
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(localPackage, 0);
            if (newVersionCode != -1){
                if (newVersionCode > packageInfo.versionCode){
                    return true;
                } else {
                    return false;
                }
            } else {
                String localVersionName = packageInfo.versionName;
                localVersionName = localVersionName.replaceAll("\\D","");
                newVersionName = newVersionName.replaceAll("\\D","");
                if (newVersionName.compareTo(localVersionName) > 0) return true;
                else return false;

//                localVersionName = localVersionName.replaceAll("(?i)v","");
//                newVersionName = newVersionName.replaceAll("(?i)v","");
//                String[] localVersionNames=localVersionName.split("\\.|-");
//                String[] newVersionNames=newVersionName.split("\\.|-");
//                for (int i = 0; i < localVersionNames.length; i++){
//                    // TODO 需要测试4位版本号与3位版本号比较结果
//                    if (newVersionNames[i].compareTo(localVersionNames[i]) > 0){
//                        return_tools true;
//                    }
//                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }
}
