package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.gt600

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600.EthernetConfigEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600.Net4GUseConfigEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.EthernetConfigData
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.Net4GUseData
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
import com.shmedo.mcloudapp.databinding.FragmentGt600NetworkConfigBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.GT600NetworkConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: GT600 网络配置页面
 *
 * 页面包含两个配置分组：
 * 1. 移动网络分组：
 *    - 4G通信开关（4G通信状态下禁止配置）
 *    - APN（暂无对应指令，仅展示）
 *    - 用户名（暂无对应指令，仅展示）
 *    - 密码（暂无对应指令，仅展示）
 *
 * 2. 以太网络分组：
 *    - IP分配（仅支持手动模式）
 *    - IP地址
 *    - 网关
 *    - 首选DNS
 *
 * 涉及指令：
 * - 查询4G开关: md_getnet4guse
 * - 设置4G开关: md_setnet4guse
 * - 查询以太网配置: md_geteth0
 * - 设置以太网配置: md_seteth0
 */
class GT600NetworkConfigFragment : OptimizedBaseIOTDeviceFragment() {

    private lateinit var binding: FragmentGt600NetworkConfigBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: GT600NetworkConfigViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    /** IP分配方式选项列表 */
    private val ipModeDisplayList = arrayListOf("手动")
    private val ipModeValueList = arrayListOf("0")

    // ========== 生命周期方法 ==========

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_gt600_network_config, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentGt600NetworkConfigBinding
        binding.llToolbar.toolbar.title = "网络配置"
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
        // 4G通信状态下禁止修改4G开关
        mStates.is4GDisabled.set(communicateWay is NetPlatformConnect)

        // 移动网络默认值
        mStates.is4GEnabled.set(true)
        mStates.apnName.set("")
        mStates.userName.set("")
        mStates.password.set("")

        // 以太网默认值
        mStates.ipMode.set(ipModeDisplayList[0])
        mStates.ipAddress.set("")
        mStates.gateway.set("")
        mStates.preferredDNS.set("")
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    // ========== 数据查询 ==========

