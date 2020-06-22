package com.shmedo.mcloudapp.maps.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

/**
 * Created by Jesley on 2016/10/13.
 */

public class ScreenShotAction extends AsyncTask<Void, Integer, File> {
    public static final String SYSTEM_SCRENN_PREFIX = "sys#screen&prefix.";

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

        view.setDrawingCacheEnabled(false);
        view.destroyDrawingCache();
    }

    @Override
    protected File doInBackground(Void... params) {
        String filename = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        if (hasExternalStorageState()) {
            filename = Environment.getExternalStorageDirectory().getPath() + File.separator + activity.getPackageName()
                    + File.separator + "ScreenShot" + File.separator + sdf.format(new Date()) + ".png";
        } else {
            filename = activity.getApplicationContext().getFilesDir().getAbsolutePath()
                    + File.separator + "ScreenShot" + File.separator + sdf.format(new Date()) + ".png";
        }

        Timber.d("filename:" + filename);
        File file = new File(filename);
        if (saveFile(bitmap, file)) {
            return file;
        }
        return null;
    }

    @Override
    protected void onPostExecute(File result) {
        dismissLoadingDialog();
        ToastUtils.show("截图完成");
//        DialogMaker.dismissProgressDialog();
//        if (result != null)
//        {
//            SessionHelper.startP2PSession(activity, account);
//            IMMessage message = MessageBuilder.createImageMessage(account, SessionTypeEnum.P2P, result, SYSTEM_SCRENN_PREFIX + result.getPath());
//            NIMClient.getService(MsgService.class).sendMessage(message, false);
//        }
    }

    /**
     * 保存Bitmap图片为本地文件
     */

    public static boolean saveFile(Bitmap bitmap, File file) {
        boolean isOk = true;
        FileOutputStream fileOutputStream = null;

        try {
            File dir = file.getParentFile();
            if (dir != null) {
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            }

            if (!file.exists())
            {
                file.createNewFile();
            }

            fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

        } catch (IOException e) {
            isOk = false;
            e.printStackTrace();
        }

        return isOk;
    }

    private static boolean hasExternalStorageState() {
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
