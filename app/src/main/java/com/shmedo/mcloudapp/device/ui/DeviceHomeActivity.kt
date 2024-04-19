package com.shmedo.mcloudapp.device.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
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
import com.shmedo.mcloudapp.device.model.TcpConnect
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
        lifecycleScope.launch {
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
        when (productType) {
            ProductType.ADME -> {
                val bundle = BaseIOTDeviceFragment.newBundleArguments(
                    communicateWay,
                    deviceInfo!!,
                    bleDevice
                )
                findNavController(R.id.device_home_host_fragment)
                    .setGraph(R.navigation.adme_graph, bundle)
            }

            ProductType.DAS,
            ProductType.BHY
            -> {
                val bundle = BaseIOTDeviceFragment.newBundleArguments(
                    communicateWay,
                    deviceInfo!!,
                    bleDevice
                )
                if (communicateWay == BleConnect) {
                    findNavController(R.id.device_home_host_fragment)
                        .setGraph(R.navigation.ble_das_graph, bundle)
                } else {
                    findNavController(R.id.device_home_host_fragment)
                        .setGraph(R.navigation.das_graph, bundle)
                }
            }

            ProductType.E40 -> {

            }

            ProductType.HAC -> {

            }

            ProductType.LR200 -> {
                val bundle = BaseIOTDeviceFragment.newBundleArguments(
                    communicateWay,
                    deviceInfo!!,
                    bleDevice
                )
                findNavController(R.id.device_home_host_fragment)
                    .setGraph(R.navigation.lr200_graph, bundle)
            }

            ProductType.M20 -> {
                val bundle = BaseIOTDeviceFragment.newBundleArguments(
                    communicateWay,
                    deviceInfo!!,
                    bleDevice
                )
                findNavController(R.id.device_home_host_fragment)
                    .setGraph(R.navigation.m20_graph, bundle)
            }

            ProductType.MR702 -> {
                val bundle =
                    BaseIOTDeviceFragment.newBundleArguments(
                        communicateWay,
                        deviceInfo!!,
                        bleDevice
                    )
                findNavController(R.id.device_home_host_fragment)
                    .setGraph(R.navigation.mr702_graph, bundle)
            }

            ProductType.RN20 -> {

            }

            ProductType.VMS -> {

            }

            ProductType.TEST_DEVICE -> {
                val bundle =
                    BaseIOTDeviceFragment.newBundleArguments(
                        communicateWay,
                        deviceInfo!!,
                        bleDevice
                    )
                findNavController(R.id.device_home_host_fragment)
                    .setGraph(R.navigation.test_device_graph, bundle)
            }

            else -> {
                when (communicateWay) {
                    NetPlatformConnect -> {

                    }

                    BleConnect -> {

                    }

                    TcpConnect -> {

                    }

                    else -> {
                    }
                }
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
            var type: ProductType = ProductType.valueByPrefix(deviceInfo.productToken.uppercase())
            //双重判断设备产品类型，先根据设备产品标识判断所属产品类型，若未判断出再根据 SN 号判断，若还未判断出来，提示不支持
            if (type == ProductType.UnKnown) {
                type = ProductType.valueBySuffix(deviceInfo.deviceToken)
                if (type == ProductType.UnKnown) {
                    Toaster.show("暂不支持此设备类型!")
                    return
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