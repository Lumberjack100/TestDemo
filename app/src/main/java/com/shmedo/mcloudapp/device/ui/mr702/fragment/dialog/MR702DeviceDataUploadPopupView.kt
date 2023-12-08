package com.shmedo.mcloudapp.device.ui.mr702.fragment.dialog

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.TimeUtils
import com.lxj.xpopup.core.CenterPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.CustomMr702DeviceDataUploadPopupBinding
import com.shmedo.mcloudapp.device.viewmodel.state.MR702EquipmentOperationViewModel

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/8
 *
 * 描述： MR702 设备数据上传
 *
 *
 */
class MR702DeviceDataUploadPopupView(context: Context) : CenterPopupView(context) {
    private lateinit var binding: CustomMr702DeviceDataUploadPopupBinding
    private lateinit var stateVM: MR702EquipmentOperationViewModel

    private var title: String = ""
    private val data: List<String> = arrayListOf("日志", "传感器数据")
    private var selectedType: Int = 1
    private var clickListener: OnClickListener? = null

    private var startTime: String = TimeUtils.millis2String(
        TimeUtils.getNowMills() - 10L * 24 * 3600 * 1000,
        "yyyy/MM/dd"
    )//10年前
    private val endTime: String = TimeUtils.millis2String(TimeUtils.getNowMills(), "yyyy/MM/dd")


    fun setTitle(
        title: String = "",
        vm: MR702EquipmentOperationViewModel
    ): MR702DeviceDataUploadPopupView {
        this.title = title
        this.stateVM = vm
        return this
    }

    fun setClickListener(clickListener: OnClickListener): MR702DeviceDataUploadPopupView {
        this.clickListener = clickListener
        return this
    }

    override fun getImplLayoutId(): Int {
        return R.layout.custom_mr702_device_data_upload_popup
    }

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind(popupImplView)!!
        binding.stateVM = stateVM

        if (title.isNotEmpty())
            binding.popupHead.tvTitle.text = title

        binding.tvTime.text = "$startTime-$endTime"
        initAdapter()

        binding.popupHead.ivClose.setOnClickListener {
            dismiss()
        }
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
        binding.tvConfirm.setOnClickListener {
            clickListener?.onConfirmClick(
                selectedType,
                binding.tvTime.text.toString(),
            )
        }

        binding.popupFooter.tvOk.setOnClickListener {
            dismiss()
        }
    }

    private fun initAdapter() {
        binding.spinner.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_list_item_1,
            data
        )
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedType = position + 1
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    interface OnClickListener {
        fun onConfirmClick(type: Int, time: String)
    }
}