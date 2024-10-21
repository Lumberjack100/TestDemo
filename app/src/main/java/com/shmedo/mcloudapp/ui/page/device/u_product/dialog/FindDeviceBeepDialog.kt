package com.shmedo.mcloudapp.ui.page.device.u_product.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseVmDbDialogFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.FindDeviceBeepViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 创建者：gonghe
 * 创建时间：2024/10/18
 * 描述： 查找设备蜂鸣弹窗
 */
class FindDeviceBeepDialog : BaseVmDbDialogFragment() {
    private val mStates: FindDeviceBeepViewModel by viewModels()

    private var productType = ProductType.UnKnown
    private val logoResIdList: MutableList<Int> = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化 ViewModel 的状态，仅在第一次创建时设置
        if (savedInstanceState == null) {
            productType =
                arguments?.getParcelable(AppContants.Extras.PRODUCT_TYPE) ?: ProductType.UnKnown
        }
        initLogoResIds()
    }

    private fun initLogoResIds() {
        logoResIdList.clear()
        when (productType) {
            ProductType.U_D_1, ProductType.U_D_2 -> {
                logoResIdList.addAll(
                    arrayListOf(
                        R.drawable.device_logo_niweiji,
                        R.drawable.device_logo_niweiji_alarm,
                        R.drawable.device_logo_niweiji_error
                    )
                )
            }

            ProductType.GNSS_M_5 -> {
                logoResIdList.addAll(
                    arrayListOf(
                        R.drawable.device_logo_m50,
                        R.drawable.device_logo_m50_alarm,
                        R.drawable.device_logo_m50_error
                    )
                )
            }

            else -> {

            }
        }
    }

    override val dataBindingConfig: DataBindingConfig
        get() = DataBindingConfig(
            R.layout.fragment_find_device_beep_dialog,
            BR.stateVM,
            mStates
        ).addBindingParam(
            BR.click,
            ClickProxy()
        )

    override fun setWindowStyle(gravity: Int) {
        super.setWindowStyle(Gravity.CENTER)
    }

    override fun initView(savedInstanceState: Bundle?) {
        startBeep()
    }

    private fun startBeep() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeat(REPEAT_POLL_NUM) { // Repeat the cycle 20 times
                for (logoResId in logoResIdList) {
                    mStates.deviceLogoResId.set(logoResId)
//                    animateLogo()
                    delay(500) // Wait for 0.5 seconds
                }
            }
            dismiss() // Close the dialog after completion
        }
    }

    private fun animateLogo() {
        val fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)

        fadeIn.duration = 250 // Set duration to half of the delay for smooth transition
        fadeOut.duration = 250

        val logoImageView = view?.findViewById<ImageView>(R.id.iv_device_logo)
        logoImageView?.let { imageView ->
            imageView.startAnimation(fadeOut)
            imageView.startAnimation(fadeIn)
        }
    }

    inner class ClickProxy {
        fun onCloseClick() {
            dismiss()
        }
    }

    companion object {
        val TAG = FindDeviceBeepDialog::class.java.simpleName
        const val REPEAT_POLL_NUM = 20

        fun newInstance(type: ProductType = ProductType.UnKnown): FindDeviceBeepDialog {
            return FindDeviceBeepDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
                }
            }
        }
    }
}