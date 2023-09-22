package com.shmedo.mcloudapp.device.ui.mr702.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shmedo.mcloudapp.R

class MR702NetworkCommunicationFragment : Fragment() {

    companion object {
        fun newInstance() = MR702NetworkCommunicationFragment()
    }

    private val viewModel: MR702NetworkCommunicationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_m_r702_network_communication, container, false)
    }
}