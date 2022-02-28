package com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/19/21 <br/>
 * 描述：    网络模式M20 设置向导弹窗
 */
public class NetM20SetupWizardDialogFragment extends BaseDispatchCmdDialog {
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.iv_close)
    ImageView mIvClose;

    @BindView(R.id.tv_content)
    TextView mTvContent;

    @BindView(R.id.dispatchCmdFailedView)
    View dispatchCmdFailedView;

    @BindView(R.id.tv_dispatch_cmd_failed_desc)
    TextView mTvDispatchCmdFailedDesc;//下发指令错误描述信息

    @BindView(R.id.tv_left)
    TextView mTvLeft;

    @BindView(R.id.tv_right)
    TextView mTvRight;

    private NetM20HomeFragment netM20HomeFragment;


    public static NetM20SetupWizardDialogFragment newInstance() {
        return new NetM20SetupWizardDialogFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.net_m20_setup_wizard_dialog_fragment;
    }

   @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        netM20HomeFragment = (NetM20HomeFragment) getParentFragment();
        initView();
    }

    private void initView() {
        mTvTitle.setText("设置向导");
        mTvContent.setText("是否水平初始化？");
        mTvDispatchCmdFailedDesc.setText("水平初始化失败");
        mTvContent.setVisibility(View.VISIBLE);
        dispatchCmdFailedView.setVisibility(View.GONE);
        mTvLeft.setText("跳过");
        mTvRight.setText("是");
    }

    @OnClick({R.id.iv_close, R.id.tv_left, R.id.tv_right})
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_close) {
            dismiss();

        } else if (id == R.id.tv_left) {
            dismiss();
            DataCenterHomeActivity.startActivity(mActivity, ProductType.M20, netM20HomeFragment.deviceInfo, AppContants.DataCenterConfigMethod.BASIC_CONFIG, true);

        } else if (id == R.id.tv_right) {
            mTvContent.setText("下发水平初始化指令...");
            disableTouch();
            netM20HomeFragment.setLevelInitial();
        }
    }

    private void disableTouch() {
        mIvClose.setEnabled(false);
        mTvLeft.setEnabled(false);
        mTvRight.setEnabled(false);
    }

    private void enableTouch() {
        mIvClose.setEnabled(true);
        mTvLeft.setEnabled(true);
        mTvRight.setEnabled(true);
    }

    public void updateDispatchCmdResult(boolean isSucc, List<String> msgIDList) {
        mTvContent.setVisibility(View.GONE);
        //下发指令失败
        if (!isSucc) {
            dispatchCmdFailedView.setVisibility(View.VISIBLE);
            mTvRight.setText("重新尝试");
            enableTouch();
            return;
        }

        //下发指令成功，等待响应，查询响应结果
        if (msgIDList != null && msgIDList.size() > 0) {
            this.msgIDList.clear();
            this.msgIDList.addAll(msgIDList);
            startQueryCmdResponse();
        }
    }

    /**
     * 查询指令响应结果成功
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
        enableTouch();
        mTvContent.setVisibility(View.VISIBLE);
        CommonSettingCmdResult cmdResponseResult = IOTParseManager.getInstance().parseSettingCmd(queryCmdResult.getResponseContent());
        if (cmdResponseResult.isSucceed()) {
            mTvContent.setText("初始化完成");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                    DataCenterHomeActivity.startActivity(mActivity, ProductType.M20, netM20HomeFragment.deviceInfo, AppContants.DataCenterConfigMethod.BASIC_CONFIG, true);
                }
            },1500);
        } else {
            mTvContent.setText(String.format("初始化失败! %s", cmdResponseResult.getReason()));
        }
    }

    /**
     * 查询指令响应结果出错了
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg){
        super.onQueryCmdResponseResultError(errMsg);
        enableTouch();
        mTvContent.setVisibility(View.VISIBLE);
        mTvContent.setText(String.format("指令响应出错! %s", errMsg));
    }

    /**
     * 查询指令响应结果超时
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultTimeOut(queryCmdResult);
        enableTouch();
    }

    @Override
    protected void showResponseTimeOutView(String tip) {
        super.showResponseTimeOutView("响应超时");
    }

}