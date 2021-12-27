package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shmedo.mcloudapp.R;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 下发指令失败弹框
 */
public class DispatchCmdFailedDialog extends BaseDispatchCmdDialog {
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.tv_dispatch_cmd_failed_desc)
    TextView mTvDesc;//描述信息

    private String desc;

    public DispatchCmdFailedDialog(String title) {
        this.title = title;
    }

    public DispatchCmdFailedDialog(String title, String desc) {
        this.title = title;
        this.desc = desc;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_dispatch_cmd_failed_dialog;
    }

   @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        mTvTitle.setText(title);
        if (!TextUtils.isEmpty(desc)) {
            mTvDesc.setText(desc);
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


}
