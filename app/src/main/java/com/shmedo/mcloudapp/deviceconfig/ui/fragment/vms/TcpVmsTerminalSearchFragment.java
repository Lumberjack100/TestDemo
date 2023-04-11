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
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.DebouncingUtils;
import com.blankj.utilcode.util.StringUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.entity.TerminalSNEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.VmsTerminalInfoAdapter;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalExternalSensorHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalHomeActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
/**
 *
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setView();
        initAdapter();
    }

    private void setView() {
        mEtKeyWords.setHint("设备SN搜索");
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
                        com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(mEtKeyWords);
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
        int spacing = ConvertUtils.dp2px( 15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        adapter = new VmsTerminalInfoAdapter(vmsTerminalInfoList);
        adapter.setAnimationEnable(false);
        adapter.setAnimationFirstOnly(false);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if(!DebouncingUtils.isValid(view, 1000)) {
                    return;
                }
                if (!tcpViewModel.getConnectStatus()) {
                    ToastUtils.show(StringUtils.getString(R.string.tcp_config_disconnect_warn));
                    return;
                }
                vmsTerminalInfo = vmsTerminalInfoList.get(position);
                VmsTerminalHomeActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, vmsTerminalInfo);
            }
        });
        adapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                if (!tcpViewModel.getConnectStatus()) {
                    ToastUtils.show(StringUtils.getString(R.string.tcp_config_disconnect_warn));
                    return true;
                }
                vmsTerminalInfo = vmsTerminalInfoList.get(position);
                showRemoveTerminalDialog();
                return true;
            }
        });
        adapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                if(!DebouncingUtils.isValid(view, 1000)) {
                    return;
                }
                if (!tcpViewModel.getConnectStatus()) {
                    ToastUtils.show(StringUtils.getString(R.string.tcp_config_disconnect_warn));
                    return;
                }
                vmsTerminalInfo = vmsTerminalInfoList.get(position);
                VmsTerminalExternalSensorHomeActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, vmsTerminalInfo);
            }
        });
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setHasFixedSize(true);
    }

    @OnClick({R.id.iv_back, R.id.tv_search})
    public void onClick(View view) {
        if(!DebouncingUtils.isValid(view, 1000)) {
            return;
        }
        switch (view.getId()) {
            case R.id.iv_back:
                mActivity.finish();
                break;

            case R.id.tv_search:
                if (TextUtils.isEmpty(mEtKeyWords.getText().toString().trim())) {
                    ToastUtils.show("请输入搜索内容");
                    return;
                }
                // 当按了搜索之后关闭软键盘
                com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(mEtKeyWords);
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
        List<VmsTerminalInfo> allTerminals = vmsViewModel.getCacheVmsTerminalList();
        if (allTerminals == null) {
            adapter.setEmptyView(R.layout.empty_view);
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
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
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

    private void doAfterSetting() {
        ToastUtils.show("删除成功");
        vmsTerminalInfoList.remove(vmsTerminalInfo);
        if(vmsTerminalInfoList.size()==0){
            adapter.setEmptyView(R.layout.empty_view);
        }
        adapter.notifyDataSetChanged();
        //刷新 VMS 主页面
        vmsViewModel.setVmsRefreshTerminal(true);
    }
}