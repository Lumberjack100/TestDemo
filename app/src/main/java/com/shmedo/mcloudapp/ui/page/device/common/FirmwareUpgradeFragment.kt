package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.PageRefreshLayout
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.core.commonlib.utils.MmkvCacheUtil
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.FirmWareEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentFirmwareUpgradeBinding
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.FirmWareInfo
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.FirmwareUpgradeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/1/5
 * @desc: 固件升级
 *
 */
class FirmwareUpgradeFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentFirmwareUpgradeBinding by lazy { getBinding() as FragmentFirmwareUpgradeBinding }
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: FirmwareUpgradeViewModel
    private lateinit var deviceRequestViewModel: DeviceRequestViewModel
    private val iotParseManager: IOTParserManager by inject()

    private val userInfo: com.shmedo.core.model.UserInfo by lazy { MmkvCacheUtil.getUser()!! }
    private val firmwareStatusList: MutableList<String> = arrayListOf("全部", "测试", "运营")


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
        deviceRequestViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_firmware_upgrade, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "固件升级"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        initRefresh()
        initAdapter()
    }

    private fun initRefresh() {
        PageRefreshLayout.startIndex = 1
        binding.refreshLayout.onRefresh {
            refreshData()
        }
    }

    private fun initAdapter() {
        binding.recyclerView.setup { rv ->
            addType<FirmWareInfo>(R.layout.item_device_firmware_info)
            R.id.item.onClick {
                val firmWareInfo = getModel<FirmWareInfo>()
                showMessage("确定下载升级此固件吗？", "温馨提示", "确定", {
                    if (isBleDisconnected()) {
                        Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                        return@showMessage
                    }
                    applyFirmwareUpgrade(firmWareInfo)
                }, "取消")
            }
        }
    }

    override fun initData() {
        super.initData()
        mStates.firmwareStatus.set(firmwareStatusList[0])
    }

    override fun createObserver() {
        super.createObserver()
        deviceRequestViewModel.firmWareListResult.observe(viewLifecycleOwner) { listDataResult: DataResult<List<FirmWareInfo>> ->
            if (!listDataResult.responseStatus.isSuccess) {
                Toaster.show(listDataResult.responseStatus.errorMessage)
                if (binding.refreshLayout.state == RefreshState.Refreshing)
                    binding.refreshLayout.finishRefresh(false)
                else
                    binding.refreshLayout.finishLoadMore(false)

                return@observe
            }
            binding.refreshLayout.addData(listDataResult.result, hasMore = {
                binding.refreshLayout.index < listDataResult.totalPage
            })
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onFirmwareStatusChooseClick() {
            val selectedIndex = firmwareStatusList.indexOf(mStates.firmwareStatus.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", firmwareStatusList.toTypedArray(),
                    null, selectedIndex,
                    { _, text ->
                        mStates.firmwareStatus.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onSearchClick() {
            binding.refreshLayout.autoRefresh()
        }
    }

    private fun refreshData() {
        deviceRequestViewModel.queryFirmwareListByProductIDWithPage(
            productID = deviceInfo.productID,
            companyID = userInfo.companyID,
            fwStatus = if (mStates.firmwareStatus.get() == "全部") ""
            else if (mStates.firmwareStatus.get() == "测试") "0" else "1",
            fwName = mStates.searchText.get(),
            fwVersion = mStates.searchText.get(),
            nameAndVersion = false,
            currentPage = binding.refreshLayout.index,
            pageSize = PAGE_SIZE
        )
    }

    private fun applyFirmwareUpgrade(firmWareInfo: FirmWareInfo) {
        showLoadingDialog(StringUtils.getString(R.string.processing))

        launchWithViewLifecycle {
            if (communicateWay is BleConnect) {
                val entity = FirmWareEntity(
                    url = firmWareInfo.absolutePath,
                    size = firmWareInfo.fwSize.toString(),
                    md5 = firmWareInfo.fwMd5
                )
                commandItems.clear()
                val command = IOTCommandUtil.getCommand(
                    IOTCommandType.MD_UPGRADE,
                    entity.toCommandString()
                )
                commandItems.add(command)
                sendCommandFromCmdList(isStartTimeoutJob = true)

            } else {
                val msgID = deviceRequestViewModel.applyFirmwareUpgrade(
                    deviceToken = deviceInfo.deviceToken,
                    firmwareID = firmWareInfo.id,
                ) { error: Throwable ->
                    dismissLoadingDialog()
                    Toaster.show("升级失败：${error.message}")
                } ?: return@launchWithViewLifecycle

                netIotCommandViewModel.processCmdResult("", arrayListOf(msgID))
            }
        }
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_UPGRADE -> {
                val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "升级失败: ${result.message}"
                        Timber.e(result.message)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            Toaster.show("设备即将进行固件升级，请稍后查看升级结果")
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}