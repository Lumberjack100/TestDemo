package com.shmedo.mcloudapp.ui.page.device

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnInputConfirmListener
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.wifi.connector.model.WifiConnectionState
import com.shmedo.lib.wifi.connector.viewmodel.WifiConnectorViewModel
import com.shmedo.lib.wifi.permission.WifiPermissionNotAvailableReason
import com.shmedo.lib.wifi.permission.WifiPermissionState
import com.shmedo.lib.wifi.permission.viewmodel.WifiPermissionViewModel
import com.shmedo.lib.wifi.scanner.model.DiscoveredWifiNetwork
import com.shmedo.lib.wifi.scanner.repository.WifiScanningState
import com.shmedo.lib.wifi.scanner.viewmodel.WifiScannerViewModel
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentWifiScannerListBinding
import com.shmedo.mcloudapp.databinding.ItemWifiNetworkBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.isESeries
import com.shmedo.mcloudapp.extensions.isGTSeries
import com.shmedo.mcloudapp.extensions.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.TcpConnect
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.WifiScannerListViewModel
import com.shmedo.mcloudapp.utils.permission.PermissionDescription
import com.shmedo.mcloudapp.utils.permission.PermissionInterceptor
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 *
 * 创建者: gonghe
 * 创建时间: 2025/11/17
 *
 * 描述: WiFi 扫描列表页面
 *
 * 功能：
 * 1. 自动监听 WiFi 开关状态，无需手动轮询
 * 2. 自动监听定位权限和服务开关状态
 * 3. 扫描 WiFi 热点
 * 4. 支持搜索关键词过滤
 * 5. 连接到选中的热点
 * 6. 支持自定义 TCP 端口连接
 *
 */
class WiFiScannerListFragment : BaseFragment() {
    private lateinit var binding: FragmentWifiScannerListBinding
    private val mStates: WifiScannerListViewModel by viewModels()

    // Koin 注入 ViewModel
    private val permissionViewModel: WifiPermissionViewModel by viewModel()
    private val scannerViewModel: WifiScannerViewModel by viewModel()
    private val connectorViewModel: WifiConnectorViewModel by viewModel()

    private var selectedNetwork: DiscoveredWifiNetwork? = null

