package com.shmedo.mcloudapp.device.ui.mr702.fragment.deviceinterface

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.viewmodel.state.MR702RS4852InterfaceViewModel

class MR702RS4852InterfaceFragment : Fragment() {

    companion object {
        fun newInstance() = MR702RS4852InterfaceFragment()
    }

    private val viewModel: MR702RS4852InterfaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_mr702_rs485_2_interface, container, false)
    }
}