package com.zk.cabinet.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;

@Entity(nameInDb = "Tools")
public class Tools {
    @Property(nameInDb = "ID")
    @Id
    private Long id;

    //案件编号
    @Property(nameInDb = "CaseNumber")
    private String caseNumber;

    //出库物品类型
    @Property(nameInDb = "propertyInvolved")
    private String propertyInvolved;

    //涉案财物名称
    @Property(nameInDb = "PropertyInvolvedName")
    private String propertyInvolvedName;

    //涉案财物号码
    @Property(nameInDb = "PropertyNumber")
    private String propertyNumber ;

    //机构编码
    @Property(nameInDb = "MechanismCoding")
    private String mechanismCoding;

    //机构名称
    @Property(nameInDb = "MechanismName")
    private String mechanismName ;

    // 标签编号
    @Property(nameInDb = "EPC")
    private String epc;

    // 所在箱号
    @Property(nameInDb = "CellNumber")
    private int cellNumber;

    // 状态
    @Property(nameInDb = "State")
    private int state;

    // 灯号
    @Property(nameInDb = "ToolLightNumber")
    private int toolLightNumber;

    // 当事人姓名
    @Property(nameInDb = "NameParty")
    private String nameParty;

    // 操作时间
    @Property(nameInDb = "OperateTime")
    private String operateTime;

    @Transient
    private boolean selected;

    @Generated(hash = 584522984)
    public Tools(Long id, String caseNumber, String propertyInvolved,
            String propertyInvolvedName, String propertyNumber,
            String mechanismCoding, String mechanismName, String epc,
            int cellNumber, int state, int toolLightNumber, String nameParty,
            String operateTime) {
        this.id = id;
        this.caseNumber = caseNumber;
        this.propertyInvolved = propertyInvolved;
        this.propertyInvolvedName = propertyInvolvedName;
        this.propertyNumber = propertyNumber;
        this.mechanismCoding = mechanismCoding;
        this.mechanismName = mechanismName;
        this.epc = epc;
        this.cellNumber = cellNumber;
        this.state = state;
        this.toolLightNumber = toolLightNumber;
        this.nameParty = nameParty;
        this.operateTime = operateTime;
    }

    @Generated(hash = 161980891)
    public Tools() {
    }

    public String getCaseNumber() {
        return this.caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getPropertyInvolved() {
        return this.propertyInvolved;
    }

    public void setPropertyInvolved(String propertyInvolved) {
        this.propertyInvolved = propertyInvolved;
    }

    public String getPropertyInvolvedName() {
        return this.propertyInvolvedName;
    }

    public void setPropertyInvolvedName(String propertyInvolvedName) {
        this.propertyInvolvedName = propertyInvolvedName;
    }

    public String getPropertyNumber() {
        return this.propertyNumber;
    }

    public void setPropertyNumber(String propertyNumber) {
        this.propertyNumber = propertyNumber;
    }

    public String getMechanismCoding() {
        return this.mechanismCoding;
    }

    public void setMechanismCoding(String mechanismCoding) {
        this.mechanismCoding = mechanismCoding;
    }

    public String getMechanismName() {
        return this.mechanismName;
    }

    public void setMechanismName(String mechanismName) {
        this.mechanismName = mechanismName;
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

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getToolLightNumber() {
        return this.toolLightNumber;
    }

    public void setToolLightNumber(int toolLightNumber) {
        this.toolLightNumber = toolLightNumber;
    }

    public String getNameParty() {
        return this.nameParty;
    }

    public void setNameParty(String nameParty) {
        this.nameParty = nameParty;
    }

    public String getOperateTime() {
        return this.operateTime;
    }

    public void setOperateTime(String operateTime) {
        this.operateTime = operateTime;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
