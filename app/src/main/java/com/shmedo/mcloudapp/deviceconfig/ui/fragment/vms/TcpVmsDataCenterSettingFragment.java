package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.DataCenterEntity;
import com.shmedo.configlibrary.iot.cmd.entity.ServerNumberEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.DataCenterInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BaseTcpConnectFragment;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * Vms网关数据中心参数配置
 */
public class TcpVmsDataCenterSettingFragment extends BaseTcpConnectFragment {
    @BindView(R.id.centerEnableSBtn)
    SwitchButton mSbCenterEnable;

    @BindView(R.id.tv_state)
    TextView mTvState;

    @BindView(R.id.tv_transfer_protocol)
    TextView mTvTransferProtocol;

    @BindView(R.id.tv_data_protocol)
    TextView mTvDataProtocol;

    @BindView(R.id.et_data_server_address)
    ClearEditText mEtDataServerAddress;

    @BindView(R.id.et_data_server_port)
    ClearEditText mEtDataServerPort;

    @BindView(R.id.et_device_id)
    ClearEditText mEtDeviceId;

    @BindView(R.id.et_device_key)
    ClearEditText mEtDeviceKey;

    @BindView(R.id.et_device_register_address)
    ClearEditText mEtDeviceRegisterAddress;

    @BindView(R.id.et_device_register_port)
    ClearEditText mEtDeviceRegisterPort;

    @BindView(R.id.et_product_id)
    ClearEditText mEtProductId;

