package com.shmedo.mcloudapp.deviceconfig.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.core.CenterPopupView;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.util.GsonFactory;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/30 <br/>
 * 描述：     TODO
 */
public class DispatchCmdDialog extends CenterPopupView implements View.OnClickListener {
    private TextView tvTitle;
    private ImageView ivCmdResult;
    private ProgressBar progressBar;
    private TextView tvCmdResult;
    private TextView tvDeviceStateDesc;

    private String title;
    private List<String> msgIDList = new ArrayList<>();

    private Handler UIHandler;
    private MyRunnable mRunnable;
    private static int repeatNum = 0;//当查询指令结果5次时，判断响应超时
    private OnQueryCmdResultListener mListener;


    private class MyRunnable implements Runnable {
        @Override
        public void run() {
            if (repeatNum > 5) {
                stopRunnable();
                progressBar.setVisibility(View.GONE);
                ivCmdResult.setVisibility(View.VISIBLE);
                tvCmdResult.setText("等待响应超时");
                return;
            }

            Timber.d("当前时间");
            queryCmdResultByMsgID();
        }
    }

    private void startRunnable(long delayMillis) {
        if (mRunnable == null) {
            mRunnable = new MyRunnable();
        }
        repeatNum++;
        UIHandler.postDelayed(mRunnable, delayMillis);
    }

    private void stopRunnable() {
        UIHandler.removeCallbacks(mRunnable);
        mRunnable = null;
        repeatNum = 0;
    }


    public DispatchCmdDialog(@NonNull Context context, String title, List<String> msgIDList) {
        super(context);
        this.title = title;
        this.msgIDList.clear();
        this.msgIDList.addAll(msgIDList);
    }


    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_dispatch_cmd;
    }

    @Override
    protected void onCreate() {
        super.onCreate();

        UIHandler = new Handler(Looper.getMainLooper());
        initView();
        setListener();
        updateView();

        startRunnable(2000);
    }

    private void initView() {
        tvTitle = findViewById(R.id.tv_title);
        ivCmdResult = findViewById(R.id.iv_query_cmd_result);
        progressBar = findViewById(R.id.progressBar);
        tvCmdResult = findViewById(R.id.tv_query_cmd_result);
        tvDeviceStateDesc = findViewById(R.id.tv_device_state_desc);
    }

    private void updateView() {
        tvTitle.setText(title);
        progressBar.setVisibility(View.VISIBLE);
        ivCmdResult.setVisibility(View.GONE);
        tvCmdResult.setText("等待响应");
        tvDeviceStateDesc.setText("");
    }


    private void setListener() {
        findViewById(R.id.iv_close).setOnClickListener(this);
        findViewById(R.id.tv_confirm).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                stopRunnable();
                dismiss();
                break;

            case R.id.tv_confirm:
                stopRunnable();
                dismiss();
                break;

            default:
                break;
        }
    }

    public void setOnQueryCmdResultListener(OnQueryCmdResultListener listener) {
        mListener = listener;
    }

    public interface OnQueryCmdResultListener {
        void onSuccess();
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
                    public void Success(List<QueryCmdResult> data, String message) {
                        if (data == null || data.size() == 0) {
                            return;
                        }

                        QueryCmdResult queryCmdResult = data.get(0);
                        processCmdResult(queryCmdResult);
                    }

                    @Override
                    public void Failure(String message) {
                        Timber.w("请求失败--%s", message);
                        ToastUtils.show("下发指令失败");
                    }
                });
    }

    private void processCmdResult(QueryCmdResult queryCmdResult) {
        if (queryCmdResult == null)
            return;

        if (queryCmdResult.getCmdStatus() == 2) {//已下发得到响应
            progressBar.setVisibility(View.GONE);
            ivCmdResult.setVisibility(View.VISIBLE);
            tvCmdResult.setText("响应成功");
            tvDeviceStateDesc.setText(queryCmdResult.getResponseContent());

        } else {
            if (repeatNum >= 5) {//已经达到设定的10秒超时时间
                Timber.d("当前时间查询次数：%s", repeatNum);
                progressBar.setVisibility(View.GONE);
                ivCmdResult.setVisibility(View.VISIBLE);
                tvCmdResult.setText("等待响应超时");
                return;
            }
            //延迟2秒后再次查询响应结果
            startRunnable(2000);
        }
    }

}
