package com.shmedo.mcloudapp.maps.helper;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;

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
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.LatLonSharePoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.share.ShareSearch;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.interfaces.SimpleCallback;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.maps.model.MapMode;
import com.shmedo.mcloudapp.maps.ui.activity.MapActivity;
import com.shmedo.mcloudapp.maps.util.MyAMapUtils;
import com.shmedo.mcloudapp.maps.view.PoiDetailBottomView;
import com.shmedo.mcloudapp.maps.view.PoiSharePopup;
import com.shmedo.mcloudapp.util.permission.PermissionHelper;

import java.util.HashMap;

import cn.sharesdk.dingding.friends.Dingding;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;
import timber.log.Timber;

import static com.shmedo.mcloudapp.maps.ui.activity.MapActivity.STATE_UNLOCKED;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/3 <br/>
 * 描述：    高德地图Poi点位打标记、显示信息辅助类
 */
public class GaoDePoiProcessHelper implements AMap.OnPOIClickListener, PoiDetailBottomView.OnPoiDetailBottomClickListener, ShareSearch.OnShareSearchListener {
    private MapActivity mapActivity;
    private PoiDetailBottomView mPoiDetailBottomView;
    private TextureMapView mapView;
    private AMap aMap;
    private Marker poiMarker;
    private LatLng mClickPoiLatLng;//当前点击的poi经纬度

    private int moveY;
    private int[] mBottomSheetLoc = new int[2];
    private ShareSearch mShareSearch;


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
        mShareSearch = new ShareSearch(mapActivity);
        mShareSearch.setOnShareSearchListener(this);
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
        mapActivity.sharePoi = new PoiItem(null, new LatLonPoint(poi.getCoordinate().latitude, poi.getCoordinate().longitude), poi.getName(), poi.getName());
    }

    /**
     * 关闭点位详情框
     */
    @Override
    public void onPoiCloseClick() {
        hidePoiDetailBottomView();
        mapActivity.sharePoi = null;
    }

    /**
     * 分享点位信息
     */
    @Override
    public void onPoiShareClick() {
        LatLonSharePoint point = new LatLonSharePoint(mapActivity.sharePoi.getLatLonPoint().getLatitude(),
                mapActivity.sharePoi.getLatLonPoint().getLongitude(), mapActivity.sharePoi.getSnippet());
        mShareSearch.searchLocationShareUrlAsyn(point);
    }

    /**
     * 跳转点位导航
     */
    @Override
    public void onPoiNaviClick() {
        mapActivity.checkPermissionForGPS(PermissionHelper.REQUEST_CODE_NAVI);
    }

    /**
     * 跳转点位路线
     */
    @Override
    public void onPoiRouteClick() {

    }

    public void doOnPermissionGranted(int requestCode) {
        if (requestCode == PermissionHelper.REQUEST_CODE_NAVI) {
            AmapNaviParams amapNaviParams = new AmapNaviParams(new Poi("我的位置", mapActivity.mLatLng, ""), null, new Poi(mapActivity.mPoiName, mClickPoiLatLng, ""), AmapNaviType.DRIVER, AmapPageType.NAVI);//, AmapPageType.NAVI
            amapNaviParams.setUseInnerVoice(true);
            AmapNaviPage.getInstance().showRouteActivity(mapActivity, amapNaviParams, null);
        }
    }

    public void destroyPoiMarker() {
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
            }
        });
    }

    @Override
    public void onPoiShareUrlSearched(String url, int errorCode) {

    }

    @Override
    public void onLocationShareUrlSearched(String url, int errorCode) {
        showShareBottomDialog(url);
    }

    @Override
    public void onNaviShareUrlSearched(String url, int errorCode) {

    }

    @Override
    public void onBusRouteShareUrlSearched(String url, int errorCode) {

    }

    @Override
    public void onWalkRouteShareUrlSearched(String url, int errorCode) {

    }

    @Override
    public void onDrivingRouteShareUrlSearched(String url, int errorCode) {

    }

    private void showShareBottomDialog(String url) {
        final PoiSharePopup poiSharePopup = new PoiSharePopup(mapActivity);
        new XPopup.Builder(mapActivity)
                .moveUpToKeyboard(false) //如果不加这个，评论弹窗会移动到软键盘上面
                .enableDrag(false)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .setPopupCallback(new SimpleCallback() {
                    @Override
                    public void onShow(BasePopupView popupView) {
                    }

                    @Override
                    public void onDismiss(BasePopupView popupView) {
                        int item = poiSharePopup.getSelectedShareItem();
                        if (item == PoiSharePopup.SHARE_DINGDING) {
                            shareText(Dingding.NAME, url);
                        } else if (item == PoiSharePopup.SHARE_WX) {
                            shareText(Wechat.NAME, url);
                        }
                    }
                })
                .asCustom(poiSharePopup)
                .show();
    }

    private void shareText(String name, String url) {
        Platform platform = ShareSDK.getPlatform(name);
        Platform.ShareParams shareParams = new Platform.ShareParams();
        shareParams.setTitle(mapActivity.sharePoi.getTitle());
        shareParams.setText(mapActivity.sharePoi.getSnippet());
        shareParams.setUrl(url);
//        Bitmap bmp = BitmapFactory.decodeResource(mapActivity.getResources(), R.drawable.map_icon);
//        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
//        bmp.recycle();
//        shareParams.setImageData(thumbBmp);
        shareParams.setShareType(Platform.SHARE_WEBPAGE);
        shareParams.setScence(0);
        platform.setPlatformActionListener(new MyPlatformActionListener());
        platform.share(shareParams);
    }


    static class MyPlatformActionListener implements PlatformActionListener {
        @Override
        public void onComplete(final Platform platform, int i, HashMap<String, Object> hashMap) {
            String ss = "";
        }

        @Override
        public void onError(final Platform platform, int arg1, Throwable throwable) {
            //失败的回调，platform:平台对象，arg1:表示当前的动作，throwable:异常信息
            throwable.printStackTrace();
            Timber.e("分享失败：%s", throwable.toString());
            ToastUtils.show("分享失败：" + throwable.toString());
        }

        @Override
        public void onCancel(Platform platform, int i) {
            ToastUtils.show("分享取消");
        }
    }
}
