package com.qg.qgtaxiapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.amap.api.maps.model.PolylineOptions;

/**
 * @author: Hx
 * @date: 2021年08月12日 15:48
 */
public class HeatMapViewModel extends ViewModel {
    public MutableLiveData<Integer> selectTab = new MutableLiveData<>();
    public PolylineOptions polylineOptions = null;
    public MutableLiveData<String> heat_date = new MutableLiveData<>();
    public MutableLiveData<String> heat_timeslot = new MutableLiveData<>();

}
