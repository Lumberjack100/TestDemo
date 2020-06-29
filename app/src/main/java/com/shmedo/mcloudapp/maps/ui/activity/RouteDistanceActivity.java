package com.shmedo.mcloudapp.maps.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
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
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.maps.util.MapErrorUtil;
import com.shmedo.mcloudapp.util.DensityUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class RouteDistanceActivity extends BaseActivity implements AMap.OnMapClickListener, DistanceSearch.OnDistanceSearchListener {
    @BindView(R.id.map)
    MapView mapView;

    @BindView(R.id.tv_distance)
    TextView mTvDistance;

    @BindView(R.id.iv_remove_marker)
    ImageView mIvRemoveMarker;

    @BindView(R.id.iv_clear_markers)
    ImageView mIvClearMarkers;

    private AMap aMap;

    private int markerHeight;
    private int markerWidth;
    private List<LatLng> latLngList = new ArrayList<>();
    private List<Marker> markerList = new ArrayList<>();
    private List<Polyline> polylineList = new ArrayList<>();


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, RouteDistanceActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_route_distance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        markerHeight = DensityUtil.Dp2Px(this, 12);
        markerWidth = DensityUtil.Dp2Px(this, 12);
        mIvRemoveMarker.setEnabled(false);
        mIvClearMarkers.setEnabled(false);
        init();
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.setOnMapClickListener(this);// 对amap添加单击地图事件监听器
    }


    @OnClick({R.id.back, R.id.iv_remove_marker, R.id.iv_clear_markers})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                break;

            case R.id.iv_remove_marker:
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
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.measure_point_red);
                    Bitmap smallMarker = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), markerWidth, markerHeight, false);
                    mLastMarker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                }

                if (latLngList.size() >= 2) {
                    searchDistanceResult();
                } else {
                    mTvDistance.setText("0米");
                }
                if (markerList.size() == 0) {
                    mIvRemoveMarker.setEnabled(false);
                    mIvClearMarkers.setEnabled(false);
                }
                break;

            case R.id.iv_clear_markers:
                mIvRemoveMarker.setEnabled(false);
                mIvClearMarkers.setEnabled(false);
                aMap.clear();
                latLngList.clear();
                markerList.clear();
                polylineList.clear();
                mTvDistance.setText("0米");
                break;

            default:
                break;
        }
    }


    @Override
    public void onMapClick(LatLng latLng) {
        processAddMarkers(latLng);
        if (latLngList.size() >= 2) {
            processAddPolylines();
            searchDistanceResult();
        }
    }

    /**
     * 绘制点标记
     *
     * @param latLng
     */
    private void processAddMarkers(LatLng latLng) {
        mIvRemoveMarker.setEnabled(true);
        mIvClearMarkers.setEnabled(true);
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
            BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.measure_point);
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), markerWidth, markerHeight, false);
            mLastMarker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        }

        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.measure_point_red);
        Bitmap smallMarker = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), markerWidth, markerHeight, false);
        MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                .position(latLng);
        Marker marker = aMap.addMarker(markerOption);
        markerList.add(marker);
    }

    /**
     * 绘制线
     */
    private void processAddPolylines() {
        LatLng latLngStart = latLngList.get(latLngList.size() - 2);
        LatLng latLngEnd = latLngList.get(latLngList.size() - 1);
        PolylineOptions polylineOptions = new PolylineOptions().add(latLngStart, latLngEnd).width(15).color(Color.BLUE);
        Polyline polyline = aMap.addPolyline(polylineOptions);
        polylineList.add(polyline);
    }

    /**
     * 开始搜索路径规划方案
     */
    public void searchDistanceResult() {
        List<LatLonPoint> latLonPoints = new ArrayList<>();
        for (int i = 0; i < latLngList.size() - 1; i++) {
            LatLng latLng = latLngList.get(i);
            latLonPoints.add(new LatLonPoint(latLng.latitude, latLng.longitude));
        }
        LatLonPoint dest = new LatLonPoint(latLngList.get(latLngList.size() - 1).latitude, latLngList.get(latLngList.size() - 1).longitude);

        DistanceSearch distanceSearch = new DistanceSearch(this);
        distanceSearch.setDistanceSearchListener(this);
        DistanceSearch.DistanceQuery distanceQuery = new DistanceSearch.DistanceQuery();
        distanceQuery.setOrigins(latLonPoints);
        distanceQuery.setDestination(dest);
        distanceQuery.setType(DistanceSearch.TYPE_DISTANCE);

        distanceSearch.calculateRouteDistanceAsyn(distanceQuery);
    }

//    /**
//     * 开始搜索路径规划方案
//     */
//    public void searchDistanceResult() {
//        List<LatLonPoint> latLonPoints = new ArrayList<>();
//        LatLng latLngStart = latLngList.get(latLngList.size() - 2);
//        LatLng latLngEnd = latLngList.get(latLngList.size() - 1);
//        latLonPoints.add(new LatLonPoint(latLngStart.latitude, latLngStart.longitude));
//        LatLonPoint dest = new LatLonPoint(latLngEnd.latitude, latLngEnd.longitude);
//
//        DistanceSearch distanceSearch = new DistanceSearch(this);
//        distanceSearch.setDistanceSearchListener(this);
//        DistanceSearch.DistanceQuery distanceQuery = new DistanceSearch.DistanceQuery();
//        distanceQuery.setOrigins(latLonPoints);
//        distanceQuery.setDestination(dest);
//        distanceQuery.setType(DistanceSearch.TYPE_DRIVING_DISTANCE);
//
//        distanceSearch.calculateRouteDistanceAsyn(distanceQuery);
//    }

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
            mTvDistance.setText(String.format("%s公里", p));
        } else {
            mTvDistance.setText(String.format("%s米", distance));
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

}
