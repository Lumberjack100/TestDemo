package com.shmedo.mcloudapp.user.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.user.viewmodel.state.LogDataViewModel

class LogDataFragment : Fragment() {

    companion object {
        fun newInstance() = LogDataFragment()
    }

    private lateinit var viewModel: LogDataViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_log_data, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LogDataViewModel::class.java)
        // TODO: Use the ViewModel
    }

}