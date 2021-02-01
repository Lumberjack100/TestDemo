package com.shmedo.mcloudapp.maps.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.shmedo.core.util.GlobalUtil;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.util.LocationUtils;
import com.shmedo.mcloudapp.util.permission.PermissionHelper;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/15 <br/>
 * 描述：    地图模块权限申请处理基类
 */
public abstract class CheckMapNeedPermissionsActivity extends BaseActivity {

    /**
     * 定位需要进行检测的权限数组
     */
    protected String[] needPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    protected boolean isNeedCheck = true;//判断是否需要检测权限，防止不停的弹框

    protected abstract void doOnPermissionGranted(int requestCode);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > 28 && getApplicationContext().getApplicationInfo().targetSdkVersion > 28) {
            needPermissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            };
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (Build.VERSION.SDK_INT >= 23) {
//            if (isNeedCheck) {
//                checkPermissionForGPS();
//            }
//        }
//    }

    /**
     * 检查是否打开系统位置服务，如果开启了，接着检查是否授予 APP 定位权限
     */
    public void checkPermissionForGPS(int requestCode) {
        if (LocationUtils.getInstance().isGpsEnabled()) {
            checkPermissionForLocation(requestCode);

        } else {
            PermissionHelper.showGPSSettingDialog(this);
        }
    }


    /**
     * 检查是否授予 APP 定位权限
     */
    public void checkPermissionForLocation(int requestCode) {
        XPermissionUtils.requestPermissionsResult(CheckMapNeedPermissionsActivity.this, XPermissionUtils.REQUEST_CODE_OPEN_APPLICATION_SETTING, needPermissions, new XPermissionUtils.OnPermissionListener() {
            @Override
            public void onPermissionGranted() {
                doOnPermissionGranted(requestCode);
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                isNeedCheck = false;

                boolean allNeverAskAgain = XPermissionUtils.isAllNeverAskAgain(CheckMapNeedPermissionsActivity.this, deniedPermissions);
                // 所有的权限都被勾上不再询问时，跳转到应用设置界面，引导用户手动打开权限
                if (allNeverAskAgain) {
                    XPermissionUtils.showRefusePermissionDialog(CheckMapNeedPermissionsActivity.this, GlobalUtil.getString(R.string.message_permission_location_rationale));
                } else {
                    ToastUtils.show(GlobalUtil.getString(R.string.message_permissions_denied));
                }
            }
        });
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case PermissionHelper.REQUEST_CODE_OPEN_GPS:
//                if (LocationUtils.getInstance().isGpsEnabled()) {
//                    mLocationClient.startLocation();
//                    aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE));
//                }
                break;

            case XPermissionUtils.REQUEST_CODE_OPEN_APPLICATION_SETTING:
//                if (XPermissionUtils.checkPermissions(CheckMapNeedPermissionsActivity.this, needPermissions)) {
//                    mLocationClient.startLocation();
//                    aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE));
//                }
                break;
        }
    }

}
