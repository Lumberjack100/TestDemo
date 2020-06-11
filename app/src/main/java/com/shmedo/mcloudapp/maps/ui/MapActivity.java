package com.shmedo.mcloudapp.maps.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;

public class MapActivity extends BaseActivity {

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


}
