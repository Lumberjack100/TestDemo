package com.shmedo.mcloudapp.ui.page.device.hac.dialog

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.lxj.xpopup.core.CenterPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.HacErrorProtectionPopupBinding
import com.shmedo.mcloudapp.ui.viewmodel.state.HacErrorProtectionTipViewModel

/**
 * 创建者：gonghe
 * 创建时间：2024/5/19
 * 描述： TODO
 */
class HacErrorProtectionTip(context: Context) : CenterPopupView(context) {
    private lateinit var binding: HacErrorProtectionPopupBinding
    private val stateVM: HacErrorProtectionTipViewModel by lazy { HacErrorProtectionTipViewModel() }


    fun setData(
        content: String = ""
    ): HacErrorProtectionTip {
        stateVM.content.set(content)
        return this
    }

    override fun getImplLayoutId(): Int {
        return R.layout.hac_error_protection_popup
    }

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind(popupImplView)!!
        binding.stateVM = stateVM
        binding.btnSure.setOnClickListener {
            dismiss()
        }
    }
}