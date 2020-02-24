package com.shmedo.mcloudapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.ProjectDeviceInfo;
import com.shmedo.mcloudapp.entity.SystemDataInfo;
import com.shmedo.mcloudapp.model.Extras;
import com.shmedo.mcloudapp.views.ClearEditText;
import com.shmedo.mcloudapp.views.EmptyDataView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

public class ProjectContainDevicesActivity extends BaseActivity {
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.ce_find_sn)
    ClearEditText mCeFindSn;

    @BindView(R.id.btn_find)
    Button mBtnFind;

    @BindView(R.id.recycler_deviceType)
    RecyclerView mRecyclerDeviceType;

    @BindView(R.id.empty_data)
    EmptyDataView mEmptyData;

    @BindView(R.id.recyclerDevice)
    RecyclerView mRecyclerDevice;

    private int projID;

    private CommonAdapter adapterDeviceType,adapterDevice;

    private List<ProjectDeviceInfo> deviceInfoList = new ArrayList<>();



    /**
     * 说明：启动Activity
     * <p>
     * 注意：这里使用到了Intent的Flag属性singleTop。singleTop模式下，在同一个task中，如果存在该Activity的实例，
     * 并且该Activity实例位于栈顶(即，该Activity位于前端)，则调用startActivity()时，不再创建该Activity的示例；
     * 而仅仅只是调用Activity的onNewIntent()。否则的话，则新建该Activity的实例，并将其置于栈顶。
     * </p>
     */
    public static void startActivity(Context context, SystemDataInfo systemDataInfo) {
        Intent intent = new Intent(context, ProjectContainDevicesActivity.class);
        intent.putExtra(Extras.QUERY_PROJECT_DEVICE, systemDataInfo);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_project_contain_devices;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initView();
        initAdapter();
    }


    private void initView() {
        Intent intent = getIntent();
        if (intent != null) {
            SystemDataInfo systemDataInfo = (SystemDataInfo) intent.getSerializableExtra(Extras.QUERY_PROJECT_DEVICE);
            if (systemDataInfo == null) {
                Timber.w("传递的参数 SystemDataInfo 值为 NULL!");
                return;
            }

            projID = systemDataInfo.getProID();
            mToolbarTitle.setText(systemDataInfo.getProjName());
        }
    }

    private void initAdapter() {
//        mRecyclerDevice.setLayoutManager(new LinearLayoutManager(this));
//        mRecyclerDevice.addItemDecoration(new DividerItemDecoration());
//        adapterDevice = new CommonAdapter<StatusInfoResult>(this, R.layout.item_all_device, statusInfoList) {
//            @Override
//            protected void convert(ViewHolder holder, final StatusInfoResult statusInfoResult, final int position) {
//                holder.setText(R.id.tv_deviceToken, statusInfoResult.getDeviceToken() + "\n" + (statusInfoResult.getLocal() ? "本地设备" : "云端设备"));
//                holder.setText(R.id.tv_electricity, statusInfoResult.getVoltage() + "V");
//                holder.setText(R.id.tv_gprs, StringUtil.setSize(statusInfoResult.getGprs()));
//                holder.setText(R.id.tv_signal, statusInfoResult.getSignal() + "");
//                holder.setTag(R.id.ll_SensorType, position);
//
//                final List<SensorAndCount> sensorAndCountList = statusInfoResult.getSensorInfo();
//                if (sensorAndCountList == null || sensorAndCountList.size() == 0) {
//                    holder.setVisibleOrGone(R.id.tv_sensor_type_1, false);
//                    holder.setVisibleOrGone(R.id.tv_sensor_type_2, false);
//                    holder.setVisibleOrGone(R.id.tv_sensor_more, false);
//                    return;
//                }
//
//                if (sensorAndCountList.size() > 1) {
//                    holder.setVisibleOrGone(R.id.tv_sensor_type_1, true);
//                    holder.setVisibleOrGone(R.id.tv_sensor_type_2, true);
//                    holder.setVisibleOrGone(R.id.tv_sensor_more, sensorAndCountList.size() > 2);
//
//                    holder.setCompoundDrawablesWithIntrinsicBounds(R.id.tv_sensor_type_1, ImageUtil.getSensorResourceID(sensorAndCountList.get(0).getSensorType()), 0, 0, 0);
//                    holder.setText(R.id.tv_sensor_type_1, "x" + sensorAndCountList.get(0).getSensorCount());
//                    holder.setCompoundDrawablesWithIntrinsicBounds(R.id.tv_sensor_type_2, ImageUtil.getSensorResourceID(sensorAndCountList.get(1).getSensorType()), 0, 0, 0);
//                    holder.setText(R.id.tv_sensor_type_2, "x" + sensorAndCountList.get(1).getSensorCount());
//
//                } else {
//                    holder.setVisibleOrGone(R.id.tv_sensor_type_1, true);
//                    holder.setVisibleOrGone(R.id.tv_sensor_type_2, false);
//                    holder.setVisibleOrGone(R.id.tv_sensor_more, false);
//
//                    holder.setCompoundDrawablesWithIntrinsicBounds(R.id.tv_sensor_type_1, ImageUtil.getSensorResourceID(sensorAndCountList.get(0).getSensorType()), 0, 0, 0);
//                    holder.setText(R.id.tv_sensor_type_1, "x" + sensorAndCountList.get(0).getSensorCount());
//                }
//
//                holder.setOnClickListener(R.id.tv_sensor_more, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                        if (sensorAndCountList == null || sensorAndCountList.size() < 3)
//                            return;
//
//                        DeviceSensorDialog deviceSensorDialog = new DeviceSensorDialog(ProjectContainDevicesActivity.this, R.style.dialog_center_full, sensorAndCountList);
//                        if (!deviceSensorDialog.isShowing()) {
//                            deviceSensorDialog.show();
//                        }
//                    }
//                });
//
//            }
//        };
////        adapterDevice.setOnItemClickListener(this);
//        mRecyclerDevice.setAdapter(adapterDevice);
    }
}
