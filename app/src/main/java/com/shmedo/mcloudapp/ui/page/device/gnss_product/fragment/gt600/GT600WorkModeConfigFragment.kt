package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.gt600

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600.BasePositionConfigEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600.GnssModeConfigEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.BasePositionData
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.GnssModeData
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.communication.model.ErrorHandlingStrategy
import com.shmedo.mcloudapp.databinding.FragmentGt600WorkModeConfigBinding
import com.shmedo.mcloudapp.extensions.formatDoubleValue
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.GT600WorkModeConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: GT600 工作模式配置页面
 *
 * 功能说明：
 * 1. 工作模式选择（基站/测站）
 *    - 选择"测站"时，隐藏下方所有配置项
 * 2. 坐标初始化选择（是/否）
 *    - 选择"是"时，显示初始化模式配置项
 * 3. 初始化模式选择（自动/手动）
 *    - 选择"手动"时，显示坐标输入框
 * 4. 坐标输入（经度、纬度、高度）
 *
 * 使用指令：
 * - MD_GNSSMODE: 查询/设置工作模式（站点类型：基站/测站）
 * - MD_GET_BASE_POSITION: 查询基站位置信息
 * - MD_SET_BASE_POSITION: 设置基站位置信息
 */
class GT600WorkModeConfigFragment : OptimizedBaseIOTDeviceFragment() {

    // ==================== 视图绑定和ViewModel ====================

    private lateinit var binding: FragmentGt600WorkModeConfigBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: GT600WorkModeConfigViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    // ==================== 配置选项列表 ====================

    /** 工作模式选项：基站、测站 */
    private val workModeList = arrayListOf("基站", "测站")

    /** 坐标初始化选项：是、否 */
    private val coordinateInitializationList = arrayListOf("是", "否")

    /** 初始化模式选项：自动、手动 */
    private val initializationModeList = arrayListOf("自动", "手动")

