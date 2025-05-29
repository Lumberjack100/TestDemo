package com.shmedo.mcloudapp.ui.page.webview

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebChromeClient
import com.just.agentweb.WebViewClient
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentWebviewBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import timber.log.Timber

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
            .useMiddlewareWebClient(LocalContentWebViewClient(requireContext())) //设置WebViewClient中间件，支持多个WebViewClient， AgentWeb 3.0.0 加入。
            .setWebViewClient(mWebViewClient) // 添加 WebViewClient
            .setWebChromeClient(mWebChromeClient) // 添加 WebChromeClient
            .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK) //严格模式 Android 4.2.2 以下会放弃注入对象
            .createAgentWeb()//创建AgentWeb
            .ready()//设置 WebSettings
            .go(url) //WebView载入该url地址的页面并显示。

        // 确保对本地文件的访问权限，尤其是 file:///android_asset/ 路径下的 JS 通过 fetch/XHR 访问其他本地文件
        mAgentWeb.agentWebSettings.webSettings.apply {
            allowFileAccess = false//允许加载本地文件html  file协议
            allowContentAccess = false // 允许 WebView 使用 File协议
       }
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

    private val mWebViewClient: WebViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Timber.d("onPageStarted: $url")
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            Timber.d("onPageFinished: $url")
        }

        override fun onReceivedHttpError(
            view: WebView,
            request: WebResourceRequest,
            errorResponse: WebResourceResponse
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            Timber
                .e("onReceivedHttpError: ${errorResponse.statusCode} for URL: ${request.url}")
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
            Timber
                .e("onReceivedError: ${error.errorCode}, Description: ${error.description} for URL: ${request.url}")
        }
    }

    private val mWebChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)
            // 如果HTML本身有title且我们没有在Fragment参数中传递标题，或者希望HTML的title覆盖参数title
            // arguments?.getString(ARG_TITLE).isNullOrEmpty().let {
            // if (it) toolbarViewModel.toolbarTitleText.set(title)
            // }
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            Timber.tag("WebViewConsole")
                .d("${consoleMessage.message()} -- From line ${consoleMessage.lineNumber()} of ${consoleMessage.sourceId()}")
            return super.onConsoleMessage(consoleMessage)
        }
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