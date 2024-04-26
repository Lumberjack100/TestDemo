package com.shmedo.mcloudapp.device.ui.u_product.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.viewmodel.state.UDProductSensorParamViewModel

/**
 * @author：gonghe
 * @time: 2024/4/26
 * @desc: 泥位计传感参数
 *
 */
class UDProductSensorParamFragment : Fragment() {

    companion object {
        fun newInstance() = UDProductSensorParamFragment()
    }

    private val viewModel: UDProductSensorParamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_u_d_product_sensor_param, container, false)
    }
}