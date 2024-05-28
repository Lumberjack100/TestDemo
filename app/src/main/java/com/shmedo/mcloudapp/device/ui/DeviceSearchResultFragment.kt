package com.shmedo.mcloudapp.device.ui

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.PageRefreshLayout
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.base.model.UserInfo
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentDeviceSearchResultBinding
import com.shmedo.mcloudapp.device.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import org.koin.androidx.viewmodel.ext.android.getViewModel

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/31
 *
 * 描述： 设备搜索结果展示页面
 *
 *
 */
class DeviceSearchResultFragment : BaseFragment() {
    private lateinit var binding: FragmentDeviceSearchResultBinding
    private lateinit var mStates: EmptyViewModel
    private lateinit var deviceRequestViewModel: DeviceRequestViewModel
    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }

    private var statusBarColor = 0
    private lateinit var keyWord: String


    override fun initViewModel() {
        mStates = getFragmentScopeViewModel()
        deviceRequestViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_device_search_result, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDeviceSearchResultBinding
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        initDeviceInfoAdapter()
        initRefresh()
    }

    private fun initDeviceInfoAdapter() {
        binding.recyclerviewDevice.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    2,
                    ConvertUtils.dp2px(10f),
                    false
                )
            )
            addType<DeviceInfo>(R.layout.item_device_info)
            R.id.item.onClick {
                val deviceInfo = getModel<DeviceInfo>()
                DeviceHomeActivity.start(mActivity, deviceInfo)
            }
            R.id.item.onLongClick {
                val deviceInfo = getModel<DeviceInfo>()
                val dataList = if (deviceInfo.followTime.isNullOrEmpty()) arrayListOf(
                    "查看数据",
                    "收藏"
                ) else arrayListOf("查看数据", "取消收藏")

                val builder = XPopup.Builder(context)
                    .hasShadowBg(true)
                    .atView(itemView)
                builder.asAttachList(dataList.toTypedArray(), null)
                { _, text ->
                    when (text) {
                        "查看数据" -> {
                            QuickFunctionActivity.start(mActivity, deviceInfo)
                        }

                        "收藏" -> {
                            followDevice(deviceInfo.deviceToken)
                        }

                        "取消收藏" -> {
                            unFollowDevice(deviceInfo.deviceToken)
                        }
                    }
                }
                    .show()
                true
            }
        }
    }

    private fun initRefresh() {
        PageRefreshLayout.startIndex = 1
        binding.refreshLayout.onRefresh {
            queryDeviceList()
        }
    }

    override fun initData() {
        arguments?.let {
            keyWord = it.getString(AppContants.Extras.DEVICE_SEARCH_KEYWORD, "")
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
            binding.llToolbar.toolbar.title = keyWord
        }
    }

    override fun createObserver() {
        deviceRequestViewModel.deviceListResult.observe(viewLifecycleOwner) { listDataResult: DataResult<List<DeviceInfo>> ->
            if (!listDataResult.responseStatus.isSuccess) {
                Toaster.show(listDataResult.responseStatus.errorMessage)
                return@observe
            }
            if (listDataResult.result.isNullOrEmpty()) {
                binding.refreshLayout.showEmpty()
                return@observe
            }
            binding.refreshLayout.addData(listDataResult.result, hasMore = {
                binding.refreshLayout.index < listDataResult.totalPage
            })
        }
        deviceRequestViewModel.followDeviceResult.observe(viewLifecycleOwner) { dataResult: DataResult<String> ->
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            Toaster.show("已收藏")
        }
        deviceRequestViewModel.cancelFollowDeviceResult.observe(viewLifecycleOwner) { dataResult: DataResult<String> ->
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            Toaster.show("已取消收藏")
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.showLoading()
    }

    private fun queryDeviceList() {
        deviceRequestViewModel.getDeviceList(
            companyID = userInfo.companyID,
            deviceToken = keyWord,
            currentPage = binding.refreshLayout.index,
            pageSize = PAGE_SIZE,
            isHasListSuperInfoPermission = MmkvCacheUtil.isHasListSuperInfoPermission()
        )
    }

    private fun followDevice(deviceSn: String) {
        deviceRequestViewModel.addUserFollowDevice(deviceSn)
    }

    private fun unFollowDevice(deviceSn: String) {
        deviceRequestViewModel.cancelUserFollowDevice(deviceSn)
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        private const val PAGE_SIZE = 20
        fun newBundleArguments(
            keyWord: String,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putString(AppContants.Extras.DEVICE_SEARCH_KEYWORD, keyWord)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}