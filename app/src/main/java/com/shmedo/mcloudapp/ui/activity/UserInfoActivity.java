package com.shmedo.mcloudapp.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.das.utils.StringUtil;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.UserInfo;
import com.shmedo.mcloudapp.entity.parameter.SetUserHeadPhotoParameter;
import com.shmedo.mcloudapp.model.BaseObserver;
import com.shmedo.mcloudapp.model.MDRetrofit;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.ui.activity.device.QuickActivationActivity;
import com.shmedo.mcloudapp.util.ActivityCollector;
import com.shmedo.mcloudapp.util.ApiName;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.FileProviderUtils;
import com.shmedo.mcloudapp.util.FileUtil;
import com.shmedo.mcloudapp.util.GlideUtils;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.ImageUtil;
import com.shmedo.mcloudapp.util.PhotoUtil;
import com.shmedo.mcloudapp.util.XPermissionUtils;
import com.shmedo.mcloudapp.views.MyMenu;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   UserInfoActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/14 15:01
 * 描述：    个人中心
 */
public class UserInfoActivity extends BaseActivity {

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.circle_image)
    CircleImageView mCircleImage;

    @BindView(R.id.tv_account)
    TextView mTvAccount;

    @BindView(R.id.tv_name)
    TextView mTvName;

    @BindView(R.id.tv_phone)
    TextView mTvPhone;

    @BindView(R.id.tv_company)
    TextView mTvCompany;

    @BindView(R.id.tv_department)
    TextView mTvDepartment;

    @BindView(R.id.RL_advice)
    RelativeLayout mRLAdvice;

    private DaoManager manager = DaoManager.getInstance();
    private UserInfo userInfo;
    private UserInfo.UserBean user;
    private InputMethodManager imm;
    private MyMenu myMenu;
    private boolean isCamera = false;


    /**
     * 说明：启动Activity
     * <p>
     * 注意：这里使用到了Intent的Flag属性singleTop。singleTop模式下，在同一个task中，如果存在该Activity的实例，
     * 并且该Activity实例位于栈顶(即，该Activity位于前端)，则调用startActivity()时，不再创建该Activity的示例；
     * 而仅仅只是调用Activity的onNewIntent()。否则的话，则新建该Activity的实例，并将其置于栈顶。
     * </p>
     */
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, UserInfoActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_user_info;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(R.anim.translate_in_from_left, R.anim.translate_out);
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("我的");
        initData();
        initView();
    }


    private void initData() {
        userInfo = MCloudApp.getCurrentUserInfo();
        if (userInfo != null && userInfo.getUser() != null) {
            user = userInfo.getUser();
            if (user.getHeadPhotoPath() != null) {
                GlideUtils.loadImage(this, user.getHeadPhotoPath(), mCircleImage, R.drawable.userphoto);
            }
            mTvAccount.setText(user.getAccount() != null ? user.getAccount() : "");
            mTvName.setText(user.getName() != null ? user.getName() : "");
            mTvPhone.setText(user.getCellPhone() != null ? user.getCellPhone() : "");
            mTvCompany.setText("");

            List<UserInfo.DepartmentsBean> departments = userInfo.getDepartments();
            if (null != departments && !departments.isEmpty()) {
                UserInfo.DepartmentsBean departmentsBean = departments.get(0);
                mTvDepartment.setText(TextUtils.isEmpty(departmentsBean.getName()) ? "" : departmentsBean.getName());
            }
        }
    }


    private void initView() {
        myMenu = new MyMenu(this, "上传头像", "拍照", "从相册中选择");
        myMenu.setOnMenuClickListener(new MyMenu.MenuOnClickListener() {
            @Override
            public void onItem1Click() {
                //调用系统相机进行拍照
                isCamera = true;
                getSDPermission();
                myMenu.cancel();
            }

            @Override
            public void onItem2Click() {
                //调用系统相册选择照片
                isCamera = false;
                getSDPermission();
                myMenu.cancel();
            }
        });
    }

    /**
     * 获取SD卡、相机权限
     */
    private void getSDPermission() {
        XPermissionUtils.requestPermissionsResult(this, 200, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, new XPermissionUtils.OnPermissionListener() {
            @Override
            public void onPermissionGranted() {
                cameraOrAlbum();
            }

            @Override
            public void onPermissionDenied() {
                XPermissionUtils.showRefusePermissionDialog(UserInfoActivity.this, getResources().getString(R.string.permission_request_camera_external_storage));
            }
        });
    }

    private void cameraOrAlbum() {
        if (isCamera) {
            //调用系统相机进行拍照
            PhotoUtil.paizhao(this, new File("/mnt/sdcard/tupian.png"));
        } else {
            //调用系统相册选择照片
            PhotoUtil.zhaopian(this);
        }
    }

    @OnClick({R.id.ll_change_photo, R.id.RL_advice, R.id.ll_userAbout, R.id.btn_exit, R.id.ll_activation})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_change_photo:
