package com.shmedo.mcloudapp.user.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.listener.OnHoverAttachListener
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.model.SessionInfo
import com.shmedo.lib.core.base.model.UserInfo
import com.shmedo.lib.core.base.viewmodel.LogViewModel
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.launchWithViewLifecycle
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.databinding.FragmentLogSessionListBinding
import com.shmedo.mcloudapp.user.model.HoverHeaderModel
import com.shmedo.mcloudapp.user.viewmodel.state.LogSessionListViewModel

class LogSessionListFragment : BaseFragment() {
    private val binding: FragmentLogSessionListBinding by lazy { getBinding() as FragmentLogSessionListBinding }
    private val mStates: LogSessionListViewModel by viewModels()
    private val loginViewModel: LogViewModel by viewModels()
    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_log_session_list, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
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
        initAdapter()
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
                        Toaster.show("普通条目")
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
//        loadLogSessionList()
    }


    override fun lazyLoadData() {
        binding.recyclerview.models = getTestData()
    }


    inner class ClickProxy {

    }

    private fun loadLogSessionList() {
        launchWithViewLifecycle {
            loginViewModel.getSessionListByUser(MmkvCacheUtil.getUserName())
                ?.let { logSessionList ->
                    //将 createTime 转换为 yyyy-MM-dd，然后按照日期分组
                    val groupMap: Map<String, List<SessionInfo>> =
                        logSessionList.groupBy { it.createDate }
                    val groupList = mutableListOf<Any>()
                    groupMap.forEach { (key, value) ->
                        groupList.add(HoverHeaderModel(key))
                        groupList.addAll(value)
                    }
                    binding.recyclerview.models = groupList
                }

        }
    }
    private fun getTestData(): List<Any> {
        return listOf(
            HoverHeaderModel("2021-08-10"),
            SessionInfo(
                key = "70:04:1D:8C:BB:9E",
                name = "MD-22T773A",
                createBy = MmkvCacheUtil.getUserName(),
                createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
                createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm")),
            ),
            SessionInfo(
                key = "70:04:1D:8C:BB:9E",
                name = "MD-22T773A",
                createBy = MmkvCacheUtil.getUserName(),
                createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
                createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm")),
            ),
            HoverHeaderModel("2021-08-09"),
            SessionInfo(
                key = "70:04:1D:8C:BB:9E",
                name = "MD-22T773A",
                createBy = MmkvCacheUtil.getUserName(),
                createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
                createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm")),
            ),
            SessionInfo(
                key = "70:04:1D:8C:BB:9E",
                name = "MD-22T773A",
                createBy = MmkvCacheUtil.getUserName(),
                createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
                createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm")),
            ),
            HoverHeaderModel("2021-08-08"),
            SessionInfo(
                key = "70:04:1D:8C:BB:9E",
                name = "MD-22T773A",
                createBy = MmkvCacheUtil.getUserName(),
                createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
                createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm")),
            ),
            SessionInfo(
                key = "70:04:1D:8C:BB:9E",
                name = "MD-22T773A",
                createBy = MmkvCacheUtil.getUserName(),
                createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
                createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm")),
            ),
            HoverHeaderModel("2021-08-07"),
            SessionInfo(
                key = "70:04:1D:8C:BB:9E",
                name = "MD-22T773A",
                createBy = MmkvCacheUtil.getUserName(),
                createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
                createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm")),
            ),
            SessionInfo(
                key = "70:04:1D:8C:BB:9E",
                name = "MD-22T773A",
                createBy = MmkvCacheUtil.getUserName(),
                createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
                createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm")),
            ),
            HoverHeaderModel("2021-08-06"),
            SessionInfo(
                key = "70:04:1D:8C:BB:9E",
                name = "MD-22T773A",
                createBy = MmkvCacheUtil.getUserName(),
                createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
                createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm")),
            ),
            SessionInfo(
                key = "70:04:1D:8C:BB:9E",
                name = "MD-22T773A",
                createBy = MmkvCacheUtil.getUserName(),
                createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
                createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm")),
            ),
            HoverHeaderModel("2021-08-05"),
            SessionInfo(
                key = "70:04:1D:8C:BB:9E",
                name = "MD-22T773A",
                createBy = MmkvCacheUtil.getUserName(),
                createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
                createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm")),
            ),
            SessionInfo(
                key = "70:04:1D:8C:BB:9E",
                name = "MD-22T773A",
                createBy = MmkvCacheUtil.getUserName(),
                createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
                createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm")),
            ),

        )
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

}