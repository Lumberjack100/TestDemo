package com.shmedo.mcloudapp.maps.callback;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;

import com.amap.api.maps.AMap;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DistanceItem;
import com.amap.api.services.route.DistanceResult;
import com.amap.api.services.route.DistanceSearch;
import com.dragon.core.util.DensityUtil;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.maps.model.MapMode;
import com.shmedo.mcloudapp.maps.ui.activity.MapActivity;
import com.shmedo.mcloudapp.maps.ui.view.DistanceToolbarView;
import com.shmedo.mcloudapp.maps.util.MapErrorUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/3 <br/>
 * 描述：    高德地图测量直线距离辅助类
 */
public class GaoDeCaculateDistanceHelper implements AMap.OnMapClickListener, DistanceSearch.OnDistanceSearchListener, DistanceToolbarView.OnDistanceToolbarViewClickListener {

    private MapActivity mapActivity;
    private DistanceToolbarView mDistanceToolbarView;

    private TextureMapView mapView;
    private AMap aMap;

    private int markerHeight;
    private int markerWidth;
    private List<LatLng> latLngList = new ArrayList<>();
    private List<Marker> markerList = new ArrayList<>();
    private List<Polyline> polylineList = new ArrayList<>();

    public GaoDeCaculateDistanceHelper(MapActivity activity) {
        mapActivity = activity;
        mapView = activity.mMapView;
        aMap = mapView.getMap();
        mDistanceToolbarView = activity.mDistanceToolbarView;
        initView();
        registerListeners();
    }

    private void initView() {
        initDistanceToolView();

    }

    private void registerListeners() {
        // 对amap添加单击地图事件监听器
        aMap.setOnMapClickListener(this);
        mDistanceToolbarView.setOnDistanceToolbarViewClickListener(this);
    }

    private void initDistanceToolView() {
        markerHeight = DensityUtil.Dp2Px(mapActivity, 12);
        markerWidth = DensityUtil.Dp2Px(mapActivity, 12);
        mDistanceToolbarView.mIvRemoveMarker.setEnabled(false);
        mDistanceToolbarView.mIvClearMarkers.setEnabled(false);
        mDistanceToolbarView.mTvDistance.setText("0米");
    }

    /**
     * 点击测距返回箭头
     */
    @Override
    public void onCancelDistanceClick() {
        mapActivity.mMapMode = MapMode.NORMAL;
        onClearMarkersClick();
        mapActivity.setDistanceToolbarViewVisibility(false);
    }

