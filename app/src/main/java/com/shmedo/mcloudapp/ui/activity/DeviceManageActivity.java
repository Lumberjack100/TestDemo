package com.shmedo.mcloudapp.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.reflect.TypeToken;
import com.hjq.toast.ToastUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.DeviceStatusAdapter;
import com.shmedo.mcloudapp.adapter.SystemAdapter;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.DeviceBasicInfoResult;
import com.shmedo.mcloudapp.entity.SensorAndCount;
import com.shmedo.mcloudapp.entity.StatusInfoResult;
import com.shmedo.mcloudapp.entity.StatusInfoResultDao;
import com.shmedo.mcloudapp.entity.SystemDataInfo;
import com.shmedo.mcloudapp.entity.parameter.LocationResult;
import com.shmedo.mcloudapp.entity.parameter.SystemParameter;
import com.shmedo.mcloudapp.model.BaseObserver;
import com.shmedo.mcloudapp.model.MDRetrofit;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.util.AmapUtil;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.XPermissionUtils;
import com.shmedo.mcloudapp.views.DeviceSensorDialog;
import com.shmedo.mcloudapp.views.DividerItemDecoration;
import com.shmedo.mcloudapp.views.EmptyDataView;
import com.shmedo.mcloudapp.views.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   DeviceManageActivity
 * 创建者:   dpc
 * 创建时间:  2019/4/11 16:16
 * 描述：    设备管理页面
 */
public class DeviceManageActivity extends BaseActivity implements OnRefreshListener, OnRefreshLoadMoreListener {

    @BindView(R.id.recycler_system)
    RecyclerView mRecyclerSystem;

    @BindView(R.id.recycler_device)
    RecyclerView mRecyclerDevice;

    @BindView(R.id.smartRefreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.empty_data)
    EmptyDataView mEmptyData;

    private List<SystemDataInfo> systemList = new ArrayList<>();
    private List<StatusInfoResult> statusInfoList = new ArrayList<>();
    private DeviceStatusAdapter deviceStatusAdapter;
    private LoadingDialog mLoadingDialog;
    private SystemAdapter systemAdapter;
    private DaoManager manager = DaoManager.getInstance();

    @Override
    protected int initContentView() {
        return R.layout.activity_device_manage;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        getProjectList();
        //获取设备列表
        getDeviceList("1");
    }


    private void initView() {
        manager.init(this);
        mLoadingDialog = new LoadingDialog(this);
        systemAdapter = new SystemAdapter(this, systemList);
        mRecyclerSystem.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerSystem.addItemDecoration(new DividerItemDecoration());
        mRecyclerSystem.setAdapter(systemAdapter);
        setSystemAdapterClick();

        deviceStatusAdapter = new DeviceStatusAdapter(this, statusInfoList);
        mRecyclerDevice.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerDevice.addItemDecoration(new DividerItemDecoration());
        mRecyclerDevice.setAdapter(deviceStatusAdapter);
        deviceStatusAdapter.setOnItemClickListener(listener);

        //mRefreshLayout.setEnableAutoLoadMore(true);
        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setOnRefreshLoadMoreListener(this);
        mRefreshLayout.setRefreshHeader(new ClassicsHeader(this));
        //mRefreshLayout.setRefreshFooter(new ClassicsFooter(this));
    }


