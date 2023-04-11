package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.DebouncingUtils;
import com.hjq.toast.ToastUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.das.DasCollectorEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.das.DasCollectorInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 通过物联网平台配置采集器
 */
public class NetDasCollectorSettingFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.collectorAddressET)
    EditText mEtCollectorAddress;

    @BindView(R.id.calculatingTimeET)
    EditText mEtCalculatingTime;

    @BindView(R.id.standbyTimeET)
    EditText mEtStandbyTime;

    @BindView(R.id.collectTimeET)
    EditText mEtCollectTime;

    @BindView(R.id.et_sensitivity)
    EditText mEtSensitivity;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.ll_sensitivity)
    ViewGroup sensitivityLayout;

    private String collectorAddress;//采集器地址
    private String calculatTime;//解算时间频度
    private String standbyTime;//待机时间
    private String collectTime;//采集时间频度
    private String sensitivity;//灵敏度

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private DasCollectorInfo collectorInfo = new DasCollectorInfo();

    public static NetDasCollectorSettingFragment newInstance(DeviceInfo deviceInfo) {
        NetDasCollectorSettingFragment fragment = new NetDasCollectorSettingFragment();
        Bundle args = new Bundle();
        args.putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_das_collector_setting;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setFilter();
        initRefreshLayout();
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        mRefreshLayout.autoRefresh();
    }

    private void setFilter() {
        mEtCollectorAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCalculatingTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtStandbyTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtCollectTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtSensitivity.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        mEtCollectorAddress.setHint("0-255");
        mEtSensitivity.setHint("30-150");
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                queryCollectorInfo();
            }
        });
    }

    private void queryCollectorInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        if(!DebouncingUtils.isValid(view, 1000)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (collectorInfo == null || TextUtils.isEmpty(collectorInfo.getType())) {
                ToastUtils.show("未获取到采集器信息，请先刷新完成后再试!");
                return;
            }
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
            ToastUtils.show("请输入采集器地址!");
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

        if (!calculatTime.equals(collectorInfo.getCalcgap())) {
            if (TextUtils.isEmpty(calculatTime)) {
                ToastUtils.show("请输入解算时间!");
                mEtCalculatingTime.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(calculatTime);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的解算时间!");
                mEtCalculatingTime.requestFocus();
                return false;
            }
        } else {
            calculatTime = null;
        }

        if (!standbyTime.equals(collectorInfo.getStandbygap())) {
            if (TextUtils.isEmpty(standbyTime)) {
                ToastUtils.show("请输入待机时间!");
                mEtStandbyTime.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(standbyTime);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的待机时间!");
                mEtStandbyTime.requestFocus();
                return false;
            }
        } else {
            standbyTime = null;
        }

        if (!collectTime.equals(collectorInfo.getCollgap())) {
            if (TextUtils.isEmpty(collectTime)) {
                ToastUtils.show("请输入采集时间!");
                mEtCollectTime.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(collectTime);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的采集时间!");
                mEtStandbyTime.requestFocus();
                return false;
            }
        } else {
            collectTime = null;
        }

        if (!"NullKey".equals(sensitivity)) {
            sensitivity = mEtSensitivity.getText().toString().trim();
            try {
                double value = Double.parseDouble(sensitivity);
                if (value < 1) {
                    ToastUtils.show("请输入正确的灵敏度!");
                    mEtSensitivity.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的灵敏度!");
                mEtSensitivity.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void processSave() {
        DasCollectorEntity entity = new DasCollectorEntity();
        entity.setType(collectorInfo.getType());
        entity.setAddr(collectorAddress);
        entity.setCalcgap(calculatTime);
        entity.setStandbygap(standbyTime);
        entity.setCollgap(collectTime);
        entity.setSensitivity("NullKey".equals(collectorInfo.getSensitivity()) ? "NullKey" : decimalFormat.format(Double.parseDouble(sensitivity)));

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_COLLECTOR_CONTROL, entity);
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
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(false);
        }
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
            case DAS_MD_GET_COLLECTOR_CONTROL: {//
                mRefreshLayout.finishRefresh(true);
                IOTCommandResult<DasCollectorInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询采集器参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                collectorInfo = commandResult.getResult();
                initCollectorInfo();
            }
            break;

            case DAS_MD_SET_COLLECTOR_CONTROL: {//
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置采集器参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("保存成功");
            }
            break;

            default:
                mRefreshLayout.finishRefresh(true);
                break;
        }
    }

    private void initCollectorInfo() {
        if (collectorInfo == null) {
            Timber.e("DasCollectorInfo 为空!");
            collectorInfo = new DasCollectorInfo();
            return;
        }
        collectorAddress = collectorInfo.getAddr();
        calculatTime = collectorInfo.getCalcgap();
        standbyTime = collectorInfo.getStandbygap();
        collectTime = collectorInfo.getCollgap();
        sensitivity = collectorInfo.getSensitivity();

        mEtCollectorAddress.setText(collectorAddress);
        mEtCalculatingTime.setText(calculatTime);
        mEtStandbyTime.setText(standbyTime);
        mEtCollectTime.setText(collectTime);
        try {
            if ("NullKey".equals(sensitivity)) {
                sensitivityLayout.setVisibility(View.GONE);
            } else {
                sensitivityLayout.setVisibility(View.VISIBLE);
                sensitivity = decimalFormat.format(Double.parseDouble(sensitivity));
                mEtSensitivity.setText(sensitivity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        if (sensitivity != null && !sensitivity.equals("NullKey") && !sensitivity.equals(mEtSensitivity.getText().toString().trim())) {
            return true;
        }
        return false;
    }
}
