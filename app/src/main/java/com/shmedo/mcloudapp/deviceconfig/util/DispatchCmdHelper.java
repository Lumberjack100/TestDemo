package com.shmedo.mcloudapp.deviceconfig.util;

import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.params.DispatchRawCmdParam;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.util.GsonFactory;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/1 <br/>
 * 描述：     TODO
 */
public class DispatchCmdHelper {
    private static final DispatchCmdHelper ourInstance = new DispatchCmdHelper();

    public static DispatchCmdHelper getInstance() {
        return ourInstance;
    }

    private DispatchCmdHelper() {
    }

    /**
     * 指令透传
     */
    public void processDispatchRawCmd(DispatchRawCmdParam dispatchRawCmdParam) {
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
                    public void Success(List<DispatchCmdItem> data, String message) {
                        if (data == null || data.size() == 0) {
                            EventBus.getDefault().post(null);
                            return;
                        }

                        EventBus.getDefault().post(data);
                    }

                    @Override
                    public void Failure(String message) {
                        EventBus.getDefault().post(null);
                    }
                });
    }
}
