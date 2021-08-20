package com.qg.qgtaxiapp.entity;

import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/20/16:00
 * @Description:
 */
public class RouteData {
    private ArrayList<LatLng> routeDataList;
    private int code;

    public RouteData() {
    }

    public RouteData(ArrayList<LatLng> routeDataList, int code) {
        this.routeDataList = routeDataList;
        this.code = code;
    }

    public ArrayList<LatLng> getRouteDataList() {
        return routeDataList;
    }

    public void setRouteDataList(ArrayList<LatLng> routeDataList) {
        this.routeDataList = routeDataList;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
