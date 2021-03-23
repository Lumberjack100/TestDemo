package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import com.shmedo.configlibrary.iot.model.vms.VmsAisleTerminalInfo;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.VmsTerminalInfoAdapter;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalHomeActivity;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
/**
 * @deprecated
 */
public class TcpVmsTerminalSearchFragment extends BaseVmsTcpCommunicateFragment implements TextWatcher {
    @BindView(R.id.et_keywords)
    ClearEditText mEtKeyWords;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private VmsTerminalInfoAdapter adapter;

    private List<VmsTerminalInfo> vmsTerminalInfoList = new ArrayList<>();

    private String keyWords;// 要输入的搜索关键字

    private VmsTerminalInfo vmsTerminalInfo;

    public static TcpVmsTerminalSearchFragment newInstance() {
        return new TcpVmsTerminalSearchFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vms_terminal_search_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        initAdapter();
        //获取网关不同通道下挂载终端的状态
        getGatewayStatus(VmsAisleNumber.NUMBER_TWO);
    }

    private void setView() {
        mEtKeyWords.setHint("项目名称搜索");
        mEtKeyWords.requestFocus();
        mEtKeyWords.addTextChangedListener(this);
        mEtKeyWords.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (TextUtils.isEmpty(textView.getText())) {
                        ToastUtils.show("请输入搜索内容");
                    } else {
                        // 当按了搜索之后关闭软键盘
                        KeyBordUtils.hideSoftKeyboard(mEtKeyWords);
                        doSearchQuery();
                    }
                    return true;
                }
                return false;
            }
        });
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
                if (!tcpViewModel.getConnectStatus()) {
                    ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
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

    /**
     * 获取网关不同通道下，挂载终端的运行情况
     *
     * @param vmsAisleNumber
     */
    private void getGatewayStatus(VmsAisleNumber vmsAisleNumber) {
        VmsAisleNumberEntity vmsAisleNumberEntity = new VmsAisleNumberEntity(vmsAisleNumber.toInt());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_GET_TERMINAL_STATUS, vmsAisleNumberEntity);
        sendCommand(command);
    }

    @OnClick({R.id.iv_back, R.id.tv_search})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        switch (v.getId()) {
            case R.id.iv_back:
                mActivity.finish();
                break;

            case R.id.tv_search:
                if (TextUtils.isEmpty(mEtKeyWords.getText().toString().trim())) {
                    ToastUtils.show("请输入搜索内容");
                    return;
                }
                // 当按了搜索之后关闭软键盘
                KeyBordUtils.hideSoftKeyboard(mEtKeyWords);
                doSearchQuery();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s == null || TextUtils.isEmpty(s.toString())) {
            return;
        }

        keyWords = s.toString();
        if (!TextUtils.isEmpty(keyWords)) {
            doSearchQuery();
        }
    }

    /**
     * 开始进行搜索
     */
    private void doSearchQuery() {
        List<VmsTerminalInfo> allTerminals = vmsViewModel.getCacheVmsTerminalList().getValue();
        if (allTerminals == null) {
            adapter.setEmptyView(R.layout.empty_view);
            adapter.notifyDataSetChanged();
            return;
        }

        vmsTerminalInfoList.clear();
        for (VmsTerminalInfo vmsTerminalInfo : allTerminals) {
            if (vmsTerminalInfo.getSn().contains(keyWords)) {
                vmsTerminalInfoList.add(vmsTerminalInfo);
            }
        }
        if (vmsTerminalInfoList.size() == 0) {
            adapter.setEmptyView(R.layout.empty_view);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 删除终端警告
     */
    private void showRemoveTerminalDialog() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
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

    private CharSequence getWarnMessage() {
        SpannableStringBuilder builder = new SpannableStringBuilder(vmsTerminalInfo.getSn());
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_52B4F8));
        builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.insert(0, "确认移除 ");
        builder.append(" 设备?");

        return builder;
    }

    /**
     * 移除网关挂载的终端
     */
    private void removeTerminal() {
        TerminalSNEntity entity = new TerminalSNEntity(vmsTerminalInfo.getSn());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_DELETE_TERMINAL, entity);
        sendCommand(command);
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case VMS_MD_GET_TERMINAL_STATUS: {//查询网关通道下的挂载终端信息
                IOTCommandResult<VmsAisleTerminalInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询网关通道下的挂载终端信息出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                VmsAisleTerminalInfo vmsAisleTerminalInfo = commandResult.getResult();
                if (vmsAisleTerminalInfo == null) {
                    return;
                }
                modifyAisleTerminalInfo(vmsAisleTerminalInfo);
                if (vmsAisleTerminalInfo.getChannel() == 1) {
                    vmsViewModel.clearCacheTerminalList();
                    vmsViewModel.addCacheTerminalList(vmsAisleTerminalInfo.getTerminal());
                    //获取网关不同通道下挂载终端的状态
                    getGatewayStatus(VmsAisleNumber.NUMBER_THREE);

                } else if (vmsAisleTerminalInfo.getChannel() == 2) {
                    vmsViewModel.addCacheTerminalList(vmsAisleTerminalInfo.getTerminal());
                }
            }
            break;

            case VMS_MD_DELETE_TERMINAL: {//获取网关的基本信息
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
        if(vmsTerminalInfoList.size()==0){
            adapter.setEmptyView(R.layout.empty_view);
        }
        adapter.notifyDataSetChanged();
        vmsViewModel.setVmsRefreshTerminal(true);
    }
}