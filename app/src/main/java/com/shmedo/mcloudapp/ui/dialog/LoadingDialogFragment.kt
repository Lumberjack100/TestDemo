package com.shmedo.mcloudapp.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.StringUtils
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentLoadingDialogBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.ui.viewmodel.state.LoadingDialogViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.LoadingState

/**
 * 创建者：gonghe
 * 创建时间：2024/10/18
 * 描述： TODO
 */
class LoadingDialogFragment : DialogFragment() {
    private var _binding: FragmentLoadingDialogBinding? = null
    private val binding get() = _binding!!

    val viewModel: LoadingDialogViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.TransparentLoadingDialog)
        isCancelable = true

        // 初始化 ViewModel 的状态，仅在第一次创建时设置
        if (savedInstanceState == null) {
            // 可以从 arguments 获取初始消息，默认为 "加载中..."
            val initialMessage =
                arguments?.getString(ARG_MESSAGE) ?: StringUtils.getString(R.string.loading)
            viewModel.showLoading(initialMessage)
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

    /**
     * 观察 ViewModel 的加载状态并更新 UI 或关闭对话框。
     */
    private fun observeViewModel() {
        launchWithViewLifecycle {
            viewModel.loadingState.collect { state ->
                when (state) {
                    is LoadingState.Visible -> updateMessage(state.message)
                    LoadingState.Hidden -> dismissAllowingStateLoss()
                }
            }
        }
    }

    private fun updateMessage(newMessage: String) {
        binding.tvMessage.text = newMessage
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val TAG = LoadingDialogFragment::class.java.simpleName
        private const val ARG_MESSAGE = "arg_message"

        /**
         * 创建 `LoadingDialogFragment` 的新实例。
         * @param message 初始消息，默认为 "加载中..."。
         * @return 一个新的 `LoadingDialogFragment` 实例。
         */
        fun newInstance(message: String = StringUtils.getString(R.string.loading_requesting_network)): LoadingDialogFragment {
            return LoadingDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MESSAGE, message)
                }
            }
        }
    }
}