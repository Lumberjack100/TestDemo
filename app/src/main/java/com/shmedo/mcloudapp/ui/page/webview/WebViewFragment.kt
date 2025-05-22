package com.shmedo.mcloudapp.ui.page.webview

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.just.agentweb.AgentWeb
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentWebviewBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel

/**
 * 通用的WebView Fragment，可以加载网页、本地HTML或Markdown文件
 */
class WebViewFragment : BaseFragment() {
    private lateinit var binding: FragmentWebviewBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private lateinit var mAgentWeb: AgentWeb


    override fun initViewModel() {
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_webview, BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentWebviewBinding
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            if (mAgentWeb.webCreator.webView.canGoBack()) {
                mAgentWeb.webCreator.webView.goBack()
            } else {
                nav().navigateUp()
            }
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mAgentWeb.webCreator.webView.canGoBack()) {
                    mAgentWeb.webCreator.webView.goBack()
                } else {
                    nav().navigateUp()
                }
            }
        })
    }

    override fun initData() {
        val title = arguments?.getString(ARG_TITLE) ?: ""
        toolbarViewModel.toolbarTitleText.set(title)

        val url = arguments?.getString(ARG_URL) ?: ""
        mAgentWeb = AgentWeb.with(this)
            .setAgentWebParent(binding.container, LinearLayout.LayoutParams(-1, -1))//传入AgentWeb的父控件
            .useDefaultIndicator()//设置进度条颜色与高度
            .createAgentWeb()//创建AgentWeb
            .ready()//设置 WebSettings
            .go(url) //WebView载入该url地址的页面并显示。
    }

    override fun onResume() {
        mAgentWeb.webLifeCycle.onResume()
        super.onResume()

        // 设置状态栏
        initImmersionBar(requireView().findViewById(R.id.ll_toolbar))
    }

    override fun onPause() {
        mAgentWeb.webLifeCycle.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        mAgentWeb.webLifeCycle.onDestroy()
        super.onDestroyView()
    }

    inner class ClickProxy : BaseClickProxy() {

    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_URL = "arg_url"

        fun newBundleArguments(
            title: String, url: String
        ): Bundle = Bundle().apply {
            putString(ARG_TITLE, title)
            putString(ARG_URL, url)
        }
    }
} 