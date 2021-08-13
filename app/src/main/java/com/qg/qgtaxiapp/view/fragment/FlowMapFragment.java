package com.qg.qgtaxiapp.view.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.databinding.FragmentFlowMapBinding;

import java.util.ArrayList;
import java.util.List;


/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/10/17:12
 * @Description:
 */
public class FlowMapFragment extends Fragment {

    private FragmentFlowMapBinding binding;

    private AMap aMap;
    private TextureMapView mapView;

    //请求权限码
    private static final int REQUEST_PERMISSIONS = 9527;

    //private UiSettings uiSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding=FragmentFlowMapBinding.inflate(inflater,container,false);
        Log.d("=================","daozhelile");
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        initMap();
        binding.choiceTurnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClick();
            }
        });
    }

    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
    }

    public void setClick(){
        List<LatLng> latLngs =new ArrayList<>();
        latLngs.add(new LatLng(43.828, 87.621));
        latLngs.add(new LatLng(45.808, 100.55));
        latLngs.add(new LatLng(43.828, 87.621));
        latLngs.add(new LatLng(46.808, 86.55));
        for (int i = 0 ; i < latLngs.size();i+=2){
            setUpMap(latLngs.get(i),latLngs.get(i+1));
        }
    }

    private void setUpMap(LatLng begin,LatLng end) {

        // 设置当前地图级别为4
        aMap.moveCamera(CameraUpdateFactory.zoomTo(4));
        // 设置地图底图文字的z轴指数，默认为0
        aMap.setMapTextZIndex(2);

        // 绘制一个乌鲁木齐到哈尔滨的大地曲线
        aMap.addPolyline((new PolylineOptions())
                .add(begin,end)
                .geodesic(true).color(Color.BLUE));

    }



    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }


    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }


}
