package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.model.CollectorConfigInfo;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.model.params.DispatchRawCmdParam;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 通过物联网平台配置采集器
 */
public class NetCollectorSettingFragment extends BaseNetIotCommunicateFragment {
    private static final String DEVICE_ID = "device_id";

    @BindView(R.id.collectorAddressET)
    EditText mEtCollectorAddress;

    @BindView(R.id.calculatingTimeET)
    EditText mEtCalculatingTime;

    @BindView(R.id.standbyTimeET)
    EditText mEtStandbyTime;

    @BindView(R.id.collectTimeET)
    EditText mEtCollectTime;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    private CollectorConfigInfo collectorConfigInfo = new CollectorConfigInfo();
    private String collectorAddress;//采集器地址
    private String calculatTime;//解算时间频度
    private String standbyTime;//待机时间
    private String collectTime;//采集时间频度

    private int deviceid;

    private static final int GET_COLLECTOR_INFO = 0x1000;
    private static final int SET_COLLECTOR_INFO = 0x1001;
    private int operaType = -1;

    public static NetCollectorSettingFragment newInstance(int deviceid) {
        NetCollectorSettingFragment fragment = new NetCollectorSettingFragment();
        Bundle args = new Bundle();
        args.putInt(DEVICE_ID, deviceid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceid = getArguments().getInt(DEVICE_ID, -1);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_net_collector_setting;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setFilter();
        queryCollectorInfo();
    }

    private void setFilter() {
        mEtCollectorAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCalculatingTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtStandbyTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtCollectTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
    }

    private void queryCollectorInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL);
        operaType = GET_COLLECTOR_INFO;
        doCommonDispatchRawCmd(command);
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
        collectorAddress = mEtCollectorAddress.getText().toString().trim();
        calculatTime = mEtCalculatingTime.getText().toString().trim();
        standbyTime = mEtStandbyTime.getText().toString().trim();
        collectTime = mEtCollectTime.getText().toString().trim();

        if (TextUtils.isEmpty(collectorAddress)) {
            ToastUtils.show("采集器地址不能为空");
            mEtCollectorAddress.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(collectorAddress);
            if (port < 0 || port > 255) {
                ToastUtils.show("请输入正确的采集器地址!");
                mEtCollectorAddress.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的采集器地址!");
            mEtCollectorAddress.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(calculatTime)) {
            ToastUtils.show("解算频度不能为空");
            mEtCalculatingTime.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(standbyTime)) {
            ToastUtils.show("待机时长不能为空");
            mEtStandbyTime.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(collectTime)) {
            ToastUtils.show("采集频度不能为空");
            mEtCollectTime.requestFocus();
            return false;
        }

        return true;
    }

    private void processSave() {
        mBtnSave.setEnabled(false);
//        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_COLLECTOR_CONTROL, dataCenterEntity);
//        operaType = SET_COLLECTOR_INFO;
//        doCommonDispatchRawCmd(command);
    }

    /**
     * 调用指令透传接口
     *
     * @param content
     */
    private void doCommonDispatchRawCmd(String content) {
        DispatchRawCmdParam rawCmdParam = new DispatchRawCmdParam();
        rawCmdParam.setContent(content);
        rawCmdParam.setCompanyID(MCloudApp.getCompanyID());
        rawCmdParam.setDeviceIDList(Arrays.asList(deviceid));

        showProgressDialog("处理中...");
        processDispatchRawCmd(rawCmdParam);
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        dismissProgressDialog();
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            showDispatchFailedDialog();
            return;
        }

        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponseRunnable(2000);
        }
    }

    /**
     * 指令下发失败弹框
     */
    private void showDispatchFailedDialog() {
        mBtnSave.setEnabled(true);
        ToastUtils.show("下发指令失败");
    }

    /**
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        mBtnSave.setEnabled(true);
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
        mBtnSave.setEnabled(true);
        ToastUtils.show("查询设备响应超时");
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
            case DAS_MD_GET_COLLECTOR_CONTROL: {//
                IOTCommandResult<CollectorConfigInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询数据中心参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                collectorConfigInfo = commandResult.getResult();
                initCollectorInfo();
            }
            break;

            case DAS_MD_SET_COLLECTOR_CONTROL: {//
//                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置数据中心参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    mBtnSave.setEnabled(true);
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
        mBtnSave.setEnabled(true);
        ToastUtils.show("保存成功");
    }

    private void initCollectorInfo(){
        if (collectorConfigInfo == null) {
            Timber.e("CollectorConfigInfo 为空!");
            collectorConfigInfo = new CollectorConfigInfo();
            return;
        }

        collectorAddress = collectorConfigInfo.getCollectorAddress();
        calculatTime = collectorConfigInfo.getWorkTime();
        standbyTime = collectorConfigInfo.getStandbyTime();
        collectTime = collectorConfigInfo.getCollectorInterval();

        mEtCollectorAddress.setText(collectorAddress);
        mEtCalculatingTime.setText(calculatTime);
        mEtStandbyTime.setText(standbyTime);
        mEtCollectTime.setText(collectTime);
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
        if (collectorAddress != null && !collectorAddress.equals(mEtCollectorAddress.getText().toString().trim())) {
            return true;
        }

        if (calculatTime != null && !calculatTime.equals(mEtCalculatingTime.getText().toString().trim())) {
            return true;
        }

        if (standbyTime != null && !standbyTime.equals(mEtStandbyTime.getText().toString().trim())) {
            return true;
        }

        if (collectTime != null && !collectTime.equals(mEtCollectTime.getText().toString().trim())) {
            return true;
        }

        return false;
    }
}
