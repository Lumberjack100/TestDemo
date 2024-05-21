package com.shmedo.mcloudapp.device.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.findNavController
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.base.viewmodel.LogViewModel
import com.shmedo.lib.core.ext.getActivityScopeViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.activity.BaseActivity
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.databinding.ActivityDeviceHomeBinding
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.ui.common.QueryDeviceDataFragment
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import org.koin.androidx.viewmodel.ext.android.getViewModel

/**
 * 创建者：gonghe
 * 创建时间：2024/5/17
 * 描述： TODO
 */
class QuickFunctionActivity : BaseActivity() {
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
        registerOnBackPressedDispatcher {
            finish()
        }
    }

    override fun initData() {
        intent.extras?.let { bundle ->
            productType = bundle.getParcelable(AppContants.Extras.PRODUCT_TYPE)!!
            communicateWay = bundle.getParcelable(AppContants.Extras.COMMUNICATION_WAY)!!
            deviceInfo = bundle.getParcelable(AppContants.Extras.DEVICE_INFO)
            bleDevice = bundle.getParcelable(AppContants.Extras.BLE_DEVICE)
        }
        binding.deviceHomeHostFragment.post {
            setGraph()
        }
    }

    private fun setGraph() {
        val bundle = QueryDeviceDataFragment.newBundleArguments(
            deviceInfo?.deviceToken ?: ""
        )
        val navController = findNavController(R.id.device_home_host_fragment)
        navController.setGraph(
            R.navigation.include_query_data_graph,
            bundle
        )

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
                        type = ProductType.TEST_DEVICE
//                        return
                    }
                }
            }
            val intent = Intent(context, QuickFunctionActivity::class.java).apply {
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