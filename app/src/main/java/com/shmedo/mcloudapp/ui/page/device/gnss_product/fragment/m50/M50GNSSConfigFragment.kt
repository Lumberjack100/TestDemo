package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m50

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gnss_m.GNSSRawConfigEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gnss_m.ModuleParamConfigEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.GNSSRawData
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.ModuleParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.communication.model.ErrorHandlingStrategy
import com.shmedo.mcloudapp.databinding.FragmentM50GnssConfigBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.M50GNSSConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/7/26
 * @desc: 一体式自供电 GNSS 接收机(M50)GNSS配置页面
 *
 * 优化特点：
 * 1. 使用新的通信架构，代码更简洁
 * 2. 统一的错误处理策略
 * 3. 响应驱动的指令执行
 * 4. 配置化的行为控制
 */
class M50GNSSConfigFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentM50GnssConfigBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: M50GNSSConfigViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    // 采样率选项列表
    private val samplingRateList = arrayListOf("1秒", "5秒")
    private val samplingRateValueList = arrayListOf("1", "5")

    // 截至高度角选项列表
    private val elevationAngleList = arrayListOf("5°", "10°", "15°", "20°", "25°", "30°")
    private val elevationAngleValueList = arrayListOf("5", "10", "15", "20", "25", "30")

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m50_gnss_config, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentM50GnssConfigBinding
        binding.llToolbar.toolbar.title = "GNSS配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher { handleBackByCheckDataModified() }
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                finishRefresh()
                return@onRefresh
            }
            queryData()
        }
    }

    override fun initData() {
        super.initData()
        resetDefaultParams()
        // 保存初始状态
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        mStates.samplingRate.set(samplingRateList[0]) // 默认"1秒"
        mStates.elevationAngle.set(elevationAngleList[0]) // 默认"5°"
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 查询数据
     */
    private fun queryData() {
        val commands = listOf(
            // 查询采样率
            IOTCommandUtil.getCommand(IOTCommandType.MD_GET_SAMPLING_RATE),
            // 查询截至高度角
            IOTCommandUtil.getCommand(IOTCommandType.MD_GET_ELEVATION_ANGLE, "type=gnss")
        )

        // 使用新架构的指令序列发送
        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig(
                    strategy = ErrorHandlingStrategy.Dialog,
                    shouldDismissLoading = false
                )
            )
        )
    }

    /**
     * 保存配置
     */
    private fun saveConfiguration() {
        // 构建保存指令序列
        val commands = buildSaveCommands()

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 构建保存指令列表
     */
    private fun buildSaveCommands(): List<String> {
        val commands = mutableListOf<String>()

        // 设置采样率指令
        val samplingRateIndex = samplingRateList.indexOf(mStates.samplingRate.get())
        val samplingRateValue =
            if (samplingRateIndex >= 0) samplingRateValueList[samplingRateIndex] else "1"
        val samplingRateEntity = GNSSRawConfigEntity(obs = samplingRateValue)
        commands.add(
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_SAMPLING_RATE,
                samplingRateEntity.toCommandString()
            )
        )

        // 设置截至高度角指令
        val elevationAngleIndex = elevationAngleList.indexOf(mStates.elevationAngle.get())
        val elevationAngleValue =
            if (elevationAngleIndex >= 0) elevationAngleValueList[elevationAngleIndex] else "5"
        val elevationAngleEntity = ModuleParamConfigEntity(altitude_angle = elevationAngleValue)
        commands.add(
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_ELEVATION_ANGLE,
                elevationAngleEntity.toCommandString()
            )
        )

        return commands
    }

    /**
     * 处理指令响应 - 这是唯一需要实现的方法
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_SAMPLING_RATE -> {
                handleSamplingRateQuery(cmdStr)
            }

            IOTCommandType.MD_GET_ELEVATION_ANGLE -> {
                handleElevationAngleQuery(cmdStr)
            }

            IOTCommandType.MD_SET_SAMPLING_RATE -> {
                handleSamplingRateSave(cmdStr)
            }

            IOTCommandType.MD_SET_ELEVATION_ANGLE -> {
                handleElevationAngleSave(cmdStr)
            }

            else -> {
                Timber.d("未处理的指令类型: ${IOTCommandUtil.extractCommandType(cmdStr)}")
            }
        }
    }

    /**
     * 处理采样率查询响应
     */
    private fun handleSamplingRateQuery(cmdStr: String) {
        val result = iotParseManager.parse<GNSSRawData>(cmdStr, IOTCommandType.MD_GET_SAMPLING_RATE)
        when (result) {
            is IOTCommandResult.Failure -> {
                val errMsg = "查询采样率出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            is IOTCommandResult.Success -> {
                initSamplingRateData(result.data)
            }
        }
    }

    /**
     * 处理截至高度角查询响应
     */
    private fun handleElevationAngleQuery(cmdStr: String) {
        val result =
            iotParseManager.parse<ModuleParam>(cmdStr, IOTCommandType.MD_GET_ELEVATION_ANGLE)
        when (result) {
            is IOTCommandResult.Failure -> {
                val errMsg = "查询截至高度角出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            is IOTCommandResult.Success -> {
                initElevationAngleData(result.data)
            }
        }
    }

    /**
     * 处理采样率保存响应
     */
    private fun handleSamplingRateSave(cmdStr: String) {
        val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
        when (result) {
            is IOTCommandResult.Failure -> {
                val errMsg = "采样率保存出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            else -> {
                if (!isCommunicationExecuting())
                    processNavigateUp()
            }
        }
    }

    /**
     * 处理截至高度角保存响应
     */
    private fun handleElevationAngleSave(cmdStr: String) {
        val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
        when (result) {
            is IOTCommandResult.Failure -> {
                val errMsg = "截至高度角保存出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            else -> {
                if (!isCommunicationExecuting())
                    processNavigateUp()
            }
        }
    }

    /**
     * 初始化采样率数据
     */
    private fun initSamplingRateData(rawData: GNSSRawData) {
        try {
            // 根据返回的 obs 值设置采样率显示
            val obsValue = rawData.obs
            val index = samplingRateValueList.indexOf(obsValue)
            if (index >= 0) {
                mStates.samplingRate.set(samplingRateList[index])
            } else {
                // 如果返回的值不在预定义列表中，使用默认值
                mStates.samplingRate.set(samplingRateList[0])
            }

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 初始化截至高度角数据
     */
    private fun initElevationAngleData(moduleParam: ModuleParam) {
        try {
            // 根据返回的 altitude_angle 值设置截至高度角显示
            val altitudeAngleValue = moduleParam.altitude_angle.split(".")[0]
            val index = elevationAngleValueList.indexOf(altitudeAngleValue)
            if (index >= 0) {
                mStates.elevationAngle.set(elevationAngleList[index])
            } else {
                // 如果返回的值不在预定义列表中，使用默认值
                mStates.elevationAngle.set(elevationAngleList[0])
            }

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 点击事件处理
     */
    inner class ClickProxy : BaseClickProxy() {

        /** 选择采样率 */
        fun onSamplingRateSelected() {
            val selectedIndex = samplingRateList.indexOf(mStates.samplingRate.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", samplingRateList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.samplingRate.set(text)
                    },
                    0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 选择截至高度角 */
        fun onElevationAngleSelected() {
            val selectedIndex = elevationAngleList.indexOf(mStates.elevationAngle.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", elevationAngleList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.elevationAngle.set(text)
                    },
                    0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 恢复默认配置 */
        fun onResetClick() {
            resetDefaultParams()
        }

        /** 提交保存 */
        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            saveConfiguration()
        }
    }

    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

} 