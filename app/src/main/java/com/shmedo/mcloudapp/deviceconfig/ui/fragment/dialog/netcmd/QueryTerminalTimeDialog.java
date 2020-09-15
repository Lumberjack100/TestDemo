package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.shmedo.configlibrary.iot.model.TerminalTime;
import com.shmedo.configlibrary.iot.parser.IOTParseManager;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.util.DateUtil;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 查询设备终端时间响应弹框
 */
public class QueryTerminalTimeDialog extends BaseDispatchCmdDialog {
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.contentView)
    View contentView;

    @BindView(R.id.tv_device_time)
    TextView mTvDeviceTime;

    @BindView(R.id.tv_system_time)
    TextView mTvSystemTime;

    @BindView(R.id.tv_time_diff_result)
    TextView mTvTimeDiffResult;

    @BindView(R.id.tv_time_diff_result_desc)
    TextView mTvTimeDiffResultDesc;

    private String deviceTime;

    public QueryTerminalTimeDialog(String title, List<String> msgIDList) {
        this.title = title;
        this.msgIDList.clear();
        this.msgIDList.addAll(msgIDList);
    }

    public QueryTerminalTimeDialog(String title, String deviceTime) {
        this.title = title;
        this.deviceTime = deviceTime;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_query_terminal_time_dialog;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        //msgIDList不为空时，表示当前是网络指令模式
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponse();
        }
    }

    private void initView() {
        mTvTitle.setText(title);
        contentView.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(deviceTime)) {
            contentView.setVisibility(View.VISIBLE);
            setTimeInfo();
        }
    }

    private void setTimeInfo() {
        String systemTime = DateUtil.getNowDateString();
        mTvDeviceTime.setText(deviceTime);
        mTvSystemTime.setText(systemTime);

        long diff = new Date().getTime() - DateUtil.stringToDate(deviceTime, "yyyy-MM-dd HH:mm:ss").getTime();

        if (diff <= 10 * 60 * 1000) {
            mTvTimeDiffResult.setText("正常");
            mTvTimeDiffResultDesc.setText("(差值小于10分钟)");
            mTvTimeDiffResult.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_3AD094));
        } else {
            mTvTimeDiffResult.setText("异常");
            mTvTimeDiffResultDesc.setText("(差值大于10分钟)");
            mTvTimeDiffResult.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        }
    }

    @OnClick({R.id.iv_close, R.id.tv_confirm})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
            case R.id.tv_confirm:
                stopRunnable();
                dismiss();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onCmdResponeSuccess(QueryCmdResult queryCmdResult) {
        contentView.setVisibility(View.VISIBLE);
        TerminalTime terminalTime = IOTParseManager.getInstance().parse(queryCmdResult.getResponseContent());
        deviceTime = terminalTime.getTime();
        setTimeInfo();
    }

}
