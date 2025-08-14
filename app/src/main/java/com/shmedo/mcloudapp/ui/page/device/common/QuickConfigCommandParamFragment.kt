package com.shmedo.mcloudapp.ui.page.device.common

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.UriUtils
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.toast.Toaster
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import com.shmedo.core.model.CmdParamInfo
import com.shmedo.core.model.DeviceCmdOrderInfo
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandResult
import com.shmedo.mcloudapp.communication.model.CommandSequenceCallbacks
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentQuickConfigCommandParamBinding
import com.shmedo.mcloudapp.databinding.ItemQuickConfigCommandParamEditBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.CustomActivityResult
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.ParamSubmitButtonItem
import com.shmedo.mcloudapp.model.QuickConfigCommandParamChooseItem
import com.shmedo.mcloudapp.model.QuickConfigCommandParamEditItem
import com.shmedo.mcloudapp.ui.dialog.CommandExecutionProgressDialog
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.ProductConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.CommandExecutionProgress
import com.shmedo.mcloudapp.ui.viewmodel.state.CommandExecutionResult
import com.shmedo.mcloudapp.ui.viewmodel.state.ExecutionStatus
import com.shmedo.mcloudapp.ui.viewmodel.state.QuickConfigCommandParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.utils.CommandExcelExporter
import com.shmedo.mcloudapp.utils.permission.PermissionHelper
import com.shmedo.mcloudapp.utils.permission.PermissionInterceptor
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/1/6
 * @desc: 快速配置设备指令参数，一键下发
 *
 * 主要功能：
 * 1. 扫描二维码获取配置模板
 * 2. 动态生成参数配置UI
 * 3. 一键下发指令序列
 * 4. 实时显示执行进度
 */
class QuickConfigCommandParamFragment : OptimizedBaseIOTDeviceFragment() {

    private lateinit var binding: FragmentQuickConfigCommandParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: QuickConfigCommandParamViewModel by viewModels()
    private val productConfigViewModel: ProductConfigViewModel by viewModel()

