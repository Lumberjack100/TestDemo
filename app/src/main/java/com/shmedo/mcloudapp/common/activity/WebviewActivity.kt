package com.shmedo.mcloudapp.common.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.viewModels
import com.just.agentweb.AgentWeb
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.databinding.ActivityWebviewBinding

class WebviewActivity : BaseActivity() {
    private val binding: ActivityWebviewBinding by lazy { getBinding() as ActivityWebviewBinding }
    private val mStates: EmptyViewModel by viewModels()

    private lateinit var mAgentWeb: AgentWeb

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_webview, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        setToolBar(binding.llToolbar.toolbar)
        if (intent.extras != null) {
            val title = intent.getStringExtra(ARG_TITLE)
            val url = intent.getStringExtra(ARG_URL)
            binding.llToolbar.toolbar.title = title
            mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(binding.container, LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .createAgentWeb()
                .ready()
                .go(url)
        }
        binding.llToolbar.toolbar.setNavigationOnClickListener {
            finish()
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

    override fun onDestroy() {
        super.onDestroy()
        mAgentWeb.webLifeCycle.onDestroy()
    }

    override fun onBackPressed() {
        if (mAgentWeb.webCreator.webView.canGoBack()) {
            mAgentWeb.webCreator.webView.goBack()
        } else super.onBackPressed()
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