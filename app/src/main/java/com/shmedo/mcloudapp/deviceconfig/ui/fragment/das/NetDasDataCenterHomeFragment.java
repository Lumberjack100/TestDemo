package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ColorUtils;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.ServerNumberEntity;
import com.shmedo.configlibrary.iot.cmd.entity.das.DasBdTerminalEntity;
import com.shmedo.configlibrary.iot.cmd.entity.das.DasDataReportEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.DataCenterStatus;
import com.shmedo.configlibrary.iot.model.das.DasBdTerminalInfo;
import com.shmedo.configlibrary.iot.model.das.DasDataReportInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

import org.jetbrains.annotations.NotNull;

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
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.reportingIntervalET)
    EditText mEtReportingInterval;

    @BindView(R.id.beiDouEnableSBtn)
    SwitchButton mSbBeiDouEnable;

    @BindView(R.id.beiDouChildsLayout)
    View beiDouChildsLayout;

    @BindView(R.id.targetAddressEt)
    EditText mEtTargetAddress;

    @BindView(R.id.tv_baudRate)
    TextView mTvBaudRate;

    @BindView(R.id.tv_data_center_one)
    TextView mTvDataCenterOne;

    @BindView(R.id.tv_data_center_two)
    TextView mTvDataCenterTwo;

    @BindView(R.id.tv_data_center_three)
    TextView mTvDataCenterThree;

    private String reportingInterval;

    private int baudRatePos;
    private String targetAddr;
    private String baudRateOld;
    private String baudRate;

    private DasDataReportInfo dataReportInfo;
    private DasBdTerminalInfo bdTerminalInfo;

    private boolean isBdTerminalParamChange = false;//判断有没有修改北斗数传终端参数
    private boolean isSaveParamOperation = false;//判断当前是保存参数操作，还是关闭北斗数传终端操作

    private static final int SERVER_NUMBER_ONE = 0x1001;
    private static final int SERVER_NUMBER_TWO = 0x1002;
    private static final int SERVER_NUMBER_THREE = 0x1003;
    private int serverNumber = -1;
    private ActivityResultLauncher<Intent> resultLauncher;

    public static NetDasDataCenterHomeFragment newInstance(DeviceInfo deviceInfo) {
        NetDasDataCenterHomeFragment fragment = new NetDasDataCenterHomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, deviceInfo);
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
        mRefreshLayout.autoRefreshAnimationOnly();//自动刷新，只显示动画不执行刷新
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
        return R.layout.das_data_center_home_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setFilter();
        setSwitchViewListener();
        initRefreshLayout();
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        mRefreshLayout.autoRefresh();
    }

    private void setFilter() {
        mEtReportingInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtTargetAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
    }

    /**
     * 开关控件事件
     */
    private void setSwitchViewListener() {
        //北斗数传终端启用开关事件
        mSbBeiDouEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mSbBeiDouEnable.setCheckedImmediatelyNoEvent(true);
                    beiDouChildsLayout.setVisibility(View.VISIBLE);
                } else {
                    mSbBeiDouEnable.setCheckedImmediatelyNoEvent(false);
                    beiDouChildsLayout.setVisibility(View.GONE);
                    disableBdTerminal();
                }
            }
        });
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                serverNumber = -1;
                getReportingTimeInfo();
            }
        });
    }

    /**
     * 获取上报时间信息
     */
    private void getReportingTimeInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_DATA_REPORT_TIME);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 获取北斗数传终端信息
     */
    private void getBdTerminalInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_BD_TERMINAL);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 关闭北斗数传终端信息
     */
    private void disableBdTerminal() {
        DasBdTerminalEntity entity = new DasBdTerminalEntity();
        entity.setSw("0");

        isSaveParamOperation = false;
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_BD_TERMINAL, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 获取数据中心状态
     */
    private void getDataCenterStatus(ServerNumber serverNumber) {
        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(serverNumber.toInt());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_GET_DATA_CENTER_STATUS, serverNumberEntity);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    @OnClick({R.id.ll_baudRate, R.id.dataCenterOneLayout, R.id.dataCenterTwoLayout, R.id.dataCenterThreeLayout, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_baudRate) {
            showBaudRateDialog();

        } else if (id == R.id.dataCenterOneLayout) {
            serverNumber = SERVER_NUMBER_ONE;
            DataCenterConfigActivity.startActivity(mActivity, resultLauncher, AppContants.DeviceType.DAS, deviceInfo, AppContants.DataCenterConfigMethod.ADVANCED_CONFIG, ServerNumber.NUMBER_ONE, mTvDataCenterOne.getText().toString());

        } else if (id == R.id.dataCenterTwoLayout) {
            serverNumber = SERVER_NUMBER_TWO;
            DataCenterConfigActivity.startActivity(mActivity, resultLauncher, AppContants.DeviceType.DAS, deviceInfo, AppContants.DataCenterConfigMethod.ADVANCED_CONFIG, ServerNumber.NUMBER_TWO, mTvDataCenterTwo.getText().toString());

        } else if (id == R.id.dataCenterThreeLayout) {
            serverNumber = SERVER_NUMBER_THREE;
            DataCenterConfigActivity.startActivity(mActivity, resultLauncher, AppContants.DeviceType.DAS, deviceInfo, AppContants.DataCenterConfigMethod.ADVANCED_CONFIG, ServerNumber.NUMBER_THREE, mTvDataCenterThree.getText().toString());
        } else if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            processSave();
        }
    }

    /**
     * 选择波特率弹框
     */
    private void showBaudRateDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"9600", "115200"},
                        null, baudRatePos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                baudRatePos = position;
                                mTvBaudRate.setText(text);
                                baudRate = text;
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
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

        if (!mSbBeiDouEnable.isChecked())
            return true;

        targetAddr = mEtTargetAddress.getText().toString().trim();
        if (TextUtils.isEmpty(targetAddr)) {
            ToastUtils.show("请输入北斗目标地址!");
            mEtTargetAddress.requestFocus();
            return false;
        }
        if (!ValidateUtil.isNumberSix(targetAddr)) {
            ToastUtils.show("请输入正确的北斗目标地址!");
            mEtTargetAddress.requestFocus();
            return false;
        }

        isBdTerminalParamChange = true;
        return true;
    }

    private void processSave() {
        DasDataReportEntity entity = new DasDataReportEntity();
        entity.setReport_intv(reportingInterval);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_DATA_REPORT_TIME, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }


    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(false);
            }
            dismissWaitDialog();
            ToastUtils.show("下发指令失败");
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
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(false);
        }
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
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(false);
        }
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
            case DAS_MD_GET_DATA_REPORT_TIME: {//
                IOTCommandResult<DasDataReportInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s %s", "查询数据上报时间出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                dataReportInfo = commandResult.getResult();
                initDataReportTime();
                getBdTerminalInfo();
            }
            break;

            case DAS_MD_GET_BD_TERMINAL: {//
                IOTCommandResult<DasBdTerminalInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s %s", "查询北斗数传终端参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                bdTerminalInfo = commandResult.getResult();
                initBdTerminalInfo();
                getDataCenterStatus(ServerNumber.NUMBER_ONE);
            }
            break;

            case MD_GET_DATA_CENTER_STATUS: {//获取设备的数据中心状态
                IOTCommandResult<DataCenterStatus> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s %s", "查询数据中心状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                DataCenterStatus centerStatus = commandResult.getResult();
                if (centerStatus.getCenterid() == 1) {
                    mTvDataCenterOne.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterOne.setTextColor(ColorUtils.getColor(getStatusColorResId(centerStatus.getStatus())));
                    //表示首次进入页面，需要逐个刷新所有的数据中心
                    if (serverNumber == -1) {
                        getDataCenterStatus(ServerNumber.NUMBER_TWO);
                    } else {
                        //表示刷新指定的数据中心
                        mRefreshLayout.finishRefresh(true);
                    }
                } else if (centerStatus.getCenterid() == 2) {
                    mTvDataCenterTwo.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterTwo.setTextColor(ColorUtils.getColor(getStatusColorResId(centerStatus.getStatus())));
                    //表示首次进入页面，需要逐个刷新所有的数据中心
                    if (serverNumber == -1) {
                        getDataCenterStatus(ServerNumber.NUMBER_THREE);
                    } else {
                        //表示刷新指定的数据中心
                        mRefreshLayout.finishRefresh(true);
                    }
                } else if (centerStatus.getCenterid() == 3) {
                    mRefreshLayout.finishRefresh(true);
                    mTvDataCenterThree.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterThree.setTextColor(ColorUtils.getColor(getStatusColorResId(centerStatus.getStatus())));
                }
            }
            break;

            case DAS_MD_SET_DATA_REPORT_TIME: {//
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "设置数据上报时间出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }

                if (mSbBeiDouEnable.isChecked() && isBdTerminalParamChange) {
                    DasBdTerminalEntity entity = new DasBdTerminalEntity();
                    entity.setSw("1");
                    entity.setDstaddr(targetAddr);
                    entity.setBaud(baudRate);

                    isSaveParamOperation = true;
                    String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_BD_TERMINAL, entity);
                    doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
                } else {
                    dismissWaitDialog();
                    ToastUtils.show("保存成功");
                }
            }
            break;

            case DAS_MD_SET_BD_TERMINAL: {//
                dismissWaitDialog();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置北斗数传终端参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                if (isSaveParamOperation) {
                    ToastUtils.show("保存成功");
                    baudRateOld = baudRate;
                }
            }
            break;

            default:
                break;
        }
    }

    private void initDataReportTime() {
        if (dataReportInfo == null) {
            Timber.e("DasDataReportInfo 为空!");
            dataReportInfo = new DasDataReportInfo();
            return;
        }

        reportingInterval = dataReportInfo.getReport_intv();
        mEtReportingInterval.setText(reportingInterval);
    }

    private void initBdTerminalInfo() {
        if (bdTerminalInfo == null) {
            Timber.e("DasBdTerminalInfo 为空!");
            bdTerminalInfo = new DasBdTerminalInfo();
            mSbBeiDouEnable.setCheckedImmediatelyNoEvent(false);
            beiDouChildsLayout.setVisibility(View.GONE);
            return;
        }

        if (bdTerminalInfo.getSw().equals("0")) {
            mSbBeiDouEnable.setCheckedImmediatelyNoEvent(false);
            beiDouChildsLayout.setVisibility(View.GONE);
        } else {
            mSbBeiDouEnable.setCheckedImmediatelyNoEvent(true);
            beiDouChildsLayout.setVisibility(View.VISIBLE);
        }
        targetAddr = bdTerminalInfo.getDstaddr();
        baudRate = bdTerminalInfo.getBaud();
        baudRateOld = bdTerminalInfo.getBaud();
        switch (baudRate) {
            case "9600":
                baudRatePos = 0;
                mTvBaudRate.setText("9600");
                break;
            case "115200":
                baudRatePos = 1;
                mTvBaudRate.setText("115200");
                break;
        }
        mEtTargetAddress.setText(targetAddr);
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
        if (reportingInterval != null && !reportingInterval.equals(mEtReportingInterval.getText().toString().trim())) {
            return true;
        }
        if (!mSbBeiDouEnable.isChecked()) {
            return false;
        }
        if (targetAddr != null && !targetAddr.equals(mEtTargetAddress.getText().toString().trim())) {
            return true;
        }
        if (baudRateOld != null && baudRate != null && !baudRateOld.equals(baudRate)) {
            return true;
        }
        return false;
    }
}
