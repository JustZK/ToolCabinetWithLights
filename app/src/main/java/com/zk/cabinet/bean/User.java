package com.zk.cabinet.bean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

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
    private String userID;

    @SerializedName("Password")
    @Property(nameInDb = "Password")
    private String password;

    @Property(nameInDb = "CardID")
    private String cardID;

    @Property(nameInDb = "FingerPrint")
    private String fingerPrint;

    @Generated(hash = 131490364)
    public User(Long id, String userName, String userID, String password,
            String cardID, String fingerPrint) {
        this.id = id;
        this.userName = userName;
        this.userID = userID;
        this.password = password;
        this.cardID = cardID;
        this.fingerPrint = fingerPrint;
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

    public String getFingerPrint() {
        return this.fingerPrint;
    }

    public void setFingerPrint(String fingerPrint) {
        this.fingerPrint = fingerPrint;
    }

    
}
