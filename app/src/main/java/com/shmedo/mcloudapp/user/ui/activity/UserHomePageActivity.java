package com.shmedo.mcloudapp.user.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.dragon.core.MCloudApp;
import com.dragon.core.model.UserInfo;
import com.dragon.core.util.DeviceInfo;
import com.dragon.core.util.GlobalUtil;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.parameter.SetUserHeadPhotoParameter;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.user.model.UpdateMyInfoParam;
import com.shmedo.mcloudapp.util.FileProviderUtils;
import com.shmedo.mcloudapp.util.FileUtils;
import com.shmedo.mcloudapp.util.GlideUtils;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.views.ClearEditText;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 用户个人详细信息页
 */
public class UserHomePageActivity extends BaseActivity {
    private static final String TEMP_PHOTO = "taken_photo.jpg";
    private static final int TAKE_PHOTO = 0x1000;
    private static final int CHOOSE_FROM_ALBUM = 0x1001;

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.userAvatar)
    CircleImageView mIvUserAvatar;

    @BindView(R.id.userNameET)
    ClearEditText mEtUserName;

    @BindView(R.id.titleET)
    ClearEditText mEtTitle;

    @BindView(R.id.emailET)
    ClearEditText mEtEmail;

    @BindView(R.id.tv_phone)
    TextView mTvPhone;

    private UserInfo userInfo;
    private UserInfo.UserBean user;
    private String userName, title, email;

    private Uri photoUri;
    private Uri userAvatarUri;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, UserHomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_user_home_page;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("我的信息");
        initData();
    }

    private void initData() {
        userInfo = MCloudApp.getCurrentUserInfo();
        if (userInfo != null && userInfo.getUser() != null) {
            user = userInfo.getUser();
            GlideUtils.loadImage(this, user.getHeadPhotoPath(), mIvUserAvatar, R.drawable.ic_avatar_default);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateView();
    }

    private void updateView() {
        if (user != null) {
//            if (user.getHeadPhotoPath() != null) {
//                GlideUtils.loadImage(this, user.getHeadPhotoPath(), mIvUserAvatar, R.drawable.ic_avatar_default);
//            }
            mEtUserName.setText(user.getName() != null ? user.getName() : "");
            mEtTitle.setText(user.getPosition() != null ? user.getPosition() : "");
            mEtEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            mTvPhone.setText(user.getCellPhone() != null ? user.getCellPhone() : "");
        }
    }


    @OnClick({R.id.userLayout, R.id.mobileLayout, R.id.btn_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.userLayout:
                showTakePictureDialog();
                break;

            case R.id.mobileLayout:
                UpdatePhoneActivity.startActivity(this);
                break;

            case R.id.btn_confirm:
                preProcessParam();
                break;
        }
    }

    private void preProcessParam() {
        userName = mEtUserName.getText().toString();
        title = mEtTitle.getText().toString();
        email = mEtEmail.getText().toString();

        if (TextUtils.isEmpty(userName)) {
            ToastUtils.show("请输入用户名");
            mEtUserName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(title)) {
            ToastUtils.show("请输入职位");
            mEtTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            ToastUtils.show("请输入邮箱");
            mEtEmail.requestFocus();
            return;
        }
        updateMyInfo();
    }

    private void updateMyInfo() {
        showLoadingDialog("处理中...");

        UpdateMyInfoParam parameter = new UpdateMyInfoParam();
        parameter.setName(userName);
        parameter.setPosition(title);
        parameter.setEmail(email);
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);

        MDRetrofit.getInstance()
                .createService()
                .UpdateMyInfo(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void Success(String result, String message) {
                        dismissLoadingDialog();
                        ToastUtils.show("修改完成");

                        UserInfo userInfo = MCloudApp.getCurrentUserInfo();
                        if (userInfo != null && userInfo.getUser() != null) {
                            UserInfo.UserBean user = userInfo.getUser();
                            user.setName(userName);
                            user.setPosition(title);
                            user.setEmail(email);
                        }
                        MCloudApp.setCurrentUserInfo(userInfo);
                    }

                    @Override
                    public void Failure(String message) {
                        dismissLoadingDialog();
                        ToastUtils.show(message);
                        Timber.w("请求失败--%s", message);
                    }
                });
    }

    /**
     * 显示选择照片的对话框。
     */
    private void showTakePictureDialog() {
        CharSequence[] items = new CharSequence[]{getString(R.string.take_photo), getString(R.string.your_album)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_avatar))
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                takePhoto();
                                break;
                            case 1:
                                chooseFromAlbum();
                                break;
                        }
                    }
                });
        builder.show();
    }

    /**
     * 打开摄像头拍照。
     */
    private void takePhoto() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            ToastUtils.show(getString(R.string.operation_failed_without_sdcard));
            return;
        }

        // 创建 File 对象，用于存储拍照后的图片
        File outputImage = new File(getExternalCacheDir(), TEMP_PHOTO);
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException ex) {
            Timber.w(ex);
        }

        photoUri = FileProviderUtils.uriFromFile(this, outputImage);
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    /**
     * 从相册中选择图片。
     */
    private void chooseFromAlbum() {
        Matisse.from(this)
                .choose(MimeType.ofAll())
                .countable(false)
                .maxSelectable(1)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .showPreview(false) // Default is `true`
                .forResult(CHOOSE_FROM_ALBUM);
    }


    /**
     * 对指定图片进行裁剪。
     *
     * @param uri 图片的uri地址。
     */
    private void cropPhoto(Uri uri) {
        int reqWidth = DeviceInfo.getScreenWidth();
        int reqHeight = reqWidth;

        CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setFixAspectRatio(true)
                .setAspectRatio(reqWidth, reqHeight)
                .setActivityTitle(GlobalUtil.getString(R.string.crop))
                .setRequestedSize(reqWidth, reqHeight)
                .setCropMenuCropButtonIcon(R.drawable.ic_crop)
                .start(this);
    }

    private void showCroppedPhoto(Uri imageUri) {
        if (imageUri == null)
            return;

        userAvatarUri = imageUri;
        GlideUtils.loadImage(this, imageUri.getPath(), mIvUserAvatar, R.drawable.ic_avatar_default);
        SetUserHeadPhotoTask();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    cropPhoto(photoUri);
                }
                break;

            case CHOOSE_FROM_ALBUM:
                if (resultCode == RESULT_OK) {
                    Uri uri = Matisse.obtainResult(data).get(0);
                    cropPhoto(uri);
                }
                break;

            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == Activity.RESULT_OK) {
                    showCroppedPhoto(result.getUri());
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Timber.w(result.getError(), "Cropping failed: %s", result.getError().getMessage());
                    ToastUtils.show(GlobalUtil.getString(R.string.crop_failed));
                }
                break;
        }
    }

    /**
     * 上传用户头像
     */
    private void SetUserHeadPhotoTask() {
        String filePath = userAvatarUri.getPath();
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        String fileName = FileUtils.getFileName(filePath);
        String fileContent = FileUtils.getFileContent(filePath);
        if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(fileContent)) {
            return;
        }
        SetUserHeadPhotoParameter parameter = new SetUserHeadPhotoParameter();
        parameter.setPhotoName(fileName);
        parameter.setPhotoContent(fileContent);
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        showLoadingDialog("正在上传...");
        MDRetrofit.getInstance()
                .createService()
                .setUserHeadPhoto(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void Success(String s, String message) {
                        dismissLoadingDialog();
                        ToastUtils.show("头像已上传");

//                initUserInfo();
                    }

                    @Override
                    public void Failure(String message) {
                        dismissLoadingDialog();
                        ToastUtils.show("上传头像失败," + message);
                    }
                });
    }
}
