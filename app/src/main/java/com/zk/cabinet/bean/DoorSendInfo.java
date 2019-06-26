package com.zk.cabinet.bean;

public class DoorSendInfo {
    private int communicationType;
    private int targetAddress;
    private int sourceAddress;
    private int lockNumber;

    public DoorSendInfo(int targetAddress, int sourceAddress, int lockNumber){
        this.communicationType = 0x03;
        this.targetAddress = targetAddress;
        this.sourceAddress = sourceAddress;
        this.lockNumber = lockNumber;
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

    public int getLockNumber() {
        return lockNumber;
    }

    public void setLockNumber(int lockNumber) {
        this.lockNumber = lockNumber;
    }
}
