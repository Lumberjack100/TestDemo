package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentUdAlarmParamSettingBinding
import com.shmedo.mcloudapp.databinding.FragmentUdFlowCalculationBinding
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDAlarmParamSettingViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDFlowCalculationViewModel
import org.koin.android.ext.android.inject
import kotlin.getValue

/**
 * @author：gonghe
 * @time: 2025/10/27
 * @desc: 流量计算
 *
 */
class UDFlowCalculationFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUdFlowCalculationBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: UDFlowCalculationViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_ud_flow_calculation,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUdFlowCalculationBinding
        binding.llToolbar.toolbar.title = "流量计算"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher {
            handleBackByCheckDataModified()
        }
        initRefresh()
    }

    override fun initData() {
        super.initData()
        initTitles()
        resetDefaultParams()
        //添加这行来保存初始状态
        mStates.saveInitialState()
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
}