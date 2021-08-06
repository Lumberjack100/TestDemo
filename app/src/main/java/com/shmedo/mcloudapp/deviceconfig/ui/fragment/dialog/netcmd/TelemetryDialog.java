package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 设备遥测响应弹框
 */
public class TelemetryDialog extends BaseDispatchCmdDialog {
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.contentView)
    View contentView;

    @BindView(R.id.tv_response_content)
    TextView mTvResponseContent;

    private String result;


    public TelemetryDialog(String title, List<String> msgIDList) {
        this.title = title;
        this.msgIDList.clear();
        this.msgIDList.addAll(msgIDList);
    }

    public TelemetryDialog(String title, String result) {
        this.title = title;
        this.result = result;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_telemetry_dialog;
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
        if (!TextUtils.isEmpty(result)) {
            contentView.setVisibility(View.VISIBLE);
            mTvResponseContent.setText(result);
        }
    }

    @OnClick({R.id.iv_close, R.id.tv_confirm})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
            case R.id.tv_confirm:
                dismiss();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
        contentView.setVisibility(View.VISIBLE);
        IOTCommandResult<String> commandResult = IOTParseManager.getInstance().parse(queryCmdResult.getResponseContent());
        if (!commandResult.isSuccess()) {
            return;
        }
        String content = commandResult.getResult();
        content = content.replace("datastreams=", "");
        mTvResponseContent.setText(content);
    }
}
