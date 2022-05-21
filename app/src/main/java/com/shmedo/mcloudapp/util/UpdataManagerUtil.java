package com.shmedo.mcloudapp.util;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;

import com.blankj.utilcode.util.CleanUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.Utils;
import com.hjq.toast.ToastUtils;
import com.pgyer.pgyersdk.PgyerSDKManager;
import com.pgyer.pgyersdk.callback.CheckoutCallBack;
import com.pgyer.pgyersdk.model.CheckSoftModel;
import com.shmedo.mcloudapp.R;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.entity.UpdateEntity;

import timber.log.Timber;

/**
 * 版本更新
 */
public class UpdataManagerUtil {

    /**
     * 检查新版本
     */
    public static void checkNewVersion(final Activity activity, boolean isShowToast) {
        PgyerSDKManager.checkVersionUpdate(activity, new CheckoutCallBack() {
            /**
             * CheckSoftModel 参数介绍
             *
             *     private int buildBuildVersion;//蒲公英生成的用于区分历史版本的build号
             *     private String forceUpdateVersion;//强制更新版本号（未设置强置更新默认为空）
             *     private String forceUpdateVersionNo;//强制更新的版本编号
             *     private boolean needForceUpdate;//	是否强制更新
             *     private boolean buildHaveNewVersion;//是否有新版本
             *     private String downloadURL;//应用安装地址
             *     private String buildVersionNo;//上传包的版本编号，默认为1 (即编译的版本号，一般来说，编译一次会
             *     变动一次这个版本号, 在 Android 上叫 Version Code。对于 iOS 来说，是字符串类型；对于 Android 来
             *     说是一个整数。例如：1001，28等。)
             *     private String buildVersion;//版本号, 默认为1.0 (是应用向用户宣传时候用到的标识，例如：1.1、8.2.1等。)
             *     private String buildShortcutUrl;//	应用短链接
             *     private String buildUpdateDescription;//	应用更新说明
             * @param model
             */
            @Override
            public void onNewVersionExist(CheckSoftModel model) {
                //检查版本成功（有新版本）
                UpdateEntity entity = new UpdateEntity()
                        .setHasUpdate(model.isBuildHaveNewVersion())
                        .setIsIgnorable(false)
                        .setVersionCode(Integer.parseInt(model.getBuildVersionNo()))
                        .setVersionName(model.getBuildVersion())
                        .setUpdateContent(model.getBuildUpdateDescription())
                        .setDownloadUrl(model.getDownloadURL());

                XUpdate.newBuild(activity)
                        .promptThemeColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.update_theme_color))
                        .promptButtonTextColor(Color.WHITE)
                        .promptTopResId(R.drawable.bg_update_top)
//                        .promptWidthRatio(0.7F)
                        .build()
                        .update(entity);
            }

            @Override
            public void onNonentityVersionExist(String string) {
                Timber.d("已是最新版本");
                //无新版本
                if (isShowToast)
                    ToastUtils.show("已是最新版本");

                String updatePath = Utils.getApp().getExternalCacheDir() + "/xupdate";
                boolean cleanResult = CleanUtils.cleanCustomDir(updatePath);
                Timber.d("清除结果：%s", cleanResult);
            }

            @Override
            public void onFail(String error) {
                Timber.w(error);
                //请求异常
                if (isShowToast && TextUtils.isEmpty(error))
                    ToastUtils.show(error);
            }
        });
    }

}
