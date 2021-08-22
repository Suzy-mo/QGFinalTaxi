package com.qg.qgtaxiapp.entity;

import java.util.List;

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
    @POST("getFlowGraph")
    Call<ResponseData<List<FlowAllData>>> getFlowAllData(@Field("date")String date);


    @FormUrlEncoded
    @POST("mainRoute")
    Call<FlowMainDataLine> getFlowMainDataLine(@Field("time")String date);

    @FormUrlEncoded
    @POST("getSalary2")
    Call<CarIncomeBean> getCarInfo(@Field("date")String date);


    @POST("selectFlowPoint")
    Call<CarTrafficMarkBean> getCarMarkers();

    @FormUrlEncoded
    @POST("selectFlowLine")
    Call<CarLineChartBean> getCarLineChart(@Field("longitude")String longitude,@Field("latitude")String latitude);

    @POST("getCenterRadiusForMobile")
    Call<FlowMainDataArea> getFlowMainDataArea();
}
