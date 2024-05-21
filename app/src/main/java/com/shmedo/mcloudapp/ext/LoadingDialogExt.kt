package com.shmedo.mcloudapp.ext

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.shmedo.mcloudapp.common.fragment.LoadingDialogFragment


// 公共扩展函数：显示加载对话框
fun FragmentActivity.showLoadingDialog(message: String = "请求网络中") {
    val fragmentManager = supportFragmentManager
    if (fragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) == null) {
        LoadingDialogFragment.newInstance(message).showNow(fragmentManager, LoadingDialogFragment.TAG)
    }
}

fun FragmentActivity.dismissLoadingDialog() {
    supportFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG)?.let {
        (it as? DialogFragment)?.dismissAllowingStateLoss()
    }
}

// 公共扩展函数：显示加载对话框（用于Fragment）
fun Fragment.showLoadingDialog(message: String = "请求网络中") {
    if (isAdded && childFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) == null) {
        LoadingDialogFragment.newInstance(message).showNow(childFragmentManager, LoadingDialogFragment.TAG)
    }
}

fun Fragment.dismissLoadingDialog() {
    if (isAdded) {
        childFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG)?.let {
            (it as? DialogFragment)?.dismissAllowingStateLoss()
        }
    }
}


