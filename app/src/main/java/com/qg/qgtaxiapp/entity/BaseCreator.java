package com.qg.qgtaxiapp.entity;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/14 19:49
 */
public class BaseCreator {
    private final static String Flow_All_BASE_URL ="http://39.98.41.126:31100/";
    private final static String Flow_Main_BASE_URL ="http://39.98.41.126:31106/";
    private static final Retrofit retrofitAll=new Retrofit.Builder().baseUrl(Flow_All_BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
    private static final Retrofit retrofitMain=new Retrofit.Builder().baseUrl(Flow_Main_BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
    public static <T> T createAll(Class<T> retrofitClass){
        return retrofitAll.create(retrofitClass);
    }
    public static <T> T createMain(Class<T> retrofitClass){
        return retrofitMain.create(retrofitClass);
    }
    public static <T> T createCarInfo(Class<T> retrofitClass){
        return retrofitAll.create(retrofitClass);
    }
}
