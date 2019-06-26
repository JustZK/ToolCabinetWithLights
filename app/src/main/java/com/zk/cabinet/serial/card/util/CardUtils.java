package com.zk.cabinet.serial.card.util;

public class CardUtils {
    // 帧头
    public static final byte HEAD = (byte) 0x02;
    // 帧尾
    public static final byte TAIL_1 = 0x0D;
    public static final byte TAIL_2 = 0x0A;
    public static final byte TAIL_3 = 0x03;

    //java 合并两个byte数组
    public static byte[] bytesMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }
}
