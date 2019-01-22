package com.shmedo.mcloudapp.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.util.StartActivityUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.util.XPermissionUtils;
import com.shmedo.mcloudapp.views.LoadingDialog;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   ScanAddDeviceActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/18 08:47
 * 描述：    扫码配置页面
 */
public class ScanAddDeviceActivity extends BaseActivity {
    @BindView(R.id.scan_config) Button mScanConfig;
    @BindView(R.id.input_config) Button mInputConfig;
    private static final int REQUEST_CODE_SCAN = 1;


    @Override protected int initContentView() {
        return R.layout.activity_scanadd_device;
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @OnClick({ R.id.scan_config, R.id.input_config })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.scan_config:
                XPermissionUtils.requestPermissionsResult(this, 200, new String[] {
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE },
                    new XPermissionUtils.OnPermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            starScan();
                        }
                        @Override
                        public void onPermissionDenied() {
                            LoadingDialog.showRefusePermissionDialog(ScanAddDeviceActivity.this,
                                "在设置-应用管理-米易通-权限中开启相机权限");
                        }
                    });
                break;
            case R.id.input_config:
                StartActivityUtil.comeOnBaby(this,InputDeviceSNActivity.class);
                break;
        }
    }

    /**
     * 扫一扫
     */
    private void starScan() {
        Intent intent = new Intent(ScanAddDeviceActivity.this, CaptureActivity.class);
        /*ZxingConfig是配置类
         *可以设置是否显示底部布局，闪光灯，相册，
         * 是否播放提示音  震动
         * 设置扫描框颜色等
         * 也可以不传这个参数
         * */
        ZxingConfig config = new ZxingConfig();
        config.setPlayBeep(true);//是否播放扫描声音 默认为true
        config.setShake(true);//是否震动  默认为true
        config.setDecodeBarCode(false);//是否扫描条形码 默认为true
        config.setReactColor(R.color.app_color_blue_2);//设置扫描框四个角的颜色 默认为白色
        config.setFrameLineColor(R.color.app_color_blue_2);//设置扫描框边框颜色 默认无色
        config.setScanLineColor(R.color.app_color_blue_2);//设置扫描线的颜色 默认白色
        config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
        intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 扫描二维码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {

                String content = data.getStringExtra(Constant.CODED_CONTENT);
                ToastUtil.showSToast("扫描结果为：" + content);
                Log.i("adu", "扫描结果为：" + content);
                Intent intent = new Intent(ScanAddDeviceActivity.this,DeviceActivity.class);
                intent.putExtra("device",content);
                startActivity(intent);
            }
        }
    }
}
