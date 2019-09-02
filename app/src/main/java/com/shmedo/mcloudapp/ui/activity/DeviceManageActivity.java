package com.shmedo.mcloudapp.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import butterknife.BindView;
import butterknife.OnClick;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.DeviceStatusAdapter;
import com.shmedo.mcloudapp.adapter.SystemAdapter;
import com.shmedo.mcloudapp.base.BaseActivity;
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
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.util.XPermissionUtils;
import com.shmedo.mcloudapp.views.ClearEditText;
import com.shmedo.mcloudapp.views.DeviceSensorDialog;
import com.shmedo.mcloudapp.views.DividerItemDecoration;
import com.shmedo.mcloudapp.views.EmptyDataView;
import com.shmedo.mcloudapp.views.LoadingDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import okhttp3.RequestBody;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   DeviceManageActivity
 * 创建者:   dpc
 * 创建时间:  2019/4/11 16:16
 * 描述：    设备管理页面
 */
public class DeviceManageActivity extends BaseActivity implements OnRefreshListener,OnRefreshLoadMoreListener{

    @BindView(R.id.ce_search) ClearEditText mCeSearch;

    @BindView(R.id.recycler_system) RecyclerView mRecyclerSystem;
    @BindView(R.id.recycler_device) RecyclerView mRecyclerDevice;
    @BindView(R.id.smartRefreshLayout) SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.empty_data) EmptyDataView mEmptyData;

    private List<SystemDataInfo> systemList = new ArrayList<>();
    //private List<AllDeviceParameter> allDeviceList = new ArrayList<>();
    private List<StatusInfoResult> statusInfoList = new ArrayList<>();
    private DeviceStatusAdapter deviceStatusAdapter;
    private LoadingDialog mLoadingDialog;
    private SystemAdapter systemAdapter;
    private DaoManager manager = DaoManager.getInstance();

    @Override protected int initContentView() {
        return R.layout.activity_device_manage;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
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

        deviceStatusAdapter = new DeviceStatusAdapter(this, statusInfoList);
        mRecyclerDevice.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerDevice.addItemDecoration(new DividerItemDecoration());

        //mRefreshLayout.setEnableAutoLoadMore(true);
        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnRefreshLoadMoreListener(this);
        mRefreshLayout.setRefreshHeader(new ClassicsHeader(this));
        //mRefreshLayout.setRefreshFooter(new ClassicsFooter(this));
        mRecyclerDevice.setAdapter(deviceStatusAdapter);
        deviceStatusAdapter.setOnItemClickListener(listener);
    }


    private void initData() {
        mLoadingDialog.showNoCancelDialog("加载数据中...");
        //获取系统
        SystemParameter parameter = new SystemParameter(null);
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(CommonVariable.JSON_TYPE, json);
        MDRetrofit.getInstance()
            .createService()
            .QueryUserListProject(CommonVariable.getAccessToken(), body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new BaseObserver<List<SystemDataInfo>>() {
                @Override public void Success(List<SystemDataInfo> infoList, String message) {
                    mLoadingDialog.dismiss();
                    Log.i("adu", message + "===" + GsonFactory.getGson().toJson(infoList));
                    //setDeviceDetail(deviceDetailInfo);
                    if (infoList.size() != 0) {
                        systemList.clear();
                        systemList.addAll(infoList);
                        systemAdapter.notifyDataSetChanged();
                    }
                }


                @Override public void Failure(String message) {
                    mLoadingDialog.dismiss();
                    Log.i("adu", "服务器连接失败--" + message);
                    ToastUtil.showSToast("服务器连接失败");
                }
            });
        setSystemAdapterClick();
    }


    private void setSystemAdapterClick() {
        systemAdapter.setOnItemClickLitener(new SystemAdapter.OnItemClickListener() {
            @Override public void onItemClick(View itemView, int position) {
                ToastUtil.showSToast("=="+systemAdapter.getDataList().get(position).getProID());
                Intent intent = new Intent(DeviceManageActivity.this,
                    DeviceManageDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("queryProjectDevice", (Serializable) systemAdapter.getDataList().get(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        systemAdapter.setOnItemMapClickListener(new SystemAdapter.OnItemMapClickListener() {
            @Override public void onItemMapClick(View view, int position) {
                getLocationPermission(position);
            }
        });
    }


    /**
     * 获取地图权限，点击展示地图
     */
    private void getLocationPermission(final int position) {

        XPermissionUtils.requestPermissionsResult(this, 200, new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION },
            new XPermissionUtils.OnPermissionListener() {
                @Override
                public void onPermissionGranted() {
                    if (systemList != null) {
                        LocationResult location = GsonFactory.getGson()
                            .fromJson(systemList.get(position).getCenterPoint(),
                                new TypeToken<LocationResult>() {}.getType());
                        Log.i("adu", "===map===" + location.toString());
                        openGuideMap(String.valueOf(location.getLat()), String.valueOf(location.getLng()),
                            systemList.get(position).getProjName());

                    }
                }


                @Override
                public void onPermissionDenied() {
                    LoadingDialog.showRefusePermissionDialog(DeviceManageActivity.this,
                        "在设置-应用管理-米易通-权限中开启相机权限");
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
            ToastUtil.showSToast("您尚未安装高德地图，我们将为您打开网页版");
            String mark = "设备:" + deviceName + "位置";
            Uri uri = Uri.parse(
                "http://uri.amap.com/marker?position=" + latitude + "," + longitude + "&name=" +
                    mark + "&src=mypage&coordinate=gaode&callnative=1");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }

    }


    @OnClick(R.id.ce_search)//点击跳转搜索页面
    public void onViewClicked() {
       Intent intent = new Intent(this,SearchDeviceActivity.class);
       startActivity(intent);
    }



    /**
     * 获取设备列表
     * @param currentCompanyID
     */
    private void getDeviceList(String currentCompanyID) {


        RequestBody body = RequestBody.create(CommonVariable.JSON_TYPE, currentCompanyID);
        MDRetrofit.getInstance()
            .createService()
            .QueryDeviceStatusInfoList(CommonVariable.getAccessToken(), body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new BaseObserver<List<StatusInfoResult>>() {

                @Override public void Success(List<StatusInfoResult> infoList, String message) {
                    mLoadingDialog.dismiss();

                    if (infoList.size() != 0) {
                        Log.i("adu","---11--"+infoList.get(0).getSignal());
                        //statusInfoList.clear();
                        //statusInfoList.addAll(infoList);
                        //deviceStatusAdapter.notifyDataSetChanged();
                        manager.getDaoSession().getStatusInfoResultDao().insertOrReplaceInTx(infoList);
                        //数据存到数据库，查出云端设备和本地设备显示
                        queryDeviceStatusList();
                    }else {
                        //mEmptyData.setVisibility(View.VISIBLE);
                    }
                }


                @Override public void Failure(String message) {
                    mLoadingDialog.dismiss();
                    Log.i("adu", "服务器连接失败--" + message);
                    ToastUtil.showSToast("服务器连接失败");
                }
            });

    }

    private void queryDeviceStatusList(){
        List<StatusInfoResult> infoList = manager.getDaoSession().getStatusInfoResultDao().queryBuilder()
            .list();
        Log.i("adu","----query status list ===="+infoList.size());
        statusInfoList.clear();
        statusInfoList.addAll(infoList);
        deviceStatusAdapter.notifyDataSetChanged();
    }
    private DeviceSensorDialog deviceSensorDialog;
    private DeviceStatusAdapter.OnItemClickListener listener = new DeviceStatusAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View v, DeviceStatusAdapter.ViewName viewName, int position,List<SensorAndCount> list) {
            switch (v.getId()){
                case R.id.ll_SensorType:
                    ToastUtil.showSToast("这是传感器"+position);
                    Log.i("adu","----传感器---"+position);
                    deviceSensorDialog = new DeviceSensorDialog(DeviceManageActivity.this,
                        R.style.dialog_center_full,list);
                     if (!deviceSensorDialog.isShowing()){
                         deviceSensorDialog.show();
                     }
                    break;
                    default:
                        Log.i("adu","----item---"+position);
                        ToastUtil.showSToast("这是item"+position);
                        break;
            }
        }
    };


    /**
     * 上拉加载更多
     */
    @Override public void onLoadMore(RefreshLayout refreshLayout) {

    }


    /**
     * 下拉刷新数据
     */
    @Override public void onRefresh(RefreshLayout refreshLayout) {
        if (CommonVariable.isNetworkConnected()) {
            statusInfoList.clear();
            getDeviceList("1");
            refreshLayout.finishRefresh();
            refreshLayout.setNoMoreData(false);
        } else {
            refreshLayout.finishRefresh();
            refreshLayout.setNoMoreData(true);
            ToastUtil.showSToast("请检查网络连接");
            //initData();
        }
    }


}
