package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentQuickConfigCommandParamBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.ParamSubmitButtonItem
import com.shmedo.mcloudapp.model.QuickConfigCommandParamChooseItem
import com.shmedo.mcloudapp.model.QuickConfigCommandParamEditItem
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.ProductConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.QuickConfigCommandParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class QuickConfigCommandParamFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentQuickConfigCommandParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: QuickConfigCommandParamViewModel by viewModels()
    private val productConfigViewModel: ProductConfigViewModel by viewModel()
    private val iotParseManager: IOTParserManager by inject()


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_quick_config_command_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentQuickConfigCommandParamBinding
        binding.llToolbar.toolbar.title = "一键配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        initAdapter()
    }

    override fun initData() {
        super.initData()
    }


    private fun initAdapter() {
        binding.recyclerView.linear().setup { rv ->
            addType<DeviceStatusInfoGroupItem>(R.layout.item_device_status_info_group)
            addType<QuickConfigCommandParamChooseItem>(R.layout.item_quick_config_command_param_choose)
            addType<QuickConfigCommandParamEditItem>(R.layout.item_quick_config_command_param_edit)
            addType<GapItem>(R.layout.item_device_status_info_gap)
            addType<ParamSubmitButtonItem>(R.layout.item_param_summit_button)

            R.id.item.onClick {
                when (itemViewType) {
                    R.layout.item_quick_config_command_param_choose -> {
                        val item = getModel<QuickConfigCommandParamChooseItem>()
                        onChooseItemClick(item)
                    }

                    else -> {}
                }
            }
            R.id.btn_submit.onClick {
                KeyboardUtils.hideSoftInput(binding.root)
                val item = getModel<ParamSubmitButtonItem>()
                if (!isDeviceConnected()) {
                    Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                    return@onClick
                }
                saveConfiguration()
            }
        }
    }

    override fun lazyLoadData() {
        //TODO 根据 sn (deviceInfo.deviceToken) 调用接口查询指令配置参数模版，例如：http://ams4.shmedo.com:22000/api/v1/GetCmdOrdersBySn/medotest?sn=2470002CR2
        launchWithViewLifecycle {
            try {
                val param: MutableMap<String, String> = HashMap()
                param["sn"] = deviceInfo.deviceToken
                val cmdOrderInfo =
                    productConfigViewModel.getDeviceGetCmdOrdersBySn("medotest", param)
                if (cmdOrderInfo == null) {
                    Toaster.show("cmdOrderInfo==null")
                    return@launchWithViewLifecycle
                }

            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    /**
     * 保存配置
     */
    private fun saveConfiguration() {

    }

    override fun handleCommandResponse(cmdStr: String) {
        TODO("Not yet implemented")
    }

    /**
     *
     */
    private fun onChooseItemClick(item: QuickConfigCommandParamChooseItem) {
        val tempList = item.fieldValueInfos.map { it.value }
        val selectedIndex = tempList.indexOf(item.value)
        XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
        XPopup.Builder(context)
            .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .enableDrag(false)
            .asBottomList(
                "", tempList.toTypedArray(),
                null, selectedIndex,
                { position, text ->
                    item.refreshValue(text)
                }, 0, R.layout.custom_xpopup_adapter_text_center
            )
            .show()
    }


    /**
     * 点击事件处理
     */
    inner class ClickProxy : BaseClickProxy() {

    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}