package com.shmedo.mcloudapp.deviceconfig.ui.activity.vms;

import android.content.Context;
import android.content.Intent;
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
import com.shmedo.configlibrary.iot.model.TerminalInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.VmsTerminalInfoAdapter;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.TcpShareViewModel;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.VmsViewModel;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/19 <br/>
 * 描述：     Vms网关挂载的终端设备搜索页面
 */
public class VmsTerminalSearchActivity extends BaseActivity implements TextWatcher {
    @BindView(R.id.et_keywords)
    ClearEditText mEtKeyWords;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private VmsTerminalInfoAdapter adapter;

    private List<TerminalInfo> terminalInfoList = new ArrayList<>();

    private String keyWords;// 要输入的搜索关键字

    private TerminalInfo terminalInfo;

    private TcpShareViewModel tcpShareViewModel;
    private VmsViewModel vmsViewModel;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, VmsTerminalSearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_vms_terminal_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initAdapter();

        vmsViewModel = getApplicationScopeViewModel(VmsViewModel.class);
        tcpShareViewModel = getApplicationScopeViewModel(TcpShareViewModel.class);
        tcpShareViewModel.getReceivedMessage().observeInActivity(this, new Observer<String>() {
            @Override
            public void onChanged(String msg) {
                parseResponseMessage(msg);
            }
        });
    }

    private void initView() {
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
        int spacing = DensityUtil.Dp2Px(this, 15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        adapter = new VmsTerminalInfoAdapter(terminalInfoList);
        adapter.setAnimationEnable(true);
        adapter.setAnimationFirstOnly(false);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (isDoubleClick(view)) {
                    return;
                }
                if (!tcpShareViewModel.getConnectStatus()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                terminalInfo = terminalInfoList.get(position);
                VmsTerminalHomeActivity.startActivity(VmsTerminalSearchActivity.this, AppContants.CommunicationWay.TCP_CONNECT, terminalInfo);
            }
        });
        adapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                terminalInfo = terminalInfoList.get(position);
                showRemoveTerminalDialog();
                return true;
            }
        });
        mRecyclerView.setAdapter(adapter);
    }

    @OnClick({R.id.iv_back, R.id.tv_search})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
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
        List<TerminalInfo> allTerminals = vmsViewModel.getVmsTerminalList().getValue();
        if (allTerminals == null) {
            adapter.setEmptyView(R.layout.empty_view);
            adapter.notifyDataSetChanged();
            return;
        }

        terminalInfoList.clear();
        for (TerminalInfo terminalInfo : allTerminals) {
            if (terminalInfo.getSn().contains(keyWords)) {
                terminalInfoList.add(terminalInfo);
            }
        }
        if (terminalInfoList.size() == 0) {
            adapter.setEmptyView(R.layout.empty_view);
        }
        adapter.notifyDataSetChanged();
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
        terminalInfoList.remove(terminalInfo);
        adapter.notifyDataSetChanged();
        vmsViewModel.getVmsRefreshTerminal().postValue(true);
    }

    private CharSequence getWarnMessage() {
        SpannableStringBuilder builder = new SpannableStringBuilder(terminalInfo.getSn());
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_52B4F8));
        builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.insert(0, "确认移除 ");
        builder.append(" 设备?");

        return builder;
    }

    /**
     * 删除终端警告
     */
    private void showRemoveTerminalDialog() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this)
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
        TerminalSNEntity entity = new TerminalSNEntity(terminalInfo.getSn());
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
                if (!isSuccess) {
                    Timber.e("发送指令失败");
                }
            }
        });
    }
}