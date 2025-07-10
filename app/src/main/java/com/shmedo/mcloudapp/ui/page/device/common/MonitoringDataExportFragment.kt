package com.shmedo.mcloudapp.ui.page.device.common

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.datepicker.MaterialDatePicker
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentMonitoringDataExportBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.MonitoringDataExportViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/7/9
 * @desc: 监测数据导出Fragment
 *
 */
class MonitoringDataExportFragment : BaseFragment() {
    private lateinit var binding: FragmentMonitoringDataExportBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: MonitoringDataExportViewModel by viewModel()

    private val periodDateList: MutableList<String> = arrayListOf(
        "当天",
        "两天内",
        "三天内",
        "一周内",
        "一个月内",
        "三个月内",
        "半年内",
        "一年内"
    )
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
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? -> nav().navigateUp() }
        registerOnBackPressedDispatcher { nav().navigateUp() }
        initTimeSpinnerAdapter()
    }

    private fun initTimeSpinnerAdapter() {
        binding.spinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_1, periodDateList
        )
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                handleTimeSelection(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        binding.spinner.setSelection(0)
    }

    // 处理时间选择
    private fun handleTimeSelection(position: Int) {
        when (position) {
            0 -> {//当天
                startTime = TimeUtils.millis2String(
                    TimeUtils.getNowMills(),
                    "yyyy-MM-dd"
                ) + " 00:00:00"
            }

            1 -> {//两天内
                startTime = TimeUtils.millis2String(
                    TimeUtils.getNowMills() - 2L * 24 * 3600 * 1000,
                    "yyyy-MM-dd"
                ) + " 00:00:00"
            }

            2 -> {//三天内
                startTime = TimeUtils.millis2String(
                    TimeUtils.getNowMills() - 3L * 24 * 3600 * 1000,
                    "yyyy-MM-dd"
                ) + " 00:00:00"
            }

            3 -> {//一周内
                startTime = TimeUtils.millis2String(
                    TimeUtils.getNowMills() - 7L * 24 * 3600 * 1000,
                    "yyyy-MM-dd"
                ) + " 00:00:00"
            }

            4 -> {//一个月内
                startTime = TimeUtils.millis2String(
                    TimeUtils.getNowMills() - 30L * 24 * 3600 * 1000,
                    "yyyy-MM-dd"
                ) + " 00:00:00"
            }

            5 -> {//三个月内
                startTime = TimeUtils.millis2String(
                    TimeUtils.getNowMills() - 90L * 24 * 3600 * 1000,
                    "yyyy-MM-dd"
                ) + " 00:00:00"
            }

            6 -> {//半年内
                startTime = TimeUtils.millis2String(
                    TimeUtils.getNowMills() - 180L * 24 * 3600 * 1000,
                    "yyyy-MM-dd"
                ) + " 00:00:00"
            }

            7 -> {//一年内
                startTime = TimeUtils.millis2String(
                    TimeUtils.getNowMills() - 365L * 24 * 3600 * 1000,
                    "yyyy-MM-dd"
                ) + " 00:00:00"
            }
        }
        mStates.periodDate.set("$startTime ~ $endTime")
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
        startTime = TimeUtils.millis2String(
            TimeUtils.getNowMills(),
            "yyyy-MM-dd"
        ) + " 00:00:00"
        endTime = TimeUtils.millis2String(
            TimeUtils.getNowMills(),
            "yyyy-MM-dd"
        ) + " 23:59:59"
        mStates.periodDate.set("$startTime ~ $endTime")
    }

    override fun createObserver() {
        // 观察UI状态，处理需要用户交互的情况
        mStates.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MonitoringDataExportViewModel.UiState.Ready -> {
                    hideLoading()
                }

                is MonitoringDataExportViewModel.UiState.Transferring -> {
                    showLoading("正在传输数据...")
                }

                is MonitoringDataExportViewModel.UiState.TransferSuccess -> {
                    hideLoading()
                    ToastUtils.showShort("数据传输完成，共${state.totalCount}条")
                }

                is MonitoringDataExportViewModel.UiState.Exporting -> {
                    showLoading("正在导出Excel...")
                }

                is MonitoringDataExportViewModel.UiState.ExportSuccess -> {
                    hideLoading()
                    // 询问用户是否分享文件
                    showMessage(
                        "Excel文件已导出成功！\n是否要分享文件？",
                        "导出成功",
                        "分享文件",
                        { shareFile(state.uri) },
                        "稍后处理"
                    )
                }

                is MonitoringDataExportViewModel.UiState.Error -> {
                    hideLoading()
                    ToastUtils.showShort(state.message)
                }
            }
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        /** 开始传输按钮 */
        fun onStartTransferClick() {
            if (!validateTime()) return

            // 根据按钮状态决定是开始还是停止传输
            if (mStates.startTransferButtonText.get() == "开始传输") {
                mStates.startDataTransfer(startTime, endTime)
            } else {
                mStates.stopTransfer()
            }
        }

        /** 导出Excel按钮 */
        fun onExportExcelClick() {
            if (!validateTime()) return
            mStates.exportToExcel(startTime, endTime)
        }
    }

    /** 显示日期时间选择器 */
    private fun showDateTimePicker(title: String, onDateSelected: (Long) -> Unit) {
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

        datePicker.addOnPositiveButtonClickListener { selection -> onDateSelected(selection) }

        datePicker.show(childFragmentManager, "datePicker")
    }

    /** 验证时间选择 */
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
     * 分享文件
     */
    private fun shareFile(uri: Uri) {
        try {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "监测数据报告")
                putExtra(Intent.EXTRA_TEXT, "请查收监测数据Excel报告")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "分享监测数据"))

        } catch (e: Exception) {
            Timber.e(e, "分享文件失败")
            Toaster.show("分享失败：${e.message}")
        }
    }

    /** 显示加载框 */
    private fun showLoading(message: String) {
        //        DialogLoadingUtils.showLoading(requireContext(), message)
        mStates.msg.set(message)
    }

    /** 隐藏加载框 */
    private fun hideLoading() {
        //        DialogLoadingUtils.hideLoading()
        mStates.msg.set("")
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        /** 创建Fragment实例 */
        fun newInstance(deviceSn: String, apiKey: String): MonitoringDataExportFragment {
            return MonitoringDataExportFragment().apply {
                arguments =
                    Bundle().apply {
                        putString("deviceSn", deviceSn)
                        putString("apiKey", apiKey)
                    }
            }
        }
    }
}
