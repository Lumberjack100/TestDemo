package com.shmedo.mcloudapp.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.shmedo.das.utils.StringUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 项目名：  das-config-app
 * 包名：    com.example.medoDas.Utils
 * 文件名:   ImageUtil
 * 创建者:   dpc
 * 创建时间:  2018/1/25 17:24
 * 描述：    TODO
 */

public class ImageUtil {


    public static void saveImageToFile(Bitmap img, String fileName) {
        if (img == null)
            return;
        if (StringUtil.isNullOrEmpty(fileName))
            return;
        File f = new File(fileName);
        try {
            if (!f.exists())
                f.createNewFile();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            //Log.e(ErrCode.ERROR_TAG, e.getMessage(), e);
        }
        img.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            //Log.e(ErrCode.ERROR_TAG, e.getMessage(), e);
        }
        try {
            fOut.close();
        } catch (IOException e) {
            //Log.e(ErrCode.ERROR_TAG, e.getMessage(), e);
        }
    }

    public static Bitmap getLocalImage(String fileName) {
        try {
            File f = new File(fileName);
            if (!f.exists())
                return null;
            else {
                FileInputStream fis = new FileInputStream(fileName);
                return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
}
