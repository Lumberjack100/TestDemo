package com.shmedo.mcloudapp.ui.page.device.das.fragment

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.setFragmentResultListener
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.CenterNumberEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das.DasBdTerminalEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ServerOne
import com.shmedo.lib.cmd.base.iot_cmd.enums.ServerThree
import com.shmedo.lib.cmd.base.iot_cmd.enums.ServerTwo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.common.DataCenterStatus
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasBdTerminalInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentDasDataCenterHomeBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDataCenterParamFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.DasSensorHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject

class DasDataCenterHomeFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasDataCenterHomeBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: DasSensorHomeViewModel
    private val iotParseManager: IOTParserManager by inject()
    private val baudRateList: MutableList<String> = arrayListOf("9600", "115200")


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_das_data_center_home,
            BR.stateVM, mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasDataCenterHomeBinding
        toolbarViewModel.toolbarTitleText.set("数据中心")
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
                val bundle = UniversalDataCenterParamFragment.newBundleArguments(
                    item,
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().navigate(
                    R.id.action_dasDataCenterHomeFragment_to_dataCenterParamFragment,
                    bundle
                )
            }
        }.models = getAdapterData()
    }

    override fun createObserver() {
        super.createObserver()
        //从编辑页面返回需要刷新事件详情页面
        setFragmentResultListener(AppContants.Extras.FRAGMENT_DATA_CENTER_HOME_RESULT_REQUEST_KEY) { key, bundle ->
            val centerNumber =
                bundle.getInt(AppContants.Extras.REFRESH_DATA_CENTER_STATUS, ServerOne.centerId)
            commandItems.clear()

            val entity = CenterNumberEntity(centerNumber.toString())
            val command =
                IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DATA_CENTER_STATUS, entity)
            commandItems.add(command)

            showLoadingDialog(StringUtils.getString(R.string.loading))
            sendCommandFromCmdList(isStartTimeoutJob = true)
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            mStates.isBdOpened.set(isChecked)
            if (!isChecked) {
                showMessage("确定要关闭吗？", "温馨提示", "确定", {
                    closeBdTerminal()
                }, "取消", {
                    mStates.isBdOpened.set(true)
                    (button as SwitchButton).setCheckedImmediatelyNoEvent(true)
                })
            }
        }

        fun onBaudRateChooseClick() {
            val selectedIndex = baudRateList.indexOf(mStates.baudRate.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", baudRateList.toTypedArray(),
                    null, selectedIndex,
                    { _, text ->
                        mStates.baudRate.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onSubmitClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun closeBdTerminal() {
        commandItems.clear()
        val entity = DasBdTerminalEntity(
            sw = "0"
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_SET_BD_TERMINAL,
            entity.toCommandString()
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initSaveCommand() {
        commandItems.clear()
        if (mStates.address.get().isEmpty()) {
            showMessageDialog("请输入目标地址!")
            return
        }
        val entity = DasBdTerminalEntity(
            sw = "1",
            dstaddr = mStates.address.get(),
            baud = mStates.baudRate.get()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_SET_BD_TERMINAL,
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

        var command = IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_BD_TERMINAL)
        commandItems.add(command)

        var entity = CenterNumberEntity(ServerOne.centerId.toString())
        command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DATA_CENTER_STATUS, entity)
        commandItems.add(command)

        entity = CenterNumberEntity(ServerTwo.centerId.toString())
        command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DATA_CENTER_STATUS, entity)
        commandItems.add(command)

        entity = CenterNumberEntity(ServerThree.centerId.toString())
        command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DATA_CENTER_STATUS, entity)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_BD_TERMINAL -> {
                val result = iotParseManager.parse<DasBdTerminalInfo>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_BD_TERMINAL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询北斗参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        initBdTerminal(result.data)
                    }
                }
            }

            IOTCommandType.MD_GET_DATA_CENTER_STATUS -> {
                val result = iotParseManager.parse<DataCenterStatus>(
                    cmdStr,
                    IOTCommandType.MD_GET_DATA_CENTER_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询数据中心状态出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initDataCenterStatus(result.data)
                    }
                }
            }

            IOTCommandType.DAS_MD_SET_BD_TERMINAL -> {
//                setEditable(false)
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "北斗参数设置出错: ${result.message}"
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

    private fun initBdTerminal(info: DasBdTerminalInfo) {
        mStates.isBdOpened.set(info.sw.toInt() == 1)
        mStates.address.set(info.dstaddr)
        mStates.baudRate.set(
            if (info.baud.isEmpty()) {
                baudRateList[0]
            } else {
                baudRateList[baudRateList.indexOf(info.baud)]
            }
        )
    }

    private fun initDataCenterStatus(dataCenterStatus: DataCenterStatus) {
        when (dataCenterStatus.centerid) {
            ServerOne.centerId -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(0)
                    .refreshStatus(dataCenterStatus.status)
            }

            ServerTwo.centerId -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(1)
                    .refreshStatus(dataCenterStatus.status)
            }

            ServerThree.centerId -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(2)
                    .refreshStatus(dataCenterStatus.status)
            }
        }
    }

    private fun getAdapterData() = mutableListOf(
        DataCenterStatusItem(1, "数据中心01", "0"),
        DataCenterStatusItem(2, "数据中心02", "0"),
        DataCenterStatusItem(3, "数据中心03", "0"),
    )

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}