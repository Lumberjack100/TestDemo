package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 下发指令失败弹框
 */
public class DispatchCmdFailedDialog extends BaseDispatchCmdDialog {
    @BindView(R.id.tv_title)
    TextView mTvTitle;


    public DispatchCmdFailedDialog(String title) {
        this.title = title;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_dispatch_cmd_failed_dialog;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView() {
        mTvTitle.setText(title);
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
    protected void onCmdResponeSuccess(QueryCmdResult queryCmdResult) {

    }

}
