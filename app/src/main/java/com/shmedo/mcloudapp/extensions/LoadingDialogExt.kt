package com.shmedo.mcloudapp.extensions

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.StringUtils
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.utils.LoadingDialogManager

/**
 * 显示加载对话框。
 * @param message 要显示的消息，默认为 "请求网络中..."。
 */
fun FragmentActivity.showLoadingDialog(message: String = StringUtils.getString(R.string.loading_requesting_network)) {
    LoadingDialogManager.showLoading(message)
}

/**
 * 关闭加载对话框。
 */
fun FragmentActivity.dismissLoadingDialog() {
    LoadingDialogManager.dismissLoading()
}


/**
 * 显示加载对话框。
 * @param message 要显示的消息，默认为 "请求网络中..."。
 */
fun Fragment.showLoadingDialog(
    message: String = StringUtils.getString(R.string.loading_requesting_network),
    onCancel: (() -> Unit)? = null
) {
    LoadingDialogManager.showLoading(message, onCancel)
}

/**
 * 关闭加载对话框。
 */
fun Fragment.dismissLoadingDialog() {
    LoadingDialogManager.dismissLoading()
}

fun Fragment.showLoadingWithId(
    message: String = StringUtils.getString(R.string.loading_requesting_network),
    onCancel: (() -> Unit)? = null
): String {
    return LoadingDialogManager.showLoadingWithId(message, onCancel)
}

fun Fragment.dismissLoadingWithId(loadingId: String) {
    LoadingDialogManager.dismissLoadingWithId(loadingId)
}

fun Fragment.updateMessageWithId(loadingId: String, message: String) {
    LoadingDialogManager.updateMessageWithId(loadingId, message)
}

/**
 * 显示对话框。
 */
inline fun <reified T : DialogFragment> Fragment.showDialogFragment(
    tag: String,
    crossinline createFragment: () -> T
) {
    val dialogFragment = childFragmentManager.findFragmentByTag(tag) as? T
        ?: createFragment()

    if (!dialogFragment.isAdded) {
        dialogFragment.show(childFragmentManager, tag)
    }
}

/**
 * 关闭对话框。
 */
fun Fragment.dismissDialogFragment(tag: String) {
    (childFragmentManager.findFragmentByTag(tag) as? DialogFragment)?.dismissAllowingStateLoss()
}