    // 新增：注册 Activity 结果回调
    private val deviceHomeLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // 当从 DeviceHomeActivity 返回时，执行断开 WiFi 操作
            Timber.i("从设备页面返回，执行断开 WiFi 操作")
            connectorViewModel.disconnect()
        }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_wifi_scanner_list, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentWifiScannerListBinding
        initAdapter()
        initRefreshLayout()
    }

    private fun initAdapter() {
        binding.recyclerviewWifi.setup { rv ->
            rv.layoutManager = LinearLayoutManager(context)
            addType<DiscoveredWifiNetwork>(R.layout.item_wifi_network)

            // 绑定数据到列表项（通过 DataBinding 自动处理）
            onBind {
                // 数据绑定在 XML 中已通过 DataBinding 处理
                // 这里只需要处理 DataBinding 无法自动处理的逻辑
                val itemBinding = getBinding<ItemWifiNetworkBinding>()
                val network = getModel<DiscoveredWifiNetwork>()

                // 显示频段信息
                itemBinding.tvFrequency.text = getFrequencyText(network.frequency)
            }

            // 点击事件：连接 WiFi
            R.id.item.onClick {
                val network = getModel<DiscoveredWifiNetwork>()
                onWifiNetworkClick(network)
            }
        }
    }

    /**
     * 获取频段文本
     */
    private fun getFrequencyText(frequency: Int): String {
        return when (frequency) {
            in 2400..2500 -> "2.4GHz"
            in 5000..6000 -> "5GHz"
            else -> "${frequency}MHz"
        }
    }

    private fun initRefreshLayout() {
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            Timber.i("refreshLayout refreshScan")
            refreshScan()
        }
    }

    override fun createObserver() {
        // 监听 WiFi 状态
        launchAndRepeatWithViewLifecycle {
            permissionViewModel.wifiState.collect { state ->
                Timber.d("WiFi State: $state")
                handleWifiState(state)
            }
        }

        // 监听定位状态
        launchAndRepeatWithViewLifecycle {
            permissionViewModel.locationState.collect { state ->
                Timber.d("Location State: $state")
                handleLocationState(state)
            }
        }

        // 监听扫描结果
        launchAndRepeatWithViewLifecycle {
            processScanResult()
        }

        // 监听搜索关键词变化
        mStates.keyWords.observe(viewLifecycleOwner) { keyword ->
            Timber.i("搜索关键词: $keyword")
            scannerViewModel.searchBySSID(keyword.orEmpty())
        }

        // 监听连接状态
        observeConnection()
    }

    /**
     * 处理 WiFi 状态变化
     */
    private fun handleWifiState(state: WifiPermissionState) {
        when (state) {
            is WifiPermissionState.Available -> {
                mStates.wifiDisabled.set(false)
                Timber.i("WiFi 已开启")
            }

            is WifiPermissionState.NotAvailable -> {
                when (state.reason) {
                    is WifiPermissionNotAvailableReason.WifiDisabled -> {
                        mStates.wifiDisabled.set(true)
                        Timber.w("WiFi 未开启")
                    }

                    else -> {
                        // 其他原因不由 WiFi 状态管理器处理
                    }
                }
            }
        }
    }

    /**
     * 处理定位状态变化
     */
    private fun handleLocationState(state: WifiPermissionState) {
        when (state) {
            is WifiPermissionState.Available -> {
                mStates.locationDisabled.set(false)
                mStates.permissionDenied.set(false)
                Timber.i("定位权限和服务已就绪，自动触发扫描")

                // 【关键优化】当定位权限和服务都就绪时，自动触发扫描
                // 这样可以确保页面在权限就绪后立即开始扫描
                scannerViewModel.startScan()
            }

            is WifiPermissionState.NotAvailable -> {
                when (state.reason) {
                    is WifiPermissionNotAvailableReason.LocationServiceDisabled -> {
                        mStates.locationDisabled.set(true)
                        mStates.permissionDenied.set(false)
                        Timber.w("定位服务未开启")
                    }

                    is WifiPermissionNotAvailableReason.PermissionRequired -> {
                        mStates.locationDisabled.set(false)
                        mStates.permissionDenied.set(true)
                        Timber.w("需要定位权限")
                    }

                    else -> {
                        // 其他原因不由定位状态管理器处理
                    }
                }
            }
        }
    }

    /**
     * 处理扫描结果
     */
    private suspend fun processScanResult() {
        scannerViewModel.scanningState.collect { state ->
            when (state) {
                WifiScanningState.Idle -> {
                    Timber.d("扫描空闲")
                }

                WifiScanningState.Scanning -> {
                    Timber.d("正在扫描...")
                    if (binding.recyclerviewWifi.bindingAdapter.modelCount == 0) {
                        binding.refreshLayout.showLoading(refresh = false)
                    }
                }

                is WifiScanningState.NetworksDiscovered -> {
                    Timber.i("发现 ${state.networks.size} 个 WiFi 网络")
                    updateNetworkList(state.networks)
                }

                is WifiScanningState.Error -> {
                    Timber.e("扫描错误: ${state.errorMsg}")
                    binding.refreshLayout.finish(false)
//                    ToastUtils.showLong(state.errorMsg)
                }
            }
        }
    }

    private fun updateNetworkList(networks: List<DiscoveredWifiNetwork>) {
        if (networks.isNotEmpty()) {
            binding.refreshLayout.showContent()
        } else {
            binding.refreshLayout.showEmpty()
        }

        binding.recyclerviewWifi.models = networks
    }

    /**
     * 监听连接状态
     */
    private fun observeConnection() {
        launchWithViewLifecycle {
            connectorViewModel.connectionState.collect { state ->
                Timber.d("WifiConnectionState State: $state")
                when (state) {
                    is WifiConnectionState.Idle -> {
                        // 空闲状态
                    }

                    is WifiConnectionState.Connecting -> {
                        showLoadingDialog(StringUtils.getString(R.string.wifi_state_connecting))
                    }

                    is WifiConnectionState.Connected -> {
                        dismissLoadingDialog()

                        val intent = DeviceHomeActivity.getStartIntent(
                            mActivity,
                            DeviceInfo(
                                deviceToken = state.ssid.replaceFirst(Regex("^MD-?"), ""),
                                deviceName = state.ssid.replaceFirst(Regex("^MD-?"), ""),
                            ),
                            communicateWay = TcpConnect
                        )
                        deviceHomeLauncher.launch(intent)
                    }

                    is WifiConnectionState.Disconnected -> {
                        Timber.d("连接已断开")
                        dismissLoadingDialog()
                    }

                    is WifiConnectionState.Error -> {
                        dismissLoadingDialog()
                        Timber.e("连接失败: ${state.message}")
                        ToastUtils.showShort("连接失败: ${state.message}")
                    }
                }
            }
        }
    }

    /**
     * WiFi 网络点击事件
     */
    private fun onWifiNetworkClick(network: DiscoveredWifiNetwork) {
        val type: ProductType = ProductType.valueByNewSuffix(network.ssid)
        if (!type.isGTSeries() && !type.isESeries()) {
            showMessageDialog("不支持的设备类型")
            return
        }

        selectedNetwork = network

        connectToWifi(network, "12345678", isOpen = !network.isSecured)

//        if (network.isSecured) {
//            showPasswordInputDialog(network)
//        } else {
//            connectToWifi(network, null, isOpen = true)
//        }
    }

    /**
     * 显示密码输入对话框
     */
    private fun showPasswordInputDialog(network: DiscoveredWifiNetwork) {
        XPopup.Builder(context)
            .autoOpenSoftInput(true)
            .asInputConfirm(
                "连接到 ${network.ssid}",
                "请输入 WiFi 密码",
                "12345678",
                "至少 8 位",
                object : OnInputConfirmListener {
                    override fun onConfirm(text: String) {
                        if (text.length < 8) {
                            ToastUtils.showShort("密码至少 8 位")
                            return
                        }
                        connectToWifi(network, text, isOpen = false)
                    }
                }
            )
            .show()
    }

    /**
     * 连接到 WiFi
     */
    private fun connectToWifi(network: DiscoveredWifiNetwork, password: String?, isOpen: Boolean) {
        connectorViewModel.connect(network.ssid, password, isOpen)
    }


    /**
     * 刷新扫描
     *
     * 触发一次新的 WiFi 扫描
     */
    private fun refreshScan() = launchWithViewLifecycle {
        Timber.i("刷新扫描")
        scannerViewModel.startScan()
        delay(5000)
        binding.refreshLayout.finish()
    }

    inner class ClickProxy {
        /**
         * 打开 WiFi 设置
         */
        fun onEnableWifiClick() {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        /**
         * 打开定位设置
         */
        fun onEnableLocationClick() {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        /**
         * 授予定位权限
         */
        fun onGrantLocationPermissionClick() {
            requestLocationPermission()
        }
    }

    /**
     * 请求定位权限
     */
    private fun requestLocationPermission() {
        XXPermissions.with(this)
            .permission(PermissionLists.getAccessFineLocationPermission())
            // 设置权限请求拦截器（局部设置）
            .interceptor(PermissionInterceptor())
            .description(PermissionDescription())
            .request(object : OnPermissionCallback {
                override fun onResult(
                    grantedList: List<IPermission>, deniedList: List<IPermission>
                ) {
                    val allGranted = deniedList.isEmpty()
                    if (allGranted) {
                        Timber.i("定位权限已授予，刷新状态并触发扫描")
                        // 权限授予后手动刷新定位状态
                        permissionViewModel.refreshLocationPermission()

                    } else {
                        Timber.w("定位权限被拒绝")
                    }
                }
            })
    }

    companion object {
        fun newInstance() = WiFiScannerListFragment()
    }
}

