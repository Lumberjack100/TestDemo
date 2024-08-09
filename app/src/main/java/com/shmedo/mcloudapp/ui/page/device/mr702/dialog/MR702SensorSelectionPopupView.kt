package com.shmedo.mcloudapp.ui.page.device.mr702.dialog

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import com.lxj.xpopup.core.CenterPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.CustomMr702SensorSelectionPopupBinding
import com.shmedo.mcloudapp.model.SensorModel

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/19 <br/>
 * 描述：     选择传感器
 */
class MR702SensorSelectionPopupView(context: Context) : CenterPopupView(context) {
    private lateinit var binding: CustomMr702SensorSelectionPopupBinding

    private var title: String = ""
    private val sensorList: MutableList<SensorModel> = mutableListOf()
    private lateinit var data: List<String>
    private var selectedSensor: SensorModel? = null
    private var selectListener: OnSelectListener? = null

    fun setData(
        title: String = "",
        list: List<SensorModel>,
        isSupportedCustom: Boolean = false
    ): MR702SensorSelectionPopupView {
        this.title = title
        this.sensorList.addAll(list)
        if (isSupportedCustom) {
            this.sensorList.add(
                SensorModel(
                    sensorName = "自定义传感器",
                    sensorType = "000",
                    modelToken = "000"
                )
            )
        }
        this.data = sensorList.map { it.sensorName }
        return this
    }

    fun setSelectListener(selectListener: OnSelectListener): MR702SensorSelectionPopupView {
        this.selectListener = selectListener
        return this
    }

    override fun getImplLayoutId(): Int {
        return R.layout.custom_mr702_sensor_selection_popup
    }

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind(popupImplView)!!
        if (title.isNotEmpty())
            binding.tvTitle.text = title

        initAdapter()
        binding.tvOk.setOnClickListener {
            selectedSensor?.let {
                selectListener?.onSelect(it)
            }
            dismiss()
        }
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun initAdapter() {
        if (data.isNotEmpty()) {
            binding.spinner.adapter = ArrayAdapter(
                context,
                android.R.layout.simple_list_item_1,
                data
            )
        }
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedSensor = sensorList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        binding.spinner.setSelection(0)
    }

    interface OnSelectListener {
        fun onSelect(sensorModel: SensorModel)
    }
}