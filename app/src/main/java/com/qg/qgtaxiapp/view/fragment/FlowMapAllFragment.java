package com.qg.qgtaxiapp.view.fragment;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.tabs.TabLayout;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.databinding.FragmentCarTraficFlowBinding;
import com.qg.qgtaxiapp.databinding.FragmentFlowMapAllBinding;
import com.qg.qgtaxiapp.databinding.FragmentFlowMapBinding;
import com.qg.qgtaxiapp.databinding.PoWindowLineBinding;
import com.qg.qgtaxiapp.entity.BaseCreator;
import com.qg.qgtaxiapp.entity.CarLineChartBean;
import com.qg.qgtaxiapp.entity.CarTrafficMarkBean;
import com.qg.qgtaxiapp.entity.FlowAllData;
import com.qg.qgtaxiapp.entity.FlowMainDataArea;
import com.qg.qgtaxiapp.entity.FlowMainDataLine;
import com.qg.qgtaxiapp.entity.IPost;
import com.qg.qgtaxiapp.entity.ResponseData;
import com.qg.qgtaxiapp.utils.LineChartsUtils;
import com.qg.qgtaxiapp.utils.MapUtils;
import com.qg.qgtaxiapp.utils.PolygonRunnable;
import com.qg.qgtaxiapp.utils.TimePickerUtils;
import com.qg.qgtaxiapp.viewmodel.CarTrafficViewModel;
import com.qg.qgtaxiapp.viewmodel.FlowMapViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/20 8:44
 */
public class FlowMapAllFragment extends Fragment {

    private FragmentFlowMapAllBinding binding;
    private FlowMapViewModel viewModel;

    private MapView mapView;
    private AMap aMap = null;//地图控制器
    private DistrictSearch districtSearch;
    private DistrictSearchQuery districtSearchQuery;
    private PolygonRunnable polygonRunnable;
    private TextView tv_setTime,tv_date,tv_choose,tv_timeTable;
    private TimePickerView datePickerView;
    private TimePickerUtils timePickerUtils;
    private MapUtils mapUtils;
    private List<Polyline> allPolyLines = new ArrayList<>();//全流图和主流向图的画图工具
    private AMapGestureListener listener;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(getActivity()).get(FlowMapViewModel.class);
        districtSearch = new DistrictSearch(getContext());
        timePickerUtils = new TimePickerUtils();
        mapUtils = new MapUtils();

