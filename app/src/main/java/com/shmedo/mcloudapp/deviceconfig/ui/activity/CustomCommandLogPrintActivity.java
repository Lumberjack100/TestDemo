package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.LogFileUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.USRBleIotCustomCommandLogPrintFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BleDasCustomCommandLogPrintFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.BleM20CustomCommandLogPrintFragment;
import com.shmedo.mcloudapp.util.FileProviderUtils;

import java.io.File;

import butterknife.OnClick;
import gdut.bsx.share2.Share2;
import gdut.bsx.share2.ShareContentType;

/**
 * 指令日志调试页面
 */
public class CustomCommandLogPrintActivity extends BaseConfigFragmentContainerActivity {
    private int deviceType = AppContants.DeviceType.DAS;


    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, CustomCommandLogPrintActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay, int deviceType) {
        Intent intent = new Intent(context, CustomCommandLogPrintActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("指令下发");
        mIvAction.setVisibility(View.VISIBLE);
        mIvAction.setImageResource(R.drawable.icon_share_command_log);
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.DEVICE_TYPE)) {
            deviceType = intent.getIntExtra(AppContants.Extras.DEVICE_TYPE, AppContants.DeviceType.DAS);
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {

            switch (deviceType) {
                case AppContants.DeviceType.DAS:
                    fragment = new BleDasCustomCommandLogPrintFragment();
                    break;

                case AppContants.DeviceType.ADME:
                    fragment = USRBleIotCustomCommandLogPrintFragment.newInstance();
                    break;

                case AppContants.DeviceType.M20:
                    fragment = BleM20CustomCommandLogPrintFragment.newInstance();
                    break;
            }
        }
        return fragment;
    }

    @OnClick({R.id.iv_action})
    public void onClick(View v) {
        if (v.getId() == R.id.iv_action) {
            shareFile();
        }
    }

    private void shareFile() {
        String path = LogFileUtil.getLogPath();
        File file = new File(path);
        if (!file.exists()) {
            ToastUtils.show("日志文件不存在");
            return;
        }
        Uri contentUri = FileProviderUtils.uriFromFile(this, file);

        new Share2.Builder(this)
                .setContentType(ShareContentType.FILE)
                .setShareFileUri(contentUri)
                .setTitle("分享文件")
                .setOnActivityResult(300)
                .build()
                .shareBySystem();
    }
}
