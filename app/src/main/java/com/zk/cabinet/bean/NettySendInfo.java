package com.zk.cabinet.bean;

public class NettySendInfo {
    private int communicationType;
    private int readerIp;
    private int fastID;
    private int antennaNumber;
    private int inventoryType;

    public NettySendInfo(int readerIp, int fastID, int antennaNumber, int inventoryType) {
        this.communicationType = 0x03;
        this.readerIp = readerIp;
        this.fastID = fastID;
        this.antennaNumber = antennaNumber;
        this.inventoryType = inventoryType;
    }

    public int getReaderIp() {
        return readerIp;
    }

    public void setReaderIp(int readerIp) {
        this.readerIp = readerIp;
    }

    public int getFastID() {
        return fastID;
    }

    public void setFastID(int fastID) {
        this.fastID = fastID;
    }

    public int getAntennaNumber() {
        return antennaNumber;
    }

    public void setAntennaNumber(int antennaNumber) {
        this.antennaNumber = antennaNumber;
    }

    public int getInventoryType() {
        return inventoryType;
    }

    public void setInventoryType(int inventoryType) {
        this.inventoryType = inventoryType;
    }

    public int getCommunicationType() {
        return communicationType;
    }

    public void setCommunicationType(int communicationType) {
        this.communicationType = communicationType;
    }
}
