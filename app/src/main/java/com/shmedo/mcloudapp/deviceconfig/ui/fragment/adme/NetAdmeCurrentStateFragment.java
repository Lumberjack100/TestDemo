package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ColorUtils;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.AdmeModuleErrorType;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.adme.AdmeCurrentStateInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
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
public class NetAdmeCurrentStateFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    /**
     * 基本信息
     */
    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_product_number)
    TextView mTvProductNumber;

    @BindView(R.id.tv_sim_card_number)
    TextView mTVSimCardNumber;

    @BindView(R.id.tv_imei_number)
    TextView mTvImeiNumber;

    @BindView(R.id.tv_firmware_version)
    TextView mTvFirmwareVersion;

    /**
     * 数据中心
     */
    @BindView(R.id.tv_signal_strength)
    TextView mTvSignalStrength;

    @BindView(R.id.tv_link_one_status)
    TextView mTvLinkOneStatus;

    @BindView(R.id.tv_link_one_send_data)
    TextView mTvLinkOneSendData;

    @BindView(R.id.tv_link_one_unsend_data)
    TextView mTvLinkOneUnsendData;

    @BindView(R.id.tv_link_one_online_rate)
    TextView mTvLinkOneOnlineRate;


    @BindView(R.id.tv_link_two_status)
    TextView mTvLinkTwoStatus;

    @BindView(R.id.tv_link_two_send_data)
    TextView mTvLinkTwoSendData;

    @BindView(R.id.tv_link_two_unsend_data)
    TextView mTvLinkTwoUnsendData;

    @BindView(R.id.tv_link_two_online_rate)
    TextView mTvLinkTwoOnlineRate;

    /**
     * 设备工作信息
     */

    @BindView(R.id.ll_device_abnormal_diagnosis)
    ViewGroup deviceAbnormalDiagnosisLayout;

    @BindView(R.id.tv_device_abnormal_diagnosis)
    TextView mTvDeviceAbnormalDiagnosis;

    @BindView(R.id.tv_work_mode)
    TextView mTvWorkMode;

    @BindView(R.id.tv_ctr_input_voltage)
    TextView mTvCTRInputVoltage;

    @BindView(R.id.tv_driver_input_voltage)
    TextView mTvDriverInputVoltage;

    @BindView(R.id.tv_device_temperature)
    TextView mTvDeviceTemperature;

    @BindView(R.id.tv_device_humidity)
    TextView mTvDeviceHumidity;

    @BindView(R.id.tv_device_drop_number)
    TextView mTvDeviceDropNumber;

    /**
     * 测斜仪信息
     */
    @BindView(R.id.tv_inclinometer_type)
    TextView mTvInclinometerType;

    @BindView(R.id.tv_inclinometer_channel_number)
    TextView mTvInclinometerChannelNumber;

    @BindView(R.id.tv_inclinometer_location_information)
    TextView mTvInclinometerLocationInfo;

    @BindView(R.id.tv_inclinometer_voltage)
    TextView mTvInclinometerVoltage;

    @BindView(R.id.tv_inclinometer_temperature)
    TextView mTvInclinometerTemperature;

    private AdmeCurrentStateInfo currentStateInfo;

    private DecimalFormat decimalFormat = new DecimalFormat();


    public static NetAdmeCurrentStateFragment newInstance(DeviceInfo deviceInfo) {
        NetAdmeCurrentStateFragment fragment = new NetAdmeCurrentStateFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adme_current_state_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initRefreshLayout();
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        mRefreshLayout.autoRefresh();
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                queryStateInfo();
            }
        });
    }

    /**
     * 获取设备的当前状态
     */
    private void queryStateInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_EQUIPMENT_STATE);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getId()));
    }

    @OnClick({R.id.ll_device_abnormal_diagnosis})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_device_abnormal_diagnosis) {
            showErrorModulesInfoDialog();
        }
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(false);
            }
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
        ToastUtils.show("查询设备状态响应错误");
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
        ToastUtils.show("查询设备状态响应超时");
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case ADME_MD_GET_EQUIPMENT_STATE: {
                mRefreshLayout.finishRefresh(true);
                IOTCommandResult<AdmeCurrentStateInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询设备状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                currentStateInfo = commandResult.getResult();
                initStatusInfo();
            }
            break;

            default:
                break;
        }
    }

    private void initStatusInfo() {
        if (currentStateInfo == null) {
            Timber.e("AdmeCurrentStateInfo is Null!");
            currentStateInfo = new AdmeCurrentStateInfo();
            return;
        }
        try {
            mTvDeviceSn.setText(currentStateInfo.getSn());
            mTvProductNumber.setText(currentStateInfo.getProductid());
            mTVSimCardNumber.setText(currentStateInfo.getSimid());
            mTvImeiNumber.setText(currentStateInfo.getImeid());
            mTvFirmwareVersion.setText(currentStateInfo.getFirversion());

            if (currentStateInfo.getTestway().equals("0")) {
                mTvWorkMode.setText("常规测量模式");
            } else if (currentStateInfo.getTestway().equals("1")) {
                mTvWorkMode.setText("特定点位模式");
            } else if (currentStateInfo.getTestway().equals("2")) {
                mTvWorkMode.setText("静态测量模式");
            } else if (currentStateInfo.getTestway().equals("3")) {
                mTvWorkMode.setText("设备停用模式");
            }
            decimalFormat.applyPattern("#.###");
            mTvCTRInputVoltage.setText(String.format("%sV", decimalFormat.format(Double.parseDouble(currentStateInfo.getCtrinputv()))));
            mTvDriverInputVoltage.setText(String.format("%sV", decimalFormat.format(Double.parseDouble(currentStateInfo.getDriveinputv()))));
            mTvDeviceTemperature.setText(String.format("%s℃", decimalFormat.format(Double.parseDouble(currentStateInfo.getTemperature()))));
            mTvDeviceHumidity.setText(String.format("%s%%", decimalFormat.format(Double.parseDouble(currentStateInfo.getHumidity()))));
            mTvDeviceDropNumber.setText(currentStateInfo.getDownnum());

            mTvInclinometerType.setText(currentStateInfo.getInctype().equals("0") ? "433测斜仪" : "蓝牙测斜仪");
            mTvInclinometerChannelNumber.setText(currentStateInfo.getIncnum());
            mTvInclinometerLocationInfo.setText(currentStateInfo.getIncloc());
            mTvInclinometerVoltage.setText(String.format("%sV", decimalFormat.format(Double.parseDouble(currentStateInfo.getIncvoltage()))));
            mTvInclinometerTemperature.setText(String.format("%s℃", decimalFormat.format(Double.parseDouble(currentStateInfo.getIntertempe()))));

            //处理设备异常诊断信息
            if (currentStateInfo.getAbndiasis().equals("0")) {
                deviceAbnormalDiagnosisLayout.setEnabled(false);
                mTvDeviceAbnormalDiagnosis.setText("正常");
                mTvDeviceAbnormalDiagnosis.setTextColor(ColorUtils.getColor(R.color.text_color_3AD094));
                mTvDeviceAbnormalDiagnosis.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }else{
                deviceAbnormalDiagnosisLayout.setEnabled(true);
                mTvDeviceAbnormalDiagnosis.setText("异常");
                mTvDeviceAbnormalDiagnosis.setTextColor(Color.RED);
                mTvDeviceAbnormalDiagnosis.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_arrow_right, 0);
            }
        } catch (Exception ex) {
            mRefreshLayout.finishRefresh(true);
            mTvDeviceAbnormalDiagnosis.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            ex.printStackTrace();
        }
    }

    /**
     * 展示异常模块信息
     */
    private void showErrorModulesInfoDialog() {
        if (currentStateInfo == null ||TextUtils.isEmpty(currentStateInfo.getAbndiasis()))
            return;

        List<String> descList = new ArrayList<>();
        String errinfo = currentStateInfo.getAbndiasis();
        String[] codes = errinfo.split("\\|");
        for (String code : codes) {
            AdmeModuleErrorType errorType = AdmeModuleErrorType.valueByCode(code);
            if (errorType != null) {
                descList.add(errorType.getDescription());
            }
        }
        if (descList.isEmpty())
            return;

        new XPopup.Builder(getContext())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asCenterList("异常信息", descList.toArray(new String[0]),
                        null, -1, null, 0, R.layout.custom_xpopup_adapter_text)
                .show();
    }
}
