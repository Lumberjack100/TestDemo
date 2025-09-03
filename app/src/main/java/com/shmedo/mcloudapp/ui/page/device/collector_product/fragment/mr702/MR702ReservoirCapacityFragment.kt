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
            val xValue = when (i) {
                1 -> mStates.x1.get()
                2 -> mStates.x2.get()
                3 -> mStates.x3.get()
                4 -> mStates.x4.get()
                5 -> mStates.x5.get()
                6 -> mStates.x6.get()
                7 -> mStates.x7.get()
                8 -> mStates.x8.get()
                9 -> mStates.x9.get()
                10 -> mStates.x10.get()
                11 -> mStates.x11.get()
                12 -> mStates.x12.get()
                13 -> mStates.x13.get()
                14 -> mStates.x14.get()
                15 -> mStates.x15.get()
                16 -> mStates.x16.get()
                17 -> mStates.x17.get()
                18 -> mStates.x18.get()
                19 -> mStates.x19.get()
                20 -> mStates.x20.get()
                21 -> mStates.x21.get()
                22 -> mStates.x22.get()
                23 -> mStates.x23.get()
                24 -> mStates.x24.get()
                25 -> mStates.x25.get()
                26 -> mStates.x26.get()
                27 -> mStates.x27.get()
                28 -> mStates.x28.get()
                29 -> mStates.x29.get()
                30 -> mStates.x30.get()
                31 -> mStates.x31.get()
                32 -> mStates.x32.get()
                33 -> mStates.x33.get()
                34 -> mStates.x34.get()
                35 -> mStates.x35.get()
                36 -> mStates.x36.get()
                37 -> mStates.x37.get()
                38 -> mStates.x38.get()
                39 -> mStates.x39.get()
                40 -> mStates.x40.get()
                41 -> mStates.x41.get()
                42 -> mStates.x42.get()
                43 -> mStates.x43.get()
                44 -> mStates.x44.get()
                45 -> mStates.x45.get()
                46 -> mStates.x46.get()
                47 -> mStates.x47.get()
                48 -> mStates.x48.get()
                49 -> mStates.x49.get()
                50 -> mStates.x50.get()
                else -> ""
            }

            val yValue = when (i) {
                1 -> mStates.y1.get()
                2 -> mStates.y2.get()
                3 -> mStates.y3.get()
                4 -> mStates.y4.get()
                5 -> mStates.y5.get()
                6 -> mStates.y6.get()
                7 -> mStates.y7.get()
                8 -> mStates.y8.get()
                9 -> mStates.y9.get()
                10 -> mStates.y10.get()
                11 -> mStates.y11.get()
                12 -> mStates.y12.get()
                13 -> mStates.y13.get()
                14 -> mStates.y14.get()
                15 -> mStates.y15.get()
                16 -> mStates.y16.get()
                17 -> mStates.y17.get()
                18 -> mStates.y18.get()
                19 -> mStates.y19.get()
                20 -> mStates.y20.get()
                21 -> mStates.y21.get()
                22 -> mStates.y22.get()
                23 -> mStates.y23.get()
                24 -> mStates.y24.get()
                25 -> mStates.y25.get()
                26 -> mStates.y26.get()
                27 -> mStates.y27.get()
                28 -> mStates.y28.get()
                29 -> mStates.y29.get()
                30 -> mStates.y30.get()
                31 -> mStates.y31.get()
                32 -> mStates.y32.get()
                33 -> mStates.y33.get()
                34 -> mStates.y34.get()
                35 -> mStates.y35.get()
                36 -> mStates.y36.get()
                37 -> mStates.y37.get()
                38 -> mStates.y38.get()
                39 -> mStates.y39.get()
                40 -> mStates.y40.get()
                41 -> mStates.y41.get()
                42 -> mStates.y42.get()
                43 -> mStates.y43.get()
                44 -> mStates.y44.get()
                45 -> mStates.y45.get()
                46 -> mStates.y46.get()
                47 -> mStates.y47.get()
                48 -> mStates.y48.get()
                49 -> mStates.y49.get()
                50 -> mStates.y50.get()
                else -> ""
            }

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
        val xValues = data.xparam.split(",")
        val yValues = data.yparam.split(",")

        // Set x coordinates
        if (xValues.isNotEmpty()) mStates.x1.set(xValues[0])
        if (xValues.size >= 2) mStates.x2.set(xValues[1])
        if (xValues.size >= 3) mStates.x3.set(xValues[2])
        if (xValues.size >= 4) mStates.x4.set(xValues[3])
        if (xValues.size >= 5) mStates.x5.set(xValues[4])
        if (xValues.size >= 6) mStates.x6.set(xValues[5])
        if (xValues.size >= 7) mStates.x7.set(xValues[6])
        if (xValues.size >= 8) mStates.x8.set(xValues[7])
        if (xValues.size >= 9) mStates.x9.set(xValues[8])
        if (xValues.size >= 10) mStates.x10.set(xValues[9])
        if (xValues.size >= 11) mStates.x11.set(xValues[10])
        if (xValues.size >= 12) mStates.x12.set(xValues[11])
        if (xValues.size >= 13) mStates.x13.set(xValues[12])
        if (xValues.size >= 14) mStates.x14.set(xValues[13])
        if (xValues.size >= 15) mStates.x15.set(xValues[14])
        if (xValues.size >= 16) mStates.x16.set(xValues[15])
        if (xValues.size >= 17) mStates.x17.set(xValues[16])
        if (xValues.size >= 18) mStates.x18.set(xValues[17])
        if (xValues.size >= 19) mStates.x19.set(xValues[18])
        if (xValues.size >= 20) mStates.x20.set(xValues[19])
        if (xValues.size >= 21) mStates.x21.set(xValues[20])
        if (xValues.size >= 22) mStates.x22.set(xValues[21])
        if (xValues.size >= 23) mStates.x23.set(xValues[22])
        if (xValues.size >= 24) mStates.x24.set(xValues[23])
        if (xValues.size >= 25) mStates.x25.set(xValues[24])
        if (xValues.size >= 26) mStates.x26.set(xValues[25])
        if (xValues.size >= 27) mStates.x27.set(xValues[26])
        if (xValues.size >= 28) mStates.x28.set(xValues[27])
        if (xValues.size >= 29) mStates.x29.set(xValues[28])
        if (xValues.size >= 30) mStates.x30.set(xValues[29])
        if (xValues.size >= 31) mStates.x31.set(xValues[30])
        if (xValues.size >= 32) mStates.x32.set(xValues[31])
        if (xValues.size >= 33) mStates.x33.set(xValues[32])
        if (xValues.size >= 34) mStates.x34.set(xValues[33])
        if (xValues.size >= 35) mStates.x35.set(xValues[34])
        if (xValues.size >= 36) mStates.x36.set(xValues[35])
        if (xValues.size >= 37) mStates.x37.set(xValues[36])
        if (xValues.size >= 38) mStates.x38.set(xValues[37])
        if (xValues.size >= 39) mStates.x39.set(xValues[38])
        if (xValues.size >= 40) mStates.x40.set(xValues[39])
        if (xValues.size >= 41) mStates.x41.set(xValues[40])
        if (xValues.size >= 42) mStates.x42.set(xValues[41])
        if (xValues.size >= 43) mStates.x43.set(xValues[42])
        if (xValues.size >= 44) mStates.x44.set(xValues[43])
        if (xValues.size >= 45) mStates.x45.set(xValues[44])
        if (xValues.size >= 46) mStates.x46.set(xValues[45])
        if (xValues.size >= 47) mStates.x47.set(xValues[46])
        if (xValues.size >= 48) mStates.x48.set(xValues[47])
        if (xValues.size >= 49) mStates.x49.set(xValues[48])
        if (xValues.size >= 50) mStates.x50.set(xValues[49])

        // Set y coordinates
        if (yValues.isNotEmpty()) mStates.y1.set(yValues[0])
        if (yValues.size >= 2) mStates.y2.set(yValues[1])
        if (yValues.size >= 3) mStates.y3.set(yValues[2])
        if (yValues.size >= 4) mStates.y4.set(yValues[3])
        if (yValues.size >= 5) mStates.y5.set(yValues[4])
        if (yValues.size >= 6) mStates.y6.set(yValues[5])
        if (yValues.size >= 7) mStates.y7.set(yValues[6])
        if (yValues.size >= 8) mStates.y8.set(yValues[7])
        if (yValues.size >= 9) mStates.y9.set(yValues[8])
        if (yValues.size >= 10) mStates.y10.set(yValues[9])
        if (yValues.size >= 11) mStates.y11.set(yValues[10])
        if (yValues.size >= 12) mStates.y12.set(yValues[11])
        if (yValues.size >= 13) mStates.y13.set(yValues[12])
        if (yValues.size >= 14) mStates.y14.set(yValues[13])
        if (yValues.size >= 15) mStates.y15.set(yValues[14])
        if (yValues.size >= 16) mStates.y16.set(yValues[15])
        if (yValues.size >= 17) mStates.y17.set(yValues[16])
        if (yValues.size >= 18) mStates.y18.set(yValues[17])
        if (yValues.size >= 19) mStates.y19.set(yValues[18])
        if (yValues.size >= 20) mStates.y20.set(yValues[19])
        if (yValues.size >= 21) mStates.y21.set(yValues[20])
        if (yValues.size >= 22) mStates.y22.set(yValues[21])
        if (yValues.size >= 23) mStates.y23.set(yValues[22])
        if (yValues.size >= 24) mStates.y24.set(yValues[23])
        if (yValues.size >= 25) mStates.y25.set(yValues[24])
        if (yValues.size >= 26) mStates.y26.set(yValues[25])
        if (yValues.size >= 27) mStates.y27.set(yValues[26])
        if (yValues.size >= 28) mStates.y28.set(yValues[27])
        if (yValues.size >= 29) mStates.y29.set(yValues[28])
        if (yValues.size >= 30) mStates.y30.set(yValues[29])
        if (yValues.size >= 31) mStates.y31.set(yValues[30])
        if (yValues.size >= 32) mStates.y32.set(yValues[31])
        if (yValues.size >= 33) mStates.y33.set(yValues[32])
        if (yValues.size >= 34) mStates.y34.set(yValues[33])
        if (yValues.size >= 35) mStates.y35.set(yValues[34])
        if (yValues.size >= 36) mStates.y36.set(yValues[35])
        if (yValues.size >= 37) mStates.y37.set(yValues[36])
        if (yValues.size >= 38) mStates.y38.set(yValues[37])
        if (yValues.size >= 39) mStates.y39.set(yValues[38])
        if (yValues.size >= 40) mStates.y40.set(yValues[39])
        if (yValues.size >= 41) mStates.y41.set(yValues[40])
        if (yValues.size >= 42) mStates.y42.set(yValues[41])
        if (yValues.size >= 43) mStates.y43.set(yValues[42])
        if (yValues.size >= 44) mStates.y44.set(yValues[43])
        if (yValues.size >= 45) mStates.y45.set(yValues[44])
        if (yValues.size >= 46) mStates.y46.set(yValues[45])
        if (yValues.size >= 47) mStates.y47.set(yValues[46])
        if (yValues.size >= 48) mStates.y48.set(yValues[47])
        if (yValues.size >= 49) mStates.y49.set(yValues[48])
        if (yValues.size >= 50) mStates.y50.set(yValues[49])

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

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}