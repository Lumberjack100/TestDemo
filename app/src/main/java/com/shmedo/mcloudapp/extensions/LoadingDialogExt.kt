package com.shmedo.mcloudapp.extensions

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
    val loadingDialog = childFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) as? LoadingDialogFragment
        ?: LoadingDialogFragment.newInstance(message)

    if (!loadingDialog.isAdded) {
        loadingDialog.show(childFragmentManager, LoadingDialogFragment.TAG)
    }
}
/**
 * 关闭加载对话框。
 */
fun Fragment.dismissLoadingDialog() {
    (childFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) as? LoadingDialogFragment)?.dismissAllowingStateLoss()
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




