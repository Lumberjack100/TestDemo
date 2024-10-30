package com.shmedo.mcloudapp.extensions

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.viewmodel.state.LoadingDialogState
import com.shmedo.mcloudapp.utils.LoadingDialogManager


fun FragmentActivity.showLoadingDialog(
    message: String = getString(R.string.loading),
    isCancelable: Boolean = true,
    timeout: Long = LoadingDialogState.DEFAULT_TIMEOUT,
    onCancel: (() -> Unit)? = null
): String = LoadingDialogManager.showLoading(
    message = message,
    isCancelable = isCancelable,
    timeout = timeout,
    onCancel = onCancel
)

fun FragmentActivity.dismissLoadingDialog(loadingId: String = LoadingDialogState.GLOBAL_LOADING) {
    LoadingDialogManager.dismissLoading(loadingId)
}

fun FragmentActivity.updateLoadingMessage(loadingId: String, message: String) {
    LoadingDialogManager.updateMessage(loadingId, message)
}


fun Fragment.showLoadingDialog(
    message: String = getString(R.string.loading),
    isCancelable: Boolean = true,
    timeout: Long = LoadingDialogState.DEFAULT_TIMEOUT,
    onCancel: (() -> Unit)? = null
): String = LoadingDialogManager.showLoading(
    message = message,
    isCancelable = isCancelable,
    loadingId = LoadingDialogState.GLOBAL_LOADING,
    timeout = timeout,
    onCancel = onCancel
)

fun Fragment.showLoadingWithUUID(
    message: String = getString(R.string.loading),
    isCancelable: Boolean = true,
    timeout: Long = LoadingDialogState.DEFAULT_TIMEOUT,
    onCancel: (() -> Unit)? = null
): String = LoadingDialogManager.showLoading(
    message = message,
    isCancelable = isCancelable,
    loadingId = null,
    timeout = timeout,
    onCancel = onCancel
)

fun Fragment.dismissLoadingDialog(loadingId: String = LoadingDialogState.GLOBAL_LOADING) {
    LoadingDialogManager.dismissLoading(loadingId)
}

fun Fragment.updateLoadingMessage(loadingId: String, message: String) {
    LoadingDialogManager.updateMessage(loadingId, message)
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
