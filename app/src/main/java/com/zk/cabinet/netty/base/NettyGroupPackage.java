package com.zk.cabinet.netty.base;

public class NettyGroupPackage {

    public static byte[] registered() {
        byte[] data;
        data = new byte[14];
        NettyUtils.initMessage(data, 0);

        data[6] = 0x02;

        data[7] = 0x00;
        data[8] = 0x00;
        data[9] = 0x00;
        data[10] = 0x00;

        data[11] = NettyUtils.calcCheckBit(data);

        int T = NettyUtils.ifTranslation(data);
        if (T > 0)
            return(NettyUtils.translationForPack(data, data.length, T));
        else
            return data;
    }


    public static byte[] keepLive() {
        byte[] data;
        data = new byte[10];
        NettyUtils.initMessage(data, 0);

        data[6] = 0x03;

        data[7] = NettyUtils.calcCheckBit(data);

        int T = NettyUtils.ifTranslation(data);
        if (T > 0)
            return(NettyUtils.translationForPack(data, data.length, T));
        else
            return data;
    }

    public static byte[] inventory(int fastID, int antennaNumber, int inventoryType) {
        byte[] data;
        data = new byte[18];
        NettyUtils.initMessage(data, 0);

        data[6] = 0x07;

        data[7] = 0x00;
        data[8] = 0x00;
        data[9] = 0x00;
        data[10] = 0x00;

        data[11] = 0x00;

        data[12] = (byte) fastID;
        data[13] = (byte) antennaNumber;
        data[14] = (byte) inventoryType;

        data[15] = NettyUtils.calcCheckBit(data);

        int T = NettyUtils.ifTranslation(data);
        if (T > 0)
            return(NettyUtils.translationForPack(data, data.length, T));
        else
            return data;
    }
}
