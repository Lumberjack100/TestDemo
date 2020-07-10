package com.shmedo.mcloudapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.toast.ToastUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.shmedo.core.utils.StringUtil;
import com.dragon.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.MultiItemTypeAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.ViewHolder;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.ConfigADMEActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.ConfigDASActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.ConfigE60Activity;
import com.shmedo.mcloudapp.entity.DeviceBasicInfoResult;
import com.shmedo.mcloudapp.entity.SensorAndCount;
import com.shmedo.mcloudapp.entity.StatusInfoResult;
import com.shmedo.mcloudapp.entity.StatusInfoResultDao;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.ImageUtil;
import com.shmedo.mcloudapp.views.DeviceSensorDialog;
import com.shmedo.mcloudapp.views.EmptyDataView;
import com.shmedo.mcloudapp.views.recycleviewitemdivider.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 创建者:   gh
 * 创建时间:  2020/2/20 21:08
 * 描述：    设备列表页面
 */
public class DeviceListActivity extends BaseActivity implements  MultiItemTypeAdapter.OnItemClickListener, OnRefreshListener, OnRefreshLoadMoreListener {
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.tv_search_hint)
    TextView mTvSearchHint;

    @BindView(R.id.recycler_devide)
    RecyclerView mRecyclerDevice;

    @BindView(R.id.smartRefreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.empty_data)
    EmptyDataView mEmptyData;

    private CommonAdapter adapter;

    private List<StatusInfoResult> statusInfoList = new ArrayList<>();

    private DaoManager manager = DaoManager.getInstance();


    /**
     * 说明：启动Activity
     * <p>
     * 注意：这里使用到了Intent的Flag属性singleTop。singleTop模式下，在同一个task中，如果存在该Activity的实例，
     * 并且该Activity实例位于栈顶(即，该Activity位于前端)，则调用startActivity()时，不再创建该Activity的示例；
     * 而仅仅只是调用Activity的onNewIntent()。否则的话，则新建该Activity的实例，并将其置于栈顶。
     * </p>
     */
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, DeviceListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }



    @Override
    protected int initContentView() {
        return R.layout.activity_device_list;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("设备管理");
        mTvSearchHint.setText("请输入设备SN号");
        initView();
        initAdapter();
        //获取设备列表
        getDeviceList("1");
    }

    private void initView() {
        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setEnableAutoLoadMore(false);
        mRefreshLayout.setEnableLoadMore(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(this);
        mRefreshLayout.setRefreshHeader(new ClassicsHeader(this));
        //mRefreshLayout.setRefreshFooter(new ClassicsFooter(this));
    }

    private void initAdapter() {
        mRecyclerDevice.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerDevice.addItemDecoration(new DividerItemDecoration());
        adapter = new CommonAdapter<StatusInfoResult>(this, R.layout.item_all_device, statusInfoList) {
            @Override
            protected void convert(ViewHolder holder, final StatusInfoResult statusInfoResult, final int position) {
                holder.setText(R.id.tv_deviceToken, statusInfoResult.getDeviceToken() + "\n" + (statusInfoResult.getLocal() ? "本地设备" : "云端设备"));
                holder.setText(R.id.tv_electricity, statusInfoResult.getVoltage() + "V");
                holder.setText(R.id.tv_gprs, StringUtil.getByteSize(statusInfoResult.getGprs()));
                holder.setText(R.id.tv_signal, statusInfoResult.getSignal() + "");
                holder.setTag(R.id.ll_SensorType, position);

                final List<SensorAndCount> sensorAndCountList = statusInfoResult.getSensorInfo();
                if (sensorAndCountList == null || sensorAndCountList.size() == 0) {
                    holder.setVisibleOrGone(R.id.tv_sensor_type_1, false);
                    holder.setVisibleOrGone(R.id.tv_sensor_type_2, false);
                    holder.setVisibleOrGone(R.id.tv_sensor_more, false);
                    return;
                }

                if (sensorAndCountList.size() > 1) {
                    holder.setVisibleOrGone(R.id.tv_sensor_type_1, true);
                    holder.setVisibleOrGone(R.id.tv_sensor_type_2, true);
                    holder.setVisibleOrGone(R.id.tv_sensor_more, sensorAndCountList.size() > 2);

                    holder.setCompoundDrawablesWithIntrinsicBounds(R.id.tv_sensor_type_1, ImageUtil.getSensorResourceID(sensorAndCountList.get(0).getSensorType()), 0, 0, 0);
                    holder.setText(R.id.tv_sensor_type_1, "x" + sensorAndCountList.get(0).getSensorCount());
                    holder.setCompoundDrawablesWithIntrinsicBounds(R.id.tv_sensor_type_2, ImageUtil.getSensorResourceID(sensorAndCountList.get(1).getSensorType()), 0, 0, 0);
                    holder.setText(R.id.tv_sensor_type_2, "x" + sensorAndCountList.get(1).getSensorCount());

                } else {
                    holder.setVisibleOrGone(R.id.tv_sensor_type_1, true);
                    holder.setVisibleOrGone(R.id.tv_sensor_type_2, false);
                    holder.setVisibleOrGone(R.id.tv_sensor_more, false);

                    holder.setCompoundDrawablesWithIntrinsicBounds(R.id.tv_sensor_type_1, ImageUtil.getSensorResourceID(sensorAndCountList.get(0).getSensorType()), 0, 0, 0);
                    holder.setText(R.id.tv_sensor_type_1, "x" + sensorAndCountList.get(0).getSensorCount());
                }

                holder.setOnClickListener(R.id.tv_sensor_more, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (sensorAndCountList == null || sensorAndCountList.size() < 3)
                            return;

                        DeviceSensorDialog deviceSensorDialog = new DeviceSensorDialog(DeviceListActivity.this, R.style.dialog_center_full, sensorAndCountList);
                        if (!deviceSensorDialog.isShowing()) {
                            deviceSensorDialog.show();
                        }
                    }
                });

            }
        };
        adapter.setOnItemClickListener(this);
        mRecyclerDevice.setAdapter(adapter);
    }


    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        StatusInfoResult statusInfoResult = statusInfoList.get(position);
        MCloudApp.setCurDeviceToken(statusInfoResult.getDeviceName());
        MCloudApp.setCurDeviceMacAddr(null);

        String deviceInfo = "MEDO," + statusInfoResult.getDeviceName() + "," + statusInfoResult.getDeviceTypeName();
        if (!TextUtils.isEmpty(statusInfoResult.getDeviceTypeName()) && statusInfoResult.getDeviceTypeName().toUpperCase().contains("ADME")) {
            ConfigADMEActivity.startActivity(DeviceListActivity.this, deviceInfo);
        }

        if (!TextUtils.isEmpty(statusInfoResult.getDeviceTypeName()) && statusInfoResult.getDeviceTypeName().toUpperCase().contains("DAS")) {
            ConfigDASActivity.startActivity(DeviceListActivity.this, deviceInfo);
        }

        if (!TextUtils.isEmpty(statusInfoResult.getDeviceName()) && statusInfoResult.getDeviceName().toUpperCase().contains("E60")) {
            DeviceBasicInfoResult deviceBasicInfoResult = new DeviceBasicInfoResult();
            deviceBasicInfoResult.setDeviceToken(statusInfoResult.getDeviceToken());
            deviceBasicInfoResult.setDeviceName(statusInfoResult.getDeviceName());
            ConfigE60Activity.startActivity(DeviceListActivity.this, deviceBasicInfoResult);
        }
    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        return false;
    }


    @OnClick({R.id.ll_search})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_search:
                SearchDeviceActivity.startActivity(this);
                break;
        }
    }



    /**
     * 上拉加载更多
     */
    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

    }


    /**
     * 下拉刷新数据
     */
    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        if (MCloudApp.isIsNetworkConnected()) {
            statusInfoList.clear();
            getDeviceList("1");
            refreshLayout.finishRefresh();
            refreshLayout.setNoMoreData(false);
        } else {
            refreshLayout.finishRefresh();
            refreshLayout.setNoMoreData(true);
            ToastUtils.show("请检查网络连接");
        }
    }

    /**
     * 获取用户在当前公司的设备状态信息列表
     *
     * @param currentCompanyID
     */
    private void getDeviceList(String currentCompanyID) {
        showLoadingDialog("加载数据中...");

        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, currentCompanyID);
        MDRetrofit.getInstance()
                .createService()
                .QueryDeviceStatusInfoList(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<StatusInfoResult>>() {

                    @Override
                    public void Success(List<StatusInfoResult> infoList, String message) {
                        dismissLoadingDialog();

                        if (null != infoList && infoList.size() != 0) {
                            for (StatusInfoResult statusInfoResult : infoList) {
                                statusInfoResult.setAccount(MCloudApp.getAccount());
                            }
                            manager.getDaoSession().getStatusInfoResultDao().insertOrReplaceInTx(infoList);

                            //从本地数据库查出云端设备和本地设备显示
                            queryDeviceStatusList();

                        } else {
                            mEmptyData.setVisibility(View.VISIBLE);
                        }
                    }


                    @Override
                    public void Failure(String message) {
                        dismissLoadingDialog();
                        Timber.w("服务器连接失败--" + message);
                    }
                });

    }

    /**
     * 从本地数据库查出云端设备和本地设备进行显示
     */
    private void queryDeviceStatusList() {
        List<StatusInfoResult> infoList = manager.getDaoSession().getStatusInfoResultDao().queryBuilder()
                .where(StatusInfoResultDao.Properties.Account.isNotNull(), StatusInfoResultDao.Properties.Account.eq(MCloudApp.getAccount()))
                .list();

        if (null != infoList && infoList.size() > 0) {
            //TODO 添加测试数据,测试后需要删除
//            infoList.get(0).setSensorInfo(initTestData());

            statusInfoList.clear();
            statusInfoList.addAll(infoList);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 传感器测试数据
     * @return
     */
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


}
