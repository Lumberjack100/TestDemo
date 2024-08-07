package com.shmedo.mcloudapp.ui.viewmodel.request

import android.graphics.Color
import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.CleanUtils
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.shmedo.core.data.repository.AppUpdateRepositoryImp
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.core.model.CheckSoftModel
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BuildConfig
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
import com.xuexiang.xupdate.XUpdate
import com.xuexiang.xupdate.entity.UpdateEntity
import com.xuexiang.xupdate.utils.UpdateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/30
 *
 * 描述： TODO
 *
 *
 */
class AppUpdateViewModel(
    private val appUpdateRepositoryImp: AppUpdateRepositoryImp,
    private val loggerRepositoryImp: LoggerRepositoryImp
) :
    BaseRequestViewModel(loggerRepositoryImp) {

    fun requestCheckAppVersion(isShowToast: Boolean = false) {
        viewModelScope.launch(Dispatchers.Default) {
            val model: CheckSoftModel =
                appUpdateRepositoryImp.checkAppVersion(
                    BuildConfig.PGY_API_KEY,
                    BuildConfig.PGY_APP_KEY
                ) { error: Throwable ->
                    Timber.d("检查版本失败：${error.errorMsg}")
                    if (isShowToast && !TextUtils.isEmpty(error.errorMsg))
                        Toaster.show(error.errorMsg)

                } ?: return@launch
            if (!model.buildHaveNewVersion) {
                Timber.d("已是最新版本")
                //无新版本
                if (isShowToast)
                    Toaster.show("已是最新版本")
                val cleanResult =
                    CleanUtils.cleanCustomDir(UpdateUtils.getDefaultDiskCacheDirPath())
                Timber.d("清除结果：$cleanResult")
                return@launch
            }

            //检查版本成功（有新版本）
            val entity = UpdateEntity().apply {
                //设置是否有新版本
                setHasUpdate(true)
                //设置是否强制更新
                setForce(model.needForceUpdate)
                //设置是否可忽略
                setIsIgnorable(false)
                //设置版本号
                setVersionCode(model.buildVersionNo.toInt())
                //设置版本名称
                setVersionName(model.buildVersion)
                //设置更新内容
                setUpdateContent(model.buildUpdateDescription)
                //设置下载地址
                setDownloadUrl(model.downloadURL)
                //设置apk缓存目录
                setApkCacheDir(UpdateUtils.getDefaultDiskCacheDirPath())
            }

            XUpdate.newBuild(Utils.getApp())
                .promptThemeColor(ColorUtils.getColor(R.color.update_theme_color))
                .promptButtonTextColor(Color.WHITE)
                .promptTopResId(R.drawable.bg_update_top)
                .build()
                .update(entity)
        }
    }
}