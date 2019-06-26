package com.zk.cabinet.serial.door.util;

import com.zk.cabinet.util.LogUtil;

public class DoorUtils {
    // 帧头
    public static final byte HEAD_HIGH = (byte) 0xA6;
    public static final byte HEAD_LOW = (byte) 0xA8;

    //java 合并两个byte数组
    public static byte[] bytesMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    /**
     * 和校验
     *
     * @param buffer 校验buffer
     * @param size 长度
     * @return boolean
     */
    public static boolean andCheck(byte[] buffer, int size) {
        LogUtil.getInstance().d("合校验：" + (buffer[size - 1]));
        byte check = buffer[size - 1], temp;
        temp = buffer[0];
        for (int i = 1; i < size - 1; i++) {
            temp = (byte) (temp + buffer[i]);
        }
        LogUtil.getInstance().d("合校验end：" + (temp));
        return check == temp;
    }

    /**
     * 计算校验位
     * @param protocol 已验证帧头帧尾的单条数据帧
     * @return 校验位
     */
    public static byte calcCheckBit (byte[] protocol) {
        byte checkBit = protocol[0];
        for (int i = 1; i < protocol.length - 1; i++) {
            checkBit += protocol[i];
        }
        return checkBit;
    }

    public static void initMessage(byte[] data) {
        // 去除帧头帧尾的长度
        int len = data.length - 2;

        data[0] = HEAD_HIGH;
        data[1] = HEAD_LOW;

        data[4] = (byte) (len / 256);
        data[5] = (byte) (len % 256);
    }
}
