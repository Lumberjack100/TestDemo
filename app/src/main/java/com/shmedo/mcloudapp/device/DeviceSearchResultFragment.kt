package com.shmedo.mcloudapp.device

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.PageRefreshLayout
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.base.model.UserInfo
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.common.widget.recycleviewitemdivider.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentDeviceSearchResultBinding

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
    private val binding: FragmentDeviceSearchResultBinding by lazy { getBinding() as FragmentDeviceSearchResultBinding }
    private val mMessenger: PageMessenger by lazy { getAppViewModel() }
    private val mStates: EmptyViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModels()
    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }

    private var statusBarColor = 0
    private lateinit var keyWord: String

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_device_search_result, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
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
//                DeviceConfigActivity.startActivity(mActivity, deviceInfo)
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
            listDataResult.result?.let {
                binding.refreshLayout.addData(it, isEmpty = {
                    binding.refreshLayout.index == 1 && it.isEmpty()
                }, hasMore = {
                    binding.refreshLayout.index < listDataResult.totalPage
                })
            }
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
            pageSize = PAGE_SIZE
        )
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