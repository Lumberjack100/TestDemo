package com.shmedo.mcloudapp.user.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.shmedo.core.util.GlobalUtil;
import com.hjq.toast.ToastUtils;
import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.feedback.PgyerFeedbackManager;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.common.ui.activity.TestBluetoothListActivity;
import com.shmedo.mcloudapp.common.ui.activity.WebViewActivity;
import com.shmedo.mcloudapp.util.permission.RuntimeRationale;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 创建者:   dpc
 * 创建时间:  2019-12-19
 * 描述：    关于app
 */
public class AboutAppActivity extends BaseActivity {
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.logoImage)
    ImageView mIvLogo;

    @BindView(R.id.tv_version)
    TextView mTvVersion;

    @BindView(R.id.connectTestLayout)
    View connectTestLayout;


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, AboutAppActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_about;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("关于");
        initData();
        initTouchListener();
    }

    private void initTouchListener() {
        mIvLogo.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onSingleClick(View v) {

            }

            @Override
            public void onDoubleClick(View v) {
                connectTestLayout.setVisibility(View.VISIBLE);
            }
        });
    }


    private void initData() {
        String localVersion = GlobalUtil.getAppVersionName();
        mTvVersion.setText(String.format("米易通 v%s", localVersion));
        connectTestLayout.setVisibility(View.GONE);
    }


    @OnClick({R.id.ll_userAdvice, R.id.userProtocolLayout, R.id.privacyPolicyLayout, R.id.appIntroLayout, R.id.connectTestLayout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_userAdvice:
                showFeedbackDialog();
                break;

            case R.id.userProtocolLayout: {
                String url = "file:///android_asset/private/UserProtocol.html";
                WebViewActivity.startActivity(this, url);
            }
            break;

            case R.id.privacyPolicyLayout: {
                String url = "file:///android_asset/private/PrivacyPolicy.html";
                WebViewActivity.startActivity(this, url);
            }
            break;

            case R.id.appIntroLayout: {
                String url = "file:///android_asset/private/AppIntro.html";
                WebViewActivity.startActivity(this, url);
            }
            break;

            case R.id.connectTestLayout: //连接测试
                TestBluetoothListActivity.startActivity(this);
                break;
        }
    }

    public abstract class DoubleClickListener implements View.OnClickListener {
        private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds

        private long lastClickTime = 0;

        @Override
        public void onClick(View v) {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                onDoubleClick(v);
            } else {
                onSingleClick(v);
            }
            lastClickTime = clickTime;
        }

        public abstract void onSingleClick(View v);

        public abstract void onDoubleClick(View v);
    }

    /**
     * 弹出反馈dialog
     */
    private void showFeedbackDialog() {
        AndPermission.with(this)
                .runtime()
                .permission(Permission.Group.STORAGE, Permission.Group.MICROPHONE)
                .rationale(new RuntimeRationale())
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        try {
                            new PgyerFeedbackManager.PgyerFeedbackBuilder()
                                    .setShakeInvoke(false)       //fasle 则不触发摇一摇，最后需要调用 invoke 方法
                                    // true 设置需要调用 register 方法使摇一摇生效
                                    .setDisplayType(PgyerFeedbackManager.TYPE.DIALOG_TYPE)   //设置以Dialog 的方式打开
                                    .setColorDialogTitle("#FFFFFF")    //设置Dialog 标题的字体颜色，默认为颜色为#ffffff
                                    .setColorTitleBg("#13a0ff")        //设置Dialog 标题栏的背景色，默认为颜色为#2E2D2D
                                    .setBarBackgroundColor("#FF0000")      // 设置顶部按钮和底部背景色，默认颜色为 #2E2D2D
                                    .setBarButtonPressedColor("#FF0000")        //设置顶部按钮和底部按钮按下时的反馈色 默认颜色为 #383737
                                    .setColorPickerBackgroundColor("#FF0000")   //设置颜色选择器的背景色,默认颜色为 #272828
                                    //.setMoreParam("KEY1","VALUE1") //自定义的反馈数据
                                    //.setMoreParam("KEY2","VALUE2") //自定义的反馈数据
                                    .builder()
                                    .invoke();
                            //PgyFeedbackShakeManager.register(AboutAppActivity.this);
                            //PgyerDialog.setDialogTitleBackgroundColor("#03A9F4");
                            //PgyFeedback.getInstance().showDialog(AboutAppActivity.this);
                        } catch (Exception ex) {
                            PgyCrashManager.reportCaughtException(ex);
                        }
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        ToastUtils.show("请同意申请权限进行有效反馈");
                    }
                }).start();

    }

}
