package com.shmedo.mcloudapp.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.base.BaseActivity
import com.shmedo.mcloudapp.viewmodel.state.SplashViewModel

class SplashActivity : BaseActivity() {
    private val mStates: SplashViewModel by viewModels()
    override fun initView(savedInstanceState: Bundle?) {
        TODO("Not yet implemented")

        CollapsingToolbarLayout
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        TODO("Not yet implemented")
    }
}