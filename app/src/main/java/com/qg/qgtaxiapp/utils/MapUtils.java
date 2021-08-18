package com.qg.qgtaxiapp.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CustomMapStyleOptions;
import com.amap.api.maps.model.Gradient;
import com.amap.api.maps.model.HeatmapTileProvider;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.application.MyApplication;
import com.qg.qgtaxiapp.entity.CarLineChartBean;
import com.qg.qgtaxiapp.entity.CarTrafficMarkBean;
import com.qg.qgtaxiapp.entity.FlowMainDataLine;
import com.qg.qgtaxiapp.entity.HeatMapData;
import com.qg.qgtaxiapp.entity.FlowAllData;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author: Hx
 * @date: 2021年08月09日 16:43
 */
public class MapUtils {

    /*
     *设置热力图颜色
     */
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.rgb( 0, 0, 255),
            Color.rgb( 0, 211, 248),
            Color.rgb(0, 255, 0),
            Color.rgb(185, 71, 0),
            Color.rgb(255, 0, 0)
    };

    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = { 0.1f,
            0.2f, 0.25f, 0.4f, 1f };

    public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(
            ALT_HEATMAP_GRADIENT_COLORS, ALT_HEATMAP_GRADIENT_START_POINTS);


    //生成HeatmapTileProvider
    public HeatmapTileProvider initBuildHeatmapTileProvider(List<LatLng> list) {
        HeatmapTileProvider.Builder builder = new HeatmapTileProvider.Builder();
        builder.data(list);
        builder.gradient(ALT_HEATMAP_GRADIENT);
        HeatmapTileProvider heatmapTileProvider = builder.build();
        return heatmapTileProvider;
    }



    //生成热力点坐标列表
    public List<LatLng> initHeatMapData(List<HeatMapData.data> data){
        List<LatLng> list = new ArrayList<>();

        for (HeatMapData.data data1 : data){
         double latitude = data1.getLatitude();
         double longitude = data1.getLongitude();
         LatLng latLng = new LatLng(latitude,longitude);
         list.add(latLng);
        }
        return list;
    }

    /*
        读取自定义地图样式
     */
    public static byte[] getAssetsStyle(Context context){
        byte[] buffer = null;
        InputStream is = null;
        try {
            is = context.getResources().getAssets().open("style.data");
            int len = is.available();
            buffer = new byte[len];
            is.read(buffer);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if (is != null){
                    is.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return buffer;
    }
    public static byte[] getAssetsStyleExtra(Context context){
        byte[] buffer = null;
        InputStream is = null;
        try {
            is = context.getResources().getAssets().open("style_extra.data");
            int len = is.available();
            buffer = new byte[len];
            is.read(buffer);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if (is != null){
                    is.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return buffer;
    }



    /**
     * 初始化地图
     * @param mapView
     */
    public AMap initMap(Context context,MapView mapView) {

        UiSettings uiSettings = null;
        AMap aMap = null;
        //初始化地图控制器对象
        aMap = mapView.getMap();

        CustomMapStyleOptions customMapStyleOptions = new CustomMapStyleOptions();
        customMapStyleOptions.setEnable(true);
        customMapStyleOptions.setStyleData(getAssetsStyle(context));
        customMapStyleOptions.setStyleExtraData(getAssetsStyleExtra(context));
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

        return aMap;
    }

    /**
     * 坐标逆编
     * @param latLng
     * @param geocodeSearch
     */
    public void getAddress(LatLng latLng, GeocodeSearch geocodeSearch){
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude,latLng.longitude);
        RegeocodeQuery regeocodeQuery = new RegeocodeQuery(latLonPoint,200f, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(regeocodeQuery);
    }

    /**
     * @param  data
     * @return List<LatLng>
     * @description  转换流向图的坐标
     * @author Suzy.Mo
     * @time
     */

    public List<LatLng> readLatLng(List<FlowAllData> data) {
        List<LatLng> points = new ArrayList<LatLng>();
        for (int i = 0; i < data.size(); i++) {
            points.add(new LatLng(data.get(i).getOffLatitude(), data.get(i).getOffLongitude()));
            points.add(new LatLng(data.get(i).getOnLatitude(), data.get(i).getOnLongitude()));
        }
        Log.d("Flow_TAG", "readLatLng: flowAllData-->LatLng转换完成");
        return points;

    }
    /**
     * @param  list aMap
     * @return Polyline
     * @description  画全部流向图的曲线
     * @author Suzy.Mo
     * @time
     */

    public Polyline setFlowAllLineTest(List<LatLng> list,AMap aMap) {

        // 设置当前地图级别为4
        //aMap.moveCamera(CameraUpdateFactory.zoomTo(4));
        // 设置地图底图文字的z轴指数，默认为0
        //aMap.setMapTextZIndex(2);

        // 绘制流向图
        Polyline mPolyline = aMap.addPolyline((new PolylineOptions())//.setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.custtexture)))
                .addAll(list)
                .width(2)
                .color(Color.parseColor("#03DAC5")));
        return mPolyline;
    }


    public List<Polyline> setFlowAllLine(List<LatLng> list, AMap aMap) {
        Log.d("Flow_TAG","setFlowAllLine2: LatLng-->List<Polyline>进入");
        List<Polyline> polylines = new ArrayList<>();
        // 设置当前地图级别为4
        //aMap.moveCamera(CameraUpdateFactory.zoomTo(4));
        // 设置地图底图文字的z轴指数，默认为0
        //aMap.setMapTextZIndex(2);

        // 绘制流向图
        for (int i = 0; i < list.size() ; i += 2){
            Polyline mPolyline = aMap.addPolyline((new PolylineOptions())//.setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.custtexture)))
                    .add(list.get(i),list.get(i+1))
                    .width(2)
                    .color(Color.parseColor("#03DAC5")));
            polylines.add(mPolyline);
        }
        Log.d("Flow_TAG","setFlowAllLine2: LatLng-->List<Polyline>转换完成");
        return polylines;
    }


    /**
     * @param  data
     * @return List<LatLng>
     * @description  转换流向图的坐标
     * @author Suzy.Mo
     * @time
     */

    public  List<LatLng> getAllLineLatLng(FlowMainDataLine.DataBean data) {
        Log.d("Flow_TAG","getAllLineLatLng: flowAllData-->LatLng");
        List<LatLng> points = new ArrayList<LatLng>();
        for (int i = 0; i < data.getLocation().size(); i ++) {
            points.add(new LatLng(data.getLocation().get(i).getLatitude(),data.getLocation().get(i).getLongitude()));
        }
        Log.d("Flow_TAG","getAllLineLatLng: flowAllData-->LatLng转换完成");
        return points;
    }
    public List<Polyline> setFlowMainLines(FlowMainDataLine lineList, AMap mAMap){
        List<Polyline> polylines = new ArrayList<>();

        //转成坐标形式
        List<List<LatLng>> list = new ArrayList<>();
        for(int i = 0; i < lineList.getData().size();i++){
            list.add(getAllLineLatLng(lineList.getData().get(i)));
        }


        List<Integer> colorList = new ArrayList<Integer>();
        List<BitmapDescriptor> bitmapDescriptors = new ArrayList<BitmapDescriptor>();

        int[] colors = new int[]{Color.argb(255, 0, 255, 0),Color.argb(255, 255, 255, 0),Color.argb(255, 255, 0, 0)};

        //用一个数组来存放纹理
        List<BitmapDescriptor> textureList = new ArrayList<BitmapDescriptor>();
        textureList.add(BitmapDescriptorFactory.fromResource(R.mipmap.flow_main_green));
        textureList.add(BitmapDescriptorFactory.fromResource(R.mipmap.flow_main_blue));
        textureList.add(BitmapDescriptorFactory.fromResource(R.mipmap.flow_main_yellow));
        textureList.add(BitmapDescriptorFactory.fromResource(R.mipmap.flow_main_red));
        textureList.add(BitmapDescriptorFactory.fromResource(R.mipmap.flow_main_zi));
        textureList.add(BitmapDescriptorFactory.fromResource(R.mipmap.flow_main_orign));
        textureList.add(BitmapDescriptorFactory.fromResource(R.mipmap.flow_main_shenlan));

        List<Integer> texIndexList = new ArrayList<Integer>();
        texIndexList.add(0);//对应上面的第0个纹理
        texIndexList.add(1);
        texIndexList.add(2);

        Random random = new Random();
        for (int i = 0; i < lineList.getData().size(); i++) {
            colorList.add(colors[random.nextInt(3)]);
            bitmapDescriptors.add(textureList.get(0));

        }

        for(int i = 0 ; i < lineList.getData().size() ; i++){
            polylines.add(setFlowMainEachLine(list.get(i),mAMap,textureList.get(i)));
        }

        return polylines;
    }
    /**
     * @param list mAMap
     * @return void
     * @description  画主流向每一条线的函数
     * @author Suzy.Mo
     * @time
     */

    public Polyline setFlowMainEachLine(List<LatLng> list, AMap mAMap, BitmapDescriptor bitmapDescriptor) {

        Polyline mPolyline =  mAMap.addPolyline(new PolylineOptions().setCustomTexture(bitmapDescriptor) //setCustomTextureList(bitmapDescriptors)
//				.setCustomTextureIndex(texIndexList)
                .addAll(list)
                .useGradient(true)
                .width(18));

//        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//        builder.include(list.get(0));
//        builder.include(list.get(list.size() - 2));
//
//        mAMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));

        return mPolyline;
    }

    /**
     * 转换为图标
     */
    public List<Marker> setCarTrafficMarkers(CarTrafficMarkBean data,AMap mAMap){
        List<Marker> markerList = new ArrayList<>();
        for(int i = 0 ; i <data.getData().size() ; i++){
            Marker marker1 = mAMap.addMarker(new MarkerOptions()
                    .position(new LatLng(data.getData().get(i).getLatitude(),data.getData().get(i).getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_marker_logo)));
            Log.d("TAG",marker1.getPosition().latitude+"\n"+marker1.getPosition().longitude);
        }
        return markerList;
    }

    public CarTrafficMarkBean testTrafficMarkers(){
        CarTrafficMarkBean data = new CarTrafficMarkBean();
        CarTrafficMarkBean.DataBean dataBean = new CarTrafficMarkBean.DataBean();
        dataBean.setLatitude(22.860);
        dataBean.setLongitude(113.399);
        CarTrafficMarkBean.DataBean dataBean1 = new CarTrafficMarkBean.DataBean();
        dataBean1.setLatitude( 23.197);
        dataBean1.setLongitude(113.4165);
        CarTrafficMarkBean.DataBean dataBean2 = new CarTrafficMarkBean.DataBean();
        dataBean2.setLatitude( 23.3674);
        dataBean2.setLongitude(113.252);
        List<CarTrafficMarkBean.DataBean> testData = new ArrayList<>();
        testData.add(dataBean);
        testData.add(dataBean1);
        testData.add(dataBean2);
        data.setData(testData);
        return data;
    }

    public CarLineChartBean testChartLine(){
        CarLineChartBean testData = new CarLineChartBean();
        CarLineChartBean.DataBean dataBean = new CarLineChartBean.DataBean();
        List<CarLineChartBean.DataBean.FeatureBean> featureBeans = new ArrayList<>();
        for(int i = 0; i < 24 ; i++){
            CarLineChartBean.DataBean.FeatureBean bean = new CarLineChartBean.DataBean.FeatureBean();
            bean.setNumber((double)60*i);
            featureBeans.add(bean);
        }
        dataBean.setFeature(featureBeans);

        List<CarLineChartBean.DataBean.WeekendBean> weekendBeans = new ArrayList<>();
        for(int i = 0; i < 24 ; i++){
            CarLineChartBean.DataBean.WeekendBean bean = new CarLineChartBean.DataBean.WeekendBean();
            bean.setNumber((double)50*i+200);
            weekendBeans.add(bean);
        }
        dataBean.setWeekend(weekendBeans);

        List<CarLineChartBean.DataBean.WorkdayBean> workdayBeans = new ArrayList<>();
        for(int i = 0; i < 24 ; i++){
            CarLineChartBean.DataBean.WorkdayBean bean = new CarLineChartBean.DataBean.WorkdayBean();
            bean.setNumber((double)100*i);
            workdayBeans.add(bean);
        }
        dataBean.setWorkday(workdayBeans);

        List<CarLineChartBean.DataBean> beans = new ArrayList<>();
        beans.add(dataBean);
        testData.setData(beans);
        return testData;
    }
}

