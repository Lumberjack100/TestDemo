package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.datepicker.MaterialDatePicker
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.TransferState
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentMonitoringDataExportBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.MonitoringDataExportViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 监测数据导出Fragment
 */
class MonitoringDataExportFragment : BaseFragment() {
    private lateinit var binding: FragmentMonitoringDataExportBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: MonitoringDataExportViewModel by viewModel()

    private val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    private var startTime: String = ""
    private var endTime: String = ""


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_monitoring_data_export,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.stateVM, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMonitoringDataExportBinding
        binding.llToolbar.toolbar.title = "数据导出"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }

        initTimeSelector()
    }

    /**
     * 初始化时间选择器
     */
    private fun initTimeSelector() {
        // 设置默认时间（最近7天）
        val calendar = Calendar.getInstance()
        endTime = dateFormat.format(calendar.time)
        binding.etEndTime.setText(displayDateFormat.format(calendar.time))

        calendar.add(Calendar.DAY_OF_MONTH, -7)
        startTime = dateFormat.format(calendar.time)
        binding.etStartTime.setText(displayDateFormat.format(calendar.time))

        // 开始时间选择
        binding.etStartTime.setOnClickListener {
            showDateTimePicker("选择开始时间") { time ->
                startTime = dateFormat.format(Date(time))
                binding.etStartTime.setText(displayDateFormat.format(Date(time)))
            }
        }

        // 结束时间选择
        binding.etEndTime.setOnClickListener {
            showDateTimePicker("选择结束时间") { time ->
                endTime = dateFormat.format(Date(time))
                binding.etEndTime.setText(displayDateFormat.format(Date(time)))
            }
        }
    }

    override fun initData() {
        super.initData()
        // 从参数获取设备信息
        arguments?.let {
            val productType: ProductType =
                it.getParcelable(AppContants.Extras.PRODUCT_TYPE) ?: ProductType.UnKnown
            val deviceSn = it.getString("deviceSn", "")
            val apiKey = it.getString("apiKey", "")
            mStates.setDeviceInfo(productType, deviceSn, apiKey)
        }
    }

    override fun createObserver() {
        // 观察UI状态
        mStates.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MonitoringDataExportViewModel.UiState.Ready -> {
                    hideLoading()
                    updateLocalDataCount(state.localDataCount)
                    updateButtons(true)
                }

                is MonitoringDataExportViewModel.UiState.Transferring -> {
                    showLoading("正在传输数据...")
                    updateButtons(false)
                }

                is MonitoringDataExportViewModel.UiState.TransferSuccess -> {
                    hideLoading()
                    ToastUtils.showShort("数据传输完成，共${state.totalCount}条")
                    updateButtons(true)
                }

                is MonitoringDataExportViewModel.UiState.Exporting -> {
                    showLoading("正在导出Excel...")
                }

                is MonitoringDataExportViewModel.UiState.ExportSuccess -> {
                    hideLoading()
                    ToastUtils.showShort("导出成功：${state.filePath}")
                }

                is MonitoringDataExportViewModel.UiState.Error -> {
                    hideLoading()
                    ToastUtils.showShort(state.message)
                    updateButtons(true)
                }
            }
        }

        // 观察传输状态
        mStates.transferState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is TransferState.Idle -> {
                    binding.cardTransferProgress.visibility = View.GONE
                    binding.tvTransferStatus.text = "传输状态：未开始"
                    // 重置已传输数据大小显示
                    binding.tvTransferredDataSize.text = "已传输数据大小：0 B"
                }

                is TransferState.Transferring -> {
                    binding.cardTransferProgress.visibility = View.VISIBLE
                    binding.tvTransferStatus.text = "传输状态：传输中"
                    binding.progressTransfer.isIndeterminate = false
                    binding.progressTransfer.progress = (state.progress * 100).toInt()
                    binding.tvTransferSpeed.text = state.speed
                    binding.tvTransferredDataSize.text = "已传输数据大小：${state.transferredDataSize}"
                }

                is TransferState.Success -> {
                    binding.cardTransferProgress.visibility = View.GONE
                    binding.tvTransferStatus.text = "传输状态：完成"
                }

                is TransferState.Error -> {
                    binding.cardTransferProgress.visibility = View.GONE
                    binding.tvTransferStatus.text = "传输状态：失败"
                }
            }
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 开始传输按钮
         */
        fun onStartTransferClick() {
            if (validateTime()) {
                mStates.startDataTransfer(startTime, endTime)
            }
        }

        /**
         * 导出Excel按钮
         */
        fun onExportExcelClick() {
            if (validateTime()) {
                mStates.exportToExcel(startTime, endTime)
            }
        }
    }


    /**
     * 显示日期时间选择器
     */
    private fun showDateTimePicker(title: String, onDateSelected: (Long) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            onDateSelected(selection)
        }

        datePicker.show(childFragmentManager, "datePicker")
    }

    /**
     * 验证时间选择
     */
    private fun validateTime(): Boolean {
        if (startTime.isEmpty() || endTime.isEmpty()) {
            ToastUtils.showShort("请选择时间范围")
            return false
        }

        if (startTime >= endTime) {
            ToastUtils.showShort("开始时间必须小于结束时间")
            return false
        }

        return true
    }

    /**
     * 更新本地数据数量显示
     */
    private fun updateLocalDataCount(count: Int) {
        binding.tvLocalDataCount.text = "本地数据：$count 条"
    }

    /**
     * 更新按钮状态
     */
    private fun updateButtons(enabled: Boolean) {
        binding.btnStartTransfer.isEnabled = enabled
        binding.btnExportExcel.isEnabled = enabled

        if (!enabled) {
            binding.btnStartTransfer.text = "停止传输"
            binding.btnStartTransfer.setOnClickListener {
                mStates.stopTransfer()
            }
        } else {
            binding.btnStartTransfer.text = "开始传输"
            binding.btnStartTransfer.setOnClickListener {
                if (validateTime()) {
                    mStates.startDataTransfer(startTime, endTime)
                }
            }
        }
    }

    /**
     * 显示加载框
     */
    private fun showLoading(message: String) {
//        DialogLoadingUtils.showLoading(requireContext(), message)
        mStates.msg.set(message)
    }

    /**
     * 隐藏加载框
     */
    private fun hideLoading() {
//        DialogLoadingUtils.hideLoading()
        mStates.msg.set("")
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        /**
         * 创建Fragment实例
         */
        fun newInstance(deviceSn: String, apiKey: String): MonitoringDataExportFragment {
            return MonitoringDataExportFragment().apply {
                arguments = Bundle().apply {
                    putString("deviceSn", deviceSn)
                    putString("apiKey", apiKey)
                }
            }
        }
    }
}