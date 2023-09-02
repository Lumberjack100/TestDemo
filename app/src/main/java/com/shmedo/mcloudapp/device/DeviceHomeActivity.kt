package com.shmedo.mcloudapp.device

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.findNavController
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.ble.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.activity.BaseActivity
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.databinding.ActivityDeviceHomeBinding

class DeviceHomeActivity : BaseActivity() {
    private val binding: ActivityDeviceHomeBinding by lazy { getBinding() as ActivityDeviceHomeBinding }
    private val mStates: EmptyViewModel by viewModels()

    private var productType = ProductType.UnKnown
    private var connectWay: Int = AppContants.CommunicationWay.NET_PLATFORM_CONNECT
    private var deviceInfo: DeviceInfo? = null
    private var device: DiscoveredBluetoothDevice? = null


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_device_home, BR.vm, mStates)
    }


    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun initData() {
        intent.extras?.let { bundle ->
            productType =
                intent.getSerializableExtra(AppContants.Extras.PRODUCT_TYPE) as ProductType
            connectWay = bundle.getInt(
                AppContants.Extras.COMMUNICATION_WAY,
                AppContants.CommunicationWay.NET_PLATFORM_CONNECT
            )
            deviceInfo = bundle.getSerializable(AppContants.Extras.DEVICE_INFO) as DeviceInfo?
            device = intent.getParcelableExtra(AppContants.Extras.BLE_DEVICE)
        }
    }

    override fun createObserver() {

    }

    override fun onResume() {
        super.onResume()
        setGraph()
    }

    private fun setGraph() {
        when (productType) {
            ProductType.DAS -> {
                if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

                } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {

                } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {

                }
            }

            ProductType.ADME -> {
                findNavController(R.id.device_home_host_fragment)
                    .setGraph(R.navigation.net_m20_graph, intent.extras)
            }

            ProductType.M20 -> {
                nav(R.id.device_home_host_fragment)
                    .setGraph(R.navigation.net_m20_graph, intent.extras)
            }

            ProductType.E40 -> {

            }

            ProductType.VMS -> {

            }

            ProductType.BHY -> {

            }

            else -> {

            }
        }
    }

    companion object {
        /**
         * 4G 通讯方式
         *
         * @param context
         * @param deviceInfo
         */
        fun start(context: Context, deviceInfo: DeviceInfo) {
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
                putExtra(AppContants.Extras.DEVICE_INFO, deviceInfo)
                putExtra(AppContants.Extras.PRODUCT_TYPE, type)
                setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(intent)
        }
    }
}