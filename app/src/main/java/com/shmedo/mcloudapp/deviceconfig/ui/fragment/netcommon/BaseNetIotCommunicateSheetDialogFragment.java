package com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon;

import static autodispose2.AutoDispose.autoDisposable;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.blankj.utilcode.util.GsonUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.model.params.DispatchRawCmdParam;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseBottomSheetDialogFragment;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrorInfo;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.RequestHeader;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.util.ResponseHandler;

import java.util.ArrayList;
import java.util.List;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/10/21 <br/>
 * 描述：     TODO
 */
@Deprecated
public abstract class BaseNetIotCommunicateSheetDialogFragment extends BaseBottomSheetDialogFragment {
    protected static final String PRO_DEVICE_INFO = "com.shmedo.mcloudapp.PRO_DEVICE_INFO";
    public DeviceInfo deviceInfo;

    protected List<String> msgIDList = new ArrayList<>();

    private Handler uiHander = new Handler();

    private int queryNum = 0;//当查询指令结果5次时，判断响应超时

    private QueryCmdResponseRunnable queryCmdResponseRunnable;//常规任务

    private class QueryCmdResponseRunnable implements Runnable {
        @Override
        public void run() {
            //轮询指令响应结果接口达到5次，判断超时
            if (queryNum > 10) {
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
            deviceInfo = getArguments().getParcelable(PRO_DEVICE_INFO);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
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
            stopQueryCmdResponseRunnable();
            onQueryCmdResponseResultSuccess(queryCmdResult);
        } else {
            //延迟2秒后再次查询响应结果
            startQueryCmdResponseRunnable(1000, false);
        }
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {

    }

    /**
     * 查询指令响应结果出错了
     *
     * @param errMsg
     */
    protected void onQueryCmdResponseResultError(String errMsg) {
        //停止轮询指令响应结果接口
        stopQueryCmdResponseRunnable();
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        //停止轮询指令响应结果接口
        stopQueryCmdResponseRunnable();
    }
}
