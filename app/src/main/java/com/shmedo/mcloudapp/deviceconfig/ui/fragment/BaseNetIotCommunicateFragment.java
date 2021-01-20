package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.GsonFactory;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.params.DispatchCmdParam;
import com.shmedo.mcloudapp.deviceconfig.model.params.DispatchRawCmdParam;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.DeviceNetModelViewModel;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.util.ResponseHandler;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/20/21 <br/>
 * 描述：     TODO
 */
public abstract class BaseNetIotCommunicateFragment extends BaseFragment {

    protected DeviceNetModelViewModel deviceNetModelViewModel;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        deviceNetModelViewModel = getActivityScopeViewModel(DeviceNetModelViewModel.class);
        deviceNetModelViewModel.getDispatchCmdItemList().observeInFragment(this, new Observer<List<DispatchCmdItem>>() {
            @Override
            public void onChanged(List<DispatchCmdItem> dispatchCmdItems) {
                dismissProgressDialog();
                //判断此页面是否处于前台
                if (!isActive) {
                    return;
                }

                onDispatchCmdItemList(dispatchCmdItems);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        dismissProgressDialog();
    }

    /**
     *
     */
    protected void onDispatchCmdItemList(List<DispatchCmdItem> dispatchCmdItems) {

    }

    /**
     * 指令下发
     */
    protected void processDispatchCmd(DispatchCmdParam dispatchCmdParam) {
        if (dispatchCmdParam == null) {
            throw new IllegalArgumentException("dispatchCmdParam 为null");
        }

        String json = GsonFactory.getGson().toJson(dispatchCmdParam);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .DispatchCmd(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<DispatchCmdItem>>() {
                    @Override
                    protected void onResponse(List<DispatchCmdItem> data, ErrCode errCode) {
                        if (!ResponseHandler.getInstance().handleResponse(errCode)) {
                            if (errCode.getCode() == 0) {
                                if (data == null || data.size() == 0) {
                                    deviceNetModelViewModel.setDispatchCmdItemList(null);
                                    return;
                                }
                                deviceNetModelViewModel.setDispatchCmdItemList(data);

                            } else {
                                deviceNetModelViewModel.setDispatchCmdItemList(null);
                                if (!TextUtils.isEmpty(errCode.getErrMessage())) {
                                    ToastUtils.show(errCode.getErrMessage());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        deviceNetModelViewModel.setDispatchCmdItemList(null);
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }

    /**
     * 指令透传
     */
    protected void processDispatchRawCmd(DispatchRawCmdParam dispatchRawCmdParam) {
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
                                    deviceNetModelViewModel.setDispatchCmdItemList(null);
                                    return;
                                }
                                deviceNetModelViewModel.setDispatchCmdItemList(data);

                            } else {
                                deviceNetModelViewModel.setDispatchCmdItemList(null);
                                if (!TextUtils.isEmpty(errCode.getErrMessage())) {
                                    ToastUtils.show(errCode.getErrMessage());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        deviceNetModelViewModel.setDispatchCmdItemList(null);
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }
}
