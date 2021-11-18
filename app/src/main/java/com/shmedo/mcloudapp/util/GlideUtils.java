package com.shmedo.mcloudapp.util;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

/**
 * 项目名：  das-config-app
 * 包名：    com.example.medoDas.Utils
 * 文件名:   GlideUtils
 * 创建者:   dpc
 * 创建时间:  2018/2/27 09:10
 */

public class GlideUtils {
    /**
     * Don't let anyone instantiate this class.
     */
    private GlideUtils() {
        throw new Error("Do not need instantiate!");
    }

    /**
     * 加载普通的图片
     *
     * @param context
     * @param strUrl
     * @param imageView
     */
    public static void loadImage(Context context, final String strUrl, final ImageView imageView, @DrawableRes final int id) {
        Glide.with(context)
                .load(strUrl)
                .apply(new RequestOptions()
                        .centerCrop()
                        .error(id)
                        .placeholder(id)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(imageView);
    }

    /**
     * 加载普通的图片
     *
     * @param context
     * @param strUrl
     * @param imageView
     */
    public static void loadImage(Context context, final String strUrl, final ImageView imageView, @DrawableRes final int errorResId, @DrawableRes final int holdResId) {
        Glide.with(context)
                .load(strUrl)
                .apply(new RequestOptions()
                        .centerCrop()
                        .error(errorResId)
                        .placeholder(holdResId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(imageView);
    }
}