    /**
     * 获取当前用户的项目列表
     */
    private void getProjectList() {
        mLoadingDialog.showNoCancelDialog("加载数据中...");
        //获取系统
        SystemParameter parameter = new SystemParameter(null);
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(CommonVariable.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .QueryUserListProject(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<SystemDataInfo>>() {
                    @Override
                    public void Success(List<SystemDataInfo> infoList, String message) {
                        mLoadingDialog.dismiss();

                        if (null != infoList && infoList.size() != 0) {
                            for (SystemDataInfo systemDataInfo : infoList) {
                                systemDataInfo.setAccount(MCloudApp.getAccount());
                            }
                            manager.getDaoSession().getSystemDataInfoDao().insertOrReplaceInTx(infoList);

                            systemList.clear();
                            systemList.addAll(infoList);
                            systemAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void Failure(String message) {
                        mLoadingDialog.dismiss();

                        Timber.w("服务器连接失败--" + message);
                    }
                });

    }


    private void setSystemAdapterClick() {
        systemAdapter.setOnItemClickLitener(new SystemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {

                DeviceManageDetailActivity.startActivity(DeviceManageActivity.this, systemAdapter.getDataList().get(position));
            }
        });
        systemAdapter.setOnItemMapClickListener(new SystemAdapter.OnItemMapClickListener() {
            @Override
            public void onItemMapClick(View view, int position) {
                getLocationPermission(position);
            }
        });
    }


    /**
     * 获取地图权限，点击展示地图
     */
    private void getLocationPermission(final int position) {
        XPermissionUtils.requestPermissionsResult(this, 200, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        if (systemList != null) {
                            LocationResult location = GsonFactory.getGson()
                                    .fromJson(systemList.get(position).getCenterPoint(), new TypeToken<LocationResult>() {
                                    }.getType());

                            Timber.d("map===" + location.toString());
                            openGuideMap(String.valueOf(location.getLat()), String.valueOf(location.getLng()), systemList.get(position).getProjName());
                        }
                    }


                    @Override
                    public void onPermissionDenied() {
                        XPermissionUtils.showRefusePermissionDialog(DeviceManageActivity.this,
                                getResources().getString(R.string.permission_request_location));
                    }
                });

    }


