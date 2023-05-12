package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.entity.CollectorConfigEntity;
import com.shmedo.configlibrary.ble.cmd.entity.CollectorSensorParamsEntity;
import com.shmedo.configlibrary.ble.cmd.entity.SetCollectorAddressEntity;
import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.model.CollectorConfigInfo;
import com.shmedo.configlibrary.ble.model.CollectorSensorParamsInfo;
import com.shmedo.configlibrary.ble.utils.ResultParserUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ExternalSensorAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ExternalSensorItem;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.sensor.DasExternalDigitalSensorActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.sensor.DasExternalVibratingWireSensorActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BaseBleCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * DAS扩展传感器配置页面
 *
 * @deprecated 后面将用物联网指令模式取代
 */
public abstract class BaseBleDasExternalSensorListFragment extends BaseBleCommunicateFragment {
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.recyclerview_sensor)
    RecyclerView mRecyclerViewSensor;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    private ExternalSensorAdapter mAdapter;
    private ExternalSensorItem curItem;

    //以传感器的通道号为 Key,CollectorSensorParamsInfo 对象为 Value
    protected HashMap<String, CollectorSensorParamsInfo> sensorHashMap = new HashMap<>();
    protected List<CollectorSensorParamsInfo> collectorSensorParamsInfoSubs = new ArrayList<>();
    protected CollectorSensorParamsInfo defaultSensorParamsInfo = new CollectorSensorParamsInfo();
    private CollectorSensorParamsInfo curSensorParamsInfo;
    private ArrayList<String> addressList = new ArrayList<>();

    private CollectorConfigInfo collectorConfigInfo;
    protected String collectorName;
    protected String collectorCode;//采集器类型

    private final int maxSensorSum = 16;
    private int accessSum = 0;    //接入扩展传感器总数
    protected int sensorIndex = 0;//接入的传感器索引号
    private boolean isAddSensor = false;//是启用新传感器还是编辑现有传感器
    private boolean isVibratingWireSensor = false;//是否振弦式传感器
    private ActivityResultLauncher<Intent> resultLauncher;

    @Override
    public void onPause() {
        super.onPause();
        isActive = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            collectorCode = getArguments().getString(AppContants.Extras.COLLECTOR_MODE);
            collectorName = CollectorModel.value(collectorCode).getDescription();
            if (CollectorModel.value(collectorCode) == CollectorModel.VW08) {//振弦式传感器
                isVibratingWireSensor = true;
            }
        }
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent intent = result.getData();
                            if (null == intent) {
                                return;
                            }
                            Parcelable parcelableData = intent.getParcelableExtra(AppContants.Extras.SENSOR_PARAM);
                            String sensorAddress = intent.getStringExtra(AppContants.Extras.SENSOR_ADDRESS);
                            SensorType sensorType = (SensorType) intent.getSerializableExtra(AppContants.Extras.SENSOR_TYPE);
                            if (isAddSensor) {
                                CollectorSensorParamsInfo sensorParamsInfo = new CollectorSensorParamsInfo();
                                sensorParamsInfo.setCollectorModel(defaultSensorParamsInfo.getCollectorModel());
                                sensorParamsInfo.setSensorAddress(sensorAddress);
                                sensorParamsInfo.setSensorType(sensorType);
                                sensorParamsInfo.setSensorData(parcelableData);
                                sensorHashMap.put(sensorAddress, sensorParamsInfo);

                                ExternalSensorItem item = new ExternalSensorItem(sensorAddress);
                                item.setVibratingWireSensor(isVibratingWireSensor);
                                mAdapter.getData().add(item);
                                mAdapter.notifyItemInserted(mAdapter.getData().size());
                                mBtnSave.setEnabled(true);
                            } else {
                                sensorHashMap.remove(curItem.getSensorAddress());
                                curSensorParamsInfo.setSensorAddress(sensorAddress);
                                curSensorParamsInfo.setSensorType(sensorType);
                                curSensorParamsInfo.setSensorData(parcelableData);
                                sensorHashMap.put(sensorAddress, curSensorParamsInfo);

                                curItem.setSensorAddress(sensorAddress);
                            }
                        }
                    }
                });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.net_das_external_sensor_list_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAdapter();
        initRefreshLayout();
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        mRefreshLayout.autoRefresh();
    }

    private void initAdapter() {
        int spanCount = 4;//跟布局里面的spanCount属性是一致的
        int spacing = ConvertUtils.dp2px(15);//每一个矩形的间距
        mRecyclerViewSensor.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        //设置每个item间距
        mRecyclerViewSensor.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        mAdapter = new ExternalSensorAdapter(mActivity, new ArrayList<>());
        mAdapter.setOnItemClickListener(new ExternalSensorAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                if (!isConnected()) {
                    ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                isAddSensor = false;
                curItem = mAdapter.getData().get(position);
                addressList.clear();
                for (ExternalSensorItem item : mAdapter.getData()) {
                    if (!TextUtils.isEmpty(item.getSensorAddress())) {
                        addressList.add(item.getSensorAddress());
                    }
                }
                curSensorParamsInfo = sensorHashMap.get(curItem.getSensorAddress());
                SensorType sensorType = curSensorParamsInfo.getSensorType();
                if (sensorType == SensorType.UNKNOWN_TYPE) {
                    ToastUtils.show("暂不支持此类型采集器！");
                    return;
                }
                Parcelable parcelableData = (Parcelable) curSensorParamsInfo.getSensorData();
                if (CollectorModel.value(collectorCode) == CollectorModel.VW08) {//振弦式传感器
                    DasExternalVibratingWireSensorActivity.startActivityForResultByFragment(mActivity, resultLauncher, addressList, curItem.getSensorAddress(), sensorType, parcelableData);
                } else { //数字式传感器
                    DasExternalDigitalSensorActivity.startActivityForResultByFragment(mActivity, resultLauncher, addressList, curItem.getSensorAddress(), sensorType, parcelableData);
                }
            }

            @Override
            public void addItem() {
                isAddSensor = true;
                addressList.clear();
                for (ExternalSensorItem item : mAdapter.getData()) {
                    if (!TextUtils.isEmpty(item.getSensorAddress())) {
                        addressList.add(item.getSensorAddress());
                    }
                }
                SensorType sensorType = defaultSensorParamsInfo.getSensorType();
                if (sensorType == SensorType.UNKNOWN_TYPE) {
                    ToastUtils.show("暂不支持此类型采集器！");
                    return;
                }
                Parcelable parcelableData = null;
                if (CollectorModel.value(collectorCode) == CollectorModel.VW08) {//振弦式传感器
                    DasExternalVibratingWireSensorActivity.startActivityForResultByFragment(mActivity, resultLauncher, addressList, null, sensorType, parcelableData);
                } else { //数字式传感器
                    DasExternalDigitalSensorActivity.startActivityForResultByFragment(mActivity, resultLauncher, addressList, null, sensorType, parcelableData);
                }
            }

            @Override
            public void deleteItem(int position) {
                if (!isConnected()) {
                    ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                if (mAdapter.getData().size() <= 1) {
                    ToastUtils.show("最少保留一个传感器!");
                    return;
                }
                warnDeleteSensorItem(position);
            }
        });
        mAdapter.setItemMax(maxSensorSum);
        mRecyclerViewSensor.setAdapter(mAdapter);
    }

    private void warnDeleteSensorItem(final int deleteItemIndex) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(requireContext())
                .title("温馨提示")
                .content("确定移除传感器?")
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        String address = mAdapter.getData().get(deleteItemIndex).getSensorAddress();
                        sensorHashMap.remove(address);
                        mAdapter.getData().remove(deleteItemIndex);
                        mAdapter.notifyItemRemoved(deleteItemIndex);
                        mAdapter.notifyItemRangeChanged(deleteItemIndex, mAdapter.getData().size() - deleteItemIndex);
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    private void clear() {
        accessSum = 0;
        sensorIndex = 0;
        curItem = null;
        sensorHashMap.clear();
        mAdapter.getData().clear();
        mAdapter.notifyDataSetChanged();
        mBtnSave.setEnabled(false);
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                clear();
                queryCollectorInfo();
                startDefaultProgress(null, AppContants.MsgWhat.MSG_SMART_REFRESH, DELAY_10000_MILLIS);
            }
        });
    }

    /**
     * 查询采集器配置信息
     */
    private void queryCollectorInfo() {
        CollectorConfigEntity collectorConfigEntity = new CollectorConfigEntity(collectorCode);
        String command = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_CONFIG, collectorConfigEntity);
        sendCommand(command);
        Timber.d("查询采集器配置信息===%s", command);
    }

    /**
     * 查询采集器接入的传感器配置信息
     */
    private void queryExtendSensorConfigInfo() {
        String address = StringUtil.formatStringTwo(String.valueOf(sensorIndex));
        CollectorSensorParamsEntity entity = new CollectorSensorParamsEntity(collectorCode, address);
        String command = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER, entity);
        sendCommand(command);
        Timber.d("获取 %s 采集器 %s 通道的传感器参数===%s", collectorName, address, command);
    }

    /**
     * 当接入的传感器个数为0时，设置采集器地址为0，关闭采集器
     */
    protected void sendCloseCollectorCmd() {
        SetCollectorAddressEntity collectorAddressEntity = new SetCollectorAddressEntity(0);
        String cmdCollectorAddress = CommandManager.getInstance().getCommand(CommandType.SET_COLLECTOR_ADDRESS, collectorAddressEntity);

        startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_20000_MILLIS);
        sendCommand(cmdCollectorAddress);
        Timber.d("设置采集器地址指令===%s", cmdCollectorAddress);
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confirm) {
            if (!isConnected()) {
                ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                return;
            }
            sendInstruction();
        }
    }

    protected abstract void sendInstruction();

    @Override
    protected void parseResponseMessage(String cmdStr) {
        // TODO #gh# 屏蔽从其他页面返回到当前页面时，接收到其他页面的最后接收到的指令数据(LiveData事件)
        if (!isResumed()) {
            return;
        }
        setResultData(cmdStr);
    }

    protected void setResultData(final String cmdStr) {
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case COLLECTOR_CONFIG://采集器配置信息 ##100
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s", "查询采集器参数出错!");
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    initEmptyDefaultCollectorSensorParamsInfo();
                    return;
                }
                initCollectorInfo(cmdStr);
                break;

            case COLLECTOR_CHANNEL_SENSOR_PARAMETER://获取XX采集器YY通道的传感器参数 ##101
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s", "查询传感器参数出错!");
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                //处理此通道的传感器配置参数
                processSensorParamsInfo(cmdStr);
                sensorIndex++;
                //还有待查询通道的传感器
                if (sensorIndex < accessSum) {
                    queryExtendSensorConfigInfo();
                } else {//所有通道的传感器参数都查询了
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(true);
                    }
                    if (mAdapter.getData().size() < 1) {
                        mBtnSave.setEnabled(false);
                    } else {
                        mBtnSave.setEnabled(true);
                    }
                    if (!sensorHashMap.values().isEmpty()) {
                        defaultSensorParamsInfo = (CollectorSensorParamsInfo) sensorHashMap.values().toArray()[0];
                    } else {
                        initEmptyDefaultCollectorSensorParamsInfo();
                    }
                }
                break;

            case SET_COLLECTOR_ADDRESS://当接入的传感器为0时，设置采集器地址为0
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("采集器地址配置错误!");
                    return;
                }
                doAfterSetting();
                break;

            case SAVE_CONFIG_INFO:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("保存参数指令错误!");
                    return;
                }
                ToastUtils.show("已保存!");
                break;

            default:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("指令出错!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                break;
        }
    }

    /**
     * 初始化采集器信息，根据接入的传感器数量遍历查询各个通道的传感器参数
     */
    private void initCollectorInfo(final String cmdStr) {
        collectorConfigInfo = ResultParserUtil.getEntityObject(cmdStr);
        if (null == collectorConfigInfo) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(false);
            }
            Timber.e("采集器配置信息为空!");
            collectorConfigInfo = new CollectorConfigInfo();
            return;
        }
        //采集器地址为 0 时，表示采集器未启用，不允许配置传感器，退出页面
        if ("0".equals(collectorConfigInfo.getCollectorAddress())) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(true);
            }
            collectorCloseWarn();
            return;
        }
        accessSum = collectorConfigInfo.getAccessSum();
        //接入传感器数量为0
        if (accessSum == 0) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(true);
            }
            initEmptyDefaultCollectorSensorParamsInfo();
            return;
        }
        //查询传感器配置信息前,重置accessNumFlag、sbcollectorSensor参数
        sensorIndex = 0;
        queryExtendSensorConfigInfo();
    }

    private void initEmptyDefaultCollectorSensorParamsInfo() {
        defaultSensorParamsInfo = new CollectorSensorParamsInfo();
        defaultSensorParamsInfo.setCollectorModel(CollectorModel.value(collectorCode));
        defaultSensorParamsInfo.setSensorData(null);
        if (CollectorModel.value(collectorCode) == CollectorModel.VW08) {
            defaultSensorParamsInfo.setSensorType(SensorType.KANG_PERCOLATE);
        } else {
            defaultSensorParamsInfo.setSensorType(SensorType.value(collectorCode));
        }
    }

    /**
     * 处理XX采集器YY通道的传感器参数
     */
    private void processSensorParamsInfo(String cmdStr) {
        CollectorSensorParamsInfo mCollectorParamsInfoSub = ResultParserUtil.getEntityObject(cmdStr);
        if (mCollectorParamsInfoSub == null) {
            Timber.e("%s 采集器 %s 通道的传感器参数为空!", collectorCode, StringUtil.formatStringTwo(String.valueOf(sensorIndex)));
            return;
        }
        Timber.d("%s 采集器 %s 通道的传感器参数-------%s", collectorCode, StringUtil.formatStringTwo(String.valueOf(sensorIndex)), mCollectorParamsInfoSub.toString());
        sensorHashMap.put(mCollectorParamsInfoSub.getSensorAddress(), mCollectorParamsInfoSub);

        ExternalSensorItem item = new ExternalSensorItem(mCollectorParamsInfoSub.getSensorAddress());
        item.setVibratingWireSensor(isVibratingWireSensor);
        mAdapter.getData().add(item);
        mAdapter.notifyItemInserted(mAdapter.getData().size());
    }

    protected void doAfterSetting() {
        stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
//        isExitMode = true;
        saveConfigInfoNoReboot();
    }

    protected void collectorCloseWarn() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getActivity());
        mBuilder.title("温馨提示：")
                .content("采集器地址为0，无法配置扩展传感器，请先修改采集器地址")
                .contentColorRes(R.color.title_text_color)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .positiveColorRes(R.color.blue_52B4F8)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        mActivity.finish();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
}
