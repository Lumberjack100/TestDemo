package com.shmedo.mcloudapp.device.ui

import android.os.Bundle
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.PageRefreshLayout
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.base.model.UserInfo
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentNetProductDeviceListBinding
import com.shmedo.mcloudapp.device.viewmodel.request.DeviceRequestViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel

@Deprecated("This class is deprecated")
class NetProductDeviceListFragment : BaseFragment() {
    private lateinit var binding: FragmentNetProductDeviceListBinding
    private lateinit var mStates: EmptyViewModel
    private lateinit var deviceRequestViewModel: DeviceRequestViewModel
    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }

    private var productID = -1

    override fun initViewModel() {
        mStates = getFragmentScopeViewModel()
        deviceRequestViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_net_product_device_list, BR.stateVM, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentNetProductDeviceListBinding
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
            productID = it.getInt(TAB_PRODUCT_ID, -1)
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
            productID = productID.toString(),
            currentPage = binding.refreshLayout.index,
            pageSize = PAGE_SIZE,
            isHasListSuperInfoPermission = MmkvCacheUtil.isHasListSuperInfoPermission(),
            onlineStatus = ""
        )
    }

    companion object {
        private const val PAGE_SIZE = 20
        const val TAB_PRODUCT_ID = "tab_product_id"
        fun newInstance() = NetProductDeviceListFragment()
    }
}