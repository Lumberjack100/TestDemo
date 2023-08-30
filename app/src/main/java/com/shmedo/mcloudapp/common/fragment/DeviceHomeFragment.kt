package com.shmedo.mcloudapp.common.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.viewmodel.state.DeviceHomeViewModel

class DeviceHomeFragment : Fragment() {

    companion object {
        fun newInstance() = DeviceHomeFragment()
    }

    private lateinit var viewModel: DeviceHomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_device_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DeviceHomeViewModel::class.java)
        // TODO: Use the ViewModel
    }

}