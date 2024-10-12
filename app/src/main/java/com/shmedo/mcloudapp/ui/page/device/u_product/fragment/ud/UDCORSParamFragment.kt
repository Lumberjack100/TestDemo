package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.u_product.UDCORSParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UDCORSParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentUdCorsParamBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDCORSParamViewModel
import org.koin.android.ext.android.inject

/**
 * @author：gonghe
 * @time: 2024/8/28
 * @desc:  一体化雷达泥位计RTK测高参数
 *
 */
class UDCORSParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUdCorsParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: UDCORSParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_ud_cors_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUdCorsParamBinding
        toolbarViewModel.toolbarTitleText.set("海拔高度")
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryData()
        }
    }

    override fun initData() {
        super.initData()
        resetDefaultParams()
    }

    private fun resetDefaultParams() {
        mStates.isOpened.set(false)
        mStates.domain.set("rtk.ntrip.qxwz.com")
        mStates.port.set("8002")
        mStates.diffAccount.set("")
        mStates.diffPassword.set("")
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            mStates.isOpened.set(isChecked)
            if (!isChecked) {
                showMessage("确定要关闭吗？", "温馨提示", "确定", {
                    disableCORS()
                }, "取消", {
                    mStates.isOpened.set(true)
                    (button as SwitchButton).setCheckedImmediatelyNoEvent(true)
                })
            }
        }

        /**
         * 更新海拔高度
         */
        fun onMeasureHeightClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            commandItems.clear()
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_MUD_LEVEL_METER_DIFF_LOCATE,
                "switch=1"
            )
            commandItems.add(command)

            showLoadingDialog(StringUtils.getString(R.string.processing))
            sendCommandFromCmdList(isStartTimeoutJob = true)
        }

        /**
         * 恢复默认配置
         */
        fun onResetClick() {
            resetDefaultParams()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    /**
     * 关闭
     */
    private fun disableCORS() {
        commandItems.clear()

        val entity = UDCORSParamEntity(use = "0")
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_RTK,
            entity.toCommandString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initSaveCommand() {
        commandItems.clear()
        if (mStates.domain.get().isEmpty()) {
            showMessageDialog("请输入域名!")
            return
        }
        if (mStates.port.get().isEmpty()) {
            showMessageDialog("请输入端口!")
            return
        }

        val entity = UDCORSParamEntity(
            use = "1",
            host = mStates.domain.get(),
            port = mStates.port.get(),
            username = mStates.diffAccount.get(),
            password = mStates.diffPassword.get()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_RTK,
            entity.toCommandString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_RTK
        )
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_RTK -> {
                val result = iotParseManager.parse<UDCORSParam>(
                    cmdStr,
                    IOTCommandType.MD_GET_RTK
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initParam(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_MUD_LEVEL_METER_DIFF_LOCATE -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "更新海拔高度出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("更新海拔高度成功")
                        }
                    }
                }
            }

            IOTCommandType.MD_SET_RTK -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("保存成功")
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initParam(info: UDCORSParam) {
        mStates.isOpened.set(info.use == "1")
        mStates.domain.set(info.host)
        mStates.port.set(info.port)
        mStates.diffAccount.set(info.username)
        mStates.diffPassword.set(info.password)
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}