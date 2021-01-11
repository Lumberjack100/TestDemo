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
import com.shmedo.configlibrary.iot.model.vms.VmsDataCenterStatus;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.adme.AdmeDataCenterConfigActivity;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

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
//        errMsg = "查询数据超时,请稍后尝试";
//        startProgressRunnable("加载中...", DELAY_MILLIS);
//        getDataCenterStatus(ServerNumber.NUMBER_ONE);
    }

    /**
     * 获取数据中心状态
     */
    private void getDataCenterStatus(ServerNumber serverNumber) {
        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(serverNumber.toInt());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_GET_DATA_CENTER_STATUS, serverNumberEntity);
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
            AdmeDataCenterConfigActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, configMethod, ServerNumber.NUMBER_ONE, mTvDataCenterOne.getText().toString());

        } else if (id == R.id.dataCenterTwoLayout) {
            AdmeDataCenterConfigActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, configMethod, ServerNumber.NUMBER_TWO, mTvDataCenterTwo.getText().toString());
        }
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case VMS_MD_GET_DATA_CENTER_STATUS: {//获取设备的数据中心状态
                IOTCommandResult<VmsDataCenterStatus> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopProgressRunnable();
                    String errMsg = String.format("%s %s", "查询数据中心状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                VmsDataCenterStatus centerStatus = commandResult.getResult();
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
            status = "已上线";
        } else if (statusId.equals("2")) {
            status = "未上线";
        }

        return status;
    }

    private int getStatusColorResId(String statusId) {
        int resId = R.color.text_color_666666;
        if (statusId.equals("0")) {
            resId = R.color.sub_title_text_color;
        } else if (statusId.equals("1")) {
            resId = R.color.text_color_3AD094;
        } else if (statusId.equals("2")) {
            resId = R.color.red;
        }

        return resId;
    }
}