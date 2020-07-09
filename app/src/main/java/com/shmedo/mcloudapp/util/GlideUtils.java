package com.shmedo.mcloudapp.util;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.shmedo.mcloudapp.R;

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

    public static void loadImageFitCenter(Context context, final String strUrl, final ImageView imageView, @DrawableRes final int id) {
        Glide.with(context)
                .load(strUrl)
                .apply(new RequestOptions()
                        .fitCenter()
                        .error(id)
                        .placeholder(id)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .transition(new DrawableTransitionOptions().crossFade(400)) //使用变换效果
                .into(imageView);
    }

    /**
     * 加载有弧度的
     *
     * @param context
     * @param strUrl
     * @param imageView
     * @param radian
     * @param id
     */
    public static void loadRadianImage(Context context, final String strUrl, final ImageView imageView,
                                       float radian, @DrawableRes final int id) {
        Glide.with(context)
                .load(strUrl)
                .apply(new RequestOptions()
                        .centerCrop()
                        .transform(new GlideRadianTransform(radian))
                        .error(id)
                        .placeholder(id)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .transition(new DrawableTransitionOptions().crossFade(300)) //使用变换效果
                .into(imageView);
    }

    /**
     * 加载圆形的ImageView
     *
     * @param context
     * @param strUrl
     * @param imageView
     * @param id
     */
    public static void loadRoundImage(Context context, final String strUrl, final ImageView imageView,
                                      @DrawableRes final int id) {
        Glide.with(context)
                .load(strUrl)
                .apply(new RequestOptions()
                        .centerCrop()
                        .transform(new GlideRoundTransform())
                        .error(id)
                        .placeholder(id)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .transition(new DrawableTransitionOptions().crossFade(300)) //使用变换效果
                .into(imageView);
    }

    //加载drawable图片
    public static void loadResImage(Context context, int resId, ImageView imageView) {
        Glide.with(context)
                .load(resourceIdToUri(context, resId))
                .apply(new RequestOptions()
                        .centerCrop()
                        .error(R.color.white)
                        .placeholder(R.color.white)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .transition(new DrawableTransitionOptions().crossFade(300)) //使用变换效果
                .into(imageView);
    }

    // 将资源ID转为Uri
    private static Uri resourceIdToUri(Context context, int resourceId) {
        return Uri.parse(ANDROID_RESOURCE + context.getPackageName() + FOREWARD_SLASH + resourceId);
    }

    private static final String ANDROID_RESOURCE = "android.resource://";
    private static final String FOREWARD_SLASH = "/";
}
