package com.zk.cabinet.netty.base;

public class NettyUtils {
    // 帧头
    public static final byte HEAD_HIGH = 0x5A;
    public static final byte HEAD_LOW = 0x55;
    // 帧尾
    public static final byte TAIL_HIGH = 0x6A;
    public static final byte TAIL_LOW = 0x69;
    // 端口
    public static final byte PORT = 0x0D;
    //针头、帧尾的个数和
    public static final int HEAD_TAIL_NUMBER = 4;

    public static final byte SPECIAL_0X5A_UNPACK = 0x5A;
    public static final byte SPECIAL_0X99_UNPACK = (byte) 0x99;
    public static final byte SPECIAL_0X6A_UNPACK = 0x6A;

    public static final byte SPECIAL_FOR_PACK = (byte) 0x99;

    public static final byte SPECIAL_0X5A_PACK = (byte) 0xA5;
    public static final byte SPECIAL_0X99_PACK = 0x66;
    public static final byte SPECIAL_0X6A_PACK = (byte) 0x95;

    // 类型
    public enum TYPE {
        MAIN_OPEN((byte) 0x01),
        MAIN_OPEN_RESULT((byte) 0x02),
        MAIN_CHECK_DOOR_STATUS((byte) 0x03),
        MAIN_CHECK_DOOR_STATUS_RESULT((byte) 0x04),
        DEPUTY_OPEN((byte) 0x05),
        DEPUTY_OPEN_RESULT((byte) 0x06),
        DEPUTY_CHECK_DOOR_STATUS((byte) 0x07),
        DEPUTY_CHECK_DOOR_STATUS_RESULT((byte) 0x08);

        byte type;

        TYPE(byte type) {
            this.type = type;
        }

        public byte getType() {
            return type;
        }
    }


    //java 合并两个byte数组
    public static byte[] bytesMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    /**
     * 是否有特征字出现
     *
     * @return 特征字个数
     */
    public static int containCheck(byte[] buffer, int size) {
        int T = 0;
        for (int i = 2; i < size - 3; i++) {
            if (buffer[i] == SPECIAL_0X99_UNPACK) {
                T++;
                if ((buffer[i + 1]) != SPECIAL_0X5A_PACK && (buffer[i + 1]) != SPECIAL_0X99_PACK && (buffer[i + 1]) != SPECIAL_0X6A_PACK) {
                    return -1;
                }
            }
        }
        return T;
    }

    /**
     * 转译 解包
     *
     * @param buffer 未转译的帧
     * @param size   帧长度
     * @param T      特征字个数
     * @return 转译后的帧
     */
    public static byte[] translationForUnlock(byte[] buffer, int size, int T) {
        int k = 0;
        byte[] tBuffer = new byte[size - T];
        for (int i = 0; i < size - T; i++) {
            tBuffer[i] = buffer[k];
            if ((buffer[k]) == SPECIAL_0X99_UNPACK) {
                if ((buffer[k + 1]) == SPECIAL_0X5A_PACK) {
                    tBuffer[i] = SPECIAL_0X5A_UNPACK;
                    k++;
                } else if ((buffer[k + 1]) == SPECIAL_0X99_PACK) {
                    tBuffer[i] = SPECIAL_0X99_UNPACK;
                    k++;
                } else if ((buffer[k + 1]) == SPECIAL_0X6A_PACK) {
                    tBuffer[i] = SPECIAL_0X6A_UNPACK;
                    k++;
                }
            }
            k++;
        }
        return tBuffer;
    }

    /**
     * 和校验
     *
     * @param buffer 校验buffer
     * @param size 长度
     * @return boolean
     */
    public static boolean andCheck(byte[] buffer, int size) {
        byte check = buffer[size - 3], temp;
        temp = buffer[0];
        for (int i = 1; i < size - 3; i++) {
            temp = (byte) (temp + buffer[i]);
        }
//        if (check == temp)
//            return true;
//        else
//            return false;

        return check == temp;
    }

    /**
     * 初始化待发送的数据(配置帧头、帧尾、帧长度)
     *
     * @param data 待发送的数据
     * @return 完成配置的数据
     */
    public static void initMessage(byte[] data, int frameNumber) {
        // 去除帧头帧尾的长度
        int len = data.length - 4;

        data[0] = HEAD_HIGH;
        data[1] = HEAD_LOW;

        data[2] = (byte) (len % 256);

        data[3] = (byte) (frameNumber % 256);
        data[4] = (byte) (frameNumber / 256);

        data[5] = PORT;

        data[data.length - 2] = TAIL_HIGH;
        data[data.length - 1] = TAIL_LOW;
    }

    /**
     * 计算校验位
     * @param protocol 已验证帧头帧尾的单条数据帧
     * @return 校验位
     */
    public static byte calcCheckBit (byte[] protocol) {
        byte checkBit = protocol[0];
        for (int i = 1; i < protocol.length - 3; i++) {
            checkBit += protocol[i];
        }
        return checkBit;
    }


    /**
     * 需要转译的个数 For 组包
     */
    public static int ifTranslation(byte[] data) {
        int T = 0;
        for (int i = 2; i < data.length - 3; i++) {
            if (data[i] == SPECIAL_0X99_UNPACK || data[i] == SPECIAL_0X5A_UNPACK || data[i] == SPECIAL_0X6A_UNPACK) {
                T++;
            }
        }
        return T;
    }

    /**
     * 转译 For 组包
     */
    public static  byte[] translationForPack(byte[] data, int size, int T) {
        int k = 2;
        byte[] tData = new byte[size + T];
        tData[0] = data[0];
        tData[1] = data[1];
        tData[size + T - 2] = data[size - 2];
        tData[size + T - 1] = data[size - 1];
        for (int i = 2; i < size - 3; i++) {
            tData[k] = data[i];
            if (data[i] == SPECIAL_0X5A_UNPACK) {
                tData[k] = SPECIAL_0X99_UNPACK;
                k++;
                tData[k] = SPECIAL_0X5A_PACK;
            }
            else if (data[i] == SPECIAL_0X99_UNPACK) {
                tData[k] = SPECIAL_0X99_UNPACK;
                k++;
                tData[k] = SPECIAL_0X99_PACK;
            }
            else if (data[i] == SPECIAL_0X6A_UNPACK) {
                tData[k] = SPECIAL_0X99_UNPACK;
                k++;
                tData[k] = SPECIAL_0X6A_PACK;
            }
            k++;

        }
        return tData;
    }

    public static int byteArrayToInt(byte[] buffer, int length) {
        int tempInt;
        String tempStr;
        StringBuilder resultStr = new StringBuilder();
        for (int i = 0; i < length; i++) {
            tempInt = buffer[i] & 0xff;
            tempStr = Integer.toHexString(tempInt);
            while (tempStr.length() < 2)
                tempStr = "0" + tempStr;
            resultStr.append(tempStr);
        }
        return Integer.parseInt(resultStr.toString(), 16);
    }

    public static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

}

