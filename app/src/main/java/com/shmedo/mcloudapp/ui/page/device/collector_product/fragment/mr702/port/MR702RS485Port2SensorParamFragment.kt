package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702.port

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.ToastParams
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRRS485Port2SensorParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port2SensorParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.communication.model.ErrorHandlingStrategy
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port2SensorParamBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.MRSensorItem
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702RS485Port2SensorParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.UUID

class MR702RS485Port2SensorParamFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702Rs485Port2SensorParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val portHomeViewModel: MR702PortHomeViewModel by activityViewModels()
    private val mStates: MR702RS485Port2SensorParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private var isAdd: Boolean = false
    private lateinit var sensorItem: MRSensorItem

    private val calculateList = mutableListOf("不计算", "计算")
    private val calculateFormulaList = mutableListOf("直线式")


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_mr702_rs485_port2_sensor_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702Rs485Port2SensorParamBinding
        binding.llToolbar.toolbar.title = "RS485-2"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            processBack(true)
        }
        registerOnBackPressedDispatcher {
            processBack(true)
        }
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_refresh_fail_warn))
                return@onRefresh
            }
            queryData()
        }
    }

    override fun initData() {
        super.initData()
        arguments?.let {
            isAdd = it.getBoolean(ADD_SENSOR)
            sensorItem = it.getParcelable(SENSOR_MODEL_ITEM)!!
        }
        mStates.isAdd.set(isAdd)
        if (isAdd) {
            binding.refreshLayout.setEnableRefresh(false)
        }

        mStates.sensorType.set(sensorItem.sensorID)
        mStates.modelName.set(sensorItem.sensorName)
        mStates.modelToken.set(sensorItem.modelToken)

        resetDefaultParam()
    }

    /**
     * 重置采集项
     */
    private fun resetDefaultParam() {
        mStates.channelNumber.set("")//通道编号
        mStates.hydrologicalIdentification.set("")//水文标识

        mStates.calculate.set(calculateList[0])//是否计算
        mStates.calculateFormula.set(calculateFormulaList[0])//计算公式
        mStates.sensitivityK.set("0")//灵敏度K 0
        mStates.temperatureCorrectionCoefficientB.set("0")//温度修正系数 b 0
        mStates.initialFrequencyF0.set("0")//初始频率 F0 0
        mStates.initialTemperatureT0.set("0")//初始温度 T0 0
        mStates.initialWaterLevel.set("0")//初始水位 0
        mStates.weirHeight.set("0")//堰角高度 0
        mStates.polyA.set("0")//多项式系数A值 0
        mStates.polyB.set("0")//多项式系数B值 0
        mStates.polyC.set("0")//多项式系数C值 0

        mStates.filterCoefficient.set("2")//滤波系数 2
        mStates.triggerValue.set("0")//触发值 0
        mStates.upperLimit.set("1000")//上限值 1000
        mStates.lowerLimit.set("0")//下限值 0
        mStates.correctValue.set("0")//修正值 0
    }

    inner class ClickProxy : BaseClickProxy() {

        /**
         * 是否计算
         */
        fun onIsCalculateChooseClick() {
            val selectedIndex = calculateList.indexOf(mStates.calculate.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", calculateList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.calculate.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 计算公式
         */
        fun onCalculateFormulaChooseClick() {
            val selectedIndex = calculateFormulaList.indexOf(mStates.calculateFormula.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择计算公式", calculateFormulaList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.calculateFormula.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        override fun onSubmitButtonClick() {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        val commands = mutableListOf<String>()

        if (mStates.modelToken.get().isEmpty()) {
            showMessageDialog("请输入物模型")
            return
        }
        if (mStates.channelNumber.get().isEmpty()) {
            showMessageDialog("请输入通道编号")
            return
        }
        try {
            val value = mStates.channelNumber.get().toInt()
            if (value < 1 || value > 16) {
                showMessageDialog("通道编号数值范围[1,16]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的通道编号!")
            return
        }
        if (mStates.hydrologicalIdentification.get().isEmpty()) {
            showMessageDialog("请输入水文标识")
            return
        }
        if (mStates.filterCoefficient.get().isEmpty()) {
            showMessageDialog("请输入滤波系数")
            return
        }
        if (mStates.triggerValue.get().isEmpty()) {
            showMessageDialog("请输入触发值")
            return
        }
        if (mStates.upperLimit.get().isEmpty()) {
            showMessageDialog("请输入上限值")
            return
        }
        if (mStates.lowerLimit.get().isEmpty()) {
            showMessageDialog("请输入下限值")
            return
        }
        if (mStates.correctValue.get().isEmpty()) {
            showMessageDialog("请输入修正值")
            return
        }
        val entity = MRRS485Port2SensorParamEntity(
            sensortype = mStates.sensorType.get(),
            chl = mStates.channelNumber.get(),
            model = mStates.modelToken.get() + "_" + mStates.channelNumber.get(),
            swtoken = mStates.hydrologicalIdentification.get(),
            filtercnt = mStates.filterCoefficient.get(),
            gateval = mStates.triggerValue.get(),
            uplimit = mStates.upperLimit.get(),
            lowlimit = mStates.lowerLimit.get(),
            corrvalue = mStates.correctValue.get(),

            calctype = if (mStates.modelToken.get() == "10066") calculateList.indexOf(mStates.calculate.get())
                .toString() else IOTConstants.NULL_KEY,
            kvalue = if (mStates.modelToken.get() == "10066") mStates.sensitivityK.get() else IOTConstants.NULL_KEY,
            bvalue = if (mStates.modelToken.get() == "10066") mStates.temperatureCorrectionCoefficientB.get() else IOTConstants.NULL_KEY,
            r0value = if (mStates.modelToken.get() == "10066") mStates.initialFrequencyF0.get() else IOTConstants.NULL_KEY,
            t0value = if (mStates.modelToken.get() == "10066") mStates.initialTemperatureT0.get() else IOTConstants.NULL_KEY,
            l0value = if (mStates.modelToken.get() == "10066") mStates.initialWaterLevel.get() else IOTConstants.NULL_KEY,
            lvalue = if (mStates.modelToken.get() == "10066") mStates.weirHeight.get() else IOTConstants.NULL_KEY,
//            polyavalue = if (mStates.modelToken.get() == "10066") mStates.polyA.get() else IOTConstants.NULL_KEY,
//            polybvalue = if (mStates.modelToken.get() == "10066") mStates.polyB.get() else IOTConstants.NULL_KEY,
//            polycvalue = if (mStates.modelToken.get() == "10066") mStates.polyC.get() else IOTConstants.NULL_KEY
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_RS485_PORT2_SENSOR_PARAM,
            entity.toCommandString()
        )
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun lazyLoadData() {
        if (isAdd) return

        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        val commands = mutableListOf<String>()

        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_GET_RS485_PORT2_SENSOR_PARAM,
            "chl=${sensorItem.chl}"
        )
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig(
                    strategy = ErrorHandlingStrategy.Dialog
                )
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_GET_RS485_PORT2_SENSOR_PARAM -> {
                val result = iotParseManager.parse<MRRS485Port2SensorParam>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_RS485_PORT2_SENSOR_PARAM
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_SET_RS485_PORT2_SENSOR_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
                            Toaster.show(ToastParams().apply {
                                text = "数据保存成功"
                                duration = 500
                            })
                            processBack()
                        }
                    }
                }
            }

            else -> {

            }
        }
    }

    private fun initParamData(sensorParam: MRRS485Port2SensorParam) {
        try {
            mStates.sensorParamWrapper.set(sensorParam)

            mStates.sensorType.set(sensorParam.sensortype)
            mStates.modelToken.set(sensorParam.model.substring(0, sensorParam.model.indexOf("_")))
            mStates.channelNumber.set(sensorParam.chl)
            mStates.hydrologicalIdentification.set(sensorParam.swtoken)
            mStates.filterCoefficient.set(sensorParam.filtercnt)
            mStates.triggerValue.set(sensorParam.gateval)
            mStates.upperLimit.set(sensorParam.uplimit)
            mStates.lowerLimit.set(sensorParam.lowlimit)
            mStates.correctValue.set(sensorParam.corrvalue)

            mStates.calculate.set(if (sensorParam.calctype == "1") calculateList[1] else calculateList[0])
            mStates.sensitivityK.set(sensorParam.kvalue)
            mStates.temperatureCorrectionCoefficientB.set(sensorParam.bvalue)
            mStates.initialFrequencyF0.set(sensorParam.r0value)
            mStates.initialTemperatureT0.set(sensorParam.t0value)
            mStates.initialWaterLevel.set(sensorParam.l0value)
            mStates.weirHeight.set(sensorParam.lvalue)
            mStates.polyA.set(sensorParam.polyavalue)
            mStates.polyB.set(sensorParam.polybvalue)
            mStates.polyC.set(sensorParam.polycvalue)
        } catch (e: Exception) {
            Timber.Forest.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    private fun processBack(isPressBackBtn: Boolean = false) {
        launchWithViewLifecycle {
            if (isPressBackBtn) {
                mMessenger.requestStatusBarColor(if (statusBarColor == 0) R.color.colorPrimary else statusBarColor)
                nav().navigateUp()
                return@launchWithViewLifecycle
            }

            delay(1000)

            // 使用优化的事件机制通知传感器添加
            val newSensor = MRSensorItem(
                sensorID = sensorItem.sensorID,
                sensorName = sensorItem.sensorName,
                modelToken = mStates.modelToken.get(),
                chl = mStates.channelNumber.get(),
                addrDesc = "通道-${mStates.channelNumber.get()}",
                isPlugin = if (isAdd) true else sensorItem.isPlugin, // 新添加的传感器默认在线
                uuid = if (isAdd) UUID.randomUUID().toString() else sensorItem.uuid
            )

            // 通过共享的 ViewModel 通知传感器更新
            portHomeViewModel.notifyPort2SensorUpdate(newSensor)

            Timber.d("通过 ViewModel 发送传感器更新事件: ${newSensor.sensorName}")

            nav().navigateUp()
        }
    }

    companion object {
        const val SENSOR_MODEL_ITEM = "sensor_model_item"
        const val ADD_SENSOR = "add_sensor"

        fun newBundleArguments(
            sensorItem: MRSensorItem,
            isAdd: Boolean = false,
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(SENSOR_MODEL_ITEM, sensorItem)
            putBoolean(ADD_SENSOR, isAdd)
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}