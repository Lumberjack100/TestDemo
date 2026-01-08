package com.shmedo.mcloudapp.ui.page.device


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.commonlib.mmkv.MmkvCacheUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.ActivityDeviceHomeBinding
import com.shmedo.mcloudapp.extensions.InsetsManager
import com.shmedo.mcloudapp.extensions.getAppViewModel
import com.shmedo.mcloudapp.extensions.isM50Series
import com.shmedo.mcloudapp.extensions.isUDSeries
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.CustomActivityResult
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.base.activity.BaseActivity
import com.shmedo.mcloudapp.ui.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.LogViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.PageMessenger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeviceHomeActivity : BaseActivity() {
    private lateinit var binding: ActivityDeviceHomeBinding
    private lateinit var mMessenger: PageMessenger
    private val mStates: EmptyViewModel by viewModels()
    private val logViewModel: LogViewModel by viewModel()

    private var productType = ProductType.UnKnown
    private var communicateWay: CommunicateWay = NetPlatformConnect
    private var deviceInfo: DeviceInfo? = null
    private var bleDevice: DiscoveredBluetoothDevice? = null


    override fun initViewModel() {
        mMessenger = getAppViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_device_home, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        binding = getBinding() as ActivityDeviceHomeBinding
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 只在 NavHost 容器上做“系统栏”基础填充（不动 IME）
        InsetsManager.applyGlobalSystemBarsBottom(binding.deviceHomeHostFragment)
    }

    override fun initData() {
        //关闭指令调试模式
        CommonMMKVOwner.isCommandDebugMode = false
        intent.extras?.let { bundle ->
            productType = bundle.getParcelable(AppContants.Extras.PRODUCT_TYPE)!!
            communicateWay = bundle.getParcelable(AppContants.Extras.COMMUNICATION_WAY)!!
            deviceInfo = bundle.getParcelable(AppContants.Extras.DEVICE_INFO)
            bleDevice = bundle.getParcelable(AppContants.Extras.BLE_DEVICE)
        }
        addHistoryList()
        deviceInfo?.let {
            logViewModel.insertIOTDeviceLogSession(it.firmwareVersion, it.deviceToken)
        }
        binding.deviceHomeHostFragment.post {
            setGraph()
        }
    }

    private fun addHistoryList() {
        lifecycleScope.launch(Dispatchers.Default) {
            deviceInfo?.let {
                val historyList = arrayListOf<String>()
                historyList.addAll(MmkvCacheUtil.getSearchHistoryData())
                if (historyList.contains(it.deviceToken)) {
                    historyList.remove(it.deviceToken)
                }
                historyList.add(0, it.deviceToken)
                if (historyList.size > 10)
                    historyList.removeAt(10)
                MmkvCacheUtil.setSearchHistoryData(MoshiUtil.toJson(historyList.toList()))
            }
        }
    }

    private fun setGraph() {
        val bundle2 = BaseIOTDeviceFragment.newBundleArguments(
            productType,
            communicateWay,
            deviceInfo!!,
            bleDevice
        )
        val navController = findNavController(R.id.device_home_host_fragment)
        when (productType) {
            ProductType.M_A_1, ProductType.ADME -> navController.setGraph(
                R.navigation.adme_graph,
                bundle2
            )

            ProductType.ADME_HAC -> navController.setGraph(
                R.navigation.adme_hac_graph,
                bundle2
            )

            ProductType.M20, ProductType.GNSS_M_1, ProductType.GNSS_M_2 -> {
                navController.setGraph(
                    R.navigation.m20s_graph,
                    bundle2
                )
            }

            in ProductType.entries.filter { it.isM50Series() } -> {
                navController.setGraph(
                    R.navigation.m50_graph,
                    bundle2
                )
            }

            ProductType.COLLECTOR_G_0 -> {//自组网报警网关
                navController.setGraph(
                    R.navigation.gw_graph,
                    bundle2
                )
            }

            ProductType.COLLECTOR_R_1, ProductType.DAS, ProductType.BHY -> {
                navController.setGraph(
                    R.navigation.das_graph, bundle2
                )
            }

            ProductType.COLLECTOR_G_3, ProductType.COLLECTOR_G_4, ProductType.COLLECTOR_R_2 -> navController.setGraph(
                R.navigation.mr702_graph,
                bundle2
            )

            ProductType.LR200, ProductType.U_I_1, ProductType.U_L_1, ProductType.U_R_1 -> {
                navController.setGraph(
                    R.navigation.u_product_graph,
                    bundle2
                )
            }

            in ProductType.entries.filter { it.isUDSeries() } -> {
                navController.setGraph(
                    R.navigation.ud_graph,
                    bundle2
                )
            }

            ProductType.LB20S -> {
                navController.setGraph(
                    R.navigation.lb20s_graph,
                    bundle2
                )
            }

            ProductType.GNSS_T_1, ProductType.GNSS_T_2 -> {
                navController.setGraph(
                    R.navigation.gt600_graph,
                    bundle2
                )
            }

            else -> {
                navController.setGraph(
                    R.navigation.default_device_graph,
                    bundle2
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mMessenger.dispatchActivityResult(CustomActivityResult(requestCode, resultCode, data))
    }


    override fun onDestroy() {
        // 清除 FLAG_KEEP_SCREEN_ON 标志
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onDestroy()
    }

    companion object {
        /**
         * 获取启动 Intent 的方法
         */
        fun getStartIntent(
            context: Context,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            communicateWay: CommunicateWay = NetPlatformConnect
        ): Intent {
            //根据设备 SN 后缀用新的产品规则来判断所属产品类型
            var type: ProductType = ProductType.valueByNewSuffix(deviceInfo.deviceToken)
            if (type == ProductType.UnKnown) {
                //根据设备产品标识判断所属产品类型
                type = ProductType.valueByPrefix(deviceInfo.productToken.uppercase())
                if (type == ProductType.UnKnown) {
                    //根据设备 SN 后缀用旧的产品规则判断所属产品类型
                    type = ProductType.valueByOldSuffix(deviceInfo.deviceToken)
                    if (type == ProductType.UnKnown) {
                        //会进入默认的设备配置主页面(R.navigation.default_device_graph)
//                        return
                    }
                }
            }

            deviceInfo.productName = deviceInfo.productName.ifEmpty { type.productName }
            deviceInfo.deviceName = deviceInfo.deviceName.ifEmpty { type.productToken }

            return Intent(context, DeviceHomeActivity::class.java).apply {
                putExtra(AppContants.Extras.PRODUCT_TYPE, type as Parcelable)
                putExtra(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
                putExtra(AppContants.Extras.DEVICE_INFO, deviceInfo)
                putExtra(AppContants.Extras.BLE_DEVICE, bleDevice)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }

        fun start(
            context: Context,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            communicateWay: CommunicateWay = NetPlatformConnect
        ) {
            val intent = getStartIntent(context, deviceInfo, bleDevice, communicateWay)
            context.startActivity(intent)
        }
    }
}