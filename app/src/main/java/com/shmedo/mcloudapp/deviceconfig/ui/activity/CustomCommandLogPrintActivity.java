package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.UriUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.LogFileUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.USRBleIotCustomCommandLogPrintFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BleDasCustomCommandLogPrintFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.Inclinometer_debug_box.InclinometerDebugBoxLoggerFragment;

import java.io.File;

import gdut.bsx.share2.Share2;
import gdut.bsx.share2.ShareContentType;

/**
 * 指令日志调试页面
 */
public class CustomCommandLogPrintActivity extends BaseConfigFragmentContainerActivity {
    private ProductType productType = ProductType.DAS;


    public static void startActivity(Context context, int connectWay, ProductType productType) {
        Intent intent = new Intent(context, CustomCommandLogPrintActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.PRODUCT_TYPE, productType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("指令调试");
        mIvAction.setVisibility(View.VISIBLE);
        mIvAction.setImageResource(R.drawable.icon_share_command_log);
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.PRODUCT_TYPE)) {
            productType = (ProductType) intent.getSerializableExtra(AppContants.Extras.PRODUCT_TYPE);
            if (productType == ProductType.INCLINOMETER_DEBUG_BOX) {
                mIvAction.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            switch (productType) {
                case DAS:
                    fragment = new BleDasCustomCommandLogPrintFragment();
                    break;

                case ADME:
                case RN20:
                case M20:
                case LR200:
                    fragment = USRBleIotCustomCommandLogPrintFragment.newInstance();
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.USB_SERIAL) {
            if (productType == ProductType.INCLINOMETER_DEBUG_BOX) {
                fragment = new InclinometerDebugBoxLoggerFragment();
            }
        }
        return fragment;
    }

    @Override
    protected boolean isShouldHideKeyboard(View v, MotionEvent event) {
        return false;
    }

    @Override
    protected void onIconActionClick() {
        shareFile();
    }

    private void shareFile() {
        String path = LogFileUtil.getLogPath();
        File file = new File(path);
        if (!file.exists()) {
            ToastUtils.show("日志文件不存在");
            return;
        }
        Uri contentUri = UriUtils.file2Uri(file);
        new Share2.Builder(this)
                .setContentType(ShareContentType.FILE)
                .setShareFileUri(contentUri)
                .setTitle("分享文件")
                .setOnActivityResult(300)
                .build()
                .shareBySystem();
    }
}
