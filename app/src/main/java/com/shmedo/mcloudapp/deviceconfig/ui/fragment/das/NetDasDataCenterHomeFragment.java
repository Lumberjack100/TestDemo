package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
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
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  4/13/21 <br/>
 * 描述：     TODO
 */
public class NetDasDataCenterHomeFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.tv_communication_method)
    TextView mTvCommunicationMethod;

    @BindView(R.id.reportingIntervalET)
    EditText mEtReportingInterval;

    @BindView(R.id.bdCardNumberET)
    EditText mEtBdCardNumber;

    @BindView(R.id.ll_bd_card_number)
    View bdCardNumberLayout;

    @BindView(R.id.tv_data_center_one)
    TextView mTvDataCenterOne;

    @BindView(R.id.tv_data_center_two)
    TextView mTvDataCenterTwo;

    @BindView(R.id.tv_data_center_three)
    TextView mTvDataCenterThree;

    private int pos = 1;
    private String dataCommunicationModeOld;
    private String dataCommunicationMode;
    private String reportingInterval;
    private String bdCardNumber;

    private String cmdDataReport;//数据上报间隔
    private String cmdBDCardNumber;//北斗卡号

    private static final int SERVER_NUMBER_ONE = 0x1001;
    private static final int SERVER_NUMBER_TWO = 0x1002;
    private static final int SERVER_NUMBER_THREE = 0x1003;
    private int serverNumber = -1;
    private ActivityResultLauncher<Intent> resultLauncher;

    public static NetDasDataCenterHomeFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetDasDataCenterHomeFragment fragment = new NetDasDataCenterHomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        showProgressDialog("加载中...");
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
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_das_data_center_home;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setFilter();
        showProgressDialog("加载中...");
        getDataCenterStatus(ServerNumber.NUMBER_ONE);
    }

    private void setFilter() {
        mEtReportingInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtBdCardNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
    }

    /**
     * 获取数据中心状态
     */
    private void getDataCenterStatus(ServerNumber serverNumber) {
        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(serverNumber.toInt());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_GET_DATA_CENTER_STATUS, serverNumberEntity);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    @OnClick({R.id.communicationMethodLayout, R.id.dataCenterOneLayout, R.id.dataCenterTwoLayout, R.id.dataCenterThreeLayout, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.communicationMethodLayout) {
            XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
            new XPopup.Builder(mActivity)
                    .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                    .asBottomList("", new String[]{"4G", "SMS", "BD", "BD+4G"},
                            null, pos, true,
                            new OnSelectListener() {
                                @Override
                                public void onSelect(int position, String text) {
                                    pos = position;
                                    mTvCommunicationMethod.setText(text);

                                    switch (text) {
                                        case "4G":
                                            dataCommunicationMode = "1";
                                            bdCardNumberLayout.setVisibility(View.GONE);
                                            break;

                                        case "SMS":
                                            dataCommunicationMode = "2";
                                            bdCardNumberLayout.setVisibility(View.GONE);
                                            break;

                                        case "BD":
                                            dataCommunicationMode = "3";
                                            bdCardNumberLayout.setVisibility(View.VISIBLE);
                                            break;

                                        case "BD+4G":
                                            dataCommunicationMode = "4";
                                            bdCardNumberLayout.setVisibility(View.VISIBLE);
                                            break;
                                    }
                                }
                            }, 0, R.layout.custom_xpopup_adapter_text_match)
                    .show();
        } else if (id == R.id.dataCenterOneLayout) {
            serverNumber = SERVER_NUMBER_ONE;
            DataCenterConfigActivity.startActivity(mActivity, resultLauncher, AppContants.DeviceType.DAS, projectDeviceInfo, AppContants.DataCenterConfigMethod.ADVANCED_CONFIG, ServerNumber.NUMBER_ONE, mTvDataCenterOne.getText().toString());

        } else if (id == R.id.dataCenterTwoLayout) {
            serverNumber = SERVER_NUMBER_TWO;
            DataCenterConfigActivity.startActivity(mActivity, resultLauncher, AppContants.DeviceType.DAS, projectDeviceInfo, AppContants.DataCenterConfigMethod.ADVANCED_CONFIG, ServerNumber.NUMBER_TWO, mTvDataCenterTwo.getText().toString());

        } else if (id == R.id.dataCenterThreeLayout) {
            serverNumber = SERVER_NUMBER_THREE;
            DataCenterConfigActivity.startActivity(mActivity, resultLauncher, AppContants.DeviceType.DAS, projectDeviceInfo, AppContants.DataCenterConfigMethod.ADVANCED_CONFIG, ServerNumber.NUMBER_THREE, mTvDataCenterThree.getText().toString());
        } else if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            processSave();
        }
    }

    private boolean checkValueIsValid() {
        reportingInterval = mEtReportingInterval.getText().toString().trim();

        if (TextUtils.isEmpty(reportingInterval)) {
            ToastUtils.show("请输入上报间隔!");
            mEtReportingInterval.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(reportingInterval);
            if (value < 1 || value > 9999) {
                ToastUtils.show("请输入正确的上报间隔!");
                mEtReportingInterval.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的上报间隔!");
            mEtReportingInterval.requestFocus();
            return false;
        }

        if (dataCommunicationMode.equals("3") || dataCommunicationMode.equals("4")) {
            bdCardNumber = mEtBdCardNumber.getText().toString().trim();
            if (TextUtils.isEmpty(bdCardNumber)) {
                ToastUtils.show("请输入北斗卡号!");
                mEtBdCardNumber.requestFocus();
                return false;
            }
            if (!ValidateUtil.isNumberSix(bdCardNumber)) {
                ToastUtils.show("请输入正确的北斗卡号!");
                mEtBdCardNumber.requestFocus();
                return false;
            }

            //设置六位目标北斗卡号
//            SixTargerBDNumberEntity bdNumberEntity = new SixTargerBDNumberEntity(bdCardNumber);
//            cmdBDCardNumber = CommandManager.getInstance().getCommand(CommandType.SIX_TARGER_BD_NUMBER, bdNumberEntity);
        }

        return true;
    }

    private void processSave() {

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
            ToastUtils.show("下发指令失败");
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponseRunnable(0);
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
        ToastUtils.show("查询设备响应错误");
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultTimeOut(queryCmdResult);
        ToastUtils.show("查询设备响应超时");
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case MD_GET_DATA_CENTER_STATUS: {//获取设备的数据中心状态
                IOTCommandResult<DataCenterStatus> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    dismissProgressDialog();
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
                        dismissProgressDialog();
                    }
                } else if (centerStatus.getCenterid() == 2) {
                    mTvDataCenterTwo.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterTwo.setTextColor(GlobalUtil.getColor(getStatusColorResId(centerStatus.getStatus())));
                    //表示首次进入页面，需要逐个刷新所有的数据中心
                    if (serverNumber == -1) {
                        getDataCenterStatus(ServerNumber.NUMBER_THREE);
                    } else {
                        //表示刷新指定的数据中心
                        dismissProgressDialog();
                    }
                } else if (centerStatus.getCenterid() == 3) {
                    dismissProgressDialog();
                    mTvDataCenterThree.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterThree.setTextColor(GlobalUtil.getColor(getStatusColorResId(centerStatus.getStatus())));
                }
            }
            break;

            default:
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
    public boolean onBackPressed() {
        if (checkValueIsChange()) {
            warnNotYetSettingBeforeLeavePage();
            return true;
        } else {
            return false;
        }
    }

    private boolean checkValueIsChange() {
        if (dataCommunicationModeOld != null && dataCommunicationMode != null && !dataCommunicationModeOld.equals(dataCommunicationMode)) {
            return true;
        }
        if (reportingInterval != null && !reportingInterval.equals(mEtReportingInterval.getText().toString().trim())) {
            return true;
        }
        if (bdCardNumber != null && !bdCardNumber.equals(mEtBdCardNumber.getText().toString().trim())) {
            return true;
        }
        return false;
    }
}
