package com.shmedo.mcloudapp.maps.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.util.DensityUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class RouteDistanceActivity extends BaseActivity implements AMap.OnMapClickListener {
    @BindView(R.id.map)
    MapView mapView;

    private AMap aMap;

    private LatLng latLngStart;
    private LatLng latLngEnd;
    private List<LatLng> latLonPoints = new ArrayList<LatLng>();
    private Marker mLastMarker;//


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, RouteDistanceActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_route_distance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.setOnMapClickListener(this);// 对amap添加单击地图事件监听器

    }


    @OnClick({R.id.back, R.id.tv_clear})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                break;

            case R.id.tv_clear:
                aMap.clear();
                latLonPoints.clear();
                break;

            default:
                break;
        }
    }


    @Override
    public void onMapClick(LatLng latLng) {
        processAddMarkers(latLng);
        processAddPolylines();
    }

    /**
     * 绘制点标记
     *
     * @param latLng
     */
    private void processAddMarkers(LatLng latLng) {
        latLonPoints.add(latLng);
        if (latLonPoints.size() == 1) {
            MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.polyline_start))
                    .position(latLng)
                    .draggable(false);
             aMap.addMarker(markerOption);
            return;
        }

        int height = DensityUtil.Dp2Px(this,12);
        int width =  DensityUtil.Dp2Px(this,12);
        if (mLastMarker != null) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.measure_point);
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), width, height, false);
            mLastMarker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        }

        BitmapDrawable bitmapDrawable = (BitmapDrawable)getResources().getDrawable(  R.drawable.measure_point_red);
        Bitmap smallMarker = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), width, height, false);
        MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                .position(latLng)
                .draggable(false);
        mLastMarker = aMap.addMarker(markerOption);
    }

    /**
     * 绘制线
     */
    private void processAddPolylines() {
        if (latLonPoints == null || latLonPoints.size() == 0) {
            return;
        }

        for (int i = 0; i < latLonPoints.size() - 1; i++) {
            latLngStart = latLonPoints.get(i);
            latLngEnd = latLonPoints.get(i + 1);
            PolylineOptions polylineOptions = new PolylineOptions().add(latLngStart, latLngEnd).width(15).color(Color.BLUE);
            aMap.addPolyline(polylineOptions);
        }
    }


    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

}
