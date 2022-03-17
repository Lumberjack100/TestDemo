package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.hjq.toast.ToastUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.das.DasCollectorEntity;
import com.shmedo.configlibrary.iot.cmd.entity.das.DasExternalSensorEntity;
import com.shmedo.configlibrary.iot.cmd.entity.das.IndexEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCollectorModel;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.IOTSensorType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.das.DasCollectorInfo;
import com.shmedo.configlibrary.iot.model.das.DasExternalSensorInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.DasExternalSensorConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.adapter.DASSensorAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.DASSensorItem;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/16 <br/>
 * 描述：   DAS扩展传感器配置页面
 */
public class NetDasExternalSensorListFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.recyclerview_sensor)
    RecyclerView mRecyclerViewSensor;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    private DASSensorAdapter sensorAdapter;
    private List<DASSensorItem> sensorItemList = new ArrayList<>();
    private DASSensorItem curSensorItem;

    //以传感器的通道号为 Key,DasExternalSensorInfo 对象为 Value
    private HashMap<String, DasExternalSensorInfo> sensorHashMap = new HashMap<>();
    private ArrayList<String> addressList = new ArrayList<>();

    private DasCollectorInfo collectorInfo;
    private int accessSum = 0;  //接入扩展传感器总数
    private int sensorIndex = 0;//接入的传感器索引号
    private int curItemPosition = 0;
    private boolean isEnableNewSensor = false;//是启用新传感器还是编辑现有传感器
    private boolean isVibratingWireSensor = false;//是否振弦式传感器
    private ActivityResultLauncher<Intent> resultLauncher;


    public static NetDasExternalSensorListFragment newInstance(DeviceInfo deviceInfo) {
        NetDasExternalSensorListFragment fragment = new NetDasExternalSensorListFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent intent = result.getData();
                            DasExternalSensorInfo sensorInfo = (DasExternalSensorInfo) intent.getSerializableExtra(AppContants.Extras.SENSOR_PARAM);
                            String sensorAddress = sensorInfo.getAddr();
                            if (isEnableNewSensor) {
                                sensorHashMap.put(sensorAddress, sensorInfo);
                                sensorItemList.remove(sensorItemList.size() - 1);
                                DASSensorItem sensorItem = new DASSensorItem(R.drawable.ic_sensor_holder_bright, false, sensorAddress);
                                sensorItem.setVibratingWireSensor(isVibratingWireSensor);
                                sensorItemList.add(sensorItem);
                                if (sensorItemList.size() < 8) {
                                    sensorItem = new DASSensorItem(R.drawable.ic_add_sensor, true);
                                    sensorItemList.add(sensorItem);
                                }
                                sensorAdapter.notifyItemRangeChanged(sensorItemList.size() - 2, 21);
                                if (sensorItemList.size() > 1) {
                                    mBtnSave.setEnabled(true);
                                }
                            } else {
                                sensorHashMap.remove(curSensorItem.getSensorAddress());
                                sensorHashMap.put(sensorAddress, sensorInfo);
                                curSensorItem.setSensorAddress(sensorAddress);
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
        initExtendSensorAdapter();
        initRefreshLayout();
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        mRefreshLayout.autoRefresh();
        initDefaultSensorItem();
    }

    private void initExtendSensorAdapter() {
        int spanCount = 4;//跟布局里面的spanCount属性是一致的
        int spacing = ConvertUtils.dp2px(15);//每一个矩形的间距
        mRecyclerViewSensor.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        //设置每个item间距
        mRecyclerViewSensor.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        sensorAdapter = new DASSensorAdapter(sensorItemList);
        sensorAdapter.setAnimationEnable(false);
        sensorAdapter.setAnimationFirstOnly(false);
        sensorAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (collectorInfo == null || TextUtils.isEmpty(collectorInfo.getType())) {
                    ToastUtils.show("未获取到采集器信息，请先刷新");
                    return;
                }
                processItemClick(position);
            }
        });
        sensorAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                curItemPosition = position;
                DASSensorItem sensorItem = sensorItemList.get(position);
                if (sensorItem.isAddButton()) {
                    return true;
                }
                if (sensorItemList.size() <= 2) {
                    ToastUtils.show("最少保留一个传感器!");
                    return true;
                }
                warnDeleteSensorItem();
                return true;
            }
        });
        mRecyclerViewSensor.setAdapter(sensorAdapter);
    }

    private void processItemClick(int position) {
        addressList.clear();
        for (DASSensorItem item : sensorItemList) {
            if (!TextUtils.isEmpty(item.getSensorAddress())) {
                addressList.add(item.getSensorAddress());
            }
        }
        curItemPosition = position;
        curSensorItem = sensorItemList.get(position);
        isEnableNewSensor = curSensorItem.isAddButton();
        IOTSensorType sensorType = null;
        DasExternalSensorInfo dasExternalSensorInfo = null;
        if (curSensorItem.isAddButton()) {
            if (sensorHashMap.values().size() == 0) {
                sensorType = IOTSensorType.getSensorTypeByCollectorCode(collectorInfo.getType());
            } else {
                DasExternalSensorInfo sensorInfo = (DasExternalSensorInfo) sensorHashMap.values().toArray()[0];
                sensorType = sensorInfo.getType().equals("0") ? IOTSensorType.getSensorTypeByCollectorCode(collectorInfo.getType()) : IOTSensorType.value(sensorInfo.getType());
            }
        } else {
            dasExternalSensorInfo = sensorHashMap.get(curSensorItem.getSensorAddress());
            sensorType = dasExternalSensorInfo.getType().equals("0") ? IOTSensorType.getSensorTypeByCollectorCode(collectorInfo.getType()) : IOTSensorType.value(dasExternalSensorInfo.getType());
        }
        DasExternalSensorConfigActivity.startActivity(mActivity, resultLauncher, deviceInfo, collectorInfo.getType(), sensorType, addressList, dasExternalSensorInfo);
    }

    private void warnDeleteSensorItem() {
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
                        removeSensor();
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
        sensorHashMap.clear();
        addressList.clear();
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                queryCollectorInfo();
            }
        });
    }

    /**
     * 查询采集器配置信息
     */
    private void queryCollectorInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 查询采集器接入的传感器配置信息
     */
    private void queryExtendSensorConfigInfo() {
        IndexEntity entity = new IndexEntity(sensorIndex);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_EXTERNAL_SENSOR, entity);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 移除传感器
     */
    private void removeSensor() {
        IndexEntity entity = new IndexEntity(curItemPosition);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_DEL_EXTERNAL_SENSOR, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 当接入的传感器个数为0时，设置采集器地址为0，关闭采集器
     */
    private void closeCollector() {
        DasCollectorEntity entity = new DasCollectorEntity();
        entity.setType(collectorInfo.getType());
        entity.setAddr("0");

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_COLLECTOR_CONTROL, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    private void setCollector() {
        DasCollectorEntity entity = new DasCollectorEntity();
        entity.setType(collectorInfo.getType());
        entity.setSensornum(sensorHashMap.values().size() + "");

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_COLLECTOR_CONTROL, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 设置采集器接入的传感器配置信息
     */
    private void setExtendSensorConfigInfo(DasExternalSensorInfo externalSensorInfo) {
        DasExternalSensorEntity entity = new DasExternalSensorEntity();
        entity.setIndex(String.valueOf(sensorIndex));
        entity.setType(externalSensorInfo.getType());
        entity.setAddr(externalSensorInfo.getAddr());
        entity.setThreshold(externalSensorInfo.getThreshold());
        entity.setCorrval(externalSensorInfo.getCorrval());
        switch (IOTSensorType.value(externalSensorInfo.getType())) {
            case KANG_PERCOLATE://基康渗压计
                entity.setTubealti(externalSensorInfo.getTubealti());
                entity.setRopelen(externalSensorInfo.getRopelen());
                entity.setPoly_a(externalSensorInfo.getPoly_a());
                entity.setPoly_b(externalSensorInfo.getPoly_b());
                entity.setPoly_c(externalSensorInfo.getPoly_c());
                entity.setTemp_k(externalSensorInfo.getTemp_k());
                entity.setTemp_t0(externalSensorInfo.getTemp_t0());
                break;

            case GUDAN_PERCOLATE://葛南渗压计
                entity.setTubealti(externalSensorInfo.getTubealti());
                entity.setRopelen(externalSensorInfo.getRopelen());
                entity.setSens_k(externalSensorInfo.getSens_k());
                entity.setTemp_b(externalSensorInfo.getTemp_b());
                entity.setTemp_t0(externalSensorInfo.getTemp_t0());
                entity.setReferval_f(externalSensorInfo.getReferval_f());
                break;

            case GUDAN_SOIL_PRESSURE://葛南土压力计
                entity.setSens_k(externalSensorInfo.getSens_k());
                entity.setTemp_b(externalSensorInfo.getTemp_b());
                entity.setTemp_t0(externalSensorInfo.getTemp_t0());
                entity.setReferval_f(externalSensorInfo.getReferval_f());
                break;

            case GUDAN_STRESS://葛南应力计
                entity.setSens_k(externalSensorInfo.getSens_k());
                entity.setTemp_b(externalSensorInfo.getTemp_b());
                entity.setTemp_t0(externalSensorInfo.getTemp_t0());
                entity.setReferval_f(externalSensorInfo.getReferval_f());
                entity.setElastic_mod(externalSensorInfo.getElastic_mod());
                break;

            case JUNXING_ZLJ_300T://轴力计
                entity.setSens_k(externalSensorInfo.getSens_k());
                entity.setTemp_b(externalSensorInfo.getTemp_b());
                entity.setReferval_f(externalSensorInfo.getReferval_f());
                entity.setTemp_t0(externalSensorInfo.getTemp_t0());
                break;

            case INCLINOMETER://固定测斜仪
                entity.setSpacing(externalSensorInfo.getSpacing());
                entity.setHolenum(externalSensorInfo.getHolenum());
                break;

            case LUYAN_INCLINOMETER://倾角仪
                entity.setInitvalx(externalSensorInfo.getInitvalx());
                entity.setInitvaly(externalSensorInfo.getInitvaly());
                break;

            case WEIR://量水堰计
                entity.setLsycsds(externalSensorInfo.getLsycsds());
                entity.setLsyysst(externalSensorInfo.getLsyysst());
                break;

            case STATIC_LEVEL://静力水准
                entity.setTubealti(externalSensorInfo.getTubealti());
                break;

            case DIGITAL_WATER_LEVEL_GAUGE://数字式水位计
                entity.setTubealti(externalSensorInfo.getTubealti());
                entity.setRopelen(externalSensorInfo.getRopelen());
                break;

            default:
                break;
        }
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_EXTERNAL_SENSOR, entity);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.btn_confirm) {
            processSave();
        }
    }

    private void processSave() {
        if (sensorHashMap.values().isEmpty()) {
            Timber.i("采集器接入的传感器信息为空,关闭采集器");
            closeCollector();
            return;
        }
        setCollector();
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String
            cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(false);
            }
            dismissWaitDialog();
            ToastUtils.show("下发指令失败");
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
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(false);
        }
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
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(false);
        }
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
            case DAS_MD_GET_COLLECTOR_CONTROL: {//
                IOTCommandResult<DasCollectorInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s %s", "查询采集器参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                clear();
                collectorInfo = commandResult.getResult();
                initCollectorInfo();
            }
            break;

            case DAS_MD_GET_EXTERNAL_SENSOR: {//获取传感器的参数
                IOTCommandResult<DasExternalSensorInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s %s", "查询传感器参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                //处理此通道的传感器配置参数
                processSensorParamsInfo(commandResult.getResult());
                sensorIndex++;
                //还有待查询通道的传感器
                if (sensorIndex < accessSum) {
                    queryExtendSensorConfigInfo();
                } else {//所有接入的传感器参数都查询了
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(true);
                    }
                    if (sensorItemList.size() < 8) {
                        DASSensorItem sensorItem = new DASSensorItem(R.drawable.ic_add_sensor, true);
                        sensorItemList.add(sensorItem);
                        sensorAdapter.notifyItemInserted(sensorItemList.size() - 1);
                    }
                    if (sensorItemList.size() <= 1) {
                        mBtnSave.setEnabled(false);
                    } else {
                        mBtnSave.setEnabled(true);
                    }
                }
            }
            break;

            case DAS_MD_SET_COLLECTOR_CONTROL: {//
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置采集器出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                if (sensorHashMap.values().isEmpty()) {
                    dismissWaitDialog();
                    ToastUtils.show("保存成功");
                } else {
                    sensorIndex = 0;
                    setExtendSensorConfigInfo((DasExternalSensorInfo) sensorHashMap.values().toArray()[sensorIndex]);
                }
            }
            break;

            case DAS_MD_SET_EXTERNAL_SENSOR: {//
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "保存传感器参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                sensorIndex++;
                //还有待保存通道的传感器
                if (sensorIndex < sensorHashMap.values().size()) {
                    setExtendSensorConfigInfo((DasExternalSensorInfo) sensorHashMap.values().toArray()[sensorIndex]);

                } else {
                    dismissWaitDialog();
                    ToastUtils.show("保存成功");
                }
            }
            break;

            case DAS_MD_DEL_EXTERNAL_SENSOR: {//移除传感器
                dismissWaitDialog();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "移除传感器出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                String address = sensorItemList.get(curItemPosition).getSensorAddress();
                sensorHashMap.remove(address);
                sensorItemList.remove(curItemPosition);
                sensorAdapter.notifyItemRemoved(curItemPosition);
                if (sensorItemList.size() <= 1) {
                    mBtnSave.setEnabled(false);
                }
            }
            break;

            default:
                break;
        }
    }

    /**
     * 初始化采集器信息，根据接入的传感器数量遍历查询各个通道的传感器参数
     */
    private void initCollectorInfo() {
        if (collectorInfo == null) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(false);
            }
            Timber.e("DasCollectorInfo 为空!");
            collectorInfo = new DasCollectorInfo();
            return;
        }
        //采集器地址为 0 时，表示采集器未启用，不允许配置传感器，退出页面
        if (collectorInfo.getAddr().equals("0")) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(true);
            }
            collectorCloseWarn();
            return;
        }
        if (IOTCollectorModel.value(collectorInfo.getType()) == IOTCollectorModel.VW08) {//振弦式传感器
            isVibratingWireSensor = true;
        }
        accessSum = Integer.parseInt(collectorInfo.getSensornum());
        //接入传感器数量为0
        if (accessSum == 0) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(true);
            }
            initDefaultSensorItem();
            return;
        }
        sensorIndex = 0;
        sensorItemList.clear();
        queryExtendSensorConfigInfo();
    }

    private void collectorCloseWarn() {
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

    private void initDefaultSensorItem() {
        sensorItemList.clear();
        DASSensorItem sensorItem = new DASSensorItem(R.drawable.ic_add_sensor, true);
        sensorItemList.add(sensorItem);
        sensorAdapter.notifyDataSetChanged();

        mBtnSave.setEnabled(false);
    }

    /**
     * 处理获取到的单个传感器参数信息
     *
     * @param sensorInfo
     */
    private void processSensorParamsInfo(DasExternalSensorInfo sensorInfo) {
        if (sensorInfo == null)
            return;

        sensorHashMap.put(sensorInfo.getAddr(), sensorInfo);

        DASSensorItem sensorItem = new DASSensorItem(R.drawable.ic_sensor_holder_bright, false, sensorInfo.getAddr());
        sensorItem.setVibratingWireSensor(isVibratingWireSensor);
        sensorItemList.add(sensorItem);
        sensorAdapter.notifyDataSetChanged();
    }
}