package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.helper.DispatchCmdHelper;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.params.DispatchCmdParam;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.CommonCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.DispatchCmdFailedDialog;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.OnClick;

/**
 * 网络模式高级设置
 */
public class NetAdvancedSettingFragment extends BaseFragment {
    private static final String DEVICE_INFO = "device_info";

    private ProjectDeviceInfo projectDeviceInfo;

    private List<String> msgIDList = new ArrayList<>();


    public static NetAdvancedSettingFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetAdvancedSettingFragment fragment = new NetAdvancedSettingFragment();
        Bundle args = new Bundle();
        args.putParcelable(DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectDeviceInfo = getArguments().getParcelable(DEVICE_INFO);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(List<DispatchCmdItem> dispatchCmdItemList) {
        dismissProgressDialog();
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            showDispatchFailedDialog();
            return;
        }

        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        showDispatchSuccessDialog();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_net_advanced_setting;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @OnClick({R.id.resetLayout, R.id.workModeLayout, R.id.productRegisterLayout, R.id.modifyAuthCodeLayout, R.id.syncInstallLocationLayout})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.resetLayout://切换连接方式
                ToastUtils.show("正在研发中,敬请期待...");
//                showResetWarnDialog();
                break;

            case R.id.workModeLayout:
                ToastUtils.show("正在研发中,敬请期待...");
                break;

            case R.id.productRegisterLayout:
                ToastUtils.show("正在研发中,敬请期待...");
                break;

            case R.id.modifyAuthCodeLayout:
                ToastUtils.show("正在研发中,敬请期待...");
                break;

            case R.id.syncInstallLocationLayout:
                ToastUtils.show("正在研发中,敬请期待...");
                break;
        }
    }

    private void showDispatchFailedDialog() {
        BaseDispatchCmdDialog newFragment = new DispatchCmdFailedDialog("恢复出厂设置");
        newFragment.show(getChildFragmentManager(), "dialog");
    }

    private void showDispatchSuccessDialog() {
        BaseDispatchCmdDialog newFragment = new CommonCmdDialog("恢复出厂设置", "设备开始恢复出厂设置...", "此过程耗时较长,请耐心等待", msgIDList);
        newFragment.show(getChildFragmentManager(), "dialog");
    }

    private void showResetWarnDialog() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("提示").content("确定恢复出厂设置吗？")
                .negativeText("取消")
                .positiveText("确定")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        processDispatchCommonCmd();
                    }
                });

        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    private void processDispatchCommonCmd() {
        DispatchCmdParam dispatchCmdParam = new DispatchCmdParam();
        dispatchCmdParam.setCmdID(107);
        dispatchCmdParam.setCompanyID(MCloudApp.getCompanyID());
        dispatchCmdParam.setDeviceIDList(Arrays.asList(projectDeviceInfo.getId()));

        showProgressDialog("指令下发中...");
        DispatchCmdHelper.getInstance().processDispatchCmd(dispatchCmdParam);
    }
}
