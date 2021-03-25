package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.DeviceInfo;
import com.shmedo.core.util.GsonFactory;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.util.ResponseHandler;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/31 <br/>
 * 描述：     网络下发指令后查询响应弹框基类
 */
public abstract class BaseDispatchCmdDialog extends DialogFragment {
    private Unbinder unbinder;
    protected AppCompatActivity mActivity;
    private View mRootView;
    /**
     * 当指令正在响应时展示的布局。
     */
    private View queryCmdResponseResultLoadingView = null;

    /**
     * 当指令响应超时展示的布局。
     */
    private View queryCmdResponseResultTimeOutView = null;

    protected String title;

    protected List<String> msgIDList = new ArrayList<>();

    protected Handler UIHandler;

    private QueryCmdResponseRunnable queryCmdResponseRunnable;

    private int queryNum = 0;//当查询指令结果10次时，判断响应超时

    private class QueryCmdResponseRunnable implements Runnable {
        @Override
        public void run() {
            //轮询指令响应结果接口达到10次，判断超时
            if (queryNum > 10) {
                onQueryCmdResponseResultTimeOut(null);
                return;
            }
            Timber.i("QueryCmdResponseRunnable run();queryNum=%s", queryNum);
            queryCmdResultByMsgID();
        }
    }

    protected void startQueryCmdResponseRunnable(long delayMillis) {
        if (queryCmdResponseRunnable == null) {
            queryCmdResponseRunnable = new QueryCmdResponseRunnable();
        }
        queryNum++;
        UIHandler.postDelayed(queryCmdResponseRunnable, delayMillis);
    }

    protected void stopQueryCmdResponseRunnable() {
        UIHandler.removeCallbacks(queryCmdResponseRunnable);
        queryCmdResponseRunnable = null;
        queryNum = 0;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (AppCompatActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setWindowStyle(Gravity.CENTER);

        if (mRootView == null) {
            mRootView = inflater.inflate(getLayoutId(), container, false);
        } else {
            ViewGroup viewGroup = (ViewGroup) mRootView.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(mRootView);
            }
        }
        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        UIHandler = new Handler(Looper.getMainLooper());
    }

    protected void setWindowStyle(int gravity) {
        Dialog mDialog = getDialog();
        Window window = mDialog.getWindow();
        //无标题  必须放在setContextView之前调用
        window.requestFeature(Window.FEATURE_NO_TITLE);
        //Sets whether this dialog is canceled when touched outside the window's bounds.
        mDialog.setCanceledOnTouchOutside(false);
        //Sets whether this dialog is cancelable with the BACK key.
        mDialog.setCancelable(false);
        window.setWindowAnimations(R.style.DialogFragmentAnimation);
        //window外可以点击,不拦截窗口外的事件
//        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        // 设置宽度为屏宽、靠近屏幕底部。
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = gravity;
        wlp.width = (int) (DeviceInfo.getScreenWidth() * 0.8f);
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
    }

    protected abstract int getLayoutId();

    /**
     * 当指令响应超时，通过此方法显示提示界面给用户。
     *
     * @param tip 界面中的提示信息
     */
    protected void showResponseTimeOutView(String tip) {
        if (queryCmdResponseResultTimeOutView != null) {
            queryCmdResponseResultTimeOutView.setVisibility(View.VISIBLE);
            return;
        }

        if (mRootView != null) {
            ViewStub viewStub = mRootView.findViewById(R.id.queryCmdResponseResultTimeOutView);
            if (viewStub != null) {
                queryCmdResponseResultTimeOutView = viewStub.inflate();
                TextView mTvResponseTimeOutDesc = queryCmdResponseResultTimeOutView.findViewById(R.id.tv_query_cmd_response_result_time_out_desc);
                mTvResponseTimeOutDesc.setText(tip);
            }
        }
    }

    private void hideResponseTimeOutView() {
        if (queryCmdResponseResultTimeOutView != null) {
            queryCmdResponseResultTimeOutView.setVisibility(View.GONE);
        }
    }

    protected void showResponseLoadingView() {
        hideResponseTimeOutView();
        if (queryCmdResponseResultLoadingView != null) {
            queryCmdResponseResultLoadingView.setVisibility(View.VISIBLE);
            return;
        }

        if (mRootView != null) {
            queryCmdResponseResultLoadingView = mRootView.findViewById(R.id.queryCmdResponseResultLoadingView);
            queryCmdResponseResultLoadingView.setVisibility(View.VISIBLE);
        }
    }

    private void hideResponseLoadingView() {
        if (queryCmdResponseResultLoadingView != null) {
            queryCmdResponseResultLoadingView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopQueryCmdResponseRunnable();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    /**
     * 开始轮询指令响应结果
     */
    protected void startQueryCmdResponse() {
        showResponseLoadingView();
        startQueryCmdResponseRunnable(0);
    }

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
            hideResponseLoadingView();
            onQueryCmdResponseResultSuccess(queryCmdResult);

        } else {
            //延迟2秒后再次查询响应结果
            startQueryCmdResponseRunnable(1000);
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
        stopQueryCmdResponseRunnable();
        hideResponseLoadingView();
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        //停止轮询指令响应结果接口
        stopQueryCmdResponseRunnable();
        hideResponseLoadingView();
        showResponseTimeOutView("响应超时");
    }
}
