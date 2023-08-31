package com.shmedo.mcloudapp.device

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.SpannedString
import android.text.style.AbsoluteSizeSpan
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ConvertUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.base.model.DeviceStatisticInfo
import com.shmedo.lib.core.base.model.PageInfo
import com.shmedo.lib.core.base.model.ProductInfo
import com.shmedo.lib.core.base.model.UserInfo
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.adapter.DeviceInfoAdapter
import com.shmedo.mcloudapp.common.adapter.ProductAdapter
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.common.viewmodel.state.NetDeviceListViewModel
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.common.widget.recycleviewitemdivider.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentNetDeviceListBinding
import java.text.DecimalFormat

class NetDeviceListFragment : BaseFragment() {
    private val binding: FragmentNetDeviceListBinding by lazy { getBinding() as FragmentNetDeviceListBinding }
    private val mMessenger: PageMessenger by lazy { getAppViewModel() }
    private val mStates: NetDeviceListViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModels()
    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }

    private val pageInfo = PageInfo(1)
    private val mProductAdapter: ProductAdapter by lazy { ProductAdapter() }
    private val mDeviceInfoAdapter: DeviceInfoAdapter by lazy { DeviceInfoAdapter() }
    private val deviceInfoList: MutableList<DeviceInfo> = ArrayList()
    private var lastSelectedProductIndex = -1
    private var companyID = -100
    private var productID = -1

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_net_device_list, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        binding.refreshLayout.setOnRefreshListener {
            //下拉刷新，需要重置页数
            pageInfo.reset()
            deviceInfoList.clear()
            queryDeviceList()
        }
        binding.refreshLayout.setOnLoadMoreListener {
            pageInfo.nextPage()
            queryDeviceList()
        }
        initProductAdapter()
        initDeviceInfoAdapter()
    }

    private fun initProductAdapter() {
        binding.recyclerviewProduct.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = mProductAdapter
            mProductAdapter.setOnItemClickListener { _, _, position ->
                val productInfo = mProductAdapter.data[position]
                if (productInfo.isChecked) {
                    return@setOnItemClickListener
                }
                if (lastSelectedProductIndex != -1) {
                    mProductAdapter.data[lastSelectedProductIndex].isChecked = false
                    mProductAdapter.notifyItemChanged(lastSelectedProductIndex)
                }
                productInfo.isChecked = true
                mProductAdapter.notifyItemChanged(position)
                val lastVisibleItemPosition =
                    (binding.recyclerviewProduct.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                //点击选中最后一个 Item 时,使RecyclerView滚动到底
                if (position == lastVisibleItemPosition) {
                    binding.recyclerviewProduct.scrollToPosition(lastVisibleItemPosition)
                }
                productID = productInfo.id
                binding.refreshLayout.autoRefresh()
            }
        }
    }

    private fun initDeviceInfoAdapter() {
        val spanCount = 2//跟布局里面的spanCount属性是一致的
        val spacing = ConvertUtils.dp2px(10f) //每一个矩形的间距
        binding.recyclerviewDevice.apply {
            layoutManager = GridLayoutManager(context, spanCount)
            addItemDecoration(MyGridSpacingItemDecoration(spanCount, spacing, false))
            adapter = mDeviceInfoAdapter
            mDeviceInfoAdapter.setOnItemClickListener { _, _, position ->
                val deviceInfo = mDeviceInfoAdapter.data[position]
//                DeviceConfigActivity.startActivity(mActivity, deviceInfo)
            }
        }
    }

    override fun createObserver() {
        deviceRequestViewModel.deviceStatisticInfoResult.observe(this) { dataResult: DataResult<DeviceStatisticInfo> ->
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
        deviceRequestViewModel.productListResult.observe(this) { dataResult: DataResult<List<ProductInfo>> ->
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            dataResult.result?.let { tempList ->
                mProductAdapter.data.clear()
                mProductAdapter.data.addAll(tempList)
                mProductAdapter.notifyDataSetChanged()
                binding.recyclerviewProduct.scrollToPosition(0)
            }
        }
        deviceRequestViewModel.deviceListResult.observe(viewLifecycleOwner) { listDataResult: DataResult<List<DeviceInfo>> ->
            if (!listDataResult.responseStatus.isSuccess) {
                Toaster.show(listDataResult.responseStatus.errorMessage)
                updateResult()
                binding.refreshLayout.finishLoadMore(false)
                return@observe
            }
            listDataResult.result?.let {
                deviceInfoList.addAll(it)
            }
            updateResult()
            if (pageInfo.page == listDataResult.totalPage) {
                binding.refreshLayout.finishRefreshWithNoMoreData()
            } else {
                if (pageInfo.isFirstPage()) binding.refreshLayout.finishRefresh()
                else binding.refreshLayout.finishLoadMore()
            }
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryDeviceList() {
        deviceRequestViewModel.getDeviceList(
            userInfo.companyID,
            productID,
            pageInfo.page,
            PAGE_SIZE
        )
    }

    private fun updateResult() {
        mDeviceInfoAdapter.data.clear()
        if (deviceInfoList.isEmpty()) {
            mDeviceInfoAdapter.notifyDataSetChanged()
            binding.stateView.showEmpty()
            return
        }
        mDeviceInfoAdapter.data.addAll(deviceInfoList)
        mDeviceInfoAdapter.notifyDataSetChanged()
        binding.stateView.showContent()
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
            binding.refreshLayout.autoRefresh()
        }
    }

    inner class ClickProxy {

    }

    companion object {
        private const val PAGE_SIZE = 30
        fun newInstance() = NetDeviceListFragment()
    }

}