package com.shmedo.mcloudapp.deviceconfig.ui.activity.usb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.Inclinometer_debug_box.CollectionConfigurationFragment;

public class CollectionConfigurationActivity extends BaseConfigFragmentContainerActivity {

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, CollectionConfigurationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("采集配置");
    }

    @Override
    protected Fragment initFragment() {
        fragment = CollectionConfigurationFragment.newInstance();
        return fragment;
    }
}
