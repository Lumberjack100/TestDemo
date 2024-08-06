package com.shmedo.mcloudapp.user.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import com.drake.brv.PageRefreshLayout
import com.drake.brv.listener.OnHoverAttachListener
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.model.SessionInfo
import com.shmedo.lib.core.base.viewmodel.LogViewModel
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.core.commonlib.utils.MmkvCacheUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.databinding.FragmentLogSessionListBinding
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.user.model.HoverHeaderModel
import org.koin.androidx.viewmodel.ext.android.getViewModel

class LogSessionListFragment : BaseFragment() {
    private lateinit var binding: FragmentLogSessionListBinding
    private lateinit var mStates: EmptyViewModel
    private lateinit var logViewModel: LogViewModel


    override fun initViewModel() {
        mStates = getFragmentScopeViewModel()
        logViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_log_session_list, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentLogSessionListBinding
        binding.llToolbar.toolbar.title = "应用日志"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
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
        binding.recyclerview.linear().setup { rv ->
            addType<HoverHeaderModel>(R.layout.item_log_session_group)
            addType<SessionInfo>(R.layout.item_log_session_item)
            R.id.item.onClick {
                when (itemViewType) {
                    R.layout.item_log_session_group -> Toaster.show("悬停条目")
                    else -> {
                        val item = getModel<SessionInfo>()
                        val bundle = LogDataFragment.newBundleArguments(item)
                        nav().navigate(
                            R.id.action_logSessionListFragment_to_logDataFragment,
                            bundle
                        )
                    }
                }
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

    inner class ClickProxy {

    }

    private fun loadLogSessionList() {
        launchWithViewLifecycle {
            logViewModel.getSessionListByUser(MmkvCacheUtil.getAccount())
                .let { logSessionList ->
                    if (logSessionList.isEmpty()) {
                        binding.refreshLayout.showEmpty()
                    } else {
                        binding.refreshLayout.showContent()
                        //将 createTime 转换为 yyyy-MM-dd，然后按照日期分组
                        val groupMap: Map<String, List<SessionInfo>> =
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