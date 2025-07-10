package com.shmedo.mcloudapp.ui.viewmodel

import android.net.Uri
import android.view.View
import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.UriUtils
import com.kunminx.architecture.domain.message.MutableResult
import com.kunminx.architecture.domain.message.Result
import com.shmedo.core.data.repository.MonitoringDataRepository
import com.shmedo.core.model.MonitoringDataQuery
import com.shmedo.core.model.TransferState
import com.shmedo.lib.ble.communicate.service.MedoBleRepository
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.mcloudapp.ui.manager.MonitoringDataTransferManager
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField
import com.shmedo.mcloudapp.utils.ExcelExporter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.UUID

/**
 * 监测数据导出 ViewModel
 */
class MonitoringDataExportViewModel(
    private val monitoringDataRepository: MonitoringDataRepository,
    private val bleRepository: MedoBleRepository,
    private val iotParseManager: IOTParserManager
) : ViewModel() {

    val msg = NonNullObservableField("")

    val periodDate = NonNullObservableField("")

    // UI状态 - 使用 ObservableField 实现数据绑定
    val transferStatusText = NonNullObservableField("传输状态：未开始")
    val progressPercentage = NonNullObservableField(0)
    val transferSpeedText = NonNullObservableField("0 KB/s")
    val localDataCountText = NonNullObservableField("本地数据：0 条")
    val transferredDataSizeText = NonNullObservableField("已传输数据大小：0 B")

    // 按钮状态
    val canStartTransfer = NonNullObservableField(true)
    val canExportExcel = NonNullObservableField(false)
    val startTransferButtonText = NonNullObservableField("开始传输")

    // 传输进度卡片可见性
    val transferProgressVisibility = ObservableInt(View.GONE)

    // UI状态
    private val _uiState = MutableResult<UiState>()
    val uiState: Result<UiState> = _uiState

    // 传输状态
    private val _transferState = MutableResult<TransferState>()
    val transferState: Result<TransferState> = _transferState

    // 当前设备信息
    private var productType = ProductType.UnKnown
    private var currentDeviceSn: String = ""
    private var apiKey: String = ""

    // 数据传输管理器
    private lateinit var transferManager: MonitoringDataTransferManager

    init {
        initTransferManager()
    }

    /**
     * 初始化传输管理器
     */
    private fun initTransferManager() {
        transferManager = MonitoringDataTransferManager(
            bleRepository = bleRepository,
            iotParseManager = iotParseManager,
            scope = viewModelScope
        )

        // 监听传输状态
        transferManager.transferState
            .onEach { state ->
                _transferState.value = state
                handleTransferStateChange(state)
            }
            .launchIn(viewModelScope)
    }

    /**
     * 设置设备信息
     */
    fun setDeviceInfo(productType: ProductType, deviceSn: String, apiKey: String) {
        this.productType = productType
        this.currentDeviceSn = deviceSn
        this.apiKey = apiKey

        // 加载本地已有数据
        loadLocalData()
    }

    /**
     * 加载本地数据
     */
    private fun loadLocalData() {
        viewModelScope.launch {
            try {
                val count = monitoringDataRepository.getDataCount(currentDeviceSn)
                updateUIState(UiState.Ready(localDataCount = count))
            } catch (e: Exception) {
                Timber.e(e, "加载本地数据失败")
            }
        }
    }

    /**
     * 开始传输数据
     */
    fun startDataTransfer(startTime: String, endTime: String) {
        if (currentDeviceSn.isEmpty() || apiKey.isEmpty()) {
            updateUIState(UiState.Error("设备信息未设置"))
            return
        }

        val query = MonitoringDataQuery(
            beginTime = startTime,
            endTime = endTime,
            apiKey = apiKey,
            msgId = UUID.randomUUID().toString()
        )

        updateUIState(UiState.Transferring)

        transferManager.startTransfer(query) { dataList ->
            // 保存每页数据到本地
            viewModelScope.launch {
                monitoringDataRepository.saveMonitoringData(currentDeviceSn, dataList)
            }
        }
    }

    /**
     * 停止传输
     */
    fun stopTransfer() {
        transferManager.stopTransfer()
        updateUIState(UiState.Ready())
    }

    /**
     * 导出数据到Excel
     */
    fun exportToExcel(startTime: String, endTime: String) {
        viewModelScope.launch {
            try {
                updateUIState(UiState.Exporting)

                // 查询时间范围内的数据
                val dataList = monitoringDataRepository.getDataByDeviceAndTimeRange(
                    currentDeviceSn,
                    startTime,
                    endTime
                )

                if (dataList.isEmpty()) {
                    updateUIState(UiState.Error("没有找到数据"))
                    return@launch
                }

                // 导出到Excel
                val outputDir = File(PathUtils.getExternalDownloadsPath(), "MCloudApp/Export")
                FileUtils.createOrExistsDir(outputDir)

                val file = ExcelExporter.exportToExcel(dataList, currentDeviceSn, outputDir)

                if (file != null) {
                    updateUIState(UiState.ExportSuccess(file.absolutePath, UriUtils.file2Uri(file)))
                } else {
                    updateUIState(UiState.Error("导出失败"))
                }

            } catch (e: Exception) {
                Timber.e(e, "导出Excel失败")
                updateUIState(UiState.Error("导出失败: ${e.message}"))
            }
        }
    }

    /**
     * 处理传输状态变化
     */
    private fun handleTransferStateChange(state: TransferState) {
        // 更新UI状态显示
        updateTransferStateUI(state)

        when (state) {
            is TransferState.Success -> {
                loadLocalData() // 重新加载本地数据
                updateUIState(UiState.TransferSuccess(state.totalCount))
            }

            is TransferState.Error -> {
                updateUIState(UiState.Error(state.message))
            }

            else -> {
                // 其他状态通过transferState LiveData传递
            }
        }
    }

    /**
     * 更新传输状态UI
     */
    private fun updateTransferStateUI(state: TransferState) {
        when (state) {
            is TransferState.Idle -> {
                transferProgressVisibility.set(View.GONE)
                transferStatusText.set("传输状态：未开始")
                transferredDataSizeText.set("已传输数据大小：0 B")
            }

            is TransferState.Transferring -> {
                transferProgressVisibility.set(View.VISIBLE)
                transferStatusText.set("传输状态：传输中")
                progressPercentage.set((state.progress * 100).toInt())
                transferSpeedText.set(state.speed)
                transferredDataSizeText.set("已传输数据大小：${state.transferredDataSize}")
            }

            is TransferState.Success -> {
                transferProgressVisibility.set(View.GONE)
                transferStatusText.set("传输状态：完成")
            }

            is TransferState.Error -> {
                transferProgressVisibility.set(View.GONE)
                transferStatusText.set("传输状态：失败")
            }
        }
    }

    /**
     * 更新UI状态
     */
    private fun updateUIState(state: UiState) {
        _uiState.value = state

        when (state) {
            is UiState.Ready -> {
                localDataCountText.set("本地数据：${state.localDataCount} 条")
                canStartTransfer.set(true)
                canExportExcel.set(state.localDataCount > 0)
                startTransferButtonText.set("开始传输")
            }

            is UiState.Transferring -> {
                canStartTransfer.set(true)
                canExportExcel.set(false)
                startTransferButtonText.set("停止传输")
            }

            is UiState.TransferSuccess -> {
                canStartTransfer.set(true)
                canExportExcel.set(true)
                startTransferButtonText.set("开始传输")
            }

            is UiState.Exporting -> {
                canStartTransfer.set(false)
                canExportExcel.set(false)
            }

            is UiState.ExportSuccess -> {
                canStartTransfer.set(true)
                canExportExcel.set(true)
            }

            is UiState.Error -> {
                canStartTransfer.set(true)
                canExportExcel.set(true)
                startTransferButtonText.set("开始传输")
            }
        }
    }

    /**
     * 清除本地数据
     */
    fun clearLocalData() {
        viewModelScope.launch {
            try {
                monitoringDataRepository.clearDeviceData(currentDeviceSn)
                loadLocalData()
            } catch (e: Exception) {
                Timber.e(e, "清除数据失败")
                updateUIState(UiState.Error("清除数据失败"))
            }
        }
    }

    /**
     * UI状态
     */
    sealed class UiState {
        data class Ready(val localDataCount: Int = 0) : UiState()
        object Transferring : UiState()
        data class TransferSuccess(val totalCount: Int) : UiState()
        object Exporting : UiState()
        data class ExportSuccess(val filePath: String, val uri: Uri) : UiState()
        data class Error(val message: String) : UiState()
    }
} 