    /**
     * 点击移除测距点标记
     */
    @Override
    public void onRemoveMarkerClick() {
        if (markerList.size() == 0 || latLngList.size() == 0) {
            return;
        }
        latLngList.remove(latLngList.size() - 1);

        Marker mLastMarker = markerList.get(markerList.size() - 1);
        markerList.remove(mLastMarker);
        mLastMarker.destroy();

        if (polylineList.size() > 0) {
            Polyline polyline = polylineList.get(polylineList.size() - 1);
            polylineList.remove(polyline);
            polyline.remove();
        }

        if (markerList.size() > 0) {
            mLastMarker = markerList.get(markerList.size() - 1);
            BitmapDrawable bitmapDrawable = (BitmapDrawable) mapActivity.getResources().getDrawable(R.drawable.measure_point_red);
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), markerWidth, markerHeight, false);
            mLastMarker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        }

        if (latLngList.size() >= 2) {
            calculateRouteDistance();
        } else {
            mDistanceToolbarView.mTvDistance.setText("0米");
        }
        if (markerList.size() == 0) {
            mDistanceToolbarView.mIvRemoveMarker.setEnabled(false);
            mDistanceToolbarView.mIvClearMarkers.setEnabled(false);
        }
    }

    /**
     * 点击清空所有测距点标记
     */
    @Override
    public void onClearMarkersClick() {
        mDistanceToolbarView.mIvRemoveMarker.setEnabled(false);
        mDistanceToolbarView.mIvClearMarkers.setEnabled(false);
        mDistanceToolbarView.mTvDistance.setText("0米");
        latLngList.clear();
        markerList.clear();
        polylineList.clear();
        aMap.clear();
        aMap.addMarker(mapActivity.mLocationMarker.getOptions());
        mapActivity.addCircle();
    }


    /**
     * 地图点击事件
     *
     * @param latLng
     */
    @Override
    public void onMapClick(LatLng latLng) {
        //打开测距模式下
        if (mapActivity.mMapMode == MapMode.CACULATE_DISTANCE) {
            addMarkersForDistance(latLng);
            if (latLngList.size() >= 2) {
                addPolylinesForDistance();
                calculateRouteDistance();
            }
        }
    }

    /**
     * 绘制点标记
     *
     * @param latLng
     */
    private void addMarkersForDistance(LatLng latLng) {
        mDistanceToolbarView.mIvRemoveMarker.setEnabled(true);
        mDistanceToolbarView.mIvClearMarkers.setEnabled(true);
        latLngList.add(latLng);

        if (latLngList.size() == 1) {
            MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.polyline_start))
                    .position(latLng)
                    .draggable(false);
            Marker marker = aMap.addMarker(markerOption);
            markerList.add(marker);
            return;
        }

        if (markerList.size() >= 2) {
            Marker mLastMarker = markerList.get(markerList.size() - 1);
            BitmapDrawable bitmapDrawable = (BitmapDrawable) mapActivity.getResources().getDrawable(R.drawable.measure_point);
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), markerWidth, markerHeight, false);
            mLastMarker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        }

        BitmapDrawable bitmapDrawable = (BitmapDrawable) mapActivity.getResources().getDrawable(R.drawable.measure_point_red);
        Bitmap smallMarker = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), markerWidth, markerHeight, false);
        MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                .position(latLng);
        Marker marker = aMap.addMarker(markerOption);
        markerList.add(marker);
    }

    /**
     * 绘制线
     */
    private void addPolylinesForDistance() {
        LatLng latLngStart = latLngList.get(latLngList.size() - 2);
        LatLng latLngEnd = latLngList.get(latLngList.size() - 1);
        PolylineOptions polylineOptions = new PolylineOptions().add(latLngStart, latLngEnd).width(15).color(Color.BLUE);
        Polyline polyline = aMap.addPolyline(polylineOptions);
        polylineList.add(polyline);
    }

    /**
     * 开始搜索路径规划方案
     */
    private void calculateRouteDistance() {
        List<LatLonPoint> latLonPoints = new ArrayList<>();
        for (int i = 0; i < latLngList.size() - 1; i++) {
            LatLng latLng = latLngList.get(i);
            latLonPoints.add(new LatLonPoint(latLng.latitude, latLng.longitude));
        }
        LatLonPoint dest = new LatLonPoint(latLngList.get(latLngList.size() - 1).latitude, latLngList.get(latLngList.size() - 1).longitude);

        DistanceSearch distanceSearch = new DistanceSearch(mapActivity);
        distanceSearch.setDistanceSearchListener(this);
        DistanceSearch.DistanceQuery distanceQuery = new DistanceSearch.DistanceQuery();
        distanceQuery.setOrigins(latLonPoints);
        distanceQuery.setDestination(dest);
        distanceQuery.setType(DistanceSearch.TYPE_DISTANCE);

        distanceSearch.calculateRouteDistanceAsyn(distanceQuery);
    }

    @Override
    public void onDistanceSearched(DistanceResult distanceResult, int errorCode) {
        if (errorCode != AMapException.CODE_AMAP_SUCCESS) {
            ToastUtils.show(MapErrorUtil.getErrorMsg(errorCode));
            return;
        }

        float totalDistance = 0;
        List<DistanceItem> distanceItems = distanceResult.getDistanceResults();
        for (DistanceItem item : distanceItems) {
            totalDistance += item.getDistance();
        }
        showDistance(totalDistance);
    }

    private void showDistance(float distance) {
        if (distance > 1000) {
            DecimalFormat decimalFormat = new DecimalFormat(".0");//构造方法的字符格式这里如果小数不足2位,会以0补足.
            String p = decimalFormat.format(distance / 1000);//format 返回的是字符串
            mDistanceToolbarView.mTvDistance.setText(String.format("%s公里", p));
        } else {
            mDistanceToolbarView.mTvDistance.setText(String.format("%s米", distance));
        }
    }
}
