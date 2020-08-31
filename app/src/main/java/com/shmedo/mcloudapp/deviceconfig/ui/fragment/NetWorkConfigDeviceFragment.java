package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.params.DispatchCmdParam;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceRunAnalysisActivity;
import com.shmedo.mcloudapp.deviceconfig.view.DispatchCmdDialog;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.GsonFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class NetWorkConfigDeviceFragment extends BaseFragment {
    private static final String DEVICE_INFO = "device_info";

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_device_model)
    TextView mTvDeviceModel;

    @BindView(R.id.tv_time)
    TextView mTvTime;

    @BindView(R.id.tv_device_communication_state_flag)
    TextView mTvDeviceCommunicationState;//通信状态(在线、离线、已连接、已断开)

    @BindView(R.id.tv_device_connect_state)
    TextView mTvDeviceConnectState;//蓝牙连接状态(断开连接、重新连接)

    @BindView(R.id.tv_device_communication_way)
    TextView mTvDeviceCommunicationWay;//通信方式(网络、蓝牙)

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private ConfigModuleAdapter moduleAdapter;

    private List<ConfigModule> configModuleList = new ArrayList<>();
    private ProjectDeviceInfo projectDeviceInfo;
    private int companyID;
    private List<String> msgIDList = new ArrayList<>();


    public static NetWorkConfigDeviceFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetWorkConfigDeviceFragment fragment = new NetWorkConfigDeviceFragment();
        Bundle args = new Bundle();
        args.putParcelable(DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectDeviceInfo = getArguments().getParcelable(DEVICE_INFO);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_net_work_config_device;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initUserData();
        setHeadInfo();
        initAdapter();
        initConfigModuleData();
    }

    private void initUserData() {
        UserInfo userInfo = MCloudApp.getCurrentUserInfo();
        if (userInfo != null && userInfo.getDepartments() != null && userInfo.getDepartments().size() > 0) {
            companyID = userInfo.getDepartments().get(0).getCompanyID();
        }
    }

    private void setHeadInfo() {
        if (projectDeviceInfo != null) {
            mTvDeviceName.setText("物联网数据采集器");
            mTvDeviceSn.setText(String.format("设备编号：%s", TextUtils.isEmpty(projectDeviceInfo.getName()) ? "" : projectDeviceInfo.getName()));
            mTvDeviceModel.setText(String.format("产品型号：%s", TextUtils.isEmpty(projectDeviceInfo.getDeviceTypeName()) ? "" : projectDeviceInfo.getDeviceTypeName()));
            mTvTime.setText(String.format("更新时间：%s", TextUtils.isEmpty(projectDeviceInfo.getLastActiveTime()) ? "" : projectDeviceInfo.getLastActiveTime()));
            if (projectDeviceInfo.isOnline()) {
                mTvDeviceCommunicationState.setText("在线");
                mTvDeviceCommunicationState.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_50E9B9));
                mTvDeviceCommunicationState.setBackgroundResource(R.drawable.bg_device_online_state_flag);
            } else {
                mTvDeviceCommunicationState.setText("离线");
                mTvDeviceCommunicationState.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
                mTvDeviceCommunicationState.setBackgroundResource(R.drawable.bg_device_offline_state_flag);
            }
        }
        mTvDeviceConnectState.setVisibility(View.INVISIBLE);
        mTvDeviceCommunicationWay.setText("网络");
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mActivity, 15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        moduleAdapter = new ConfigModuleAdapter(configModuleList);
        moduleAdapter.setAnimationEnable(true);
        moduleAdapter.setAnimationFirstOnly(false);
        moduleAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {

                ConfigModule configModule = (ConfigModule) configModuleList.get(position);
                processItemClick(configModule);
            }
        });
        mRecyclerView.setAdapter(moduleAdapter);
    }

    @OnClick({R.id.tv_device_communication_way, R.id.rl_run_state_analysis})
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.tv_device_communication_way://切换连接方式
                break;

            case R.id.rl_run_state_analysis:
                DeviceRunAnalysisActivity.startActivity(mActivity, projectDeviceInfo);
                break;
        }
    }

    private void processItemClick(ConfigModule configModule) {

        switch (configModule.getName()) {
            case "状态":
            case "时间":
            case "遥测":
            case "重启":
                dispatchCmd(configModule.getCmdID());
                break;

            case "固件升级":
                break;

            case "采集器配置":
                break;

            default:
                break;
        }
    }

    private void initConfigModuleData() {
        configModuleList.clear();
        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, 5, "状态", "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_current_time, 1, "时间", "获取当前设备时间");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_telemetry, 6, "遥测", "远距离测量");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_reboot, 7, "重启", "重新启动当前设备");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_firmware_upgrade, 24, "固件升级", "版本:01.0012");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_collector_config, "采集器配置", "采集器参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_instruction_send, "指令下发", "服务端代码指令下发");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_advanced_setting, "设置", "高级设置");
        configModuleList.add(configModule);
    }

    private void showDialog() {
        DispatchCmdDialog dispatchCmdDialog = new DispatchCmdDialog(mActivity, "遥测", msgIDList);
        dispatchCmdDialog.setOnQueryCmdResultListener(new DispatchCmdDialog.OnQueryCmdResultListener() {
            @Override
            public void onSuccess() {

            }
        });
        new XPopup.Builder(getContext())
                .asCustom(dispatchCmdDialog)
                .show();
    }

    private void dispatchCmd(int cmdID) {
        DispatchCmdParam dispatchCmdParam = new DispatchCmdParam();
        dispatchCmdParam.setCmdID(cmdID);
        dispatchCmdParam.setCompanyID(companyID);
        dispatchCmdParam.setDeviceIDList(Arrays.asList(projectDeviceInfo.getId()));

        String json = GsonFactory.getGson().toJson(dispatchCmdParam);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .DispatchCmd(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<DispatchCmdItem>>() {
                    @Override
                    public void Success(List<DispatchCmdItem> data, String message) {
                        if (data == null || data.size() == 0) {
                            return;
                        }

                        msgIDList.clear();
                        for (DispatchCmdItem cmdItem : data) {
                            msgIDList.add(cmdItem.getMsgID());
                        }
                        showDialog();
                    }

                    @Override
                    public void Failure(String message) {
                        Timber.w("请求失败--%s", message);
                        ToastUtils.show("下发指令失败");
                    }
                });
    }
}
