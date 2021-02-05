package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.adme.AdmeCurrentStateInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/28<br/>
 * 描述：     ADME  查看当前状态页面
 */
public class BleAdmeCurrentStateFragment extends BaseBleIotCommunicateFragment {
    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout swipeRefresh;

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

    @BindView(R.id.tv_device_abnormal_diagnosis)
    TextView mTvDeviceAbnormalDiagnosis;

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

    public static BleAdmeCurrentStateFragment newInstance() {
        return new BleAdmeCurrentStateFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_current_state_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initRefreshLayout();
        // 进入页面，刷新数据
        swipeRefresh.setRefreshing(true);
        queryParamInfo();
    }

    private void initRefreshLayout() {
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_light);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryParamInfo();
            }
        });
    }

    /**
     * 获取设备的当前状态
     */
    private void queryParamInfo() {
//        errMsg = "查询数据超时,请稍后尝试";
//        startProgressRunnable("加载中...", WRITE_TIME_OUT_SECOND);

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_EQUIPMENT_STATE);
        sendCommand(command);
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case ADME_MD_GET_EQUIPMENT_STATE: {
                swipeRefresh.setRefreshing(false);
//                stopProgressRunnable();
                IOTCommandResult<AdmeCurrentStateInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询设备状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                currentStateInfo = commandResult.getResult();
                initParamConfigInfo();
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private void initParamConfigInfo() {
        if (currentStateInfo == null) {
            Timber.e("AdmeCurrentStateInfo is Null!");
            currentStateInfo = new AdmeCurrentStateInfo();
            return;
        }
        mTvDeviceSn.setText(currentStateInfo.getSn());
        mTvProductNumber.setText(currentStateInfo.getProductid());
        mTVSimCardNumber.setText(currentStateInfo.getSimid());
        mTvImeiNumber.setText(currentStateInfo.getImeid());
        mTvFirmwareVersion.setText(currentStateInfo.getFirversion());

        mTvWorkMode.setText(currentStateInfo.getTestway());
        mTvCTRInputVoltage.setText(String.format("%s V", currentStateInfo.getCtrinputv()));
        mTvDriverInputVoltage.setText(String.format("%s V", currentStateInfo.getDriveinputv()));
        mTvDeviceTemperature.setText(String.format("%s ℃", currentStateInfo.getTemperature()));
        mTvDeviceHumidity.setText(String.format("%s %%", currentStateInfo.getHumidity()));
        mTvDeviceAbnormalDiagnosis.setText(currentStateInfo.getAbndiasis());
        mTvDeviceDropNumber.setText(currentStateInfo.getDownnum());

        mTvInclinometerType.setText(currentStateInfo.getInctype());
        mTvInclinometerChannelNumber.setText(currentStateInfo.getIncnum());
        mTvInclinometerLocationInfo.setText(currentStateInfo.getIncloc());
        mTvInclinometerVoltage.setText(String.format("%s V", currentStateInfo.getIncvoltage()));
        mTvInclinometerTemperature.setText(String.format("%s ℃", currentStateInfo.getIntertempe()));
    }
}