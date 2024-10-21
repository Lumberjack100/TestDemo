package com.shmedo.mcloudapp.extensions

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.dialog.LoadingDialogFragment

/**
 * 显示加载对话框。
 * @param message 要显示的消息，默认为 "请求网络中..."。
 */
fun FragmentActivity.showLoadingDialog(message: String = this.getString(R.string.loading_requesting_network)) {
    val loadingDialog =
        supportFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) as? LoadingDialogFragment
            ?: LoadingDialogFragment.newInstance(message)

    if (!loadingDialog.isAdded) {
        loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
    }
}

/**
 * 关闭加载对话框。
 */
fun FragmentActivity.dismissLoadingDialog() {
    (supportFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) as? LoadingDialogFragment)?.dismissAllowingStateLoss()
}

/**
 * 更新加载对话框的消息。
 * @param message 新的消息。
 */
fun FragmentActivity.updateLoadingMessage(message: String) {
    (supportFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) as? LoadingDialogFragment)?.apply {
        if (isAdded && isVisible) {
            viewModel.updateMessage(message)
        }
    }
}


/**
 * 显示加载对话框。
 * @param message 要显示的消息，默认为 "请求网络中..."。
 */
fun Fragment.showLoadingDialog(message: String = getString(R.string.loading_requesting_network)) {
    showDialogFragment(LoadingDialogFragment.TAG) {
        LoadingDialogFragment.newInstance(message)
    }
}

/**
 * 更新加载对话框的消息。
 * @param message 新的消息。
 */
fun Fragment.updateLoadingMessage(message: String) {
    (childFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) as? LoadingDialogFragment)?.apply {
        if (isAdded && isVisible) {
            viewModel.updateMessage(message)
        }
    }
}

/**
 * 关闭加载对话框。
 */
fun Fragment.dismissLoadingDialog() {
    dismissDialogFragment(LoadingDialogFragment.TAG)
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