    // ==================== 生命周期方法 ====================

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_gt600_work_mode_config, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentGt600WorkModeConfigBinding
        binding.llToolbar.toolbar.title = "工作模式"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher { handleBackByCheckDataModified() }
        initRefresh()
    }

    /**
     * 初始化下拉刷新
     */
    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_refresh_fail_warn))
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

    /**
     * 重置为默认参数
     */
    private fun resetDefaultParams() {
        mStates.workMode.set(workModeList[1]) // 默认测站
        mStates.coordinateInitialization.set(coordinateInitializationList[1]) // 默认否
        mStates.initializationMode.set(initializationModeList[0]) // 默认自动
        mStates.longitude.set("") // 经度
        mStates.latitude.set("") // 纬度
        mStates.altitude.set("") // 高度
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    // ==================== 数据查询和保存 ====================

    /**
     * 查询工作模式和基站位置信息
     * 1. 首先查询 GNSS 工作模式（站点类型）
     * 2. 然后查询基站位置信息
     */
    private fun queryData() {
        val commands = listOf(
            // 查询工作模式（站点类型：基站/测站）
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_GNSSMODE,
                GnssModeConfigEntity.createQueryEntity().toCommandString()
            ),
            // 查询基站位置信息
            IOTCommandUtil.getCommand(IOTCommandType.MD_GET_BASE_POSITION)
        )

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

    /**
     * 保存配置
     */
    private fun saveConfiguration() {
        // 验证输入数据
        if (!validateInputData()) {
            return
        }

        // 构建保存指令
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
     * 验证输入数据
     * @return 验证通过返回 true，否则返回 false
     */
    private fun validateInputData(): Boolean {
        // 仅当基站模式 + 坐标初始化为"是" + 手动模式时需要验证坐标输入
        if (mStates.workMode.get() == "基站"
            && mStates.coordinateInitialization.get() == "是"
            && mStates.initializationMode.get() == "手动"
        ) {

            if (mStates.longitude.get().isEmpty()) {
                showMessageDialog("请输入经度!")
                return false
            }
            if (mStates.latitude.get().isEmpty()) {
                showMessageDialog("请输入纬度!")
                return false
            }
            if (mStates.altitude.get().isEmpty()) {
                showMessageDialog("请输入高度!")
                return false
            }

            // 验证输入格式
            try {
                mStates.longitude.get().toDouble()
                mStates.latitude.get().toDouble()
                mStates.altitude.get().toDouble()
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的坐标数值!")
                return false
            }
        }

        return true
    }

    /**
     * 构建保存指令列表
     * @return 指令字符串列表
     *
     * 保存流程：
     * 1. 设置工作模式（站点类型：基站/测站）- 使用 MD_GNSSMODE
     * 2. 如果是基站模式，设置基站位置信息 - 使用 MD_SET_BASE_POSITION
     */
    private fun buildSaveCommands(): List<String> {
        val commands = mutableListOf<String>()

        // 1. 设置工作模式（站点类型）
        // station: 0=基站, 1=测站
        val stationValue = if (mStates.workMode.get() == "基站") "0" else "1"
        val gnssModeCommand = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GNSSMODE,
            GnssModeConfigEntity.createSetEntity(stationValue).toCommandString()
        )
        commands.add(gnssModeCommand)

        // 2. 如果是基站模式且坐标初始化为"是"时
        if (mStates.workMode.get() == "基站" && mStates.coordinateInitialization.get() == "是") {
            val basePositionEntity = BasePositionConfigEntity(
                mode = if (mStates.initializationMode.get() == "手动") "1" else "0",
                lat = if (mStates.initializationMode.get() == "手动") mStates.latitude.get() else IOTConstants.NULL_KEY,
                lon = if (mStates.initializationMode.get() == "手动") mStates.longitude.get() else IOTConstants.NULL_KEY,
                alt = if (mStates.initializationMode.get() == "手动") mStates.altitude.get() else IOTConstants.NULL_KEY,
            )
            val basePositionCommand = IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_BASE_POSITION,
                basePositionEntity.toCommandString()
            )
            commands.add(basePositionCommand)
        }

        return commands
    }

    // ==================== 指令响应处理 ====================

    /**
     * 处理指令响应
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GNSSMODE -> {
                handleGnssModeResponse(cmdStr)
            }

            IOTCommandType.MD_GET_BASE_POSITION -> {
                handleBasePositionQuery(cmdStr)
            }

            IOTCommandType.MD_SET_BASE_POSITION -> {
                handleBasePositionSave(cmdStr)
            }

            else -> {
                Timber.d("未处理的指令类型: ${IOTCommandUtil.extractCommandType(cmdStr)}")
            }
        }
    }

    /**
     * 处理 GNSS 工作模式指令响应
     * 根据 method 参数判断是查询响应还是设置响应
     */
    private fun handleGnssModeResponse(cmdStr: String) {
        // 判断是查询响应还是设置响应
        if (cmdStr.contains("method=0")) {
            // 查询响应
            val result = iotParseManager.parse<GnssModeData>(
                cmdStr,
                IOTCommandType.MD_GNSSMODE
            )
            when (result) {
                is IOTCommandResult.Failure -> {
                    val errMsg = "查询工作模式出错: ${result.message}"
                    handleFailureResult(errMsg, isMessageDialog = true)
                }

                is IOTCommandResult.Success -> {
                    initGnssModeData(result.data)
                }
            }
        } else {
            // 设置响应
            when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                is IOTCommandResult.Failure -> {
                    val errMsg = "设置工作模式出错: ${result.message}"
                    handleFailureResult(errMsg, isMessageDialog = true)
                }

                is IOTCommandResult.Success -> {
                    // 设置成功，继续等待后续指令完成
                    if (!isCommunicationExecuting()) {
                        processNavigateUp()
                    }
                }
            }
        }
    }

    /**
     * 初始化 GNSS 工作模式数据
     * @param data 从设备查询到的工作模式数据
     */
    private fun initGnssModeData(data: GnssModeData) {
        try {
            // 解析 station 字段：0=基站，1=测站
            data.station.toIntOrNull()?.let {
                when (it) {
                    0 -> mStates.workMode.set(workModeList[0]) // 基站
                    1 -> mStates.workMode.set(workModeList[1]) // 测站
                }
            }
            Timber.d("GNSS工作模式已加载: station=${data.station}, workMode=${mStates.workMode.get()}")
        } catch (e: Exception) {
            Timber.e(e, "解析GNSS工作模式数据失败")
        }
    }

    /**
     * 处理基站位置查询响应
     */
    private fun handleBasePositionQuery(cmdStr: String) {
        val result = iotParseManager.parse<BasePositionData>(
            cmdStr,
            IOTCommandType.MD_GET_BASE_POSITION
        )
        when (result) {
            is IOTCommandResult.Failure -> {
                val errMsg = "查询基站位置信息出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            is IOTCommandResult.Success -> {
                initParamData(result.data)
            }
        }
    }

    /**
     * 处理基站位置保存响应
     */
    private fun handleBasePositionSave(cmdStr: String) {
        when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
            is IOTCommandResult.Failure -> {
                val errMsg = "保存配置出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            is IOTCommandResult.Success -> {
                if (!isCommunicationExecuting()) {
                    processNavigateUp()
                }
            }
        }
    }

    /**
     * 初始化参数数据
     * @param data 从设备查询到的基站位置数据
     */
    private fun initParamData(data: BasePositionData) {
        try {
            // 解析 mode 字段：0=自动模式，1=手动模式
            data.mode.toIntOrNull()?.let {
                when (it) {
                    0 -> mStates.initializationMode.set(initializationModeList[0]) // 自动
                    1 -> mStates.initializationMode.set(initializationModeList[1]) // 手动
                }
            }

            // 解析坐标数据
            mStates.longitude.set(data.lon.formatDoubleValue("", 6))
            mStates.latitude.set(data.lat.formatDoubleValue("", 6))
            mStates.altitude.set(data.alt.formatDoubleValue("", 3))

            // 根据坐标值判断坐标初始化状态
            // 如果有坐标值且不为0，则认为坐标已初始化
            val hasCoordinates = data.lat.toDoubleOrNull()?.let { it != 0.0 } == true
                    || data.lon.toDoubleOrNull()?.let { it != 0.0 } == true

            if (mStates.initializationMode.get() == "手动" && hasCoordinates) {
                mStates.coordinateInitialization.set(coordinateInitializationList[0]) // 是
            }

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    // ==================== 点击事件处理 ====================

    /**
     * 点击事件代理类
     */
    inner class ClickProxy : BaseClickProxy() {

        /**
         * 选择工作模式
         */
        fun onWorkModeChooseClick() {
            val selectedIndex = workModeList.indexOf(mStates.workMode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", workModeList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.workMode.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择坐标初始化
         */
        fun onCoordinateInitializationChooseClick() {
            val selectedIndex =
                coordinateInitializationList.indexOf(mStates.coordinateInitialization.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", coordinateInitializationList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.coordinateInitialization.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择初始化模式
         */
        fun onInitializationModeChooseClick() {
            val selectedIndex = initializationModeList.indexOf(mStates.initializationMode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", initializationModeList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.initializationMode.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 恢复默认配置
         */
        fun onResetClick() {
            resetDefaultParams()
        }

        /**
         * 提交保存
         */
        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            saveConfiguration()
        }
    }

    // ==================== 返回处理 ====================

    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }
}
