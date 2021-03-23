package com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.GsonFactory;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.model.params.DispatchRawCmdParam;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.ResponseHandler;

import java.util.ArrayList;
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

    protected List<String> msgIDList = new ArrayList<>();

    private Handler uiHander = new Handler();

    private int queryNum = 0;//当查询指令结果5次时，判断响应超时

    private QueryCmdResponseRunnable queryCmdResponseRunnable;//常规任务

    private class QueryCmdResponseRunnable implements Runnable {
        @Override
        public void run() {
            //轮询指令响应结果接口达到5次，判断超时
            if (queryNum > 5) {
                onQueryCmdResponseResultTimeOut(null);
                return;
            }
            Timber.i("QueryCmdResponseRunnable run();queryNum=%s", queryNum);
            queryCmdResultByMsgID();
        }
    }

    protected void startQueryCmdResponseRunnable(long delayMillis) {
        this.startQueryCmdResponseRunnable(delayMillis, true);
    }

    protected void startQueryCmdResponseRunnable(long delayMillis, boolean isFirstCall) {
        if (!isFirstCall && queryCmdResponseRunnable == null) {
            return;
        }
        if (queryCmdResponseRunnable == null) {
            queryCmdResponseRunnable = new QueryCmdResponseRunnable();
        }
        queryNum++;
        uiHander.postDelayed(queryCmdResponseRunnable, delayMillis);
    }

    protected void stopQueryCmdResponseRunnable() {
        uiHander.removeCallbacksAndMessages(null);
        queryCmdResponseRunnable = null;
        queryNum = 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(PRO_DEVICE_INFO)) {
            projectDeviceInfo = getArguments().getParcelable(PRO_DEVICE_INFO);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        dismissProgressDialog();
        stopQueryCmdResponseRunnable();
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
            stopQueryCmdResponseRunnable();
            onQueryCmdResponseResultSuccess(queryCmdResult);
        } else {
            //延迟2秒后再次查询响应结果
            startQueryCmdResponseRunnable(2000, false);
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
        stopQueryCmdResponseRunnable();
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        dismissProgressDialog();
        //停止轮询指令响应结果接口
        stopQueryCmdResponseRunnable();
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
