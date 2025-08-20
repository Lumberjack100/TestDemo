package com.shmedo.mcloudapp.extensions

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.dialog.LoadingDialogConfig
import com.shmedo.mcloudapp.utils.LoadingDialogManager

// ========== FragmentActivity 扩展 ==========

fun FragmentActivity.showLoadingDialog(
    message: String = getString(R.string.loading),
    isCancelable: Boolean = true,
    timeout: Long = LoadingDialogConfig.DEFAULT_TIMEOUT,
    onCancel: (() -> Unit)? = null
): String = LoadingDialogManager.showLoading(
    message = message,
    isCancelable = isCancelable,
    loadingId = LoadingDialogConfig.DEFAULT_LOADING_ID,
    timeout = timeout,
    onCancel = onCancel
)

fun FragmentActivity.dismissLoadingDialog(
    loadingId: String = LoadingDialogConfig.DEFAULT_LOADING_ID
) = LoadingDialogManager.dismissLoading(loadingId)


// ========== Fragment 扩展 ==========

fun Fragment.showLoadingDialog(
    message: String = getString(R.string.loading),
    isCancelable: Boolean = true,
    timeout: Long = LoadingDialogConfig.DEFAULT_TIMEOUT,
    onCancel: (() -> Unit)? = null
): String = LoadingDialogManager.showLoading(
    message = message,
    isCancelable = isCancelable,
    loadingId = LoadingDialogConfig.DEFAULT_LOADING_ID,
    timeout = timeout,
    onCancel = onCancel
)

fun Fragment.showLoadingWithUUID(
    message: String = getString(R.string.loading),
    isCancelable: Boolean = true,
    timeout: Long = LoadingDialogConfig.DEFAULT_TIMEOUT,
    onCancel: (() -> Unit)? = null
): String = LoadingDialogManager.showLoading(
    message = message,
    isCancelable = isCancelable,
    loadingId = null, // 自动生成UUID
    timeout = timeout,
    onCancel = onCancel
)

fun Fragment.dismissLoadingDialog(
    loadingId: String = LoadingDialogConfig.DEFAULT_LOADING_ID
) = LoadingDialogManager.dismissLoading(loadingId)

fun Fragment.updateLoadingMessage(loadingId: String, message: String) =
    LoadingDialogManager.updateMessage(loadingId, message)


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
