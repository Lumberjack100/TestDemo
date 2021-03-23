package com.shmedo.mcloudapp.common.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.shmedo.mcloudapp.R;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
import cn.bingoogolapple.qrcode.core.BarcodeType;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import timber.log.Timber;

public class ScanActivity extends BaseActivity implements QRCodeView.Delegate {

    private static final int REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY = 666;
    public static final String CODED_CONTENT = "coded_content";

    @BindView(R.id.zxingview)
    ZXingView mZXingView;

    @BindView(R.id.flashLightIv)
    ImageView flashLightIv;

    @BindView(R.id.flashLightTv)
    TextView flashLightTv;

    private boolean FLASH_OPEN = false;


    public static void startActivityForResultByFragment(Fragment context, int requestCode) {
        Intent intent = new Intent(context.getActivity(), ScanActivity.class);
        context.startActivityForResult(intent, requestCode);
    }


    public static void startActivityForResult(Activity context, int requestCode) {
        Intent intent = new Intent(context, ScanActivity.class);
        context.startActivityForResult(intent, requestCode);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_scan;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mZXingView.setDelegate(this);
        //#gh# 设置只扫描识别二维码
        mZXingView.setType(BarcodeType.ONLY_QR_CODE, null);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mZXingView.startCamera(); // 打开后置摄像头开始预览，但是并未开始识别
//        mZXingView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT); // 打开前置摄像头开始预览，但是并未开始识别
        mZXingView.startSpotAndShowRect(); // 显示扫描框，并开始识别
    }

    @Override
    protected void onStop() {
        mZXingView.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mZXingView.onDestroy(); // 销毁二维码扫描控件
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Timber.i("Scan Code result: " + result);
        vibrate();

        Intent intent = getIntent();
        intent.putExtra(CODED_CONTENT, result);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
        // 这里是通过修改提示文案来展示环境是否过暗的状态，接入方也可以根据 isDark 的值来实现其他交互效果
        String tipText = mZXingView.getScanBoxView().getTipText();
        String ambientBrightnessTip = "\n环境过暗，请打开闪光灯";
        if (isDark) {
            if (!tipText.contains(ambientBrightnessTip)) {
                mZXingView.getScanBoxView().setTipText(tipText + ambientBrightnessTip);
            }
        } else {
            if (tipText.contains(ambientBrightnessTip)) {
                tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip));
                mZXingView.getScanBoxView().setTipText(tipText);
            }
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Timber.e("打开相机出错");
    }


    @OnClick({R.id.capture_imageview_back, R.id.flashLightLayout, R.id.inputSNLayout, R.id.albumLayout})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.capture_imageview_back:
                finish();
                break;

            case R.id.flashLightLayout:
                FLASH_OPEN = !FLASH_OPEN;
                switchFlashImg();
                break;

            case R.id.inputSNLayout:
//                InputDeviceSNActivity.startActivity(this);
//                finish();
                break;

            case R.id.albumLayout:
                 /*
                从相册选取二维码图片，这里为了方便演示，使用的是
                https://github.com/bingoogolapple/BGAPhotoPicker-Android
                这个库来从图库中选择二维码图片，这个库不是必须的，你也可以通过自己的方式从图库中选择图片
                 */
                Intent photoPickerIntent = new BGAPhotoPickerActivity.IntentBuilder(this)
                        .cameraFileDir(null)
                        .maxChooseCount(1)
                        .selectedPhotos(null)
                        .pauseOnScroll(false)
                        .build();
                startActivityForResult(photoPickerIntent, REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY);
                break;
        }
    }

    /**
     * 切换闪光灯图片
     */
    public void switchFlashImg() {
        if (FLASH_OPEN) {
            mZXingView.openFlashlight(); // 打开闪光灯
            flashLightIv.setImageResource(R.drawable.ic_open_flashligh);
            flashLightTv.setText(R.string.close_flash);

        } else {
            mZXingView.closeFlashlight(); // 关闭闪光灯
            flashLightIv.setImageResource(R.drawable.ic_close_flashligh);
            flashLightTv.setText(R.string.open_flash);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mZXingView.startSpotAndShowRect(); // 显示扫描框，并开始识别

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY) {
            final String picturePath = BGAPhotoPickerActivity.getSelectedPhotos(data).get(0);
            // 本来就用到 QRCodeView 时可直接调 QRCodeView 的方法，走通用的回调
            mZXingView.decodeQRCode(picturePath);
        }
    }

}
