package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UDFlowCalculationInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentUdFlowCalculationBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.FlowSectionShape
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDFlowCalculationViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2025/1/13
 * 描述： 一体式泥位计流量计算配置
 */
class UDFlowCalculationFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUdFlowCalculationBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: UDFlowCalculationViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val shapeLabels: Array<String> =
        FlowSectionShape.entries.map { it.label }.toTypedArray()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_ud_flow_calculation,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUdFlowCalculationBinding
        binding.llToolbar.toolbar.title = "流量计算"
        binding.llToolbar.toolbar.setNavigationOnClickListener { handleBackByCheckDataModified() }
        registerOnBackPressedDispatcher {
            handleBackByCheckDataModified()
        }
        initRefresh()
    }

    override fun initData() {
        super.initData()
        resetDefaultParams()
        mStates.saveInitialState()
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

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

    private fun resetDefaultParams() {
        mStates.onShapeChanged(FlowSectionShape.CUSTOM)
        mStates.customize.set("0")
        mStates.cannalwide.set("0")
        mStates.initdepth.set("0")
        mStates.initheight.set("0")
        mStates.maxdepth.set("0")
        mStates.bottomwide.set("0")
        mStates.sloperatio.set("0")
        mStates.diameter.set("0")
        mStates.hcorvalue.set("0")
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onShapeClick() {
            val currentIndex = FlowSectionShape.entries.indexOf(mStates.selectedShape.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "",
                    shapeLabels,
                    null,
                    currentIndex,
                    { position, _ ->
                        FlowSectionShape.entries.getOrNull(position)?.let {
                            mStates.onShapeChanged(it)
                        }
                    },
                    0,
                    R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onResetClick() {
            resetDefaultParams()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            if (!validateInputs()) {
                return
            }
            sendSaveCommand()
        }
    }

    private fun validateInputs(): Boolean {
        val shape = mStates.selectedShape.get()

        fun requireDecimal(value: String, emptyMsg: String, invalidMsg: String): Boolean {
            val content = value.trim()
            if (content.isEmpty()) {
                showMessageDialog(emptyMsg)
                return false
            }
            if (content.toDoubleOrNull() == null) {
                showMessageDialog(invalidMsg)
                return false
            }
            return true
        }

        if (shape == FlowSectionShape.CUSTOM) {
            if (mStates.customize.get().trim().isEmpty()) {
                showMessageDialog("请输入自定义参数!")
                return false
            }
        }

        //矩形/梯形
        if (shape == FlowSectionShape.RECTANGLE || shape == FlowSectionShape.TRAPEZOID) {
            if (!requireDecimal(
                    mStates.cannalwide.get(),
                    "请输入${if (shape == FlowSectionShape.RECTANGLE) "截面宽度" else "截面上宽"}!",
                    "请输入正确的${if (shape == FlowSectionShape.RECTANGLE) "截面宽度" else "截面上宽"}!"
                )
            ) {
                return false
            }
        }

        //梯形特有参数校验
        if (shape == FlowSectionShape.TRAPEZOID) {
            if (!requireDecimal(
                    mStates.bottomwide.get(),
                    "请输入截面下宽!",
                    "请输入正确的截面下宽!"
                )
            ) {
                return false
            }
        }

        if (shape == FlowSectionShape.RECTANGLE || shape == FlowSectionShape.TRAPEZOID || shape == FlowSectionShape.U_SHAPE) {
            if (!requireDecimal(
                    mStates.maxdepth.get(),
                    "请输入截面深度!",
                    "请输入正确的截面深度!"
                )
            ) {
                return false
            }
        }

        //梯形特有参数校验
        if (shape == FlowSectionShape.TRAPEZOID) {
            if (!requireDecimal(
                    mStates.sloperatio.get(),
                    "请输入边坡系数!",
                    "请输入正确的边坡系数!"
                )
            ) {
                return false
            }
        }

        if (shape == FlowSectionShape.CIRCLE || shape == FlowSectionShape.U_SHAPE) {
            if (!requireDecimal(
                    mStates.diameter.get(),
                    "请输入${if (shape == FlowSectionShape.U_SHAPE) "U型直径" else "截面直径"}!",
                    "请输入正确的${if (shape == FlowSectionShape.U_SHAPE) "U型直径" else "截面直径"}!"
                )
            ) {
                return false
            }
        }

        if (!requireDecimal(mStates.initdepth.get(), "请输入初始水深!", "请输入正确的初始水深!")) {
            return false
        }
        if (!requireDecimal(mStates.initheight.get(), "请输入初始空高!", "请输入正确的初始空高!")) {
            return false
        }

        val hcor = mStates.hcorvalue.get().trim()
        if (hcor.isNotEmpty() && hcor.toDoubleOrNull() == null) {
            showMessageDialog("请输入正确的空高修正值!")
            return false
        }

        return true
    }

    private fun sendSaveCommand() {
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_MUD_LEVEL_METER_SENSOR,
            buildCommandParameter()
        )
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    private fun buildCommandParameter(): String {
        val shape = mStates.selectedShape.get()
        val params = mutableListOf("shape=${shape.value}")

        when (shape) {
            FlowSectionShape.CUSTOM -> {
                params.add("customize=${formatValue(mStates.customize.get())}")
            }

            FlowSectionShape.RECTANGLE -> {
                params.add("cannalwide=${formatValue(mStates.cannalwide.get())}")
                params.add("maxdepth=${formatValue(mStates.maxdepth.get())}")
            }

            FlowSectionShape.TRAPEZOID -> {
                params.add("cannalwide=${formatValue(mStates.cannalwide.get())}")
                params.add("bottomwide=${formatValue(mStates.bottomwide.get())}")
                params.add("maxdepth=${formatValue(mStates.maxdepth.get())}")
                params.add("sloperatio=${formatValue(mStates.sloperatio.get())}")
            }

            FlowSectionShape.CIRCLE -> {
                params.add("diameter=${formatValue(mStates.diameter.get())}")
            }

            FlowSectionShape.U_SHAPE -> {
                params.add("diameter=${formatValue(mStates.diameter.get())}")
                params.add("maxdepth=${formatValue(mStates.maxdepth.get())}")
            }
        }

        params.add("initdepth=${formatValue(mStates.initdepth.get())}")
        params.add("initheight=${formatValue(mStates.initheight.get())}")
        params.add("hcorvalue=${formatValue(mStates.hcorvalue.get(), true)}")

        return params.joinToString("&")
    }

    private fun formatValue(value: String, allowEmpty: Boolean = false): String {
        val content = value.trim()
        if (content.isEmpty()) {
            return if (allowEmpty) "0" else "0"
        }
        return content
    }

    private fun queryData() {
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_MUD_LEVEL_METER_SENSOR
        )
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false,
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_MUD_LEVEL_METER_SENSOR -> {
                when (val result =
                    iotParseManager.parse<UDFlowCalculationInfo>(
                        cmdStr,
                        IOTCommandType.MD_GET_MUD_LEVEL_METER_SENSOR
                    )
                ) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询流量计算参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initFlowCalculationData(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_MUD_LEVEL_METER_SENSOR -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "保存流量计算参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
                            processNavigateUp()
                        }
                    }
                }
            }

            else -> Timber.d("未处理的指令类型: ${IOTCommandUtil.extractCommandType(cmdStr)}")
        }
    }

    private fun initFlowCalculationData(info: UDFlowCalculationInfo) {
        try {
            val shapeValue = info.shape.trim().takeIf {
                it.isNotEmpty() && !it.equals(IOTConstants.NULL_KEY, true)
            }?.toIntOrNull() ?: FlowSectionShape.CUSTOM.value
            val shape = FlowSectionShape.fromValue(shapeValue)
            mStates.onShapeChanged(shape)

            mStates.customize.set(info.customize.normalizeField())
            mStates.cannalwide.set(info.cannalwide.normalizeField())
            mStates.initdepth.set(info.initdepth.normalizeField())
            mStates.initheight.set(info.initheight.normalizeField())
            mStates.maxdepth.set(info.maxdepth.normalizeField())
            mStates.bottomwide.set(info.bottomwide.normalizeField())
            mStates.sloperatio.set(info.sloperatio.normalizeField())
            mStates.diameter.set(info.diameter.normalizeField())
            mStates.hcorvalue.set(info.hcorvalue.normalizeField())

            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.message ?: "初始化流量计算参数失败")
        }
    }

    private fun String.normalizeField(): String {
        val content = trim()
        return if (content.isEmpty() || equals(IOTConstants.NULL_KEY, true)) {
            "0"
        } else {
            content
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
