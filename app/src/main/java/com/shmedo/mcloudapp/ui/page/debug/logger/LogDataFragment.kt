package com.shmedo.mcloudapp.ui.page.debug.logger

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.Utils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.data.source.local.entity.LogItem
import com.shmedo.core.data.source.local.entity.LogLevel
import com.shmedo.core.data.source.local.entity.LogSession
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentLogDataBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.LogViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogDataFragment : BaseFragment() {
    private lateinit var binding: FragmentLogDataBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private lateinit var mStates: EmptyViewModel
    private lateinit var logViewModel: LogViewModel

    private var statusBarColor = 0
    private lateinit var sessionInfo: LogSession
    private var logLevel = Log.VERBOSE


    private val logLevelList by lazy { Utils.getApp().resources.getStringArray(R.array.log_levels) }


    override fun initViewModel() {
        mStates = getFragmentScopeViewModel()
        logViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_log_data, BR.vm, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentLogDataBinding
        //设置menu 关键代码
        mActivity.setSupportActionBar(binding.llToolbar.toolbar)
        addMenu()
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                nav().navigateUp()
            }
        })
        initAdapter()
    }

    private fun initAdapter() {
        binding.recyclerview.setup { rv ->
            addType<LogItem>(R.layout.item_common_log)
        }
    }

    override fun initData() {
        arguments?.let {
            sessionInfo = it.getParcelable(SESSION_INFO)!!
            statusBarColor =
                it.getInt(com.shmedo.core.commonlib.utils.AppContants.Extras.STATUS_BAR_COLOR)
        }
        binding.llToolbar.toolbar.title = sessionInfo.name
        binding.llToolbar.toolbar.subtitle = sessionInfo.key
        loadLogList()
    }

    private fun loadLogList() {
        launchWithViewLifecycle {
            logViewModel.getLogListBySessionId(
                sessionInfo.id,
                logLevel
            ).let { logList ->
                if (logList.isEmpty()) {
                    binding.stateLayout.showEmpty()
                } else {
                    binding.stateLayout.showContent()
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
                            logLevel = when (position) {
                                0 -> Log.VERBOSE
                                1 -> Log.DEBUG
                                2 -> Log.INFO
                                3 -> Log.WARN
                                4 -> Log.ERROR
                                else -> Log.VERBOSE
                            }
                            loadLogList()
                        }
                        setText(logLevelList[0], false)
                    }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_share -> {//分享
                        shareLogToFile()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**
     * 分享日志到文件
     */
    private fun shareLogToFile() {
        launchWithViewLifecycle {
            binding.recyclerview.models?.let { logList ->
                val logContent = StringBuilder()
                logList.forEach { logInfo ->
                    (logInfo as LogItem).apply {
                        logContent.append(LogLevel.getTag(logLevel))
                        logContent.append(" ")
                        logContent.append(createTime)
                        logContent.append(" ")
                        logContent.append(data)
                        logContent.append("\n")
                    }
                }

                // 创建文件并写入日志内容
                val fileName = "${getString(R.string.app_name)}_realtime_log_${
                    SimpleDateFormat(
                        "yyyyMMddHHmmss",
                        Locale.getDefault(Locale.Category.FORMAT)
                    ).format(
                        Date()
                    )
                }.txt"

                val file = File(Utils.getApp().cacheDir.path, fileName)
                if (FileIOUtils.writeFileFromString(file, logContent.toString())) {
                    // 分享文件
                    shareFile(file)
                }
            }
        }
    }

    /**
     * 分享文件
     */
    private fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(
            Utils.getApp(),
            "${Utils.getApp().packageName}.fileprovider",
            file
        )
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            //设置剪贴板数据以授予接收应用对URI的访问权限
            val clip = ClipData.newRawUri("", uri)
            clipData = clip
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "分享到"))
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        private const val SESSION_INFO = "session_info"
        fun newBundleArguments(
            sessionInfo: LogSession,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(SESSION_INFO, sessionInfo)
            putInt(
                com.shmedo.core.commonlib.utils.AppContants.Extras.STATUS_BAR_COLOR,
                statusBarColor
            )
        }
    }
}