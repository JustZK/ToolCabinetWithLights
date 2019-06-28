package com.zk.cabinet.serial.light.util;

import com.zk.cabinet.serial.door.util.DoorUtils;

import java.util.ArrayList;

public class LightGroupPackage {
    public static byte[] checkLightState(int targetAddress, int sourceAddress) {
        byte[] data;
        data = new byte[9];
        DoorUtils.initMessage(data);

        data[2] = (byte) targetAddress;
        data[3] = (byte) 0xff;

        data[6] = 0x00;
        data[7] = 0x05;

        data[8] = DoorUtils.calcCheckBit(data);

        return data;
    }


    public static byte[] openLight(int targetAddress, int sourceAddress, ArrayList<Integer> lightNumber) {
        byte[] data;
        data = new byte[17];
        LightUtils.initMessage(data);

        data[2] = (byte) targetAddress;
        data[3] = (byte) 0xff;

        data[6] = 0x00;
        data[7] = 0x07;

        String[] lightNumbers = new String[64];
        for (int i = 0; i < 64; i++) {
            lightNumbers[i] = "0";
        }
        for (int a : lightNumber) {
            int lockNumberTemp = a % 8;
            if (lockNumberTemp == 0) lockNumberTemp = 8;

            if (a <= 8) {
                lightNumbers[lockNumberTemp - 1] = "1";
            } else if (a > 8 && a <= 16) {
                lightNumbers[8 + lockNumberTemp - 1] = "1";
            } else if (a > 16 && a <= 24) {
                lightNumbers[16 + lockNumberTemp - 1] = "1";
            } else if (a > 24 && a <= 32) {
                lightNumbers[24 + lockNumberTemp - 1] = "1";
            } else if (a > 32 && a <= 40) {
                lightNumbers[32 + lockNumberTemp - 1] = "1";
            } else if (a > 40 && a <= 48) {
                lightNumbers[40 + lockNumberTemp - 1] = "1";
            } else if (a > 48 && a <= 56) {
                lightNumbers[48 + lockNumberTemp - 1] = "1";
            } else if (a > 56 && a <= 64) {
                lightNumbers[56 + lockNumberTemp - 1] = "1";
            }
        }
        String temp = "";
        for (int i = 7; i >= 0; i--) {
            temp += lightNumbers[i];
        }
        data[15] = (byte) Integer.parseInt(temp, 2);
        temp = "";
        for (int i = 15; i >= 8; i--) {
            temp += lightNumbers[i];
        }
        data[14] = (byte) Integer.parseInt(temp, 2);
        temp = "";
        for (int i = 23; i >= 16; i--) {
            temp += lightNumbers[i];
        }
        data[13] = (byte) Integer.parseInt(temp, 2);
        temp = "";
        for (int i = 31; i >= 24; i--) {
            temp += lightNumbers[i];
        }
        data[12] = (byte) Integer.parseInt(temp, 2);
        temp = "";
        for (int i = 39; i >= 32; i--) {
            temp += lightNumbers[i];
        }
        data[11] = (byte) Integer.parseInt(temp, 2);
        temp = "";
        for (int i = 47; i >= 40; i--) {
            temp += lightNumbers[i];
        }
        data[10] = (byte) Integer.parseInt(temp, 2);
        temp = "";
        for (int i = 55; i >= 48; i--) {
            temp += lightNumbers[i];
        }
        data[9] = (byte) Integer.parseInt(temp, 2);
        temp = "";
        for (int i = 63; i >= 56; i--) {
            temp += lightNumbers[i];
        }
        data[8] = (byte) Integer.parseInt(temp, 2);

        data[16] = LightUtils.calcCheckBit(data);

        return data;
    }
}
