package com.qg.qgtaxiapp.viewmodel;

import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.viewpager2.widget.ViewPager2;

import com.amap.api.maps.model.LatLng;
import com.qg.qgtaxiapp.entity.RouteData;

import java.util.ArrayList;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/15/19:19
 * @Description:
 */
public class HistoryMapViewModel extends ViewModel {
    public ViewPager2 viewPager2;
    public MutableLiveData<ArrayList<RouteData>> RouteLiveData=new MutableLiveData<>();
}
