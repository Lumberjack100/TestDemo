package com.shmedo.mcloudapp.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.MultiItemTypeAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.ViewHolder;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.DeviceBasicInfoResult;
import com.shmedo.mcloudapp.entity.SensorAndCount;
import com.shmedo.mcloudapp.entity.StatusInfoResult;
import com.shmedo.mcloudapp.entity.StatusInfoResultDao;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.ImageUtil;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.views.ClearEditText;
import com.shmedo.mcloudapp.views.DeviceSensorDialog;
import com.shmedo.mcloudapp.views.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   SearchDeviceActivity
 * 创建者:   dpc
 * 创建时间:  2019/8/20 18:08
 * 描述：    搜索设备
 */
public class SearchDeviceActivity extends BaseActivity implements MultiItemTypeAdapter.OnItemClickListener {

    @BindView(R.id.ce_search)
    ClearEditText mCeSearch;

    @BindView(R.id.btn_search)
    Button mBtnSearch;

    @BindView(R.id.recycler_device)
    RecyclerView mRecyclerDevice;

    private CommonAdapter adapter;

    private List<StatusInfoResult> statusInfoList = new ArrayList<>();
    private DaoManager manager = DaoManager.getInstance();

    @Override
    protected int initContentView() {
        return R.layout.activity_search_device;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initAdapter();
    }


    private void initView() {
        manager.init(this);
        //搜索框获取焦点，弹出软键盘
        KeyBordUtils.popSoftKeyboard(mCeSearch,true);
    }

    private void initAdapter(){
        mRecyclerDevice.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerDevice.addItemDecoration(new DividerItemDecoration());
        adapter = new CommonAdapter<StatusInfoResult>(this, R.layout.item_all_device, statusInfoList) {
            @Override
            protected void convert(ViewHolder holder, final StatusInfoResult statusInfoResult, final int position) {
                holder.setText(R.id.tv_deviceStatus, statusInfoResult.getDeviceName());
                holder.setText(R.id.tv_deviceToken, statusInfoResult.getDeviceToken() + "\n" + (statusInfoResult.getLocal() ? "本地设备" : "云端设备"));
                holder.setText(R.id.tv_electricity, statusInfoResult.getVoltage() + "V");
                holder.setText(R.id.tv_gprs, StringUtil.setSize(statusInfoResult.getGprs()));
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

                        DeviceSensorDialog deviceSensorDialog = new DeviceSensorDialog(SearchDeviceActivity.this, R.style.dialog_center_full, sensorAndCountList);
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

        if (!TextUtils.isEmpty(statusInfoResult.getDeviceTypeName()) && statusInfoResult.getDeviceTypeName().toUpperCase().contains("DAS")) {
            String deviceInfo = "MEDO," + statusInfoResult.getDeviceToken() + "," + statusInfoResult.getDeviceTypeName();
            ConfigDASActivity.startActivity(SearchDeviceActivity.this, deviceInfo);
            SearchDeviceActivity.this.finish();
        }

        if (!TextUtils.isEmpty(statusInfoResult.getDeviceName()) && statusInfoResult.getDeviceName().toUpperCase().contains("E60")) {

            DeviceBasicInfoResult deviceBasicInfoResult = new DeviceBasicInfoResult();
            deviceBasicInfoResult.setDeviceToken(statusInfoResult.getDeviceToken());
            deviceBasicInfoResult.setDeviceTypeName(statusInfoResult.getDeviceName());

            ConfigE60Activity.startActivity(SearchDeviceActivity.this, deviceBasicInfoResult);
            SearchDeviceActivity.this.finish();
        }
    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        return false;
    }


    @OnClick(R.id.btn_search)
    public void onViewClicked() {
        String snName = mCeSearch.getText().toString();
        if (StringUtil.isEmpty(snName)) {
            ToastUtils.show("设备的SN号不能为空");
            return;
        }
        KeyBordUtils.hideSoftKeyboard(mBtnSearch);
        List<StatusInfoResult> list = fuzzyQueryDevice(snName);
        statusInfoList.clear();
        statusInfoList.addAll(list);
        adapter.notifyDataSetChanged();
    }

    /**
     * 通过设备名字进行模糊查询
     */
    private List<StatusInfoResult> fuzzyQueryDevice(String name) {
        List<StatusInfoResult> list = manager.getDaoSession()
                .getStatusInfoResultDao()
                .queryBuilder()
                .where(StatusInfoResultDao.Properties.DeviceToken.like("%" + name + "%"))
                .list();
        return list;
    }
}
