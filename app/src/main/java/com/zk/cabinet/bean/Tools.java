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

    // 工具状态
    @Expose
    @SerializedName("ToolLightNumber")
    @Property(nameInDb = "ToolLightNumber")
    private int toolLightNumber;

    // 借
    @Expose
    @SerializedName("Borrower")
    @Property(nameInDb = "Borrower")
    private String borrower;

    @Expose
    @SerializedName("Selected")
    @Property(nameInDb = "Selected")
    private boolean selected;

    @Generated(hash = 65853307)
    public Tools(Long id, String toolName, String epc, int cellNumber,
            int toolState, int toolLightNumber, String borrower, boolean selected) {
        this.id = id;
        this.toolName = toolName;
        this.epc = epc;
        this.cellNumber = cellNumber;
        this.toolState = toolState;
        this.toolLightNumber = toolLightNumber;
        this.borrower = borrower;
        this.selected = selected;
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

    public int getToolLightNumber() {
        return this.toolLightNumber;
    }

    public void setToolLightNumber(int toolLightNumber) {
        this.toolLightNumber = toolLightNumber;
    }

    public boolean getSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    
}
