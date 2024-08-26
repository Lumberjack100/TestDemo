package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.viewmodel.state.UDAltitudeParamViewModel
/**
 * @author：gonghe
 * @time: 2024/4/26
 * @desc: 泥位计传感参数
 *
 */
class UDAltitudeParamFragment : Fragment() {

    companion object {
        fun newInstance() = UDAltitudeParamFragment()
    }

    private val viewModel: UDAltitudeParamViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_ud_altitude_param, container, false)
    }
}