package com.shmedo.mcloudapp.maps.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;

public class PoiSearchActivity extends BaseActivity {


    public static void startActivityForResult(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, PoiSearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_poi_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initStates();
    }

    /**
     * 沉浸式状态栏
     */
    private void initStates() {
        if (Build.VERSION.SDK_INT > 19 && getApplicationContext().getApplicationInfo().targetSdkVersion > 19) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }
}
