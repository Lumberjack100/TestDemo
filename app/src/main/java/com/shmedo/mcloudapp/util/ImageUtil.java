package com.shmedo.mcloudapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import com.shmedo.core.utils.StringUtil;
import com.shmedo.mcloudapp.R;

import org.jetbrains.annotations.NotNull;

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
 */

public class ImageUtil {

    public static void saveImageToFile(Bitmap bmp, String fileName) {
        if (bmp == null)
            return;

        if (TextUtils.isEmpty(fileName))
            return;

        File file = new File(fileName);
        try {
            if (!file.exists())
                file.createNewFile();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
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


    /**
     * 根据传感器类型获取对应的本地图片资源
     *
     * @author：gonghe
     * @time 2019-09-05
     */
    public static int getSensorResourceID(int type) {
        String sensorType = StringUtil.formatStringTwo(String.valueOf(type));
        switch (sensorType) {
            case "02":
                return R.drawable.medo_icon_sensortype_2;

            case "04":
                return R.drawable.medo_icon_sensortype_3;

            case "06":
                return R.drawable.medo_icon_sensortype_4;

            case "08":
                return R.drawable.medo_icon_sensortype_5;

            case "12":
                return R.drawable.medo_icon_sensortype_6;

            case "15":
                return R.drawable.medo_icon_sensortype_7;

            case "50":
                return R.drawable.medo_icon_sensortype_8;

            case "51":
                return R.drawable.medo_icon_sensortype_9;

            case "53":
                return R.drawable.medo_icon_sensortype_13;
        }

        return R.drawable.medo_icon_sensortype_0;
    }

    private static Bitmap getBitmap(@NotNull VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap getBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable) {
            return getBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }
}
