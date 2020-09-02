package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shmedo.iot.parser.IOTParseManager;
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


    public TelemetryDialog(String title, List<String> msgIDList) {
        this.title = title;
        this.msgIDList.clear();
        this.msgIDList.addAll(msgIDList);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_telemetry_dialog;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        showResponseLoadingView();
        startRunnable(2000);
    }

    private void initView() {
        mTvTitle.setText(title);
        contentView.setVisibility(View.GONE);
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
        String content = IOTParseManager.getInstance().parse(queryCmdResult.getResponseContent());
        content = content.replace("datastreams=", "");
        mTvResponseContent.setText(content);
    }


}
