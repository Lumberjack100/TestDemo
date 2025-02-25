package com.shmedo.mcloudapp.ui.page.device.mr702.fragment.port

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702PlusePortViewModel

/**
 * @author：gonghe
 * @time: 2025/2/25
 * @desc: 脉冲端口
 *
 */
class MR702PlusePortFragment : Fragment() {

    companion object {
        fun newInstance() = MR702PlusePortFragment()
    }

    private val viewModel: MR702PlusePortViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_mr702_pluse_port, container, false)
    }
}