package com.zk.cabinet.serial.door.util;

import com.zk.cabinet.util.LogUtil;

public class DoorGroupPackage {
    public static byte[] checkBoxDoorState(int targetAddress, int sourceAddress) {
        byte[] data;
        data = new byte[9];
        DoorUtils.initMessage(data);

        data[2] = (byte) targetAddress;
        data[3] = (byte) 0xff;

        data[6] = 0x00;
        data[7] = 0x01;

        data[8] = DoorUtils.calcCheckBit(data);

        return data;
    }


    public static byte[] openDoor(int targetAddress, int sourceAddress, int lockNumber) {
        byte[] data;
        data = new byte[13];
        DoorUtils.initMessage(data);

        data[2] = (byte) targetAddress;
        data[3] = (byte) 0xff;

        data[6] = 0x00;
        data[7] = 0x03;

        LogUtil.getInstance().d("开门："+lockNumber);
        int lockNumberTemp = lockNumber % 8;
        if (lockNumberTemp == 0) lockNumberTemp =8;
        String temp = "1";
        for (int i = 1 ; i < lockNumberTemp; i++){
            temp += "0";
        }

        if (lockNumber <= 8){
            data[8] = 0x00;
            data[9] = 0x00;
            data[10] = 0x00;
            data[11] = (byte) Integer.parseInt(temp,2);
        } else if (lockNumber > 8 && lockNumber <= 16){
            data[8] = 0x00;
            data[9] = 0x00;
            data[11] = 0x00;
            data[10] = (byte) Integer.parseInt(temp,2);
        } else if (lockNumber > 16 && lockNumber <= 24){
            data[8] = 0x00;
            data[10] = 0x00;
            data[11] = 0x00;
            data[9] = (byte) Integer.parseInt(temp,2);
        } else if (lockNumber > 24 && lockNumber <= 32){
            data[9] = 0x00;
            data[10] = 0x00;
            data[11] = 0x00;
            data[8] = (byte) Integer.parseInt(temp,2);
        }
        data[12] = DoorUtils.calcCheckBit(data);


        return data;
    }
}
