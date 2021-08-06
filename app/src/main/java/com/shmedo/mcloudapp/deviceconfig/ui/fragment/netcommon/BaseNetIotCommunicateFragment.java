package com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.GsonFactory;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.model.params.DispatchRawCmdParam;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.AdmeViewModel;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.ConfigPageViewModel;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.ResponseHandler;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/20/21 <br/>
 * 描述：     TODO #gh#
 */
public abstract class BaseNetIotCommunicateFragment extends BaseFragment {
    protected static final String PRO_DEVICE_INFO = "com.shmedo.mcloudapp.PRO_DEVICE_INFO";
    public static final int DELAY_MILLIS = 60000;//超时时间

    public ProjectDeviceInfo projectDeviceInfo;

    protected ConfigPageViewModel configPageViewModel;

    protected AdmeViewModel admeViewModel;

    protected List<String> msgIDList = new ArrayList<>();

    private int queryNum = 0;//当查询指令结果10次时，判断响应超时

    private Handler uiHander = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull @NotNull Message msg) {
            if (msg.what == AppContants.MsgWhat.MSG_DEFAULT) {
                //轮询指令响应结果接口达到10次，判断超时
                if (queryNum > 10) {
                    onQueryCmdResponseResultTimeOut(null);
                    return false;
                }
                Timber.i("handleMessage();queryNum=%s", queryNum);
                queryCmdResultByMsgID();
            }
            return false;
        }
    });

    protected void startQueryCmdResponse() {
        queryNum++;
        uiHander.sendEmptyMessageDelayed(AppContants.MsgWhat.MSG_DEFAULT, 0);
    }

    protected void startQueryCmdResponseDelayed(long delayMillis) {
        queryNum++;
        uiHander.sendEmptyMessageDelayed(AppContants.MsgWhat.MSG_DEFAULT, delayMillis);
    }

    protected void stopQueryCmdResponse() {
        queryNum = 0;
        uiHander.removeMessages(AppContants.MsgWhat.MSG_DEFAULT);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(PRO_DEVICE_INFO)) {
            projectDeviceInfo = getArguments().getParcelable(PRO_DEVICE_INFO);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

    @Override
    public void onStop() {
        super.onStop();
        dismissProgressDialog();
        stopQueryCmdResponse();
    }

    /**
     * 保存配置信息
     */
    protected void saveConfigInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 调用指令透传接口
     *
     * @param content
     */
    protected void doCommonDispatchRawCmd(String content, List<Integer> deviceIDList) {
        DispatchRawCmdParam rawCmdParam = new DispatchRawCmdParam();
        rawCmdParam.setContent(content);
        rawCmdParam.setCompanyID(MCloudApp.getCompanyID());
        rawCmdParam.setDeviceIDList(deviceIDList);

        processDispatchRawCmd(rawCmdParam);
    }

    /**
     * 指令透传
     */
    private void processDispatchRawCmd(DispatchRawCmdParam dispatchRawCmdParam) {
        if (dispatchRawCmdParam == null) {
            throw new IllegalArgumentException("dispatchRawCmdParam 为null");
        }
        String json = GsonFactory.getGson().toJson(dispatchRawCmdParam);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .DispatchRawCmd(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<DispatchCmdItem>>() {
                    @Override
                    protected void onResponse(List<DispatchCmdItem> data, ErrCode errCode) {
                        if (!ResponseHandler.getInstance().handleResponse(errCode)) {
                            if (errCode.getCode() == 0) {
                                if (data == null || data.size() == 0) {
                                    onDispatchCmdResult(null, dispatchRawCmdParam.getContent());
                                    return;
                                }
                                onDispatchCmdResult(data, dispatchRawCmdParam.getContent());
                            } else {
                                onDispatchCmdResult(null, dispatchRawCmdParam.getContent());
                                if (!TextUtils.isEmpty(errCode.getErrMessage())) {
                                    ToastUtils.show(errCode.getErrMessage());
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
        String json = GsonFactory.getGson().toJson(msgIDList);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .QueryCmdResultByMsgID(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<QueryCmdResult>>() {
                    @Override
                    protected void onResponse(List<QueryCmdResult> data, ErrCode errCode) {
                        if (!ResponseHandler.getInstance().handleResponse(errCode)) {
                            if (errCode.getCode() == 0) {
                                if (data == null || data.size() == 0) {
                                    onQueryCmdResponseResultError("");
                                    return;
                                }
                                QueryCmdResult queryCmdResult = data.get(0);
                                processCmdResult(queryCmdResult);
                            } else {
                                if (!TextUtils.isEmpty(errCode.getErrMessage())) {
                                    ToastUtils.show(errCode.getErrMessage());
                                    onQueryCmdResponseResultError(errCode.getErrMessage());
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
        } else {
            //延迟1秒后再次查询响应结果
            startQueryCmdResponseDelayed(1000);
        }
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
        dismissProgressDialog();
    }

    /**
     * 查询指令响应结果出错了
     *
     * @param errMsg
     */
    protected void onQueryCmdResponseResultError(String errMsg) {
        dismissProgressDialog();
        //停止轮询指令响应结果接口
        stopQueryCmdResponse();
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        dismissProgressDialog();
        //停止轮询指令响应结果接口
        stopQueryCmdResponse();
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
