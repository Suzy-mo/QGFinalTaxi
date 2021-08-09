package com.qg.qgtaxiapp.utils;

import android.graphics.Color;

import com.amap.api.maps.model.Gradient;
import com.amap.api.maps.model.HeatmapTileProvider;
import com.amap.api.maps.model.LatLng;

import java.util.Arrays;

/**
 * @author: Hx
 * @date: 2021年08月09日 16:43
 */
public class MapUtils {

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
    public HeatmapTileProvider initBuildHeatmapTileProvider(LatLng[] latlngs) {
        HeatmapTileProvider.Builder builder = new HeatmapTileProvider.Builder();
        builder.data(Arrays.asList(latlngs));
        builder.gradient(ALT_HEATMAP_GRADIENT);
        HeatmapTileProvider heatmapTileProvider = builder.build();
        return heatmapTileProvider;
    }



    //生成热力点坐标列表
    public LatLng[] initHeatMapData(double latitude, double longitude){

        LatLng[] latlngs = new LatLng[500];
        double x = latitude;
        double y = longitude;

        for (int i = 0; i < 500; i++) {
            double x_ = 0;
            double y_ = 0;
            x_ = Math.random() * 0.5 - 0.25;
            y_ = Math.random() * 0.5 - 0.25;
            latlngs[i] = new LatLng(x + x_, y + y_);
        }
        return latlngs;
    }
}
