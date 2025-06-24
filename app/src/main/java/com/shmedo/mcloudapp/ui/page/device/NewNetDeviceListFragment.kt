package com.shmedo.mcloudapp.ui.page.device

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
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
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.impl.PartShadowPopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import com.shmedo.core.commonlib.mmkv.AuthMMKVOwner
import com.shmedo.core.commonlib.mmkv.MmkvCacheUtil
import com.shmedo.core.model.DeviceInfo
import com.shmedo.core.model.ProductGroupConfig
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentNewNetDeviceListBinding
import com.shmedo.mcloudapp.extensions.getAppViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.FilterDeviceTabItem
import com.shmedo.mcloudapp.model.ProductGroupItem
import com.shmedo.mcloudapp.model.ProductSeriesItem
import com.shmedo.mcloudapp.model.SingleSelectionItem
import com.shmedo.mcloudapp.ui.dialog.NewProductSelectionPartShadowPopupView
import com.shmedo.mcloudapp.ui.dialog.SingleSelectionPartShadowPopupView
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.NetDeviceListViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.ui.widget.recyclerview.RecycleViewDivider
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


/**
 * 创建者：gonghe
 * 创建时间：2024/5/15
 * 描述： 4G 通讯下设备列表页面
 */
class NewNetDeviceListFragment : BaseFragment() {
    private lateinit var binding: FragmentNewNetDeviceListBinding
    private lateinit var mMessenger: PageMessenger
    private val mStates: NetDeviceListViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModel()

    // 在类的顶部添加缺少的字段定义
    private val productSeriesIdMap = mutableMapOf<String, List<Int>>()
    private val productGroupList = mutableListOf<Any>()
    private val onlineStatusList = mutableListOf<SingleSelectionItem>()

    private var productSelectionPopupView: PartShadowPopupView? = null
    private var onlineStatusSelectionPopupView: PartShadowPopupView? = null


    override fun initViewModel() {
        mMessenger = getAppViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_new_net_device_list, BR.stateVM, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentNewNetDeviceListBinding
        PageRefreshLayout.startIndex = 1
        initDeviceRefresh()
        initTabAdapter()
        initDeviceInfoAdapter()
    }

