package com.shmedo.mcloudapp.ui.page.device.mr702.dialog

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.TimeUtils
import com.hjq.toast.Toaster
import com.lxj.xpopup.core.CenterPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.CustomManualSettingPopupBinding
import com.shmedo.mcloudapp.model.MonitoringElement
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702EquipmentOperationViewModel

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/6
 *
 * 描述： 人工置数
 *
 *
 */
class MR702ManualSettingPopupView(context: Context) : CenterPopupView(context) {
    private lateinit var binding: CustomManualSettingPopupBinding
    private lateinit var stateVM: MR702EquipmentOperationViewModel

    private var title: String = ""
    private lateinit var monitoringElementList: List<MonitoringElement>
    private lateinit var data: List<String>
    private var selectedElement: MonitoringElement? = null
    private var clickListener: OnClickListener? = null


    fun setTitle(
        title: String = "",
        list: List<MonitoringElement>,
        vm: MR702EquipmentOperationViewModel
    ): MR702ManualSettingPopupView {
        this.title = title
        this.monitoringElementList = list.toList()
        this.data = monitoringElementList.map { it.name }
        this.stateVM = vm
        return this
    }

    fun setClickListener(clickListener: OnClickListener): MR702ManualSettingPopupView {
        this.clickListener = clickListener
        return this
    }

    override fun getImplLayoutId(): Int {
        return R.layout.custom_manual_setting_popup
    }

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind(popupImplView)!!
        binding.stateVM = stateVM
        binding.stateVM!!.observationTime.set(TimeUtils.getNowString())
        binding.stateVM!!.unit.set(monitoringElementList[0].engUnit)

        if (title.isNotEmpty())
            binding.popupHead.tvTitle.text = title

        initAdapter()

        binding.popupHead.ivClose.setOnClickListener {
            dismiss()
        }
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
        binding.tvConfirm.setOnClickListener {
            if (binding.etCollectData.text.isNullOrEmpty()) {
                Toaster.show("请输入采集数据")
                return@setOnClickListener
            }
            clickListener?.onConfirmClick(
                System.currentTimeMillis(),
                selectedElement!!.code,
                binding.etCollectData.text.toString(),
                selectedElement!!.engUnit
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
                selectedElement = monitoringElementList[position]
                stateVM.unit.set(selectedElement!!.engUnit)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        binding.spinner.setSelection(0)
    }

    interface OnClickListener {
        fun onConfirmClick(otime: Long, type: Int, data: String, unit: String)
    }
}