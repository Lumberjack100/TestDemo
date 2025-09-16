package com.shmedo.mcloudapp.ui.page.main

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import com.hjq.toast.Toaster
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.enums.PopupAnimation
import com.lxj.xpopup.impl.PartShadowPopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import com.shmedo.core.commonlib.mmkv.AuthMMKVOwner
import com.shmedo.core.model.DeviceInfo
import com.shmedo.core.model.UserInfo
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentDeviceManageHomeBinding
import com.shmedo.mcloudapp.extensions.getAppViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.CustomActivityResult
import com.shmedo.mcloudapp.model.SingleSelectionItem
import com.shmedo.mcloudapp.ui.adapter.PageAdapter
import com.shmedo.mcloudapp.ui.dialog.ScanQRCodeResultPopupView
import com.shmedo.mcloudapp.ui.dialog.SingleSelectionPartShadowPopupView
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.page.device.BleScannerListFragment
import com.shmedo.mcloudapp.ui.page.device.DeviceHomeActivity
import com.shmedo.mcloudapp.ui.page.device.NewNetDeviceListFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.request.LoginRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.DeviceManageHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.utils.permission.PermissionDescription
import com.shmedo.mcloudapp.utils.permission.PermissionHelper
import com.shmedo.mcloudapp.utils.permission.PermissionHelper.REQUEST_CODE_SCAN
import com.shmedo.mcloudapp.utils.permission.PermissionInterceptor
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class DeviceManageHomeFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private lateinit var binding: FragmentDeviceManageHomeBinding
    private lateinit var mMessenger: PageMessenger
    private val mStates: DeviceManageHomeViewModel by viewModels()
    private val loginRequestViewModel: LoginRequestViewModel by viewModel()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModel()

    private val activeColor: Int = ColorUtils.getColor(R.color.title_text_color)
    private val normalColor: Int = ColorUtils.getColor(R.color.text_color_666666)
    private val activeSize: Float = 20f
    private val normalSize: Float = 16f
    private val tabs = arrayOf("4G", "蓝牙")
    private val moreChooseList =
        arrayListOf("扫码连接", "查询数据")//"扫一扫", "WIFI 设备", "USB 设备", "查询数据"

    private val userInfo: UserInfo by lazy { AuthMMKVOwner.userInfo!! }
    private val companyList: MutableList<SingleSelectionItem> = mutableListOf()
    private var companySelectionPopupView: PartShadowPopupView? = null


    override fun initViewModel() {
        mMessenger = getAppViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_device_manage_home,
            BR.stateVM,
            mStates
        ).addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDeviceManageHomeBinding
        initViewPager()
    }

    override fun initData() {
        mStates.isHasListSuperInfoPermission.set(AuthMMKVOwner.listSuperInfoPermission)
        mStates.companyName.set(userInfo.companyName)
        //如果没有运维权限，需要初始化企业列表，用于切换企业
        if (!AuthMMKVOwner.listSuperInfoPermission)
            initCompanyList()
    }

    override fun createObserver() {
        mMessenger.activityResultDispatcher.observe(
            viewLifecycleOwner
        ) { result: CustomActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.requestCode == REQUEST_CODE_SCAN && result.data != null) {
                val obj: HmsScan? = result.data.getParcelableExtra<HmsScan>(ScanUtil.RESULT)
                if (obj == null) {
                    Toaster.show("扫码结果为空")
                    return@observe
                }
                Timber.d("扫码结果：${obj.originalValue}")
                handleScanResult(obj.originalValue)
            }
        }
        deviceRequestViewModel.deviceInfoResult.observe(viewLifecycleOwner) { dataResult: DataResult<DeviceInfo> ->
            if (!dataResult.responseStatus.isSuccess) {
                showMessageDialog("获取设备信息失败!${dataResult.responseStatus.errorMessage}")
                return@observe
            }
            DeviceHomeActivity.start(
                mActivity,
                dataResult.result!!,
            )
        }
    }

    private fun initViewPager() {
        val mFragments = listOf<Fragment>(
            NewNetDeviceListFragment.newInstance(), BleScannerListFragment.newInstance()
        )
        binding.viewpager.adapter = PageAdapter(this, mFragments)
        binding.viewpager.offscreenPageLimit = mFragments.size
        binding.viewpager.isUserInputEnabled = false
        binding.tabs.addOnTabSelectedListener(this)

        val tabLayoutMediator =
            TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
                val textView = TextView(requireContext())
                textView.text = tabs[position]
                if (position == 0) { // 第一个为默认选中
                    textView.textSize = activeSize
                    textView.typeface = Typeface.DEFAULT_BOLD
                    textView.gravity = Gravity.CENTER
                    textView.setTextColor(activeColor)
                } else {
                    textView.textSize = normalSize
                    textView.typeface = Typeface.DEFAULT
                    textView.gravity = Gravity.CENTER
                    textView.setTextColor(normalColor)
                }
                tab.customView = textView
            }
        tabLayoutMediator.attach()
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        val textView = tab.customView as TextView?
        textView?.apply {
            textSize = activeSize
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(activeColor)
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        val textView = tab.customView as TextView?
        textView?.apply {
            textSize = normalSize
            typeface = Typeface.DEFAULT
            setTextColor(normalColor)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {}

    private fun initCompanyList() {
        launchWithViewLifecycle {
            val tempList = loginRequestViewModel.getCompanyList()
            if (tempList.isEmpty())
                return@launchWithViewLifecycle

            companyList.clear()
            companyList.addAll(tempList)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.root.post {
            initImmersionBar(binding.statusBarView, isTitleBar = false, isStatusBarDarkFont = true)
        }
    }

    inner class ClickProxy {
        /**
         *
         */
        fun onMoreChooseClick() {
            XPopup.Builder(context).isViewMode(true).hasShadowBg(false)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .popupAnimation(PopupAnimation.TranslateFromRight) //NoAnimation表示禁用动画
                .atView(binding.ivMore)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
                .asAttachList(
                    moreChooseList.toTypedArray(), null
                ) { _, text ->
                    when (text) {
                        "扫码连接" -> {
                            requestPermissionForBluetooth()
                        }

                        "查询数据" -> {
                            nav().safeNavigate(R.id.action_global_to_queryDeviceDataFragment)
                        }
                    }
                }.show()
        }

        /**
         * 切换企业
         */
        fun showCompanySelectionPopupView() {
            if (companySelectionPopupView != null && companySelectionPopupView!!.isShow) {
                return
            }
            val position =
                if (mStates.companyIndex in companyList.indices) mStates.companyIndex else 0
            companySelectionPopupView =
                SingleSelectionPartShadowPopupView(requireContext()).apply {
                    setData(
                        companyList,
                        position
                    )
                    setSelectListener(object :
                        SingleSelectionPartShadowPopupView.OnSelectListener {
                        override fun onSelect(selectionItem: SingleSelectionItem, position: Int) {
                            mStates.companyName.set(selectionItem.name)
                            mStates.companyIndex = position
                            AuthMMKVOwner.companyID = selectionItem.extValue.toIntOrNull() ?: 0
                            //延迟 500ms 刷新设备列表
                            binding.rlCompany.postDelayed({
                                mMessenger.requestRefreshDeviceList()
                            }, 500)
                        }
                    })
                }
            XPopup.Builder(context)
                .atView(binding.rlCompany)
                .isClickThrough(true)
                .isViewMode(true)
                .isRequestFocus(false)
                .dismissOnTouchOutside(true)// 点击外部是否关闭弹窗，默认为true
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .setPopupCallback(object : SimpleCallback() {
                    override fun onCreated(popupView: BasePopupView?) {
                        super.onCreated(popupView)
                        mStates.isShowCompanySelectionPopupView.set(true)
                    }

                    override fun onDismiss(popupView: BasePopupView?) {
                        super.onDismiss(popupView)
                        mStates.isShowCompanySelectionPopupView.set(false)
                        companySelectionPopupView = null
                    }
                })
                .asCustom(companySelectionPopupView)
                .show()
        }
    }

    //<editor-fold desc="扫码处理">
    private fun requestPermissionForBluetooth() {
        XXPermissions.with(this)
            .permission(PermissionLists.getCameraPermission())
            .permission(PermissionLists.getReadMediaImagesPermission())
            .permission(PermissionLists.getReadMediaVisualUserSelectedPermission())
            // 设置权限请求拦截器（局部设置）
            .interceptor(PermissionInterceptor())
            .description(PermissionDescription())
            .request(object : OnPermissionCallback {
                override fun onResult(
                    grantedList: List<IPermission>, deniedList: List<IPermission>
                ) {
                    val allGranted = deniedList.isEmpty()
                    if (!allGranted) {
                        return
                    }
                    // 扫一扫
                    val options = HmsScanAnalyzerOptions.Creator()
//                        .setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE)
//                        .setViewType(1)
                        .setErrorCheck(true)
                        .create()

                    ScanUtil.startScan(
                        mActivity, PermissionHelper.REQUEST_CODE_SCAN, options
                    )
                }
            })
    }

    /**
     * https://cloud.shmedo.cn/mcloudapp/device?sn=189150L
     * @param result
     */
    private fun handleScanResult(result: String) {
        var result = result
        if (TextUtils.isEmpty(result)) {
            showMessageDialog("米易通无法识别该二维码")
            return
        }
        if (result.contains("MEDO")) {
            if (result.contains("=")) {
                result = result.substring(result.indexOf("=") + 1)
            }
            parseOldDeviceCode(result)
        } else if (result.startsWith("https://cloud.shmedo.cn/mcloudapp/device")) {
            parseNewDeviceCode(result)
        } else {
            showMessageDialog("米易通无法识别该二维码")
            return
        }
    }

    /**
     * 处理老设备条码规则，例如：MEDO,189150L,DAS
     */
    private fun parseOldDeviceCode(barCode: String) {
        val localData = barCode.split(",".toRegex()).dropLastWhile { it.isEmpty() }
        if (localData.size != 3) {
            showMessageDialog("米易通无法识别该二维码")
            return
        }
        if (TextUtils.isEmpty(localData[1])) {
            showMessageDialog("米易通无法识别该二维码")
            return
        }
        mStates.scanQRCodeResult.set(localData[1])
        showChooseConnectType()
    }

    /**
     * 处理新设备条码规则，例如：https://cloud.shmedo.cn/mcloudapp/device?sn=189150L
     */
    private fun parseNewDeviceCode(barCode: String) {
        val localData = barCode.split("=".toRegex()).dropLastWhile { it.isEmpty() }
        if (localData.size != 2) {
            showMessageDialog("米易通无法识别该二维码")
            return
        }
        if (TextUtils.isEmpty(localData[1])) {
            showMessageDialog("米易通无法识别该二维码")
            return
        }
        mStates.scanQRCodeResult.set(localData[1])
        showChooseConnectType()
    }

    private fun showChooseConnectType() {
        val popupView = ScanQRCodeResultPopupView(requireContext())
        popupView.setTitle("选择连接方式", mStates)
            .setClickListener(object : ScanQRCodeResultPopupView.OnClickListener {
                override fun onBleConnectClick() {
                    binding.tabs.getTabAt(1)?.select()
                    mMessenger.dispatchScanSNResult(mStates.scanQRCodeResult.get())
                }

                override fun on4gConnectClick() {
                    deviceRequestViewModel.getDeviceDetailInfo(mStates.scanQRCodeResult.get())
                }
            })
        XPopup.Builder(context).dismissOnBackPressed(false) // 按返回键是否关闭弹窗，默认为true
            .dismissOnTouchOutside(false)// 点击外部是否关闭弹窗，默认为true
            .enableDrag(false).isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .asCustom(popupView).show()
    }

    // </editor-fold>
}


