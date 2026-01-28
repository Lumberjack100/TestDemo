package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mg301

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.setup
import com.hjq.toast.ToastParams
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.DeviceError
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentMr702EquipmentOperationBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingWithUUID
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.MG301SatDataClearModule
import com.shmedo.mcloudapp.model.UnifiedDeviceModule
import com.shmedo.mcloudapp.model.toUnified
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import org.koin.android.ext.android.inject

/**
 * 创建者: gonghe
 * 创建时间: 2026/01/20
 * 描述: MG301 设备操作页面
 *
 * 功能模块：
 * - 卫星数据清空：清空卫星模组内所有待发数据
 */
class MG301DeviceOperationFragment : OptimizedBaseIOTDeviceFragment() {

    // ==================== 视图绑定 ====================

    /** 页面视图绑定对象 */
    private lateinit var binding: FragmentMr702EquipmentOperationBinding

    /** 工具栏视图模型 */
    private val toolbarViewModel: ToolbarViewModel by viewModels()

    /** IOT 指令解析管理器 */
    private val iotParseManager: IOTParserManager by inject()

    // ==================== 状态变量 ====================

    /** Loading 弹窗标识 ID，用于后续关闭弹窗 */
    private var loadingDialogId = ""

    // ==================== 生命周期方法 ====================

    /**
     * 配置 DataBinding
     * 使用 MR702 设备操作页面布局（布局结构通用，可复用）
     */
    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_mr702_equipment_operation,
            BR.toolbarVM,
            toolbarViewModel
        ).addBindingParam(BR.click, BaseClickProxy())
    }

    /**
     * 初始化视图
     * - 设置工具栏标题和返回按钮
     * - 初始化功能模块列表
     */
    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702EquipmentOperationBinding

        // 设置工具栏标题
        binding.llToolbar.toolbar.title = "设备操作"

        // 设置返回按钮点击事件
        binding.llToolbar.toolbar.setNavigationOnClickListener { _: View? ->
            nav().navigateUp()
        }

        // 注册系统返回键处理
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }

        // 初始化功能模块列表适配器
        initModuleAdapter()
    }

    // ==================== 功能模块列表 ====================

    /**
     * 初始化功能模块列表适配器
     * 使用 RecyclerView 网格布局展示功能卡片
     */
    private fun initModuleAdapter() {
        binding.rvModule.setup { rv ->
            // 添加网格间距装饰器（2列，15dp间距）
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    2,
                    ConvertUtils.dp2px(15f),
                    false
                )
            )

            // 添加功能模块卡片类型
            addType<UnifiedDeviceModule>(R.layout.item_device_config_module)

            // 设置卡片点击事件
            R.id.item.onClick {
                val unifiedModule = getModel<UnifiedDeviceModule>()
                processItemClick(unifiedModule.module)
            }
        }.models = getModuleList()
    }

    /**
     * 获取功能模块列表
     * 目前只包含：卫星数据清空
     *
     * @return 功能模块列表
     */
    private fun getModuleList(): ArrayList<UnifiedDeviceModule> {
        return arrayListOf(
            // 卫星数据清空模块
            MG301SatDataClearModule().toUnified()
        )
    }

    // ==================== 点击事件处理 ====================

    /**
     * 处理功能模块点击事件
     *
     * @param functionModule 被点击的功能模块
     */
    private fun processItemClick(functionModule: DeviceFunctionModule) {
        // 检查设备连接状态
        if (!isDeviceConnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }

        when (functionModule) {
            // 卫星数据清空功能
            is MG301SatDataClearModule -> {
                handleSatDataClear()
            }

            else -> {
                // 其他模块（如果有导航目标）
                if (functionModule.navId != 0) {
                    // 预留：未来可能添加更多功能模块
                }
            }
        }
    }

    /**
     * 处理卫星数据清空操作
     * 显示确认弹窗，用户确认后发送清空指令
     */
    private fun handleSatDataClear() {
        showMessage(
            "是否执行缓存待发数据清空",
            "温馨提示",
            "确定",
            {
                // 用户确认后发送清空指令
                sendSatDataClearCommand()
            },
            "取消"
        )
    }

    /**
     * 发送卫星数据清空指令
     *
     * 指令格式: $cmd=md_cleansatdata&number=0
     * 成功应答: $cmd=md_cleansatdata&result=succ
     * 失败应答: $cmd=md_cleansatdata&result=fail&reason=
     */
    private fun sendSatDataClearCommand() {
        // 构建指令：$cmd=md_cleansatdata&number=0
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_CLEAN_SAT_DATA,
            "number=0"
        )

        // 显示 Loading 弹窗
        loadingDialogId = showLoadingWithUUID(StringUtils.getString(R.string.processing))

        // 发送指令序列
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                timeout = AppContants.Communication.DELAY_10000_MILLIS, // 10秒超时
                showLoadingDialog = false, // 使用自定义 Loading 弹窗
                errorConfig = ErrorConfig.customConfig { error ->
                    // 处理错误
                    dismissLoadingDialog(loadingDialogId)
                    if (error is DeviceError.Timeout) {
                        showMessageDialog("设备未响应")
                    } else {
                        showMessageDialog("卫星数据清空失败: ${error.message}")
                    }
                }
            )
        )
    }

    // ==================== 指令响应处理 ====================

    /**
     * 处理设备指令响应
     *
     * @param cmdStr 指令响应字符串
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            // 卫星数据清空指令响应
            IOTCommandType.MD_CLEAN_SAT_DATA -> {
                handleSatDataClearResponse(cmdStr)
            }

            else -> {
                // 其他指令响应（预留）
            }
        }
    }

    /**
     * 处理卫星数据清空指令响应
     *
     * @param cmdStr 指令响应字符串
     */
    private fun handleSatDataClearResponse(cmdStr: String) {
        when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
            is IOTCommandResult.Failure -> {
                // 指令执行失败
                dismissLoadingDialog(loadingDialogId)
                val errMsg = "卫星数据清空失败: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            else -> {
                // 指令执行成功
                if (!isCommunicationExecuting()) {
                    dismissLoadingDialog(loadingDialogId)
                    Toaster.show(ToastParams().apply {
                        text = "卫星数据清空成功"
                        duration = 1000
                    })
                }
            }
        }
    }
}
