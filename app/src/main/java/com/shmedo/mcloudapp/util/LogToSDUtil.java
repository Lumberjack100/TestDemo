package com.shmedo.mcloudapp.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Environment;

import androidx.appcompat.app.AlertDialog;

import com.shmedo.mcloudapp.ui.activity.device.senior.LogPrintActivity;
import com.shmedo.mcloudapp.util.permission.FileUtils;
import com.shmedo.mcloudapp.util.permission.RuntimeRationale;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 创建者:   dpc
 * 创建时间:  2019-12-23
 * 描述：    日志输出到sd卡中
 */
public class LogToSDUtil {

    /**
     * 日志输出功能需求：
     *  一、以每个设备的sn号新建文件夹，以时间（天）为单位新建文件
     *  二、文件内容格式：    年月日时分秒 文件内容
     *  三、同一sn号同一天的日志 追加内容
     */
    public static void saveLogToSD(String content,String snNumber){
        File filesPath = Environment.getExternalStorageDirectory().getAbsoluteFile();
        File file =  LogFileUtil.createLogFile(filesPath,snNumber);
        try {
            FileOutputStream fos = new FileOutputStream(file,true);
            fos.write(content.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void requestPermissionForSaveLog(final Activity activity,String snNumber) {
        if (!FileUtils.externalAvailable()) {
            new AlertDialog.Builder(activity)
                    .setTitle("提示")
                    .setMessage("请允许写入文件权限")
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
                        LogPrintActivity.startActivity(activity,snNumber);

                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {

                    }
                })
                .start();
    }
}
