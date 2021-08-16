package com.qg.qgtaxiapp.entity;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/14 19:41
 */
public interface IPost {

//    @FormUrlEncoded
//    @HTTP(method = "GET",path = "getFlowGraph/{day}",hasBody = false)
//    Call<ResponseData<FlowAllData>> getFlowAllData(@Path("day") String day);

    @GET("getFlowGraph")
    Call<ResponseData<FlowAllData>> getFlowAllData(@Path("date") String day);

//    @FormUrlEncoded
//    @GET("flow/main")
//    Call<ResponseData<FlowAllData>> getFlowAllData(@Field("day")String day);


    @FormUrlEncoded
    @POST("flow/main")
    Call<ResponseData<FlowMainData>> getFlowMainData(@Field("data")String day);

}
