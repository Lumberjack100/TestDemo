package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/29<br/>
 * 描述：    ADME 数据中心页面
 */
public class BleAdmeDataCenterHomeFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.tv_data_center_one)
    TextView mTvDataCenterOne;

    @BindView(R.id.tv_data_center_two)
    TextView mTvDataCenterTwo;

    private int configMethod = AppContants.DataCenterConfigMethod.BASIC_CONFIG;

    private static final int SERVER_NUMBER_ONE = 0x1001;
    private static final int SERVER_NUMBER_TWO = 0x1002;
    private static final int SERVER_NUMBER_THREE = 0x1003;
    private static final int SERVER_NUMBER_FOUR = 0x1004;
    private int serverNumber = -1;
    private ActivityResultLauncher<Intent> resultLauncher;

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
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            refreshSpecifiedServerStatus();
                        }
                    }
                });
    }

    /**
     * 刷新指定的数据中心状态
     */
    private void refreshSpecifiedServerStatus() {
        startProgressRunnable("加载中...", WRITE_TIME_OUT_SECOND);
        switch (serverNumber) {
            case SERVER_NUMBER_ONE:
                getDataCenterStatus(ServerNumber.NUMBER_ONE);
                break;

            case SERVER_NUMBER_TWO:
                getDataCenterStatus(ServerNumber.NUMBER_TWO);
                break;

//            case SERVER_NUMBER_THREE:
//                getDataCenterStatus(ServerNumber.NUMBER_THREE);
//                break;
//
//            case SERVER_NUMBER_FOUR:
//                getDataCenterStatus(ServerNumber.NUMBER_FOUR);
//                break;
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_data_center_home_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        startProgressRunnable("加载中...", WRITE_TIME_OUT_SECOND);
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
            serverNumber = SERVER_NUMBER_ONE;
            DataCenterConfigActivity.startActivity(mActivity, resultLauncher,AppContants.DeviceType.ADME, AppContants.CommunicationWay.BLE_CONNECT, configMethod, ServerNumber.NUMBER_ONE, mTvDataCenterOne.getText().toString());

        } else if (id == R.id.dataCenterTwoLayout) {
            serverNumber = SERVER_NUMBER_TWO;
            DataCenterConfigActivity.startActivity(mActivity, resultLauncher,AppContants.DeviceType.ADME, AppContants.CommunicationWay.BLE_CONNECT, configMethod, ServerNumber.NUMBER_TWO, mTvDataCenterTwo.getText().toString());
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
                    //表示首次进入页面，需要逐个刷新所有的数据中心
                    if (serverNumber == -1) {
                        getDataCenterStatus(ServerNumber.NUMBER_TWO);
                    } else {
                        //表示刷新指定的数据中心
                        stopProgressRunnable();
                    }
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