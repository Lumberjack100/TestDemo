package com.shmedo.mcloudapp.device.ui

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.viewModels
import androidx.navigation.findNavController
import com.blankj.utilcode.util.TimeUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.base.model.SessionInfo
import com.shmedo.lib.core.base.viewmodel.LogViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.activity.BaseActivity
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.databinding.ActivityDeviceHomeBinding
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.model.TcpConnect
import com.shmedo.mcloudapp.device.ui.mr702.fragment.MR702HomeFragment

class DeviceHomeActivity : BaseActivity() {
    private val binding: ActivityDeviceHomeBinding by lazy { getBinding() as ActivityDeviceHomeBinding }
    private val mStates: EmptyViewModel by viewModels()
    private val logViewModel: LogViewModel by viewModels()

    private var productType = ProductType.UnKnown
    private var communicateWay: CommunicateWay = NetPlatformConnect
    private var deviceInfo: DeviceInfo? = null
    private var bleDevice: DiscoveredBluetoothDevice? = null


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_device_home, BR.vm, mStates)
    }

    override fun initData() {
        intent.extras?.let { bundle ->
            productType = bundle.getParcelable(AppContants.Extras.PRODUCT_TYPE)!!
            communicateWay = bundle.getParcelable(AppContants.Extras.COMMUNICATION_WAY)!!
            deviceInfo = bundle.getParcelable(AppContants.Extras.DEVICE_INFO)
            bleDevice = bundle.getParcelable(AppContants.Extras.BLE_DEVICE)
            addLogSession()
        }
        binding.deviceHomeHostFragment.post {
            setGraph()
        }
    }

    private fun addLogSession() {
        deviceInfo?.let {
            val sessionInfo = SessionInfo(
                key = it.firmwareVersion,
                name = it.deviceToken,
                createBy = MmkvCacheUtil.getUserName(),
                createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
                createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm")),
            )
            logViewModel.insertSession(sessionInfo)
        }
    }

    override fun createObserver() {

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

            ProductType.BHY -> {

            }

            ProductType.DAS -> {

            }

            ProductType.E40 -> {

            }

            ProductType.HAC -> {

            }

            ProductType.LR200 -> {

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
                    MR702HomeFragment.newBundleArguments(communicateWay, deviceInfo!!, bleDevice)
                findNavController(R.id.device_home_host_fragment)
                    .setGraph(R.navigation.mr702_graph, bundle)
            }

            ProductType.RN20 -> {

            }

            ProductType.VMS -> {

            }

            else -> {
                when (communicateWay) {
                    NetPlatformConnect -> {

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
            if (type === ProductType.UnKnown) {
                type = ProductType.valueBySuffix(deviceInfo.deviceToken)
                if (type === ProductType.UnKnown) {
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