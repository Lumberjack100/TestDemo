package com.shmedo.mcloudapp.maps.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

/**
 * Created by Jesley on 2016/10/13.
 */

public class ScreenShotAction extends AsyncTask<Void, Integer, File> {
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
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        //从缓存中获取当前屏幕的图片
        bitmap = view.getDrawingCache();
        showLoadingDialog(activity, "处理中");

//        view.setDrawingCacheEnabled(false);
//        view.destroyDrawingCache();
    }

    @Override
    protected File doInBackground(Void... params) {
        String filename = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        filename = activity.getExternalFilesDir(null).getAbsolutePath() + File.separator + "ScreenShot" + File.separator + sdf.format(new Date()) + ".png";

        Timber.d("filename:" + filename);
        File file = new File(filename);
        if (saveFile(bitmap, file)) {
            return file;
        }
        return null;
    }

    @Override
    protected void onPostExecute(File file) {
        dismissLoadingDialog();
        ToastUtils.show("截图已保存到相册");
        if (file != null && file.exists()) {
            boolean result = file.delete();
        }
    }

    /**
     * 保存Bitmap图片为本地文件
     */

    private boolean saveFile(Bitmap bitmap, File fileImage) {
        boolean isOk = true;

        try {
            File dir = fileImage.getParentFile();
            if (dir != null) {
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            }
            if (!fileImage.exists()) {
                fileImage.createNewFile();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(fileImage);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            isOk = false;
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(activity.getContentResolver(), fileImage.getAbsolutePath(), fileImage.getName(), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(fileImage);
        intent.setData(uri);
        activity.sendBroadcast(intent);

        return isOk;
    }

    private boolean hasExternalStorageState() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
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
