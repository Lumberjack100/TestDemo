package com.shmedo.mcloudapp.device.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.viewmodel.state.BleCustomCommandLogPrintViewModel

class BleCustomCommandLogPrintFragment : Fragment() {

    companion object {
        fun newInstance() = BleCustomCommandLogPrintFragment()
    }

    private lateinit var viewModel: BleCustomCommandLogPrintViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ble_custom_command_log_print, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BleCustomCommandLogPrintViewModel::class.java)
        // TODO: Use the ViewModel
    }

}