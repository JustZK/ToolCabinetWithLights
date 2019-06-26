package com.zk.cabinet.serial.card;

import com.zk.cabinet.callback.CardListener;
import com.zk.cabinet.serial.SerialHelper;
import com.zk.cabinet.serial.card.util.CardUtils;
import com.zk.cabinet.util.LogUtil;

import java.io.IOException;
import java.security.InvalidParameterException;

public class CardSerialOperation extends SerialHelper {
    public static final String TAG = "CardSerialOperation";

    private CardListener cardListener;

    private final String PATH = "/dev/ttyS1";
    private final String BAUD = "115200";
    private boolean isConnect = false;
    private volatile static CardSerialOperation instance;

    private byte[] remainBuffer = null;//上次解析剩余的数据

    private CardSerialOperation() {

    }

    public static CardSerialOperation getInstance() {
        if (instance == null) {
            synchronized (CardSerialOperation.class) {
                if (instance == null)
                    instance = new CardSerialOperation();
            }
        }
        return instance;
    }

    public void onCardListener(CardListener cardListener){
        this.cardListener = cardListener;
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
        stopSend();
        close();
        isConnect = false;
    }


    @Override
    protected void onDataReceived(String sPort, byte[] buffer, int size) {
        StringBuilder buffers = new StringBuilder();
        for (int i = 0; i < size; i++) {
            buffers.append(Integer.toHexString((buffer[i] & 0xff)));
            buffers.append(" ");
        }
        LogUtil.getInstance().d(TAG, "onDataReceived：：：：" + buffers);

        byte[] tempBytes;
        //如果上次解析有剩余，则将其加上
        if (remainBuffer != null && remainBuffer.length != 0) {
            tempBytes = CardUtils.bytesMerger(remainBuffer, buffer);
        } else {
            tempBytes = buffer;
        }

        remainBuffer = interceptionReceivedData(tempBytes);
    }

    /**
     * 截取完整的帧
     *
     * @param dataBytes 加上上次剩余的帧后的数据
     * @return 返回截取剩余的帧
     */
    private byte[] interceptionReceivedData(byte[] dataBytes) {
        if (dataBytes == null || dataBytes.length == 0) {
            return null;
        }
        int size = dataBytes.length;
        //针头帧尾的位置
        int headPosition1 = -1, tailPosition1 = -1, tailPosition2 = -1, tailPosition3 = -1;
        for (int i = 0; i < (size - 2); i++) {
            if (dataBytes[i] == CardUtils.HEAD) {
                headPosition1 = i;
            }
            if (dataBytes[i] == CardUtils.TAIL_1 &&
                    dataBytes[i + 1] == CardUtils.TAIL_2 &&
                    dataBytes[i + 2] == CardUtils.TAIL_3) {
                tailPosition1 = i;
                tailPosition2 = i + 1;
                tailPosition3 = i + 2;
                break;
            }
        }
        if (headPosition1 != -1 && tailPosition1 != -1 && tailPosition2 != -1 && tailPosition3 != -1) {
            if (tailPosition1 > headPosition1) {
                byte[] tempCompleteBytes = new byte[tailPosition3 - headPosition1 + 1];
                System.arraycopy(dataBytes, headPosition1, tempCompleteBytes, 0, tailPosition3 - headPosition1 + 1);
                //TODO tempCompleteBytes是本次取出的完整的数据
                if (cardListener != null){

                }
                //如果一组数据中有多个帧，则将剩余的数据发送
                if (size > (tailPosition3 + 1)) {
                    byte[] subTempBytes = new byte[size - tailPosition3 - 1];
                    System.arraycopy(dataBytes, tailPosition3 + 1, subTempBytes, 0, subTempBytes.length);
                    return interceptionReceivedData(subTempBytes);
                } else {
                    return null;
                }
            } else {
                byte[] subTempBytes = new byte[size - headPosition1];
                System.arraycopy(dataBytes, headPosition1, subTempBytes, 0, subTempBytes.length);
                return interceptionReceivedData(subTempBytes);
            }
        } else {
            return dataBytes;
        }
    }
}