//                myMenu.show();
                break;

            case R.id.RL_advice: //基本资料

                break;

            case R.id.ll_userAbout:
                Intent intent = new Intent(this, AboutAppActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_exit:
                exitApp();
                break;

            case R.id.ll_activation: //快速激活
                QuickActivationActivity.startActivity(this);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        Uri fileUri;
        //裁切后输出的图片
        File outputFile = new File("/mnt/sdcard/tupian.png");

        switch (requestCode) {
            case PhotoUtil.REQUEST_CODE_PAIZHAO:
                //拍照完成，进行图片裁切
                File file = new File("/mnt/sdcard/tupian.png");
                fileUri = FileProviderUtils.uriFromFile(this, file);
                PhotoUtil.Caiqie(this, fileUri, outputFile);
                break;

            case PhotoUtil.REQUEST_CODE_ZHAOPIAN:
                //相册选择图片完毕，进行图片裁切
                if (data == null || data.getData() == null) {
                    return;
                }
                fileUri = data.getData();
                PhotoUtil.Caiqie(this, fileUri, outputFile);
                break;

            case PhotoUtil.REQUEST_CODE_CAIQIE:
                //图片裁切完成，显示裁切后的图片
                try {
                    Uri uri = Uri.fromFile(outputFile);
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
//                    mCircleImage.setImageBitmap(bitmap);
                    //压缩后保存，等待上传到服务器
                    ImageUtil.saveImageToFile(bitmap, MCloudApp.getUserHeadPhotoFileName());
                    //上传用户头像
                    SetUserHeadPhotoTask();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;

            default:
                break;
        }
    }


    /**
     * 上传用户头像
     */
    private void SetUserHeadPhotoTask() {
        String filePath = MCloudApp.getUserHeadPhotoFileName();
        if (StringUtil.isNullOrEmpty(filePath)) {
            return;
        }
        String fileName = FileUtil.getFileName(filePath);
        String fileContent = FileUtil.toString(filePath);
        if (StringUtil.isNullOrEmpty(fileName) || StringUtil.isNullOrEmpty(fileContent)) {
            return;
        }
        SetUserHeadPhotoParameter parameter = new SetUserHeadPhotoParameter();
        parameter.setPhotoName(fileName);
        parameter.setPhotoContent(fileContent);
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(CommonVariable.JSON_TYPE, json);
        showLoadingDialog("正在上传...");
        MDRetrofit.getInstance().createService(ApiName.HTTPS).setUserHeadPhoto(body).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new BaseObserver<String>() {
            @Override
            public void Success(String s, String message) {
               dismissLoadingDialog();
                ToastUtils.show("头像已上传");

                initUserInfo();
            }

            @Override
            public void Failure(String message) {
                dismissLoadingDialog();
                ToastUtils.show("上传头像失败," + message);
            }
        });
    }

    private void initUserInfo() {
        String userPhotFileName = MCloudApp.getUserHeadPhotoFileName();
        if (StringUtil.isNullOrEmpty(userPhotFileName)) {
            return;
        }

        File f = new File(userPhotFileName);
        if (f.exists()) {
            Bitmap bitmap = ImageUtil.getLocalImage(userPhotFileName);
            if (bitmap != null) {
                mCircleImage.setImageBitmap(bitmap);
            }
        } else {
            userInfo = MCloudApp.getCurrentUserInfo();
            if (userInfo == null) {
                return;
            }
            final String headPhotoPath = userInfo.getUser().getHeadPhotoPath();
            if (StringUtil.isNullOrEmpty(headPhotoPath)) {
                return;
            }
            GlideUtils.loadImage(this, headPhotoPath, mCircleImage, R.drawable.userphoto);
        }
    }


    /**
     * 退出app
     */
    private void exitApp() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(UserInfoActivity.this)
                .title("提示")
                .content(getResources().getString(R.string.exit_login_tip))
                .negativeText("取消")
                .positiveText("确定")
                .negativeColor(getResources().getColor(R.color.font_main))
                .positiveColor(getResources().getColor(R.color.colorPrimary))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();

                        MCloudApp.logout();
                        ActivityCollector.finishAll();
                        exitLogin();   //注销账号
                    }
                });

        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }


    /**
     * 跳转到登录页面
     */
    private void exitLogin() {
        Intent in = new Intent(this, LoginActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(in);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.translate_out_to_left);
    }
}
