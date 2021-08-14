package com.qg.qgtaxiapp.entity;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/14 19:41
 */
public interface IPost {

    @FormUrlEncoded
    @POST("user/login")
    Call<ResponseData<FlowAllData>> loginData(@Field("data")String data);

    @FormUrlEncoded
    @POST("user/login")
    Call<ResponseData<FlowMainData>> loginData(@Field("username")String username, @Field("password")String password);

}
