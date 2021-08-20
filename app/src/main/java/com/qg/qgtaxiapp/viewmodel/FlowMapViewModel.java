package com.qg.qgtaxiapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.viewpager2.widget.ViewPager2;

import com.amap.api.maps.model.PolylineOptions;
import com.qg.qgtaxiapp.entity.FlowAllData;
import com.qg.qgtaxiapp.entity.FlowMainDataArea;
import com.qg.qgtaxiapp.entity.FlowMainDataLine;

import java.util.List;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/14 15:16
 */
public class FlowMapViewModel extends ViewModel {

    public ViewPager2 vp2;
    public MutableLiveData<Integer> selectTab = new MutableLiveData<>();
    public PolylineOptions polylineOptions = null;
    public MutableLiveData<String> flow_date = new MutableLiveData<>();

    public MutableLiveData<List<FlowAllData>> allData = new MutableLiveData<>();
    public MutableLiveData<FlowMainDataLine> MainDataLine = new MutableLiveData<>();
    public MutableLiveData<List<FlowMainDataArea>> MainDataArea = new MutableLiveData<>();

}
