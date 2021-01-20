package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 设备重新启动/恢复出厂设置等响应弹框
 */
public class CommonCmdDialog extends BaseDispatchCmdDialog {
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.contentView)
    View contentView;

    @BindView(R.id.tv_response_success_desc)
    TextView mTvDesc;//描述信息

    @BindView(R.id.tv_response_success_tip)
    TextView mTvTip;//温馨提示

    private String desc;
    private String tip;


    /**
     *
     * @param title 标题
     * @param desc 响应成功显示内容
     * @param msgIDList
     */
    public CommonCmdDialog(String title, String desc, List<String> msgIDList) {
        this.title = title;
        this.desc = desc;
        this.tip = null;
        this.msgIDList.clear();
        this.msgIDList.addAll(msgIDList);
    }

    /**
     *
     * @param title 标题
     * @param desc 响应成功显示内容
     * @param tip 响应成功温馨提示
     * @param msgIDList
     */
    public CommonCmdDialog(String title, String desc, String tip, List<String> msgIDList) {
        this.title = title;
        this.desc = desc;
        this.tip = tip;
        this.msgIDList.clear();
        this.msgIDList.addAll(msgIDList);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_reboot_dialog;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponse();
        }
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

        CommonSettingCmdResult cmdResponseResult = IOTParseManager.getInstance().parseSettingCmd(queryCmdResult.getResponseContent());
        if (cmdResponseResult.isSucceed()) {
            mTvDesc.setText(desc);
            if (!TextUtils.isEmpty(tip)) {
                mTvTip.setVisibility(View.VISIBLE);
                mTvTip.setText(tip);
            } else {
                mTvTip.setVisibility(View.GONE);
            }
        } else {
            mTvDesc.setText(String.format("%s失败! %s", title, cmdResponseResult.getReason()));
        }
    }
}
