package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.Manifest;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallbackWithBeforeParam;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.entity.BroadcastEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.RealtimeSpeechDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/2 <br/>
 * 描述：     语音播报
 */
public class NetVoiceBroadcastFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.tv_broadcast_num)
    TextView mTvBroadcastNum;

    @BindView(R.id.broadcast_text)
    EditText mEtResultText;

    private StringBuffer buffer = new StringBuffer();

    private int broadcastNum;
    private int broadcastSize;
    private String broadcastContent;


    public static NetVoiceBroadcastFragment newInstance(DeviceInfo deviceInfo) {
        NetVoiceBroadcastFragment fragment = new NetVoiceBroadcastFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.voice_broadcast_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEtResultText.requestFocus();
    }

    @OnClick({R.id.ll_broadcast_num, R.id.btn_speech, R.id.btn_send})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_broadcast_num) {
            showBroadcastNumDialog();

        } else if (id == R.id.btn_speech) {
            requestPermissions();

        } else if (id == R.id.btn_send) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            sendBroadcast();
        }
    }

    private void sendBroadcast() {
        StringBuffer buffer = new StringBuffer();
        try {
            broadcastNum = Integer.parseInt(mTvBroadcastNum.getText().toString().trim());
            byte[] bytes = broadcastContent.getBytes(StandardCharsets.UTF_8);
            broadcastSize = bytes.length;
            for (byte b : bytes) {
                buffer.append(b);
            }
        } catch (Exception ex) {
            broadcastNum = 1;
        }
        BroadcastEntity entity = new BroadcastEntity();
        entity.setB_num(broadcastNum);
        entity.setB_size(broadcastSize);
        entity.setB_content(buffer.toString());

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_BROADCAST, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    private boolean checkValueIsValid() {
        broadcastContent = mEtResultText.getText().toString().trim();
        if (TextUtils.isEmpty(broadcastContent)) {
            ToastUtils.show("请输入播报内容!");
            mEtResultText.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * 选择播报遍数
     */
    private void showBroadcastNumDialog() {
        final String[] values = getResources().getStringArray(R.array.broadcast_num);
        int pos = Arrays.asList(values).indexOf(String.valueOf(mTvBroadcastNum.getText()));

        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", values,
                        null, pos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvBroadcastNum.setText(text);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    private void requestPermissions() {
        PermissionX.init(this)
                .permissions(Manifest.permission.RECORD_AUDIO)
//                .explainReasonBeforeRequest()
                .onExplainRequestReason(new ExplainReasonCallbackWithBeforeParam() {
                    @Override
                    public void onExplainReason(ExplainScope scope, List<String> deniedList, boolean beforeRequest) {
                        scope.showRequestReasonDialog(deniedList, "米易通需要以下权限继续", "允许", "拒绝");
                    }
                })
                .onForwardToSettings(new ForwardToSettingsCallback() {
                    @Override
                    public void onForwardToSettings(ForwardScope scope, List<String> deniedList) {
                        scope.showForwardToSettingsDialog(deniedList, "请前往设置页面授予权限", "去设置");
                    }
                })
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                        if (allGranted) {
                            buffer.setLength(0);
                            mEtResultText.setText(null);// 清空显示内容
                            showSpeechDialog();
                        } else {
                            ToastUtils.show("下列权限被拒绝：" + deniedList);
                        }
                    }
                });
    }

    private void showSpeechDialog() {
        buffer.setLength(0);
        RealtimeSpeechDialog speechDialog = RealtimeSpeechDialog.newInstance();
        speechDialog.setSpeechDialogClickListener(mListener);
        speechDialog.show(getChildFragmentManager(), "dialog");
    }

    private RealtimeSpeechDialog.SpeechDialogListener mListener = new RealtimeSpeechDialog.SpeechDialogListener() {
        @Override
        public void onResult(String result, boolean isLast) {
            buffer.append(result);
            mEtResultText.setText(buffer.toString());
            mEtResultText.setSelection(mEtResultText.length());
        }
    };

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            dismissWaitDialog();
            showDispatchFailedDialog(cmdStr);
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponse();
        }
    }

    /**
     * 指令下发失败弹框
     */
    private void showDispatchFailedDialog(String cmdStr) {
        ToastUtils.show("下发指令失败");
    }

    /**
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
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
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case MD_BROADCAST: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "语音播报出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("语音播报已发送!");
            }
            break;

            default:
                break;
        }
    }
}