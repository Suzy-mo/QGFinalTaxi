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
    private final static String BASE_URL="http://39.98.41.126:31109/";
    private static Retrofit retrofit=new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
    public static <T> T create(Class<T> retrofitClass){
        return retrofit.create(retrofitClass);
    }
}
