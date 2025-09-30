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
        setStyle(STYLE_NO_TITLE, R.style.TransparentLoadingDialog)
        isCancelable = false
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
            ProductType.M20, ProductType.GNSS_M_1, ProductType.GNSS_M_2 -> {
                logoResIdList.addAll(
                    arrayListOf(
                        R.drawable.device_logo_m20,
                        R.drawable.device_logo_m20_alarm,
                        R.drawable.device_logo_m20_error
                    )
                )
            }

            ProductType.GNSS_M_5, ProductType.GNSS_M_6, ProductType.GNSS_M_7, ProductType.GNSS_M_8 -> {
                logoResIdList.addAll(
                    arrayListOf(
                        R.drawable.device_logo_m50,
                        R.drawable.device_logo_m50_alarm,
                        R.drawable.device_logo_m50_error
                    )
                )
            }

            ProductType.GNSS_E_1, ProductType.GNSS_E_2-> {
                logoResIdList.addAll(
                    arrayListOf(
                        R.drawable.device_logo_e40,
                        R.drawable.device_logo_e40_alarm,
                        R.drawable.device_logo_e40_error
                    )
                )
            }

            ProductType.GNSS_E_3 -> {
                logoResIdList.addAll(
                    arrayListOf(
                        R.drawable.device_logo_e50_pro,
                        R.drawable.device_logo_e50_pro_alarm,
                        R.drawable.device_logo_e50_pro_error
                    )
                )
            }

            ProductType.GNSS_T_1-> {
                logoResIdList.addAll(
                    arrayListOf(
                        R.drawable.device_logo_gt600,
                        R.drawable.device_logo_gt600_alarm,
                        R.drawable.device_logo_gt600_error
                    )
                )
            }


            ProductType.COLLECTOR_G_0 -> {//自组网报警网关
                logoResIdList.addAll(
                    arrayListOf(
                        R.drawable.device_logo_gateway,
                        R.drawable.device_logo_gateway_alarm,
                        R.drawable.device_logo_gateway_error
                    )
                )
            }

            ProductType.DAS, ProductType.BHY -> {
                logoResIdList.addAll(
                    arrayListOf(
                        R.drawable.device_logo_das,
                        R.drawable.device_logo_das_alarm,
                        R.drawable.device_logo_das_error
                    )
                )
            }

            ProductType.COLLECTOR_R_1 -> {
                logoResIdList.addAll(
                    arrayListOf(
                        R.drawable.device_logo_mr701_new,
                        R.drawable.device_logo_mr701_new_alarm,
                        R.drawable.device_logo_mr701_new_error
                    )
                )
            }

            ProductType.COLLECTOR_R_2 -> {
                logoResIdList.addAll(
                    arrayListOf(
                        R.drawable.device_logo_mr702,
                        R.drawable.device_logo_mr702_alarm,
                        R.drawable.device_logo_mr702_error
                    )
                )
            }

            ProductType.LR200 -> {
                logoResIdList.addAll(
                    arrayListOf(
                        R.drawable.device_logo_bhy_3_lr200,
                        R.drawable.device_logo_bhy_3_lr200_alarm,
                        R.drawable.device_logo_bhy_3_lr200_error
                    )
                )
            }

            ProductType.U_I_1, ProductType.U_R_1 -> {
                logoResIdList.addAll(
                    arrayListOf(
                        R.drawable.device_logo_bhy_3s,
                        R.drawable.device_logo_bhy_3s_alarm,
                        R.drawable.device_logo_bhy_3s_error
                    )
                )
            }

            ProductType.U_D_1, ProductType.U_D_2, ProductType.U_D_3 -> {
                logoResIdList.addAll(
                    arrayListOf(
                        R.drawable.device_logo_dr030,
                        R.drawable.device_logo_dr030_alarm,
                        R.drawable.device_logo_dr030_error
                    )
                )
            }

            ProductType.LB20S -> {
                logoResIdList.addAll(
                    arrayListOf(
                        R.drawable.device_logo_lb20s,
                        R.drawable.device_logo_lb20s_alarm,
                        R.drawable.device_logo_lb20s_error
                    )
                )
            }
            else -> {
                logoResIdList.addAll(
                    arrayListOf(
                        R.drawable.device_logo_default,
                        R.drawable.device_logo_default_alarm,
                        R.drawable.device_logo_default_error
                    )
                )
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
        const val REPEAT_POLL_NUM = 7

        fun newInstance(type: ProductType = ProductType.UnKnown): FindDeviceBeepDialog {
            return FindDeviceBeepDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
                }
            }
        }
    }
}