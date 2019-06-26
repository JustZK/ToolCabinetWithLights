package com.zk.cabinet.bean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity(nameInDb = "Tools")
public class Tools {

    // 本地id
    @Property(nameInDb = "ID")
    @Id
    private Long id;

    // 工具名称
    @Expose
    @SerializedName("ToolName")
    @Property(nameInDb = "ToolName")
    private String toolName;

    // 工具标签编号
    @Expose
    @SerializedName("EPC")
    @Property(nameInDb = "EPC")
    @Unique
    private String epc;

    // 所在箱号
    @Expose
    @SerializedName("CellNumber")
    @Property(nameInDb = "CellNumber")
    private int cellNumber;

    // 工具状态
    @Expose
    @SerializedName("ToolState")
    @Property(nameInDb = "ToolState")
    @NotNull
    private int toolState;

    // 借
    @Expose
    @SerializedName("Borrower")
    @Property(nameInDb = "Borrower")
    private String borrower;



    @Generated(hash = 2110080583)
    public Tools(Long id, String toolName, String epc, int cellNumber,
            int toolState, String borrower) {
        this.id = id;
        this.toolName = toolName;
        this.epc = epc;
        this.cellNumber = cellNumber;
        this.toolState = toolState;
        this.borrower = borrower;
    }

    @Generated(hash = 161980891)
    public Tools() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToolName() {
        return this.toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getEpc() {
        return this.epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public int getCellNumber() {
        return this.cellNumber;
    }

    public void setCellNumber(int cellNumber) {
        this.cellNumber = cellNumber;
    }

    public int getToolState() {
        return this.toolState;
    }

    public void setToolState(int toolState) {
        this.toolState = toolState;
    }

    public String getBorrower() {
        return this.borrower;
    }

    public void setBorrower(String borrower) {
        this.borrower = borrower;
    }

    
}
