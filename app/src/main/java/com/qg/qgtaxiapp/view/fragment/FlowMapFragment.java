package com.qg.qgtaxiapp.view.fragment;

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
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.google.android.material.tabs.TabLayout;
import com.qg.qgtaxiapp.databinding.FragmentFlowMapBinding;
import com.qg.qgtaxiapp.entity.BaseCreator;
import com.qg.qgtaxiapp.entity.FlowAllData;
import com.qg.qgtaxiapp.entity.FlowMainDataLine;
import com.qg.qgtaxiapp.entity.IPost;
import com.qg.qgtaxiapp.entity.ResponseData;
import com.qg.qgtaxiapp.utils.MapUtils;
import com.qg.qgtaxiapp.utils.PolygonRunnable;
import com.qg.qgtaxiapp.utils.TimePickerUtils;
import com.qg.qgtaxiapp.view.activity.MainActivity;
import com.qg.qgtaxiapp.viewmodel.FlowMapViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/10/17:12
 * @Description:
 */

public class FlowMapFragment extends Fragment {
    private static final int MIN_DISTANCE = 200; //最小滑动距离
    private String tabList[] = {"全流图","主流图"};
    private FragmentFlowMapBinding binding;
    private FlowMapViewModel flowMapViewModel;
    private TabLayout tabLayout;
    private static int TAB_ALL = 0,TAB_MAIN = 1;
    private int tabPosition = TAB_ALL; //Tab显示位置
    private FlowMapFragment.MyGestureDetector myGestureDetector;
    private GestureDetector gestureDetector;
    private UiSettings uiSettings;
    private MapView mapView;
    private AMap aMap = null;//地图控制器
    private Polyline mPolyline ;//画图工具
    private DistrictSearch districtSearch;
    private DistrictSearchQuery districtSearchQuery;
    private PolygonRunnable polygonRunnable;
    private TextView tv_setTime,tv_date,tv_choose,tv_timeTable;
    private TimePickerView datePickerView;
    private TimePickerUtils timePickerUtils;
    private MapUtils mapUtils;
    private List<Polyline> allPolyLines ;//全流图和主流向图的画图工具


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        flowMapViewModel = new ViewModelProvider(getActivity()).get(FlowMapViewModel.class);
        myGestureDetector = new FlowMapFragment.MyGestureDetector();
        gestureDetector = new GestureDetector(getContext(), myGestureDetector);
        districtSearch = new DistrictSearch(getContext());
        timePickerUtils = new TimePickerUtils();
        mapUtils = new MapUtils();
        flowMapViewModel.selectTab.setValue(tabPosition);

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
        binding = FragmentFlowMapBinding.inflate(inflater,container,false);
        tabLayout = binding.fragmentHeatTabLayout;
        mapView = binding.flowMap;
        mapView.onCreate(savedInstanceState);
        tv_setTime = binding.tvSetTime;
        tv_date = binding.tvDate;
        tv_choose = binding.tvHeatChooseTime;
        tv_timeTable = binding.tvTimeLabel;

        initFirstView();
        initTimeChoose();
        initListener();

        setTabChangeObserve();
        setTimeChooseObserve();
        setAllDataObserve();
        setMainDataObserve();

        for (String tabName : tabList){
            tabLayout.addTab(tabLayout.newTab().setText(tabName));
        }

