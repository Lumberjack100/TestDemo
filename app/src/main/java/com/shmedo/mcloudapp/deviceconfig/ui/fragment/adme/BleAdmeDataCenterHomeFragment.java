package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.ServerNumberEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.configlibrary.iot.model.DataCenterStatus;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterConfigActivity;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/29<br/>
 * 描述：    ADME 数据中心页面
 */
public class BleAdmeDataCenterHomeFragment extends BaseBleIotCommunicateFragment {
    @BindView(R.id.tv_data_center_one)
    TextView mTvDataCenterOne;

    @BindView(R.id.tv_data_center_two)
    TextView mTvDataCenterTwo;

    private int configMethod = AppContants.DataCenterConfigMethod.BASIC_CONFIG;


    public static BleAdmeDataCenterHomeFragment newInstance(int configMethod) {
        BleAdmeDataCenterHomeFragment fragment = new BleAdmeDataCenterHomeFragment();
        Bundle args = new Bundle();
        args.putInt(AppContants.Extras.DATA_CENTER_CONFIG_METHOD, configMethod);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            configMethod = getArguments().getInt(AppContants.Extras.DATA_CENTER_CONFIG_METHOD);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_data_center_home_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
//        startProgressRunnable("加载中...", DELAY_MILLIS);
        getDataCenterStatus(ServerNumber.NUMBER_ONE);
    }

    /**
     * 获取数据中心状态
     */
    private void getDataCenterStatus(ServerNumber serverNumber) {
        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(serverNumber.toInt());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_GET_DATA_CENTER_STATUS, serverNumberEntity);
        sendCommand(command);
    }

    @OnClick({R.id.dataCenterOneLayout, R.id.dataCenterTwoLayout})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        if (!isConnected()) {
            ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
            return;
        }

        int id = view.getId();
        if (id == R.id.dataCenterOneLayout) {
            DataCenterConfigActivity.startActivity(mActivity, AppContants.DeviceType.ADME, AppContants.CommunicationWay.BLE_CONNECT, configMethod, ServerNumber.NUMBER_ONE, mTvDataCenterOne.getText().toString());

        } else if (id == R.id.dataCenterTwoLayout) {
            DataCenterConfigActivity.startActivity(mActivity, AppContants.DeviceType.ADME, AppContants.CommunicationWay.BLE_CONNECT, configMethod, ServerNumber.NUMBER_TWO, mTvDataCenterTwo.getText().toString());
        }
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case MD_GET_DATA_CENTER_STATUS: {//获取设备的数据中心状态
                IOTCommandResult<DataCenterStatus> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopProgressRunnable();
                    String errMsg = String.format("%s %s", "查询数据中心状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                DataCenterStatus centerStatus = commandResult.getResult();
                if (centerStatus.getCenterid() == 1) {
                    mTvDataCenterOne.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterOne.setTextColor(GlobalUtil.getColor(getStatusColorResId(centerStatus.getStatus())));
                    getDataCenterStatus(ServerNumber.NUMBER_TWO);
                } else if (centerStatus.getCenterid() == 2) {
                    stopProgressRunnable();
                    mTvDataCenterTwo.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterTwo.setTextColor(GlobalUtil.getColor(getStatusColorResId(centerStatus.getStatus())));
                }
            }
            break;
            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private String getStatusTextById(String statusId) {
        String status = "未知状态";
        if (statusId.equals("0")) {
            status = "未开启";
        } else if (statusId.equals("1")) {
            status = "已连接";
        } else if (statusId.equals("2")) {
            status = "未连接";
        }

        return status;
    }

    private int getStatusColorResId(String statusId) {
        int resId = R.color.device_unopened_platform;
        if (statusId.equals("0")) {
            resId = R.color.device_unopened_platform;
        } else if (statusId.equals("1")) {
            resId = R.color.title_text_color;
        } else if (statusId.equals("2")) {
            resId = R.color.device_not_connected_platform;
        }
        return resId;
    }
}