package com.shmedo.mcloudapp.maps.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 截取自己应用内部除了导航栏之外的屏幕
 */

public class ScreenShotAction extends AsyncTask<Void, Integer, Boolean> {
    @SuppressLint("StaticFieldLeak")
    private Activity activity = null;

    @SuppressLint("StaticFieldLeak")
    private View targetView = null;

    private Bitmap bitmap = null;

    private MaterialDialog loadingDialog = null;


    public ScreenShotAction(Activity activity) {
        this(activity, activity.getWindow().getDecorView());
    }

    public ScreenShotAction(Activity activity, View targetView) {
        this.activity = activity;
        this.targetView = targetView;
    }

    @Override
    protected void onPreExecute() {
        View view = targetView.getRootView();
        bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        showLoadingDialog(activity, "处理中");
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        return saveImage(bitmap, sdf.format(new Date()));
    }

    @Override
    protected void onPostExecute(Boolean result) {
        dismissLoadingDialog();
        ToastUtils.show(result ? "截图已保存到相册" : "保存截图发生错误");
    }

    private boolean saveImage(Bitmap bitmap, @NonNull String name) {
        boolean saved = true;
        String IMAGES_FOLDER_NAME = "/medo/screenshot";
        OutputStream fos;
        Uri imageUri = null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = activity.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures" + IMAGES_FOLDER_NAME);
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(imageUri);
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                        + IMAGES_FOLDER_NAME;
                File fileDir = new File(imagesDir);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                File fileImage = new File(imagesDir, name + ".png");
                imageUri = Uri.fromFile(fileImage);
                fos = new FileOutputStream(fileImage);
            }

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            saved = false;
            e.printStackTrace();
        }

        // 最后通知图库更新
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(imageUri);
        activity.sendBroadcast(intent);

        return saved;
    }


    private void showLoadingDialog(Context context, String tip) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            return;
        }

        if (loadingDialog == null) {
            loadingDialog = new MaterialDialog.Builder(context)
                    .content(TextUtils.isEmpty(tip) ? "正在加载..." : tip)
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .build();
            loadingDialog.setCancelable(false);
            loadingDialog.setCanceledOnTouchOutside(false);
        }

        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
}
