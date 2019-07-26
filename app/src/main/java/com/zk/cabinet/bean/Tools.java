package com.zk.cabinet.bean;

public class Tools {

    //案件编号
    private String caseNumber;

    //涉案财物名称
    private String propertyInvolvedName;

    //涉案财物号码
    private String propertyNumber ;

    //机构编码
    private String mechanismCoding;

    //机构名称
    private String mechanismName ;

    // 标签编号
    private String epc;

    // 所在箱号
    private int cellNumber;

    // 状态
    private int toolState;

    // 灯号
    private int toolLightNumber;

    // 借
    private String borrower;

    private boolean selected;

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getPropertyInvolvedName() {
        return propertyInvolvedName;
    }

    public void setPropertyInvolvedName(String propertyInvolvedName) {
        this.propertyInvolvedName = propertyInvolvedName;
    }

    public String getPropertyNumber() {
        return propertyNumber;
    }

    public void setPropertyNumber(String propertyNumber) {
        this.propertyNumber = propertyNumber;
    }

    public String getMechanismCoding() {
        return mechanismCoding;
    }

    public void setMechanismCoding(String mechanismCoding) {
        this.mechanismCoding = mechanismCoding;
    }

    public String getMechanismName() {
        return mechanismName;
    }

    public void setMechanismName(String mechanismName) {
        this.mechanismName = mechanismName;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public int getCellNumber() {
        return cellNumber;
    }

    public void setCellNumber(int cellNumber) {
        this.cellNumber = cellNumber;
    }

    public int getToolState() {
        return toolState;
    }

    public void setToolState(int toolState) {
        this.toolState = toolState;
    }

    public int getToolLightNumber() {
        return toolLightNumber;
    }

    public void setToolLightNumber(int toolLightNumber) {
        this.toolLightNumber = toolLightNumber;
    }

    public String getBorrower() {
        return borrower;
    }

    public void setBorrower(String borrower) {
        this.borrower = borrower;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
