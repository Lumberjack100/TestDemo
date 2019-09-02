package com.shmedo.mcloudapp.ui.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.facebook.stetho.common.LogUtil;
import com.google.gson.reflect.TypeToken;
import com.shmedo.das.utils.StringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.UserInfo;
import com.shmedo.mcloudapp.entity.UserInfoWrapper;
import com.shmedo.mcloudapp.entity.parameter.SetUserHeadPhotoParameter;
import com.shmedo.mcloudapp.model.BaseObserver;
import com.shmedo.mcloudapp.model.MDRetrofit;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.FileProviderUtils;
import com.shmedo.mcloudapp.util.FileUtil;
import com.shmedo.mcloudapp.util.GlideUtils;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.ImageUtil;
import com.shmedo.mcloudapp.util.SystemProgramUtils;
import com.shmedo.mcloudapp.util.XPermissionUtils;
import com.shmedo.mcloudapp.views.HintDialog;
import com.shmedo.mcloudapp.views.LoadingDialog;
import com.shmedo.mcloudapp.views.MyMenu;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
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

    @BindView(R.id.circle_image) CircleImageView mCircleImage;
    @BindView(R.id.ll_change_photo) LinearLayout mLlChangePhoto;
    @BindView(R.id.tv_account) TextView mTvAccount;
    @BindView(R.id.tv_phone) TextView mTvPhone;
    @BindView(R.id.tv_name) TextView mTvName;
    @BindView(R.id.tv_company) TextView mTvCompany;
    @BindView(R.id.tv_department) TextView mTvDepartment;
    @BindView(R.id.RL_advice) RelativeLayout mRLAdvice;
    @BindView(R.id.btn_exit) Button mBtnExit;
    private DaoManager manager = DaoManager.getInstance();
    private UserInfoWrapper userInfoWrapper;
    private UserInfo userInfo;
    private UserInfo.UserBean user;
    private InputMethodManager imm;
    private MyMenu myMenu;
    private boolean isCamera = false;
    private LoadingDialog mLoadingDialog;

    @Override protected int initContentView() {
        return R.layout.activity_user_info;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
    }


    private void initData() {
        manager.init(this);
        mLoadingDialog = new LoadingDialog(this);
        userInfoWrapper = manager.getDaoSession().getUserInfoWrapperDao().queryBuilder().unique();
        userInfo = GsonFactory.getGson()
            .fromJson(userInfoWrapper.getUserInfo(), new TypeToken<UserInfo>() {}.getType());
        if (userInfo.getUser() != null) {
            user = userInfo.getUser();
            Log.i("adu", "----user----" + user.toString());
            if (user.getHeadPhotoPath() != null) {
                GlideUtils.loadImage(this, userInfo.getUser().getHeadPhotoPath(), mCircleImage,
                    R.drawable.userphoto);
            }
            mTvAccount.setText(user.getAccount() != null ? user.getAccount() : "");
            mTvPhone.setText(user.getCellPhone() != null ? user.getCellPhone() : "");
            mTvName.setText(user.getName() != null ? user.getName() : "");
            mTvCompany.setText(user.getPosition() != null ? user.getPosition() : "");
            mTvDepartment.setText(user.getUpdateTime() != null ? user.getUpdateTime() : "");
        }
        //加载头像
        //if (MyCache.isRecreate(this)) {
        //    return;
        //}
        //user = CommonVariable.getLoginUser();
        try {
            Log.d("adu", "getHeadPhotoPath===" + user.getHeadPhotoPath());
            if (user != null) {
                if (user == null) {
                    return;
                }
                if (user.getName() != null) {
                    mTvName.setText(user.getName());
                    mTvAccount.setText(user.getAccount());
                }
                //initUserInfo();
            } else {
                //initUserSubInfo();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initView() {
        if (Build.VERSION.SDK_INT >= 21) {
            getSupportActionBar().setElevation(0);
        }
        //mLoadingDialog = new LoadingDialog(this);
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
        XPermissionUtils.requestPermissionsResult(this, 200, new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE },
            new XPermissionUtils.OnPermissionListener() {
                @Override public void onPermissionGranted() {
                    cameraOrAlbum();
                }


                @Override public void onPermissionDenied() {

                }
            });

    }
    private void cameraOrAlbum() {
        if (isCamera) {
            //调用系统相机进行拍照
            SystemProgramUtils.paizhao( this,
                new File("/mnt/sdcard/tupian.png"));
        } else {
            //调用系统相册选择照片
            SystemProgramUtils.zhaopian( this);
        }
    }
    @OnClick({ R.id.ll_change_photo, R.id.RL_advice ,R.id.btn_exit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_change_photo:
                myMenu.show();
                break;
            case R.id.RL_advice:

                break;
            case R.id.btn_exit:
                exitApp();
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        Uri filtUri;
        //裁切后输出的图片
        File outputFile = new File("/mnt/sdcard/tupian.png");
        switch (requestCode) {
            case SystemProgramUtils.REQUEST_CODE_PAIZHAO:
                //拍照完成，进行图片裁切
                File file = new File("/mnt/sdcard/tupian.png");
                filtUri = FileProviderUtils.uriFromFile( this, file);
                SystemProgramUtils.Caiqie(this, filtUri, outputFile);
                break;
            case SystemProgramUtils.REQUEST_CODE_ZHAOPIAN:
                //相册选择图片完毕，进行图片裁切
                if (data == null || data.getData() == null) {
                    return;
                }
                filtUri = data.getData();
                SystemProgramUtils.Caiqie(this, filtUri, outputFile);
                break;
            case SystemProgramUtils.REQUEST_CODE_CAIQIE:
                //图片裁切完成，显示裁切后的图片
                try {
                    Uri uri = Uri.fromFile(outputFile);
                    Bitmap bitmap = BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(uri));
                    mCircleImage.setImageBitmap(bitmap);
                    ImageUtil.saveImageToFile(bitmap, CommonVariable.getUserHeadPhotoFileName());
                    ////上传用户头像
                  SetUserHeadPhotoTask();
                    //as.execute();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            default:
                break;
        }
    }


    private void SetUserHeadPhotoTask() {
        String filePath = CommonVariable.getUserHeadPhotoFileName();
        if (StringUtil.isNullOrEmpty(filePath)) {
            return ;
        }
        String fileName = FileUtil.getFileName(filePath);
        String fileContent = FileUtil.toString(filePath);
        if (StringUtil.isNullOrEmpty(fileName) || StringUtil.isNullOrEmpty(fileContent)) {
            return  ;
        }
        SetUserHeadPhotoParameter parameter = new SetUserHeadPhotoParameter();
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(CommonVariable.JSON_TYPE, json);
        mLoadingDialog.showNoCancelDialog("正在上传...");
        MDRetrofit.getInstance().createService().setUserHeadPhoto(body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new BaseObserver<String>() {
                @Override public void Success(String s, String message) {
                    mLoadingDialog.dismiss();
                    Log.i("adu","===setUserHeadPhoto接口==="+s);
                    initUserInfo();

                }
                @Override public void Failure(String message) {
                    mLoadingDialog.dismiss();
                    Log.i("adu","setUserHeadPhoto接口==="+message);
                }
            });
    }
    private void initUserInfo() {

        String userPhotFileName = CommonVariable.getUserHeadPhotoFileName();

        if (StringUtil.isNullOrEmpty(userPhotFileName)) {
            return;
        }
        File f = new File(userPhotFileName);
        LogUtil.i("adu",userPhotFileName+"tbUser.getHeadPhotoPath()===22=="+f.getName());
        if (f.exists()) {
            Bitmap bitmap = ImageUtil.getLocalImage(userPhotFileName);
            if (bitmap != null) {
                mCircleImage.setImageBitmap(bitmap);
            }
            LogUtil.d("adu","tbUser.getHeadPhotoPath()===222222=="+f.exists());
        } else {
            userInfo = CommonVariable.getCurrentUserInfo();
            if (userInfo == null) {
                return;
            }
            final String headPhotoPath = userInfo.getUser().getHeadPhotoPath();

            if (StringUtil.isNullOrEmpty(headPhotoPath)) {
                return;
            }
            GlideUtils.loadImage(this, headPhotoPath, mCircleImage,R.drawable.userphoto);
        }
    }


    /**
     * 退出app
     */
    private void exitApp() {
        new HintDialog.Builder(this)
            .setMessage("确定注销并退出吗?")
            .setCancelBtnListener(null)
            .setConfirmBtnListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == -1) {
                        dialog.cancel();
                        exitLogin();   //注销账号
                    }
                }
            })
            .onCreate().show();
    }


    private void exitLogin() {
        Intent in = new Intent(this, LoginActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(in);
        finish();
    }
}
