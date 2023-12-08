package com.shmedo.mcloudapp.device.ui.mr702.fragment.dialog

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.lxj.xpopup.core.CenterPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.CustomMr702ParamExportPopupBinding
import com.shmedo.mcloudapp.device.viewmodel.state.MR702EquipmentOperationViewModel

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/5
 *
 * 描述：参数导出
 */
class MR702ParamExportPopupView(context: Context) : CenterPopupView(context) {
    private lateinit var binding: CustomMr702ParamExportPopupBinding
    private lateinit var stateVM: MR702EquipmentOperationViewModel

    private var title: String = ""
    private var clickListener: OnClickListener? = null


    fun setTitle(
        title: String = "",
        vm: MR702EquipmentOperationViewModel
    ): MR702ParamExportPopupView {
        this.title = title
        this.stateVM = vm
        return this
    }

    fun setClickListener(clickListener: OnClickListener): MR702ParamExportPopupView {
        this.clickListener = clickListener
        return this
    }

    override fun getImplLayoutId(): Int {
        return R.layout.custom_mr702_param_export_popup
    }

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind(popupImplView)!!
        binding.stateVM = stateVM

        if (title.isNotEmpty())
            binding.popupHead.tvTitle.text = title

        binding.popupHead.ivClose.setOnClickListener {
            dismiss()
        }
        binding.tvOk.setOnClickListener {
            if (binding.tvOk.text == "导出") {
                clickListener?.onExportClick()
                return@setOnClickListener
            }
            dismiss()
        }
    }


    interface OnClickListener {
        fun onExportClick()
    }
}