package com.shmedo.mcloudapp.device

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.PageRefreshLayout
import com.drake.brv.utils.setup
import com.google.android.material.tabs.TabLayout
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
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
import com.shmedo.mcloudapp.device.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.NetDeviceListViewModel
import java.text.DecimalFormat

class NetDeviceListFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private val binding: FragmentNetDeviceListBinding by lazy { getBinding() as FragmentNetDeviceListBinding }
    private val mMessenger: PageMessenger by lazy { getAppViewModel() }
    private val mStates: NetDeviceListViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModels()
    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }

    private val activeBg: Int = R.drawable.bg_product_tab_checked
    private val normalBg: Int = R.drawable.bg_product_tab_normal
    private val activeColor: Int = ColorUtils.getColor(R.color.white)
    private val normalColor: Int =  ColorUtils.getColor(R.color.colorPrimary)
    private val activeSize: Float = 15f
    private val normalSize: Float = 15f

    private val productInfoList = mutableListOf<ProductInfo>()
    private var companyID = -100
    private var productID = -1

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_net_device_list, BR.stateVM, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
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

    private fun queryDeviceList() {
        deviceRequestViewModel.getDeviceList(
            companyID = userInfo.companyID,
            productID = productID,
            currentPage = binding.refreshLayout.index,
            pageSize = PAGE_SIZE,
            isHasListSuperInfoPermission = MmkvCacheUtil.isHasListSuperInfoPermission()
        )
    }

    inner class ClickProxy {
        fun onGoToSearch() {
            nav().navigate(
                R.id.action_mainFragment_to_deviceSearchFragment
            )
        }

        fun onShowSelectProductPopup() {
            val selectionPopupView = ProductSelectionPartShadowPopupView(requireContext())
            selectionPopupView.setData(productInfoList, binding.tabs.selectedTabPosition)
                .setSelectListener(object : ProductSelectionPartShadowPopupView.OnSelectListener {
                    override fun onSelect(productInfo: ProductInfo, position: Int) {
                        binding.tabs.getTabAt(position)?.select()
                    }
                })
            XPopup.Builder(context)
                .atView(binding.headLine)
                .isViewMode(true)
                .dismissOnBackPressed(false) // 按返回键是否关闭弹窗，默认为true
                .dismissOnTouchOutside(true)// 点击外部是否关闭弹窗，默认为true
                .enableDrag(false)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asCustom(selectionPopupView)
                .show()
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
                productInfoList.clear()
                productInfoList.addAll(tempList)
                initTabLayout()
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

    private fun updateTopView(rate: String) {
        val spannableString = SpannableString(rate)
        val absoluteSizeSpan = AbsoluteSizeSpan(18, true)
        spannableString.setSpan(
            absoluteSizeSpan,
            rate.indexOf("%"),
            spannableString.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.llStatistics.tvOnlineRate.text = spannableString
//        binding.tvOnlineRate.text = SpannedString(spannableString)
    }

    private fun initTabLayout() {
        val tabLayout = binding.tabs
        productInfoList.forEachIndexed { index, productInfo ->
            val tabView = LayoutInflater.from(requireContext())
                .inflate(R.layout.custom_tab_product, null)
            val textView = tabView.findViewById<TextView>(R.id.tabText)
            textView.text = productInfo.productName
            if (index == 0) { // 第一个为默认选中
                tabView.setBackgroundResource(activeBg)
                textView.textSize = activeSize
                textView.setTextColor(activeColor)
            } else {
                tabView.setBackgroundResource(normalBg)
                textView.textSize = normalSize
                textView.setTextColor(normalColor)
            }
            tabLayout.addTab(tabLayout.newTab().setCustomView(tabView))
        }
        tabLayout.addOnTabSelectedListener(this)
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        tab.customView?.let {
            it.setBackgroundResource(activeBg)
            val textView = it.findViewById<TextView>(R.id.tabText)
            textView.textSize = activeSize
            textView.setTextColor(activeColor)
            productID = productInfoList[tab.position].id
            binding.refreshLayout.showLoading()
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        tab.customView?.let {
            it.setBackgroundResource(normalBg)
            val textView = it.findViewById<TextView>(R.id.tabText)
            textView.textSize = normalSize
            textView.setTextColor(normalColor)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {}

    override fun onResume() {
        super.onResume()
        if (companyID != userInfo.companyID) {
            companyID = userInfo.companyID
            productID = -1
            deviceRequestViewModel.getDeviceStatByCompanyID(
                userInfo.companyID,
                MmkvCacheUtil.isHasListSuperInfoPermission()
            )
            deviceRequestViewModel.getProductList(userInfo.companyID)
            binding.refreshLayout.showLoading()
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
        fun newInstance() = NetDeviceListFragment()
    }

}