package com.shmedo.mcloudapp.device

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.SpannedString
import android.text.style.AbsoluteSizeSpan
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.PageRefreshLayout
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.base.model.DeviceStatisticInfo
import com.shmedo.lib.core.base.model.ProductInfo
import com.shmedo.lib.core.base.model.UserInfo
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentNetDeviceListBinding
import com.shmedo.mcloudapp.databinding.ItemProductBinding
import com.shmedo.mcloudapp.device.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.NetDeviceListViewModel
import java.text.DecimalFormat

class NetDeviceListFragment : BaseFragment() {
    private val binding: FragmentNetDeviceListBinding by lazy { getBinding() as FragmentNetDeviceListBinding }
    private val mMessenger: PageMessenger by lazy { getAppViewModel() }
    private val mStates: NetDeviceListViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModels()
    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }

    private var lastSelectedProductIndex = 0
    private var companyID = -100
    private var productID = -1

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_net_device_list, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        initProductAdapter()
        initDeviceInfoAdapter()
        initRefresh()
        binding.searchPlaceholder.llSearchPlaceholder.setOnClickListener {
            nav().navigate(
                R.id.action_mainFragment_to_deviceSearchFragment
            )
        }
    }

    private fun initProductAdapter() {
        binding.recyclerviewProduct.setup { rv ->
            rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            addType<ProductInfo>(R.layout.item_product)
            onBind {
                val productInfo = getModel<ProductInfo>()
                if (productInfo.isChecked) {
                    getBinding<ItemProductBinding>().tvName.setTextAppearance(R.style.Product_Tag_Checked_TitleStyle)
                } else {
                    getBinding<ItemProductBinding>().tvName.setTextAppearance(R.style.Product_Tag_UnChecked_TitleStyle)
                }
            }
            R.id.item.onClick {
                val productInfo = getModel<ProductInfo>()
                if (productInfo.isChecked) {
                    return@onClick
                }
                if (lastSelectedProductIndex != -1) {
                    getModel<ProductInfo>(lastSelectedProductIndex).isChecked = false
                    notifyItemChanged(lastSelectedProductIndex)
                }
                lastSelectedProductIndex = modelPosition
                productInfo.isChecked = true
                notifyItemChanged(modelPosition)
                val lastVisibleItemPosition =
                    (binding.recyclerviewProduct.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                //点击选中最后一个 Item 时,使RecyclerView滚动到底
                if (modelPosition == lastVisibleItemPosition) {
                    binding.recyclerviewProduct.scrollToPosition(lastVisibleItemPosition)
                }
                productID = productInfo.id
                binding.refreshLayout.showLoading()
            }
        }
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

    override fun createObserver() {
        deviceRequestViewModel.deviceStatisticInfoResult.observe(viewLifecycleOwner) { dataResult: DataResult<DeviceStatisticInfo> ->
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            dataResult.result?.let {
                mStates.onlineCount.set(it.onlineCount)
                mStates.offlineCount.set(it.offlineCount)
                val df = DecimalFormat("#.##") //格式化小数
                val rate: String = df.format(it.onlinePercent) + "%"
                updateTopView(rate)
            }
        }
        deviceRequestViewModel.productListResult.observe(viewLifecycleOwner) { dataResult: DataResult<List<ProductInfo>> ->
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            dataResult.result?.let { tempList ->
                binding.recyclerviewProduct.models = tempList
                binding.recyclerviewProduct.scrollToPosition(0)
            }
        }
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

    private fun queryDeviceList() {
        deviceRequestViewModel.getDeviceList(
            companyID = userInfo.companyID,
            productID = productID,
            currentPage = binding.refreshLayout.index,
            pageSize = PAGE_SIZE
        )
    }

    private fun updateTopView(rate: String) {
        val spannableString = SpannableString(rate)
        val absoluteSizeSpan = AbsoluteSizeSpan(18, true)
        spannableString.setSpan(
            absoluteSizeSpan,
            rate.indexOf("%"),
            spannableString.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvOnlineRate.text = SpannedString(spannableString)
    }

    override fun onResume() {
        super.onResume()
        if (companyID != userInfo.companyID) {
            companyID = userInfo.companyID
            productID = -1
            deviceRequestViewModel.getDeviceStatByCompanyID(userInfo.companyID)
            deviceRequestViewModel.getProductList(userInfo.companyID)
            binding.refreshLayout.showLoading()
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
        fun newInstance() = NetDeviceListFragment()
    }

}