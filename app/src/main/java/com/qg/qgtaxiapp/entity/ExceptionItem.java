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
    private String startAddress;
    private String endAddress;
    private String date;
    private String exceptionText;

    public ExceptionItem() {
    }

    public ExceptionItem(String carID, String startAddress, String endAddress, String date, String exceptionText) {
        this.carID = carID;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.date = date;
        this.exceptionText = exceptionText;
    }

    public String getCarID() {
        return carID;
    }

    public void setCarID(String carID) {
        this.carID = carID;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getExceptionText() {
        return exceptionText;
    }

    public void setExceptionText(String exceptionText) {
        this.exceptionText = exceptionText;
    }
}
