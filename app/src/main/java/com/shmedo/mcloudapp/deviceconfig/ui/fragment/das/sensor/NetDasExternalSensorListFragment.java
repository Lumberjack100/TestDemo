package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

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
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.hjq.toast.ToastUtils;
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
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.DasExternalSensorConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.projects.adapter.DASSensorAdapter;
import com.shmedo.mcloudapp.projects.model.DASSensorItem;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

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

    @BindView(R.id.recyclerview_sensor)
    RecyclerView mRecyclerViewSensor;

    private DASSensorAdapter sensorAdapter;
    private List<DASSensorItem> sensorItemList = new ArrayList<>();
    private DASSensorItem curSensorItem;

    private String collectorName;
    private String collectorCode;//采集器类型
    private int accessSum = 0;  //接入扩展传感器总数
    private int sensorIndex = 0;//接入的传感器索引号
    private ActivityResultLauncher<Intent> resultLauncher;

    //以传感器的通道号为 Key,TerminalSensorInfo 对象为 Value
    private HashMap<String, DasExternalSensorInfo> sensorHashMap = new HashMap<>();
    private List<DasExternalSensorInfo> sensorInfoList = new ArrayList<>();

    private boolean isEnableNewSensor = false;//是启用新传感器还是编辑现有传感器

    private DasCollectorInfo collectorInfo;
    private ArrayList<String> addressList = new ArrayList<>();


    public static NetDasExternalSensorListFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetDasExternalSensorListFragment fragment = new NetDasExternalSensorListFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
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
                                DASSensorItem sensorItem = new DASSensorItem(R.drawable.ic_sensor_holder_bright);
                                sensorItem.setSensorAddress(sensorAddress);
                                sensorItemList.add(sensorItem);
                                if (sensorItemList.size() < 8) {
                                    sensorItem = new DASSensorItem(R.drawable.ic_add_sensor, true);
                                    sensorItemList.add(sensorItem);
                                }
                                sensorAdapter.notifyDataSetChanged();
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initExtendSensorAdapter();
        queryCollectorInfo();
    }

    private void initExtendSensorAdapter() {
        int spanCount = 4;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(getActivity(), 15);//每一个矩形的间距
        mRecyclerViewSensor.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        //设置每个item间距
        mRecyclerViewSensor.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        sensorAdapter = new DASSensorAdapter(sensorItemList);
        sensorAdapter.setAnimationEnable(true);
        sensorAdapter.setAnimationFirstOnly(false);
        sensorAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                processItemClick(position);
            }
        });
        sensorAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                DASSensorItem sensorItem = sensorItemList.get(position);
                if (sensorItem.isAddButton()) {
                    return true;
                }
                warnDeleteSensorItem(position);
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
        curSensorItem = sensorItemList.get(position);
        DasExternalSensorInfo sensorInfo = null;
        if (curSensorItem.isAddButton()) {
            isEnableNewSensor = true;
            IOTSensorType sensorType = getSensorTypeByCollectorCode(collectorCode);
            sensorInfo = new DasExternalSensorInfo();
            sensorInfo.setType(sensorType.toString());
        } else {
            isEnableNewSensor = false;
            sensorInfo = sensorHashMap.get(curSensorItem.getSensorAddress());
        }

        DasExternalSensorConfigActivity.startActivity(mActivity, resultLauncher, projectDeviceInfo, collectorCode, addressList, sensorInfo);
    }

    private void warnDeleteSensorItem(int position) {
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
                        sensorHashMap.remove(address);
                        sensorItemList.remove(position);
                        sensorAdapter.notifyDataSetChanged();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /**
     * 查询采集器配置信息
     */
    private void queryCollectorInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL);
        showProgressDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 查询采集器接入的传感器配置信息
     */
    private void queryExtendSensorConfigInfo() {
        IndexEntity entity = new IndexEntity(sensorIndex);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_EXTERNAL_SENSOR, entity);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 当接入的传感器个数为0时，设置采集器地址为0，关闭采集器
     */
    private void closeCollector() {
        DasCollectorEntity entity = new DasCollectorEntity();
        entity.setType(collectorInfo.getType());
        entity.setAddr("0");

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_COLLECTOR_CONTROL, entity);
        showProgressDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
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
        entity.setSpacing(externalSensorInfo.getSpacing());
        entity.setHolenum(externalSensorInfo.getHolenum());
        entity.setTubealti(externalSensorInfo.getTubealti());
        entity.setRopelen(externalSensorInfo.getRopelen());
        entity.setPoly_a(externalSensorInfo.getPoly_a());
        entity.setPloy_b(externalSensorInfo.getPloy_b());
        entity.setPloy_c(externalSensorInfo.getPloy_c());
        entity.setTemp_k(externalSensorInfo.getTemp_k());
        entity.setTemp_t0(externalSensorInfo.getTemp_t0());
        entity.setSens_k(externalSensorInfo.getSens_k());
        entity.setTemp_b(externalSensorInfo.getTemp_b());
        entity.setReferval_f(externalSensorInfo.getReferval_f());

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_EXTERNAL_SENSOR, entity);
        showProgressDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
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
        sensorInfoList.clear();
        sensorInfoList.addAll(sensorHashMap.values());
        if (sensorInfoList.isEmpty()) {
            Timber.i("采集器接入的传感器信息为空,关闭采集器");
            closeCollector();
            return;
        }

        sensorIndex = 0;
        showProgressDialog("处理中...");
        setExtendSensorConfigInfo(sensorInfoList.get(sensorIndex));

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
            showDispatchFailedDialog();
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
    private void showDispatchFailedDialog() {
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
            case DAS_MD_GET_COLLECTOR_CONTROL: {//
                IOTCommandResult<DasCollectorInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    dismissProgressDialog();
                    String errMsg = String.format("%s %s", "查询采集器参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                collectorInfo = commandResult.getResult();
                initCollectorInfo();
            }
            break;

            case DAS_MD_GET_EXTERNAL_SENSOR: {//获取传感器的参数
                IOTCommandResult<DasExternalSensorInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    dismissProgressDialog();
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
                } else {//所有通道的传感器参数都查询了
                    dismissProgressDialog();
                    if (sensorItemList.size() < 8) {
                        DASSensorItem sensorItem = new DASSensorItem(R.drawable.ic_add_sensor, true);
                        sensorItemList.add(sensorItem);
                        sensorAdapter.notifyDataSetChanged();
                    }
                }
            }
            break;

            case DAS_MD_SET_COLLECTOR_CONTROL: {//
                dismissProgressDialog();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "关闭采集器出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                doAfterSetting();
            }
            break;

            case DAS_MD_SET_EXTERNAL_SENSOR: {//
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissProgressDialog();
                    String errMsg = String.format("%s %s", "保存传感器参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                sensorIndex++;
                //还有待保存通道的传感器
                if (sensorIndex < sensorInfoList.size()) {
                    setExtendSensorConfigInfo(sensorInfoList.get(sensorIndex));

                } else {
                    dismissProgressDialog();
                    ToastUtils.show("保存成功");
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
            dismissProgressDialog();
            Timber.e("DasCollectorInfo 为空!");
            collectorInfo = new DasCollectorInfo();
            return;
        }
        //采集器地址为 0 时，表示采集器未启用，不允许配置传感器，退出页面
        if (collectorInfo.getAddr().equals("0")) {
            dismissProgressDialog();
            collectorCloseWarn();
            return;
        }
        collectorCode = collectorInfo.getType();
        accessSum = Integer.parseInt(collectorInfo.getSensornum());
        //接入传感器数量为0
        if (accessSum == 0) {
            dismissProgressDialog();
            initDefaultSensorItem();
            return;
        }
        sensorIndex = 0;
        queryExtendSensorConfigInfo();
    }

    private void initDefaultSensorItem() {
        sensorItemList.clear();
        DASSensorItem sensorItem = new DASSensorItem(R.drawable.ic_add_sensor, true);
        sensorItemList.add(sensorItem);
        sensorAdapter.notifyDataSetChanged();
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
        addSensorItem(sensorInfo.getAddr());
    }

    private void addSensorItem(String address) {
        DASSensorItem sensorItem = new DASSensorItem(R.drawable.ic_sensor_holder_bright);
        sensorItem.setSensorAddress(address);
        sensorItemList.add(sensorItem);
        sensorAdapter.notifyDataSetChanged();
    }

    private void doAfterSetting() {
        ToastUtils.show("保存成功");

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

    private IOTSensorType getSensorTypeByCollectorCode(String code) {

        switch (IOTCollectorModel.value(code)) {
            case VW08:
                return IOTSensorType.KANG_PERCOLATE;

            case RAIN08:
                return IOTSensorType.RAIN_GAUGE;

            case DS08:
                return IOTSensorType.WIRE_SHIFT;

            case HD08:
                return IOTSensorType.SOIL_MOISTURE;

            case CX08:
                return IOTSensorType.INCLINOMETER;

            case UDS08:
                return IOTSensorType.ULTRASONIC_LEVEL_GAUGE;

            case RD08:
                return IOTSensorType.RADAR_LEVEL_GAUGE;

            case CS08:
                return IOTSensorType.INFRASOUND_SENSOR;

            default:
                return null;
        }
    }


}