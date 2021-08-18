package com.qg.qgtaxiapp.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.qg.qgtaxiapp.databinding.FragmentCarTraficFlowBinding;
import com.qg.qgtaxiapp.databinding.PoWindowLineBinding;
import com.qg.qgtaxiapp.entity.BaseCreator;
import com.qg.qgtaxiapp.entity.CarLineChartBean;
import com.qg.qgtaxiapp.entity.CarTrafficMarkBean;
import com.qg.qgtaxiapp.entity.IPost;
import com.qg.qgtaxiapp.utils.MapUtils;
import com.qg.qgtaxiapp.utils.PolygonRunnable;
import com.qg.qgtaxiapp.viewmodel.CarTrafficViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/16 23:18
 */
public class CarTrafficFlowFragment extends Fragment {

    private final int NOW_LINE = 0,FEATURE = 1;
    private FragmentCarTraficFlowBinding binding;
    private PoWindowLineBinding pwBinding;
    private CarTrafficViewModel viewModel;

    private MapView mapView;
    private AMap aMap = null;//地图控制器
    private DistrictSearch districtSearch;
    private DistrictSearchQuery districtSearchQuery;
    private PolygonRunnable polygonRunnable;
    private MapUtils mapUtils;
    private List<Marker> markers ;

    private PopupWindow popupWindow;
    private View rootView,contentView;
    ImageView backIV ;
    LineChart lineChart;
    List<Entry> lineCharData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        districtSearch = new DistrictSearch(getContext());
        viewModel = new ViewModelProvider(getActivity()).get(CarTrafficViewModel.class);
        mapUtils = new MapUtils();
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
                            polygonRunnable = new PolygonRunnable(item,handler);
                            new Thread(polygonRunnable).start();
                        }
                    }else {
                        showLog(districtResult.getAMapException().getErrorMessage());
                    }
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCarTraficFlowBinding.inflate(inflater,container,false);
        binding.windowsPwIv.setVisibility(View.INVISIBLE);
        mapView = binding.carTrafficFlowMap;
        mapView.onCreate(savedInstanceState);
        aMap = mapUtils.initMap(getContext(),mapView);
        drawBoundary();

        initMapMarkers();
        setMarkerListener();

        setLineDataObserve();
        setChooseObserve();
        return binding.getRoot();
    }

    private void setChooseObserve() {
    }

    private void setLineDataObserve() {
        viewModel.lineChartData.observe(getActivity(), new Observer<CarLineChartBean>() {
            @Override
            public void onChanged(CarLineChartBean carLineChartBean) {
                if(carLineChartBean!=null){
                    showPopupWindows(carLineChartBean);
                }
            }
        });
    }

    private void showPopupWindows(CarLineChartBean carLineChartBean) {
            viewModel.choose.setValue(NOW_LINE);
            binding.windowsPwIv.setVisibility(View.VISIBLE);
    }

    private void setMarkerListener() {
        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                getLineChartData(marker);
                return true;
            }
        });
    }

    private void getLineChartData(Marker marker) {
        String location = String.valueOf(marker.getPosition().latitude)+"-"+String.valueOf(marker.getPosition().longitude);
        new Thread(()->{
            IPost iPost = BaseCreator.createCarInfo(IPost.class);
            iPost.getCarLineChart(location).enqueue(new Callback<CarLineChartBean>() {
                @Override
                public void onResponse(Call<CarLineChartBean> call, Response<CarLineChartBean> response) {
                    if(response.isSuccessful()){
                        getActivity().runOnUiThread(()->{
                            viewModel.lineChartData.setValue(response.body());
                        });
                    }else {
                        getActivity().runOnUiThread(()->{
                            showLog("获取数据失败");
                        });
                    }
                }

                @Override
                public void onFailure(Call<CarLineChartBean> call, Throwable t) {
                    getActivity().runOnUiThread(()->{
                        showLog("获取数据失败 请检查服务器的问题或请求格式的问题");
                    });
                }
            });
        }).start();
    }

    private void initMapMarkers() {
        new Thread(()->{
            IPost iPost = BaseCreator.createCarInfo(IPost.class);
            iPost.getCarMarkers().enqueue(new Callback<CarTrafficMarkBean>() {
                @Override
                public void onResponse(Call<CarTrafficMarkBean> call, Response<CarTrafficMarkBean> response) {
                    if(response.isSuccessful()){
                        List<Marker> markerList = mapUtils.setCarTrafficMarkers(response.body(),aMap);
                        getActivity().runOnUiThread(()->{
                            markers = markerList;
                        });
                    }else {
                        getActivity().runOnUiThread(()->{
                            showLog("获取数据失败");
                        });
                    }
                }

                @Override
                public void onFailure(Call<CarTrafficMarkBean> call, Throwable t) {
                    getActivity().runOnUiThread(()->{
                        showLog("获取数据失败 请检查服务器的问题或请求格式的问题");
                    });
                }
            });
        }).start();
    }

    /**
     * Log.d打印日志
     */
    private void showLog(String log){
        Log.d("TAG_HeatMapFragment", log);
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
    private Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){

                case 0:{
                    PolylineOptions options = (PolylineOptions) msg.obj;
                    aMap.addPolyline(options);
                    viewModel.polylineOptions = options;
                }break;

                case 1:{
//                    List<HeatMapData.data> data = (List<HeatMapData.data>) msg.obj;
//                    viewModel.heatData.setValue(mapUtils.initHeatMapData(data));
                }break;
            }
        }
    };
}
