package com.qg.qgtaxiapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.amap.api.maps.model.LatLng;
import com.qg.qgtaxiapp.entity.HistoryInfo;

import java.util.ArrayList;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/13/16:46
 * @Description:
 */
public class SkipAndHistoryRouteViewModel extends ViewModel {
    public MutableLiveData<ArrayList<LatLng>> data=new MutableLiveData<>();
}
