package com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
 * 创建时间:  1/19/21 <br/>
 * 描述：    M20 数据中心主页面
 */
public class BleM20DataCenterHomeFragment extends BaseGOCBleIotCommunicateFragment {
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
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_m20_data_center_home_fragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isLevelInit) {
            mBtnComplete.setVisibility(View.VISIBLE);
        } else {
            mBtnComplete.setVisibility(View.GONE);
        }
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
            DataCenterConfigActivity.startActivity(mActivity, AppContants.DeviceType.M20, AppContants.CommunicationWay.BLE_CONNECT, configMethod, ServerNumber.NUMBER_ONE, mTvDataCenterOne.getText().toString());

        } else if (id == R.id.dataCenterTwoLayout) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            DataCenterConfigActivity.startActivity(mActivity, AppContants.DeviceType.M20, AppContants.CommunicationWay.BLE_CONNECT, configMethod, ServerNumber.NUMBER_TWO, mTvDataCenterTwo.getText().toString());

        } else if (id == R.id.dataCenterThreeLayout) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            DataCenterConfigActivity.startActivity(mActivity, AppContants.DeviceType.M20, AppContants.CommunicationWay.BLE_CONNECT, configMethod, ServerNumber.NUMBER_TWO, mTvDataCenterThree.getText().toString());

        } else if (id == R.id.dataCenterFourLayout) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            DataCenterConfigActivity.startActivity(mActivity, AppContants.DeviceType.M20, AppContants.CommunicationWay.BLE_CONNECT, configMethod, ServerNumber.NUMBER_TWO, mTvDataCenterFour.getText().toString());

        } else if (id == R.id.btn_confirm) {
            mActivity.finish();
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
//                    stopProgressRunnable();
                    mTvDataCenterTwo.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterTwo.setTextColor(GlobalUtil.getColor(getStatusColorResId(centerStatus.getStatus())));
                    getDataCenterStatus(ServerNumber.NUMBER_THREE);
                } else if (centerStatus.getCenterid() == 3) {
                    mTvDataCenterThree.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterThree.setTextColor(GlobalUtil.getColor(getStatusColorResId(centerStatus.getStatus())));
                    getDataCenterStatus(ServerNumber.NUMBER_FOUR);
                } else if (centerStatus.getCenterid() == 4) {
                    stopProgressRunnable();
                    mTvDataCenterFour.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterFour.setTextColor(GlobalUtil.getColor(getStatusColorResId(centerStatus.getStatus())));
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