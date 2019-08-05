package com.zk.cabinet.util;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.zk.cabinet.bean.User;
import com.zk.cabinet.callback.FingerprintListener;
import com.zk.cabinet.callback.FingerprintVerifyListener;
import com.zk.cabinet.db.UserService;
import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.core.utils.LogHelper;
import com.zkteco.android.biometric.module.fingerprint.FingerprintCaptureListener;
import com.zkteco.android.biometric.module.fingerprint.FingerprintFactory;
import com.zkteco.android.biometric.module.fingerprint.FingerprintSensor;
import com.zkteco.android.biometric.module.fingerprint.exception.FingerprintSensorException;
import com.zkteco.zkfinger.FingerprintService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FingerprintParsingLibrary {
    private static final int VID = 6997;    //Silkid VID always 6997
    private static final int PID = 289;     //Silkid PID always 289
    private UsbManager musbManager = null;
    private final String ACTION_USB_PERMISSION = "com.zk.cabinet.USB_PERMISSION";
    private FingerprintSensor fingerprintSensor;
    private FingerprintService fingerprintService;
    private volatile static FingerprintParsingLibrary instance;
    public static final String TAG = "FingerprintParsingLibrary";
    private List<User> userList;

    private FingerprintListener fingerprintListener;
    private FingerprintVerifyListener fingerprintVerifyListener;
    private boolean isVerify;

    private FingerprintParsingLibrary() {
    }

    public static FingerprintParsingLibrary getInstance() {
        if (instance == null) {
            synchronized (FingerprintParsingLibrary.class) {
                if (instance == null)
                    instance = new FingerprintParsingLibrary();
            }
        }
        return instance;
    }

    public void onFingerprintListener(FingerprintListener fingerprintListener) {
        this.fingerprintListener = fingerprintListener;
    }

    public void onFingerprintVerifyListener(FingerprintVerifyListener fingerprintVerifyListener) {
        this.fingerprintVerifyListener = fingerprintVerifyListener;
    }

    public void setFingerprintVerify(boolean verify) {
        this.isVerify = verify;
    }

    public void upUserList() {
        userList = UserService.getInstance().loadAll();
    }

    public void init(Context context) {
        startFingerprintSensor(context);
        RequestDevicePermission(context);
        upUserList();

        LogUtil.getInstance().d("指纹open ：fingerprintSensor：：：" + fingerprintSensor);

        try {
            fingerprintSensor.open(0);
        } catch (FingerprintSensorException e) {
            e.printStackTrace();
            LogUtil.getInstance().d("指纹open ：：：：" + e);
        }
        fingerprintSensor.setFingerprintCaptureListener(0, new FingerprintCaptureListener() {
            @Override
            public void captureOK(int i, byte[] bytes, int[] ints, byte[] bytes1) {
                LogUtil.getInstance().d("指纹：i：：：" + i);

                StringBuilder buffers = new StringBuilder();
                for (int j = 0; j < bytes1.length; j++) {
                    buffers.append(Integer.toHexString((bytes1[j] & 0xff)));
                    buffers.append(" ");
                }
                LogUtil.getInstance().d("指纹FingerprintCaptureListener", "test Received" + buffers);

                if (fingerprintListener != null) {
                    fingerprintListener.fingerprint(bytes1);
                }

                if (isVerify) {
                    boolean isExit = false;
                    for (User user : userList) {
                        if (user.getFingerPrint() != null) {
                            int result = FingerprintService.verify(bytes1, user.getFingerPrint());
                            LogUtil.getInstance().d("指纹对比结果：" + result);
                            if (result >= 23) {
                                if (fingerprintVerifyListener != null) {
                                    fingerprintVerifyListener.fingerprintVerify(true, user);
                                }
                                isExit = true;
                                break;
                            }
                        }
                    }
                    if (!isExit) {
                        if (fingerprintVerifyListener != null) {
                            fingerprintVerifyListener.fingerprintVerify(false, null);
                        }
                    }
                }
            }

            @Override
            public void captureError(FingerprintSensorException e) {
                LogUtil.getInstance().d("error：FingerprintSensorException：：：" + e);
            }
        });
        try {
            fingerprintSensor.startCapture(0);
        } catch (FingerprintSensorException e) {
            e.printStackTrace();
            LogUtil.getInstance().d("指纹：startCapture FingerprintSensorException：：：" + e);
        }
        fingerprintSensor.setFingerprintCaptureMode(0, FingerprintCaptureListener.MODE_CAPTURE_TEMPLATE);


//        fingerprintService = new FingerprintService();
        int[] limit = new int[1];
        LogUtil.getInstance().d("指纹对比初始化结果：" + FingerprintService.init(limit));
    }

    public void close() {
        try {
            fingerprintSensor.close(0);
        } catch (FingerprintSensorException e) {
            e.printStackTrace();
        }
        FingerprintService.close();
    }

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    } else {
//                        Toast.makeText(this, "USB未授权", Toast.LENGTH_SHORT).show();
                        LogUtil.getInstance().d("USB未授权");
                        //mTxtReport.setText("USB未授权");
                    }
                }
            }
        }
    };

    private void RequestDevicePermission(Context context) {
        musbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        context.registerReceiver(mUsbReceiver, filter);

        for (UsbDevice device : musbManager.getDeviceList().values()) {
            if (device.getVendorId() == VID && device.getProductId() == PID) {
                Intent intent = new Intent(ACTION_USB_PERMISSION);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                musbManager.requestPermission(device, pendingIntent);
            }
        }
    }

    private void startFingerprintSensor(Context context) {
        // Define output log level
        LogHelper.setLevel(Log.VERBOSE);
        // Start fingerprint sensor
        Map fingerprintParams = new HashMap();
        //set vid
        fingerprintParams.put(ParameterHelper.PARAM_KEY_VID, VID);
        //set pid
        fingerprintParams.put(ParameterHelper.PARAM_KEY_PID, PID);
        fingerprintSensor = FingerprintFactory.createFingerprintSensor(context, TransportType.USB, fingerprintParams);

        /*
        fingerprintParams.put(ParameterHelper.PARAM_SERIAL_SERIALNAME, fpSerialName);
        fingerprintParams.put(ParameterHelper.PARAM_SERIAL_BAUDRATE, fpBaudrate);
        fingerprintSensor = FingerprintFactory.createFingerprintSensor(this, TransportType.SERIALPORT, fingerprintParams);
        */
    }


    public String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public byte[] hexStrToByteArray(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        try {
            byte[] byteArray = new byte[str.length() / 2];
            for (int i = 0; i < byteArray.length; i++) {
                String subStr = str.substring(2 * i, 2 * i + 2);
                byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
            }
            return byteArray;
        } catch (Exception e) {
            return null;
        }
    }
}
