package com.shmedo.mcloudapp.ui.page.device

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.drake.brv.BindingAdapter.BindingViewHolder
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
import com.shmedo.mcloudapp.extensions.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.WifiScannerListViewModel
import com.shmedo.mcloudapp.utils.permission.PermissionDescription
import com.shmedo.mcloudapp.utils.permission.PermissionInterceptor
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * WiFi 扫描列表页面
 *
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: 参考 BleScannerListFragment 实现
 *
 * 功能：
 * 1. 扫描 WiFi 热点
 * 2. 过滤 IoT 设备热点（MD- 前缀）
 * 3. 支持搜索关键词过滤
 * 4. 连接到选中的热点
 * 5. 支持自定义 TCP 端口连接
 */
class WiFiScannerListFragment : BaseFragment() {
    private lateinit var binding: FragmentWifiScannerListBinding
    private val mStates: WifiScannerListViewModel by viewModels()

    // Koin 注入 ViewModel
    private val permissionViewModel: WifiPermissionViewModel by viewModel()
    private val scannerViewModel: WifiScannerViewModel by viewModel()
    private val connectorViewModel: WifiConnectorViewModel by viewModel()

    private var selectedNetwork: DiscoveredWifiNetwork? = null
    private var customTcpPort: Int = 8888 // 默认端口

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
                bindWifiNetworkItem()
            }

            // 点击事件：连接 WiFi
            R.id.item.onClick {
                val network = getModel<DiscoveredWifiNetwork>()
                onWifiNetworkClick(network)
            }
        }
    }

    /**
     * 绑定 WiFi 网络项数据
     */
    private fun BindingViewHolder.bindWifiNetworkItem() {
        val itemBinding = getBinding<ItemWifiNetworkBinding>()
        val network = getModel<DiscoveredWifiNetwork>()

        // 显示频段信息
        itemBinding.tvFrequency.text = getFrequencyText(network.frequency)
        // 显示信号质量文本
        itemBinding.tvSignalQuality.text = getSignalQualityText(network.level)
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

    /**
     * 获取信号质量文本
     */
    private fun getSignalQualityText(level: Int): String {
        return when {
            level >= -50 -> "优秀"
            level >= -60 -> "良好"
            level >= -70 -> "一般"
            else -> "较弱"
        }
    }

    private fun initRefreshLayout() {
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            refreshScan()
        }
    }

    override fun createObserver() {
        // 监听权限状态
        launchAndRepeatWithViewLifecycle {
            permissionViewModel.permissionState.collect { state ->
                Timber.d("WiFi Permission State: $state")
                when (state) {
                    is WifiPermissionState.Checking -> {
                        Timber.d("正在检查权限...")
                    }

                    is WifiPermissionState.NotAvailable -> {
                        handlePermissionNotAvailable(state.reason)
                    }

                    is WifiPermissionState.Available -> {
                        mStates.wifiDisabled.set(false)
                        mStates.locationDisabled.set(false)
                        mStates.permissionDenied.set(false)

                        // 权限就绪，开始处理扫描结果
                        launchAndRepeatWithViewLifecycle {
                            processScanResult()
                        }
                    }
                }
            }
        }

        // 监听搜索关键词变化
        mStates.keyWords.observe(viewLifecycleOwner) { keyword ->
            Timber.i("搜索关键词: $keyword")
            scannerViewModel.searchBySSID(keyword.orEmpty())
        }

        // 监听连接状态
        observeConnection()

        // 触发权限检查
        permissionViewModel.checkPermissions()
    }

    private fun handlePermissionNotAvailable(reason: WifiPermissionNotAvailableReason) {
        when (reason) {
            is WifiPermissionNotAvailableReason.WifiDisabled -> {
                mStates.wifiDisabled.set(true)
                mStates.locationDisabled.set(false)
                mStates.permissionDenied.set(false)
            }

            is WifiPermissionNotAvailableReason.PermissionRequired -> {
                mStates.wifiDisabled.set(false)
                mStates.locationDisabled.set(false)
                mStates.permissionDenied.set(true)
            }

            is WifiPermissionNotAvailableReason.LocationServiceDisabled -> {
                mStates.wifiDisabled.set(false)
                mStates.locationDisabled.set(true)
                mStates.permissionDenied.set(false)
            }
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
                    if (!allGranted) {
                        return
                    }
                    permissionViewModel.checkPermissions()
                }
            })
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
                    binding.refreshLayout.showError()
                    ToastUtils.showShort("扫描失败: ${state.errorMsg}")
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
     * WiFi 网络点击事件
     */
    private fun onWifiNetworkClick(network: DiscoveredWifiNetwork) {
        selectedNetwork = network

        // 先显示端口配置对话框
        showPortConfigDialog(network)
    }

    /**
     * 显示端口配置对话框
     */
    private fun showPortConfigDialog(network: DiscoveredWifiNetwork) {
        XPopup.Builder(context)
            .asInputConfirm(
                "配置 TCP 端口",
                "请输入设备 TCP 端口（默认 8888）",
                customTcpPort.toString(),
                null,
                object : OnInputConfirmListener {
                    override fun onConfirm(text: String) {
                        val port = text.toIntOrNull()
                        if (port == null || port !in 1..65535) {
                            ToastUtils.showShort("端口号无效，请输入 1-65535 之间的数字")
                            return
                        }
                        customTcpPort = port

                        // 继续连接流程
                        if (network.isSecured) {
                            showPasswordInputDialog(network)
                        } else {
                            connectToWifi(network, null, isOpen = true)
                        }
                    }
                }
            )
            .show()
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
                null,
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
        Timber.i("连接到 WiFi: ${network.ssid}, TCP 端口: $customTcpPort")
        showLoadingDialog("正在连接...")

        connectorViewModel.connect(network.ssid.orEmpty(), password, isOpen, customTcpPort)
    }

    /**
     * 监听连接状态
     */
    private fun observeConnection() {
        launchAndRepeatWithViewLifecycle {
            connectorViewModel.connectionState.collect { state ->
                when (state) {
                    is WifiConnectionState.Idle -> {
                        // 空闲状态
                    }

                    is WifiConnectionState.Connecting -> {
                        Timber.d("正在连接...")
                    }

                    is WifiConnectionState.Connected -> {
                        dismissLoadingDialog()
                        Timber.i("已连接: ${state.ssid}, IP: ${state.ipAddress}")
                        ToastUtils.showShort("已连接到 ${state.ssid}")

                        // TODO: 跳转到设备控制页面（通过 TCP 通信）
                        // navigateToDeviceControl(state.ssid, state.ipAddress, customTcpPort)
                        Timber.i("准备跳转到设备控制页面: SSID=${state.ssid}, IP=${state.ipAddress}, Port=$customTcpPort")
                    }

                    is WifiConnectionState.Disconnected -> {
                        dismissLoadingDialog()
                        ToastUtils.showShort("连接已断开")
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
     * 刷新扫描
     */
    private fun refreshScan() = launchWithViewLifecycle {
        scannerViewModel.refresh()
        delay(1000)
        binding.refreshLayout.finish()
    }

    override fun lazyLoadData() {
        // 页面加载时自动刷新
        // binding.refreshLayout.autoRefresh()
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

    companion object {
        fun newInstance() = WiFiScannerListFragment()
    }
}

