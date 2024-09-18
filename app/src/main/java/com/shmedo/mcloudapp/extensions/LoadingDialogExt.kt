package com.shmedo.mcloudapp.extensions

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.shmedo.mcloudapp.ui.dialog.LoadingDialogFragment


// 公共扩展函数：显示加载对话框
fun FragmentActivity.showLoadingDialog(message: String = "请求网络中") {
    val fragmentManager = supportFragmentManager
    val existingDialog = fragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) as? LoadingDialogFragment
    if (existingDialog != null) {
        existingDialog.updateMessage(message)
    } else {
        LoadingDialogFragment.newInstance(message)
            .showNow(fragmentManager, LoadingDialogFragment.TAG)
    }
}


// 公共扩展函数：显示加载对话框（用于Fragment）
fun Fragment.showLoadingDialog(message: String = "请求网络中") {
    if (isAdded) {
        val existingDialog = childFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) as? LoadingDialogFragment
        if (existingDialog != null) {
            existingDialog.updateMessage(message)
        } else {
            LoadingDialogFragment.newInstance(message)
                .showNow(childFragmentManager, LoadingDialogFragment.TAG)
        }
    }
}

fun FragmentActivity.dismissLoadingDialog() {
    supportFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG)?.let {
        (it as? DialogFragment)?.dismissAllowingStateLoss()
    }
}

fun Fragment.dismissLoadingDialog() {
    childFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG)?.let {
        (it as? DialogFragment)?.dismissAllowingStateLoss()
    }
}


