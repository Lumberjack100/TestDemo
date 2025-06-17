package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
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
import com.shmedo.mcloudapp.databinding.FragmentBleDasDataCenterHomeBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
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
class BleDasDataCenterHomeFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentBleDasDataCenterHomeBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: BleDasDataCenterHomeViewModel
    private val mdParseManager: MDParserManager by inject()
    private val communicatModeList: MutableList<String> = arrayListOf("4G", "SMS", "BD", "BD+4G")


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

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
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
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

            commandItems.clear()
            val command = MDCommandUtil.getCommand(MDCommandType.QUERY_NETWORK_STATUS, centerNumber.toString())
            commandItems.add(command)

            showLoadingDialog(StringUtils.getString(R.string.loading))
            sendCommandFromCmdList(isStartTimeoutJob = true)
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
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        commandItems.clear()
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

        commandItems.clear()
        var command = MDCommandUtil.getCommand(
            MDCommandType.DATA_MASSAGE_MODEL,
            (communicatModeList.indexOf(mStates.dataCommunicationMode.get()) + 1).toString()
        )
        Timber.Forest.d("设置数据通讯模式===%s", command)
        commandItems.add(command)

        command = MDCommandUtil.getCommand(
            MDCommandType.DATA_REPORT_INTERVAL,
            mStates.reportingInterval.get()
        )
        Timber.Forest.d("设置数据上报间隔===%s", command)
        commandItems.add(command)

        if (mStates.isBdCardNumberVisible.get()) {
            command = MDCommandUtil.getCommand(
                MDCommandType.SIX_TARGER_BD_NUMBER,
                mStates.bdCardNumber.get()
            )
            Timber.Forest.d("北斗配置参数===%s", command)
            commandItems.add(command)
        }

        command = MDCommandUtil.getCommand(
            MDCommandType.SAVE_CONFIG_INFO,
            SaveConfigMode.SAVE_NO_REBOOT.toString()
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

        var command =
            MDCommandUtil.getCommand(MDCommandType.BASE_CONFIG)
        commandItems.add(command)

        for (i in 1..3) {
            command = MDCommandUtil.getCommand(MDCommandType.QUERY_NETWORK_STATUS, i.toString())
            commandItems.add(command)
        }

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.BASE_CONFIG -> {
                val result = mdParseManager.parse<DasBaseConfigInfo>(
                    cmdStr,
                    MDCommandType.BASE_CONFIG
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询基础配置信息出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
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
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initDataCenterStatus(result.data)
                    }
                }
            }

            MDCommandType.DATA_MASSAGE_MODEL -> {//设置数据通讯方式
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "数据通讯方式配置错误!"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.DATA_REPORT_INTERVAL -> {//设置数据上报间隔
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "数据上报间隔配置错误!"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SIX_TARGER_BD_NUMBER -> {//北斗配置
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "北斗目标卡号配置错误!"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SAVE_CONFIG_INFO -> {//
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "保存出错!"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
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
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(0)
                    .refreshStatus(info.linkEnable)
            }

            ServerTwo.centerId.toString() -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(1)
                    .refreshStatus(info.linkEnable)
            }

            ServerThree.centerId.toString() -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(2)
                    .refreshStatus(info.linkEnable)
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