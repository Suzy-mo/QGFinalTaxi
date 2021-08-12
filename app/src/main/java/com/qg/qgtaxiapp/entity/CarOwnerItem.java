package com.qg.qgtaxiapp.entity;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/11/19:56
 * @Description:
 */
public class CarOwnerItem {
    private String carID;//车牌
    private String mile;//公里
    private String score;//评分
    private String companyID;//公司ID

    public CarOwnerItem() {
    }

    public String getCarID() {
        return carID;
    }

    public void setCarID(String carID) {
        this.carID = carID;
    }

    public String getMile() {
        return mile;
    }

    public void setMile(String mile) {
        this.mile = mile;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public CarOwnerItem(String carID, String mile, String score, String companyID) {
        this.carID = carID;
        this.mile = mile;
        this.score = score;
        this.companyID = companyID;
    }
}
