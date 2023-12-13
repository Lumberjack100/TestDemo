package com.shmedo.mcloudapp.device.ui.adme.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeMeterWheelViewModel

class AdmeMeterWheelFragment : Fragment() {

    companion object {
        fun newInstance() = AdmeMeterWheelFragment()
    }

    private lateinit var viewModel: AdmeMeterWheelViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_adme_meter_wheel, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AdmeMeterWheelViewModel::class.java)
        // TODO: Use the ViewModel
    }

}