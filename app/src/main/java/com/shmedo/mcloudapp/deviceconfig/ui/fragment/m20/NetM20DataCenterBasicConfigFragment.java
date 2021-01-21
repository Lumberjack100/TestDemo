package com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.entity.DataCenterEntity;
import com.shmedo.configlibrary.iot.cmd.entity.ServerNumberEntity;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.configlibrary.iot.model.DataCenterInfo;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.params.DispatchRawCmdParam;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.CommonCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.DispatchCmdFailedDialog;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/21/21 <br/>
 * 描述：    M20网络模式  数据中心基本参数配置页面
 */
public class NetM20DataCenterBasicConfigFragment extends BaseNetIotCommunicateFragment {
    public static final String PRO_DEVICE_INFO = "com.shmedo.mcloudapp.PRO_DEVICE_INFO";

    @BindView(R.id.maskLayerChild)
    ViewGroup maskLayerLayout;

    @BindView(R.id.centerEnableSBtn)
    SwitchButton mSbCenterEnable;

    @BindView(R.id.et_data_server_address)
    ClearEditText mEtDataServerAddress;

    @BindView(R.id.et_data_server_port)
    ClearEditText mEtDataServerPort;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    private ServerNumber serverNumber;
    private String serverStatus;
    private DataCenterInfo dataCenterInfo;

    private String dataServerAddress;//数据服务器地址
    private String dataServerPort;//数据服务器端口

    private boolean centerEnableInitial;//数据中心开关初始状态，用于判断开关是否有打开后没有设置参数就返回
    private boolean isSaveParamOperation = false;//判断当前是保存参数操作，还是关闭数据中心操作

    private ProjectDeviceInfo projectDeviceInfo;

    private static final int GET_DATA_SENTER = 0x1000;
    private static final int SET_DATA_SENTER = 0x1001;
    private int operaType = -1;


    public static NetM20DataCenterBasicConfigFragment newInstance(ServerNumber serverNumber, String status, ProjectDeviceInfo projectDeviceInfo) {
        NetM20DataCenterBasicConfigFragment fragment = new NetM20DataCenterBasicConfigFragment();
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
            projectDeviceInfo = getArguments().getParcelable(PRO_DEVICE_INFO);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.net_m20_data_center_basic_config_fragment;
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

        //数据中心地址为空表示数据中心未启用
        if (!TextUtils.isEmpty(serverStatus) && serverStatus.contains("未开启")) {
            centerEnableInitial = false;
            mSbCenterEnable.setCheckedImmediatelyNoEvent(false);
            maskLayerLayout.setVisibility(View.VISIBLE);
        } else {
            centerEnableInitial = true;
            mSbCenterEnable.setCheckedImmediatelyNoEvent(true);
            maskLayerLayout.setVisibility(View.GONE);
        }
    }

    private void setSwitchViewListener() {
        mSbCenterEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定要关闭数据中心？");
                } else {
                    maskLayerLayout.setVisibility(View.GONE);
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
                        maskLayerLayout.setVisibility(View.VISIBLE);
                        centerEnableInitial = mSbCenterEnable.isChecked();
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

        DispatchRawCmdParam rawCmdParam = new DispatchRawCmdParam();
        rawCmdParam.setContent(command);
        rawCmdParam.setCompanyID(MCloudApp.getCompanyID());
        rawCmdParam.setDeviceIDList(Arrays.asList(projectDeviceInfo.getId()));

        operaType = GET_DATA_SENTER;
        showProgressDialog("指令下发中...");
        processDispatchRawCmd(rawCmdParam);
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
        mBtnSave.setEnabled(false);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_SET_DATA_CENTER, dataCenterEntity);
//        sendCommand(command);
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }

            processSave();
        }
    }

    private boolean checkValueIsValid() {
        dataServerAddress = mEtDataServerAddress.getText().toString().trim();
        dataServerPort = mEtDataServerPort.getText().toString().trim();

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
                ToastUtils.show("请输入正确的数据中心端口号!");
                mEtDataServerPort.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的数据中心端口号!");
            mEtDataServerPort.requestFocus();
            return false;
        }

        return true;
    }

    private void processSave() {
        DataCenterEntity dataCenterEntity = new DataCenterEntity();
        dataCenterEntity.setServerNumber(serverNumber);
        dataCenterEntity.setAddr(dataServerAddress);
        dataCenterEntity.setPort(dataServerPort);

        centerEnableInitial = mSbCenterEnable.isChecked();
        isSaveParamOperation = true;
        mBtnSave.setEnabled(false);

//        errMsg = "发送指令超时,请稍后尝试";
//        startProgressRunnable("正在发送配置指令...", WRITE_TIME_OUT_SECOND);
//        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_SET_DATA_CENTER, dataCenterEntity);
//        sendCommand(command);
    }

    @Override
    protected void onDispatchCmdItemList(List<DispatchCmdItem> dispatchCmdItemList) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            showDispatchFailedDialog();
            return;
        }

        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        showDispatchSuccessDialog();
    }

    /**
     * 指令下发失败弹框
     */
    private void showDispatchFailedDialog() {
        String title = "数据中心";
        BaseDispatchCmdDialog newFragment = new DispatchCmdFailedDialog(title);
        newFragment.show(getChildFragmentManager(), "dialog");
    }

    /**
     * 指令下发成功弹框
     */
    private void showDispatchSuccessDialog() {
        BaseDispatchCmdDialog newFragment = new CommonCmdDialog("数据中心", "水平初始化完成", msgIDList);
        if (newFragment != null)
            newFragment.show(getChildFragmentManager(), "dialog");
    }


    @Override
    public boolean onBackPressed() {
        if (checkValueIsChange()) {
            warnNotYetSettingBeforeLeavePage();
            return true;
        } else {
            return false;
        }
    }

    private boolean checkValueIsChange() {
        if (centerEnableInitial != mSbCenterEnable.isChecked()) {
            return true;
        }

        if (dataServerAddress != null && !dataServerAddress.equals(mEtDataServerAddress.getText().toString().trim())) {
            return true;
        }

        if (dataServerPort != null && !dataServerPort.equals(mEtDataServerPort.getText().toString().trim())) {
            return true;
        }

        return false;
    }
}