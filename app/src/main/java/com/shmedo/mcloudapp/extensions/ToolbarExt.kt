package com.shmedo.mcloudapp.extensions

import android.text.Html
import android.text.Spanned
import androidx.appcompat.widget.Toolbar
import com.blankj.utilcode.util.Utils
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.utils.SettingUtil

/**
 * 创建者：gonghe
 * 创建时间：2024/1/31
 * 描述： TODO
 */

/**
 * 初始化普通的toolbar 只设置标题
 */
fun Toolbar.init(titleStr: String = ""): Toolbar {
    setBackgroundColor(SettingUtil.getColor(Utils.getApp()))
    title = titleStr
    return this
}

/**
 * 初始化有返回键的toolbar
 */
fun Toolbar.initClose(
    titleStr: String = "",
    backImg: Int = R.drawable.ic_navigation_white,
    onBack: (toolbar: Toolbar) -> Unit
): Toolbar {
    setBackgroundColor(SettingUtil.getColor(Utils.getApp()))
    title = titleStr.toHtml()
    setNavigationIcon(backImg)
    setNavigationOnClickListener { onBack.invoke(this) }
    return this
}

fun String.toHtml(flag: Int = Html.FROM_HTML_MODE_LEGACY): Spanned {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        Html.fromHtml(this, flag)
    } else {
        Html.fromHtml(this)
    }
}