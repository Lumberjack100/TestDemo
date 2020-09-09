package com.shmedo.mcloudapp.util;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.event.ForceToLoginEvent;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.network.ErrCode;

import org.greenrobot.eventbus.EventBus;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/8 <br/>
 * 描述：     对服务器的返回进行相应的逻辑处理。注意此类只处理公众的返回逻辑，涉及具体的业务逻辑，仍然交由接口调用处自行处理。
 */
public class ResponseHandler {
    private static final ResponseHandler ourInstance = new ResponseHandler();

    public static ResponseHandler getInstance() {
        return ourInstance;
    }

    private ResponseHandler() {
    }

    /**
     * 当网络请求正常响应的时候，根据状态码处理通用部分的逻辑。
     *
     * @param errCode
     * @return 如果已经将该响应处理掉了，返回true，否则返回false。
     */
    public boolean handleResponse(ErrCode errCode) {
        switch (errCode.getCode()) {
            case 8:
                Timber.w("handleResponse: errCode code is 8");
                ToastUtils.show(GlobalUtil.getString(R.string.server_internal_error));
                return true;

            case 10:
            case 11:
                Timber.w("handleResponse: errCode code is %s", errCode.getCode());
                ToastUtils.show(GlobalUtil.getString(R.string.login_status_expired));
                MCloudApp.logout();
                EventBus.getDefault().post(new ForceToLoginEvent());
                return true;

            default:
                return false;
        }
    }

    /**
     * 当网络请求没有正常响应的时候，根据异常类型进行相应的处理。
     *
     * @param ex
     */
    public void handleFailure(Exception ex) {
        if (ex instanceof ConnectException) {
            ToastUtils.show(GlobalUtil.getString(R.string.network_connect_error));
            return;
        } else if (ex instanceof SocketTimeoutException) {
            ToastUtils.show(GlobalUtil.getString(R.string.network_connect_timeout));
            return;
        } else if (ex instanceof NoRouteToHostException) {
            ToastUtils.show(GlobalUtil.getString(R.string.no_route_to_host));
            return;
        } else if (ex instanceof UnknownHostException) {
            ToastUtils.show(GlobalUtil.getString(R.string.no_route_to_host));
            return;
        }

        Timber.w(ex, "handleFailure exception ");
        ToastUtils.show(ex.getMessage());
    }
}