    /**
     * 查询网络配置参数
     * 发送查询指令：4G开关 + 以太网配置
     */
    private fun queryData() {
        val commands = listOf(
            // 查询4G开关状态
            IOTCommandUtil.getCommand(IOTCommandType.MD_GET_NET_4G_USE),
            // 查询以太网配置
            IOTCommandUtil.getCommand(IOTCommandType.MD_GET_ETHERNET)
        )

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载对话框
                errorConfig = ErrorConfig(
                    strategy = ErrorHandlingStrategy.Dialog
                )
            )
        )
    }

    // ========== 数据保存 ==========

    /**
     * 保存配置
     * 发送设置指令
     */
    private fun saveConfiguration() {
        // 验证输入数据
        if (!validateInputData()) {
            return
        }

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
     */
    private fun validateInputData(): Boolean {
        // 验证IP地址
        if (mStates.ipAddress.get().isNotEmpty() && !RegexUtils.isIP(mStates.ipAddress.get())) {
            showMessageDialog("请输入有效的IP地址!")
            return false
        }
        // 验证网关
        if (mStates.gateway.get().isNotEmpty() && !RegexUtils.isIP(mStates.gateway.get())) {
            showMessageDialog("请输入有效的网关地址!")
            return false
        }
        // 验证DNS
        if (mStates.preferredDNS.get().isNotEmpty() && !RegexUtils.isIP(mStates.preferredDNS.get())) {
            showMessageDialog("请输入有效的DNS地址!")
            return false
        }
        return true
    }

    /**
     * 构建保存指令列表
     */
    private fun buildSaveCommands(): List<String> {
        val commands = mutableListOf<String>()

        // 4G开关设置指令（仅当开关未被禁用时发送）
        if (!mStates.is4GDisabled.get()) {
            val net4GEntity = Net4GUseConfigEntity(
                use = if (mStates.is4GEnabled.get()) "1" else "0"
            )
            commands.add(
                IOTCommandUtil.getCommand(
                    IOTCommandType.MD_SET_NET_4G_USE,
                    net4GEntity.toCommandString()
                )
            )
        }

        // 以太网配置设置指令
        val ethernetEntity = EthernetConfigEntity(
            dhcp = getValueFromDisplayList(
                mStates.ipMode.get(),
                ipModeDisplayList,
                ipModeValueList,
                "0"
            ),
            ip = mStates.ipAddress.get(),
            gateway = mStates.gateway.get(),
            dns = mStates.preferredDNS.get()
        )
        commands.add(
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_ETHERNET,
                ethernetEntity.toCommandString()
            )
        )

        return commands
    }

    /**
     * 从显示列表中获取对应的实际值
     */
    private fun getValueFromDisplayList(
        displayValue: String,
        displayList: List<String>,
        valueList: List<String>,
        defaultValue: String
    ): String {
        val index = displayList.indexOf(displayValue)
        return if (index in valueList.indices) valueList[index] else defaultValue
    }

    // ========== 响应处理 ==========

    /**
     * 处理指令响应
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_NET_4G_USE -> {
                handleNet4GUseQueryResponse(cmdStr)
            }

            IOTCommandType.MD_GET_ETHERNET -> {
                handleEthernetConfigQueryResponse(cmdStr)
            }

            IOTCommandType.MD_SET_NET_4G_USE -> {
                handleNet4GUseSaveResponse(cmdStr)
            }

            IOTCommandType.MD_SET_ETHERNET -> {
                handleEthernetConfigSaveResponse(cmdStr)
            }

            else -> {
                Timber.d("未处理的指令类型: ${IOTCommandUtil.extractCommandType(cmdStr)}")
            }
        }
    }

    /**
     * 处理4G开关查询响应
     */
    private fun handleNet4GUseQueryResponse(cmdStr: String) {
        val result = iotParseManager.parse<Net4GUseData>(cmdStr, IOTCommandType.MD_GET_NET_4G_USE)
        when (result) {
            is IOTCommandResult.Failure -> {
                val errMsg = "查询4G开关出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            is IOTCommandResult.Success -> {
                initNet4GUseData(result.data)
            }
        }
    }

    /**
     * 处理以太网配置查询响应
     */
    private fun handleEthernetConfigQueryResponse(cmdStr: String) {
        val result = iotParseManager.parse<EthernetConfigData>(cmdStr, IOTCommandType.MD_GET_ETHERNET)
        when (result) {
            is IOTCommandResult.Failure -> {
                val errMsg = "查询以太网配置出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            is IOTCommandResult.Success -> {
                initEthernetConfigData(result.data)
            }
        }
    }

    /**
     * 处理4G开关设置响应
     */
    private fun handleNet4GUseSaveResponse(cmdStr: String) {
        when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
            is IOTCommandResult.Failure -> {
                val errMsg = "设置4G开关出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            else -> {
                if (!isCommunicationExecuting()) processNavigateUp()
            }
        }
    }

    /**
     * 处理以太网配置设置响应
     */
    private fun handleEthernetConfigSaveResponse(cmdStr: String) {
        when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
            is IOTCommandResult.Failure -> {
                val errMsg = "设置以太网配置出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            else -> {
                if (!isCommunicationExecuting()) processNavigateUp()
            }
        }
    }

    // ========== 数据初始化 ==========

    /**
     * 初始化4G开关数据
     */
    private fun initNet4GUseData(data: Net4GUseData) {
        try {
            mStates.is4GEnabled.set(data.use == "1")
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e, "初始化4G开关数据出错")
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 初始化以太网配置数据
     */
    private fun initEthernetConfigData(data: EthernetConfigData) {
        try {
            // IP分配方式
            val dhcpIndex = ipModeValueList.indexOf(data.dhcp)
            if (dhcpIndex in ipModeDisplayList.indices) {
                mStates.ipMode.set(ipModeDisplayList[dhcpIndex])
            }
            mStates.ipAddress.set(data.ip)
            mStates.gateway.set(data.gateway)
            mStates.preferredDNS.set(data.dns)
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e, "初始化以太网配置数据出错")
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    // ========== 点击事件处理 ==========

    inner class ClickProxy : BaseClickProxy() {

        /**
         * IP分配方式选择
         */
        fun onIPModeChooseClick() {
            val selectedIndex = ipModeDisplayList.indexOf(mStates.ipMode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "请选择分配方式", ipModeDisplayList.toTypedArray(),
                    null, selectedIndex,
                    { _, text ->
                        mStates.ipMode.set(text)
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

    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }
}
