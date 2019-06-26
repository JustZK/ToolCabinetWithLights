package com.zk.cabinet.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by ZK on 2018/1/11.
 */

@Entity(nameInDb = "Cabinet")
public class Cabinet {

    @Property(nameInDb = "ID")
    @Id
    private Long id;

    @Property(nameInDb = "CellNumber")
    @Unique
    private int cellNumber;

    @Property(nameInDb = "BoxName")
    private String boxName;

    @Property(nameInDb = "Proportion")
    private int proportion;

    @Property(nameInDb = "TargetAddress")
    private int targetAddress;

    @Property(nameInDb = "SourceAddress")
    private int sourceAddress;

    @Property(nameInDb = "LockNumber")
    private int lockNumber;

    @Property(nameInDb = "ReaderDeviceID")
    private int readerDeviceID;

    @Property(nameInDb = "AntennaNumber")
    private String antennaNumber;

    @Property(nameInDb = "SignBroken")
    private int signBroken;

    @Generated(hash = 273177328)
    public Cabinet(Long id, int cellNumber, String boxName, int proportion,
            int targetAddress, int sourceAddress, int lockNumber,
            int readerDeviceID, String antennaNumber, int signBroken) {
        this.id = id;
        this.cellNumber = cellNumber;
        this.boxName = boxName;
        this.proportion = proportion;
        this.targetAddress = targetAddress;
        this.sourceAddress = sourceAddress;
        this.lockNumber = lockNumber;
        this.readerDeviceID = readerDeviceID;
        this.antennaNumber = antennaNumber;
        this.signBroken = signBroken;
    }

    @Generated(hash = 456667810)
    public Cabinet() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCellNumber() {
        return this.cellNumber;
    }

    public void setCellNumber(int cellNumber) {
        this.cellNumber = cellNumber;
    }

    public String getBoxName() {
        return this.boxName;
    }

    public void setBoxName(String boxName) {
        this.boxName = boxName;
    }

    public int getProportion() {
        return this.proportion;
    }

    public void setProportion(int proportion) {
        this.proportion = proportion;
    }

    public int getTargetAddress() {
        return this.targetAddress;
    }

    public void setTargetAddress(int targetAddress) {
        this.targetAddress = targetAddress;
    }

    public int getSourceAddress() {
        return this.sourceAddress;
    }

    public void setSourceAddress(int sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public int getLockNumber() {
        return this.lockNumber;
    }

    public void setLockNumber(int lockNumber) {
        this.lockNumber = lockNumber;
    }

    public int getReaderDeviceID() {
        return this.readerDeviceID;
    }

    public void setReaderDeviceID(int readerDeviceID) {
        this.readerDeviceID = readerDeviceID;
    }

    public String getAntennaNumber() {
        return this.antennaNumber;
    }

    public void setAntennaNumber(String antennaNumber) {
        this.antennaNumber = antennaNumber;
    }

    public int getSignBroken() {
        return this.signBroken;
    }

    public void setSignBroken(int signBroken) {
        this.signBroken = signBroken;
    }

}
