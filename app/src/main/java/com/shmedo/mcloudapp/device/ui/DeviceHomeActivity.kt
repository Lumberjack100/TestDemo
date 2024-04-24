package com.shmedo.mcloudapp.device.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.base.viewmodel.LogViewModel
import com.shmedo.lib.core.ext.getActivityScopeViewModel
import com.shmedo.lib.core.ext.getIOTDeviceLogSession
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.activity.BaseActivity
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.databinding.ActivityDeviceHomeBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.getViewModel

class DeviceHomeActivity : BaseActivity() {
    private lateinit var binding: ActivityDeviceHomeBinding
    private lateinit var mStates: EmptyViewModel
    private lateinit var logViewModel: LogViewModel

    private var productType = ProductType.UnKnown
    private var communicateWay: CommunicateWay = NetPlatformConnect
    private var deviceInfo: DeviceInfo? = null
    private var bleDevice: DiscoveredBluetoothDevice? = null


    override fun initViewModel() {
        mStates = getActivityScopeViewModel()
        logViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_device_home, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as ActivityDeviceHomeBinding
    }

    override fun initData() {
        intent.extras?.let { bundle ->
            productType = bundle.getParcelable(AppContants.Extras.PRODUCT_TYPE)!!
            communicateWay = bundle.getParcelable(AppContants.Extras.COMMUNICATION_WAY)!!
            deviceInfo = bundle.getParcelable(AppContants.Extras.DEVICE_INFO)
            bleDevice = bundle.getParcelable(AppContants.Extras.BLE_DEVICE)
        }
        addHistoryList()
        deviceInfo?.let {
            logViewModel.insertSession(getIOTDeviceLogSession(it.firmwareVersion, it.deviceToken))
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
        val bundle = BaseIOTDeviceFragment.newBundleArguments(
            communicateWay,
            deviceInfo!!,
            bleDevice
        )
        val navController = findNavController(R.id.device_home_host_fragment)
        when (productType) {
            ProductType.COLLECTOR_G_0 -> {
                navController.setGraph(
                    R.navigation.gw100_graph,
                    bundle
                )
            }

            ProductType.M_A_1, ProductType.ADME -> navController.setGraph(R.navigation.adme_graph, bundle)
            ProductType.COLLECTOR_R_1, ProductType.DAS, ProductType.BHY -> {
                val graphId =
                    if (communicateWay == BleConnect) R.navigation.ble_das_graph else R.navigation.das_graph
                navController.setGraph(graphId, bundle)
            }

            ProductType.LR200 -> navController.setGraph(R.navigation.lr200_graph, bundle)
            ProductType.GNSS_M_1,ProductType.GNSS_M_2, ProductType.M20 -> navController.setGraph(R.navigation.m20_graph, bundle)
            ProductType.COLLECTOR_R_2, ProductType.MR702 -> navController.setGraph(
                R.navigation.mr702_graph,
                bundle
            )

            ProductType.TEST_DEVICE -> navController.setGraph(
                R.navigation.test_device_graph,
                bundle
            )

            else -> {

            }
        }
    }

    companion object {
        fun start(
            context: Context,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            communicateWay: CommunicateWay = NetPlatformConnect
        ) {
            //根据设备 SN 用新的产品规则判断所属产品类型
            var type: ProductType = ProductType.valueByNewSuffix(deviceInfo.deviceToken)
            if (type == ProductType.UnKnown) {
                //根据设备产品标识判断所属产品类型
                type = ProductType.valueByPrefix(deviceInfo.productToken.uppercase())
                if (type == ProductType.UnKnown) {
                    ///根据设备 SN 用旧的产品规则判断所属产品类型
                    type = ProductType.valueByOldSuffix(deviceInfo.deviceToken)
                    if (type == ProductType.UnKnown) {
                        Toaster.show(StringUtils.getString(R.string.unsupported_device_type))
                        return
                    }
                }
            }
            val intent = Intent(context, DeviceHomeActivity::class.java).apply {
                putExtra(AppContants.Extras.PRODUCT_TYPE, type as Parcelable)
                putExtra(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
                putExtra(AppContants.Extras.DEVICE_INFO, deviceInfo)
                putExtra(AppContants.Extras.BLE_DEVICE, bleDevice)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(intent)
        }
    }
}