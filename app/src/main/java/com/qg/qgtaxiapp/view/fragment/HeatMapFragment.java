package com.qg.qgtaxiapp.view.fragment;

import static com.qg.qgtaxiapp.utils.MapUtils.getAssetsStyle;
import static com.qg.qgtaxiapp.utils.MapUtils.getAssetsStyleExtra;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CustomMapStyleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.google.android.material.tabs.TabLayout;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.databinding.FragmentHeatMapBinding;
import com.qg.qgtaxiapp.utils.MapUtils;
import com.qg.qgtaxiapp.utils.PolygonRunnable;
import com.qg.qgtaxiapp.view.activity.MainActivity;
import com.qg.qgtaxiapp.viewmodel.HeatMapViewModel;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/10/17:09
 * @Description:
 */
public class HeatMapFragment extends Fragment{

    private static final int MIN_DISTANCE = 200; //最小滑动距离
    private FragmentHeatMapBinding binding;
    private HeatMapViewModel heatMapViewModel;
    private String tabList[] = {"热力图","载客热点","广告牌"};
    private TabLayout tabLayout;
    private int tabPosition = 0; //Tab显示位置
    private MyGestureDetector myGestureDetector;
    private GestureDetector gestureDetector;
    private UiSettings uiSettings;
    private MapView mapView;
    private AMap aMap = null;//地图控制器
    private DistrictSearch districtSearch;
    private DistrictSearchQuery districtSearchQuery;
    private PolygonRunnable polygonRunnable;
    private TextView tv_setTime;
    private TimePickerView timePickerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        districtSearch = new DistrictSearch(getContext());
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

    @SuppressLint("NewApi")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHeatMapBinding.inflate(inflater,container,false);
        tabLayout = binding.fragmentHeatTabLayout;
        heatMapViewModel = new ViewModelProvider(getActivity()).get(HeatMapViewModel.class);
        myGestureDetector = new MyGestureDetector();
        gestureDetector = new GestureDetector(getContext(), myGestureDetector);
        mapView = binding.fragmentHeatMapView;
        mapView.onCreate(savedInstanceState);
        tv_setTime = binding.tvSetTime;
        initTimePicker();
        tv_setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerView.show();
            }
        });

        for (String tabName : tabList){
            tabLayout.addTab(tabLayout.newTab().setText(tabName));
        }
        /*
            注册屏幕事件监听
         */
        ((MainActivity)this.getActivity()).registerMyTouchListener(myTouchListener);

        heatMapViewModel.selectTab.observe(getActivity(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                tabLayout.selectTab(tabLayout.getTabAt(integer));
            }
        });

        initMap(savedInstanceState);
        drawBoundary();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        /** 触摸事件的注销 */
        ((MainActivity)this.getActivity()).unRegisterMyTouchListener(myTouchListener);
        if (mapView != null){
            mapView.onDestroy();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null){
            mapView.onDestroy();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    /*
        获取时间
     */
    private String getTime(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }
    /*
        初始化时间选择器
     */
    private void initTimePicker(){
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        startDate.set(2017,0,1);
        endDate.set(2017,11,31);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            timePickerView = new TimePickerBuilder(getContext(), new OnTimeSelectListener() {
                @Override
                public void onTimeSelect(Date date, View v) {
                    showLog(getTime(date));
                }
            }).setLayoutRes(R.layout.timepicker_date, new CustomListener() {
                @Override
                public void customLayout(View v) {
                    TextView next = v.findViewById(R.id.timepicker_date_next);
                    ImageView cancel = v.findViewById(R.id.timepicker_date_cancel);
                    next.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            timePickerView.dismiss();
                        }
                    });
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            timePickerView.dismiss();
                        }
                    });

                }
            })
                .setTitleText("时间选择 • 日期")
                .setOutSideCancelable(false)
                .isCyclic(true)
                .setRangDate(startDate,endDate)
                .setOutSideColor(getResources().getColor(R.color.timepicker_outside))
                .setBgColor(getResources().getColor(R.color.timepicker_background))
                .isDialog(true)
                .isCenterLabel(true)
                .setType(new boolean[]{false,true,true,false,false,false})
                .build();
        }

    }
    /**
     * 初始化地图
     * @param savedInstanceState
     */
    private void initMap(Bundle savedInstanceState) {

        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mapView.onCreate(savedInstanceState);
        //初始化地图控制器对象
        aMap = mapView.getMap();

        CustomMapStyleOptions customMapStyleOptions = new CustomMapStyleOptions();
        customMapStyleOptions.setEnable(true);
        customMapStyleOptions.setStyleData(getAssetsStyle(getContext()));
        customMapStyleOptions.setStyleExtraData(getAssetsStyleExtra(getContext()));
        aMap.setCustomMapStyle(customMapStyleOptions);

        uiSettings = aMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_LEFT);
        uiSettings.setLogoBottomMargin(-100);
        uiSettings.setScaleControlsEnabled(true);
        uiSettings.setZoomControlsEnabled(false);
        /*
            设置地图的范围
         */
        LatLng northeast = new LatLng(23.955343,114.054936);
        LatLng southwest = new LatLng(22.506530,112.968270);
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast).include(southwest).build();
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,10));
        aMap.setMapStatusLimits(bounds);
    }

    /** 接收MainActivity的Touch回调的对象，重写其中的onTouchEvent函数 */
    MainActivity.MyTouchListener myTouchListener = new MainActivity.MyTouchListener() {
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            //处理手势事件（根据个人需要去返回和逻辑的处理）
            return gestureDetector.onTouchEvent(event);
        }
    };

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
        Log.d("TAG_HeatMapFragment", log);
    }

    /**
     * 自定义MyGestureDetector类继承SimpleOnGestureListener
     */
    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(e1.getX() - e2.getX() > MIN_DISTANCE){//左滑

                if (tabPosition != (tabLayout.getTabCount() - 1)){
                    tabPosition ++;
                }
                heatMapViewModel.selectTab.setValue(tabPosition);
            }else if(e2.getX() - e1.getX() > MIN_DISTANCE){//右滑

                if (tabPosition != 0){
                    tabPosition --;
                }
                heatMapViewModel.selectTab.setValue(tabPosition);
            }
            showLog("第" + tabPosition + "个Tab");
            return true;
        }
    }

    /*
        获取边界点的设置
     */
    private void drawBoundary(){
        if (heatMapViewModel.polylineOptions != null){
            aMap.addPolyline(heatMapViewModel.polylineOptions);
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
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){

                case 0:{
                    PolylineOptions options = (PolylineOptions) msg.obj;
                    aMap.addPolyline(options);
                    heatMapViewModel.polylineOptions = options;
                }break;

            }
        }
    };
}
