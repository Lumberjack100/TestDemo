package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.TerminalSNEntity;
import com.shmedo.configlibrary.iot.cmd.entity.vms.VmsAisleNumberEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.VmsAisleNumber;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.vms.VmsAisleInfo;
import com.shmedo.configlibrary.iot.model.vms.VmsAisleTerminalInfo;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.VmsTerminalInfoAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateSheetDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.VmsViewModel;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/10/21 <br/>
 * 描述：     Vms 网关 4g 模式挂载的终端设备列表页面
 */
public class NetVmsTerminalListFragment extends BaseNetIotCommunicateSheetDialogFragment {
    @BindView(R.id.progress_overlay)
    View progressOverlay;

    @BindView(R.id.tv_progress_text)
    TextView mTvProgressText;

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private VmsTerminalInfoAdapter adapter;

    private List<VmsTerminalInfo> vmsTerminalInfoList = new ArrayList<>();
    private VmsTerminalInfo vmsTerminalInfo;

    private VmsAisleInfo vmsAisleInfo;

    private VmsViewModel vmsViewModel;

    public NetVmsTerminalListFragment(ProjectDeviceInfo projectDeviceInfo, VmsAisleInfo vmsAisleInfo) {
        this.projectDeviceInfo = projectDeviceInfo;
        this.vmsAisleInfo = vmsAisleInfo;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.vms_terminal_list_fragment;
    }

    /**
     * 设置弹窗高度，默认为屏幕高度的四分之三
     */
    @Override
    protected int getPeekHeight() {
        int peekHeight = getResources().getDisplayMetrics().heightPixels;
        //设置弹窗高度为屏幕高度的4/5
        return peekHeight - peekHeight / 5;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initAdapter();
        vmsViewModel = getApplicationScopeViewModel(VmsViewModel.class);
        adapter.setEmptyView(R.layout.empty_view);
        initData();
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mActivity, 15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        adapter = new VmsTerminalInfoAdapter(vmsTerminalInfoList);
        adapter.setAnimationEnable(true);
        adapter.setAnimationFirstOnly(false);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (isDoubleClick(view)) {
                    return;
                }
                vmsTerminalInfo = vmsTerminalInfoList.get(position);
                VmsTerminalHomeActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, vmsTerminalInfo);
            }
        });
        adapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                vmsTerminalInfo = vmsTerminalInfoList.get(position);
                showRemoveTerminalDialog();
                return true;
            }
        });
        mRecyclerView.setAdapter(adapter);
    }

    private void initData() {
        if (vmsAisleInfo == null) {
            return;
        }

        VmsAisleNumber vmsAisleNumber;
        String title;
        if (vmsAisleInfo.getChannel() == 1) {
            title = "数据通道1-设备(";
            vmsAisleNumber = VmsAisleNumber.value(1);
        } else {
            title = "数据通道2-设备(";
            vmsAisleNumber = VmsAisleNumber.value(2);
        }
        title += vmsAisleInfo.getTerminalnum() + ")";
        mTvTitle.setText(title);

        getGatewayStatus(vmsAisleNumber);
    }

    /**
     * 获取网关不同通道下，挂载终端的运行情况
     *
     * @param vmsAisleNumber
     */
    private void getGatewayStatus(VmsAisleNumber vmsAisleNumber) {
        VmsAisleNumberEntity vmsAisleNumberEntity = new VmsAisleNumberEntity(vmsAisleNumber.toInt());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_GET_GATEWAY_STATUS, vmsAisleNumberEntity);
        showProgressBar();
        mTvProgressText.setText("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 移除网关挂载的终端
     */
    private void removeTerminal() {
        TerminalSNEntity entity = new TerminalSNEntity(vmsTerminalInfo.getSn());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_DELETE_TERMINAL, entity);
        showProgressBar();
        mTvProgressText.setText("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    @OnClick({R.id.iv_close})
    public void onClick(View view) {
        if (view.getId() == R.id.iv_close) {
            dismiss();
        }
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            hideProgressBar();
            showDispatchFailedDialog(cmdStr);
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponseRunnable(2000);
        }
    }

    /**
     * 指令下发失败弹框
     */
    private void showDispatchFailedDialog(String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case VMS_MD_GET_GATEWAY_STATUS:
            case VMS_MD_DELETE_TERMINAL:
                ToastUtils.show("下发指令失败");
                break;
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
        hideProgressBar();
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
        hideProgressBar();
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
        hideProgressBar();
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case VMS_MD_DELETE_TERMINAL: {//删除终端设备
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "删除终端出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                doAfterSetting();
            }
            break;

            case VMS_MD_GET_GATEWAY_STATUS: {//获取网关的状态
                IOTCommandResult<VmsAisleTerminalInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询网关基本信息出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                VmsAisleTerminalInfo vmsAisleTerminalInfo = commandResult.getResult();
                modifyAisleTerminalInfo(vmsAisleTerminalInfo);

                if (vmsAisleTerminalInfo.getTerminal().size() == 0) {
                    adapter.setEmptyView(R.layout.empty_view);
                    return;
                }
                vmsTerminalInfoList.clear();
                vmsTerminalInfoList.addAll(vmsAisleTerminalInfo.getTerminal());
                adapter.notifyDataSetChanged();
            }
            break;
        }
    }

    /**
     * 修改某个通道下接入的的终端信息，设置终端的网络号、信道号与所属通道一致
     *
     * @param vmsAisleTerminalInfo
     */
    private void modifyAisleTerminalInfo(VmsAisleTerminalInfo vmsAisleTerminalInfo) {
        if (vmsAisleTerminalInfo == null)
            return;

        if (vmsAisleTerminalInfo.getTerminal() == null)
            return;

        for (VmsTerminalInfo vmsTerminalInfo : vmsAisleTerminalInfo.getTerminal()) {
            vmsTerminalInfo.setNetid(vmsAisleTerminalInfo.getNetid());
            vmsTerminalInfo.setChl(vmsAisleTerminalInfo.getChl());
        }
    }

    private void doAfterSetting() {
        ToastUtils.show("删除成功");
        vmsTerminalInfoList.remove(vmsTerminalInfo);
        adapter.notifyDataSetChanged();
        vmsViewModel.setVmsRefreshTerminal(true);

        String title;
        if (vmsAisleInfo.getChannel() == 0) {
            title = "通道01-设备(";
        } else if (vmsAisleInfo.getChannel() == 1) {
            title = "通道02-设备(";
        } else {
            title = "通道03-设备(";
        }
        title += vmsTerminalInfoList.size() + ")";
        mTvTitle.setText(title);
    }

    private CharSequence getWarnMessage() {
        SpannableStringBuilder builder = new SpannableStringBuilder(vmsTerminalInfo.getSn());
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(getContext().getResources().getColor(R.color.blue_52B4F8));
        builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.insert(0, "确认移除 ");
        builder.append(" 设备?");

        return builder;
    }

    /**
     * 删除终端警告
     */
    private void showRemoveTerminalDialog() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(requireContext())
                .title("温馨提示：")
                .content(getWarnMessage())
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        removeTerminal();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    private void showProgressBar() {
        progressOverlay.setVisibility(View.VISIBLE);
        //TODO #gh# android:clickable="true" 和 android:focusable="true" 已经实现了禁止触摸遮罩层下面的 View,
        // 防止点击未遮住的ToolBar，添加下面代码禁用窗体触摸
        mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideProgressBar() {
        progressOverlay.setVisibility(View.GONE);
        //get user interaction back
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
