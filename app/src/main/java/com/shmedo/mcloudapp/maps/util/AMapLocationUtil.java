package com.shmedo.mcloudapp.maps.util;

import com.amap.api.location.AMapLocationClientOption;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/17 <br/>
 * 描述：    TODO #gh#
 */
public class AMapLocationUtil {
    private static class InstanceHolder {
        private static final AMapLocationUtil INSTANCE = new AMapLocationUtil();
    }

    public static AMapLocationUtil getInstance() {
        return InstanceHolder.INSTANCE;
    }


    public AMapLocationClientOption getAMapLocationClientOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        mOption.setSensorEnable(true);//是否开启设备传感器，当设置为true时，网络定位可以返回海拔、角度和速度。
        return mOption;
    }
}
