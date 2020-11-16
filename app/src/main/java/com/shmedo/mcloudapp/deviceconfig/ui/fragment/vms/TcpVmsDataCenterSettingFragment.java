package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.ServerNumberEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.VmsAisleParamInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BaseTcpConnectFragment;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;


public class TcpVmsDataCenterSettingFragment extends BaseTcpConnectFragment {



    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    private static final String SERVER_NUMBER = "server_number";
    private ServerNumber serverNumber;

    private TcpVmsDataCenterSettingViewModel mViewModel;


    public static TcpVmsDataCenterSettingFragment newInstance(ServerNumber serverNumber) {
        TcpVmsDataCenterSettingFragment fragment = new TcpVmsDataCenterSettingFragment();
        Bundle args = new Bundle();
        args.putSerializable(SERVER_NUMBER, serverNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            serverNumber = (ServerNumber) getArguments().getSerializable(SERVER_NUMBER);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tcp_vms_data_center_setting_fragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(TcpVmsDataCenterSettingViewModel.class);
        // TODO: Use the ViewModel

        setView();
        queryVmsDataCenterInfo();
    }

    private void setView() {

    }

    /**
     * 获取设备的数据中心参数
     */
    private void queryVmsDataCenterInfo() {
        startProgressRunnable("初始化数据...", QUERY_CMD_DELAY_MILLIS);

        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(serverNumber.toInt());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_GET_DATA_CENTER, serverNumberEntity);
        sendCommand(command);
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
                KeyBordUtils.hideSoftKeyboard(view);

                if (!tcpShareViewModel.getConnectStatus()) {
                    ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
                    return;
                }

                if (!checkValue()) {
                    Timber.w("通道参数存在错误!");
                    return;
                }

                processSave();
                break;
        }
    }

    private boolean checkValue() {


        return true;
    }

    private void processSave() {

    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        if (!isActive) {
            return;
        }
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case MD_GET_DATA_CENTER: {//获取网关的数据中心参数
//                stopProgressRunnable();
                IOTCommandResult<VmsAisleParamInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = "查询网关的数据中心参数出错!";
                    Timber.e("%s%s", errMsg, commandResult.getMessage());
                    ToastUtils.show(errMsg);
                    return;
                }
//                vmsAisleParamInfo = commandResult.getResult();
//                initViewData();
            }
            break;

            case MD_SET_DATA_CENTER: {//设置网关通道的控制参数
//                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = "设置参数失败!";
                    Timber.e("%s%s", errMsg, cmdResult.getReason());
                    ToastUtils.show(errMsg);
                    mBtnSave.setEnabled(true);
                    return;
                }
                doAfterSetting();
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }

    }

    private void doAfterSetting() {
        ToastUtils.show("设置成功");
        mBtnSave.setEnabled(true);
    }

}