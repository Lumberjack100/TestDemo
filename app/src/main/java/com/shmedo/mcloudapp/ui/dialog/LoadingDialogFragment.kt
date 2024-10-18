package com.shmedo.mcloudapp.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.shmedo.mcloudapp.databinding.FragmentLoadingDialogBinding

/**
 * 创建者：gonghe
 * 创建时间：2024/10/18
 * 描述： TODO
 */
class LoadingDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentLoadingDialogBinding
    private var message: String = "Loading..."

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoadingDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateMessage(message)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    fun updateMessage(newMessage: String) {
        message = newMessage
        if (::binding.isInitialized) {
            binding.tvMessage.text = message
        }
    }

    companion object {
        const val TAG = "LoadingDialogFragment"
        fun newInstance(message: String = "Loading..."): LoadingDialogFragment {
            return LoadingDialogFragment().apply {
                this.message = message
            }
        }
    }
}