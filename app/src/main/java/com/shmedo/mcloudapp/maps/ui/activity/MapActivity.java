package com.shmedo.mcloudapp.maps.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.amap.api.maps.TextureMapView;
import com.shmedo.mcloudapp.R;

import butterknife.BindView;

public class MapActivity extends CheckMapNeedPermissionsActivity {
    @BindView(R.id.map)
    TextureMapView mMapView;


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, MapActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
//        if (null != mLocationClient) {
//            mLocationClient.stopLocation();
//            mLocationClient.onDestroy();
//            mLocationClient = null;
//        }
    }






    @Override
    protected void doOnPermissionGranted() {
    }
}