        aMap = mapUtils.initMap(getContext(),mapView);
        drawBoundary();
        return binding.getRoot();
    }

    private void setTimeChooseObserve() {
        flowMapViewModel.flow_date.observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tv_date.setText(s);
                renewUI(s,flowMapViewModel.selectTab.getValue());
            }
        });
    }

    /**
     * 更新页面（滑动或者时间选择后）
     */
    private void renewUI(String s,Integer integer) {
        if(integer == TAB_ALL){
            showLog("在全流图");
            getAllLineData(s);
        }else {
            showLog("renewUI：在主流图");
            getMainData(s);
        }
    }

    private void setMainDataObserve() {

        flowMapViewModel.MainDataLine.observe(getActivity(), new Observer<FlowMainDataLine>() {
            @Override
            public void onChanged(FlowMainDataLine flowMainDataLine) {
                aMap.clear();
                aMap = mapUtils.initMap(getContext(),mapView);
                drawBoundary();
                showLog("主流图数据变化监听成功");
                List<Polyline> polylines = mapUtils.setFlowMainLines(flowMainDataLine, aMap);
                showLog("主流图数据转换成功");
                allPolyLines = polylines;
            }
        });
//        flowMapViewModel.MainDataLine.observe(getActivity(), new Observer<List<FlowMainDataLine>>() {
//            @Override
//            public void onChanged(List<FlowMainDataLine> flowMainDataLines) {
//                new Thread(()->{
//                    List<Polyline> polylines = mapUtils.setFlowMainLines(flowMainDataLines,aMap);
//                    getActivity().runOnUiThread(()->{
//                        mainPolyLines =polylines;
//                    });
//                }).start();
//            }
//        });


    }

    private void setTabChangeObserve() {
        flowMapViewModel.selectTab.observe(getActivity(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                renewUI(flowMapViewModel.flow_date.getValue(),integer);
//                if (integer == TAB_ALL) {
//
//                    for (int i = 0; i < AllPolyLines.size(); i++) {
//                        AllPolyLines.get(i).setVisible(true);
//                    }
//                } else {
//                    for (int i = 0; i < AllPolyLines.size(); i++) {
//                        AllPolyLines.get(i).setVisible(false);
//                    }
//                }
            }
        });

    }

    private void setAllDataObserve() {
        flowMapViewModel.allData.observe(getActivity(), new Observer<List<FlowAllData>>() {
            @Override
            public void onChanged(List<FlowAllData> dataBeans) {
                if(dataBeans == null){
                    showLog("setAllDataObserve：数据为空");
                }else{
                    aMap.clear();
                    aMap = mapUtils.initMap(getContext(),mapView);
                    drawBoundary();
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

        /*
            注册屏幕事件监听
         */
        ((MainActivity)this.getActivity()).registerMyTouchListener(myTouchListener);

        flowMapViewModel.selectTab.observe(getActivity(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                tabLayout.selectTab(tabLayout.getTabAt(integer));
            }
        });
    }

    /**
     * 时间选择器的设置
     */
    private void initTimeChoose() {
        datePickerView = timePickerUtils.initFlowDatePicker(getContext(), getActivity(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                showLog(timePickerUtils.getDate(date).toString());
                flowMapViewModel.flow_date.setValue(timePickerUtils.getDate(date));
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

    private void getMainData(String s) {
        if(s!=null){
            new Thread(()->{
                showLog("getMainData : 开子线程去获取");
                IPost iPost = BaseCreator.createMain(IPost.class);
                iPost.getFlowMainDataLine(s).enqueue(new Callback<FlowMainDataLine>() {
                    @Override
                    public void onResponse(Call<FlowMainDataLine> call, Response<FlowMainDataLine> response) {
                        getActivity().runOnUiThread(()->{
                            showLog("回到主线程");
                            showLog(response.toString());
                            if (response.isSuccessful()){
                                showLog("getMainData: onResponse: 拿到数据");
                                flowMapViewModel.MainDataLine.setValue(response.body());
                                showLog("拿到的第一个数据是："+response.body().getData().get(0).getLocation().get(0).getLatitude()+"\n主流图拿到数成功");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<FlowMainDataLine> call, Throwable t) {
                        getActivity().runOnUiThread(()->{
                            showLog("获取数据失败");
                        });
                    }
                });


            }).start();
        }else {
            Toast.makeText(getContext(),"请选择要查看的日期",Toast.LENGTH_SHORT).show();
        }

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
        new Thread(()->{
            IPost iPost = BaseCreator.createAll(IPost.class);
            iPost.getFlowAllData(s).enqueue(new Callback<ResponseData<List<FlowAllData>>>() {
                @Override
                public void onResponse(Call<ResponseData<List<FlowAllData>>> call, Response<ResponseData<List<FlowAllData>>> response) {
                        getActivity().runOnUiThread(()->{
                            flowMapViewModel.allData.setValue(response.body().getData());
                            showLog("拿到数据的情况：" + response.body().getMsg());
                            showLog("拿到数据的第一个点：" + response.body().getData().get(0).getOffLatitude());
                        });
                        showLog(response.body().getMsg());

                }

                @Override
                public void onFailure(Call<ResponseData<List<FlowAllData>>> call, Throwable t) {
                    getActivity().runOnUiThread(()->{
                        showLog("onFailure:不知道什么原因获取失败");
                   });
                }
            });

        }).start();

//        //没有数据暂时设置模拟
//        List<FlowAllData.DataBean> data = mapUtils.setAllData();
//        flowMapViewModel.allData.setValue(data);
//        showLog("数据获取成功");


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
            //mPolyline.setVisible(false);
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
        Log.d("TAG_FlowMapFragment", log);
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
                flowMapViewModel.selectTab.setValue(tabPosition);
            }else if(e2.getX() - e1.getX() > MIN_DISTANCE){//右滑

                if (tabPosition != 0){
                    tabPosition --;
                }
                flowMapViewModel.selectTab.setValue(tabPosition);
            }
            flowMapViewModel.selectTab.setValue(tabPosition);
            showLog("第" + tabPosition + "个Tab");
            return true;
        }
    }

    /*
        获取边界点的设置
     */
    private void drawBoundary(){
        if (flowMapViewModel.polylineOptions != null){
            aMap.addPolyline(flowMapViewModel.polylineOptions);
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
                    flowMapViewModel.polylineOptions = options;
                }break;
            }
        }
    };

}
