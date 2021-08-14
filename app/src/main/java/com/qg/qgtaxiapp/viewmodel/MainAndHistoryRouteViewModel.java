package com.qg.qgtaxiapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;
import com.qg.qgtaxiapp.entity.HistoryInfo;

import java.util.ArrayList;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/13/14:55
 * @Description:
 */
public class MainAndHistoryRouteViewModel extends ViewModel {
    public PolylineOptions polylineOptions = null;

}
