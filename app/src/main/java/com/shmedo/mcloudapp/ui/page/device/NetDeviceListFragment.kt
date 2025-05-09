package com.shmedo.mcloudapp.ui.page.device

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.view.LayoutInflater
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.ColorUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.extensions.withArguments
import com.shmedo.core.commonlib.mmkv.AuthMMKVOwner
import com.shmedo.core.model.DeviceStatisticInfo
import com.shmedo.core.model.ProductInfo
import com.shmedo.core.model.UserInfo
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentNetDeviceListBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.ui.adapter.PageAdapter
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.NetDeviceListViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.text.DecimalFormat

@Deprecated("This class is deprecated", ReplaceWith("NetProductDeviceListFragment"))
class NetDeviceListFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private lateinit var binding: FragmentNetDeviceListBinding
    private lateinit var mStates: NetDeviceListViewModel
    private lateinit var deviceRequestViewModel: DeviceRequestViewModel
    private val userInfo: UserInfo by lazy { AuthMMKVOwner.userInfo!! }

    private val activeBg: Int = R.drawable.bg_product_tab_checked
    private val normalBg: Int = R.drawable.bg_product_tab_normal
    private val activeColor: Int = ColorUtils.getColor(R.color.white)
    private val normalColor: Int = ColorUtils.getColor(R.color.colorPrimary)
    private val activeSize: Float = 15f
    private val normalSize: Float = 15f

    private val productInfoList = mutableListOf<ProductInfo>()
    private var companyID = -100
    private var productID = -1


    override fun initViewModel() {
        mStates = getFragmentScopeViewModel()
        deviceRequestViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_net_device_list, BR.stateVM, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentNetDeviceListBinding
    }

    override fun initData() {
        companyID = AuthMMKVOwner.companyID
        productID = -1
        deviceRequestViewModel.getAllProductTabList(
            companyID = AuthMMKVOwner.companyID,
            isHasListSuperInfoPermission = AuthMMKVOwner.listSuperInfoPermission
        )
        binding.page.showLoading(false)
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onGoToSearch() {
            nav().safeNavigate(
                R.id.action_mainFragment_to_deviceSearchFragment
            )
        }

        fun onShowSelectProductPopup() {
//            val selectionPopupView = ProductSelectionPartShadowPopupView(requireContext())
//            selectionPopupView.setData(productInfoList, binding.tabs.selectedTabPosition)
//                .setSelectListener(object : ProductSelectionPartShadowPopupView.OnSelectListener {
//                    override fun onSelect(productInfo: ProductInfo, position: Int) {
//                        binding.tabs.getTabAt(position)?.select()
//                    }
//                })
//            XPopup.Builder(context)
//                .atView(binding.headLine)
//                .isViewMode(true)
//                .dismissOnBackPressed(false) // 按返回键是否关闭弹窗，默认为true
//                .dismissOnTouchOutside(true)// 点击外部是否关闭弹窗，默认为true
//                .enableDrag(false)
//                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
//                .asCustom(selectionPopupView)
//                .show()
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
                binding.page.showError()
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            dataResult.result?.let { tempList ->
                productInfoList.clear()
                productInfoList.addAll(tempList)
                initViewPager()
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

    private fun initViewPager() {
        val mTabFragments = productInfoList.map {
            NetProductDeviceListFragment.newInstance()
                .withArguments(NetProductDeviceListFragment.TAB_PRODUCT_ID to it.id)
        }
        binding.viewpager.adapter = PageAdapter(this, mTabFragments)
        binding.viewpager.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        binding.viewpager.isUserInputEnabled = false
        val tabLayoutMediator =
            TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
                val tabView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.custom_tab_product, null)
                val textView =
                    tabView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tabText)
                textView.text = productInfoList[position].productName
                if (position == 0) {
                    textView.textSize = activeSize
                    textView.setTextColor(activeColor)
                    tabView.setBackgroundResource(activeBg)
                    tab.setCustomView(tabView)
                } else {
                    textView.textSize = normalSize
                    textView.setTextColor(normalColor)
                    tabView.setBackgroundResource(normalBg)
                    tab.setCustomView(tabView)
                }
            }
        tabLayoutMediator.attach()
        binding.tabs.addOnTabSelectedListener(this)

        binding.page.showContent(false)
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        tab.customView?.let {
            it.setBackgroundResource(activeBg)
            val textView =
                it.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tabText)
            textView.textSize = activeSize
            textView.setTextColor(activeColor)
            productID = productInfoList[tab.position].id
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        tab.customView?.let {
            it.setBackgroundResource(normalBg)
            val textView =
                it.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tabText)
            textView.textSize = normalSize
            textView.setTextColor(normalColor)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {}

//    override fun lazyLoadData() {
//        deviceRequestViewModel.getProductList(AuthMMKVOwner.companyID)
//    }

    override fun onResume() {
        super.onResume()
        if (companyID != AuthMMKVOwner.companyID) {
            companyID = AuthMMKVOwner.companyID
            productID = -1
            deviceRequestViewModel.getAllProductTabList(
                companyID = AuthMMKVOwner.companyID,
                isHasListSuperInfoPermission = AuthMMKVOwner.listSuperInfoPermission
            )
        }
        deviceRequestViewModel.getDeviceStatByCompanyID(
            AuthMMKVOwner.companyID,
            AuthMMKVOwner.listSuperInfoPermission
        )
    }

    companion object {
        fun newInstance() = NetDeviceListFragment()
    }
}