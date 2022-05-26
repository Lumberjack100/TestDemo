package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.StringUtils;
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
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeBaseInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeMotionState;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.AdvancedSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeAdvancedConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeBasicParamActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;

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

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;//设备名称

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;//设备SN号

    @BindView(R.id.tv_product_model)
    TextView mTvFirmwareVersion;//版本信息

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
    private String equipModel;//设备模式
    private String[] modes;


    public static NetAdmeHomeFragment newInstance(DeviceInfo deviceInfo) {
        NetAdmeHomeFragment fragment = new NetAdmeHomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.adme_home_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        modes = getResources().getStringArray(R.array.adme_device_mode);
        initAdapter();
        updateHeadInfo();
        queryEquipmentBaseInfo();
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = ConvertUtils.dp2px(15);//每一个矩形的间距
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
                DeviceCurrentStateActivity.startActivity(mActivity, deviceInfo, ProductType.ADME);
                break;

            case "基础配置":
                AdmeBasicParamActivity.startActivity(mActivity, deviceInfo);
                break;

            case "高级配置":
                AdmeAdvancedConfigActivity.startActivity(mActivity, deviceInfo);
                break;

            case "设置":
                AdvancedSettingActivity.startActivity(mActivity, deviceInfo, ProductType.ADME);
                break;
        }
    }

    /**
     * 获取设备的基本信息
     */
    private void queryEquipmentBaseInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_EQUIPMENT_BASIS);
        showWaitDialog("加载中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 获取电机的运行状态
     */
    private void queryMotorState() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_MOTION_STATE);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    @OnClick({R.id.ll_switch_config_model})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        if (v.getId() == R.id.ll_switch_config_model) {//切换设备模式
            showSwitchConfigModelDialog();
        }
    }

    private void showSwitchConfigModelDialog() {
        int pos = Arrays.asList(modes).indexOf(String.valueOf(mTvConfigModel.getText()));
        pos = pos == -1 ? 0 : pos;
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", modes,
                        null, pos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                //不可以主动切换到异常保护模式
                                if (text.equals(modes[2])) {
                                    return;
                                }
                                mTvConfigModel.setText(text);
                                if (text.equals(modes[0])) {//设备配置模式
                                    equipModel = "0";
                                    admeViewModel.deviceMode = 0;
                                } else if (text.equals(modes[1])) {//自动检测模式
                                    equipModel = "1";
                                    admeViewModel.deviceMode = 1;
                                }
                                setEquipModel();
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 设置设备模式
     */
    private void setEquipModel() {
        AdmeEquipModelEntity entity = new AdmeEquipModelEntity(equipModel);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_EQUIPMENT_MODEL, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            dismissWaitDialog();
            showDispatchFailedDialog(cmdStr);
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponse();
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
                    dismissWaitDialog();
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
                dismissWaitDialog();
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
                    dismissWaitDialog();
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
                dismissWaitDialog();
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
        try {
            mTvDeviceName.setText(TextUtils.isEmpty(deviceInfo.getProductName()) ? "自动化测斜机器人" : deviceInfo.getProductName());
            mTvDeviceSn.setText(String.format("设备编号：%s", TextUtils.isEmpty(deviceInfo.getDeviceToken()) ? "" : deviceInfo.getDeviceToken()));
            mTvFirmwareVersion.setText(String.format("固件版本：%s", TextUtils.isEmpty(deviceInfo.getFirmwareVersion()) ? "--" : deviceInfo.getFirmwareVersion()));
            mTvMotionState.setText("运行状态：--");
            if (admeBaseInfo != null) {
                setDeviceState(mTvDeviceState, !TextUtils.isEmpty(admeBaseInfo.getOnline()) && !admeBaseInfo.getOnline().equals("0"));
                equipModel = admeBaseInfo.getEquimodel();
                if (equipModel.equals("0")) {
                    admeViewModel.deviceMode = 0;
                    mTvConfigModel.setText(modes[0]);
                } else if (equipModel.equals("1")) {
                    admeViewModel.deviceMode = 1;
                    mTvConfigModel.setText(modes[1]);
                } else if (equipModel.equals("2")) {
                    admeViewModel.deviceMode = 2;
                    mTvConfigModel.setText(modes[2]);
                }
            } else {
                setDeviceState(mTvDeviceState, deviceInfo.isOnlineStatus());
                equipModel = "0";
                admeViewModel.deviceMode = 0;
                mTvConfigModel.setText(modes[0]);
            }
            mTvPlatformCommunicationState.setVisibility(View.GONE);
            mTvDeviceConnectOperate.setVisibility(View.GONE);

            updateConfigModuleData();
        } catch (Exception ex) {
            ex.printStackTrace();
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
        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, StringUtils.getString(R.string.device_config_module_current_state), "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_basic_config, "基础配置", "设备基础参数配置");
        configModuleList.add(configModule);

        if (equipModel.equals("0")) {//0：设备配置模式，1：自动监测模式
            configModule = new ConfigModule(R.drawable.ic_device_advanced_setting, "高级配置", "设备高级参数配置");
            configModuleList.add(configModule);

            configModule = new ConfigModule(R.drawable.ic_device_setting, "设置", "高级设置");
            configModuleList.add(configModule);
        } else if (equipModel.equals("1")) {//1：自动监测模式
            configModule = new ConfigModule(R.drawable.ic_device_advanced_setting, "高级配置", "设备高级参数配置");
            configModuleList.add(configModule);

        } else if (equipModel.equals("2")) {
            configModuleList.clear();
            configModule = new ConfigModule(R.drawable.ic_device_current_state, StringUtils.getString(R.string.device_config_module_current_state), "获取当前设备状态");
            configModuleList.add(configModule);
        }
        moduleAdapter.notifyDataSetChanged();
    }
}
