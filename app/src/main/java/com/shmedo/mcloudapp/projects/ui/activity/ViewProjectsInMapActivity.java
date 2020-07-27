package com.shmedo.mcloudapp.projects.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MyLocationStyle;
import com.google.gson.reflect.TypeToken;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.entity.ProjectDetailInfoDao;
import com.shmedo.mcloudapp.entity.parameter.LocationResult;
import com.shmedo.mcloudapp.projects.cluster.ClusterClickListener;
import com.shmedo.mcloudapp.projects.cluster.ClusterItem;
import com.shmedo.mcloudapp.projects.cluster.ClusterOverlay;
import com.shmedo.mcloudapp.projects.cluster.ClusterRender;
import com.shmedo.mcloudapp.projects.model.ProjectDetailInfo;
import com.shmedo.mcloudapp.projects.model.RegionItem;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.GsonFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

public class ViewProjectsInMapActivity extends BaseActivity implements ClusterRender, AMap.OnMapLoadedListener, ClusterClickListener {
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.mapView)
    public MapView mMapView;

    private AMap aMap; //地图控制器对象
    private MyLocationStyle mLocationStyle;
    private int clusterRadius = 100;
    private Map<Integer, Drawable> mBackDrawAbles = new HashMap<Integer, Drawable>();

    private ClusterOverlay mClusterOverlay;

    private int userId;


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ViewProjectsInMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_view_projects_in_map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("项目地图");
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        initData();
        initView();
    }

    private void initData() {
        UserInfo userInfo = MCloudApp.getCurrentUserInfo();
        if (userInfo != null && userInfo.getUser() != null) {
            UserInfo.UserBean user = userInfo.getUser();
            userId = user.getId();
        }
    }

    private void initView() {
        if (aMap == null) {
            //初始化地图控制器对象
            aMap = mMapView.getMap();
            setUpMap();
        }
        aMap.setOnMapLoadedListener(this);
    }

    private void setUpMap() {
        setLocationStyle();
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.getUiSettings().setZoomControlsEnabled(true); //隐藏缩放控件
        aMap.getUiSettings().setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_RIGHT);//设置logo位置
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setMyLocationEnabled(true);
    }

    private void setLocationStyle() {
        // 自定义系统定位蓝点
        if (mLocationStyle == null) {
            mLocationStyle = new MyLocationStyle();
        }
        // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.setMyLocationStyle(mLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁资源
        mClusterOverlay.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }


    @Override
    public void onMapLoaded() {
        List<ProjectDetailInfo> detailInfoList = queryLocalProjectList();

        //添加测试数据
        new Thread() {
            public void run() {

                List<ClusterItem> clusterItemList = new ArrayList<ClusterItem>();
                for (ProjectDetailInfo detailInfo : detailInfoList) {
                    LocationResult location = GsonFactory.getGson()
                            .fromJson(detailInfo.getCenterPoint(), new TypeToken<LocationResult>() {
                            }.getType());
                    if (location != null) {
                        LatLng latLng = new LatLng(location.getLat(), location.getLng());
                        RegionItem regionItem = new RegionItem(latLng, detailInfo.getProjectName());
                        clusterItemList.add(regionItem);
                    }
                }

                mClusterOverlay = new ClusterOverlay(aMap, clusterItemList,
                        DensityUtil.Dp2Px(getApplicationContext(), clusterRadius), getApplicationContext());
                mClusterOverlay.setClusterRenderer(ViewProjectsInMapActivity.this);
                mClusterOverlay.setOnClusterClickListener(ViewProjectsInMapActivity.this);
            }
        }.start();
    }

    @Override
    public void onClick(Marker marker, List<ClusterItem> clusterItems) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (ClusterItem clusterItem : clusterItems) {
            builder.include(clusterItem.getPosition());
        }
        // 地图显示经纬度范围
        LatLngBounds latLngBounds = builder.build();
        //设置显示在规定屏幕范围内的地图经纬度范围
        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 0));
    }

    @Override
    public Drawable getDrawAble(int clusterNum) {
        int radius = DensityUtil.Dp2Px(getApplicationContext(), 80);
        if (clusterNum == 1) {
            Drawable bitmapDrawable = mBackDrawAbles.get(1);
            if (bitmapDrawable == null) {
                bitmapDrawable = getApplication().getResources().getDrawable(R.drawable.ic_marker_thumbtack);
                mBackDrawAbles.put(1, bitmapDrawable);
            }
            return bitmapDrawable;

        } else if (clusterNum < 5) {
            Drawable bitmapDrawable = mBackDrawAbles.get(2);
            if (bitmapDrawable == null) {
                bitmapDrawable = new BitmapDrawable(null, drawCircle(radius, Color.argb(159, 210, 154, 6)));
                mBackDrawAbles.put(2, bitmapDrawable);
            }
            return bitmapDrawable;

        } else if (clusterNum < 10) {
            Drawable bitmapDrawable = mBackDrawAbles.get(3);
            if (bitmapDrawable == null) {
                bitmapDrawable = new BitmapDrawable(null, drawCircle(radius, Color.argb(199, 217, 114, 0)));
                mBackDrawAbles.put(3, bitmapDrawable);
            }
            return bitmapDrawable;

        } else {
            Drawable bitmapDrawable = mBackDrawAbles.get(4);
            if (bitmapDrawable == null) {
                bitmapDrawable = new BitmapDrawable(null, drawCircle(radius, Color.argb(235, 215, 66, 2)));
                mBackDrawAbles.put(4, bitmapDrawable);
            }
            return bitmapDrawable;
        }
    }

    private Bitmap drawCircle(int radius, int color) {
        Bitmap bitmap = Bitmap.createBitmap(radius * 2, radius * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        RectF rectF = new RectF(0, 0, radius * 2, radius * 2);
        paint.setColor(color);
        canvas.drawArc(rectF, 0, 360, true, paint);
        return bitmap;
    }

    /**
     * 查询位置信息不为空的当前用户的设备
     */
    private List<ProjectDetailInfo> queryLocalProjectList() {
        List<ProjectDetailInfo> detailInfoList =
                DaoManager.getInstance().getDaoSession()
                        .getProjectDetailInfoDao()
                        .queryBuilder()
                        .where(ProjectDetailInfoDao.Properties.UserId.eq(userId))
                        .list();

        return detailInfoList;
    }
}
