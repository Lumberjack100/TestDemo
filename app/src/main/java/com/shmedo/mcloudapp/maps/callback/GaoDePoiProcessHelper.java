package com.shmedo.mcloudapp.maps.callback;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.AmapPageType;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.maps.model.MapMode;
import com.shmedo.mcloudapp.maps.ui.activity.MapActivity;
import com.shmedo.mcloudapp.maps.ui.view.PoiDetailBottomView;
import com.shmedo.mcloudapp.maps.util.MyAMapUtils;

import static com.shmedo.mcloudapp.maps.ui.activity.MapActivity.STATE_UNLOCKED;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/3 <br/>
 * 描述：    高德地图Poi点位打标记、显示信息辅助类
 */
public class GaoDePoiProcessHelper implements AMap.OnPOIClickListener, PoiDetailBottomView.OnPoiDetailBottomClickListener {
    private MapActivity mapActivity;
    private PoiDetailBottomView mPoiDetailBottomView;
    private TextureMapView mapView;
    private AMap aMap;
    private Marker poiMarker;
    private LatLng mClickPoiLatLng;//当前点击的poi经纬度

    private int moveY;
    private int[] mBottomSheetLoc = new int[2];


    public GaoDePoiProcessHelper(MapActivity mapActivity) {
        this.mapActivity = mapActivity;
        mapView = mapActivity.mMapView;
        aMap = mapView.getMap();
        mPoiDetailBottomView = mapActivity.mPoiDetailBottomView;
        registerListeners();
    }


    private void registerListeners() {
        // 地图poi点击
        aMap.setOnPOIClickListener(this);
        mPoiDetailBottomView.setOnPoiDetailBottomClickListener(this);
    }

    public void setOnPOIClickListener(boolean isEnable) {
        aMap.setOnPOIClickListener(isEnable ? this : null);
    }

    /**
     * 当用户点击底图上的poi时回调此方法。
     */
    @Override
    public void onPOIClick(Poi poi) {
        if (poi == null || poi.getCoordinate() == null || TextUtils.isEmpty(poi.getName())) {
            return;
        }
        // 当前正在处理poi点击
        mapActivity.isPoiClick = true;
        addPOIMarderAndShowDetail(poi.getCoordinate(), poi.getName());
    }

    /**
     * 关闭点位详情框
     */
    @Override
    public void onPoiCloseClick() {
        hidePoiDetailBottomView();
    }

    /**
     * 分享点位信息
     */
    @Override
    public void onPoiShareClick() {

    }

    /**
     * 跳转点位导航
     */
    @Override
    public void onPoiNaviClick() {
        AmapNaviParams amapNaviParams = new AmapNaviParams(new Poi("我的位置", mapActivity.mLatLng, ""), null, new Poi(mapActivity.mPoiName, mClickPoiLatLng, ""), AmapNaviType.DRIVER, AmapPageType.NAVI);//, AmapPageType.NAVI
        amapNaviParams.setUseInnerVoice(true);
        AmapNaviPage.getInstance().showRouteActivity(mapActivity, amapNaviParams, null);
    }

    /**
     * 跳转点位路线
     */
    @Override
    public void onPoiRouteClick() {

    }

    public void destroyPoiMarker(){
        if (poiMarker != null) {
            poiMarker.destroy();
        }
    }

