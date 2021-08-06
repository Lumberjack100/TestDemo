package com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon;

import android.app.Activity;
import android.content.Intent;
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
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/22/21 <br/>
 * 描述：    通用网络模式数据中心高级参数配置页面
 */
public class UniversalNetDataCenterAdvancedConfigFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.contentLayout)
    ViewGroup contentLayout;

    @BindView(R.id.centerEnableSBtn)
    SwitchButton mSbCenterEnable;

    @BindView(R.id.tv_transfer_protocol)
    TextView mTvTransferProtocol;

    @BindView(R.id.tv_data_protocol)
    TextView mTvDataProtocol;

    @BindView(R.id.tv_platform_type)
    TextView mTvPlatformType;

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

    @BindView(R.id.ll_mqtt_child_items)
    ViewGroup mqttChildItemsLayout;

    private ServerNumber serverNumber;
    private String serverStatus;
    private DataCenterInfo dataCenterInfo;

    private int transferProtocolPos;
    private String transferProtocolOld;//

    private int dataProtocolPos;
    private String dataProtocolOld;//

    private int platformTypePos;
    private String platformTypeOld;//

    private String transferProtocol;// 传输协议
    private String dataProtocol;//数据协议
    private String platformType;//平台类型
    private String dataServerAddress;//数据服务器地址
    private String dataServerPort;//数据服务器端口
    private String deviceId;//设备 Id
    private String deviceKey;//设备Key
    private String registerAddress;// 注册地址
    private String registerPort;//注册端口
    private String productId;//产品 Id
    private String registerCode;//注册码

    private boolean enableButtonOriginalState;//数据中心开关初始状态，用于判断开关是否有打开后没有设置参数就返回
    private boolean isSaveParamOperation = false;//判断当前是保存参数操作，还是关闭数据中心操作
    private boolean isResultOK = false;//返回的结果是否是 Activity.RESULT_OK


    public static UniversalNetDataCenterAdvancedConfigFragment newInstance(ServerNumber serverNumber, String status, ProjectDeviceInfo projectDeviceInfo) {
        UniversalNetDataCenterAdvancedConfigFragment fragment = new UniversalNetDataCenterAdvancedConfigFragment();
        Bundle args = new Bundle();
        args.putSerializable(AppContants.Extras.DATA_SERVER_NUMBER, serverNumber);
        args.putSerializable(AppContants.Extras.DATA_SERVER_STATUS, status);
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            serverNumber = (ServerNumber) getArguments().getSerializable(AppContants.Extras.DATA_SERVER_NUMBER);
            serverStatus = getArguments().getString(AppContants.Extras.DATA_SERVER_STATUS);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.universal_data_center_advanced_config_fragment;
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

        //数据中心地址为空表示数据中心未启用
        if (!TextUtils.isEmpty(serverStatus) && serverStatus.contains("未开启")) {
            enableButtonOriginalState = false;
            mSbCenterEnable.setCheckedImmediatelyNoEvent(false);
            contentLayout.setVisibility(View.GONE);
        } else {
            enableButtonOriginalState = true;
            mSbCenterEnable.setCheckedImmediatelyNoEvent(true);
            contentLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setSwitchViewListener() {
        mSbCenterEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定要关闭数据中心？");
                } else {
                    contentLayout.setVisibility(View.VISIBLE);
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
                        closeDataServer();//关闭服务器
                        contentLayout.setVisibility(View.GONE);
                        enableButtonOriginalState = mSbCenterEnable.isChecked();
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
        showProgressDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
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

        isSaveParamOperation = false;
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_SET_DATA_CENTER, dataCenterEntity);
        showProgressDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    @OnClick({R.id.ll_transfer_protocol, R.id.ll_data_protocol, R.id.ll_platform_type, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_transfer_protocol) {
            showTransferProtocolDialog();

        } else if (id == R.id.ll_data_protocol) {
            showDataProtocolDialog();

        } else if (id == R.id.ll_platform_type) {
            showPlatformTypeDialog();

        } else if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
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
            mqttChildItemsLayout.setVisibility(View.GONE);
        } else {
            mqttChildItemsLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 选择数据协议弹框
     */
    private void showDataProtocolDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"CMD", "NMEA", "DIFF_IN", "DIFF_OUT", "RAW_OUT", "RES_OUT"},
                        null, dataProtocolPos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                dataProtocolPos = position;
                                mTvDataProtocol.setText(text);
                                switch (text) {
                                    case "CMD":
                                        dataProtocol = "1";
                                        break;

                                    case "NMEA":
                                        dataProtocol = "2";
                                        break;

                                    case "DIFF_IN":
                                        dataProtocol = "3";
                                        break;

                                    case "DIFF_OUT":
                                        dataProtocol = "4";
                                        break;

                                    case "RAW_OUT":
                                        dataProtocol = "5";
                                        break;

                                    case "RES_OUT":
                                        dataProtocol = "6";
                                        break;
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    /**
     * 选择平台类型弹框
     */
    private void showPlatformTypeDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"地灾一期", "成都理工平台", "MDNET", "地灾二期"},
                        null, platformTypePos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                platformTypePos = position;
                                mTvPlatformType.setText(text);
                                switch (text) {
                                    case "地灾一期":
                                        platformType = "0";
                                        break;

                                    case "成都理工平台":
                                        platformType = "1";
                                        break;

                                    case "MDNET":
                                        platformType = "2";
                                        break;

                                    case "地灾二期":
                                        platformType = "3";
                                        break;
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
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

//        if (TextUtils.isEmpty(dataServerAddress)) {
//            ToastUtils.show("请输入数据中心地址!");
//            mEtDataServerAddress.requestFocus();
//            return false;
//        }

        if (!TextUtils.isEmpty(dataServerPort)) {
            try {
                int port = Integer.parseInt(dataServerPort);
                if (port < 0 || port > 65535) {
                    ToastUtils.show("请输入正确的数据中心端口号!");
                    mEtDataServerPort.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的数据中心端口号!");
                mEtDataServerPort.requestFocus();
                return false;
            }
        }

//        if (transferProtocol.equals("MQTT")) {
//            if (TextUtils.isEmpty(productId)) {
//                ToastUtils.show("产品ID!");
//                mEtProductId.requestFocus();
//                return false;
//            }
//            if (TextUtils.isEmpty(deviceId)) {
//                ToastUtils.show("设备ID!");
//                mEtDeviceId.requestFocus();
//                return false;
//            }
//            if (TextUtils.isEmpty(deviceKey)) {
//                ToastUtils.show("设备Key!");
//                mEtDeviceKey.requestFocus();
//                return false;
//            }
//            if (TextUtils.isEmpty(registerAddress)) {
//                ToastUtils.show("设备注册地址!");
//                mEtDeviceRegisterAddress.requestFocus();
//                return false;
//            }
//            if (TextUtils.isEmpty(registerPort)) {
//                ToastUtils.show("设备注册端口!");
//                mEtDeviceRegisterPort.requestFocus();
//                return false;
//            }

        if (!TextUtils.isEmpty(registerPort)) {
            try {
                int port = Integer.parseInt(registerPort);
                if (port < 0 || port > 65535) {
                    ToastUtils.show("请输入正确的设备注册端口号!");
                    mEtDeviceRegisterPort.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的设备注册端口号!");
                mEtDeviceRegisterPort.requestFocus();
                return false;
            }
        }
//            if (TextUtils.isEmpty(registerCode)) {
//                ToastUtils.show("设备注册码!");
//                mEtDeviceRegisterCode.requestFocus();
//                return false;
//            }
//        }

        return true;
    }

    private void processSave() {
        DataCenterEntity dataCenterEntity = new DataCenterEntity();
        dataCenterEntity.setServerNumber(serverNumber);
        dataCenterEntity.setProtocol(transferProtocol);
        dataCenterEntity.setDatatype(dataProtocol);
        dataCenterEntity.setPlattype(platformType);
        dataCenterEntity.setAddr(dataServerAddress);
        dataCenterEntity.setPort(dataServerPort);
        dataCenterEntity.setDeviceid(deviceId);
        dataCenterEntity.setDevicekey(deviceKey);
        dataCenterEntity.setHttpaddr(registerAddress);
        dataCenterEntity.setHttpport(registerPort);
        dataCenterEntity.setProjid(productId);
        dataCenterEntity.setRegcode(registerCode);

        enableButtonOriginalState = mSbCenterEnable.isChecked();
        isSaveParamOperation = true;

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_SET_DATA_CENTER, dataCenterEntity);
        showProgressDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
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
            showDispatchFailedDialog(cmdStr);
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
    private void showDispatchFailedDialog(String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case MD_GET_DATA_CENTER:
            case MD_SET_DATA_CENTER:
                ToastUtils.show("下发指令失败");
                break;
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
        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case MD_GET_DATA_CENTER: {//获取设备的数据中心参数
                IOTCommandResult<DataCenterInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询数据中心参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                dataCenterInfo = commandResult.getResult();
                initDataCenterData();
            }
            break;

            case MD_SET_DATA_CENTER: {//设置设备的数据中心参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置数据中心参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                doAfterSetting();
            }
            break;

            default:
                break;
        }
    }

    private void doAfterSetting() {
        if (isSaveParamOperation) {
            ToastUtils.show("保存成功");
        }
        transferProtocolOld = transferProtocol;
        dataProtocolOld = dataProtocol;
        platformTypeOld = platformType;
        isResultOK = true;
    }

    private void initDataCenterData() {
        if (dataCenterInfo == null) {
            Timber.e("DataCenterInfo 为空!");
            dataCenterInfo = new DataCenterInfo();
            return;
        }
        transferProtocolOld = dataCenterInfo.getProtocol().trim();
        transferProtocol = dataCenterInfo.getProtocol().trim();

        dataProtocolOld = dataCenterInfo.getDatatype().trim();
        dataProtocol = dataCenterInfo.getDatatype().trim();

        platformTypeOld = dataCenterInfo.getPlattype().trim();
        platformType = dataCenterInfo.getPlattype().trim();

        dataServerAddress = dataCenterInfo.getAddr().trim();
        dataServerPort = dataCenterInfo.getPort().trim();
        deviceId = dataCenterInfo.getDeviceid().trim();
        deviceKey = dataCenterInfo.getDevicekey().trim();
        registerAddress = dataCenterInfo.getHttpaddr().trim();
        registerPort = dataCenterInfo.getHttpport().trim();
        productId = dataCenterInfo.getProjid().trim();
        registerCode = dataCenterInfo.getRegcode().trim();

        mTvTransferProtocol.setText(transferProtocolOld);
        if (transferProtocolOld.contains("TCP-C")) {
            mqttChildItemsLayout.setVisibility(View.GONE);
            transferProtocolPos = 0;
        } else if (transferProtocolOld.contains("TCP-S")) {
            mqttChildItemsLayout.setVisibility(View.GONE);
            transferProtocolPos = 1;
        } else if (transferProtocolOld.contains("MQTT")) {
            mqttChildItemsLayout.setVisibility(View.VISIBLE);
            transferProtocolPos = 2;
        }

        //"CMD", "NMEA", "DIFF_IN", "DIFF_OUT", "RAW_OUT", "RES_OUT"
        switch (dataProtocolOld) {
            case "1":
                dataProtocolPos = 0;
                mTvDataProtocol.setText("CMD");
                break;

            case "2":
                dataProtocolPos = 1;
                mTvDataProtocol.setText("NMEA");
                break;

            case "3":
                dataProtocolPos = 2;
                mTvDataProtocol.setText("DIFF_IN");
                break;

            case "4":
                dataProtocolPos = 3;
                mTvDataProtocol.setText("DIFF_OUT");
                break;

            case "5":
                dataProtocolPos = 4;
                mTvDataProtocol.setText("RAW_OUT");
                break;

            case "6":
                dataProtocolPos = 5;
                mTvDataProtocol.setText("RES_OUT");
                break;
        }

        switch (platformTypeOld) {
            case "0":
                platformTypePos = 0;
                mTvPlatformType.setText("地灾一期");
                break;
            case "1":
                platformTypePos = 1;
                mTvPlatformType.setText("成都理工平台");
                break;

            case "2":
                platformTypePos = 2;
                mTvPlatformType.setText("MDNET");
                break;

            case "3":
                platformTypePos = 3;
                mTvPlatformType.setText("地灾二期");
                break;
        }
        mEtDataServerAddress.setText(dataServerAddress);
        mEtDataServerPort.setText(dataServerPort);
        mEtDeviceId.setText(deviceId);
        mEtDeviceKey.setText(deviceKey);
        mEtDeviceRegisterAddress.setText(registerAddress);
        mEtDeviceRegisterPort.setText(registerPort);
        mEtProductId.setText(productId);
        mEtDeviceRegisterCode.setText(registerCode);
    }

    private void setResult() {
        Intent intent = new Intent();
        mActivity.setResult(isResultOK ? Activity.RESULT_OK : Activity.RESULT_CANCELED, intent);
    }

    @Override
    public boolean onBackPressed() {
        setResult();
        if (checkValueIsChange()) {
            warnNotYetSettingBeforeLeavePage();
            return true;
        } else {
            return false;
        }
    }

    private boolean checkValueIsChange() {
        if (!mSbCenterEnable.isChecked()) {
            return false;
        }
        if (enableButtonOriginalState != mSbCenterEnable.isChecked()) {
            return true;
        }

        if (dataServerAddress != null && !dataServerAddress.equals(mEtDataServerAddress.getText().toString().trim())) {
            return true;
        }
        if (dataServerPort != null && !dataServerPort.equals(mEtDataServerPort.getText().toString().trim())) {
            return true;
        }
        if (transferProtocolOld != null && transferProtocol != null && !transferProtocolOld.equals(transferProtocol)) {
            return true;
        }
        if (dataProtocolOld != null && dataProtocol != null && !dataProtocolOld.equals(dataProtocol)) {
            return true;
        }
        if (platformTypeOld != null && platformType != null && !platformTypeOld.equals(platformType)) {
            return true;
        }

        if (transferProtocol != null && transferProtocol.equals("MQTT")) {//MQTT自动注册
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