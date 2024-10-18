package com.shmedo.mcloudapp.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.shmedo.mcloudapp.ui.dialog.LoadingDialogFragment

fun FragmentActivity.showLoadingDialog(message: String = "请求网络中") {
    val loadingDialog = supportFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) as? LoadingDialogFragment
        ?: LoadingDialogFragment.newInstance(message)

    if (!loadingDialog.isAdded) {
        loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
    } else {
        loadingDialog.updateMessage(message)
    }
}

fun FragmentActivity.dismissLoadingDialog() {
    val loadingDialog = supportFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) as? LoadingDialogFragment
    loadingDialog?.dismissAllowingStateLoss()
}

fun FragmentActivity.updateLoadingMessage(message: String) {
    val loadingDialog = supportFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) as? LoadingDialogFragment
    loadingDialog?.updateMessage(message)
}

fun Fragment.showLoadingDialog(message: String = "请求网络中") {
    val loadingDialog = childFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) as? LoadingDialogFragment
        ?: LoadingDialogFragment.newInstance(message)

    if (!loadingDialog.isAdded) {
        loadingDialog.show(childFragmentManager, LoadingDialogFragment.TAG)
    } else {
        loadingDialog.updateMessage(message)
    }
}

fun Fragment.dismissLoadingDialog() {
    val loadingDialog = childFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) as? LoadingDialogFragment
    loadingDialog?.dismiss()
}

fun Fragment.updateLoadingMessage(message: String) {
    val loadingDialog = childFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) as? LoadingDialogFragment
    loadingDialog?.updateMessage(message)
}


