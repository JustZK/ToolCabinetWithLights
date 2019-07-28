package com.zk.cabinet;

import android.app.Application;
import android.hardware.usb.UsbManager;

import com.tencent.bugly.crashreport.CrashReport;
import com.zk.cabinet.constant.SelfComm;
import com.zk.cabinet.db.CabinetService;
import com.zk.cabinet.db.DBHelper;
import com.zk.cabinet.netty.server.NettyServerParsingLibrary;
import com.zk.cabinet.network.NetworkRequest;
import com.zk.cabinet.serial.door.DoorSerialOperation;
import com.zk.cabinet.serial.light.LightSerialOperation;
import com.zk.cabinet.util.SharedPreferencesUtil;
import com.zk.cabinet.util.SharedPreferencesUtil.Key;
import com.zk.cabinet.util.SharedPreferencesUtil.Record;
import com.zkteco.android.biometric.module.fingerprint.FingerprintSensor;

import java.util.ArrayList;

public class ToolCabinetApplication extends Application {
    private static final int VID = 6997;    //Silkid VID always 6997
    private static final int PID = 289;     //Silkid PID always 289
    private UsbManager musbManager = null;
    private final String ACTION_USB_PERMISSION = "com.zk.cabinet.USB_PERMISSION";

    private FingerprintSensor fingerprintSensor;

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

        NetworkRequest.getInstance().init(this);

        NettyServerParsingLibrary nettyServerParsingLibrary = NettyServerParsingLibrary.getInstance();
        nettyServerParsingLibrary.init(7880);
        new Thread() {
            @Override
            public void run() {
                NettyServerParsingLibrary.getInstance().start();
            }
        }.start();

//        c nettyServerParsingLibrary= new c(1230);

//        NettyParsingLibrary nettyParsingLibrary = new NettyParsingLibrary("192.168.2.107" , 8080);

//        SerialOperation.getInstance().openComPort();

//        SerialOperation.getInstance().testSendOpenDoor();

//        startFingerprintSensor();
//        RequestDevicePermission();
//
//        LogUtil.getInstance().d("指纹open ：fingerprintSensor：：：" +fingerprintSensor);
//
//        try {
//            fingerprintSensor.open(0);
//        } catch (FingerprintSensorException e) {
//            e.printStackTrace();
//            LogUtil.getInstance().d("指纹open ：：：：" +e);
//        }
//        fingerprintSensor.setFingerprintCaptureListener(0, new FingerprintCaptureListener() {
//            @Override
//            public void captureOK(int i, byte[] bytes, int[] ints, byte[] bytes1) {
//                LogUtil.getInstance().d("指纹：i：：：" +i);
////                LogUtil.getInstance().d("指纹：bytes：：：" +bytes.toString());
////                LogUtil.getInstance().d("指纹：ints：：：" +ints.toString());
////                LogUtil.getInstance().d("指纹：bytes1：：：" +bytes1.toString());
//            }
//
//            @Override
//            public void captureError(FingerprintSensorException e) {
//                LogUtil.getInstance().d("error：FingerprintSensorException：：：" +e);
//            }
//        });
//        try {
//            fingerprintSensor.startCapture(0);
//        } catch (FingerprintSensorException e) {
//            e.printStackTrace();
//            LogUtil.getInstance().d("指纹：startCapture FingerprintSensorException：：：" +e);
//        }
//        fingerprintSensor.setFingerprintCaptureMode(0, FingerprintCaptureListener.MODE_CAPTURE_TEMPLATE);


    }

//    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            if (ACTION_USB_PERMISSION.equals(action))
//            {
//                synchronized (this)
//                {
//                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
//                    {
//                    }
//                    else
//                    {
////                        Toast.makeText(this, "USB未授权", Toast.LENGTH_SHORT).show();
//                        LogUtil.getInstance().d("USB未授权");
//                        //mTxtReport.setText("USB未授权");
//                    }
//                }
//            }
//        }
//    };
//
//    private void RequestDevicePermission()
//    {
//        musbManager = (UsbManager)this.getSystemService(Context.USB_SERVICE);
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(ACTION_USB_PERMISSION);
//        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
//        this.registerReceiver(mUsbReceiver, filter);
//
//        for (UsbDevice device : musbManager.getDeviceList().values())
//        {
//            if (device.getVendorId() == VID && device.getProductId() == PID)
//            {
//                Intent intent = new Intent(ACTION_USB_PERMISSION);
//                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
//                musbManager.requestPermission(device, pendingIntent);
//            }
//        }
//    }
//
//    private void startFingerprintSensor() {
//        // Define output log level
//        LogHelper.setLevel(Log.VERBOSE);
//        // Start fingerprint sensor
//        Map fingerprintParams = new HashMap();
//        //set vid
//        fingerprintParams.put(ParameterHelper.PARAM_KEY_VID, VID);
//        //set pid
//        fingerprintParams.put(ParameterHelper.PARAM_KEY_PID, PID);
//        fingerprintSensor = FingerprintFactory.createFingerprintSensor(this, TransportType.USB, fingerprintParams);
//
//        /*
//        fingerprintParams.put(ParameterHelper.PARAM_SERIAL_SERIALNAME, fpSerialName);
//        fingerprintParams.put(ParameterHelper.PARAM_SERIAL_BAUDRATE, fpBaudrate);
//        fingerprintSensor = FingerprintFactory.createFingerprintSensor(this, TransportType.SERIALPORT, fingerprintParams);
//        */
//    }
}
