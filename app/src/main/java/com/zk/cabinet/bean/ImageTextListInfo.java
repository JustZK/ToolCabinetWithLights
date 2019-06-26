package com.zk.cabinet.bean;

/**
 * Created by ZK on 2017/12/6.
 */

public class ImageTextListInfo {
    private int mTitle;
    private String mValue;

    public ImageTextListInfo(int mTitle, String mValue){
        this.mTitle = mTitle;
        this.mValue = mValue;
    }

    public int getTitle() {
        return mTitle;
    }

    public void setTitle(int mTitle) {
        this.mTitle = mTitle;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String mValue) {
        this.mValue = mValue;
    }
}
