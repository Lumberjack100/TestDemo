package com.shmedo.mcloudapp.maps.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
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

    private AMap aMap;

    private LatLng latLngStart;
    private LatLng latLngEnd;
    private List<LatLng> latLngList = new ArrayList<LatLng>();
    private Marker mLastMarker;//


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


    @OnClick({R.id.back, R.id.iv_remove_marker, R.id.tv_clear_markers})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                break;

            case R.id.iv_remove_marker:
                break;

            case R.id.tv_clear_markers:
                aMap.clear();
                latLngList.clear();
                mTvDistance.setText("0米");
                break;

            default:
                break;
        }
    }


    @Override
    public void onMapClick(LatLng latLng) {
        processAddMarkers(latLng);
        processAddPolylines();
        if (latLngList.size() >= 2) {
            searchDistanceResult();
        }
    }

    /**
     * 绘制点标记
     *
     * @param latLng
     */
    private void processAddMarkers(LatLng latLng) {
        latLngList.add(latLng);
        if (latLngList.size() == 1) {
            MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.polyline_start))
                    .position(latLng)
                    .draggable(false);
            aMap.addMarker(markerOption);
            return;
        }

        int height = DensityUtil.Dp2Px(this, 12);
        int width = DensityUtil.Dp2Px(this, 12);
        if (mLastMarker != null) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.measure_point);
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), width, height, false);
            mLastMarker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        }

        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.measure_point_red);
        Bitmap smallMarker = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), width, height, false);
        MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                .position(latLng)
                .draggable(false);
        mLastMarker = aMap.addMarker(markerOption);
    }

    /**
     * 绘制线
     */
    private void processAddPolylines() {
        if (latLngList == null || latLngList.size() == 0) {
            return;
        }

        for (int i = 0; i < latLngList.size() - 1; i++) {
            latLngStart = latLngList.get(i);
            latLngEnd = latLngList.get(i + 1);
            PolylineOptions polylineOptions = new PolylineOptions().add(latLngStart, latLngEnd).width(15).color(Color.BLUE);
            Polyline polyline=aMap.addPolyline(polylineOptions);
        }
    }

    /**
     * 开始搜索路径规划方案
     */
    public void searchDistanceResult() {
        List<LatLonPoint> latLonPoints = new ArrayList<LatLonPoint>();
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
        distanceQuery.setType(DistanceSearch.TYPE_DRIVING_DISTANCE);

        distanceSearch.calculateRouteDistanceAsyn(distanceQuery);
    }

    @Override
    public void onDistanceSearched(DistanceResult distanceResult, int errorCode) {
        if (errorCode != AMapException.CODE_AMAP_SUCCESS) {
            ToastUtils.show(MapErrorUtil.getErrorMsg(errorCode));
            return;
        }

        float distance = 0;
        List<DistanceItem> distanceItems = distanceResult.getDistanceResults();
        for (DistanceItem item : distanceItems) {
            distance += item.getDistance();
        }

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
