package com.shmedo.mcloudapp.ddshare;

import android.app.Activity;
import android.os.Bundle;

import com.android.dingtalk.share.ddsharemodule.DDShareApiFactory;
import com.android.dingtalk.share.ddsharemodule.IDDAPIEventHandler;
import com.android.dingtalk.share.ddsharemodule.IDDShareApi;
import com.android.dingtalk.share.ddsharemodule.ShareConstant;
import com.android.dingtalk.share.ddsharemodule.message.BaseReq;
import com.android.dingtalk.share.ddsharemodule.message.BaseResp;
import com.android.dingtalk.share.ddsharemodule.message.SendAuth;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.BuildConfig;

import timber.log.Timber;

/**
 * Created by hanhanliu on 15/12/9.
 */
public class DDShareActivity extends Activity implements IDDAPIEventHandler {

    private IDDShareApi mIDDShareApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //activity的export为true，try起来，防止第三方拒绝服务攻击
            mIDDShareApi = DDShareApiFactory.createDDShareApi(this, BuildConfig.Dingding_APP_ID, false);
            mIDDShareApi.handleIntent(getIntent(), this);
        } catch (Exception e) {
            e.printStackTrace();
            Timber.d("e===========>%s", e.toString());
        }
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Timber.d("onReq=============>");
    }

    @Override
    public void onResp(BaseResp baseResp) {
        int errCode = baseResp.mErrCode;
        Timber.d("errorCode==========>%s", errCode);
        String errMsg = baseResp.mErrStr;
        Timber.d("errMsg==========>%s", errMsg);

        if (baseResp.getType() == ShareConstant.COMMAND_SENDAUTH_V2 && (baseResp instanceof SendAuth.Resp)) {
            SendAuth.Resp authResp = (SendAuth.Resp) baseResp;
            switch (errCode) {
                case BaseResp.ErrCode.ERR_OK:
                    ToastUtils.show("授权成功，授权码为:" + authResp.code);
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL:
                    ToastUtils.show("授权取消");
                    break;
                default:
                    ToastUtils.show("授权异常" + baseResp.mErrStr);
                    break;
            }
        } else {
            switch (errCode) {
                case BaseResp.ErrCode.ERR_OK:
                    ToastUtils.show("分享成功");
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL:
                    ToastUtils.show("分享取消");
                    break;
                default:
                    ToastUtils.show("分享失败" + baseResp.mErrStr);
                    break;
            }
        }

        finish();
    }
}