    // 进度对话框
    private var executionProgressDialog: BasePopupView? = null
    private var executionProgressDialogInstance: CommandExecutionProgressDialog? = null

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_quick_config_command_param,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.stateVM, mStates)
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentQuickConfigCommandParamBinding
        binding.llToolbar.toolbar.title = "一键快速配置参数"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
            nav().navigateUp()
        }

        // 添加扫码菜单
        binding.llToolbar.toolbar.inflateMenu(R.menu.quick_config_menu)
        binding.llToolbar.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_scan_qr -> {
                    startQRCodeScan()
                    true
                }

                else -> false
            }
        }

        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }

        initAdapter()
    }

    override fun initData() {
        super.initData()

        // 检查是否有传入的URL参数（用于测试）
        arguments?.getString("qr_url")?.let { url ->
            handleScanResult(url)
        }
    }

    override fun createObserver() {
        super.createObserver()

        // 观察扫码结果 - 只处理快速配置的扫码结果
        mMessenger.activityResultDispatcher.observe(
            viewLifecycleOwner
        ) { result: CustomActivityResult ->
            if (result.resultCode == Activity.RESULT_OK &&
                result.requestCode == PermissionHelper.REQUEST_CODE_QUICK_CONFIG_SCAN &&
                result.data != null
            ) {
                val obj: HmsScan? = result.data.getParcelableExtra<HmsScan>(ScanUtil.RESULT)
                if (obj == null) {
                    Toaster.show("扫码结果为空")
                    return@observe
                }
                Timber.d("快速配置扫码结果：${obj.originalValue}")
                handleScanResult(obj.originalValue)
            }
        }

        // 观察配置数据变化
        mStates.cmdOrderInfo.observe(viewLifecycleOwner) { cmdOrderInfo ->
            cmdOrderInfo?.let {
                buildConfigurationUI(it)
            }
        }
    }

    private fun initAdapter() {
        binding.recyclerView.linear().setup {
            addType<DeviceStatusInfoGroupItem>(R.layout.item_device_status_info_group)
            addType<QuickConfigCommandParamChooseItem>(R.layout.item_quick_config_command_param_choose)
            addType<QuickConfigCommandParamEditItem>(R.layout.item_quick_config_command_param_edit)
            addType<GapItem>(R.layout.item_device_status_info_gap)
            addType<ParamSubmitButtonItem>(R.layout.item_param_summit_button)
            onBind {
                when (itemViewType) {
                    R.layout.item_quick_config_command_param_edit -> {
                        val item = getModel<QuickConfigCommandParamEditItem>()
                        val itemBinding = getBinding<ItemQuickConfigCommandParamEditBinding>()
                        // 新增：监听 EditText 输入变化
                        itemBinding.etValue.doAfterTextChanged { text: Editable? ->
                            if (text.isNullOrEmpty()) {
                                return@doAfterTextChanged
                            }
                            val newValue = text.toString()
                            mStates.parameterValues[item.cmdEngName] = newValue
                            Timber.d("参数更新: ${item.cmdEngName} = $newValue")
                        }
                    }

                    else -> {}
                }
            }

            R.id.item.onClick {
                when (itemViewType) {
                    R.layout.item_quick_config_command_param_choose -> {
                        val item = getModel<QuickConfigCommandParamChooseItem>()
                        onChooseItemClick(item)
                    }
                }
            }

            R.id.btn_submit.onClick {
                KeyboardUtils.hideSoftInput(binding.root)
                if (!isDeviceConnected()) {
                    Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                    return@onClick
                }
                executeConfiguration()
            }
        }
    }

    override fun lazyLoadData() {
        // 如果有默认配置URL，可以在这里加载
    }

    //<editor-fold desc="二维码扫描、图片识别功能">
    /**
     * 启动二维码扫描
     */
    private fun startQRCodeScan() {
        XXPermissions.with(this)
            .permission(Permission.CAMERA)
            // 设置权限请求拦截器（局部设置）
            .interceptor(PermissionInterceptor())
            .request(object : OnPermissionCallback {
                override fun onGranted(
                    grantedPermissions: MutableList<String>, allGranted: Boolean
                ) {
                    if (!allGranted) {
                        return
                    }
                    // 扫一扫 - 使用快速配置专用的REQUEST_CODE
                    val options = HmsScanAnalyzerOptions.Creator()
//                        .setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE)
//                        .setViewType(1)
                        .setErrorCheck(true)
                        .create()

                    ScanUtil.startScan(
                        mActivity, PermissionHelper.REQUEST_CODE_QUICK_CONFIG_SCAN, options
                    )
                }
            })
    }

    /**
     * 处理扫码结果
     */
    private fun handleScanResult(qrCodeUrl: String) {
        Timber.i("扫码结果: $qrCodeUrl")

        if (validateQRCodeUrl(qrCodeUrl)) {
            fetchCommandConfiguration(qrCodeUrl)
        } else {
            Toaster.show("无效的配置二维码")
        }
    }

    /**
     * 验证URL格式
     */
    private fun validateQRCodeUrl(url: String): Boolean {
        // URL规则：http://ams4.shmedo.com:22000/api/v1/GetCmdOrdersBySn/{suffix}
        val pattern = Regex("^https?://.*/api/v1/GetCmdOrdersBySn/[\\w]+$")
        return pattern.matches(url)
    }

    /**
     * 提取验证后缀
     */
    private fun extractVerificationSuffix(url: String): String? {
        return url.substringAfterLast("/").takeIf { it.isNotBlank() }
    }
    // </editor-fold>

    // ==================== 配置数据获取 ====================
    /**
     * 获取指令配置
     */
    private fun fetchCommandConfiguration(qrCodeUrl: String) {
        launchWithViewLifecycle {
            try {
//                showLoadingDialog("正在获取配置信息...")

                val verificationSuffix = extractVerificationSuffix(qrCodeUrl)
                    ?: throw IllegalArgumentException("无效的URL格式")

                val params = mapOf("sn" to deviceInfo.deviceToken)

                Timber.d("请求配置: suffix=$verificationSuffix, sn=${deviceInfo.deviceToken}")

                val cmdOrderInfo = productConfigViewModel.getDeviceGetCmdOrdersBySn(
                    verificationSuffix = verificationSuffix,
                    param = params
                ) { error: Throwable ->
                    showError("获取配置失败：${error.message}")
                }

                if (cmdOrderInfo != null) {
                    Timber.i("配置获取成功: ${cmdOrderInfo.productName}")
                    mStates.cmdOrderInfo.value = cmdOrderInfo
                }

            } catch (error: Exception) {
                Timber.e(error, "获取配置失败")
                showError("获取配置失败：${error.message}")
            } finally {
//                dismissLoadingDialog()
            }
        }
    }

    // ==================== UI构建 ====================

    /**
     * 构建配置UI
     */
    private fun buildConfigurationUI(cmdOrderInfo: DeviceCmdOrderInfo) {
        val uiItems = mutableListOf<Any>()

        // 合并并排序动态指令
        val commandItems = mergeAndSortDynamicCommands(cmdOrderInfo)

        // 构建UI项
        commandItems.forEachIndexed { index, cmdItem ->
            if (index > 0) {
                uiItems.add(GapItem(height = ConvertUtils.dp2px(12f)))
            }

            // 添加分组标题项
            uiItems.add(
                DeviceStatusInfoGroupItem(cmdItem.note)
            )

            // 添加参数配置项
            if (!cmdItem.isFixed && cmdItem.parameters.isNotEmpty()) {
                addParameterItems(uiItems, cmdItem)
            }
        }

        // 添加提交按钮
        uiItems.add(GapItem(height = ConvertUtils.dp2px(60f)))
        uiItems.add(ParamSubmitButtonItem(btnText = "一键配置"))

        mStates.emptyContent.set(uiItems.isEmpty())
        binding.recyclerView.models = uiItems
    }

    /**
     * 合并和排序指令
     */
    private fun mergeAndSortDynamicCommands(cmdOrderInfo: DeviceCmdOrderInfo): List<CommandItem> {
        val commandItems = mutableListOf<CommandItem>()

        // 添加可配置指令
        cmdOrderInfo.dynamicCmds.forEach { cmdOrder ->
            commandItems.add(
                CommandItem(
                    command = cmdOrder.cmd,
                    orderIndex = cmdOrder.orderIndex,
                    isFixed = false,
                    note = cmdOrder.note.ifBlank { "可配置指令" },
                    parameters = cmdOrder.cmdParaInfos
                )
            )
        }

        // 按orderIndex排序
        return commandItems.sortedBy { it.orderIndex }
    }

    /**
     * 添加参数配置项
     */
    private fun addParameterItems(uiItems: MutableList<Any>, cmdItem: CommandItem) {
        // 只处理动态参数（isVarData为true的参数）
        val dynamicParameters = cmdItem.parameters.filter { it.isVarData }

        dynamicParameters.forEachIndexed { paramIndex, param ->
            val bgResId = if (paramIndex == dynamicParameters.lastIndex) {
                R.drawable.shape_common_click_item_bottom_corner_4
            } else {
                R.drawable.layer_common_click_item_with_divider
            }

            when (param.fieldType) {
                "字符" -> {
                    val editItem = QuickConfigCommandParamEditItem(
                        cmdChnName = param.cmdChnName,
                        cmdEngName = param.cmdEngName,
                        value = param.defaultValue,
                        bgResId = bgResId
                    )
                    // 设置默认值
                    mStates.parameterValues[param.cmdEngName] = param.defaultValue
                    uiItems.add(editItem)
                }

                "选择" -> {
                    val fieldValues = param.fieldValueInfos?.toMutableList()
                        ?: mutableListOf()
                    val valueMap = fieldValues.associate { it.value to it.display }

                    val chooseItem = QuickConfigCommandParamChooseItem(
                        cmdChnName = param.cmdChnName,
                        cmdEngName = param.cmdEngName,
                        displayValue = valueMap[param.defaultValue]
                            ?: fieldValues.firstOrNull()?.display ?: "",
                        cmdValue = param.defaultValue,
                        fieldValueInfos = fieldValues,
                        bgResId = bgResId
                    )
                    // 设置默认值
                    mStates.parameterValues[param.cmdEngName] = param.defaultValue
                    uiItems.add(chooseItem)
                }

                else -> {
                    Timber.w("未知的字段类型: ${param.fieldType}")
                }
            }
        }
    }

    /**
     * 处理选择项点击
     */
    private fun onChooseItemClick(item: QuickConfigCommandParamChooseItem) {
        val tempMap = item.fieldValueInfos.associate { it.display to it.value }
        val displayList = item.fieldValueInfos.map { it.display }
        val selectedIndex = displayList.indexOf(item.displayValue)

        XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
        XPopup.Builder(context)
            .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
            .isDestroyOnDismiss(true)
            .enableDrag(false)
            .asBottomList(
                item.cmdChnName,
                displayList.toTypedArray(),
                null,
                selectedIndex,
                { position, displayName ->
                    val selectedValue = tempMap[displayName] ?: ""
                    item.refreshValue(displayName, selectedValue)
                    // 保存选择的值
                    mStates.parameterValues[item.cmdEngName] = selectedValue
                    Timber.d("选择更新: ${item.cmdEngName} = $selectedValue ($displayName)")
                },
                0,
                R.layout.custom_xpopup_adapter_text_center
            )
            .show()
    }

    // ==================== 指令执行 ====================

    /**
     * 验证参数
     */
    private fun validateParameters(): Boolean {
        val cmdOrderInfo = mStates.cmdOrderInfo.value ?: return false

        cmdOrderInfo.dynamicCmds.forEach { dynamicCmdInfo ->
            val dynamicParameters = dynamicCmdInfo.cmdParaInfos.filter { it.isVarData }
            dynamicParameters.forEach { param ->
                val value = mStates.parameterValues[param.cmdEngName]
                if (value.isNullOrBlank() && param.fieldType == "字符") {
                    Toaster.show("请填写${param.cmdChnName}")
                    return false
                }
            }
        }

        return true
    }

    /**
     * 构建指令序列
     */
    private fun buildCommandSequence(): List<String> {
        val cmdOrderInfo = mStates.cmdOrderInfo.value ?: return emptyList()
        val commands = mutableListOf<Pair<Int, String>>()

        // 处理固定指令
        cmdOrderInfo.fixedCmds.forEach { fixedCmd ->
            commands.add(fixedCmd.orderIndex to fixedCmd.cmd)
        }

        // 处理可配置指令
        cmdOrderInfo.dynamicCmds.forEach { dynamicCmdInfo ->
            var processedCommand = dynamicCmdInfo.cmd

            val dynamicParameters = dynamicCmdInfo.cmdParaInfos.filter { it.isVarData }
            // 替换参数占位符
            dynamicParameters.forEach { param ->
                val paramKey = "{${param.cmdEngName}}"
                val paramValue = mStates.parameterValues[param.cmdEngName]
                    ?: param.defaultValue
                processedCommand = processedCommand.replace(paramKey, paramValue)
            }

            commands.add(dynamicCmdInfo.orderIndex to processedCommand)
        }

        // 按orderIndex排序并提取指令
        return commands.sortedBy { it.first }.map { it.second }
    }

    /**
     * 执行配置
     */
    private fun executeConfiguration() {
        // 验证参数
        if (!validateParameters()) {
            return
        }

        // 构建指令序列
        val commands = buildCommandSequence()
        if (commands.isEmpty()) {
            Toaster.show("没有可执行的指令")
            return
        }

        Timber.d("一键配置执行指令序列: ${commands.size}条")
        commands.forEachIndexed { index, cmd ->
            Timber.d("指令[$index]: $cmd")
        }

        // 清空之前的执行结果
        mStates.executionResults.clear()

        // 显示进度对话框，并在对话框准备好后初始化进度
        showCommandExecutionProgressDialog(commands.size)

        // 配置执行参数
        val config = CommandSequenceConfig(
            showLoadingDialog = false, // 使用自定义进度对话框
            stopOnFirstCmdError = false, // 不要在第一个指令下发失败时停止
            enableBusinessParseFailureInterrupt = false, // 指令响应内容解析出错后不中断后续指令执行
            errorConfig = ErrorConfig.toastConfig()
        )

        // 执行指令序列
        sendCommandSequence(
            commands = commands,
            config = config,
            callbacks = CommandSequenceCallbacks(
                onSuccess = { result ->
                    // 记录成功结果
                    val executionResult = CommandExecutionResult(
                        command = result.command,
                        response = result.responseData,
                        success = true
                    )
                    mStates.executionResults.add(executionResult)

                    // 更新进度
                    updateCommandExecutionProgress(commands.size)

                    addDeviceLogItem(
                        Log.INFO,
                        "[${mStates.executionResults.size}/${commands.size}] 成功: ${result.command}"
                    )
                    true // 继续执行
                },
                onComplete = { results ->
                    handleCommandExecutionComplete(results)
                },
                onError = { error, command ->
                    // 记录失败结果
                    val executionResult = CommandExecutionResult(
                        command = command,
                        response = "",
                        success = false,
                        errorMessage = error.message
                    )
                    mStates.executionResults.add(executionResult)

                    // 更新进度
                    updateCommandExecutionProgress(commands.size)

                    addDeviceLogItem(
                        Log.ERROR,
                        "[${mStates.executionResults.size}/${commands.size}] 失败: $command, ${error.message}"
                    )
                }
            )
        )
    }


    override fun handleCommandResponse(cmdStr: String) {}


    /**
     * 显示错误信息
     */
    private fun showError(message: String) {
        addDeviceLogItem(Log.ERROR, message)
        showMessageDialog(
            title = "温馨提示",
            message = message,
            positiveButtonText = "确定"
        )
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar, isKeyboardEnable = true)
    }

    /**
     * 显示指令执行进度对话框
     */
    private fun showCommandExecutionProgressDialog(totalCommands: Int) {
        val dialog = CommandExecutionProgressDialog(
            context = requireContext(),
            onExportExcel = {
                // 导出Excel
                exportExecutionResultsToExcel()
            },
            onClose = {
                // 关闭对话框
                Timber.d("用户关闭进度对话框")
            }
        )

        executionProgressDialogInstance = dialog
        executionProgressDialog = XPopup.Builder(context)
            .dismissOnTouchOutside(false)
            .dismissOnBackPressed(false)
            .enableDrag(false)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .customHostLifecycle(viewLifecycleOwner.lifecycle)
            .setPopupCallback(object : SimpleCallback() {
                override fun onShow(popupView: BasePopupView?) {
                    super.onShow(popupView)
                    // 对话框显示完成后初始化进度
                    initializeCommandExecutionProgress(totalCommands)
                    // 延迟一帧确保 binding 完全初始化
                    popupView?.post {
                        updateExecutionStatusToStarted(totalCommands)
                    }
                }

                override fun onDismiss(popupView: BasePopupView?) {
                    super.onDismiss(popupView)
                    executionProgressDialogInstance = null
                    executionProgressDialog = null
                }
            })
            .asCustom(dialog)

        executionProgressDialog?.show()
    }

    /**
     * 初始化指令执行进度，在开始执行时调用
     */
    private fun initializeCommandExecutionProgress(totalCommands: Int) {
        val initialProgress = CommandExecutionProgress(
            currentIndex = 0,
            totalCount = totalCommands,
            currentCommand = "准备执行指令序列...",
            status = ExecutionStatus.PENDING,
            successCount = 0,
            failedCount = 0,
            startTime = System.currentTimeMillis(),
        )

        executionProgressDialogInstance?.updateProgress(initialProgress)
    }

    /**
     * 更新执行状态为开始执行
     */
    private fun updateExecutionStatusToStarted(totalCommands: Int) {
        val startedProgress = CommandExecutionProgress(
            currentIndex = 1,
            totalCount = totalCommands,
            currentCommand = "开始执行指令序列...",
            status = ExecutionStatus.EXECUTING,
            successCount = 0,
            failedCount = 0,
            startTime = System.currentTimeMillis(),
        )

        executionProgressDialogInstance?.updateProgress(startedProgress)
    }

    /**
     * 更新指令执行进度
     */
    private fun updateCommandExecutionProgress(totalCommands: Int) {
        val executionResults = mStates.executionResults
        val currentCommand = executionResults.lastOrNull()?.command ?: ""

        // 优化状态判断逻辑：如果有结果说明已经在执行了
        val status = when {
            executionResults.isEmpty() -> ExecutionStatus.PENDING
            executionResults.size < totalCommands -> ExecutionStatus.EXECUTING
            else -> ExecutionStatus.EXECUTING // 让完成状态由 handleCommandExecutionComplete 处理
        }

        val progress = CommandExecutionProgress(
            currentIndex = executionResults.size + 1,
            totalCount = totalCommands,
            currentCommand = currentCommand.ifEmpty { "正在执行..." },
            status = status,
            successCount = executionResults.count { it.success },
            failedCount = executionResults.count { !it.success },
            startTime = System.currentTimeMillis(),
        )

        executionProgressDialogInstance?.updateProgress(progress)
    }

    /**
     * 处理执行完成
     */
    private fun handleCommandExecutionComplete(results: List<CommandResult>) {
        val executionResults = mStates.executionResults
        val totalCount = executionResults.size
        val successCount = executionResults.count { it.success }

        // 更新为完成状态
        val finalProgress = CommandExecutionProgress(
            currentIndex = totalCount,
            totalCount = totalCount,
            currentCommand = "",
            status = ExecutionStatus.COMPLETED,
            successCount = successCount,
            failedCount = totalCount - successCount,
            startTime = System.currentTimeMillis(),
        )

        executionProgressDialogInstance?.updateProgress(finalProgress)
    }

    /**
     * 导出执行结果到Excel
     */
    private fun exportExecutionResultsToExcel() {
        launchWithViewLifecycle {
            try {
                showLoadingDialog("正在导出Excel...")

                val results = mStates.executionResults
                if (results.isEmpty()) {
                    Toaster.show("没有可导出的数据")
                    return@launchWithViewLifecycle
                }

                // 获取输出目录
                val outputDir = java.io.File(PathUtils.getExternalAppFilesPath(), "export")
                if (!outputDir.exists()) {
                    outputDir.mkdirs()
                }

                // 导出Excel
                val file = CommandExcelExporter.exportToExcel(
                    results = results,
                    deviceSn = deviceInfo.deviceToken,
                    outputDir = outputDir
                )

                if (file != null) {
                    // 显示导出成功对话框
                    showExportSuccessDialog(file)
                } else {
                    Toaster.show("导出失败")
                }

            } catch (e: Exception) {
                Timber.e(e, "导出Excel失败")
                Toaster.show("导出失败：${e.message}")
            } finally {
                dismissLoadingDialog()
            }
        }
    }

    /**
     * 显示导出成功对话框
     */
    private fun showExportSuccessDialog(file: java.io.File) {
        val uri = UriUtils.file2Uri(file)

        showMessage(
            title = "导出成功",
            message = "Excel文件已导出成功！\n文件路径：${file.absolutePath}",
            positiveButtonText = "分享文件",
            positiveAction = {
                shareFile(uri)
            },
            negativeButtonText = "确定"
        )
    }

    /**
     * 分享文件
     */
    private fun shareFile(uri: Uri) {
        try {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "指令配置结果")
                putExtra(Intent.EXTRA_TEXT, "请查收指令配置结果Excel文件")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "分享配置结果"))
        } catch (e: Exception) {
            Timber.e(e, "分享文件失败")
            Toaster.show("分享失败：${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mStates.clearData()
    }

}

// ==================== 数据模型定义 ====================

/**
 * 指令项（内部使用）
 */
data class CommandItem(
    val command: String,
    val orderIndex: Int,
    val isFixed: Boolean,
    val note: String,
    val parameters: List<CmdParamInfo> = emptyList()
)
