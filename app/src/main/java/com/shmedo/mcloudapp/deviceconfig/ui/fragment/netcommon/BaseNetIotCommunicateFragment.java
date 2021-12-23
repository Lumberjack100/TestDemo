package com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon;

import static autodispose2.AutoDispose.autoDisposable;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.hjq.toast.ToastUtils;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.OnBackPressedListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommand;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.callback.WeakHandler;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.model.params.DispatchRawCmdParam;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.AdmeViewModel;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.ConfigPageViewModel;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrorInfo;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.RequestHeader;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.util.ResponseHandler;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/20/21 <br/>
 * 描述：     TODO #gh#
 */
public abstract class BaseNetIotCommunicateFragment extends BaseFragment {
    protected static final String PRO_DEVICE_INFO = "com.shmedo.mcloudapp.PRO_DEVICE_INFO";
    protected static final int DELAY_10000_MILLIS = 10000;
    protected static final int DELAY_15000_MILLIS = 15000;
    protected static final int DELAY_20000_MILLIS = 20000;
    protected static final int DELAY_60000_MILLIS = 60000;//超时时间

    public DeviceInfo deviceInfo;

    protected ConfigPageViewModel configPageViewModel;

    protected AdmeViewModel admeViewModel;

    protected List<String> msgIDList = new ArrayList<>();

    private int queryNum = 0;//当查询指令结果10次时，判断响应超时

    private final CommandHandler mCommandHandler = new CommandHandler(this);

    private Disposable cmdResultDisposable;

    private static final class CommandHandler extends WeakHandler<BaseNetIotCommunicateFragment> {
        private CommandHandler(BaseNetIotCommunicateFragment fragment) {
            super(fragment);
        }

        @Override
        protected void handleMessage(Message msg, BaseNetIotCommunicateFragment fragment) {
            switch (msg.what) {
                case AppContants.MsgWhat.MSG_DEFAULT:
                    try {
                        //轮询指令响应结果接口达到10次，判断超时
                        if (fragment.queryNum > 30) {
                            fragment.onQueryCmdResponseResultTimeOut(null);
                            return;
                        }
                        Timber.d("handleMessage();queryNum=%s", fragment.queryNum);
                        fragment.queryCmdResultByMsgID();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
            }
        }
    }

    protected void startQueryCmdResponse() {
        queryNum++;
        mCommandHandler.sendEmptyMessageDelayed(AppContants.MsgWhat.MSG_DEFAULT, 0);
    }

    private void startQueryCmdResponseDelayed(long delayMillis) {
        queryNum++;
        mCommandHandler.sendEmptyMessageDelayed(AppContants.MsgWhat.MSG_DEFAULT, delayMillis);
    }

