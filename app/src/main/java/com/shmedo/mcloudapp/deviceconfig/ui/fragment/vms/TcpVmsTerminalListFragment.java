package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.hjq.toast.ToastUtils;
import com.littlegreens.netty.client.listener.MessageStateListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.entity.TerminalSNEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.TerminalBean;
import com.shmedo.configlibrary.iot.model.VmsAisleTerminalInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.VmsTerminalInfoAdapter;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseBottomSheetDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.TcpShareViewModel;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.VmsViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class TcpVmsTerminalListFragment extends BaseBottomSheetDialogFragment {
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private VmsTerminalInfoAdapter adapter;

    private List<TerminalBean> terminalBeanList = new ArrayList<>();

    private VmsAisleTerminalInfo vmsAisleTerminalInfo;

    private TerminalBean terminalBean;

    private TcpShareViewModel tcpShareViewModel;
    private VmsViewModel vmsViewModel;


    public TcpVmsTerminalListFragment(VmsAisleTerminalInfo vmsAisleTerminalInfo) {
        this.vmsAisleTerminalInfo = vmsAisleTerminalInfo;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.tcp_vms_terminal_list_fragment;
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
        initView();
        vmsViewModel = getApplicationScopeViewModel(VmsViewModel.class);
        tcpShareViewModel = getApplicationScopeViewModel(TcpShareViewModel.class);
        tcpShareViewModel.getReceivedMessage().observeInFragment(this, new Observer<String>() {
            @Override
            public void onChanged(String msg) {
                parseResponseMessage(msg);
            }
        });
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mActivity, 15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        adapter = new VmsTerminalInfoAdapter(terminalBeanList);
        adapter.setAnimationEnable(true);
        adapter.setAnimationFirstOnly(false);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                terminalBean = terminalBeanList.get(position);
                VmsTerminalHomeActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, terminalBean);
            }
        });
        adapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                terminalBean = terminalBeanList.get(position);
                showRemoveTerminalDialog();
                return true;
            }
        });
        mRecyclerView.setAdapter(adapter);
    }

    private void initView() {
        if (vmsAisleTerminalInfo == null)
            return;

        String title;
        if (vmsAisleTerminalInfo.getChannel() == 0) {
            title = "通道01-设备(";
        } else if (vmsAisleTerminalInfo.getChannel() == 1) {
            title = "通道02-设备(";
        } else {
            title = "通道03-设备(";
        }
        title += vmsAisleTerminalInfo.getTerminal().size() + ")";
        mTvTitle.setText(title);

        if (vmsAisleTerminalInfo.getTerminal().size() == 0) {
            adapter.setEmptyView(R.layout.empty_view);
            return;
        }
        terminalBeanList.clear();
        terminalBeanList.addAll(vmsAisleTerminalInfo.getTerminal());
//        terminalBeanList.addAll(vmsAisleTerminalInfo.getTerminal());
        adapter.notifyDataSetChanged();
    }

    @OnClick({R.id.iv_close})
    public void onClick(View view) {
        if (view.getId() == R.id.iv_close) {
            dismiss();
        }
    }

    /**
     * 解析设备的参数指令
     */
    private void parseResponseMessage(String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case MD_DELETE_TERMINAL: {//获取网关的基本信息
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = "删除终端出错!";
                    Timber.e("%s%s", errMsg, cmdResult.getReason());
                    ToastUtils.show(errMsg);
                    return;
                }
                doAfterSetting();
            }
            break;
        }
    }

    private void doAfterSetting() {
        ToastUtils.show("删除成功");
        terminalBeanList.remove(terminalBean);
        adapter.notifyDataSetChanged();
        vmsViewModel.getVmsRefreshTerminal().postValue(true);
    }

    private CharSequence getWarnMessage() {
        SpannableStringBuilder builder = new SpannableStringBuilder(terminalBean.getSn());
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
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
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

    /**
     * 移除网关挂载的终端
     */
    private void removeTerminal() {
        TerminalSNEntity entity = new TerminalSNEntity(terminalBean.getSn());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_DELETE_TERMINAL, entity);
        sendCommand(command);
    }

    private void sendCommand(String cmdStr) {
        String apiKey = "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9";
        if (!TextUtils.isEmpty(vmsViewModel.getDeviceApiKey().getValue())) {
            apiKey = vmsViewModel.getDeviceApiKey().getValue();
        }
        cmdStr += "&apikey=" + apiKey
                + "&msgid=" + UUID.randomUUID().toString();

        Timber.d("发送指令：%s", cmdStr);
        tcpShareViewModel.sendMsgToServer(cmdStr, new MessageStateListener() {
            @Override
            public void isSendSuccss(boolean isSuccess) {
                if (isSuccess) {
//                    Timber.d("发送指令成功");
                } else {
                    Timber.e("发送指令失败");
                }
            }
        });
    }
}