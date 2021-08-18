package com.qg.qgtaxiapp.viewmodel;

import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.amap.api.maps.model.PolylineOptions;
import com.qg.qgtaxiapp.entity.CarLineChartBean;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/18 9:00
 */
public class CarTrafficViewModel extends ViewModel {

    public MutableLiveData<Integer> choose = new MutableLiveData<>();
    public MutableLiveData<CarLineChartBean> lineChartData = new MutableLiveData<>();
    public PolylineOptions polylineOptions = null;

}
