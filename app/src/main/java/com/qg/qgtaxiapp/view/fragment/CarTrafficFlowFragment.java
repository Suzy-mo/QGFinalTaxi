package com.qg.qgtaxiapp.view.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.databinding.ExceptionCheckDialogLayoutBinding;
import com.qg.qgtaxiapp.databinding.FragmentCarTraficFlowBinding;
import com.qg.qgtaxiapp.databinding.PoWindowLineBinding;
import com.qg.qgtaxiapp.entity.BaseCreator;
import com.qg.qgtaxiapp.entity.CarLineChartBean;
import com.qg.qgtaxiapp.entity.CarTrafficMarkBean;
import com.qg.qgtaxiapp.entity.IPost;
import com.qg.qgtaxiapp.utils.LineChartsUtils;
import com.qg.qgtaxiapp.utils.MapUtils;
import com.qg.qgtaxiapp.utils.PolygonRunnable;
import com.qg.qgtaxiapp.view.myview.MarkerView;
import com.qg.qgtaxiapp.viewmodel.CarTrafficViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @Name???QGTaxiApp
 * @Description???
 * @Author???Suzy.Mo
 * @Date???2021/8/16 23:18
 */
public class CarTrafficFlowFragment extends Fragment {

    private final int NOW_LINE = 0,FEATURE = 1;
    private FragmentCarTraficFlowBinding binding;
    private PoWindowLineBinding pwBinding;
    private CarTrafficViewModel viewModel;

    private MapView mapView;
    private AMap aMap = null;//???????????????
    private DistrictSearch districtSearch;
    private DistrictSearchQuery districtSearchQuery;
    private PolygonRunnable polygonRunnable;
    private MapUtils mapUtils;
    private List<Marker> markers ;

    private PopupWindow popupWindow;
    private View rootView,contentView;
    private AMapGestureListener listener;

    ImageView backIV,chooseIv ;
    LineChart lineChart;
    List<Entry> weekendData = new ArrayList<>(), workdayData = new ArrayList<>(),featureData = new ArrayList<>();

    private AlertDialog dialog;//??????
    private AlertDialog.Builder builder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        districtSearch = new DistrictSearch(getContext());
        viewModel = new ViewModelProvider(getActivity()).get(CarTrafficViewModel.class);
        mapUtils = new MapUtils();


