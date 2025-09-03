package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.ServerOne
import com.shmedo.lib.cmd.base.iot_cmd.enums.ServerThree
import com.shmedo.lib.cmd.base.iot_cmd.enums.ServerTwo
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.enums.SaveConfigMode
import com.shmedo.lib.cmd.base.md_cmd.model.common.DeviceNetStatus
import com.shmedo.lib.cmd.base.md_cmd.model.das.DasBaseConfigInfo
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.communication.model.ErrorHandlingStrategy
import com.shmedo.mcloudapp.databinding.FragmentBleDasDataCenterHomeBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.BleDasDataCenterHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/6/17
 * @desc: 物联网采集器(DAS)数据中心参数配置页面 - 支持蓝牙通讯方式
 *
 */
class BleDasDataCenterHomeFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentBleDasDataCenterHomeBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: BleDasDataCenterHomeViewModel by viewModels()
    private val mdParseManager: MDParserManager by inject()
    private val communicatModeList: MutableList<String> = arrayListOf("4G", "SMS", "BD", "BD+4G")


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_ble_das_data_center_home,
            BR.stateVM, mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentBleDasDataCenterHomeBinding
        binding.llToolbar.toolbar.title = "数据链路"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        initRefresh()
        initAdapter()
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

    private fun initAdapter() {
        binding.recyclerView.setup { rv ->
            addType<DataCenterStatusItem>(R.layout.data_center_status_item)
            R.id.item.onClick {
                val item = getModel<DataCenterStatusItem>()
                val bundle = BleDasDataCenterParamFragment.Companion.newBundleArguments(
                    item,
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().safeNavigate(
                    R.id.action_global_to_bleDataCenterParamFragment,
                    bundle
                )
            }
        }.models = getAdapterData()
    }

    override fun initData() {
        super.initData()
        mStates.dataCommunicationMode.set(communicatModeList[0])
    }

    override fun createObserver() {
        super.createObserver()
        //从编辑页面返回需要刷新事件详情页面
        setFragmentResultListener(AppContants.Extras.FRAGMENT_DATA_CENTER_HOME_RESULT_REQUEST_KEY) { key, bundle ->
            val centerNumber = bundle.getInt(AppContants.Extras.REFRESH_DATA_CENTER_STATUS, ServerOne.centerId)
            val command = MDCommandUtil.getCommand(MDCommandType.QUERY_NETWORK_STATUS, centerNumber.toString())

            sendCommandSequence(
                commands = listOf(command),
                config = CommandSequenceConfig(
                    loadingMessage = StringUtils.getString(R.string.loading),
                    errorConfig = ErrorConfig.dialogConfig()
                )
            )
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onBaudRateChooseClick() {
            val selectedIndex = communicatModeList.indexOf(mStates.dataCommunicationMode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", communicatModeList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.dataCommunicationMode.set(text)
                        mStates.isBdCardNumberVisible.set(position == 2 || position == 3)
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
        if (mStates.reportingInterval.get().isEmpty()) {
            showMessageDialog("请输入上报间隔!")
            return
        }
        if (mStates.isBdCardNumberVisible.get()) {
            if (mStates.bdCardNumber.get().isEmpty()) {
                showMessageDialog("请输入北斗卡号!")
                return
            }
        }

        val commands = mutableListOf<String>()
        var command = MDCommandUtil.getCommand(
            MDCommandType.DATA_MASSAGE_MODEL,
            (communicatModeList.indexOf(mStates.dataCommunicationMode.get()) + 1).toString()
        )
        Timber.d("设置数据通讯模式===%s", command)
        commands.add(command)

        command = MDCommandUtil.getCommand(
            MDCommandType.DATA_REPORT_INTERVAL,
            mStates.reportingInterval.get()
        )
        Timber.d("设置数据上报间隔===%s", command)
        commands.add(command)

        if (mStates.isBdCardNumberVisible.get()) {
            command = MDCommandUtil.getCommand(
                MDCommandType.SIX_TARGER_BD_NUMBER,
                mStates.bdCardNumber.get()
            )
            Timber.d("北斗配置参数===%s", command)
            commands.add(command)
        }

        command = MDCommandUtil.getCommand(
            MDCommandType.SAVE_CONFIG_INFO,
            SaveConfigMode.SAVE_NO_REBOOT.toString()
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

        var command =
            MDCommandUtil.getCommand(MDCommandType.BASE_CONFIG)
        commands.add(command)

        for (i in 1..3) {
            command = MDCommandUtil.getCommand(MDCommandType.QUERY_NETWORK_STATUS, i.toString())
            commands.add(command)
        }

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
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.BASE_CONFIG -> {
                val result = mdParseManager.parse<DasBaseConfigInfo>(
                    cmdStr,
                    MDCommandType.BASE_CONFIG
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询基础配置信息出错"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is MDCommandResult.Success -> {
                        initBaseConfigInfo(result.data)
                    }
                }
            }

            MDCommandType.QUERY_NETWORK_STATUS -> {
                val result = mdParseManager.parse<DeviceNetStatus>(
                    cmdStr,
                    MDCommandType.QUERY_NETWORK_STATUS
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询数据链路状态错"
                        handleFailureResult(errMsg)
                    }

                    is MDCommandResult.Success -> {
                        initDataCenterStatus(result.data)
                    }
                }
            }

            MDCommandType.DATA_MASSAGE_MODEL -> {//设置数据通讯方式
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "数据通讯方式配置错误!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {

                    }
                }
            }

            MDCommandType.DATA_REPORT_INTERVAL -> {//设置数据上报间隔
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "数据上报间隔配置错误!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {

                    }
                }
            }

            MDCommandType.SIX_TARGER_BD_NUMBER -> {//北斗配置
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "北斗目标卡号配置错误!"
                        handleFailureResult(errMsg)
                    }

                    else -> {

                    }
                }
            }

            MDCommandType.SAVE_CONFIG_INFO -> {//
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "保存出错!"
                        handleFailureResult(errMsg, isMessageDialog = true)
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

    private fun initBaseConfigInfo(info: DasBaseConfigInfo) {
        mStates.dataCommunicationMode.set(communicatModeList[info.dataCommunicateMode.toInt() - 1])
        mStates.isBdCardNumberVisible.set(info.dataCommunicateMode == "3" || info.dataCommunicateMode == "4")
        mStates.reportingInterval.set(info.dataReportInterval)
        mStates.bdCardNumber.set(info.targetBGNum)
    }

    private fun initDataCenterStatus(info: DeviceNetStatus) {
        when (info.linkNumber) {
            ServerOne.centerId.toString() -> {
                //0 未启用  1 已连接  2 未连接
                val statusCode = info.linkEnable.compareAndReturn(
                    "0",
                    "0",
                    info.linkStatus.compareAndReturn("1", "1", "2")
                )
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(0)
                    .refreshStatus(statusCode)
            }

            ServerTwo.centerId.toString() -> {
                //0 未启用  1 已连接  2 未连接
                val statusCode = info.linkEnable.compareAndReturn(
                    "0",
                    "0",
                    info.linkStatus.compareAndReturn("1", "1", "2")
                )
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(1)
                    .refreshStatus(statusCode)
            }

            ServerThree.centerId.toString() -> {
                //0 未启用  1 已连接  2 未连接
                val statusCode = info.linkEnable.compareAndReturn(
                    "0",
                    "0",
                    info.linkStatus.compareAndReturn("1", "1", "2")
                )
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(2)
                    .refreshStatus(statusCode)
            }
        }
    }

    private fun getAdapterData() = mutableListOf(
        DataCenterStatusItem(
            centerid = 1,
            name = "数据链路1",
            status = "0",
            bgResId = R.drawable.layer_common_click_item_top_corner_4_with_divider
        ),
        DataCenterStatusItem(
            centerid = 2,
            name = "数据链路2",
            status = "0",
            bgResId = R.drawable.layer_common_click_item_with_divider
        ),
        DataCenterStatusItem(
            centerid = 3,
            name = "数据链路3",
            status = "0",
            bgResId = R.drawable.shape_common_click_item_bottom_corner_4
        )
    )

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}