package com.zk.cabinet.serial.door;

import com.zk.cabinet.bean.DoorSendInfo;
import com.zk.cabinet.callback.DoorListener;
import com.zk.cabinet.serial.SerialHelper;
import com.zk.cabinet.serial.door.util.DoorGroupPackage;
import com.zk.cabinet.serial.door.util.DoorUtils;
import com.zk.cabinet.util.LogUtil;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DoorSerialOperation extends SerialHelper {
    public static final String TAG = "DoorSerialOperation";

    private DoorListener doorListener;

    private final String PATH = "/dev/ttyS0";
    private final String BAUD = "115200";
    private boolean isConnect = false;
    private volatile static DoorSerialOperation instance;

    private ScheduledExecutorService servicePool;
    private TimerTask taskCheckBoxDoorState;

//    private ArrayList<Integer> targetAddress;
    private int checkBoxDoorStateTargetAddress = -1;

    private DoorSerialOperation() {
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

        taskCheckBoxDoorState = new TimerTask() {
            @Override
            public void run() {
                if (checkBoxDoorStateTargetAddress > 0) {
                    addSendTask(DoorGroupPackage.checkBoxDoorState(checkBoxDoorStateTargetAddress, 0xff));
                }
            }
        };

        servicePool.scheduleAtFixedRate(taskCheckBoxDoorState, 1000 , 500, TimeUnit.MILLISECONDS);
    }

    public void startCheckBoxDoorState(int checkBoxDoorStateTargetAddress){
        this.checkBoxDoorStateTargetAddress = checkBoxDoorStateTargetAddress;
    }

    public static DoorSerialOperation getInstance() {
        if (instance == null) {
            synchronized (DoorSerialOperation.class) {
                if (instance == null)
                    instance = new DoorSerialOperation();
            }
        }
        return instance;
    }

    public void onDoorListener(DoorListener doorListener) {
        this.doorListener = doorListener;
    }

    public void send(DoorSendInfo doorSendInfo) {
        switch (doorSendInfo.getCommunicationType()) {
            case 0x03:
                addSendTask(DoorGroupPackage.openDoor(doorSendInfo.getTargetAddress(), doorSendInfo.getSourceAddress(),
                        doorSendInfo.getLockNumber()));
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
        LogUtil.getInstance().d("onDataReceived---：：：：", buffer, size);

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
            LogUtil.getInstance().d("门 和校验通过");
            int type = (buffer[6] < 0 ? 256 + buffer[6] : buffer[6]) * 256
                    + (buffer[7] < 0 ? 256 + buffer[7] : buffer[7]);
            switch (type) {
                case 0x00 * 256 + 0x04:
                    if (doorListener != null){
                        doorListener.openBoxResult(buffer[11] == 0x00);
                    }
                    break;
                case 0x00 * 256 + 0x02:
                    ArrayList<Integer> boxStateList = new ArrayList<>();
                    for (int i = 0; i < 8; i++) {
                        if ((buffer[8] >> i & 0x1) == 1) {
                            boxStateList.add(24 + i + 1);
                            LogUtil.getInstance().d("门开启：" + (24 + i + 1));
                        }
                    }
                    for (int i = 0; i < 8; i++) {
                        if ((buffer[9] >> i & 0x1) == 1) {
                            boxStateList.add(16 + i + 1);
                            LogUtil.getInstance().d("门开启：" + (16 + i + 1));
                        }
                    }
                    for (int i = 0; i < 8; i++) {
                        if ((buffer[10] >> i & 0x1) == 1) {
                            boxStateList.add(8 + i + 1);
                            LogUtil.getInstance().d("门开启：" + (8 + i + 1));
                        }
                    }
                    for (int i = 0; i < 8; i++) {
                        if ((buffer[11] >> i & 0x1) == 1) {
                            boxStateList.add(i + 1);
                            LogUtil.getInstance().d("门开启：" + (i + 1));
                        }
                    }
                    if (doorListener != null){
                        doorListener.checkBoxDoorState(buffer[3], boxStateList);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