    @BindView(R.id.et_device_register_code)
    ClearEditText mEtDeviceRegisterCode;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.ll_child_items)
    ViewGroup childItemsLayout;

    @BindView(R.id.ll_transfer_protocol)
    ViewGroup transferProtocolLayout;

    private static final String DATA_SERVER_NUMBER = "data_server_number";
    private ServerNumber serverNumber;
    private DataCenterInfo dataCenterInfo;

    private int transferProtocolPos;
    private String transferProtocolOld;//

    private String transferProtocol;// 传输协议
    private String dataProtocol;//数据协议
    private String dataServerAddress;//数据服务器地址
    private String dataServerPort;//数据服务器端口
    private String deviceId;//设备 Id
    private String deviceKey;//设备Key
    private String registerAddress;// 注册地址
    private String registerPort;//注册端口
    private String productId;//产品 Id
    private String registerCode;//注册码


    public static TcpVmsDataCenterSettingFragment newInstance(ServerNumber serverNumber) {
        TcpVmsDataCenterSettingFragment fragment = new TcpVmsDataCenterSettingFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATA_SERVER_NUMBER, serverNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            serverNumber = (ServerNumber) getArguments().getSerializable(DATA_SERVER_NUMBER);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tcp_vms_data_center_setting_fragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        setSwitchViewListener();
        queryDataCenterInfo();
    }

    private void setView() {
        mEtDataServerAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        mEtDataServerPort.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtDeviceId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        mEtDeviceKey.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        mEtDeviceRegisterAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        mEtDeviceRegisterPort.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtProductId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        mEtDeviceRegisterCode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
    }

    private void updateViewState(boolean isDisabled) {
        if (isDisabled) {
            transferProtocolLayout.setEnabled(isDisabled);
        } else {

        }
    }

    private void setSwitchViewListener() {
        mSbCenterEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!tcpShareViewModel.getConnectStatus()) {
                    ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
                    mSbCenterEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定要关闭数据中心？");
                } else {
                    childItemsLayout.setVisibility(View.VISIBLE);
                    mBtnSave.setEnabled(true);

                    queryDataCenterInfo();
                }
            }
        });
    }

    /**
     * 关闭SwitchButton
     */
    private void showCloseSwitchButtonDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("温馨提示：")
                .content(content)
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
                        childItemsLayout.setVisibility(View.GONE);
                        closeDataServer();//关闭服务器
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        mSbCenterEnable.setCheckedImmediatelyNoEvent(true);
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /**
     * 获取设备的数据中心参数
     */
    private void queryDataCenterInfo() {

        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(serverNumber.toInt());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_GET_DATA_CENTER, serverNumberEntity);
        sendCommand(command);
    }

    /**
     * 关闭数据中心</br>
     * addr和port设置为空时，关闭该数据中心
     */
    private void closeDataServer() {
        DataCenterEntity dataCenterEntity = new DataCenterEntity();
        dataCenterEntity.setServerNumber(serverNumber);
        dataCenterEntity.setAddr("");
        dataCenterEntity.setPort("");

        mBtnSave.setEnabled(false);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_SET_DATA_CENTER, dataCenterEntity);
        sendCommand(command);
    }

    @OnClick({R.id.ll_transfer_protocol, R.id.btn_confirm})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ll_transfer_protocol) {
            showTransferProtocolDialog();

        } else if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);

            if (!tcpShareViewModel.getConnectStatus()) {
                ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
                return;
            }

            if (!checkValueIsValid()) {
                Timber.w("通道参数存在错误!");
                return;
            }

            processSave();
        }
    }

    private void showTransferProtocolDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"TCP-C", "TCP-S", "MQTT"},
                        null, transferProtocolPos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                updateViewByTransferProtocol(position, text);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    private void updateViewByTransferProtocol(int position, String text) {
        transferProtocolPos = position;
        transferProtocol = text;
        mTvTransferProtocol.setText(text);

        if (!text.contains("MQTT")) {
            childItemsLayout.setVisibility(View.GONE);
        } else {
            childItemsLayout.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkValueIsValid() {
        dataServerAddress = mEtDataServerAddress.getText().toString().trim();
        dataServerPort = mEtDataServerPort.getText().toString().trim();
        deviceId = mEtDeviceId.getText().toString().trim();
        deviceKey = mEtDeviceKey.getText().toString().trim();
        registerAddress = mEtDeviceRegisterAddress.getText().toString().trim();
        registerPort = mEtDeviceRegisterPort.getText().toString().trim();
        productId = mEtProductId.getText().toString().trim();
        registerCode = mEtDeviceRegisterCode.getText().toString().trim();

        if (TextUtils.isEmpty(dataServerAddress)) {
            ToastUtils.show("数据中心地址不能为空!");
            mEtDataServerAddress.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(dataServerPort)) {
            ToastUtils.show("数据中心端口不能为空!");
            mEtDataServerPort.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(dataServerPort);
            if (port < 0 || port > 65535) {
                ToastUtils.show("请输入有效的数据中心端口号!");
                mEtDataServerPort.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入有效的数据中心端口号!");
            mEtDataServerPort.requestFocus();
            return false;
        }

        if (transferProtocol.equals("MQTT")) {
//            if (TextUtils.isEmpty(productId)) {
//                ToastUtils.show("产品ID不能为空!");
//                mEtProductId.requestFocus();
//                return false;
//            }
//            if (TextUtils.isEmpty(deviceId)) {
//                ToastUtils.show("设备ID不能为空!");
//                mEtDeviceId.requestFocus();
//                return false;
//            }
//            if (TextUtils.isEmpty(deviceKey)) {
//                ToastUtils.show("设备Key不能为空!");
//                mEtDeviceKey.requestFocus();
//                return false;
//            }


//            if (TextUtils.isEmpty(registerAddress)) {
//                ToastUtils.show("设备注册地址不能为空!");
//                mEtDeviceRegisterAddress.requestFocus();
//                return false;
//            }
//            if (TextUtils.isEmpty(registerPort)) {
//                ToastUtils.show("设备注册端口不能为空!");
//                mEtDeviceRegisterPort.requestFocus();
//                return false;
//            }
//            try {
//                int port = Integer.parseInt(registerPort);
//                if (port < 0 || port > 65535) {
//                    ToastUtils.show("请输入有效的设备注册端口号!");
//                    mEtDeviceRegisterPort.requestFocus();
//                    return false;
//                }
//            } catch (Exception ex) {
//                ToastUtils.show("请输入有效的设备注册端口号!");
//                mEtDeviceRegisterPort.requestFocus();
//                return false;
//            }
//
//            if (TextUtils.isEmpty(registerCode)) {
//                ToastUtils.show("设备注册码不能为空!");
//                mEtDeviceRegisterCode.requestFocus();
//                return false;
//            }
        }

        return true;
    }

    private void processSave() {
        DataCenterEntity dataCenterEntity = new DataCenterEntity();
        dataCenterEntity.setServerNumber(serverNumber);
        dataCenterEntity.setProtocol(transferProtocol);
        dataCenterEntity.setDatatype(dataProtocol);
        dataCenterEntity.setAddr(dataServerAddress);
        dataCenterEntity.setPort(dataServerPort);
        dataCenterEntity.setDeviceid(deviceId);
        dataCenterEntity.setDevicekey(deviceKey);
        dataCenterEntity.setHttpaddr(registerAddress);
        dataCenterEntity.setHttpport(registerPort);
        dataCenterEntity.setProjid(productId);
        dataCenterEntity.setRegcode(registerCode);

        mBtnSave.setEnabled(false);
//        errMsg = "发送指令超时,请稍后尝试";
//        startProgressRunnable("正在发送配置指令...", SEND_CMD_DELAY_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_SET_DATA_CENTER, dataCenterEntity);
        sendCommand(command);
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
                IOTCommandResult<DataCenterInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = "查询网关的数据中心参数出错!";
                    Timber.e("%s%s", errMsg, commandResult.getMessage());
                    ToastUtils.show(errMsg);
                    return;
                }
                dataCenterInfo = commandResult.getResult();
                initDataCenterData();
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
        transferProtocolOld = transferProtocol;
        mBtnSave.setEnabled(true);
    }

    private void initDataCenterData() {
        if (dataCenterInfo == null) {
            Timber.e("DataCenterInfo 为空!");
            dataCenterInfo = new DataCenterInfo();
            return;
        }

        transferProtocolOld = dataCenterInfo.getProtocol().trim();
        transferProtocol = dataCenterInfo.getProtocol().trim();
        dataProtocol = dataCenterInfo.getDatatype().trim();
        dataServerAddress = dataCenterInfo.getAddr().trim();
        dataServerPort = dataCenterInfo.getPort().trim();
        deviceId = dataCenterInfo.getDeviceid().trim();
        deviceKey = dataCenterInfo.getDevicekey().trim();
        registerAddress = dataCenterInfo.getHttpaddr().trim();
        registerPort = dataCenterInfo.getHttpport().trim();
        productId = dataCenterInfo.getProjid().trim();
        registerCode = dataCenterInfo.getRegcode().trim();

        mTvTransferProtocol.setText(transferProtocolOld);
        mTvDataProtocol.setText(dataProtocol);
        mEtDataServerAddress.setText(dataServerAddress);
        mEtDataServerPort.setText(dataServerPort);
        mEtDeviceId.setText(deviceId);
        mEtDeviceKey.setText(deviceKey);
        mEtDeviceRegisterAddress.setText(registerAddress);
        mEtDeviceRegisterPort.setText(registerPort);
        mEtProductId.setText(productId);
        mEtDeviceRegisterCode.setText(registerCode);

        if (!transferProtocolOld.contains("MQTT")) {
            childItemsLayout.setVisibility(View.GONE);
        } else {
            childItemsLayout.setVisibility(View.VISIBLE);
        }

        //数据中心地址为空表示数据中心未启用
        if (TextUtils.isEmpty(dataServerAddress)) {
            mSbCenterEnable.setCheckedImmediatelyNoEvent(false);
            childItemsLayout.setVisibility(View.GONE);
            mBtnSave.setEnabled(false);
        } else {
            mSbCenterEnable.setCheckedImmediatelyNoEvent(true);
            childItemsLayout.setVisibility(View.VISIBLE);
            mBtnSave.setEnabled(true);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (tcpShareViewModel.getConnectStatus()) {
            if (checkValueIsChange()) {
                warnNotYetSettingBeforeLeavePage();
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    private boolean checkValueIsChange() {
        if (!mSbCenterEnable.isChecked()) {
            return false;
        }

        if (transferProtocolOld != null && transferProtocolOld != null && !transferProtocolOld.equals(transferProtocol)) {
            return true;
        }

        if (dataServerAddress != null && !dataServerAddress.equals(mEtDataServerAddress.getText().toString().trim())) {
            return true;
        }

        if (dataServerPort != null && !dataServerPort.equals(mEtDataServerPort.getText().toString().trim())) {
            return true;
        }

        if (transferProtocol.equals("MQTT")) {//MQTT自动注册

            if (deviceId != null && !deviceId.equals(mEtDeviceId.getText().toString().trim())) {
                return true;
            }

            if (deviceKey != null && !deviceKey.equals(mEtDeviceKey.getText().toString().trim())) {
                return true;
            }
            if (registerAddress != null && !registerAddress.equals(mEtDeviceRegisterAddress.getText().toString().trim())) {
                return true;
            }

            if (registerPort != null && !registerPort.equals(mEtDeviceRegisterPort.getText().toString().trim())) {
                return true;
            }

            if (productId != null && !productId.equals(mEtProductId.getText().toString().trim())) {
                return true;
            }

            if (registerCode != null && !registerCode.equals(mEtDeviceRegisterCode.getText().toString().trim())) {
                return true;
            }

        }

        return false;
    }
}