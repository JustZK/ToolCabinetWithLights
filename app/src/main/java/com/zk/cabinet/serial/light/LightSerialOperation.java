package com.zk.cabinet.serial.light;

import com.zk.cabinet.bean.LightSendInfo;
import com.zk.cabinet.callback.LightListener;
import com.zk.cabinet.serial.SerialHelper;
import com.zk.cabinet.serial.door.util.DoorUtils;
import com.zk.cabinet.serial.light.util.LightGroupPackage;
import com.zk.cabinet.util.LogUtil;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LightSerialOperation extends SerialHelper {
    public static final String TAG = "DoorSerialOperation";

    private LightListener lightListener;

    private final String PATH = "/dev/ttyS2";
    private final String BAUD = "9600";
    private boolean isConnect = false;
    private volatile static LightSerialOperation instance;

    private ScheduledExecutorService servicePool;
    private TimerTask taskCheckLightState;

//    private ArrayList<Integer> targetAddress;
    private int checkLightStateTargetAddress = -1;

    private LightSerialOperation() {
        servicePool = Executors.newScheduledThreadPool(1);

//        targetAddress = new ArrayList<>();
//
//        List<Cabinet> cabinetList = CabinetService.getInstance().loadAll();
//        if (cabinetList != null){
//            for (Cabinet cabinet : cabinetList){
//                if (cabinet.getCellNumber() > 0 &&
//                        !targetAddress.contains(cabinet.getTargetAddress())){
//
//                    LogUtil.getInstance().d("targetAddress : " + cabinet.getTargetAddress());
//                    targetAddress.add(cabinet.getTargetAddress());
//                }
//            }
//        }

        taskCheckLightState = new TimerTask() {
            @Override
            public void run() {
                if (checkLightStateTargetAddress > 0) {
                    addSendTask(LightGroupPackage.checkLightState(checkLightStateTargetAddress, 0xff));
                }
            }
        };

        servicePool.scheduleAtFixedRate(taskCheckLightState, 1000 , 500, TimeUnit.MILLISECONDS);
    }

    public void startCheckLightState(int checkLightStateTargetAddress){
        this.checkLightStateTargetAddress = checkLightStateTargetAddress;
    }

    public static LightSerialOperation getInstance() {
        if (instance == null) {
            synchronized (LightSerialOperation.class) {
                if (instance == null)
                    instance = new LightSerialOperation();
            }
        }
        return instance;
    }

    public void onLightListener(LightListener lightListener) {
        this.lightListener = lightListener;
    }

    public void send(LightSendInfo lightSendInfo) {
        switch (lightSendInfo.getCommunicationType()) {
            case 0x07:
                addSendTask(LightGroupPackage.openLight(lightSendInfo.getTargetAddress(), lightSendInfo.getSourceAddress(),
                        lightSendInfo.getLightNumber()));
                break;
        }
    }

    //打开串口
    public void openComPort() {
        setPort(PATH);
        setBaudRate(BAUD);
        try {
            open();
        } catch (SecurityException e) {
            LogUtil.getInstance().d("打开串口失败:没有串口读/写权限!");
        } catch (IOException e) {
            LogUtil.getInstance().d("打开串口失败:未知错误!");
        } catch (InvalidParameterException e) {
            LogUtil.getInstance().d("打开串口失败:参数错误!");
        }
        isConnect = true;
        LogUtil.getInstance().d("打开串口");
    }

    //关闭串口
    public void closeComPort() {
        isConnect = false;
        stopSend();
        close();

    }


    @Override
    protected void onDataReceived(String sPort, byte[] buffer, int size) {
        LogUtil.getInstance().d("（灯）onDataReceived---：：：：", buffer, size);

        if (buffer[0] == DoorUtils.HEAD_HIGH && buffer[1] == DoorUtils.HEAD_LOW) {
            int needleLength = buffer[4] * 256 + buffer[5];
            byte[] dataBuffer = new byte[needleLength + 2];
            System.arraycopy(buffer, 0, dataBuffer, 0, dataBuffer.length);
            checkReceived(dataBuffer, needleLength + 2);
        }

    }

    private void checkReceived(byte[] buffer, int size) {
        StringBuilder buffers = new StringBuilder();
        for (int i = 0; i < size; i++) {
            buffers.append(Integer.toHexString((buffer[i] & 0xff)));
            buffers.append(" ");
        }
        LogUtil.getInstance().d(TAG, "onDataReceived：：：：" + buffers);
        if (DoorUtils.andCheck(buffer, size)) {
            LogUtil.getInstance().d("灯 和校验通过");
            int type = (buffer[6] < 0 ? 256 + buffer[6] : buffer[6]) * 256
                    + (buffer[7] < 0 ? 256 + buffer[7] : buffer[7]);
            switch (type) {
                case 0x00 * 256 + 0x08:
                    if (lightListener != null){
                        lightListener.openLightResult(buffer[11] == 0x00);
                    }
                    break;
                case 0x00 * 256 + 0x06:
                    ArrayList<Integer> boxStateList = new ArrayList<>();
                    for (int i = 0; i < 8; i++) {
                        if ((buffer[8] >> i & 0x1) == 1) {
                            boxStateList.add(56 + i + 1);
                            LogUtil.getInstance().d("灯开启：" + (24 + i + 1));
                        }
                    }
                    for (int i = 0; i < 8; i++) {
                        if ((buffer[9] >> i & 0x1) == 1) {
                            boxStateList.add(48 + i + 1);
                            LogUtil.getInstance().d("灯开启：" + (16 + i + 1));
                        }
                    }
                    for (int i = 0; i < 8; i++) {
                        if ((buffer[10] >> i & 0x1) == 1) {
                            boxStateList.add(40 + i + 1);
                            LogUtil.getInstance().d("灯开启：" + (8 + i + 1));
                        }
                    }
                    for (int i = 0; i < 8; i++) {
                        if ((buffer[11] >> i & 0x1) == 1) {
                            boxStateList.add(32 + i + 1);
                            LogUtil.getInstance().d("灯开启：" + (i + 1));
                        }
                    }

                    for (int i = 0; i < 8; i++) {
                        if ((buffer[12] >> i & 0x1) == 1) {
                            boxStateList.add(24 + i + 1);
                            LogUtil.getInstance().d("灯开启：" + (24 + i + 1));
                        }
                    }
                    for (int i = 0; i < 8; i++) {
                        if ((buffer[13] >> i & 0x1) == 1) {
                            boxStateList.add(16 + i + 1);
                            LogUtil.getInstance().d("灯开启：" + (16 + i + 1));
                        }
                    }
                    for (int i = 0; i < 8; i++) {
                        if ((buffer[14] >> i & 0x1) == 1) {
                            boxStateList.add(8 + i + 1);
                            LogUtil.getInstance().d("灯开启：" + (8 + i + 1));
                        }
                    }
                    for (int i = 0; i < 8; i++) {
                        if ((buffer[15] >> i & 0x1) == 1) {
                            boxStateList.add(i + 1);
                            LogUtil.getInstance().d("灯开启：" + (i + 1));
                        }
                    }
                    if (lightListener != null){
                        lightListener.checkLightState(buffer[3], boxStateList);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
