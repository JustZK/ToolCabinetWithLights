package com.zk.cabinet.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.json.JSONException;
import org.json.JSONObject;

@Entity(nameInDb = "InventoryAll")
public class InventoryAll {
    @Property(nameInDb = "ID")
    @Id
    private Long id;

    @Property(nameInDb = "EPC")
    private String EPC;

    @Property(nameInDb = "RSSI")
    private int RSSI;

    @Property(nameInDb = "CountErNumber")
    private int countErNumber;

    @Property(nameInDb = "ReaderID")
    private int readerID;

    @Property(nameInDb = "AntennaNumber")
    private int antennaNumber;

    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("EPC", EPC);
            obj.put("RSSI", RSSI);
            obj.put("CountErNumber", countErNumber);
            obj.put("ReaderID", readerID);
            obj.put("AntennaNumber", antennaNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Generated(hash = 111762091)
    public InventoryAll(Long id, String EPC, int RSSI, int countErNumber,
            int readerID, int antennaNumber) {
        this.id = id;
        this.EPC = EPC;
        this.RSSI = RSSI;
        this.countErNumber = countErNumber;
        this.readerID = readerID;
        this.antennaNumber = antennaNumber;
    }

    @Generated(hash = 1431015785)
    public InventoryAll() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEPC() {
        return this.EPC;
    }

    public void setEPC(String EPC) {
        this.EPC = EPC;
    }

    public int getRSSI() {
        return this.RSSI;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    public int getCountErNumber() {
        return this.countErNumber;
    }

    public void setCountErNumber(int countErNumber) {
        this.countErNumber = countErNumber;
    }

    public int getReaderID() {
        return this.readerID;
    }

    public void setReaderID(int readerID) {
        this.readerID = readerID;
    }

    public int getAntennaNumber() {
        return this.antennaNumber;
    }

    public void setAntennaNumber(int antennaNumber) {
        this.antennaNumber = antennaNumber;
    }

   
}
