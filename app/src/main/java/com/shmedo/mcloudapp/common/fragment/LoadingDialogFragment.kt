package com.shmedo.mcloudapp.common.fragment

import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
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
            .cancelOnTouchOutside(false)
            .cornerRadius(10f)
            .customView(R.layout.layout_custom_progress_dialog_view)

        // 设置消息文本，可以通过参数传递
        dialog.getCustomView()?.findViewById<TextView>(R.id.loading_tips)?.text =
            arguments?.getString("message") ?: "请求网络中"

        return dialog
    }

    companion object {
        fun newInstance(message: String = "请求网络中"): LoadingDialogFragment {
            val fragment = LoadingDialogFragment()
            val bundle = Bundle()
            bundle.putString("message", message)
            fragment.arguments = bundle
            return fragment
        }
    }
}