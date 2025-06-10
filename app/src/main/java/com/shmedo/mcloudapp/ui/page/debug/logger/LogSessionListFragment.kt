package com.shmedo.mcloudapp.ui.page.debug.logger

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import com.drake.brv.PageRefreshLayout
import com.drake.brv.listener.OnHoverAttachListener
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.mmkv.MmkvCacheUtil
import com.shmedo.core.data.source.local.entity.LogSession
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentLogSessionListBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.HoverHeaderModel
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.LogViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.getViewModel
import timber.log.Timber

class LogSessionListFragment : BaseFragment() {
    private lateinit var binding: FragmentLogSessionListBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private lateinit var mStates: EmptyViewModel
    private lateinit var logViewModel: LogViewModel


    override fun initViewModel() {
        mStates = getFragmentScopeViewModel()
        logViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_log_session_list, BR.vm, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentLogSessionListBinding
        binding.llToolbar.toolbar.title = "应用日志"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
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
        binding.recyclerview.linear().setup { rv ->
            addType<HoverHeaderModel>(R.layout.item_log_session_group)
            addType<LogSession>(R.layout.item_log_session_item)
            R.id.item.onClick {
                when (itemViewType) {
                    R.layout.item_log_session_group -> Timber.d("悬停条目")
                    else -> {
                        val item = getModel<LogSession>()
                        val bundle = LogDataFragment.newBundleArguments(item)
                        nav().safeNavigate(
                            R.id.action_logSessionListFragment_to_logDataFragment,
                            bundle
                        )
                    }
                }
            }
            R.id.item.onLongClick {
                when (itemViewType) {
                    R.layout.item_log_session_group -> Timber.d("悬停条目")
                    else -> {
                        val item = getModel<LogSession>()
                        val dataList = arrayListOf("删除")
                        val builder = XPopup.Builder(context)
                            .hasShadowBg(true)
                            .atView(itemView)
                        builder.asAttachList(dataList.toTypedArray(), null)
                        { _, text ->
                            when (text) {
                                "删除" -> {
                                    launchWithViewLifecycle {
                                        logViewModel.deleteLogSessionById(item.id)
                                        delay(500)
                                        loadLogSessionList() // Refresh the list after deletion
                                    }
                                }
                            }
                        }
                            .show()
                    }
                }
                true
            }

            // 可选项, 粘性监听器
            onHoverAttachListener = object : OnHoverAttachListener {
                override fun attachHover(v: View) {
                    ViewCompat.setElevation(v, 10F) // 悬停时显示阴影
                }

                override fun detachHover(v: View) {
                    ViewCompat.setElevation(v, 0F) // 非悬停时隐藏阴影
                }
            }
        }
    }

    override fun initData() {
        loadLogSessionList()
    }

    private fun loadLogSessionList() {
        launchWithViewLifecycle {
            logViewModel.getLogSessionList(MmkvCacheUtil.getAccount())
                .let { logSessionList ->
                    if (logSessionList.isEmpty()) {
                        binding.refreshLayout.showEmpty()
                    } else {
                        binding.refreshLayout.showContent()
                        //将 createTime 转换为 yyyy-MM-dd，然后按照日期分组
                        val groupMap: Map<String, List<LogSession>> =
                            logSessionList.reversed().groupBy { it.createDate }
                        val groupList = mutableListOf<Any>()
                        groupMap.forEach { (key, value) ->
                            groupList.add(HoverHeaderModel(key))
                            groupList.addAll(value)
                        }
                        binding.recyclerview.models = groupList
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}