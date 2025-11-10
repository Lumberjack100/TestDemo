package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702

import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRReservoirCapacityEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRReservoirCapacity
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.communication.model.ErrorHandlingStrategy
import com.shmedo.mcloudapp.databinding.FragmentMr702ReservoirCapacityBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702ReservoirCapacityViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject

/**
 * @author：gonghe
 * @time: 2025/1/23
 * @desc: 库容计算
 *
 */
class MR702ReservoirCapacityFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702ReservoirCapacityBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: MR702ReservoirCapacityViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_reservoir_capacity, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702ReservoirCapacityBinding
        binding.llToolbar.toolbar.title = "库容计算"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher {
            handleBackByCheckDataModified()
        }
        initRefresh()
        initOtherListener()
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

    override fun initData() {
        super.initData()
        resetDefaultParams()
        //添加这行来保存初始状态
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        mStates.isOpened.set(true)
        mStates.pointCount.set("3")
        mStates.resetAllPoints()
    }

    private fun initOtherListener() {
        binding.etPointCount.doAfterTextChanged { text: Editable? ->
            if (text.isNullOrEmpty()) {
                return@doAfterTextChanged
            }
            val count = text.toString().toInt()
            if (count > 50 || count < 3) {
                showMessageDialog("坐标点数量有效值为 [3,50]！")
            }
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 恢复默认配置
         */
        fun onResetClick() {
            resetDefaultParams()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            if (!mStates.isOpened.get()) {
                disable()
                return
            }
            initSaveCommand()
        }
    }

    /**
     * 关闭
     */
    private fun disable() {
        val commands = mutableListOf<String>()
        val entity = MRReservoirCapacityEntity(
            switch = "0",
            count = mStates.pointCount.get(),
            xparam = "",
            yparam = ""
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_RESERVOIR_CAPACITY,
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

    private fun initSaveCommand() {
        val commands = mutableListOf<String>()

        if (mStates.pointCount.get().isEmpty()) {
            Toaster.show("请输入坐标点数量")
            return
        }
        val pointCount = mStates.pointCount.get().toInt()
        // Build x and y parameter strings
        val xParams = StringBuilder()
        val yParams = StringBuilder()

        for (i in 1..pointCount) {
            val xValue = mStates.getXPoint(i).get()
            val yValue = mStates.getYPoint(i).get()

            if (i > 1) {
                xParams.append(",")
                yParams.append(",")
            }
            xParams.append(xValue)
            yParams.append(yValue)
        }

        val entity = MRReservoirCapacityEntity(
            switch = "1",
            count = mStates.pointCount.get(),
            xparam = xParams.toString(),
            yparam = yParams.toString()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_RESERVOIR_CAPACITY,
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
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        val commands = mutableListOf<String>()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_RESERVOIR_CAPACITY)
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
            IOTCommandType.MR_MD_GET_RESERVOIR_CAPACITY -> {
                val result = iotParseManager.parse<MRReservoirCapacity>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_RESERVOIR_CAPACITY
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initParam(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_SET_RESERVOIR_CAPACITY -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
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

            else -> {
            }
        }
    }

    private fun initParam(data: MRReservoirCapacity) {
        mStates.isOpened.set(data.switch == "1")
        mStates.pointCount.set(if (data.count.isEmpty() || data.count.toInt() < 3) "3" else data.count)

        // Split x and y coordinates
        val xValues = data.xparam.takeIf { it.isNotEmpty() }?.split(",") ?: emptyList()
        val yValues = data.yparam.takeIf { it.isNotEmpty() }?.split(",") ?: emptyList()

        mStates.resetAllPoints()

        xValues.take(MR702ReservoirCapacityViewModel.MAX_POINT_COUNT).forEachIndexed { index, value ->
            mStates.getXPoint(index + 1).set(value)
        }

        yValues.take(MR702ReservoirCapacityViewModel.MAX_POINT_COUNT).forEachIndexed { index, value ->
            mStates.getYPoint(index + 1).set(value)
        }

        // 保存初始状态
        mStates.saveInitialState()
    }

    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }

}
