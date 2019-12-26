package com.shmedo.mcloudapp.ui.activity;

import android.Manifest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.hjq.toast.ToastUtils;
import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.feedback.PgyerFeedbackManager;
import com.pgyersdk.update.DownloadFileListener;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.pgyersdk.update.javabean.AppBean;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.util.permission.Permission;
import com.shmedo.mcloudapp.util.permission.RuntimeRationale;
import com.shmedo.mcloudapp.util.permission.UpdataManagerUtil;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 创建者:   dpc
 * 创建时间:  2019-12-19
 * 描述：    关于app
 */
public class AboutAppActivity extends BaseActivity {

    @BindView(R.id.tv_version)
    TextView mTvVersion;
    @BindView(R.id.ll_userAdvice)
    LinearLayout mLlUserAdvice;
    @BindView(R.id.ll_userUpData) LinearLayout mLlUserUpData;


    @Override protected int initContentView() {
        return R.layout.activity_about;
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }


    private void initData() {
        String localVersion = "";
        try {
            PackageInfo packageInfo = getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mTvVersion.setText("米易通当前版本：" + localVersion);
    }


    @OnClick({ R.id.ll_userAdvice, R.id.ll_userUpData })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_userAdvice:
                showFeedbackDialog();
                break;
            case R.id.ll_userUpData:
                UpdataManagerUtil.requestPermissionForInstallPackage(this,true);
                break;
        }
    }


    /**
     * 弹出反馈dialog
     */
    private void showFeedbackDialog() {
        AndPermission.with(this)
                .runtime()
                .permission(Permission.Group.STORAGE,Permission.Group.MICROPHONE)
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
                            PgyCrashManager.reportCaughtException( ex);
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





    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
}
