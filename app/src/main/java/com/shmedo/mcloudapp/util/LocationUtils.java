package com.shmedo.mcloudapp.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.os.Build;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.hjq.toast.ToastUtils;
import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.SyncPositionBean;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;
import com.yanzhenjie.permission.runtime.Permission;

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

    private final UnPeekLiveData<SyncPositionBean> syncPositionBeanLiveData = new UnPeekLiveData<>();


    public static LocationUtils getInstance() {
        return LocationHolder.INSTANCE;
    }


    public ProtectedUnPeekLiveData<SyncPositionBean> getSyncPositionBean() {
        return syncPositionBeanLiveData;
    }


    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @return true 表示开启
     */
    public boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) MCloudApp.getContext().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }

        return false;
    }

    public void getPositionPermission(Activity activity) {
        if (Build.VERSION.SDK_INT > 28 && MCloudApp.getContext().getApplicationInfo().targetSdkVersion > 28) {
            locationNeedPermissions = new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Permission.ACCESS_BACKGROUND_LOCATION
            };
        }

        XPermissionUtils.requestPermissionsResult(activity, 200, locationNeedPermissions,
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        startLocalService();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        boolean allNeverAskAgain = XPermissionUtils.isAllNeverAskAgain(activity, deniedPermissions);
                        // 所有的权限都被勾上不再询问时，跳转到应用设置界面，引导用户手动打开权限
                        if (allNeverAskAgain) {
                            XPermissionUtils.showRefusePermissionDialog(activity, GlobalUtil.getString(R.string.message_permission_location_rationale));
                        } else {
                            ToastUtils.show(GlobalUtil.getString(R.string.message_permission_location_denied));
                        }
                    }
                });
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
                    Timber.i("定位成功\n星数: " + location.getSatellites());
                    //卫星信号强
                    if (location.getGpsAccuracyStatus() == AMapLocation.GPS_ACCURACY_GOOD) {
                        SyncPositionBean bean = new SyncPositionBean();
                        bean.setLatitude(location.getLatitude());
                        bean.setLongitude(location.getLongitude());
                        bean.setAddress(location.getAddress());
                        bean.setType("location");
                        syncPositionBeanLiveData.postValue(bean);

                    } else {
                        ToastUtils.show("卫星定位信号弱");
                    }

                } else {
                    Timber.i("定位失败\n错误码：" + location.getErrorCode()
                            + "\n错误信息:" + location.getErrorInfo()
                            + "\n错误描述:" + location.getLocationDetail());
                    ToastUtils.show("定位失败");
                }
            } else {
                Timber.i("定位失败，location is null");
                ToastUtils.show("定位失败");
            }
            stopLocalService();
        });
        mLocationClient.startLocation();
    }

    public void stopLocalService() {
        if (null != mLocationClient) {
            mLocationClient.onDestroy();
            mLocationClient = null;
            mLocationOption = null;
        }
    }

    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
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

}
