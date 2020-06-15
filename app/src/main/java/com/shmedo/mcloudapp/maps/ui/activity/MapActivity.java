package com.shmedo.mcloudapp.maps.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.amap.api.maps.MapView;
import com.shmedo.mcloudapp.R;

import butterknife.BindView;

public class MapActivity extends CheckMapNeedPermissionsActivity {
    @BindView(R.id.map)
    MapView mMapView;


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
    }



    @Override
    protected void doOnPermissionGranted() {

    }
}
