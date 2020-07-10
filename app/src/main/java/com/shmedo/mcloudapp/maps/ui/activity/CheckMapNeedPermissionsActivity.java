package com.shmedo.mcloudapp.maps.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.util.LocationUtils;
import com.shmedo.mcloudapp.util.XPermissionUtils;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/15 <br/>
 * 描述：    地图模块权限申请处理基类
 */
public abstract class CheckMapNeedPermissionsActivity extends BaseActivity {
    public static final int PERMISSION_CODE_GPS = 0x011;

    /**
     * 定位需要进行检测的权限数组
     */
    protected String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    private boolean isNeedCheck = true;//判断是否需要检测权限，防止不停的弹框


    protected abstract void doOnPermissionGranted();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > 28 && getApplicationContext().getApplicationInfo().targetSdkVersion > 28) {
            needPermissions = new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            };
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23) {
            if (isNeedCheck) {
                checkPermissionForGPS();
            }
        }
    }

    /**
     * 检查是否打开系统位置服务，如果开启了，接着检查是否授予 APP 定位权限
     */
    private void checkPermissionForGPS() {
        if (LocationUtils.getInstance().isGpsEnabled()) {
            checkPermissionForLocation();

        } else {
            showGPSSettingDialog();
        }
    }



    /**
     * 检查是否授予 APP 定位权限
     */
    private void checkPermissionForLocation() {
        XPermissionUtils.requestPermissionsResult(CheckMapNeedPermissionsActivity.this, XPermissionUtils.REQUEST_CODE_OPEN_APPLICATION_SETTING, needPermissions, new XPermissionUtils.OnPermissionListener() {
            @Override
            public void onPermissionGranted() {
                doOnPermissionGranted();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                isNeedCheck = false;
                showPermissionSettingDialog();
            }
        });
    }

    private void showPermissionSettingDialog() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(CheckMapNeedPermissionsActivity.this)
                .title("权限申请").content(getResources().getString(R.string.permission_request_location))
                .positiveText("去设置")
                .positiveColor(getResources().getColor(R.color.colorPrimary))
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        XPermissionUtils.startAppSettings(CheckMapNeedPermissionsActivity.this);
                    }
                });

        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    protected void showGPSSettingDialog() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(CheckMapNeedPermissionsActivity.this)
                .title("权限申请").content(getResources().getString(R.string.permission_request_location_hardware))
                .negativeText("暂不开启")
                .positiveText("去设置")
                .negativeColor(getResources().getColor(R.color.gray_797979))
                .positiveColor(getResources().getColor(R.color.colorPrimary))
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, PERMISSION_CODE_GPS);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });

        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        XPermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case PERMISSION_CODE_GPS:
                if (LocationUtils.getInstance().isGpsEnabled()) {
//                    mLocationClient.startLocation();
//                    aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE));
                }
                break;

            case XPermissionUtils.REQUEST_CODE_OPEN_APPLICATION_SETTING:
                if (XPermissionUtils.checkPermissions(CheckMapNeedPermissionsActivity.this, needPermissions)) {
                    //                    mLocationClient.startLocation();
//                    aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE));
                }
                break;
        }
    }

}
