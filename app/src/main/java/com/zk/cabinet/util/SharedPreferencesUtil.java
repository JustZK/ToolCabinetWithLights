package com.zk.cabinet.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;

/**
 * Created by ZK on 2017/12/11.
 */

public class SharedPreferencesUtil {
    private SharedPreferences sp;
    private volatile static SharedPreferencesUtil instance;

    public SharedPreferencesUtil(){
    }

    public static SharedPreferencesUtil getInstance() {
        if (instance == null) {
            synchronized (SharedPreferencesUtil.class) {
                if (instance == null)
                    instance = new SharedPreferencesUtil();
            }
        }
        return instance;
    }

    public void init(Context context){
        sp = context.getSharedPreferences(Key.ToolCabinet.name(), Context.MODE_PRIVATE);
    }

    public void removeValue(Key key){
        Editor editor = sp.edit();
        editor.remove(key.name());
    }

    public boolean getBoolean (Key key, boolean defaultValue) {
        return sp.getBoolean(key.name(), defaultValue);
    }

    public int getInt (Key key, int defaultValue) {
        return sp.getInt(key.name(), defaultValue);
    }

    public long getLong(Key key, Long defaultValue) {
        return sp.getLong(key.name(), defaultValue);
    }

    public String getString (Key key, String defaultValue) {
        return sp.getString(key.name(), defaultValue);
    }

    public boolean commitValue(Record record) {
        if (record == null) return false;
        Editor editor = sp.edit();
        dataFilling(editor, record);
        return editor.commit();
    }

    public void applyValue(Record record) {
        if (record == null) return;
        Editor editor = sp.edit();
        dataFilling(editor, record);
        editor.apply();
    }

    public void applyValue(ArrayList<Record> records) {
        if (records == null || records.size() <= 0)
            return;
        Editor editor = sp.edit();
        dataFilling(editor, records);
        editor.apply();
    }

    private void dataFilling(Editor editor, ArrayList<Record> records) {
        if (editor == null)
            return;
        Record recordTemp;
        for (int i = 0; i < records.size(); i++) {
            recordTemp = records.get(i);
            dataFilling(editor, recordTemp);
        }
    }

    private void dataFilling(Editor editor, Record record) {
        if (editor == null) return;
        switch (record.getType()) {
            case type_int:
                editor.putInt(record.key.name(), record.intValue);
                break;
            case type_boolean:
                editor.putBoolean(record.key.name(), record.booleanValue);
                break;
            case type_String:
                editor.putString(record.key.name(), record.stringValue);
                break;
            case type_long:
                editor.putLong(record.key.name(), record.longValue);
                break;
            default:
                break;
        }
    }

    public static class Record {
        private Key key;
        private int intValue;
        private Long longValue;
        private boolean booleanValue;
        private String stringValue;
        private Type type;

        public Record(Key key){
            this.key = key;
        }

        public Record(Key key, int intValue){
            this(key);
            this.intValue = intValue;
            this.type = Type.type_int;
        }

        public Record(Key key, Long longValue){
            this(key);
            this.longValue = longValue;
            this.type = Type.type_long;
        }

        public Record(Key key, boolean booleanValue){
            this(key);
            this.booleanValue = booleanValue;
            this.type = Type.type_boolean;
        }

        public Record(Key key, String stringValue){
            this(key);
            this.stringValue = stringValue;
            this.type = Type.type_String;
        }

        public Key getKey() {
            return key;
        }

        public void setKey(Key key) {
            this.key = key;
        }

        public int getIntValue() {
            return intValue;
        }

        public void setIntValue(int intValue) {
            this.intValue = intValue;
        }

        public boolean isBooleanValue() {
            return booleanValue;
        }

        public void setBooleanValue(boolean booleanValue) {
            this.booleanValue = booleanValue;
        }

        public String getStringValue() {
            return stringValue;
        }

        public void setStringValue(String stringValue) {
            this.stringValue = stringValue;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public Long getLongValue() {
            return longValue;
        }

        public void setLongValue(Long longValue) {
            this.longValue = longValue;
        }

        public Type getType(){
            return this.type;
        }

        enum Type {
            type_int,
            type_boolean,
            type_String,
            type_long,
        }
    }

    public enum Key{
        ToolCabinet,
        Root, //配置管理员账户 String
        RootPwd, //配置管理员密码 String
        UserTemp, //登陆账户 String
        DeviceId,//设备编号 String
        UnitNumber,//单位编号 String
        UnitAddress,//单位地址 String
        IP, //IP String
        SubnetMask, //子网掩码 String
        Gateway, //网关 String
        DNS, //DNS String
        PlatformServiceIp,//平台服务IP String
        PlatformServicePort,//平台服务端口 Int
        SyncInterval,// 同步间隔 Int
        NumberOfBoxes,//箱体数量 Int
        NotClosedDoorAlarmTime,//未关门报警时间 Int
        Countdown,//倒计时时间 Int
        ReaderServiceIpPort,//读写器IP端口 String 多个 以","隔开
    }
}

