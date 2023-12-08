package com.shmedo.mcloudapp.device.ui.mr702.fragment.dialog

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.lxj.xpopup.core.CenterPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.CustomMr702ParamImportPopupBinding
import com.shmedo.mcloudapp.device.viewmodel.state.MR702EquipmentOperationViewModel

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
    private var clickListener: OnClickListener? = null


    fun setTitle(
        title: String = "",
        vm: MR702EquipmentOperationViewModel
    ): MR702ParamImportPopupView {
        this.title = title
        this.stateVM = vm
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
        fun onConfirmClick(configUrl: String, md5: String, size: Int)
    }
}