        /*
            ????????????????????????
         */
        districtSearch.setOnDistrictSearchListener(new DistrictSearch.OnDistrictSearchListener() {
            @Override
            public void onDistrictSearched(DistrictResult districtResult) {

                if (districtResult != null && districtResult.getDistrict() != null){
                    if (districtResult.getAMapException().getErrorCode() == AMapException.CODE_AMAP_SUCCESS){

                        ArrayList<DistrictItem> districtItems = districtResult.getDistrict();
                        DistrictItem item = null;
                        if (districtItems != null && districtItems.size() > 0){
                            //????????? adcode???440100
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
        initListener();
        aMap.setAMapGestureListener(listener);
        viewModel.choose.setValue(NOW_LINE);

        drawBoundary();

        initMapMarkers();
        setMarkerListener();

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        if(viewModel.lineChartData.getValue()==null){
            builder = new AlertDialog.Builder(getContext());
            pwBinding = PoWindowLineBinding.inflate(getLayoutInflater());
            backIV = pwBinding.backTv;
            chooseIv = pwBinding.carTraficChooseIv;
            lineChart = pwBinding.lineChar;
            builder.setView(pwBinding.getRoot());
            if(dialog == null){
                dialog = builder.create();
            }
            dialog.dismiss();
            setLineDataObserve();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setLineDataObserve() {
        showLog("setLineDataObserve:???????????????????????????");
        viewModel.lineChartData.observe(getActivity(), new Observer<CarLineChartBean>() {
            @Override
            public void onChanged(CarLineChartBean carLineChartBean) {
                if(carLineChartBean!=null){
                    showLog("setLineDataObserve:???????????? ????????????");
                    showDialogWindows(carLineChartBean);
                    //showPopupWindows(carLineChartBean);
                }
            }
        });
    }

    private void showDialogWindows(CarLineChartBean carLineChartBean){
        viewModel.choose.setValue(NOW_LINE);
        initDialogListener(carLineChartBean);
        setCharline(carLineChartBean);
        dialog.show();
        binding.windowsPwIv.setVisibility(View.INVISIBLE);
        changeDialogSize(dialog);
    }

    private void changeDialogSize(AlertDialog dialog) {
        Window window=dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void initDialogListener(CarLineChartBean carLineChartBean) {
        binding.windowsPwIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                binding.windowsPwIv.setVisibility(View.INVISIBLE);
            }
        });

        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                binding.windowsPwIv.setVisibility(View.INVISIBLE);
            }
        });

        chooseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("??????????????????????????????");
                showLog("carLineChartBean: ??????????????????"+carLineChartBean.getData().get(0).getFeature().get(0));
                if(viewModel.choose.getValue() == FEATURE){
                    showLog("?????????????????????????????????????????????");
                    setNowLineChart(carLineChartBean);
                }else if(viewModel.choose.getValue() == NOW_LINE){
                    showLog("?????????????????????????????????????????????");
                    setFeatureChart(carLineChartBean);
                }else{
                    showLog("????????????");
                }
            }
        });
    }


    private void setCharline(CarLineChartBean carLineChartBean) {
        showLog("setCharline: ?????????????????????????????????????????????");
        if(viewModel.choose.getValue() == NOW_LINE){
            setNowLineChart(carLineChartBean);
        }else if(viewModel.choose.getValue() == FEATURE){
            setFeatureChart(carLineChartBean);
        }else{
            showLog("????????????");
        }

    }

    private void initListener() {
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

    private void setFeatureChart(CarLineChartBean carLineChartBean) {
        showLog("setFeatureChart: ????????????????????????");
        featureData.clear();
        viewModel.choose.setValue(FEATURE);
        chooseIv.setImageResource(R.drawable.car_trafic_line_feature);
        for(int i = 1 ; i < 24 ;i ++){
            featureData.add(new Entry(i,carLineChartBean.getData().get(0).getFeature().get(i).getNumber().floatValue()));
        }
        showLog("setFeatureChart: ?????????????????? ??????????????????");
        LineChartsUtils chartsUtils = new LineChartsUtils(getContext());
        //lineChart = new LineChart(getContext());
        chartsUtils.setFeatureLine(lineChart, featureData);

    }

    private void setNowLineChart(CarLineChartBean carLineChartBean) {
        showLog("setNowLineChart: ????????????????????????");
        weekendData.clear();
        workdayData.clear();
        //weekendData = new ArrayList<>();
        //workdayData = new ArrayList<>();
        viewModel.choose.setValue(NOW_LINE);
        chooseIv.setImageResource(R.drawable.car_trafic_line_now);
        if(carLineChartBean!=null){
            for(int i = 1 ; i < 24 ;i ++){
                workdayData.add(new Entry(i,carLineChartBean.getData().get(0).getWeekday().get(i).getNumber().floatValue()));
                weekendData.add(new Entry(i,carLineChartBean.getData().get(0).getWeekend().get(i).getNumber().floatValue()));
            }showLog("setNowLineChart: ?????????????????? ??????????????????");
            LineChartsUtils chartsUtils = new LineChartsUtils(getContext());
            chartsUtils.setNowLine(lineChart, workdayData, weekendData);
        }else {
            showLog("????????????");
        }
    }

    private void setMarkerListener() {
        showLog("setMarkerListener: ???????????????????????????");
        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                showLog("setMarkerListener: ????????????????????????! ?????????:"+marker.getPosition().toString());
                getLineChartData(marker);
                return true;
            }
        });
    }

    /**
     * ?????????????????????????????????
     * @param marker
     */
    private void getLineChartData(Marker marker) {
        showLog("getLineChartData: ????????????????????????????????????");
        //viewModel.lineChartData.setValue(mapUtils.testChartLine());
        String latitude = String.valueOf(marker.getPosition().latitude);
        String longitude = String.valueOf(marker.getPosition().longitude);
//        String latitude = "23.14174901";
//        String longitude = "113.2738607";
        new Thread(()->{
            IPost iPost = BaseCreator.createCarTraffic(IPost.class);
            iPost.getCarLineChart(longitude,latitude).enqueue(new Callback<CarLineChartBean>() {
                @Override
                public void onResponse(Call<CarLineChartBean> call, Response<CarLineChartBean> response) {
                    if(response.isSuccessful()){
                        getActivity().runOnUiThread(()->{
                            showLog(response.toString());
                            viewModel.lineChartData.setValue(response.body());
                            showLog("getLineChartData: ???????????????viewModel??????");
                        });
                    }else {
                        getActivity().runOnUiThread(()->{
                            showLog("??????????????????");
                        });
                    }
                }

                @Override
                public void onFailure(Call<CarLineChartBean> call, Throwable t) {
                    getActivity().runOnUiThread(()->{
                        showLog("?????????????????? ???????????????????????????????????????????????????");
                    });
                }
            });
        }).start();
        showLog("getLineChartData: ??????????????????");
    }

    /**
     * ????????????????????????
     */
    private void initMapMarkers() {
        showLog("??????initMapMarkers????????????????????????");
        //markers = mapUtils.setCarTrafficMarkers(mapUtils.testTrafficMarkers(),aMap);
        new Thread(()->{
            IPost iPost = BaseCreator.create06(IPost.class);
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
                            showLog("??????????????????");
                        });
                    }
                }

                @Override
                public void onFailure(Call<CarTrafficMarkBean> call, Throwable t) {
                    getActivity().runOnUiThread(()->{
                        showLog("?????????????????? ???????????????????????????????????????????????????");
                    });
                }
            });
        }).start();
        showLog("?????????????????????");
    }

    /**
     * Log.d????????????
     */
    private void showLog(String log){
        Log.d("TAG_HeatMapFragment", log);
    }

    /*
        ????????????????????????
     */
    private void drawBoundary(){
        if (viewModel.polylineOptions != null){
            aMap.addPolyline(viewModel.polylineOptions);
            return;
        }
        String city = "??????";
        districtSearchQuery = new DistrictSearchQuery();
        //???????????????
        districtSearchQuery.setKeywords(city);
        //???????????????????????????
        districtSearchQuery.setShowBoundary(true);
        districtSearch.setQuery(districtSearchQuery);
        districtSearch.searchDistrictAsyn();
    }

    /*
        ????????????
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

    @Override
    public void onPause() {
        dialog.dismiss();
        viewModel.lineChartData.setValue(null);
        super.onPause();
    }

    @Override
    public void onStop() {
        dialog.dismiss();
        viewModel.lineChartData.setValue(null);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        dialog.dismiss();
        viewModel.lineChartData.setValue(null);
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        dialog.dismiss();
        viewModel.lineChartData.setValue(null);
        super.onDestroyView();
    }
}
