package com.shmedo.mcloudapp.user.ui.activity;

import static autodispose2.AutoDispose.autoDisposable;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.UriUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserWrapperInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrorInfo;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.RequestHeader;
import com.shmedo.mcloudapp.util.GlideUtils;
import com.shmedo.mcloudapp.util.ResponseHandler;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 用户个人详细信息页
 */
public class UserHomePageActivity extends BaseActivity implements TextWatcher {
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

    @BindView(R.id.btn_confirm)
    Button mBtnConfirm;

    private UserWrapperInfo userWrapperInfo;
    private UserWrapperInfo.UserInfo user;
    private String userName, title, email;

    private Uri photoUri;
    private Uri userAvatarUri;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, UserHomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_home_page;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("我的信息");
        mBtnConfirm.setEnabled(false);
        initData();
    }

    private void initData() {
        userWrapperInfo = MCloudApp.getCurrentUserInfo();
        if (userWrapperInfo != null && userWrapperInfo.getUser() != null) {
            user = userWrapperInfo.getUser();
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
            userName = user.getName() != null ? user.getName() : "";
            title = user.getPosition() != null ? user.getPosition() : "";
            email = user.getEmail() != null ? user.getEmail() : "";
            mEtUserName.setText(userName);
            mEtTitle.setText(title);
            mEtEmail.setText(email);
            mTvPhone.setText(user.getCellPhone() != null ? user.getCellPhone() : "");
        }
        initListener();
    }

    private void initListener() {
        mEtUserName.addTextChangedListener(this);
        mEtTitle.addTextChangedListener(this);
        mEtEmail.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!mEtUserName.getText().toString().equals(userName)
                || !mEtTitle.getText().toString().equals(title)
                || !mEtEmail.getText().toString().equals(email)) {
            mBtnConfirm.setEnabled(true);
        } else {
            mBtnConfirm.setEnabled(false);
        }
    }

    @OnClick({R.id.userLayout, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.userLayout) {//
            showTakePictureDialog();
        } else if (id == R.id.btn_confirm) {//
            preProcessParam();
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
        updateMyInfo();
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
                                checkTakePhotoPermission();
                                break;
                            case 1:
                                checkSDCardPermission();
                                break;
                        }
                    }
                });
        builder.show();
    }

    private void checkTakePhotoPermission() {
        XPermissionUtils.requestPermissionsResult(this, 200, new String[]{
                        Manifest.permission.CAMERA},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        takePhoto();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        boolean allNeverAskAgain = XPermissionUtils.isAllNeverAskAgain(UserHomePageActivity.this, deniedPermissions);
                        // 所有的权限都被勾上不再询问时，跳转到应用设置界面，引导用户手动打开权限
                        if (allNeverAskAgain) {
                            XPermissionUtils.showRefusePermissionDialog(UserHomePageActivity.this, StringUtils.getString(R.string.message_permission_camera_rationale));
                        } else {
                            ToastUtils.show(StringUtils.getString(R.string.message_permission_camera_denied));
                        }
                    }
                });
    }

    private void checkSDCardPermission() {
        XPermissionUtils.requestPermissionsResult(this, 200, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        chooseFromAlbum();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        boolean allNeverAskAgain = XPermissionUtils.isAllNeverAskAgain(UserHomePageActivity.this, deniedPermissions);
                        // 所有的权限都被勾上不再询问时，跳转到应用设置界面，引导用户手动打开权限
                        if (allNeverAskAgain) {
                            XPermissionUtils.showRefusePermissionDialog(UserHomePageActivity.this, StringUtils.getString(R.string.message_permission_storage_rationale));
                        } else {
                            ToastUtils.show(StringUtils.getString(R.string.message_permission_storage_denied));
                        }
                    }
                });
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
        photoUri = UriUtils.file2Uri(outputImage);
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
                    ToastUtils.show(StringUtils.getString(R.string.crop_failed));
                }
                break;
        }
    }

    /**
     * 对指定图片进行裁剪。
     *
     * @param uri 图片的uri地址。
     */
    private void cropPhoto(Uri uri) {
        int reqWidth = ScreenUtils.getScreenWidth();
        int reqHeight = reqWidth;
        CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setFixAspectRatio(true)
                .setAspectRatio(reqWidth, reqHeight)
                .setActivityTitle(StringUtils.getString(R.string.crop))
                .setRequestedSize(reqWidth, reqHeight)
                .setCropMenuCropButtonIcon(R.drawable.ic_crop)
                .start(this);
    }

    /**
     * 显示剪裁后的头像，并上传至服务器
     *
     * @param imageUri
     */
    private void showCroppedPhoto(Uri imageUri) {
        if (imageUri == null)
            return;

        userAvatarUri = imageUri;
        GlideUtils.loadImage(this, imageUri.getPath(), mIvUserAvatar, R.drawable.ic_avatar_default);
        SetUserHeadPhotoTask();
    }

    private String getBase64ImageString(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        String imgString = bitmapToBase64(bitmap);

        return imgString;
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    private String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream outputStream = null;
        try {
            if (bitmap != null) {
                outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                outputStream.flush();
                outputStream.close();

                byte[] bitmapBytes = outputStream.toByteArray();
                float byteLength = ((float) bitmapBytes.length / 1024 / 1024);
                Timber.i("bytes.length=  " + byteLength + "MB");

                result = Base64.encodeToString(bitmapBytes, Base64.NO_WRAP);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 上传用户头像
     */
    private void SetUserHeadPhotoTask() {
        String fileName = FileUtils.getFileName(userAvatarUri.getPath());
        String fileContent = getBase64ImageString(userAvatarUri.getPath());
        if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(fileContent)) {
            ToastUtils.show("头像图片文件名或图片Base64字符串为空");
            return;
        }
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("companyID", MCloudApp.getCompanyID());
            jsonObjectRequest.put("userID", MCloudApp.getUserID());
            jsonObjectRequest.put("content", fileContent);
            jsonObjectRequest.put("extension", "png");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);
        showWaitDialog("正在上传...");

        MDRetrofit.getInstance()
                .createService()
                .uploadUserAvatar(MCloudApp.getAccessToken(), body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(new BaseObserver<String>() {
                    @Override
                    protected void onResponse(String data, ErrorInfo errorInfo) {
                        dismissWaitDialog();
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                ToastUtils.show("头像已上传");
                                if (user != null && userAvatarUri != null && !TextUtils.isEmpty(userAvatarUri.getPath())) {
                                    user.setHeadPhotoPath(userAvatarUri.getPath());
                                }
                            } else {
                                if (!TextUtils.isEmpty(errorInfo.getMsg())) {
                                    ToastUtils.show(errorInfo.getMsg());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissWaitDialog();
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }

    private void updateMyInfo() {
        showWaitDialog("处理中...");
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("companyID", MCloudApp.getCompanyID());
            jsonObjectRequest.put("userID", MCloudApp.getUserID());
            jsonObjectRequest.put("name", userName);
            if (!TextUtils.isEmpty(title))
                jsonObjectRequest.put("position", title);
            if (!TextUtils.isEmpty(email))
                jsonObjectRequest.put("email", email);
        } catch (JSONException e) {
            dismissWaitDialog();
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);

        MDRetrofit.getInstance()
                .createService()
                .updateUser(MCloudApp.getAccessToken(), body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(new BaseObserver<String>() {
                    @Override
                    protected void onResponse(String s, ErrorInfo errorInfo) {
                        dismissWaitDialog();
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                mBtnConfirm.setEnabled(false);
                                updateUserInfoCache();
                                exitActivcity();
                            } else {
                                if (!TextUtils.isEmpty(errorInfo.getMsg())) {
                                    ToastUtils.show(errorInfo.getMsg());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissWaitDialog();
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }

    private void updateUserInfoCache() {
        if (user != null) {
            user.setName(userName);
            user.setPosition(title);
            user.setEmail(email);
        }
    }

    private void exitActivcity() {
        ToastUtils.show("已修改");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }
}
