package com.shmedo.mcloudapp.device.ui.adme.fragment

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.device.base.iot_cmd.enums.AdmeModuleErrorType
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeCurrentStateInfo
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeMotionState
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.databinding.FragmentAdmeCurrentStateBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeCurrentStateViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

class AdmeCurrentStateFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentAdmeCurrentStateBinding by lazy { getBinding() as FragmentAdmeCurrentStateBinding }
    private val mStates: AdmeCurrentStateViewModel by viewModels()
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private var currentStateInfo: AdmeCurrentStateInfo? = null

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_adme_current_state, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "运行状态"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
                nav().navigateUp()
            }
        })
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

        command = IOTCommandUtil.getCommand(IOTCommandType.ADME_MD_GET_MOTION_STATE)
        commandItems.add(command)

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
                        sendCommandFromCmdList()
                        currentStateInfo = result.data
                        initStatusInfo(result.data)
                    }
                }
            }

            IOTCommandType.ADME_MD_GET_MOTION_STATE -> {//获取ADME的运行状态
                val result = iotParseManager.parse<AdmeMotionState>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_MOTION_STATE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "获取设备的运行状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        binding.refreshLayout.finish()
                        sendCommandFromCmdList()
                        updateMotionState(result.data)
                    }
                }
            }

            else -> {}
        }
    }

    private fun initStatusInfo(currentStateInfo: AdmeCurrentStateInfo) {
        val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))
        try {
            mStates.sn.set(currentStateInfo.sn)
            mStates.productType.set(currentStateInfo.productid)
            mStates.sim.set(currentStateInfo.simid)
            mStates.imei.set(currentStateInfo.imeid)
            mStates.firmwareVersion.set(currentStateInfo.firversion)
            mStates.signal.set(String.format("%sdBm", currentStateInfo.signalstr))
            mStates.signalValue.set(abs(currentStateInfo.signalstr.toInt()))

            if (currentStateInfo.abndiasis == "0") {
                mStates.deviceNormal.set(true)
                mStates.deviceAbnormalDiagnosis.set("正常")
            } else {
                mStates.deviceNormal.set(false)
                mStates.deviceAbnormalDiagnosis.set("异常")
            }
            when (currentStateInfo.testway) {
                "0" -> mStates.workMode.set("常规测量模式")
                "1" -> mStates.workMode.set("特定点位模式")
                "2" -> mStates.workMode.set("静态测量模式")
                "3" -> mStates.workMode.set("设备停用模式")
            }
            mStates.ctrInputVoltage.set(
                String.format(
                    "%sV",
                    decimalFormat.format(currentStateInfo.ctrinputv.toDouble())
                )
            )
            mStates.driverInputVoltage.set(
                String.format(
                    "%sV",
                    decimalFormat.format(currentStateInfo.driveinputv.toDouble())
                )
            )
            mStates.deviceTemperature.set(
                String.format(
                    "%s℃",
                    decimalFormat.format(currentStateInfo.temperature.toDouble())
                )
            )
            mStates.deviceHumidity.set(
                String.format(
                    "%s%%",
                    decimalFormat.format(currentStateInfo.humidity.toDouble())
                )
            )
            mStates.deviceDropNumber.set(currentStateInfo.downnum)
            mStates.deviceMileage.set(
                String.format(
                    "%sm",
                    decimalFormat.format(currentStateInfo.runmileage.toDouble() / 10)
                )
            )

            mStates.inclinometerType.set(if (currentStateInfo.inctype == "0") "433测斜仪" else "蓝牙测斜仪")
            mStates.inclinometerChannelNumber.set(currentStateInfo.incnum)
            mStates.inclinometerLocationInfo.set(currentStateInfo.incloc)
            mStates.inclinometerVoltage.set(
                String.format(
                    "%sV",
                    decimalFormat.format(currentStateInfo.incvoltage.toDouble())
                )
            )
            mStates.inclinometerTemperature.set(
                String.format(
                    "%s℃",
                    decimalFormat.format(currentStateInfo.intertempe.toDouble())
                )
            )
            mStates.inclinometer4gSignal.set(String.format("%sdBm", currentStateInfo.scsq))
            mStates.inclinometer4gSignalValue.set(abs(currentStateInfo.scsq.toInt()))
            mStates.inclinometerBluetoothSignal.set(String.format("%sdBm", currentStateInfo.bcsq))
            mStates.inclinometerBluetoothSignalValue.set(abs(currentStateInfo.bcsq.toInt()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateMotionState(admeMotionState: AdmeMotionState) {
        try {
            if (admeMotionState.measmode.isEmpty()) {
                mStates.ctrMotionInfoVisible.set(false)
                return
            }
            mStates.ctrMotionInfoVisible.set(true)
            mStates.measureMode.set(if (admeMotionState.measmode == "0") "正测" else "反测")
            val measurePoint = admeMotionState.measpoint
            when (admeMotionState.motorinfo) {
                "0" -> mStates.motorInfo.set("磁开关触发")
                "1" -> mStates.motorInfo.set("磁开关触发,测斜仪配对,设置参数")
                "2" -> {
                    val msg = if (measurePoint.isNotEmpty() && !measurePoint.contains("|"))
                        String.format("测斜仪下放: %s 米", measurePoint)
                    else
                        "测斜仪下放"
                    mStates.motorInfo.set(msg)
                }

                "3" -> {
                    val msg = if (measurePoint.isNotEmpty() && !measurePoint.contains("|"))
                        String.format(
                            "管底等待-位置(%s m)-剩余时间(%s s)",
                            measurePoint,
                            admeMotionState.waittime
                        )
                    else
                        "管底等待"
                    mStates.motorInfo.set(msg)
                }

                "4" -> {
                    if (measurePoint.isNotEmpty() && measurePoint.contains("|")) {
                        val points = measurePoint.split("|")
                        val msg = if (points[0].isNotEmpty() && points[1].isNotEmpty())
                            String.format(
                                "测点测量-测斜仪位置(%s m)-测点序列(%s)",
                                points[1],
                                points[0]
                            )
                        else
                            "测点测量"
                        mStates.motorInfo.set(msg)
                    }
                }

                "5" -> mStates.motorInfo.set("磁开关触发，测量结束")
                "6" -> mStates.motorInfo.set("测斜仪配对,读取数据")
                "7" -> mStates.motorInfo.set("数据上传")
                "8" -> mStates.motorInfo.set("周期等待")
            }
        } catch (e: Exception) {
            e.printStackTrace()
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