package com.shmedo.mcloudapp.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

/**
 * 项目名：  das-config-app
 * 包名：    com.example.medoDas.Utils
 * 文件名:   SystemProgramUtils
 * 创建者:   dpc
 * 创建时间:  2018/4/18 09:10
 * 描述：    TODO
 * @author adu
 */
public class SystemProgramUtils {
    public static final int REQUEST_CODE_PAIZHAO = 1;
    public static final int REQUEST_CODE_ZHAOPIAN = 2;
    public static final int REQUEST_CODE_CAIQIE = 3;

    public static void paizhao(Activity activity, File outputFile){
        Intent intent = new Intent();
        intent.setAction("android.media.action.IMAGE_CAPTURE");
        intent.addCategory("android.intent.category.DEFAULT");
        Uri uri = FileProviderUtils.uriFromFile(activity, outputFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        activity.startActivityForResult(intent, REQUEST_CODE_PAIZHAO);
    }

    public static void zhaopian(Activity activity){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction("android.intent.action.PICK");
        intent.addCategory("android.intent.category.DEFAULT");
        activity.startActivityForResult(intent, REQUEST_CODE_ZHAOPIAN);
    }

    public static void Caiqie(Activity activity, Uri uri, File outputFile) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        // Android 7.0 以上添加权限
        FileProviderUtils.setIntentDataAndType(activity, intent, "image/*", uri, true);
        intent.putExtra("crop", "true");

        // 设置裁剪区域的宽高比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // 设置裁剪区域的宽度和高度
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);

        intent.putExtra("scale",true);
        intent.putExtra("scaleUpIfNeeded", true);
        // 图片输出格式
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        // 取消人脸识别
        intent.putExtra("noFaceDetection", true);

        //return-data为true时，直接返回bitmap，可能会很占内存，不建议，小米等个别机型会出异常！！！
        //所以适配小米等个别机型，裁切后的图片，不能直接使用data返回，应使用uri指向
        //裁切后保存的URI，不属于我们向外共享的，所以可以使用fill://类型的URI
        Uri outputUri = Uri.fromFile(outputFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        // 若为false则表示不返回数据
        intent.putExtra("return-data", false);

        // 以广播方式刷新系统相册，以便能够在相册中找到刚刚所拍摄和裁剪的照片
        Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentBc.setData(uri);
        activity.sendBroadcast(intentBc);

        activity.startActivityForResult(intent, REQUEST_CODE_CAIQIE);
    }
}
