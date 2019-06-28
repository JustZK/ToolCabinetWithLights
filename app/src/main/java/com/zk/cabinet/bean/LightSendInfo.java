package com.zk.cabinet.bean;

import java.util.ArrayList;

public class LightSendInfo {
    private int communicationType;
    private int targetAddress;
    private int sourceAddress;
    private ArrayList<Integer> lightNumber;

    public LightSendInfo(int targetAddress, int sourceAddress, ArrayList<Integer> lightNumber){
        this.communicationType = 0x07;
        this.targetAddress = targetAddress;
        this.sourceAddress = sourceAddress;
        this.lightNumber = lightNumber;
    }

    public int getCommunicationType() {
        return communicationType;
    }

    public void setCommunicationType(int communicationType) {
        this.communicationType = communicationType;
    }

    public int getTargetAddress() {
        return targetAddress;
    }

    public void setTargetAddress(int targetAddress) {
        this.targetAddress = targetAddress;
    }

    public int getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(int sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public ArrayList<Integer> getLightNumber() {
        return lightNumber;
    }

    public void setLightNumber(ArrayList<Integer> lightNumber) {
        this.lightNumber = lightNumber;
    }
}