        datePickerView = timePickerUtils.initFlowDatePicker(getContext(), getActivity(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                showLog("onTimeSelect: 选择时间"+timePickerUtils.getDate(date));
                tv_date.setText(timePickerUtils.getDate(date));
                getAllLineData(timePickerUtils.getDate(date));
            }
        });

        /*
            获取边界数据回调
         */
        districtSearch.setOnDistrictSearchListener(new DistrictSearch.OnDistrictSearchListener() {
            @Override
            public void onDistrictSearched(DistrictResult districtResult) {

                if (districtResult != null && districtResult.getDistrict() != null){
                    if (districtResult.getAMapException().getErrorCode() == AMapException.CODE_AMAP_SUCCESS){

                        ArrayList<DistrictItem> districtItems = districtResult.getDistrict();
                        DistrictItem item = null;
                        if (districtItems != null && districtItems.size() > 0){
                            //广州市 adcode：440100
                            for (DistrictItem districtItem : districtItems){
                                if(districtItem.getAdcode().equals("440100")){
                                    item = districtItem;
                                    break;
                                }
                            }
                            if (item == null){
                                return;
                            }
                            Log.d("TAG_Hx","创建子线程");
                            polygonRunnable = new PolygonRunnable(item,handler);
                            new Thread(polygonRunnable).start();
                        }
                    }
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFlowMapAllBinding.inflate(inflater,container,false);
        mapView = binding.flowMap;
        mapView.onCreate(savedInstanceState);
        tv_setTime = binding.tvSetTime;
        tv_date = binding.tvDate;
        tv_choose = binding.tvHeatChooseTime;
        tv_timeTable = binding.tvTimeLabel;


        initFirstView();

        aMap = mapUtils.initMap(getContext(),mapView);
        initMapListener();
        aMap.setAMapGestureListener(listener);
        drawBoundary();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        clearMap();
        super.onResume();
    }

    private void initMapListener() {
        listener=new AMapGestureListener() {
            @Override
            public void onDoubleTap(float v, float v1) {

            }

            @Override
            public void onSingleTap(float v, float v1) {

            }

            @Override
            public void onFling(float v, float v1) {

            }

            @Override
            public void onScroll(float v, float v1) {
                viewModel.vp2.setUserInputEnabled(false);
            }

            @Override
            public void onLongPress(float v, float v1) {

            }

            @Override
            public void onDown(float v, float v1) {

            }

            @Override
            public void onUp(float v, float v1) {
                viewModel.vp2.setUserInputEnabled(true);
            }

            @Override
            public void onMapStable() {

            }
        };
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initListener();
        setAllDataObserve();
        //super.onViewCreated(view, savedInstanceState);
    }

    private void setAllDataObserve() {
        viewModel.allData.observe(getActivity(), new Observer<List<FlowAllData>>() {
            @Override
            public void onChanged(List<FlowAllData> dataBeans) {
                if(dataBeans == null){
                    showLog("setAllDataObserve：数据为空");
                }else{
                    clearMap();
                    List<LatLng> mData = mapUtils.readLatLng(dataBeans);
                    allPolyLines = mapUtils.setFlowAllLine(mData, aMap);
                    showLog("setAllDataObserve：展示全部数据");
                }
            }
        });

    }

    /**
     * 设置控件的监听器
     */
    private void initListener() {
        tv_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLastView();
                datePickerView.show();
            }
        });

        tv_setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerView.show();
            }
        });


    }

    /**
     * 设置初始的界面
     */
    private void initFirstView() {
        tv_choose.setVisibility(View.VISIBLE);
        tv_setTime.setVisibility(View.INVISIBLE);
        tv_date.setVisibility(View.INVISIBLE);
        tv_timeTable.setVisibility(View.INVISIBLE);
    }

    /**
     * 点击修改时间后
     */
    private void initLastView() {
        tv_choose.setVisibility(View.INVISIBLE);
        tv_setTime.setVisibility(View.VISIBLE);
        tv_date.setVisibility(View.VISIBLE);
        tv_timeTable.setVisibility(View.VISIBLE);
    }


    /**
     * @param s
     * @return void
     * @description  开子线程获取所有的流向
     * @author Suzy.Mo
     * @time
     */

    private void getAllLineData(String s) {
        showLog("getAllLineData:查询的日期为"+s);
        IPost iPost = BaseCreator.createAll(IPost.class);
        iPost.getFlowAllData(s).enqueue(new Callback<ResponseData<List<FlowAllData>>>() {
            @Override
            public void onResponse(Call<ResponseData<List<FlowAllData>>> call, Response<ResponseData<List<FlowAllData>>> response) {
                if (response.body() != null) {
                    viewModel.allData.setValue(response.body().getData());
                    showLog("getAllLineData： 数据已放到flowMapViewModel");
                } else {
                    showMsg("暂时没有相关数据");
                }
                showLog(response.body().getMsg());
            }

            @Override
            public void onFailure(Call<ResponseData<List<FlowAllData>>> call, Throwable t) {
                showLog("onFailure:不知道什么原因获取失败");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null){
            mapView.onDestroy();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mapView != null){
            mapView.onDestroy();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * Toast提示
     * @param msg 提示内容
     */
    private void showMsg(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
    /**
     * Log.d打印日志
     */
    private void showLog(String log){
        Log.d("TAG_FlowMap_AllFragment", log);
    }

    /*
        获取边界点的设置
     */
    private void drawBoundary(){
        if (viewModel.polylineOptions != null){
            aMap.addPolyline(viewModel.polylineOptions);
            return;
        }
        String city = "广州";
        districtSearchQuery = new DistrictSearchQuery();
        //设置关键字
        districtSearchQuery.setKeywords(city);
        //设置是否返回边界值
        districtSearchQuery.setShowBoundary(true);
        districtSearch.setQuery(districtSearchQuery);
        districtSearch.searchDistrictAsyn();
    }

    /*
        消息处理
     */
    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){

                case 0:{
                    PolylineOptions options = (PolylineOptions) msg.obj;
                    aMap.addPolyline(options);
                    viewModel.polylineOptions = options;
                }break;
            }
        }
    };

    private void clearMap(){
        showLog("clearMap: 清空划线");
        for (int i = 0 ; i <allPolyLines.size(); i++){
            allPolyLines.get(i).setVisible(false);
        }
        allPolyLines.clear();
    }
}
