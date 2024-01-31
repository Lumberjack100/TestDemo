package com.shmedo.mcloudapp.device.ui.das.fragment.externalsensor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shmedo.mcloudapp.R

class DasExternalSensorListFragment : Fragment() {

    companion object {
        fun newInstance() = DasExternalSensorListFragment()
    }

    private lateinit var viewModel: DasExternalSensorListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_das_external_sensor_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DasExternalSensorListViewModel::class.java)
        // TODO: Use the ViewModel
    }

}