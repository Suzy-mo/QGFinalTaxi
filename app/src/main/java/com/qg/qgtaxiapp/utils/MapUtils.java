package com.qg.qgtaxiapp.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.district.DistrictItem;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.application.MyApplication;
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
     * @param  data
     * @return List<LatLng>
     * @description  转换流向图的坐标
     * @author Suzy.Mo
     * @time
     */

    public  List<LatLng> readLatLng(List<FlowAllData.DataBean> data) {
        List<LatLng> points = new ArrayList<LatLng>();
        for (int i = 0; i < data.size(); i ++) {
            points.add(new LatLng(data.get(i).getLatitude(),data.get(i).getLongitude()));
        }
        return points;
    }

    /**
     * @param  list aMap
     * @return Polyline
     * @description  画全部流向图的曲线
     * @author Suzy.Mo
     * @time
     */

    public Polyline setFlowAllLine(List<LatLng> list,AMap aMap) {

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

    /**
     * @param list mAMap
     * @return void
     * @description  画主流向的函数
     * @author Suzy.Mo
     * @time
     */

    public Polyline setFlowMainLine(List<LatLng> list, AMap mAMap) {
        List<Integer> colorList = new ArrayList<Integer>();
        List<BitmapDescriptor> bitmapDescriptors = new ArrayList<BitmapDescriptor>();

        int[] colors = new int[]{Color.argb(255, 0, 255, 0),Color.argb(255, 255, 255, 0),Color.argb(255, 255, 0, 0)};

        //用一个数组来存放纹理
        List<BitmapDescriptor> textureList = new ArrayList<BitmapDescriptor>();
        textureList.add(BitmapDescriptorFactory.fromResource(R.mipmap.custtexture));

        List<Integer> texIndexList = new ArrayList<Integer>();
        texIndexList.add(0);//对应上面的第0个纹理
        texIndexList.add(1);
        texIndexList.add(2);

        Random random = new Random();
        for (int i = 0; i < list.size(); i++) {
            colorList.add(colors[random.nextInt(3)]);
            bitmapDescriptors.add(textureList.get(0));

        }

        Polyline mPolyline =  mAMap.addPolyline(new PolylineOptions().setCustomTexture(BitmapDescriptorFactory.fromResource(R.mipmap.custtexture)) //setCustomTextureList(bitmapDescriptors)
//				.setCustomTextureIndex(texIndexList)
                .addAll(list)
                .useGradient(true)
                .width(18));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(list.get(0));
        builder.include(list.get(list.size() - 2));

        mAMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));

        return mPolyline;
    }

    public List<FlowAllData.DataBean> setAllData(){
        List<FlowAllData.DataBean> mData = new ArrayList<>();
        FlowAllData.DataBean dataBean = new FlowAllData.DataBean();
        dataBean.setLatitude(23.131467);dataBean.setLatitude(113.284837);mData.add(dataBean);
        dataBean.setLatitude(23.138388);dataBean.setLatitude(113.286461);mData.add(dataBean);
        dataBean.setLatitude(23.140177);dataBean.setLatitude(113.280282);mData.add(dataBean);
        dataBean.setLatitude(23.142006);dataBean.setLatitude(113.282005);mData.add(dataBean);
        dataBean.setLatitude(23.141077);dataBean.setLatitude(113.285651);mData.add(dataBean);
        dataBean.setLatitude(23.129552);dataBean.setLatitude(113.284006);mData.add(dataBean);
        dataBean.setLatitude(23.136475);dataBean.setLatitude(113.289954);mData.add(dataBean);
        dataBean.setLatitude(23.130344);dataBean.setLatitude(113.296271);mData.add(dataBean);
        dataBean.setLatitude( 23.132371);dataBean.setLatitude(113.292864);mData.add(dataBean);
        dataBean.setLatitude(23.116095);dataBean.setLatitude(113.300873);mData.add(dataBean);
        dataBean.setLatitude(23.11092);dataBean.setLatitude(113.296599);mData.add(dataBean);
        dataBean.setLatitude( 23.13906);dataBean.setLatitude(113.296599);mData.add(dataBean);
        dataBean.setLatitude(23.137483);dataBean.setLatitude(113.298799);mData.add(dataBean);
        dataBean.setLatitude(23.137361);dataBean.setLatitude(113.295664);mData.add(dataBean);
        dataBean.setLatitude(23.131446);dataBean.setLatitude(23.137361);mData.add(dataBean);
        dataBean.setLatitude( 23.130885);dataBean.setLatitude(113.29107);mData.add(dataBean);
        dataBean.setLatitude(23.136245);dataBean.setLatitude(113.299721);mData.add(dataBean);
        dataBean.setLatitude(23.132397);dataBean.setLatitude(113.282472);mData.add(dataBean);

        return mData;
    }
}

