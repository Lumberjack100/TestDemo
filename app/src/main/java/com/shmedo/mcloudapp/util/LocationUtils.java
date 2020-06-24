package com.shmedo.mcloudapp.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.SyncPositionBean;
import com.shmedo.mcloudapp.util.permission.RuntimeRationale;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 创建者:   dpc
 * 创建时间:  2019-12-25
 * 描述：    TODO
 */
public class LocationUtils {
    private static final int REQUEST_CODE_SETTING = 1;

    @SuppressLint("StaticFieldLeak")
    private static AMapLocationClient mLocationClient;

    private AMapLocationClientOption mLocationOption = null;

    /**
     * 定位需要进行检测的权限数组
     */
    private String[] locationNeedPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static class LocationHolder {
        private static final LocationUtils INSTANCE = new LocationUtils();
    }

    public static LocationUtils getInstance() {

        return LocationHolder.INSTANCE;
    }


    public void getPositionPermission(Activity activity) {
        if (Build.VERSION.SDK_INT > 28 && MCloudApp.getContext().getApplicationInfo().targetSdkVersion > 28) {
            locationNeedPermissions = new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Permission.ACCESS_BACKGROUND_LOCATION
            };
        }

        AndPermission.with(activity)
                .runtime()
                .permission(locationNeedPermissions)
                .rationale(new RuntimeRationale())
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        startLocalService();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                        if (AndPermission.hasAlwaysDeniedPermission(activity, permissions)) {
                            showSettingDialog(activity, permissions);
                        }
                    }
                })
                .start();
    }

    public void startLocalService() {
        //初始化定位
        mLocationClient = new AMapLocationClient(MCloudApp.getContext());
        //设置定位回调监听
        mLocationOption = getDefaultOption();
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.setLocationListener(location -> {
            if (null != location) {
                if (location.getErrorCode() == 0) {
                    //latitude=31.080949#longitude=121.526511#
                    // province=上海市#coordType=GCJ02#
                    // city=上海市#district=闵行区#cityCode=021#adCode=310112#
                    // address=上海市闵行区万里路1188号靠近浦江智谷#country=中国#
                    // road=万里路#poiName=浦江智谷#street=万里路#streetNum=1188号#
                    // aoiName=浦江智谷#poiid=#floor=#errorCode=0#errorInfo=success#
                    // locationDetail=#csid:95d47daae7dc4ee3b2af8e0ec17be6ce#
                    // description=在浦江智谷附近#locationType=5
                    Timber.i("定位成功===" + location.toString());
                    SyncPositionBean bean = new SyncPositionBean();
                    bean.setLatitude(location.getLatitude());
                    bean.setLongitude(location.getLongitude());
                    bean.setAddress(location.getAddress());
                    bean.setType("location");
                    EventBus.getDefault().post(bean);
                    stopLocalService();
                } else {
                    ToastUtils.show("定位失败");
                    Timber.i("定位失败\n错误码：" + location.getErrorCode()
                            + "\n错误信息:" + location.getErrorInfo()
                            + "\n错误描述:" + location.getLocationDetail());
                }
            } else {
                ToastUtils.show("定位失败，loc is null");
            }
        });
        mLocationClient.startLocation();
    }

    public void stopLocalService() {
        if (null != mLocationClient) {
            mLocationClient.onDestroy();
            mLocationClient.stopLocation();
            mLocationClient = null;
            mLocationOption = null;
        }
    }

    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        mOption.setGeoLanguage(AMapLocationClientOption.GeoLanguage.DEFAULT);//可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
        return mOption;
    }


    public void showSettingDialog(Activity context, final List<String> permissions) {
        List<String> permissionNames = Permission.transformText(context, permissions);
        @SuppressLint({"StringFormatInvalid", "LocalSuppress"})
        String message = context.getString(R.string.message_permission_always_failed, TextUtils.join("\n", permissionNames));

        new AlertDialog.Builder(context).setCancelable(false)
                .setTitle("提示")
                .setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AndPermission.with(context).runtime().setting().start(REQUEST_CODE_SETTING);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

}
