package com.shmedo.mcloudapp.device.ui.das.fragment.ble.deviceinfo

import android.os.Bundle
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.model.das.DeviceStatusInfoOne
import com.shmedo.lib.device.base.md_cmd.model.das.DeviceStatusInfoTwo
import com.shmedo.lib.device.base.md_cmd.model.das.SystemRunStateInfo
import com.shmedo.lib.device.base.md_cmd.model.das.VersionMessageInfo
import com.shmedo.lib.device.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.device.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.device.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentDasBaseInfoBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.DasBaseInfoViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： TODO
 */
class BleDasBaseInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasBaseInfoBinding
    private lateinit var mStates: DasBaseInfoViewModel
    private val mdParseManager: MDParserManager by inject()
    private val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))

    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_das_base_info, BR.stateVM, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasBaseInfoBinding
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryBaseInfo()
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 获取设备的基本信息
     */
    private fun queryBaseInfo() {
        commandItems.clear()

        /**
         * 查询设备状态1: ##041\r\n <br/>
         * 应答: $$041,(1),(2),(3),(4),(5),(6)\r\n <br/>
         * （1）SN号 <br/>
         * （2）IMEI号 <br/>
         * （3）SIM卡号 <br/>
         * （4）启动代码1 <br/>
         * （5）启动代码2 <br/>
         * （6）信号强度，1~11为1格信号，12~18为2格信号，19~25为3格信号，26~31为4格信号 <br/>
         */
        var command = MDCommandUtil.getCommand(MDCommandType.QUERY_DAS_STATUS_1)
        commandItems.add(command)

        /**
         * 获取版本信息 ##040\r\n <br/>
         * 应答: $$040,(1),(2),(3)\r\n<br/>
         * (1)“产品序列号”<br/>
         * (2)“固件版本号”<br/>
         * (3)“生产日期”<br/>
         */
        command = MDCommandUtil.getCommand(MDCommandType.VERSION_MESSAGE)
        commandItems.add(command)

        /**
         * 获取信号强度 ##014\r\n<br/>
         * 应答:$$014,(1),(2) ,(3),(4) ,(5),(6) ,(7),(8), (9),(10) \r\n<br/>
         * (1)：信号值<br/>
         * (2)：GPS定位搜星数目<br/>
         * (3)：启动代码<br/>
         * (4)：重启代码<br/>
         * (5)：sim卡ccid<br/>
         * (6)：设备内部温度<br/>
         * (7)：设备内部电池电压<br/>
         * (8)：设备外部电压<br/>
         * (9)：运营商类型<br/>
         * (10)：网络制式<br/>
         */
        command =
            MDCommandUtil.getCommand(MDCommandType.SYSTEM_RUN_STATE)
        commandItems.add(command)

        /**
         * 查询设备状态2:##042\r\n<br/>
         * $$042,(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(15),(16),(17),(18)\r\n<br/>
         * （1）SN号<br/>
         * （2）经度<br/>
         * （3）纬度<br/>
         * （4）设备内部电压<br/>
         * （5）设备外部电压<br/>
         * （6）太阳能控制器状态<br/>
         * （7）太阳能板电压<br/>
         * （8）电池电压<br/>
         * （9）日发电量<br/>
         * （10）日耗电量<br/>
         * （11）机箱内部温湿度状态<br/>
         * （12）机箱内部温度<br/>
         * （13）机箱内部湿度<br/>
         * （14）机箱外部温湿度状态<br/>
         * （15）机箱外部温度<br/>
         * （16）机箱外部湿度<br/>
         * （17）开关量类型，1：雨量计，2：关闭，3：断线报警器<br/>
         * （18）降雨量或断线报警器状态(1:断开，0：闭合)<br/>
         * 示例：$$042,150000L,0.000000,0.000000,8.4,11.9,0,1.1,11.9,0.0,0.0,0,23.1,35.9,0,23.8,35.1,2,0.0<br/>
         */
        command =
            MDCommandUtil.getCommand(MDCommandType.QUERY_DAS_STATUS_2)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.QUERY_DAS_STATUS_1 -> {//##041\r\n：查询设备状态1
                val result = mdParseManager.parse<DeviceStatusInfoOne>(
                    cmdStr,
                    MDCommandType.QUERY_DAS_STATUS_1
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询基本信息出错"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initDeviceStatusOne(result.data)
                    }
                }
            }

            MDCommandType.VERSION_MESSAGE -> {//##040\r\n：获取版本信息
                val result = mdParseManager.parse<VersionMessageInfo>(
                    cmdStr,
                    MDCommandType.VERSION_MESSAGE
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询版本信息出错"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        mStates.wrapBaseInfo.get().apply {
                            ver = result.data.firmwareVersion
                        }
                        mStates.wrapBaseInfo.notifyChange()
                    }
                }
            }

            MDCommandType.SYSTEM_RUN_STATE -> {//##014\r\n：获取信号强度
                val result = mdParseManager.parse<SystemRunStateInfo>(
                    cmdStr,
                    MDCommandType.SYSTEM_RUN_STATE
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询运行状态出错"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initOperatorInformation(result.data)
                    }
                }
            }

            MDCommandType.QUERY_DAS_STATUS_2 -> {//##042\r\n：查询设备状态2
                val result = mdParseManager.parse<DeviceStatusInfoTwo>(
                    cmdStr,
                    MDCommandType.QUERY_DAS_STATUS_2
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询太阳能、机箱温湿度状态出错"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        setDeviceStatusTwo(result.data)
                    }
                }
            }

            else -> {}
        }
    }

    /**
     *
     */
    private fun initDeviceStatusOne(info: DeviceStatusInfoOne) {
        mStates.wrapBaseInfo.get().apply {
            sn = deviceInfo.deviceToken
            iccid = info.simNumber
            imei = info.imeiNumber
            code = "${MDCommandUtil.formatStringTwo(info.startCodeOne)}${
                MDCommandUtil.formatStringTwo(info.startCodeTwo)
            }"
        }
        mStates.wrapBaseInfo.notifyChange()
    }

    private fun initOperatorInformation(info: SystemRunStateInfo) {
        decimalFormat.applyPattern("#.#")
        try {
            mStates.wrapBaseInfo.get().apply {
                involt =
                    if (info.batteryVoltage.contains("%")) info.batteryVoltage else getDeviceInternalBattery(
                        info.batteryVoltage.toDoubleOrNull() ?: 0.0
                    )
                outvolt = info.externalVoltage.toDoubleOrNull()?.let {
                    decimalFormat.format(it)
                } ?: ""
            }
            mStates.wrapBaseInfo.notifyChange()

            mStates.signalValue.set(info.gprsSignal.toIntOrNull()?.let {
                if (it <= 0)
                    it
                else
                    it * 2 - 113
            } ?: -113)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun getDeviceInternalBattery(internalBattery: Double): String {
        return if (internalBattery <= 6) {
            "1%"
        } else {
            val result = ((internalBattery - 6) / (8.2 - 6) * 100).toInt()
            if (result > 100) {
                "100%"
            } else {
                "$result%"
            }
        }
    }

    private fun setDeviceStatusTwo(info: DeviceStatusInfoTwo) {
        //设备经纬度
        mStates.wrapBaseInfo.get().apply {
            local = "${info.longitude},${info.latitude}"
        }
        mStates.wrapBaseInfo.notifyChange()

        //太阳能控制器
        decimalFormat.applyPattern("#.#")
        try {
            mStates.errNo.set(if (info.solarControllerStatus == "1") "0" else "1")
            mStates.solarvolt.set(info.solarPanelVoltage.toDoubleOrNull()?.let {
                decimalFormat.format(it)
            } ?: "")
            mStates.batvolt.set(info.batteryVoltage.toDoubleOrNull()?.let {
                decimalFormat.format(it)
            } ?: "")
            mStates.solarpwr.set(info.dailyPowerGeneration.toDoubleOrNull()?.let {
                decimalFormat.format(it)
            } ?: "")
            mStates.loadpwr.set(info.dailyPowerConsumption.toDoubleOrNull()?.let {
                decimalFormat.format(it)
            } ?: "")
        } catch (e: Exception) {
            Timber.e(e)
        }

        //温湿度状态
        decimalFormat.applyPattern("#.#")
        try {
            mStates.inthErrNo.set(if (info.internalTempHumidityStatus == "1") "0" else "1")
            mStates.inthTemp.set(info.internalTemperature.toDoubleOrNull()?.let {
                decimalFormat.format(it)
            } ?: "")
            mStates.inthHumi.set(info.internalHumidity.toDoubleOrNull()?.let {
                decimalFormat.format(it)
            } ?: "")

            mStates.outthErrNo.set(if (info.externalTempHumidityStatus == "1") "0" else "1")
            mStates.outthTemp.set(info.externalTemperature.toDoubleOrNull()?.let {
                decimalFormat.format(it)
            } ?: "")
            mStates.outthHumi.set(info.externalHumidity.toDoubleOrNull()?.let {
                decimalFormat.format(it)
            } ?: "")
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    companion object {
        fun newInstance() = BleDasBaseInfoFragment()
    }
}