    private fun initDeviceRefresh() {
        binding.devicePageRefreshLayout.onRefresh {
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
                        nav().safeNavigate(
                            R.id.action_global_to_followDeviceListFragment
                        )
                    }
                }
            }
        }
    }

    /**
     * 显示产品选择弹窗
     */
    private fun showProductSelectionPopupView(view: View) {
        if (productSelectionPopupView != null && productSelectionPopupView!!.isShow) {
            return
        }
        val filterDeviceTabItem = binding.rvTab.bindingAdapter.getModel<FilterDeviceTabItem>(0)
        filterDeviceTabItem.refreshDropDown(true)

        productSelectionPopupView = NewProductSelectionPartShadowPopupView(requireContext()).apply {
            setData(
                productGroupList,
                filterDeviceTabItem.singleSelectionItemLastSelectedIndex
            )
            setSelectListener(object : NewProductSelectionPartShadowPopupView.OnSelectListener {
                override fun onSelect(item: ProductSeriesItem, position: Int) {
                    filterDeviceTabItem.refreshDropDown(false)
                    filterDeviceTabItem.refreshValue(item.name, position)
                    mStates.filterProductParam.set(item.name)
                    binding.devicePageRefreshLayout.showLoading()
                }
            })
        }
        XPopup.Builder(context)
            .atView(view)
            .isClickThrough(true)
            .isViewMode(true)
            .isRequestFocus(false)
            .dismissOnTouchOutside(true)// 点击外部是否关闭弹窗，默认为true
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .setPopupCallback(object : SimpleCallback() {
                override fun onDismiss(popupView: BasePopupView?) {
                    super.onDismiss(popupView)
                    productSelectionPopupView = null
                    filterDeviceTabItem.refreshDropDown(false)
                }
            })
            .asCustom(productSelectionPopupView)
            .show()
    }

    /**
     * 显示在线状态选择弹窗
     */
    private fun showOnlineStatusSelectionPopupView(view: View) {
        if (onlineStatusSelectionPopupView != null && onlineStatusSelectionPopupView!!.isShow) {
            return
        }
        val filterDeviceTabItem = binding.rvTab.bindingAdapter.getModel<FilterDeviceTabItem>(1)
        filterDeviceTabItem.refreshDropDown(true)

        onlineStatusSelectionPopupView =
            SingleSelectionPartShadowPopupView(requireContext()).apply {
                setData(
                    onlineStatusList,
                    filterDeviceTabItem.singleSelectionItemLastSelectedIndex
                )
                setSelectListener(object :
                    SingleSelectionPartShadowPopupView.OnSelectListener {
                    override fun onSelect(item: SingleSelectionItem, position: Int) {
                        filterDeviceTabItem.refreshDropDown(false)
                        filterDeviceTabItem.refreshValue(item.name, position)
                        mStates.filterOnlineStatusParam.set(if (item.name == onlineStatusList[0].name) "" else if (item.name == onlineStatusList[1].name) "true" else "false")
                        binding.devicePageRefreshLayout.showLoading()
                    }
                })
            }
        XPopup.Builder(context)
            .atView(view)
            .isClickThrough(true)
            .isViewMode(true)
            .isRequestFocus(false)
            .dismissOnTouchOutside(true)// 点击外部是否关闭弹窗，默认为true
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .setPopupCallback(object : SimpleCallback() {
                override fun onDismiss(popupView: BasePopupView?) {
                    super.onDismiss(popupView)
                    onlineStatusSelectionPopupView = null
                    filterDeviceTabItem.refreshDropDown(false)
                }
            })
            .asCustom(onlineStatusSelectionPopupView)
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
        // 加载产品分组配置
        loadProductGroupConfig()
    }

    private fun loadProductGroupConfig() {
        launchWithViewLifecycle(Dispatchers.IO) {
            try {
                val sensorConfigList: List<ProductGroupConfig> =
                    MmkvCacheUtil.getProductGroupConfig()

                productGroupList.clear()
                productSeriesIdMap.clear()

                // 添加"全部产品"选项
                productGroupList.add("全部产品")
                val allProductsItem = ProductGroupItem(
                    children = mutableListOf(
                        ProductSeriesItem(name = "全部产品", productIdList = mutableListOf())
                    )
                )
                productGroupList.add(allProductsItem)

                // 添加到映射关系中
                productSeriesIdMap["全部产品"] = emptyList()

                sensorConfigList.forEach { productGroupConfig ->
                    productGroupList.add(productGroupConfig.productGroup)

                    val productSeriesItems = productGroupConfig.children.map { subProductConfig ->
                        ProductSeriesItem(
                            name = subProductConfig.productSeriesName,
                            productIdList = subProductConfig.productIdList
                        ).apply {
                            // 建立产品系列名称到产品ID列表的映射关系
                            productSeriesIdMap[subProductConfig.productSeriesName] =
                                subProductConfig.productIdList
                        }
                    }.toMutableList()

                    productGroupList.add(ProductGroupItem(children = productSeriesItems))
                }

            } catch (e: Exception) {
                Timber.e(e)
            }
        }
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
                value = "我的收藏"
            )
        )
        binding.rvTab.models = tabList
    }

    private fun initOnlineStatusData() {
        onlineStatusList.clear()
        onlineStatusList.add(
            SingleSelectionItem(
                name = "全部状态",
                checked = true
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


    /**
     * 刷新设备列表
     */
    private fun refreshDeviceList() {
        val productIDList: List<Int> =
            productSeriesIdMap[mStates.filterProductParam.get()] ?: emptyList()
        deviceRequestViewModel.getDeviceList(
            companyID = AuthMMKVOwner.companyID,
            productIDList = productIDList, // 使用产品ID列表
            currentPage = binding.devicePageRefreshLayout.index,
            pageSize = PAGE_SIZE,
            isHasListSuperInfoPermission = AuthMMKVOwner.listSuperInfoPermission,
            onlineStatus = mStates.filterOnlineStatusParam.get()
        )
    }

    private fun followDevice(deviceSn: String) {
        deviceRequestViewModel.addUserFollowDevice(deviceSn)
    }

    private fun unFollowDevice(deviceSn: String) {
        deviceRequestViewModel.cancelUserFollowDevice(deviceSn)
    }

    override fun lazyLoadData() {
        binding.devicePageRefreshLayout.showLoading()
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onGoToSearch() {
            nav().safeNavigate(
                R.id.action_mainFragment_to_deviceSearchFragment
            )
        }
    }

    override fun createObserver() {
        // 获取设备列表
        deviceRequestViewModel.deviceListResult.observe(viewLifecycleOwner) { listDataResult: DataResult<List<DeviceInfo>> ->
            if (!listDataResult.responseStatus.isSuccess) {
                Toaster.show(listDataResult.responseStatus.errorMessage)
                binding.devicePageRefreshLayout.showError()
                return@observe
            }
            listDataResult.result?.let {
                binding.devicePageRefreshLayout.addData(it, isEmpty = {
                    binding.devicePageRefreshLayout.index == 1 && it.isEmpty()
                }, hasMore = {
                    binding.devicePageRefreshLayout.index < listDataResult.totalPage
                })
            }
        }
        // 收藏设备
        deviceRequestViewModel.followDeviceResult.observe(viewLifecycleOwner) { dataResult: DataResult<String> ->
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            Toaster.show("已收藏")
        }
        // 取消收藏设备
        deviceRequestViewModel.cancelFollowDeviceResult.observe(viewLifecycleOwner) { dataResult: DataResult<String> ->
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            Toaster.show("已取消收藏")
        }
        mMessenger.isRefreshDeviceList.observe(viewLifecycleOwner) {
            binding.devicePageRefreshLayout.showLoading()
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
        fun newInstance() = NewNetDeviceListFragment()
    }
}