package com.qg.qgtaxiapp.entity;

import java.io.Serializable;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/12/21:23
 * @Description: 所有的搜索历史记录
 */
public class HistoryInfo {
    private String carID;

    public HistoryInfo() {
    }

    public HistoryInfo(String carID) {
        this.carID = carID;
    }

    public String getCarID() {
        return carID;
    }

    public void setCarID(String carID) {
        this.carID = carID;
    }
}
