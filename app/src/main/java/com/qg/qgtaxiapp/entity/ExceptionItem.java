package com.qg.qgtaxiapp.entity;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/12/14:29
 * @Description:
 */
public class ExceptionItem {
    private String carID;
    private String address;
    private String companyID;
    private String exceptionText;

    public ExceptionItem() {
    }

    public ExceptionItem(String carID, String address, String companyID, String exceptionText) {
        this.carID = carID;
        this.address = address;
        this.companyID = companyID;
        this.exceptionText = exceptionText;
    }

    public String getCarID() {
        return carID;
    }

    public void setCarID(String carID) {
        this.carID = carID;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public String getExceptionText() {
        return exceptionText;
    }

    public void setExceptionText(String exceptionText) {
        this.exceptionText = exceptionText;
    }
}
