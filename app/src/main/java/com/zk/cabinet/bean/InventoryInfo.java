package com.zk.cabinet.bean;

public class InventoryInfo {
    private int antennaNumber;
    private int RSSI;
    private String EPC;

    public InventoryInfo(int antennaNumber, int RSSI, String EPC){
        this.antennaNumber = antennaNumber;
        this.RSSI = RSSI;
        this.EPC = EPC;
    }

    public int getAntennaNumber() {
        return antennaNumber;
    }

    public void setAntennaNumber(int antennaNumber) {
        this.antennaNumber = antennaNumber;
    }

    public int getRSSI() {
        return RSSI;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    public String getEPC() {
        return EPC;
    }

    public void setEPC(String EPC) {
        this.EPC = EPC;
    }
}
