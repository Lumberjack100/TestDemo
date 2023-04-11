package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.DebouncingUtils;
import com.hjq.toast.ToastUtils;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.das.DasFixedPointReportEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.das.DasFixedPointReportInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class NetFixedPointReportingFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.tv_reporting_method)
    TextView mTvReportingMethod;//上报方式

    @BindView(R.id.tv_reporting_start_time)
    TextView mTvReportingStartTime;//上报起始时间

    @BindView(R.id.reportingIntervalET)
    EditText mEtReportingInterval;//上报间隔

    @BindView(R.id.ll_reporting_start_time)
    ViewGroup reportingStartTimeLayout;

    private String reportingMethod;//上报方式
    private String reportingInterval;//上报间隔

    private final String[] reportingMethods = new String[]{"定时上报", "定时定点上报"};

    private DasFixedPointReportInfo dasFixedPointReportInfo = new DasFixedPointReportInfo();

    public static NetFixedPointReportingFragment newInstance(DeviceInfo deviceInfo) {
        NetFixedPointReportingFragment fragment = new NetFixedPointReportingFragment();
        Bundle args = new Bundle();
        args.putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_net_fixed_point_reporting;
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
        mEtReportingInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        mTvReportingMethod.setText(reportingMethods[0]);
        reportingMethod = "0";

        mTvReportingStartTime.setText("0");
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                queryReportingInfo();
            }
        });
    }

    /**
     * 查询定时上报参数
     */
    private void queryReportingInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_DATA_REPORT_TYPE);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    @OnClick({R.id.ll_reporting_method, R.id.ll_reporting_start_time, R.id.btn_confirm})
    public void onClick(View view) {
        if(!DebouncingUtils.isValid(view, 1000)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_reporting_method) {
            showReportingMethodDialog();

        } else if (id == R.id.ll_reporting_start_time) {
            showTimePicker();

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
     * 选择上报方式
     */
    public void showReportingMethodDialog() {
        int pos = Arrays.asList(reportingMethods).indexOf(String.valueOf(mTvReportingMethod.getText()));
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", reportingMethods,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvReportingMethod.setText(text);
                                if (position == 0) {
                                    reportingMethod = "0";
                                    reportingStartTimeLayout.setVisibility(View.GONE);
                                } else {
                                    reportingMethod = "1";
                                    reportingStartTimeLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    private void showTimePicker() {
        new TimePickerDialog(mActivity, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String time = String.format(Locale.getDefault(), "%2d", hourOfDay);
                mTvReportingStartTime.setText(time);
            }
        }, 0, 0, true).show();
    }

    private boolean checkValueIsValid() {
        reportingInterval = mEtReportingInterval.getText().toString();

        if (!dasFixedPointReportInfo.getTimegap().equals(reportingInterval)) {
            if (TextUtils.isEmpty(reportingInterval)) {
                ToastUtils.show("请输入上报时间间隔!");
                mEtReportingInterval.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(reportingInterval);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的上报时间间隔!");
                mEtReportingInterval.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void processSave() {
        DasFixedPointReportEntity entity = new DasFixedPointReportEntity();
        entity.setType(reportingMethod);
        entity.setTimepoint(mTvReportingStartTime.getText().toString());
        entity.setTimegap(reportingInterval);

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_DATA_REPORT_TYPE, entity);
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
            case DAS_MD_GET_DATA_REPORT_TYPE: {//
                mRefreshLayout.finishRefresh(true);
                IOTCommandResult<DasFixedPointReportInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询定时上报参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    MessageDialog.show("提示", commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg, "我已知晓");
                    return;
                }
                dasFixedPointReportInfo = commandResult.getResult();
                initParamInfo();
            }
            break;

            case DAS_MD_SET_DATA_REPORT_TYPE: {//
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置定时上报参数出错!", cmdResult.getReason());
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

    private void initParamInfo() {
        if (dasFixedPointReportInfo == null) {
            Timber.e("DasFixedPointReportInfo 为空!");
            dasFixedPointReportInfo = new DasFixedPointReportInfo();
            return;
        }
        reportingMethod = dasFixedPointReportInfo.getType();
        reportingInterval = dasFixedPointReportInfo.getTimegap();

        if (reportingMethod.equals("0")) {
            mTvReportingMethod.setText(reportingMethods[0]);
            reportingStartTimeLayout.setVisibility(View.GONE);
        } else {
            mTvReportingMethod.setText(reportingMethods[1]);
            reportingStartTimeLayout.setVisibility(View.VISIBLE);
        }
        mTvReportingStartTime.setText(dasFixedPointReportInfo.getTimepoint());
        mEtReportingInterval.setText(reportingInterval);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}