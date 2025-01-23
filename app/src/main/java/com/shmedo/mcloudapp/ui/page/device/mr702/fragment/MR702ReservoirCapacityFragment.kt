package com.shmedo.mcloudapp.ui.page.device.mr702.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702ReservoirCapacityViewModel

/**
 * @author：gonghe
 * @time: 2025/1/23
 * @desc: 库容计算
 *
 */
class MR702ReservoirCapacityFragment : Fragment() {

    companion object {
        fun newInstance() = MR702ReservoirCapacityFragment()
    }

    private val viewModel: MR702ReservoirCapacityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_mr702_reservoir_capacity, container, false)
    }
}