    /**
     * 添加POImarker
     */
    public void addPOIMarderAndShowDetail(LatLng latLng, String poiName) {
        mClickPoiLatLng = latLng;

        //移动地图中心点到指定位置
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mapActivity.mZoomLevel));
        mapActivity.mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
        mapActivity.mCurrentGpsState = STATE_UNLOCKED;
        //当前没有正在定位才能修改状态
        if (!mapActivity.isFirstLocation) {
            mapActivity.mGpsView.setGpsState(mapActivity.mCurrentGpsState);
        }
        mapActivity.isCanMoveToCenter = false;
        mapActivity.mPoiName = poiName;
        // 添加marker标记
        addPOIMarker(latLng);
        String distanceStr = MyAMapUtils.calculateDistanceStr(mapActivity.mLatLng, latLng);
        showPoiDetailBottomView(poiName, String.format("距离您%s", distanceStr));
    }


    private void addPOIMarker(LatLng latLng) {
        destroyPoiMarker();
        MarkerOptions markOptiopns = new MarkerOptions();
        markOptiopns.position(latLng);
        markOptiopns.icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_mark));
        poiMarker = aMap.addMarker(markOptiopns);
    }

    /**
     * 底部显示POI详情
     *
     * @param locTitle 定位标题,比如当前所在位置名称
     * @param locInfo  定位信息,比如当前在什么附近/距离当前位置多少米
     */
    public void showPoiDetailBottomView(String locTitle, String locInfo) {
        mapActivity.mMapMode = MapMode.SHOW_POIDETAIL;
        if (mPoiDetailBottomView.getVisibility() == View.GONE) {
            mapActivity.mGpsView.setVisibility(View.VISIBLE);
            mapActivity.mRouteView.setVisibility(View.GONE);
            mPoiDetailBottomView.setVisibility(View.VISIBLE);
            moveGspButtonAbove();
        }
        mPoiDetailBottomView.tvPoiTitle.setText(locTitle);
        mPoiDetailBottomView.tvPoiDistance.setText(locInfo);
        mPoiDetailBottomView.tvNavi.setVisibility(locTitle.equals("我的位置") ? View.GONE : View.VISIBLE);
    }


    /**
     * 隐藏底部POI详情
     */
    private void hidePoiDetailBottomView() {
        mapActivity.mMapMode = MapMode.NORMAL;
        //gsp控件回退到原来位置、并显示底部其他控件
        mapActivity.mRouteView.setVisibility(View.VISIBLE);
        mPoiDetailBottomView.setVisibility(View.GONE);
        if (poiMarker != null) {
            poiMarker.destroy();
            poiMarker = null;
        }
        resetGpsButtonPosition();
    }

    /**
     * 将GpsButton移动到poi detail上面
     */
    private void moveGspButtonAbove() {
        mPoiDetailBottomView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPoiDetailBottomView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (mapActivity.mGpsView.isAbovePoiDetail()) {
                    //已经在上面，不需要重复调用
                    return;
                }
                if (moveY == 0) {
                    //计算Y轴方向移动距离
                    moveY = mapActivity.mGspContainer.getTop() - mPoiDetailBottomView.getTop() + mapActivity.mGspContainer.getMeasuredHeight() + mapActivity.getResources().getDimensionPixelSize(R.dimen.dimen_size_10);
                    mPoiDetailBottomView.getLocationInWindow(mBottomSheetLoc);
                }
                if (moveY > 0) {
                    mapActivity.mZoomView.setTranslationY(-moveY);
                    mapActivity.mGspContainer.setTranslationY(-moveY);
                    mapActivity.mGpsView.setAbovePoiDetail(true);
                }

                //设置 MapView 的bottomMargin
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mapView.getLayoutParams());
                layoutParams.bottomMargin = mPoiDetailBottomView.getHeight();
                mapView.setLayoutParams(layoutParams);
            }
        });
    }

    /**
     * 将GpsButton移动到原来位置
     */
    public void resetGpsButtonPosition() {
        mPoiDetailBottomView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPoiDetailBottomView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (!mapActivity.mGpsView.isAbovePoiDetail()) {
                    //已经在下面，不需要重复调用
                    return;
                }
                //回到原来位置
                mapActivity.mZoomView.setTranslationY(0);
                mapActivity.mGspContainer.setTranslationY(0);
                mapActivity.mGpsView.setAbovePoiDetail(false);

                //设置 MapView 的bottomMargin
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mapView.getLayoutParams());
                layoutParams.bottomMargin = 0;
                mapActivity.mMapView.setLayoutParams(layoutParams);
            }
        });
    }
}