    /**
     * 打开高德地图，如果未安装就打开网页版
     */
    private void openGuideMap(String latitude, String longitude, String deviceName) {
        if (AmapUtil.isAvilible(this, "com.autonavi.minimap")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "androidamap://viewMap?sourceApplication=DAS&poiname=" + deviceName + "&lat=" +
                            longitude + "&lon=" + latitude + "&dev=0"));
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setPackage("com.autonavi.minimap");
            startActivity(intent);
        } else {
            ToastUtils.show("您尚未安装高德地图，我们将为您打开网页版");
            String mark = "设备:" + deviceName + "位置";
            Uri uri = Uri.parse(
                    "http://uri.amap.com/marker?position=" + latitude + "," + longitude + "&name=" +
                            mark + "&src=mypage&coordinate=gaode&callnative=1");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }

    }


    @OnClick(R.id.ll_search)//点击跳转搜索页面
    public void onViewClicked() {
        Intent intent = new Intent(this, SearchDeviceActivity.class);
        startActivity(intent);
    }


    /**
     * 获取设备列表
     *
     * @param currentCompanyID
     */
    private void getDeviceList(String currentCompanyID) {
        RequestBody body = RequestBody.create(CommonVariable.JSON_TYPE, currentCompanyID);
        MDRetrofit.getInstance()
                .createService()
                .QueryDeviceStatusInfoList(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<StatusInfoResult>>() {

                    @Override
                    public void Success(List<StatusInfoResult> infoList, String message) {
                        mLoadingDialog.dismiss();

                        if (null != infoList && infoList.size() != 0) {
                            for (StatusInfoResult statusInfoResult : infoList) {
                                statusInfoResult.setAccount(MCloudApp.getAccount());
                            }
                            manager.getDaoSession().getStatusInfoResultDao().insertOrReplaceInTx(infoList);

                            //从本地数据库查出云端设备和本地设备显示
                            queryDeviceStatusList();

                        } else {
                            //mEmptyData.setVisibility(View.VISIBLE);
                        }
                    }


                    @Override
                    public void Failure(String message) {
                        mLoadingDialog.dismiss();

                        Timber.w("服务器连接失败--" + message);
                        ToastUtils.show("服务器连接失败");
                    }
                });

    }

    private void queryDeviceStatusList() {
        List<StatusInfoResult> infoList = manager.getDaoSession().getStatusInfoResultDao().queryBuilder()
                .where(StatusInfoResultDao.Properties.Account.isNotNull(), StatusInfoResultDao.Properties.Account.eq(MCloudApp.getAccount()))
                .list();

        if (null != infoList && infoList.size() > 0) {
            //TODO 添加测试数据,测试后需要删除
            infoList.get(0).setSensorInfo(initTestData());

            statusInfoList.clear();
            statusInfoList.addAll(infoList);
            deviceStatusAdapter.notifyDataSetChanged();
        }
    }

    private DeviceSensorDialog deviceSensorDialog;
    private DeviceStatusAdapter.OnItemClickListener listener = new DeviceStatusAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View v, DeviceStatusAdapter.ViewName viewName, int position) {
            switch (v.getId()) {
                case R.id.ll_SensorType://传感器类型 Layout 点击事件
                    Timber.d("----传感器---" + position);

                    List<SensorAndCount> data = statusInfoList.get(position).getSensorInfo();
                    if (data == null || data.size() < 3)
                        return;

                    deviceSensorDialog = new DeviceSensorDialog(DeviceManageActivity.this, R.style.dialog_center_full, data);
                    if (!deviceSensorDialog.isShowing()) {
                        deviceSensorDialog.show();
                    }
                    break;

                default://整个 Item 点击事件
                    StatusInfoResult statusInfoResult = statusInfoList.get(position);
                    MCloudApp.setCurDeviceToken(statusInfoResult.getDeviceName());
                    MCloudApp.setCurDeviceMacAddr(null);

                    if (!TextUtils.isEmpty(statusInfoResult.getDeviceTypeName()) && statusInfoResult.getDeviceTypeName().toUpperCase().contains("ADME")) {
                        String deviceInfo = "MEDO," + statusInfoResult.getDeviceName() + "," + statusInfoResult.getDeviceTypeName();
                        ConfigADMEActivity.startActivity(DeviceManageActivity.this, deviceInfo);
                        return;
                    }

                    if (!TextUtils.isEmpty(statusInfoResult.getDeviceTypeName()) && statusInfoResult.getDeviceTypeName().toUpperCase().contains("DAS")) {
                        String deviceInfo = "MEDO," + statusInfoResult.getDeviceName() + "," + statusInfoResult.getDeviceTypeName();
                        ConfigDASActivity.startActivity(DeviceManageActivity.this, deviceInfo);
                        return;
                    }

                    if (!TextUtils.isEmpty(statusInfoResult.getDeviceName()) && statusInfoResult.getDeviceName().toUpperCase().contains("E60")) {
                        DeviceBasicInfoResult deviceBasicInfoResult = new DeviceBasicInfoResult();
                        deviceBasicInfoResult.setDeviceToken(statusInfoResult.getDeviceToken());
                        deviceBasicInfoResult.setDeviceName(statusInfoResult.getDeviceName());
                        ConfigE60Activity.startActivity(DeviceManageActivity.this, deviceBasicInfoResult);
                        return;
                    }
                    break;
            }
        }
    };

    private List<SensorAndCount> initTestData() {
        List<SensorAndCount> list = new ArrayList<>();

        SensorAndCount sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(2);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(4);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(6);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(8);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(12);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(15);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(15);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(15);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(15);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(15);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(15);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(15);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(15);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(50);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(51);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(51);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(51);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(51);
        list.add(sensorAndCount);

        sensorAndCount = new SensorAndCount();
        sensorAndCount.setSensorCount(3);
        sensorAndCount.setSensorType(53);
        list.add(sensorAndCount);

        return list;
    }


    /**
     * 上拉加载更多
     */
    @Override
    public void onLoadMore(RefreshLayout refreshLayout) {

    }


    /**
     * 下拉刷新数据
     */
    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        if (MCloudApp.isIsNetworkConnected()) {
            statusInfoList.clear();
            getDeviceList("1");
            refreshLayout.finishRefresh();
            refreshLayout.setNoMoreData(false);
        } else {
            refreshLayout.finishRefresh();
            refreshLayout.setNoMoreData(true);
            ToastUtils.show("请检查网络连接");
            //getProjectList();
        }
    }


}
