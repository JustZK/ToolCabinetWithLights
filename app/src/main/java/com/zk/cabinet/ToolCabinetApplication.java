package com.zk.cabinet;

import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.tencent.bugly.crashreport.CrashReport;
import com.zk.cabinet.constant.SelfComm;
import com.zk.cabinet.db.CabinetService;
import com.zk.cabinet.db.DBHelper;
import com.zk.cabinet.netty.server.NettyServerParsingLibrary;
import com.zk.cabinet.network.NetworkRequest;
import com.zk.cabinet.serial.door.DoorSerialOperation;
import com.zk.cabinet.serial.light.LightSerialOperation;
import com.zk.cabinet.util.LogUtil;
import com.zk.cabinet.util.MediaPlayerUtil;
import com.zk.cabinet.util.SharedPreferencesUtil;
import com.zk.cabinet.util.SharedPreferencesUtil.Key;
import com.zk.cabinet.util.SharedPreferencesUtil.Record;
import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.core.utils.LogHelper;
import com.zkteco.android.biometric.module.fingerprint.FingerprintCaptureListener;
import com.zkteco.android.biometric.module.fingerprint.FingerprintFactory;
import com.zkteco.android.biometric.module.fingerprint.FingerprintSensor;
import com.zkteco.android.biometric.module.fingerprint.exception.FingerprintSensorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ToolCabinetApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //TODO  打包的时候记得注释掉
        CrashReport.initCrashReport(getApplicationContext(), "23298db165", true);

        SharedPreferencesUtil.getInstance().init(this);

        if (SharedPreferencesUtil.getInstance().getString(Key.Root, "").equals("")) {
            ArrayList<Record> records = new ArrayList<>();
            records.add(new Record(Key.Root, SelfComm.CONFIG_MANAGER_DEFAULT_ACCOUNT));
            records.add(new Record(Key.RootPwd, SelfComm.CONFIG_MANAGER_DEFAULT_PASSWORD));
            SharedPreferencesUtil.getInstance().applyValue(records);
        }

        DBHelper.getInstance().init(this);
        if (CabinetService.getInstance().loadAll().size() == 0) {
            CabinetService.getInstance().buildTest();
        }

//        CardSerialOperation.getInstance().openComPort();

        DoorSerialOperation.getInstance().openComPort();
        LightSerialOperation.getInstance().openComPort();
        MediaPlayerUtil.getInstance().init(this);

        NetworkRequest.getInstance().init(this);

        NettyServerParsingLibrary nettyServerParsingLibrary = NettyServerParsingLibrary.getInstance();
        nettyServerParsingLibrary.init(7880);
        new Thread() {
            @Override
            public void run() {
                NettyServerParsingLibrary.getInstance().start();
            }
        }.start();


    }

}
