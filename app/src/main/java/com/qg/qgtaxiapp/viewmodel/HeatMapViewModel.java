package com.qg.qgtaxiapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.viewpager2.widget.ViewPager2;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.TileOverlayOptions;
import com.qg.qgtaxiapp.entity.HeatMapData;

import java.util.List;

/**
 * @author: Hx
 * @date: 2021年08月12日 15:48
 */
public class HeatMapViewModel extends ViewModel {

    public ViewPager2 heatMapVP;
    public PolylineOptions polylineOptions = null;

    public MutableLiveData<String> heat_date = new MutableLiveData<>();
    public MutableLiveData<String> heat_timeslot = new MutableLiveData<>();
    public String heatTime = null;
    public MutableLiveData<List<LatLng>> heatData = new MutableLiveData<>();

    public MutableLiveData<List<LatLng>> passengerData = new MutableLiveData<>();

    public MutableLiveData<List<LatLng>> adData = new MutableLiveData<>();
}
