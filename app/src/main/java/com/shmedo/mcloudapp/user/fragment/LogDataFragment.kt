package com.shmedo.mcloudapp.user.fragment

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.blankj.utilcode.util.Utils
import com.drake.brv.PageRefreshLayout
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.model.LogInfo
import com.shmedo.lib.core.base.model.LogLevel
import com.shmedo.lib.core.base.model.SessionInfo
import com.shmedo.lib.core.base.viewmodel.LogViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.lib.core.ext.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.databinding.FragmentLogDataBinding

class LogDataFragment : BaseFragment() {
    private val binding: FragmentLogDataBinding by lazy { getBinding() as FragmentLogDataBinding }
    private val mStates: EmptyViewModel by viewModels()
    private val loginViewModel: LogViewModel by viewModels()

    private var statusBarColor = 0
    private lateinit var sessionInfo: SessionInfo

    private val logLevelList by lazy { Utils.getApp().resources.getStringArray(R.array.log_levels) }


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_log_data, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        //设置menu 关键代码
        mActivity.setSupportActionBar(binding.toolbar)
        addMenu()
        binding.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
                nav().navigateUp()
            }
        })
        initRefresh()
        initAdapter()
    }

    private fun initRefresh() {
        PageRefreshLayout.startIndex = 1
        binding.refreshLayout.setEnableRefresh(false)
        binding.refreshLayout.setEnableLoadMore(false)
    }

    private fun initAdapter() {
        binding.recyclerview.setup { rv ->
            addType<LogInfo>(R.layout.item_common_log)
        }
    }

    override fun initData() {
        arguments?.let {
            sessionInfo = it.getParcelable(SESSION_INFO)!!
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
        }
        binding.toolbar.title = sessionInfo.name
        binding.toolbar.subtitle = sessionInfo.key
        loadLogList()
    }

    private fun loadLogList(level: Int = LogLevel.DEBUG) {
        launchAndRepeatWithViewLifecycle {
            loginViewModel.getLogListBySessionId(
                sessionInfo.id,
                level
            )?.let { logList ->
                if (logList.isEmpty()) {
                    binding.refreshLayout.showEmpty()
                } else {
                    binding.refreshLayout.showContent()
                    binding.recyclerview.models = logList
                }
            }
        }
    }

    private fun addMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.log_menu, menu)
                val menuItem = menu.findItem(R.id.action_filter)
                menuItem.actionView?.findViewById<MaterialAutoCompleteTextView>(R.id.log_level)
                    ?.apply {
                        setAdapter(
                            ArrayAdapter.createFromResource(
                                requireContext(),
                                R.array.log_levels, R.layout.popup_levels_item
                            )
                        )
                        setOnItemClickListener { _, _, position, _ ->
                            loadLogList(position)
                        }
                        setText(logLevelList[0], false)
                    }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_share -> {
                        //分享
                        shareLog()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**
     * 分享日志
     */
    private fun shareLog() {
        launchAndRepeatWithViewLifecycle {
            binding.recyclerview.models?.let { logList ->
                val logContent = StringBuilder()
                logList.forEach { logInfo ->
                    (logInfo as LogInfo).apply {
                        logContent.append(LogLevel.getTag(logLevel))
                        logContent.append(" ")
                        logContent.append(createTime)
                        logContent.append(" ")
                        logContent.append(data)
                        logContent.append("\n")
                    }
                }
                startActivity(Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, logContent.toString())
                    type = "text/plain"
                }, "分享到"))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.toolbar)
    }

    companion object {
        private const val SESSION_INFO = "session_info"
        fun newBundleArguments(
            sessionInfo: SessionInfo,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(SESSION_INFO, sessionInfo)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}