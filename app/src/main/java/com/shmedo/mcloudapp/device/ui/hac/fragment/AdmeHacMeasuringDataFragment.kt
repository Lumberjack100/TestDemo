package com.shmedo.mcloudapp.device.ui.hac.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeHacMeasuringDataViewModel

class AdmeHacMeasuringDataFragment : Fragment() {

    companion object {
        fun newInstance() = AdmeHacMeasuringDataFragment()
    }

    private val viewModel: AdmeHacMeasuringDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_adme_hac_measuring_data, container, false)
    }
}