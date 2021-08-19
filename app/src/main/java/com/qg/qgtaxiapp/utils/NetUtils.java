package com.qg.qgtaxiapp.utils;

import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/13/14:00
 * @Description:
 */
public class NetUtils {
    private String getRouteBaseURL ="http://39.98.41.126:31103/";
    private String getCarOwnerURL="http://39.98.41.126:31103/";
    private String getExceptionURL="http://39.98.41.126:31103/";
    private String getHeatMapURL = "http://39.98.41.126:31103/selectByTimeSlot2/";
    private String getPassengerURL = "http://39.98.41.126:31103/getHotPoints2";
    private String getPieDataURL="http://39.98.41.126:31100/getUtilization2/";
    private static NetUtils instance=new NetUtils();
    private static OkHttpClient okHttpClient;

    public static NetUtils getInstance(){
        return instance;
    }

    private NetUtils(){
        okHttpClient=new OkHttpClient();
        okHttpClient.newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS).build();
    }

    public void getRouteData(String carID, String date, Callback callback){
        String requestStr="getTrace2/"+carID+"/"+date;
        Request request=new Request.Builder().url(getRouteBaseURL +requestStr).build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public void getExceptionData(int pagerID,int limit,String name,Callback callback){
        String requestStr="findErrorTaxis2/"+pagerID+"/"+limit;
        if(name.length()!=0){
            requestStr=requestStr+"/"+name;
        }
        Request request=new Request.Builder().url(getExceptionURL+requestStr).build();
        okHttpClient.newCall(request).enqueue(callback);
    }


    public void getCarOwnerData(String carID,Callback callback){
        String requestStr="findCarInfoByPlate/"+carID;
        Request request=new Request.Builder().url(getCarOwnerURL+requestStr).build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public void getHeatMapData(String time, int area, int count, Callback callback){
        String requestStr = time + "/" + area + "/" + count;
        Request request = new Request.Builder().url(getHeatMapURL + requestStr).build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public void getPassengerData(Callback callback){
        Request request = new Request.Builder().url(getPassengerURL).build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public void getCurrentPieData(String date, Callback callback){
        String requestStr=getPieDataURL+date;
        Request request=new Request.Builder().url(requestStr).build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public void getCurrentPieData(Callback callback){
        String requestStr=getPieDataURL+"-----";
        Request request=new Request.Builder().url(requestStr).build();
        okHttpClient.newCall(request).enqueue(callback);
    }
}
