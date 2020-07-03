package com.shmedo.mcloudapp.maps.callback;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.TextureMapView;
import com.shmedo.mcloudapp.maps.ui.activity.MapActivity;
import com.shmedo.mcloudapp.maps.util.SensorEventHelper;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/3 <br/>
 * 描述：    TODO
 */
public class GaoDeLocationSourceHelper implements AMapLocationListener, LocationSource {
    private MapActivity mapActivity;

    private TextureMapView mapView;
    private AMap aMap;

    private SensorEventHelper mSensorHelper;


    public GaoDeLocationSourceHelper(MapActivity mapActivity) {
        this.mapActivity = mapActivity;
        mapView = mapActivity.mMapView;
        aMap = mapView.getMap();
        initView();
        registerListeners();
    }

    private void initView() {
        mSensorHelper = new SensorEventHelper(mapActivity);
        mSensorHelper.registerSensorListener();
    }

    private void registerListeners() {
        // 对amap添加单击地图事件监听器

    }

    public void onResume() {
        if (null == mSensorHelper) {
            mSensorHelper = new SensorEventHelper(mapActivity);
            //重新注册
            mSensorHelper.registerSensorListener();
        }
    }

    public void onPause() {
        deactivate();
        if (mSensorHelper != null) {
            mSensorHelper.unRegisterSensorListener();
            mSensorHelper.setCurrentMarker(null);
            mSensorHelper = null;
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {

    }

    @Override
    public void deactivate() {

    }
}
