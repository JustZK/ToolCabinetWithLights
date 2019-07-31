package com.zk.cabinet.bean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

@Entity(nameInDb = "User")
public class User {
    @Property(nameInDb = "ID")
    @Id
    private Long id;

    @Expose
    @SerializedName("UserName")
    @Property(nameInDb = "UserName")
    private String userName;

    @SerializedName("UserID")
    @Property(nameInDb = "UserID")
    @Unique
    private String userID;

    @SerializedName("Code")
    @Property(nameInDb = "Code")
    private String code;

    @SerializedName("MobilePhone")
    @Property(nameInDb = "MobilePhone")
    private String mobilePhone;

    @SerializedName("Password")
    @Property(nameInDb = "Password")
    private String password;

    @Property(nameInDb = "CardID")
    private String cardID;

    @Property(nameInDb = "FingerPrint")
    private byte[] fingerPrint;

    @Property(nameInDb = "FingerPrintTime")
    private String fingerPrintTime;

    @Property(nameInDb = "MechanismCoding")
    private String mechanismCoding;

    @Property(nameInDb = "MechanismName")
    private String mechanismName;

    @Generated(hash = 1909410224)
    public User(Long id, String userName, String userID, String code,
            String mobilePhone, String password, String cardID, byte[] fingerPrint,
            String fingerPrintTime, String mechanismCoding, String mechanismName) {
        this.id = id;
        this.userName = userName;
        this.userID = userID;
        this.code = code;
        this.mobilePhone = mobilePhone;
        this.password = password;
        this.cardID = cardID;
        this.fingerPrint = fingerPrint;
        this.fingerPrintTime = fingerPrintTime;
        this.mechanismCoding = mechanismCoding;
        this.mechanismName = mechanismName;
    }

    @Generated(hash = 586692638)
    public User() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return this.userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCardID() {
        return this.cardID;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public byte[] getFingerPrint() {
        return this.fingerPrint;
    }

    public void setFingerPrint(byte[] fingerPrint) {
        this.fingerPrint = fingerPrint;
    }

    public String getFingerPrintTime() {
        return this.fingerPrintTime;
    }

    public void setFingerPrintTime(String fingerPrintTime) {
        this.fingerPrintTime = fingerPrintTime;
    }

    public String getMechanismName() {
        return this.mechanismName;
    }

    public void setMechanismName(String mechanismName) {
        this.mechanismName = mechanismName;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMobilePhone() {
        return this.mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getMechanismCoding() {
        return this.mechanismCoding;
    }

    public void setMechanismCoding(String mechanismCoding) {
        this.mechanismCoding = mechanismCoding;
    }

    
    
}
