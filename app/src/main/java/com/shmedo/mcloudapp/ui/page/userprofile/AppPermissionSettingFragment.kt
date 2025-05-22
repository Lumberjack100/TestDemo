package com.shmedo.mcloudapp.ui.page.userprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.viewmodel.state.AppPermissionSettingViewModel

class AppPermissionSettingFragment : Fragment() {

    companion object {
        fun newInstance() = AppPermissionSettingFragment()
    }

    private val viewModel: AppPermissionSettingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_app_permission_setting, container, false)
    }
}