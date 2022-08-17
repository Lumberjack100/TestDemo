package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.ConvertUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
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
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.sensor.DasExternalDigitalSensorActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.sensor.DasExternalVibratingWireSensorActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BaseBleCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.adapter.DASSensorAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.DASSensorItem;

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
    private static final int REQUEST_CODE_SENSOR_CONFIG = 0x0102;

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.recyclerview_sensor)
    RecyclerView mRecyclerViewSensor;

    private DASSensorAdapter sensorAdapter;
    private List<DASSensorItem> sensorItemList = new ArrayList<>();
    private DASSensorItem curSensorItem;

    protected String collectorName;
    protected String collectorCode;//采集器类型
    private int accessSum = 0;              //接入扩展传感器总数
    protected int sensorIndex = 0;//接入的传感器索引号

    protected List<CollectorSensorParamsInfo> collectorSensorParamsInfoSubs = new ArrayList<>();
    //以传感器的通道号为 Key,CollectorSensorParamsInfo 对象为 Value
    protected HashMap<String, CollectorSensorParamsInfo> collectorSensorHashMap = new HashMap<>();
    protected CollectorSensorParamsInfo defaultCollectorSensorParamsInfo = new CollectorSensorParamsInfo();
    private CollectorSensorParamsInfo curCollectorSensorParamsInfo;
    private String curSensorAddress;
    private ArrayList<String> addressList = new ArrayList<>();
    private boolean isEnableNewSensor = false;//是启用新传感器还是编辑现有传感器
    private boolean isVibratingWireSensor = false;//是否振弦式传感器

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
    }

    @Override
    protected int getLayoutId() {
        return R.layout.net_das_external_sensor_list_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initExtendSensorAdapter();
        initRefreshLayout();
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        mRefreshLayout.autoRefresh();
    }

    private void initExtendSensorAdapter() {
        int spanCount = 4;//跟布局里面的spanCount属性是一致的
        int spacing = ConvertUtils.dp2px(15);//每一个矩形的间距
        mRecyclerViewSensor.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        //设置每个item间距
        mRecyclerViewSensor.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        sensorAdapter = new DASSensorAdapter(sensorItemList);
        sensorAdapter.setAnimationEnable(true);
        sensorAdapter.setAnimationFirstOnly(false);
        sensorAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                processItemClick(position);
            }
        });
        sensorAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return true;
                }
                DASSensorItem sensorItem = sensorItemList.get(position);
                if (sensorItem == null || sensorItem.isAddButton()) {
                    return true;
                }
                if (sensorItemList.size() <= 2) {
                    ToastUtils.show("最少保留一个传感器!");
                    return true;
                }
                warnDeleteSensorItem(position);
                return true;
            }
        });
        mRecyclerViewSensor.setAdapter(sensorAdapter);
    }

    private void processItemClick(int position) {
        Parcelable parcelableData;
        SensorType sensorType;
        curSensorItem = sensorItemList.get(position);
        curSensorAddress = curSensorItem.getSensorAddress();

        addressList.clear();
        for (DASSensorItem item : sensorItemList) {
            if (!TextUtils.isEmpty(item.getSensorAddress())) {
                addressList.add(item.getSensorAddress());
            }
        }
        if (curSensorItem.isAddButton()) {
            isEnableNewSensor = true;
            sensorType = defaultCollectorSensorParamsInfo.getSensorType();
            parcelableData = null;
        } else {
            isEnableNewSensor = false;
            curCollectorSensorParamsInfo = collectorSensorHashMap.get(curSensorAddress);
            sensorType = curCollectorSensorParamsInfo.getSensorType();
            parcelableData = (Parcelable) curCollectorSensorParamsInfo.getSensorData();
        }
        if (sensorType == SensorType.UNKNOWN_TYPE) {
            ToastUtils.show("暂不支持此类型采集器！");
            return;
        }

        if (CollectorModel.value(collectorCode) == CollectorModel.VW08) {//振弦式传感器
            DasExternalVibratingWireSensorActivity.startActivityForResultByFragment(this, REQUEST_CODE_SENSOR_CONFIG, addressList, curSensorAddress, sensorType, parcelableData);

        } else { //数字式传感器
            DasExternalDigitalSensorActivity.startActivityForResultByFragment(this, REQUEST_CODE_SENSOR_CONFIG, addressList, curSensorAddress, sensorType, parcelableData);
        }
    }

    private void warnDeleteSensorItem(final int position) {
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
                        String address = sensorItemList.get(position).getSensorAddress();
                        collectorSensorHashMap.remove(address);
                        sensorItemList.remove(position);
                        sensorAdapter.notifyItemRemoved(position);
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    private void clear() {
        accessSum = 0;
        sensorIndex = 0;
        sensorItemList.clear();
        curSensorItem = null;
        collectorSensorHashMap.clear();
        addressList.clear();
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
        String address = StringUtil.formatStringTwo(sensorIndex + "");
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
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
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
                    Timber.e("查询采集器配置信息指令出错!");
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    initDefaultSensorItems();
                    initEmptyDefaultCollectorSensorParamsInfo();
                    return;
                }
                CollectorConfigInfo collectorConfigInfo = ResultParserUtil.getEntityObject(cmdStr);
                if (collectorConfigInfo == null) {
                    Timber.e("采集器配置信息为空!");
                } else {
                    if (collectorConfigInfo.getCollectorAddress().equals("0")) {
                        if (mRefreshLayout.isRefreshing()) {
                            mRefreshLayout.finishRefresh(true);
                        }
                        collectorCloseWarn();
                        return;
                    }
                    accessSum = collectorConfigInfo.getAccessSum();
                }
                if (accessSum == 0) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(true);
                    }
                    initDefaultSensorItems();
                    initEmptyDefaultCollectorSensorParamsInfo();
                    return;
                }
                //查询传感器配置信息前,重置accessNumFlag、sbcollectorSensor参数
                sensorIndex = 0;
                queryExtendSensorConfigInfo();
                break;

            case COLLECTOR_CHANNEL_SENSOR_PARAMETER://获取XX采集器YY通道的传感器参数 ##101
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    Timber.e("查询采集器配置信息指令出错!");
                    return;
                }
                //处理此通道的传感器配置参数
                processCollectorSensorParamsInfo(cmdStr);
                sensorIndex++;
                //还有待查询通道的传感器
                if (sensorIndex < accessSum) {
                    queryExtendSensorConfigInfo();
                } else {//所有通道的传感器参数都查询了
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(true);
                    }
                    if (sensorItemList.size() < 8) {
                        DASSensorItem sensorItem = new DASSensorItem(R.drawable.ic_add_sensor, true);
                        sensorItemList.add(sensorItem);
                    }
                    sensorAdapter.notifyDataSetChanged();
                    if (!collectorSensorHashMap.values().isEmpty()) {
                        defaultCollectorSensorParamsInfo = (CollectorSensorParamsInfo) collectorSensorHashMap.values().toArray()[0];
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

    private void initEmptyDefaultCollectorSensorParamsInfo() {
        defaultCollectorSensorParamsInfo = new CollectorSensorParamsInfo();
        defaultCollectorSensorParamsInfo.setCollectorModel(CollectorModel.value(collectorCode));
        defaultCollectorSensorParamsInfo.setSensorData(null);
        if (CollectorModel.value(collectorCode) == CollectorModel.VW08) {
            defaultCollectorSensorParamsInfo.setSensorType(SensorType.KANG_PERCOLATE);
        } else {
            defaultCollectorSensorParamsInfo.setSensorType(SensorType.value(collectorCode));
        }
    }

    /**
     * 处理XX采集器YY通道的传感器参数
     */
    private void processCollectorSensorParamsInfo(String cmdStr) {
        CollectorSensorParamsInfo mCollectorParamsInfoSub = ResultParserUtil.getEntityObject(cmdStr);
        if (mCollectorParamsInfoSub == null) {
            Timber.e("%s 采集器 %s 通道的传感器参数为空!", collectorCode, StringUtil.formatStringTwo(sensorIndex + ""));
            return;
        }
        Timber.d("%s 采集器 %s 通道的传感器参数-------%s", collectorCode, StringUtil.formatStringTwo(sensorIndex + ""), mCollectorParamsInfoSub.toString());
        collectorSensorHashMap.put(mCollectorParamsInfoSub.getSensorAddress(), mCollectorParamsInfoSub);
        addSensorItem(mCollectorParamsInfoSub.getSensorAddress());
    }

    private void initDefaultSensorItems() {
        sensorItemList.clear();
        DASSensorItem sensorItem = new DASSensorItem(R.drawable.ic_add_sensor, true);
        sensorItemList.add(sensorItem);
        sensorAdapter.notifyDataSetChanged();
    }

    private void addSensorItem(String address) {
        DASSensorItem sensorItem = new DASSensorItem(R.drawable.ic_sensor_holder_bright, false, address);
        sensorItem.setVibratingWireSensor(isVibratingWireSensor);
        sensorItemList.add(sensorItem);
        sensorAdapter.notifyItemInserted(sensorItemList.size() - 1);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode != Activity.RESULT_OK)
            return;

        switch (requestCode) {
            case REQUEST_CODE_SENSOR_CONFIG:
                if (intent != null) {
                    Parcelable parcelableData = intent.getParcelableExtra(AppContants.Extras.SENSOR_PARAM);
                    String sensorAddress = intent.getStringExtra(AppContants.Extras.SENSOR_ADDRESS);
                    SensorType sensorType = (SensorType) intent.getSerializableExtra(AppContants.Extras.SENSOR_TYPE);

                    if (isEnableNewSensor) {
                        CollectorSensorParamsInfo collectorSensorParamsInfoSub = new CollectorSensorParamsInfo();
                        collectorSensorParamsInfoSub.setCollectorModel(defaultCollectorSensorParamsInfo.getCollectorModel());
//                        collectorSensorParamsInfoSub.setChannelNumber(curChannelNumber);
                        collectorSensorParamsInfoSub.setSensorAddress(sensorAddress);
                        collectorSensorParamsInfoSub.setSensorType(sensorType);
                        collectorSensorParamsInfoSub.setSensorData(parcelableData);
                        collectorSensorHashMap.put(sensorAddress, collectorSensorParamsInfoSub);

                        sensorItemList.remove(sensorItemList.size() - 1);
                        DASSensorItem sensorItem = new DASSensorItem(R.drawable.ic_sensor_holder_bright, false, sensorAddress);
                        sensorItem.setVibratingWireSensor(isVibratingWireSensor);
                        sensorItemList.add(sensorItem);
                        if (sensorItemList.size() < 8) {
                            sensorItem = new DASSensorItem(R.drawable.ic_add_sensor, true);
                            sensorItemList.add(sensorItem);
                        }
                        sensorAdapter.notifyDataSetChanged();

                    } else {
                        collectorSensorHashMap.remove(curSensorAddress);
                        curCollectorSensorParamsInfo.setSensorAddress(sensorAddress);
                        curCollectorSensorParamsInfo.setSensorType(sensorType);
                        curCollectorSensorParamsInfo.setSensorData(parcelableData);
                        collectorSensorHashMap.put(sensorAddress, curCollectorSensorParamsInfo);
                        curSensorItem.setSensorAddress(sensorAddress);
                        curSensorAddress = sensorAddress;
                    }
                }
                break;
        }
    }
}
