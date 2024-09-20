package com.shmedo.mcloudapp.ui.page.webview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.viewModels
import com.just.agentweb.AgentWeb
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.ActivityWebviewBinding
import com.shmedo.mcloudapp.ui.page.base.activity.BaseActivity
import com.shmedo.mcloudapp.ui.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel

class WebviewActivity : BaseActivity() {
    private lateinit var binding: ActivityWebviewBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: EmptyViewModel by viewModels()

    private lateinit var mAgentWeb: AgentWeb


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_webview, BR.vm, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as ActivityWebviewBinding
        setToolBar(binding.llToolbar.toolbar)

        binding.llToolbar.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun initData() {
        if (intent.extras != null) {
            val title = intent.getStringExtra(ARG_TITLE)
            val url = intent.getStringExtra(ARG_URL)
            mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(binding.container, LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .createAgentWeb()
                .ready()
                .go(url)
        }
    }

    override fun onPause() {
        mAgentWeb.webLifeCycle.onPause()
        super.onPause()
    }

    override fun onResume() {
        mAgentWeb.webLifeCycle.onResume()
        super.onResume()
    }

    override fun onBackPressed() {
        if (mAgentWeb.webCreator.webView.canGoBack()) {
            mAgentWeb.webCreator.webView.goBack()
        } else super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        mAgentWeb.webLifeCycle.onDestroy()
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_URL = "arg_url"
        fun startActivity(context: Context, title: String, url: String) {
            val intent = Intent(context, WebviewActivity::class.java)
            intent.putExtra(ARG_TITLE, title)
            intent.putExtra(ARG_URL, url)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }
}