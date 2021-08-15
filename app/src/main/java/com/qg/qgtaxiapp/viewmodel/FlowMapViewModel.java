package com.qg.qgtaxiapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.google.android.material.shape.ShapePath;
import com.qg.qgtaxiapp.entity.FlowAllData;

import java.util.List;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/14 15:16
 */
public class FlowMapViewModel extends ViewModel {

    public MutableLiveData<Integer> selectTab = new MutableLiveData<>();
    public PolylineOptions polylineOptions = null;
    public MutableLiveData<String> flow_date = new MutableLiveData<>();

    public MutableLiveData<List<FlowAllData.DataBean>> allData = new MutableLiveData<>();

}
