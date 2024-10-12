package com.shmedo.mcloudapp.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.textview.MaterialTextView
import com.shmedo.mcloudapp.R

/**
 * 创建者：gonghe
 * 创建时间：2024/1/31
 * 描述： TODO
 */
class LoadingDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // 创建对话框实例
        val dialog = MaterialDialog(requireContext())
            .cancelable(true)
            .lifecycleOwner(this)
            .cancelable(true)
            .cancelOnTouchOutside(false)
            .maxWidth(R.dimen.dimen_size_200)
            .customView(R.layout.layout_custom_progress_dialog_view)

        dialog.view.setBackgroundResource(R.color.transparent)

        // 设置消息文本，可以通过参数传递
        dialog.getCustomView()
            .findViewById<MaterialTextView>(R.id.loading_tips)?.text =
            arguments?.getString("message") ?: "请求网络中"

        return dialog
    }

    fun updateMessage(message: String) {
        view?.findViewById<MaterialTextView>(R.id.loading_tips)?.text = message
    }

    override fun onDestroyView() {
        // 防止内存泄漏
        dialog?.setDismissMessage(null)
        super.onDestroyView()
    }

    companion object {
        const val TAG = "com.shmedo.mcloudapp.common.fragment.LoadingDialogFragment"
        fun newInstance(message: String = "请求网络中"): LoadingDialogFragment {
            return LoadingDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("message", message)
                }
            }
        }
    }
}