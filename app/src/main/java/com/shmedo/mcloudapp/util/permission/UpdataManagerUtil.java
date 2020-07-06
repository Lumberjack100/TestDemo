package com.shmedo.mcloudapp.util.permission;

import android.app.Activity;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.update.DownloadFileListener;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.pgyersdk.update.javabean.AppBean;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.util.FileUtils;
import com.shmedo.mcloudapp.util.XPermissionUtils;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.util.List;

import timber.log.Timber;

/**
 * 版本更新
 */
public class UpdataManagerUtil {

    public static void requestPermissionForInstallPackage(final Activity activity, boolean tag) {
        if (!FileUtils.externalAvailable()) {
            new AlertDialog.Builder(activity)
                    .setTitle("提示")
                    .setMessage("您的手机没有SD卡，无法进行版本自动升级")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return;
        }

        AndPermission.with(activity)
                .runtime()
                .permission(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
                .rationale(new RuntimeRationale())
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        if (tag) {
                            upDataVersion();
                        } else {
                            UpgradeVersion();
                        }
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
//                        if (AndPermission.hasAlwaysDeniedPermission(activity, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)) {
//                            showStoragePermissionSettingDialog(activity);
//                        }
                        showStoragePermissionSettingDialog(activity);
                    }
                })
                .start();
    }


    private static void UpgradeVersion() {
        //蒲公英检查更新
        try {
            new PgyUpdateManager.Builder()
                    .setForced(false)                //设置是否强制提示更新
                    // v3.0.4+ 以上同时可以在官网设置强制更新最高低版本；网站设置和代码设置一种情况成立则提示强制更新
                    .setUserCanRetry(true)         //失败后是否提示重新下载
                    .setDeleteHistroyApk(true)     // 检查更新前是否删除本地历史 Apk， 默认为true
                    .register();

        } catch (Exception e) {
            PgyCrashManager.reportCaughtException(e);
        }
    }

    private static void upDataVersion() {
        new PgyUpdateManager.Builder()
                .setForced(true)                //设置是否强制更新,非自定义回调更新接口此方法有用
                .setUserCanRetry(false)         //失败后是否提示重新下载，非自定义下载 apk 回调此方法有用
                .setDeleteHistroyApk(false)     // 检查更新前是否删除本地历史 Apk
                .setUpdateManagerListener(new UpdateManagerListener() {
                    @Override
                    public void onNoUpdateAvailable() {
                        //没有更新是回调此方法
                        Timber.d("there is no new version");
                        ToastUtils.show("当前已是最新版本");
                    }

                    @Override
                    public void onUpdateAvailable(AppBean appBean) {
                        //没有更新是回调此方法
                        Timber.d("there is new version can update"
                                + "new versionCode is " + appBean.getVersionCode());

                        //调用以下方法，DownloadFileListener 才有效；如果完全使用自己的下载方法，不需要设置DownloadFileListener
                        PgyUpdateManager.downLoadApk(appBean.getDownloadURL());
                    }

                    @Override
                    public void checkUpdateFailed(Exception e) {
                        //更新检测失败回调
                        Timber.e(e, "check update failed ");
                    }
                })
                //注意 ：下载方法调用 PgyUpdateManager.downLoadApk(appBean.getDownloadURL()); 此回调才有效
                .setDownloadFileListener(new DownloadFileListener() {   // 使用蒲公英提供的下载方法，这个接口才有效。
                    @Override
                    public void downloadFailed() {
                        //下载失败
                        Timber.e("download apk failed");
                    }

                    @Override
                    public void downloadSuccessful(File file) {
                        Timber.e("download apk failed");
                        PgyUpdateManager.installApk(file);  // 使用蒲公英提供的安装方法提示用户 安装apk
                    }

                    @Override
                    public void onProgressUpdate(Integer... integers) {
                        Timber.e("update download apk progress : %s", integers[0]);
                    }
                })
                .register();
    }

    private static void showStoragePermissionSettingDialog(final Activity activity) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(activity)
                .title("权限申请").content(activity.getResources().getString(R.string.permission_external_storage))
                .positiveText("去设置")
                .positiveColor(activity.getResources().getColor(R.color.colorPrimary))
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        AndPermission.with(activity).runtime().setting().start(XPermissionUtils.REQUEST_CODE_STORAGE_PERMISSION);
                    }
                });

        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /*private void writeApkForInstallPackage(final Activity activity) {
        new TaskExecutor<File>(activity) {
            @Override
            protected File doInBackground(Void... voids) {
                try {
                    InputStream input = activity.getAssets().open("android.apk");
                    File apk = new File(FileUtils.getExternalDir(activity, Environment.DIRECTORY_DOWNLOADS), "AndPermission.apk");
                    if (apk.exists()) return apk;

                    OutputStream output = new BufferedOutputStream(new FileOutputStream(apk));

                    IOUtils.write(input, output);
                    IOUtils.close(output);

                    return apk;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onFinish(File apkFile) {
                if (apkFile == null) {
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.title_dialog)
                            .setMessage(R.string.message_error_save_failed)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                } else {
                    installPackage(apkFile);
                }
            }
        }.execute();
    }*/


}
