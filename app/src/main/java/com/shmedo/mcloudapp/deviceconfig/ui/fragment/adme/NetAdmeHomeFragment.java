package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeEquipModelEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeBaseInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeMotionState;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.AdvancedSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeAdvancedConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeBasicParamActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/23 <br/>
 * 描述：     TODO
 */
public class NetAdmeHomeFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.progress_overlay)
    View progressOverlay;

    @BindView(R.id.tv_progress_text)
    TextView mTvProgressText;

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;//设备名称

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;//设备SN号

    @BindView(R.id.tv_product_model)
    TextView mTvProductModel;//版本信息

    @BindView(R.id.tv_time_or_sub_model)
    TextView mTvMotionState;//运行状态

    @BindView(R.id.tv_platform_communication_state)
    TextView mTvPlatformCommunicationState;// 与平台通信状态

    @BindView(R.id.tv_device_state_flag)
    TextView mTvDeviceState;//蓝牙连接状态(已连接、已断开)

    @BindView(R.id.tv_device_connect_operate)
    TextView mTvDeviceConnectOperate;//蓝牙连接操作(断开连接、重新连接)

    @BindView(R.id.tv_config_model)
    TextView mTvConfigModel;//设备模式

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private ConfigModuleAdapter moduleAdapter;
    private List<ConfigModule> configModuleList = new ArrayList<>();
    private ConfigModule selectedConfigModule;

    private AdmeBaseInfo admeBaseInfo;
    private int equipModellPos;
    private String equipModel;//设备模式

    public static NetAdmeHomeFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetAdmeHomeFragment fragment = new NetAdmeHomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_home_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateHeadInfo() ;
        initAdapter();
        initConfigModuleData();
        queryEquipmentBaseInfo();
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mActivity, 15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        moduleAdapter = new ConfigModuleAdapter(configModuleList);
        moduleAdapter.setAnimationEnable(false);
        moduleAdapter.setAnimationFirstOnly(false);
        moduleAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (isDoubleClick(view)) {
                    return;
                }
                selectedConfigModule = configModuleList.get(position);
                processItemClick();
            }
        });
        mRecyclerView.setAdapter(moduleAdapter);
    }

    private void processItemClick() {
        switch (selectedConfigModule.getName()) {
            case "状态":
                DeviceCurrentStateActivity.startActivity(mActivity, projectDeviceInfo, AppContants.DeviceType.ADME);
                break;

            case "基础配置":
                AdmeBasicParamActivity.startActivity(mActivity, projectDeviceInfo);
                break;

            case "数据中心":
                DataCenterHomeActivity.startActivity(mActivity, AppContants.DeviceType.ADME, projectDeviceInfo, AppContants.DataCenterConfigMethod.BASIC_CONFIG);
                break;

            case "高级配置":
                AdmeAdvancedConfigActivity.startActivity(mActivity, projectDeviceInfo);
                break;

            case "设置":
                AdvancedSettingActivity.startActivity(mActivity, projectDeviceInfo, AppContants.DeviceType.ADME);
                break;
        }
    }

    private void initConfigModuleData() {
        configModuleList.clear();

        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, GlobalUtil.getString(R.string.device_config_module_current_state), "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_basic_config, "基础配置", "设备基础参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_data_center, "数据中心", "基础参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_advanced_setting, "高级配置", "设备高级参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_setting, "设置", "高级设置");
        configModuleList.add(configModule);
    }

    /**
     * 获取设备的基本信息
     */
    private void queryEquipmentBaseInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_EQUIPMENT_BASIS);
        showProgressDialog("加载中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 获取电机的运行状态
     */
    private void queryMotorState() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_MOTION_STATE);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }


    @OnClick({ R.id.ll_switch_config_model})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        switch (v.getId()) {
            case R.id.ll_switch_config_model://切换设备模式
                showSwitchConfigModelDialog();
                break;
        }
    }

    private void showSwitchConfigModelDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"设备配置模式", "自动监测模式"},
                        null, equipModellPos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                equipModellPos = position;
                                mTvConfigModel.setText(text);
                                if (text.equals("设备配置模式")) {
                                    equipModel = "0";
                                    admeViewModel.deviceMode = 0;
                                } else {
                                    equipModel = "1";
                                    admeViewModel.deviceMode = 1;
                                }
                                setEquipModel();
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    /**
     * 设置设备模式
     */
    private void setEquipModel() {
        AdmeEquipModelEntity entity = new AdmeEquipModelEntity(equipModel);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_EQUIPMENT_MODEL, entity);
        showProgressDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            dismissProgressDialog();
            showDispatchFailedDialog(cmdStr);
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponseRunnable(0);
        }
    }

    /**
     * 指令下发失败弹框
     */
    private void showDispatchFailedDialog(String cmdStr) {
        ToastUtils.show("下发指令失败");
    }


    /**
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        ToastUtils.show("指令响应错误");
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultTimeOut(queryCmdResult);
        ToastUtils.show("指令响应超时");
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
//        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case ADME_MD_GET_EQUIPMENT_BASIS: {//获取设备的基本信息
                IOTCommandResult<AdmeBaseInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    dismissProgressDialog();
                    String errMsg = String.format("%s %s", "获取设备的基本信息出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                admeBaseInfo = commandResult.getResult();
                updateHeadInfo();
                //获取设备的运行状态
                queryMotorState();
            }
            break;

            case ADME_MD_GET_MOTION_STATE: {//获取ADME的运行状态
                dismissProgressDialog();
                IOTCommandResult<AdmeMotionState> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取设备的运行状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                AdmeMotionState admeMotionState = commandResult.getResult();
                updateMotionState(admeMotionState);
//                startQueryMotorStateRunnable();
            }
            break;

            case ADME_MD_SET_EQUIPMENT_MODEL: {//设置设备模式
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissProgressDialog();
                    String errMsg = String.format("%s %s", "设置设备模式出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                updateConfigModuleData();
                saveConfigInfo();
            }
            break;

            case MD_SAVE_CONFIG_PARAM: {
                dismissProgressDialog();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "保存指令出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
            }
            break;

            default:
                break;
        }
    }

    /**
     * 更新头部信息
     */
    private void updateHeadInfo() {
        mTvPlatformCommunicationState.setVisibility(View.GONE);
        mTvDeviceConnectOperate.setVisibility(View.GONE);
        mTvDeviceName.setText("水平自动监测设备");

        if (admeBaseInfo != null) {
            mTvDeviceSn.setText(String.format("设备编号：%s", admeBaseInfo.getSn()));
            mTvProductModel.setText(String.format("产品型号：%s", !TextUtils.isEmpty(admeBaseInfo.getProductid()) ? admeBaseInfo.getProductid() : "ADME"));
            mTvMotionState.setText("运行状态：--");
            if (!TextUtils.isEmpty(admeBaseInfo.getOnline()) && !admeBaseInfo.getOnline().equals("0")) {
                mTvDeviceState.setText("在线");
                mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_50E9B9));
                mTvDeviceState.setBackgroundResource(R.drawable.bg_device_online_state_flag);

            } else if (admeBaseInfo.getOnline().equals("0")) {
                mTvDeviceState.setText("离线");
                mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
                mTvDeviceState.setBackgroundResource(R.drawable.bg_device_offline_state_flag);
            }

            if (!TextUtils.isEmpty(admeBaseInfo.getEquimodel())) {
                equipModel = admeBaseInfo.getEquimodel();
                if (equipModel.equals("0")) {
                    admeViewModel.deviceMode = 0;
                    equipModellPos = 0;
                    mTvConfigModel.setText("设备配置模式");
                } else {
                    admeViewModel.deviceMode = 1;
                    equipModellPos = 1;
                    mTvConfigModel.setText("自动监测模式");
                }
                updateConfigModuleData();
            }
        } else {
            mTvDeviceSn.setText(String.format("设备编号：%s", TextUtils.isEmpty(projectDeviceInfo.getToken()) ? "" : projectDeviceInfo.getToken()));
            mTvProductModel.setText(String.format("产品型号：%s", TextUtils.isEmpty(projectDeviceInfo.getDeviceTypeName()) ? "ADME" : projectDeviceInfo.getDeviceTypeName()));
            mTvMotionState.setText("运行状态：--");
            if (projectDeviceInfo.isOnline()) {
                mTvDeviceState.setText("在线");
                mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_50E9B9));
                mTvDeviceState.setBackgroundResource(R.drawable.bg_device_online_state_flag);
            } else {
                mTvDeviceState.setText("离线");
                mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
                mTvDeviceState.setBackgroundResource(R.drawable.bg_device_offline_state_flag);
            }

            admeViewModel.deviceMode = 0;
            equipModellPos = 0;
            mTvConfigModel.setText("设备配置模式");
        }
    }

    /**
     * 刷新电机运动状态
     */
    private void updateMotionState(AdmeMotionState admeMotionState) {
        if (admeMotionState == null) {
            Timber.e("AdmeMotionState is Null!");
            return;
        }
        switch (admeMotionState.getMotionstate()) {
            case "0":
                mTvMotionState.setText("运行状态：管口停止");
                break;

            case "1":
                mTvMotionState.setText("运行状态：管底停止");
                break;

            case "2":
                mTvMotionState.setText("运行状态：管口测量");
                break;

            case "3":
                mTvMotionState.setText("运行状态：管口测试");
                break;

            case "4":
                mTvMotionState.setText("运行状态：上拉测量");
                break;

            case "5":
                mTvMotionState.setText("运行状态：上拉测试");
                break;

            case "6":
                mTvMotionState.setText("运行状态：下放测量");
                break;

            case "7":
                mTvMotionState.setText("运行状态：下放测试");
                break;

            default:
                break;
        }
    }

    private void updateConfigModuleData() {
        if (TextUtils.isEmpty(equipModel))
            return;

        configModuleList.clear();
        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, GlobalUtil.getString(R.string.device_config_module_current_state), "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_basic_config, "基础配置", "设备基础参数配置");
        configModuleList.add(configModule);

        if (equipModel.equals("0")) {//0：设备配置模式，1：自动监测模式
            configModule = new ConfigModule(R.drawable.ic_device_data_center, "数据中心", "基础参数配置");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_device_advanced_setting, "高级配置", "设备高级参数配置");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_device_setting, "设置", "高级设置");
            configModuleList.add(configModule);
        } else {
            configModule = new ConfigModule(R.drawable.ic_device_advanced_setting, "高级配置", "设备高级参数配置");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_device_setting, "设置", "高级设置");
            configModuleList.add(configModule);
        }
        moduleAdapter.notifyDataSetChanged();
    }

}