    private void stopQueryCmdResponse() {
        queryNum = 0;
        mCommandHandler.removeMessages(AppContants.MsgWhat.MSG_DEFAULT);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopQueryCmdResponse();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(PRO_DEVICE_INFO)) {
            deviceInfo = getArguments().getParcelable(PRO_DEVICE_INFO);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configPageViewModel = getActivityScopeViewModel(ConfigPageViewModel.class);
        configPageViewModel.configPageEditableChanged.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isEditable) {
                onEditableChanged(isEditable);
            }
        });
        admeViewModel = getApplicationScopeViewModel(AdmeViewModel.class);
    }

    protected void onEditableChanged(boolean isEditable) {
    }

    /**
     * 保存配置信息
     */
    protected void saveConfigInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getId()));
    }

    /**
     * 调用指令透传接口
     *
     * @param content
     */
    protected void doCommonDispatchRawCmd(String content, List<Integer> deviceIDList) {
        try {
            DispatchRawCmdParam rawCmdParam = new DispatchRawCmdParam();
            rawCmdParam.setContent(content);
            rawCmdParam.setCompanyID(MCloudApp.getCompanyID());
            rawCmdParam.setDeviceIDList(deviceIDList);

            processDispatchRawCmd(rawCmdParam);

            String command_type = content.contains("&") ? content.substring(content.indexOf(IOTCommand.COMMAND_HEADER) + 1, content.indexOf("&")) : content.substring(content.indexOf(IOTCommand.COMMAND_HEADER) + 1);
            Map<String, Object> valueMap = new HashMap<String, Object>();
            valueMap.put("login_user", SPStaticUtils.getString(AppContants.User.UID, ""));
            valueMap.put("device_sn", TextUtils.isEmpty(deviceInfo.getDeviceToken()) ? "" : deviceInfo.getDeviceToken());
            valueMap.put("command_type", command_type);
            valueMap.put("command_content", content);
            MobclickAgent.onEventObject(MCloudApp.getContext(), "Dispatch_Command", valueMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 指令透传
     */
    private void processDispatchRawCmd(DispatchRawCmdParam dispatchRawCmdParam) {
        if (dispatchRawCmdParam == null) {
            throw new IllegalArgumentException("dispatchRawCmdParam 为null");
        }
        String json = GsonUtils.toJson(dispatchRawCmdParam);
        RequestBody body = RequestBody.create(RequestHeader.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .batchDispatchRawCmd(MCloudApp.getAccessToken(), body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe(new BaseObserver<List<DispatchCmdItem>>() {
                    @Override
                    protected void onResponse(List<DispatchCmdItem> data, ErrorInfo errorInfo) {
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                if (data == null || data.size() == 0) {
                                    onDispatchCmdResult(null, dispatchRawCmdParam.getContent());
                                    return;
                                }
                                onDispatchCmdResult(data, dispatchRawCmdParam.getContent());
                            } else {
                                onDispatchCmdResult(null, dispatchRawCmdParam.getContent());
                                if (!TextUtils.isEmpty(errorInfo.getMsg())) {
                                    ToastUtils.show(errorInfo.getMsg());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        onDispatchCmdResult(null, dispatchRawCmdParam.getContent());
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }

    /**
     * 调用指令下发/透传接口结果返回
     */
    protected abstract void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItems, String cmdStr);

    /**
     * 查询设备对下发/透传的指令响应结果
     */
    private void queryCmdResultByMsgID() {
        String json = GsonUtils.toJson(msgIDList);
        RequestBody body = RequestBody.create(RequestHeader.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .queryCmdResultByMsgID(MCloudApp.getAccessToken(), body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe(new BaseObserver<List<QueryCmdResult>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        cmdResultDisposable = d;
                    }

                    @Override
                    protected void onResponse(List<QueryCmdResult> data, ErrorInfo errorInfo) {
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                if (data == null || data.size() == 0) {
                                    onQueryCmdResponseResultError("");
                                    return;
                                }
                                QueryCmdResult queryCmdResult = data.get(0);
                                processCmdResult(queryCmdResult);
                            } else {
                                if (!TextUtils.isEmpty(errorInfo.getMsg())) {
                                    ToastUtils.show(errorInfo.getMsg());
                                    onQueryCmdResponseResultError(errorInfo.getMsg());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                        onQueryCmdResponseResultError(e.getMessage());
                    }
                });
    }

    private void processCmdResult(QueryCmdResult queryCmdResult) {
        if (queryCmdResult.getCmdStatus() == 2) {//已下发得到响应
            stopQueryCmdResponse();
            onQueryCmdResponseResultSuccess(queryCmdResult);
            try {
                IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
                String content = queryCmdResult.getResponseContent();
                Map<String, Object> valueMap = new HashMap<String, Object>();
                valueMap.put("login_user", SPStaticUtils.getString(AppContants.User.UID, ""));
                valueMap.put("device_sn", TextUtils.isEmpty(deviceInfo.getDeviceToken()) ? "" : deviceInfo.getDeviceToken());
                valueMap.put("command_type", type.toString());
                valueMap.put("command_content", content);
                MobclickAgent.onEventObject(MCloudApp.getContext(), "Response_Command", valueMap);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            //已经调用 stopQueryCmdResponse() 或 stopAllProgress() 停止查询
            if (queryNum == 0)
                return;
            //延迟1秒后再次查询响应结果
            startQueryCmdResponseDelayed(500);
        }
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
        dismissWaitDialog();
    }

    /**
     * 查询指令响应结果出错了
     *
     * @param errMsg
     */
    protected void onQueryCmdResponseResultError(String errMsg) {
        dismissWaitDialog();
        //停止轮询指令响应结果接口
        stopQueryCmdResponse();
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        dismissWaitDialog();
        //停止轮询指令响应结果接口
        stopQueryCmdResponse();
    }

    protected void dismissWaitDialog() {
        WaitDialog.dismiss();
    }

    protected void showWaitDialog(String message) {
        WaitDialog.show(message)
                .setOnBackPressedListener(new OnBackPressedListener() {//返回按键监听
                    @Override
                    public boolean onBackPressed() {
                        cancelRequest();
                        WaitDialog.dismiss();
                        return false;
                    }
                });
    }

    // 取消请求
    private void cancelRequest() {
        if (cmdResultDisposable != null) {
            Timber.d("取消请求");
            stopQueryCmdResponse();
            cmdResultDisposable.dispose();
            cmdResultDisposable = null;
        }
    }

    protected void warnNotYetSettingBeforeLeavePage() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(requireContext())
                .title("温馨提示：")
                .content("您已经修改了参数，还未配置到设备，确定离开页面吗？")
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
                        mActivity.finish();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
}
