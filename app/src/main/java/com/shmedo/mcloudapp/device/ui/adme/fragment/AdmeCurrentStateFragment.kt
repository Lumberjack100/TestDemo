package com.shmedo.mcloudapp.device.ui.adme.fragment

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.enums.AdmeCTRMotionState
import com.shmedo.lib.device.base.iot_cmd.enums.AdmeModuleErrorType
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeCurrentStateInfo
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeMotionState
import com.shmedo.lib.device.base.iot_cmd.model.hac.HacMotionState
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentAdmeCurrentStateBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeCurrentStateViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2023/12/13
 * @desc: ADME 运行状态页面
 *
 */
class AdmeCurrentStateFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeCurrentStateBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AdmeCurrentStateViewModel
    private val iotParseManager: IOTParserManager by inject()
    private val decimalFormat = DecimalFormat("#.###", DecimalFormatSymbols(Locale.getDefault()))

    private var currentStateInfo: AdmeCurrentStateInfo? = null


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_adme_current_state, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdmeCurrentStateBinding
        binding.llToolbar.toolbar.title = "运行状态"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
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
            queryEquipmentState()
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onShowErrorModulesInfoClick() {
            showErrorModulesInfoDialog()
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 获取设备的当前状态
     */
    private fun queryEquipmentState() {
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(IOTCommandType.ADME_MD_GET_EQUIPMENT_STATE)
        commandItems.add(command)

        if (productType == ProductType.ADME_HAC) {
            command = IOTCommandUtil.getCommand(IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE)
            commandItems.add(command)
        } else {
            command = IOTCommandUtil.getCommand(IOTCommandType.ADME_MD_GET_MOTION_STATE)
            commandItems.add(command)
        }

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_MD_GET_EQUIPMENT_STATE -> {
                val result =
                    iotParseManager.parse<AdmeCurrentStateInfo>(
                        cmdStr,
                        IOTCommandType.ADME_MD_GET_EQUIPMENT_STATE
                    )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询设备状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        currentStateInfo = result.data
                        initStatusInfo(result.data)
                    }
                }
            }

            IOTCommandType.ADME_MD_GET_MOTION_STATE -> {//获取 ADME 的运行状态
                val result = iotParseManager.parse<AdmeMotionState>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_MOTION_STATE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "获取CTR工作状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        updateMotionState(result.data)
                    }
                }
            }

            IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE -> {//获取ADME HAC 的运行状态
                val result = iotParseManager.parse<HacMotionState>(
                    cmdStr,
                    IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "获取CTR工作状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        updateHacMotionState(result.data)
                    }
                }
            }

            else -> {}
        }
    }

    private fun initStatusInfo(currentStateInfo: AdmeCurrentStateInfo) {
        try {
            mStates.deviceNormal.set(currentStateInfo.abndiasis == "0")
            when (currentStateInfo.testway) {
                "0" -> mStates.workMode.set("常规测量模式")
                "1" -> mStates.workMode.set("特定点位模式")
                "2" -> mStates.workMode.set("静态测量模式")
                "3" -> mStates.workMode.set("设备停用模式")
            }
            mStates.wrapStateInfo.set(currentStateInfo)
            mStates.wrapStateInfo.get().apply {
                ctrinputv = ctrinputv.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: IOTConstants.NULL_KEY
                driveinputv = driveinputv.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: IOTConstants.NULL_KEY
                temperature = temperature.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: IOTConstants.NULL_KEY
                humidity = humidity.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: IOTConstants.NULL_KEY
                runmileage = runmileage.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it / 100)
                    } ?: IOTConstants.NULL_KEY

                nexttime = nexttime.toULongOrNull()
                    ?.let {
                        if (it > 0u) {
                            TimeUtils.millis2String(
                                it.toLong(),
                                "yyyy-MM-dd HH:mm"
                            )
                        } else IOTConstants.NULL_KEY
                    } ?: IOTConstants.NULL_KEY

                incvoltage = incvoltage.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: IOTConstants.NULL_KEY

                intertempe = intertempe.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: IOTConstants.NULL_KEY
            }
            mStates.wrapStateInfo.notifyChange()

            if (currentStateInfo.scsq != IOTConstants.NULL_KEY) {
                mStates.signalValue.set(currentStateInfo.scsq.toIntOrNull()?.let {
                    if (it <= 0)
                        it
                    else
                        it * 2 - 113
                } ?: -113)
            }
            if (currentStateInfo.bcsq != IOTConstants.NULL_KEY) {
                mStates.inclinometerBluetoothSignalValue.set(
                    currentStateInfo.bcsq.toIntOrNull()?.let {
                        if (it <= 0)
                            it
                        else
                            it * 2 - 113
                    } ?: -113)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun updateHacMotionState(hacMotionState: HacMotionState) {
        try {
            if (hacMotionState.measmode.isEmpty()) {
                mStates.ctrMotionInfoVisible.set(false)
                return
            }
            mStates.ctrMotionInfoVisible.set(true)
            //CTR 工作异常
            if (hacMotionState.abndiasis != "0") {
                mStates.measureMode.set("异常保护")
                mStates.isMotorInfoNormal.set(false)
                //列出异常原因
                val stringBuilder = StringBuilder()
                stringBuilder.append("异常原因: ")
                val codes = hacMotionState.abndiasis.split("|")
                codes.forEach { code ->
                    val errorType = AdmeModuleErrorType.valueByCode(code)
                    if (errorType != null) {
                        stringBuilder.append(errorType.description)
                        stringBuilder.append(";")
                    }
                }
                //移除最后一个分号
                if (stringBuilder.isNotEmpty()) {
                    stringBuilder.deleteCharAt(stringBuilder.length - 1)
                }
                mStates.motorInfo.set(stringBuilder.toString())
                return
            }
            updateMotionInfo(
                hacMotionState.measmode,
                hacMotionState.measpoint,
                hacMotionState.motorinfo,
                hacMotionState.waittime
            )

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun updateMotionState(admeMotionState: AdmeMotionState) {
        try {
            if (admeMotionState.measmode.isEmpty()) {
                mStates.ctrMotionInfoVisible.set(false)
                return
            }
            mStates.ctrMotionInfoVisible.set(true)
            updateMotionInfo(
                admeMotionState.measmode,
                admeMotionState.measpoint,
                admeMotionState.motorinfo,
                admeMotionState.waittime
            )
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun updateMotionInfo(
        measmode: String,
        measurePoint: String,
        motorinfo: String,
        waittime: String
    ) {
        mStates.isMotorInfoNormal.set(true)
        mStates.measureMode.set(if (measmode == "0") "正测" else "反测")
        when (val ctrMotionState = AdmeCTRMotionState.valueByCode(motorinfo)) {
            AdmeCTRMotionState.DOWN -> {//测斜仪下放
                val msg = if (measurePoint.isNotEmpty() && !measurePoint.contains("|"))
                    String.format("测斜仪下放: %s 米", measurePoint)
                else
                    "测斜仪下放"
                mStates.motorInfo.set(msg)
            }

            AdmeCTRMotionState.BOTTOM_WAITING -> {//管底等待
                val msg = if (measurePoint.isNotEmpty() && !measurePoint.contains("|"))
                    String.format(
                        "管底等待-位置(%s 米)-剩余时间(%s 秒)",
                        measurePoint,
                        waittime
                    )
                else
                    "管底等待"
                mStates.motorInfo.set(msg)
            }

            AdmeCTRMotionState.POINT_MEASUREMENT -> {//测点测量
                if (measurePoint.isNotEmpty() && measurePoint.contains("|")) {
                    val points = measurePoint.split("|")
                    val msg = if (points[0].isNotEmpty() && points[1].isNotEmpty())
                        String.format(
                            "测点测量-测斜仪位置(%s 米)-测点序列(%s)",
                            points[1],
                            points[0]
                        )
                    else
                        "测点测量"
                    mStates.motorInfo.set(msg)
                } else
                    mStates.motorInfo.set("测点测量")
            }

            else -> {
                mStates.motorInfo.set(ctrMotionState.description)
            }
        }
    }

    fun showErrorModulesInfoDialog() {
        if (currentStateInfo == null || currentStateInfo!!.abndiasis.isEmpty()) {
            Toaster.show("设备异常信息为空")
            return
        }
        val descList: ArrayList<String> = ArrayList()
        val codes = currentStateInfo!!.abndiasis.split("|")
        codes.forEach {
            val errorType = AdmeModuleErrorType.valueByCode(it)
            if (errorType != null) {
                descList.add(errorType.description)
            }
        }
        XPopup.Builder(context)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .enableDrag(false)
            .asCenterList(
                "异常信息", descList.toTypedArray(),
                null, -1,
                null, 0, R.layout.custom_xpopup_adapter_abnormal_info
            )
            .show()
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}