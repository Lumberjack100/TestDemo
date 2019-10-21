package com.shmedo.mcloudapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.model.Extras;
import com.shmedo.mcloudapp.ui.fragment.DeviceFragment;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.tabdevice
 * 文件名:   AllDeviceActivity
 * 创建者:   dpc
 * 创建时间:  2019/3/12 15:00
 * 描述：    tab-设备
 */
public class AllDeviceActivity extends BaseActivity {

    private DeviceFragment deviceFragment;


    /**
     * 说明：启动Activity
     * <p>
     * 注意：这里使用到了Intent的Flag属性singleTop。singleTop模式下，在同一个task中，如果存在该Activity的实例，
     * 并且该Activity实例位于栈顶(即，该Activity位于前端)，则调用startActivity()时，不再创建该Activity的示例；
     * 而仅仅只是调用Activity的onNewIntent()。否则的话，则新建该Activity的实例，并将其置于栈顶。
     * </p>
     */
    public static void startActivity(Context context, String deviceInfo) {
        Intent intent = new Intent(context, AllDeviceActivity.class);
        intent.putExtra(Extras.CUR_DEVICE_NAME, deviceInfo);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, String deviceInfo, String macAddress) {
        Intent intent = new Intent(context, AllDeviceActivity.class);
        intent.putExtra(Extras.CUR_DEVICE_NAME, deviceInfo);
        intent.putExtra(Extras.DEVICE_MAC_ADDRESS, macAddress);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_all_device;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }


    private void initData() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        deviceFragment = new DeviceFragment();
        transaction.replace(R.id.frame_content, deviceFragment);
        transaction.commit();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
