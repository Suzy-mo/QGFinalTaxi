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
    @POST("flow/all")
    Call<ResponseData<FlowAllData>> getFlowAllData(@Field("day")String day);

    @FormUrlEncoded
    @POST("flow/main")
    Call<ResponseData<FlowMainData>> getFlowMainData(@Field("day")String day);

}
