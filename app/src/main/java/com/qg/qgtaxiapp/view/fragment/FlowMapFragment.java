package com.qg.qgtaxiapp.view.fragment;

import android.app.AlertDialog;
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
import com.bigkoo.pickerview.view.TimePickerView;
import com.google.android.material.tabs.TabLayout;
import com.qg.qgtaxiapp.databinding.FragmentFlowMapBinding;
import com.qg.qgtaxiapp.entity.BaseCreator;
import com.qg.qgtaxiapp.entity.FlowAllData;
import com.qg.qgtaxiapp.entity.IPost;
import com.qg.qgtaxiapp.entity.ResponseData;
import com.qg.qgtaxiapp.utils.MapUtils;
import com.qg.qgtaxiapp.utils.PolygonRunnable;
import com.qg.qgtaxiapp.utils.TimePickerUtils;
import com.qg.qgtaxiapp.view.activity.MainActivity;
import com.qg.qgtaxiapp.viewmodel.FlowMapViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
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
    private TextView tv_setTime;
    private TimePickerView datePickerView;
    private TimePickerUtils timePickerUtils;
    private MapUtils mapUtils;
    private AlertDialog dialog = null;
    private TextView tv_date;
    private TextView tv_timeslot;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        flowMapViewModel = new ViewModelProvider(getActivity()).get(FlowMapViewModel.class);
        myGestureDetector = new FlowMapFragment.MyGestureDetector();
        gestureDetector = new GestureDetector(getContext(), myGestureDetector);
        districtSearch = new DistrictSearch(getContext());
        timePickerUtils = new TimePickerUtils();
        mapUtils = new MapUtils();
        //EventBus.getDefault().register(this);
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
        mapView = binding.fragmentHeatMapView;
        mapView.onCreate(savedInstanceState);
        tv_setTime = binding.tvSetTime;
        tv_date = binding.tvDate;
        datePickerView = timePickerUtils.initDatePicker(getContext(),getActivity());


        flowMapViewModel.flow_date.observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tv_date.setText(s);
                tv_setTime.setText(s);
                if(flowMapViewModel.selectTab.getValue() == TAB_ALL){
                    getAllLineData(s);
                }else {
                    getMainData(s);
                }

            }
        });

        flowMapViewModel.allData.observe(getActivity(), new Observer<List<FlowAllData.DataBean>>() {
            @Override
            public void onChanged(List<FlowAllData.DataBean> dataBeans) {
                List<LatLng> mData = mapUtils.readLatLng(dataBeans);
                if(flowMapViewModel.selectTab.getValue() == TAB_ALL){
                    mPolyline = mapUtils.setFlowAllLine(mData,aMap);
                }else {
                    mPolyline = mapUtils.setFlowMainLine(mData,aMap);
                }

            }
        });


        tv_setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerView.show();
            }
        });

        for (String tabName : tabList){
            tabLayout.addTab(tabLayout.newTab().setText(tabName));
        }
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

        aMap = mapUtils.initMap(getContext(),mapView);
        drawBoundary();
        return binding.getRoot();
    }

    private void getMainData(String s) {

    }

    /**
     * @param s
     * @return void
     * @description  开子线程获取所有的流向
     * @author Suzy.Mo
     * @time
     */

    private void getAllLineData(String s) {
        new Thread(()->{
            IPost iPost = BaseCreator.create(IPost.class);
            iPost.getFlowAllData(s).enqueue(new Callback<ResponseData<FlowAllData>>() {
                @Override
                public void onResponse(Call<ResponseData<FlowAllData>> call, Response<ResponseData<FlowAllData>> response) {
                    showLog(response.body().getMsg());
                    getActivity().runOnUiThread(()->{
                        flowMapViewModel.allData.setValue(response.body().getData().getData());
                    });
                }

                @Override
                public void onFailure(Call<ResponseData<FlowAllData>> call, Throwable t) {
                    getActivity().runOnUiThread(()->{
                        showLog("获取失败");
                    });

                }
            });
        }).start();
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
            mPolyline.setVisible(false);
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


//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onShowTimeSlotSet(EventBusEvent.showTimeSlotSet event){
//        dialog = timePickerUtils.initTimeSlotDialog(getContext());
//        dialog.show();
//
//        Window window = dialog.getWindow();
//        WindowManager manager = getActivity().getWindowManager();
//        Display display = manager.getDefaultDisplay();
//        WindowManager.LayoutParams params = window.getAttributes();
//        params.width = (int) (display.getWidth() * 0.98);
//        window.setAttributes(params);
//    }
//

}
