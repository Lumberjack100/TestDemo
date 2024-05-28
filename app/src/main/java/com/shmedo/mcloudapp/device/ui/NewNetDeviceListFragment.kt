package com.shmedo.mcloudapp.device.ui

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.PageRefreshLayout
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.base.model.DeviceStatisticInfo
import com.shmedo.lib.core.base.model.UserInfo
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.common.widget.recyclerview.RecycleViewDivider
import com.shmedo.mcloudapp.databinding.FragmentNewNetDeviceListBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.FilterDeviceTabItem
import com.shmedo.mcloudapp.device.model.SingleSelectionItem
import com.shmedo.mcloudapp.device.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.NetDeviceListViewModel
import com.shmedo.mcloudapp.ext.nav
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.text.DecimalFormat

/**
 * 创建者：gonghe
 * 创建时间：2024/5/15
 * 描述： TODO
 */
class NewNetDeviceListFragment : BaseFragment() {
    private lateinit var binding: FragmentNewNetDeviceListBinding
    private lateinit var mStates: NetDeviceListViewModel
    private lateinit var deviceRequestViewModel: DeviceRequestViewModel
    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }

    private val productTabList = mutableListOf<SingleSelectionItem>()
    private val onlineStatusList = mutableListOf<SingleSelectionItem>()


    override fun initViewModel() {
        mStates = getFragmentScopeViewModel()
        deviceRequestViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_new_net_device_list, BR.stateVM, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentNewNetDeviceListBinding
        PageRefreshLayout.startIndex = 1
        initPageRefresh()
        initDeviceRefresh()
        initTabAdapter()
        initDeviceInfoAdapter()
    }

    private fun initPageRefresh() {
        binding.page.onRefresh {
            refreshPage()
            binding.refreshLayout.showLoading()
        }
    }

    private fun initDeviceRefresh() {
        binding.refreshLayout.onRefresh {
            refreshDeviceList()
        }
    }

    private fun initTabAdapter() {
        binding.rvTab.setup { rv ->
            rv.addItemDecoration(
                RecycleViewDivider(
                    LinearLayoutManager.HORIZONTAL,
                    ConvertUtils.dp2px(10f),
                    ColorUtils.getColor(R.color.transparent)
                )
            )
            addType<FilterDeviceTabItem>(R.layout.item_filter_device_tab)

            R.id.item.onClick {
                val item = getModel<FilterDeviceTabItem>()
                when (item.name) {
                    "所属产品" -> {
                        showProductSelectionPopupView(itemView)
                    }

                    "在线状态" -> {
                        showOnlineStatusSelectionPopupView(itemView)
                    }

                    "我的收藏" -> {
                        nav().navigate(
                            R.id.action_global_to_followDeviceListFragment
                        )
                    }
                }
            }
        }
    }

    private fun showProductSelectionPopupView(view: View) {
        val filterDeviceTabItem = binding.rvTab.bindingAdapter.getModel<FilterDeviceTabItem>(0)
        val selectionPopupView = ProductSelectionPartShadowPopupView(requireContext())
        selectionPopupView.setData(
            productTabList,
            filterDeviceTabItem.singleSelectionItemLastSelectedIndex
        )
            .setSelectListener(object : ProductSelectionPartShadowPopupView.OnSelectListener {
                override fun onSelect(selectionItem: SingleSelectionItem, position: Int) {
                    filterDeviceTabItem.refreshValue(selectionItem.name, position)
                    mStates.filterProductID.set(selectionItem.extValue)
                    binding.refreshLayout.showLoading()
                }
            })
        XPopup.Builder(context)
            .atView(view)
            .isClickThrough(true)
            .isViewMode(true)
            .isRequestFocus(false)
            .dismissOnTouchOutside(true)// 点击外部是否关闭弹窗，默认为true
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .asCustom(selectionPopupView)
            .show()
    }

    private fun showOnlineStatusSelectionPopupView(view: View) {
        val filterDeviceTabItem = binding.rvTab.bindingAdapter.getModel<FilterDeviceTabItem>(1)
        val selectionPopupView = SingleSelectionPartShadowPopupView(requireContext())
        selectionPopupView.setData(
            onlineStatusList,
            filterDeviceTabItem.singleSelectionItemLastSelectedIndex
        )
            .setSelectListener(object :
                SingleSelectionPartShadowPopupView.OnSelectListener {
                override fun onSelect(selectionItem: SingleSelectionItem, position: Int) {
                    filterDeviceTabItem.refreshValue(selectionItem.name, position)
                    mStates.filterOnlineStatus.set(if (selectionItem.name == onlineStatusList[0].name) "" else if (selectionItem.name == onlineStatusList[1].name) "true" else "false")
                    binding.refreshLayout.showLoading()
                }
            })
        XPopup.Builder(context)
            .atView(view)
            .isClickThrough(true)
            .isViewMode(true)
            .isRequestFocus(false)
            .dismissOnTouchOutside(true)// 点击外部是否关闭弹窗，默认为true
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .asCustom(selectionPopupView)
            .show()
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

    override fun initData() {
        initTabData()
        initOnlineStatusData()
    }

    private fun initTabData() {
        val tabList = mutableListOf<FilterDeviceTabItem>()
        tabList.add(
            FilterDeviceTabItem(
                name = "所属产品",
                value = "全部产品"
            )
        )
        tabList.add(
            FilterDeviceTabItem(
                name = "在线状态",
                value = "全部状态"
            )
        )
        tabList.add(
            FilterDeviceTabItem(
                name = "我的收藏",
                value = "我的收藏",
                isShowDropDown = false
            )
        )
        binding.rvTab.models = tabList
        deviceRequestViewModel.getAllProductTabList(userInfo.companyID)
    }

    private fun initOnlineStatusData() {
        onlineStatusList.clear()
        onlineStatusList.add(
            SingleSelectionItem(
                name = "全部状态",
                isChecked = true
            )
        )
        onlineStatusList.add(
            SingleSelectionItem(
                name = "在线",
            )
        )
        onlineStatusList.add(
            SingleSelectionItem(
                name = "离线",
            )
        )
    }

    private fun refreshPage() {
        deviceRequestViewModel.getDeviceStatByCompanyID(
            userInfo.companyID,
            MmkvCacheUtil.isHasListSuperInfoPermission()
        )
    }

    private fun refreshDeviceList() {
        deviceRequestViewModel.getDeviceList(
            companyID = userInfo.companyID,
            productID = mStates.filterProductID.get(),
            currentPage = binding.refreshLayout.index,
            pageSize = PAGE_SIZE,
            isHasListSuperInfoPermission = MmkvCacheUtil.isHasListSuperInfoPermission(),
            onlineStatus = mStates.filterOnlineStatus.get()
        )
    }

    private fun followDevice(deviceSn: String) {
        deviceRequestViewModel.addUserFollowDevice(deviceSn)
    }

    private fun unFollowDevice(deviceSn: String) {
        deviceRequestViewModel.cancelUserFollowDevice(deviceSn)
    }

    override fun lazyLoadData() {
        binding.page.showLoading(refresh = false)
        refreshPage()
        refreshDeviceList()
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onGoToSearch() {
            nav().navigate(
                R.id.action_mainFragment_to_deviceSearchFragment
            )
        }
    }

    override fun createObserver() {
        deviceRequestViewModel.deviceStatisticInfoResult.observe(viewLifecycleOwner) { dataResult: DataResult<DeviceStatisticInfo> ->
            if (!dataResult.responseStatus.isSuccess) {
                binding.page.showError()
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            dataResult.result?.let {
                mStates.onlineCount.set(it.onlineCount)
                mStates.offlineCount.set(it.offlineCount)
                val df = DecimalFormat("#.##") //格式化小数
                val rate: String = df.format(it.onlinePercent) + "%"
                updateTopView(rate)
                binding.page.showContent(false)
            }
        }
        launchWithViewLifecycle {
            deviceRequestViewModel.allProductTabResultFlow.collect {
                if (!it.responseStatus.isSuccess) {
                    Toaster.show(it.responseStatus.errorMessage)
                    return@collect
                }
                it.result?.let { tempList ->
                    productTabList.clear()
                    productTabList.addAll(tempList)
                }
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
    }

    companion object {
        private const val PAGE_SIZE = 20
        fun newInstance() = NewNetDeviceListFragment()
    }
}