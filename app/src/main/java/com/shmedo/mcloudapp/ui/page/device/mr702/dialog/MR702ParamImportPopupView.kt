package com.shmedo.mcloudapp.ui.page.device.mr702.dialog

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.hjq.toast.Toaster
import com.lxj.xpopup.core.CenterPopupView
import com.shmedo.core.data.repository.NetDataRepository
import com.shmedo.core.model.DeviceBackupInfo
import com.shmedo.core.model.DeviceDetailInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.lib.network.response.PageList
import com.shmedo.lib.network.response.ResponseStatus
import com.shmedo.lib.network.response.ResultSource
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.CustomMr702ParamImportPopupBinding
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702EquipmentOperationViewModel
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/8
 *
 * 描述： TODO
 *
 *
 */
class MR702ParamImportPopupView(context: Context) : CenterPopupView(context) {
    private lateinit var binding: CustomMr702ParamImportPopupBinding
    private lateinit var stateVM: MR702EquipmentOperationViewModel

    private var title: String = ""
    private var deviceSn: String = ""
    private var deviceId: String = ""
    private val backupInfoList: ArrayList<DeviceBackupInfo> = arrayListOf()
    private var selectedBackupInfo: DeviceBackupInfo? = null
    private var clickListener: OnClickListener? = null


    fun setTitle(
        title: String = "",
        vm: MR702EquipmentOperationViewModel
    ): MR702ParamImportPopupView {
        this.title = title
        this.stateVM = vm
        this.deviceSn = stateVM.sn.get()
        this.deviceId = stateVM.deviceId.get()
        return this
    }

    fun setClickListener(clickListener: OnClickListener): MR702ParamImportPopupView {
        this.clickListener = clickListener
        return this
    }

    override fun getImplLayoutId(): Int {
        return R.layout.custom_mr702_param_import_popup
    }

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind(popupImplView)!!
        binding.stateVM = stateVM

        if (title.isNotEmpty())
            binding.popupHead.tvTitle.text = title

        initAdapter()

        binding.popupHead.ivClose.setOnClickListener {
            dismiss()
        }
        binding.tvSearch.setOnClickListener {
            getData()
        }
        binding.tvOk.setOnClickListener {
            if (binding.tvOk.text == "导入") {
                clickListener?.onImportClick(selectedBackupInfo?.id.toString())
                return@setOnClickListener
            }
            dismiss()
        }
    }

    private fun initAdapter() {
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedBackupInfo = backupInfoList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    interface OnClickListener {
        fun onImportClick(backupID: String)
    }

    private fun getData() {
        lifecycleScope.launch {
            if (stateVM.sn.get() != deviceSn) {
                val deviceDetailInfo = getDeviceDetailInfo(stateVM.sn.get()) ?: return@launch
                deviceId = deviceDetailInfo.deviceInfo.id.toString()
            }

            queryDeviceBackupList(deviceId)?.let {
                val tempList = it.currentPageData
                if (tempList.isNullOrEmpty()) {
                    Toaster.show("没有查到备份数据")
                    return@launch
                }
                backupInfoList.clear()
                backupInfoList.addAll(tempList)
                val data = backupInfoList.map { info ->
                    info.backupVersion
                }
                binding.spinner.adapter = ArrayAdapter(
                    context,
                    android.R.layout.simple_list_item_1,
                    data
                )
                binding.spinner.setSelection(0)
            }
        }
    }

    private suspend fun getDeviceDetailInfo(deviceToken: String = ""): DeviceDetailInfo? {
        val jsonObjectRequest = JSONObject()
        try {
            jsonObjectRequest.put("deviceToken", deviceToken)
        } catch (e: JSONException) {
            Timber.e(e)
        }
        return NetDataRepository.instance.getDeviceDetailInfo(jsonObjectRequest.toString()) { error: Throwable ->
            Toaster.show(error.errorMsg)
        }
    }

    private suspend fun queryDeviceBackupList(
        deviceID: String,
        currentPage: Int = 1,
        pageSize: Int = 100
    ): PageList<DeviceBackupInfo>? {
        val jsonObjectRequest = JSONObject()
        try {
            jsonObjectRequest.put("deviceID", deviceID)
            jsonObjectRequest.put("currentPage", currentPage)
            jsonObjectRequest.put("pageSize", pageSize)
        } catch (e: JSONException) {
            Timber.e(e)
        }
        return NetDataRepository.instance.queryDeviceBackupListWithPage(jsonObjectRequest.toString()) { error: Throwable ->
            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = false
            responseStatus.errorMessage = error.errorMsg
            responseStatus.source = ResultSource.NETWORK
        }
    }
}