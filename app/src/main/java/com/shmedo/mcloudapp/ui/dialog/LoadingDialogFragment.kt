package com.shmedo.mcloudapp.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentLoadingDialogBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.ui.viewmodel.state.LoadingConfig
import com.shmedo.mcloudapp.ui.viewmodel.state.LoadingDialogState
import com.shmedo.mcloudapp.ui.viewmodel.state.LoadingDialogViewModel
import com.shmedo.mcloudapp.utils.LoadingDialogManager

/**
 * 创建者：gonghe
 * 创建时间：2024/10/18
 * 描述： TODO
 */
class LoadingDialogFragment : DialogFragment() {
    private var _binding: FragmentLoadingDialogBinding? = null
    private val binding get() = _binding!!

    val viewModel: LoadingDialogViewModel by viewModels()
    private var currentLoadingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.TransparentLoadingDialog)

        arguments?.let { args ->
            val config = LoadingConfig(
                message = args.getString(ARG_MESSAGE, ""),
                loadingId = args.getString(ARG_LOADING_ID, LoadingDialogState.GLOBAL_LOADING),
                isCancelable = args.getBoolean(ARG_CANCELABLE, true),
                timeoutDuration = args.getLong(ARG_TIMEOUT, LoadingDialogState.DEFAULT_TIMEOUT),
                onCancel = null  // Handled through LoadingDialogManager
            )
            currentLoadingId = config.loadingId
            this.isCancelable = config.isCancelable
            viewModel.showLoading(config)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoadingDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        launchWithViewLifecycle {
            viewModel.dialogState.collect { state ->
                when (state) {
                    is LoadingDialogState.Visible -> {
                        binding.tvMessage.text = state.message
                        dialog?.setCanceledOnTouchOutside(state.isCancelable)
                    }

                    LoadingDialogState.Hidden -> dismissAllowingStateLoss()
                }
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        currentLoadingId?.let { LoadingDialogManager.onDialogCanceled(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "LoadingDialogFragment"
        private const val ARG_MESSAGE = "arg_message"
        private const val ARG_LOADING_ID = "arg_loading_id"
        private const val ARG_CANCELABLE = "arg_cancelable"
        private const val ARG_TIMEOUT = "arg_timeout"

        fun newInstance(config: LoadingConfig) = LoadingDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MESSAGE, config.message)
                putString(ARG_LOADING_ID, config.loadingId)
                putBoolean(ARG_CANCELABLE, config.isCancelable)
                putLong(ARG_TIMEOUT, config.timeoutDuration)
            }
        }
    }
}