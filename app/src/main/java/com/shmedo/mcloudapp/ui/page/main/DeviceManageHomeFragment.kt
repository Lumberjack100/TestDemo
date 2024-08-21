package com.shmedo.mcloudapp.ui.page.main

import android.Manifest
import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.ColorUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.toast.Toaster
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentDeviceManageHomeBinding
import com.shmedo.mcloudapp.extensions.getAppViewModel
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.CustomActivityResult
import com.shmedo.mcloudapp.ui.adapter.PageAdapter
import com.shmedo.mcloudapp.ui.dialog.ScanQRCodeResultPopupView
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.page.device.BleScannerListFragment
import com.shmedo.mcloudapp.ui.page.device.DeviceHomeActivity
import com.shmedo.mcloudapp.ui.page.device.NewNetDeviceListFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.ui.viewmodel.state.ScanQRCodeResultPopupViewViewModel
import com.shmedo.mcloudapp.utils.permission.PermissionHelper
import com.shmedo.mcloudapp.utils.permission.PermissionHelper.REQUEST_CODE_SCAN
import com.shmedo.mcloudapp.utils.permission.PermissionInterceptor
import org.koin.androidx.viewmodel.ext.android.getViewModel
import timber.log.Timber

class DeviceManageHomeFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private lateinit var binding: FragmentDeviceManageHomeBinding
    private lateinit var mMessenger: PageMessenger
    private lateinit var mStates: ScanQRCodeResultPopupViewViewModel
    private lateinit var deviceRequestViewModel: DeviceRequestViewModel

    private val activeColor: Int = ColorUtils.getColor(R.color.title_text_color)
    private val normalColor: Int = ColorUtils.getColor(R.color.text_color_666666)
    private val activeSize: Float = 18f
    private val normalSize: Float = 16f
    private val tabs = arrayOf("4G", "蓝牙")
    private val moreChooseList =
        arrayListOf("扫码连接", "查询数据")//"扫一扫", "WIFI 设备", "USB 设备", "查询数据"

    override fun initViewModel() {
        mMessenger = getAppViewModel()
        mStates = getFragmentScopeViewModel()
        deviceRequestViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_device_manage_home, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDeviceManageHomeBinding
        initViewPager()
    }

    override fun initData() {}

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
                scanResult(obj.originalValue)
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
        val mFragments =
            listOf<Fragment>(
                NewNetDeviceListFragment.newInstance(),
                BleScannerListFragment.newInstance()
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

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.toolbar, isStatusBarDarkFont = true)
    }

    inner class ClickProxy {
        /**
         * 恢复默认配置
         */
        fun onMoreChooseClick() {
            XPopup.Builder(context)
                .hasShadowBg(false)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .isDarkTheme(false)
                .popupAnimation(PopupAnimation.TranslateFromRight) //NoAnimation表示禁用动画
                .atView(binding.ivMore)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
                .asAttachList(
                    moreChooseList.toTypedArray(),
                    null
                ) { position, text ->
                    when (text) {
                        "扫码连接" -> {
                            requestPermissionForBluetooth()
                        }

                        "查询数据" -> {
                            nav().navigate(R.id.action_global_to_queryDeviceDataFragment)
                        }
                    }
                }
                .show()
        }
    }

    private fun requestPermissionForBluetooth() {
        XXPermissions.with(this)
            // 申请多个权限
            .permission(
                arrayOf(
                    Manifest.permission.CAMERA
                )
            )
            // 设置权限请求拦截器（局部设置）
            .interceptor(PermissionInterceptor())
            // 设置不触发错误检测机制（局部设置）
            //.unchecked()
            .request(object : OnPermissionCallback {
                override fun onGranted(
                    grantedPermissions: MutableList<String>,
                    allGranted: Boolean
                ) {
                    if (!allGranted) {
                        return
                    }
                    // 扫一扫
                    val options = HmsScanAnalyzerOptions.Creator().setErrorCheck(true)
                        .setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE)
                        .create()
                    ScanUtil.startScan(
                        mActivity,
                        PermissionHelper.REQUEST_CODE_SCAN,
                        options
                    )
                }
            })
    }

    /**
     * https://cloud.shmedo.cn/mcloudapp/device?sn=189150L
     * @param result
     */
    private fun scanResult(result: String) {
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
        XPopup.Builder(context)
            .dismissOnBackPressed(false) // 按返回键是否关闭弹窗，默认为true
            .dismissOnTouchOutside(false)// 点击外部是否关闭弹窗，默认为true
            .enableDrag(false)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .asCustom(popupView)
            .show()
    }
}


