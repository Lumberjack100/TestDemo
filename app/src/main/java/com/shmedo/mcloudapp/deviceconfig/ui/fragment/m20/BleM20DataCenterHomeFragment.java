package com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ColorUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.ServerNumberEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.configlibrary.iot.model.DataCenterStatus;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/19/21 <br/>
 * 描述：    M20蓝牙模式 数据中心主页面
 */
public class BleM20DataCenterHomeFragment extends BaseUSRBleIotCommunicateFragment {
    public static final String LEVEL_INITIAL = "com.shmedo.mcloudapp.LEVEL_INITIAL";

    @BindView(R.id.tv_data_center_one)
    TextView mTvDataCenterOne;

    @BindView(R.id.tv_data_center_two)
    TextView mTvDataCenterTwo;

    @BindView(R.id.tv_data_center_three)
    TextView mTvDataCenterThree;

    @BindView(R.id.tv_data_center_four)
    TextView mTvDataCenterFour;

    @BindView(R.id.btn_confirm)
    Button mBtnComplete;

    private int configMethod = AppContants.DataCenterConfigMethod.BASIC_CONFIG;
    private boolean isLevelInit = false;

    private static final int SERVER_NUMBER_ONE = 0x1001;
    private static final int SERVER_NUMBER_TWO = 0x1002;
    private static final int SERVER_NUMBER_THREE = 0x1003;
    private static final int SERVER_NUMBER_FOUR = 0x1004;
    private int serverNumber = -1;
    private ActivityResultLauncher<Intent> resultLauncher;


    public static BleM20DataCenterHomeFragment newInstance(int configMethod, boolean isLevelInit) {
        BleM20DataCenterHomeFragment fragment = new BleM20DataCenterHomeFragment();
        Bundle args = new Bundle();
        args.putInt(AppContants.Extras.DATA_CENTER_CONFIG_METHOD, configMethod);
        args.putBoolean(LEVEL_INITIAL, isLevelInit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            configMethod = getArguments().getInt(AppContants.Extras.DATA_CENTER_CONFIG_METHOD);
            isLevelInit = getArguments().getBoolean(LEVEL_INITIAL, false);
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
        startDefaultProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);

        switch (serverNumber) {
            case SERVER_NUMBER_ONE:
                getDataCenterStatus(ServerNumber.NUMBER_ONE);
                break;

            case SERVER_NUMBER_TWO:
                getDataCenterStatus(ServerNumber.NUMBER_TWO);
                break;

            case SERVER_NUMBER_THREE:
                getDataCenterStatus(ServerNumber.NUMBER_THREE);
                break;

            case SERVER_NUMBER_FOUR:
                getDataCenterStatus(ServerNumber.NUMBER_FOUR);
                break;
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.m20_data_center_home_fragment;
    }

   @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isLevelInit) {
            mBtnComplete.setVisibility(View.VISIBLE);
        } else {
            mBtnComplete.setVisibility(View.GONE);
        }
        startDefaultProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
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

    @OnClick({R.id.dataCenterOneLayout, R.id.dataCenterTwoLayout, R.id.dataCenterThreeLayout, R.id.dataCenterFourLayout, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }

        int id = view.getId();
        if (id == R.id.dataCenterOneLayout) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            serverNumber = SERVER_NUMBER_ONE;
            DataCenterConfigActivity.startActivity(mActivity, resultLauncher, ProductType.M20, AppContants.CommunicationWay.BLE_CONNECT, configMethod, ServerNumber.NUMBER_ONE, mTvDataCenterOne.getText().toString());

        } else if (id == R.id.dataCenterTwoLayout) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            serverNumber = SERVER_NUMBER_TWO;
            DataCenterConfigActivity.startActivity(mActivity, resultLauncher, ProductType.M20, AppContants.CommunicationWay.BLE_CONNECT, configMethod, ServerNumber.NUMBER_TWO, mTvDataCenterTwo.getText().toString());

        } else if (id == R.id.dataCenterThreeLayout) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            serverNumber = SERVER_NUMBER_THREE;
            DataCenterConfigActivity.startActivity(mActivity, resultLauncher, ProductType.M20, AppContants.CommunicationWay.BLE_CONNECT, configMethod, ServerNumber.NUMBER_THREE, mTvDataCenterThree.getText().toString());

        } else if (id == R.id.dataCenterFourLayout) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            serverNumber = SERVER_NUMBER_FOUR;
            DataCenterConfigActivity.startActivity(mActivity, resultLauncher, ProductType.M20, AppContants.CommunicationWay.BLE_CONNECT, configMethod, ServerNumber.NUMBER_FOUR, mTvDataCenterFour.getText().toString());

        } else if (id == R.id.btn_confirm) {
            mActivity.finish();
        }
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        switch (msg.what) {
            case AppContants.MsgWhat.MSG_DEFAULT:
                ToastUtils.show("响应超时,请稍后尝试");
                break;
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
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "查询数据中心状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                DataCenterStatus centerStatus = commandResult.getResult();
                if (centerStatus.getCenterid() == 1) {
                    mTvDataCenterOne.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterOne.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(getStatusColorResId(centerStatus.getStatus())));
                    //表示首次进入页面，需要逐个刷新所有的数据中心
                    if (serverNumber == -1) {
                        getDataCenterStatus(ServerNumber.NUMBER_TWO);
                    } else {
                        //表示刷新指定的数据中心
                        stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    }
                } else if (centerStatus.getCenterid() == 2) {
                    mTvDataCenterTwo.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterTwo.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(getStatusColorResId(centerStatus.getStatus())));
                    if (serverNumber == -1) {
                        getDataCenterStatus(ServerNumber.NUMBER_THREE);
                    } else {
                        stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    }
                } else if (centerStatus.getCenterid() == 3) {
                    mTvDataCenterThree.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterThree.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(getStatusColorResId(centerStatus.getStatus())));
                    if (serverNumber == -1) {
                        getDataCenterStatus(ServerNumber.NUMBER_FOUR);
                    } else {
                        stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    }
                } else if (centerStatus.getCenterid() == 4) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    mTvDataCenterFour.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterFour.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(getStatusColorResId(centerStatus.getStatus())));
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
            resId = R.color.text_color_3AD094;
        } else if (statusId.equals("2")) {
            resId = R.color.device_not_connected_platform;
        }
        return resId;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        serverNumber = -1;
    }
}