package com.qg.qgtaxiapp.view.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.HeatmapTileProvider;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.TileOverlayOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.bigkoo.pickerview.view.TimePickerView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.qg.qgtaxiapp.databinding.FragmentHeatMapBinding;
import com.qg.qgtaxiapp.entity.EventBusEvent;
import com.qg.qgtaxiapp.entity.HeatMapData;
import com.qg.qgtaxiapp.utils.MapUtils;
import com.qg.qgtaxiapp.utils.NetUtils;
import com.qg.qgtaxiapp.utils.PolygonRunnable;
import com.qg.qgtaxiapp.utils.TimePickerUtils;
import com.qg.qgtaxiapp.view.activity.MainActivity;
import com.qg.qgtaxiapp.viewmodel.HeatMapViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/10/17:09
 * @Description:
 */
public class HeatMapFragment extends Fragment{

    private static final int MIN_DISTANCE = 200; //最小滑动距离
    private String tabList[] = {"热力图","载客热点","广告牌"};
    private FragmentHeatMapBinding binding;
    private HeatMapViewModel heatMapViewModel;
    private TabLayout tabLayout;
    private int tabPosition = 0; //Tab显示位置
    private MyGestureDetector myGestureDetector;
    private GestureDetector gestureDetector;
    private MapView mapView;
    private AMap aMap = null;//地图控制器
    private DistrictSearch districtSearch;
    private DistrictSearchQuery districtSearchQuery;
    private PolygonRunnable polygonRunnable;
    private TimePickerView datePickerView;
    private TimePickerUtils timePickerUtils;
    private MapUtils mapUtils;
    private AlertDialog dialog = null;
    private TextView tv_changeTime;
    private TextView tv_date;
    private TextView tv_timeslot;
    private TextView tv_chooseTime;
    private ConstraintLayout cl_timeSet;
    private ConstraintLayout cl_chooseTime;
    private NetUtils netUtils = NetUtils.getInstance();
    private HeatmapTileProvider heatmapTileProvider;
    private TileOverlayOptions tileOverlayOptions;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        heatMapViewModel = new ViewModelProvider(getActivity()).get(HeatMapViewModel.class);
        myGestureDetector = new MyGestureDetector();
        gestureDetector = new GestureDetector(getContext(), myGestureDetector);
        districtSearch = new DistrictSearch(getContext());
        timePickerUtils = new TimePickerUtils();
        mapUtils = new MapUtils();
        EventBus.getDefault().register(this);
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
        binding = FragmentHeatMapBinding.inflate(inflater,container,false);
        tabLayout = binding.fragmentHeatTabLayout;
        mapView = binding.fragmentHeatMapView;
        mapView.onCreate(savedInstanceState);
        tv_changeTime = binding.tvChangeTime;
        tv_date = binding.tvDate;
        tv_timeslot = binding.tvTimeslot;
        tv_chooseTime = binding.tvChooseTime;
        cl_chooseTime = binding.fragmentHeatClChooseTime;
        cl_timeSet = binding.fragmentHeatClTime;

        datePickerView = timePickerUtils.initDatePicker(getContext(),getActivity());

        for (String tabName : tabList){
            tabLayout.addTab(tabLayout.newTab().setText(tabName));
        }

        heatMapViewModel.selectTab.observe(getActivity(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                switch (integer){
                    case 0:{
                        if (heatMapViewModel.heatTime == null) {
                            cl_timeSet.setVisibility(View.GONE);
                            cl_chooseTime.setVisibility(View.VISIBLE);
                        }else {
                            cl_timeSet.setVisibility(View.VISIBLE);
                            cl_chooseTime.setVisibility(View.GONE);
                            tv_date.setText(heatMapViewModel.heat_date.getValue());
                            tv_timeslot.setText(heatMapViewModel.heat_timeslot.getValue());
                        }
                    }break;

                    case 1:{
                        if (heatMapViewModel.passageTime == null) {
                            cl_timeSet.setVisibility(View.GONE);
                            cl_chooseTime.setVisibility(View.VISIBLE);
                        }else {
                            cl_timeSet.setVisibility(View.VISIBLE);
                            cl_chooseTime.setVisibility(View.GONE);
                            tv_date.setText(heatMapViewModel.passage_date.getValue());
                            tv_timeslot.setText(heatMapViewModel.passage_timeslot.getValue());
                        }
                    }break;

                    case 2:{
                        showLog("广告牌");
                    }break;
                }
            }
        });

        heatMapViewModel.heatData.observe(getActivity(), new Observer<List<LatLng>>() {
            @Override
            public void onChanged(List<LatLng> list) {
                heatmapTileProvider = mapUtils.initBuildHeatmapTileProvider(list);
                tileOverlayOptions = new TileOverlayOptions();
                tileOverlayOptions.tileProvider(heatmapTileProvider);
                aMap.addTileOverlay(tileOverlayOptions);
            }
        });

        /*
            时间监测
         */
        heatMapViewModel.heat_date.observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tv_date.setText(s);
            }
        });
        heatMapViewModel.heat_timeslot.observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tv_timeslot.setText(s);
            }
        });


        tv_changeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerView.show();
            }
        });

        tv_chooseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerView.show();
            }
        });


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

        aMap = mapUtils.initMap(getContext(),mapView);
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
                heatMapViewModel.tab = tabPosition;
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

                case 1:{
                    List<HeatMapData.data> data = (List<HeatMapData.data>) msg.obj;
                    heatMapViewModel.heatData.setValue(mapUtils.initHeatMapData(data));
                }break;
            }
        }
    };

    /*
        显示时间段设置Dialog
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShowTimeSlotSet(EventBusEvent.showTimeSlotSet event){
        dialog = timePickerUtils.initTimeSlotDialog(getContext());
        dialog.show();

        Window window = dialog.getWindow();
        WindowManager manager = getActivity().getWindowManager();
        Display display = manager.getDefaultDisplay();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int) (display.getWidth() * 0.98);
        window.setAttributes(params);
    }
    /*
        完成时间设置
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSetTimeFinish(EventBusEvent.setTimeFinish event){
        if (cl_chooseTime.getVisibility() == View.VISIBLE) {
            cl_chooseTime.setVisibility(View.GONE);
            cl_timeSet.setVisibility(View.VISIBLE);
        }
        if (heatMapViewModel.tab == 0) {
            heatMapViewModel.heat_date.setValue(timePickerUtils.getDate());
            heatMapViewModel.heat_timeslot.setValue(timePickerUtils.getTimeslot());
            heatMapViewModel.heatTime = timePickerUtils.getTime();
            getHeatData();
        }
        else if (heatMapViewModel.tab == 1){
            heatMapViewModel.passage_date.setValue(timePickerUtils.getDate());
            heatMapViewModel.passage_timeslot.setValue(timePickerUtils.getTimeslot());
            heatMapViewModel.passageTime = timePickerUtils.getTime();
        }
    }
    /*
        获取热力图数据
     */
    private void getHeatData(){
        netUtils.setGetHeatMapData(heatMapViewModel.heatTime, 0, 100, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showLog("热力图数据获取失败");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                Gson gson = new Gson();
                HeatMapData heatMapData = gson.fromJson(json,HeatMapData.class);
                List<HeatMapData.data> data = heatMapData.getData();
                Message message = handler.obtainMessage();
                message.what = 1;
                message.obj = data;
                handler.sendMessage(message);
            }
        });
    }

}
