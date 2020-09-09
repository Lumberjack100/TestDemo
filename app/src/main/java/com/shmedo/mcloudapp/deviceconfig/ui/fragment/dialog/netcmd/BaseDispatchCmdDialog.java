package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd;

import android.app.Dialog;
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
import androidx.fragment.app.DialogFragment;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.DeviceInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.util.GsonFactory;
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

    private View mRootView;

    /**
     * 当指令响应超时展示的布局。
     */
    private View responseFailedView = null;

    /**
     * 当指令正在响应时展示的布局。
     */
    private View responseLoadingView = null;

    protected String title;

    protected List<String> msgIDList = new ArrayList<>();

    private Handler UIHandler;

    private MyRunnable mRunnable;

    private static int repeatNum = 0;//当查询指令结果5次时，判断响应超时


    private class MyRunnable implements Runnable {
        @Override
        public void run() {
            if (repeatNum > 5) {
                stopRunnable();
                hideResponseLoadingView();
                showResponseFailedView("响应超时");
                return;
            }

            Timber.d("当前时间");
            queryCmdResultByMsgID();
        }
    }

    protected void startRunnable(long delayMillis) {
        if (mRunnable == null) {
            mRunnable = new MyRunnable();
        }
        repeatNum++;
        UIHandler.postDelayed(mRunnable, delayMillis);
    }

    protected void stopRunnable() {
        UIHandler.removeCallbacks(mRunnable);
        mRunnable = null;
        repeatNum = 0;
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
    protected void showResponseFailedView(String tip) {
        if (responseFailedView != null) {
            responseFailedView.setVisibility(View.VISIBLE);
            return;
        }

        if (mRootView != null) {
            ViewStub viewStub = mRootView.findViewById(R.id.cmdResponseFailedView);
            if (viewStub != null) {
                responseFailedView = viewStub.inflate();
                TextView noContentText = responseFailedView.findViewById(R.id.tv_response_failed_desc);
                noContentText.setText(tip);
            }
        }
    }

    /**
     * 将load error view进行隐藏。
     */
    protected void hideResponseFailedView() {
        if (responseFailedView != null) {
            responseFailedView.setVisibility(View.GONE);
        }
    }


    protected void showResponseLoadingView() {
        hideResponseFailedView();
        if (responseLoadingView != null) {
            responseLoadingView.setVisibility(View.VISIBLE);
            return;
        }

        if (mRootView != null) {
            responseLoadingView = mRootView.findViewById(R.id.cmdResponseLoadingView);
            responseLoadingView.setVisibility(View.VISIBLE);
        }
    }


    protected void hideResponseLoadingView() {
        if (responseLoadingView != null) {
            responseLoadingView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
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
                                    return;
                                }

                                QueryCmdResult queryCmdResult = data.get(0);
                                processCmdResult(queryCmdResult);
                            }else{
                                if (!TextUtils.isEmpty(errCode.getErrMessage())) {
                                    ToastUtils.show(errCode.getErrMessage());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }

    private void processCmdResult(QueryCmdResult queryCmdResult) {
        if (queryCmdResult.getCmdStatus() == 2) {//已下发得到响应
            stopRunnable();
            hideResponseLoadingView();
            onCmdResponeSuccess(queryCmdResult);

        } else {
            if (repeatNum >= 5) {//已经达到设定的10秒超时时间
                Timber.d("当前时间已查询次数：%s", repeatNum);
                stopRunnable();
                hideResponseLoadingView();
                showResponseFailedView("响应超时");
                onCmdResponeFailed(queryCmdResult);
                return;
            }
            //延迟2秒后再次查询响应结果
            startRunnable(2000);
        }
    }

    protected void onCmdResponeSuccess(QueryCmdResult queryCmdResult) {

    }

    protected void onCmdResponeFailed(QueryCmdResult queryCmdResult) {

    }

}
