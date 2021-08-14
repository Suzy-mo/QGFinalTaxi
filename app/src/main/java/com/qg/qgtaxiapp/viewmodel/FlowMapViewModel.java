package com.qg.qgtaxiapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.amap.api.maps.model.PolylineOptions;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/14 15:16
 */
public class FlowMapViewModel extends ViewModel {
    public MutableLiveData<Integer> selectTab = new MutableLiveData<>();
    public PolylineOptions polylineOptions = null;
    public MutableLiveData<String> heat_date = new MutableLiveData<>();
    public MutableLiveData<String> heat_timeslot = new MutableLiveData<>();


}
