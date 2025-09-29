package com.shmedo.mcloudapp.ui.page.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.navigation.findNavController
import com.blankj.utilcode.util.NetworkUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.ActivityMainBinding
import com.shmedo.mcloudapp.extensions.getActivityScopeViewModel
import com.shmedo.mcloudapp.extensions.getAppViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.model.CustomActivityResult
import com.shmedo.mcloudapp.ui.page.base.activity.BaseActivity
import com.shmedo.mcloudapp.ui.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.PageMessenger

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mMessenger: PageMessenger
    private lateinit var mStates: EmptyViewModel


    override fun initViewModel() {
        mMessenger = getAppViewModel()
        mStates = getActivityScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_main, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as ActivityMainBinding
        processBackPressed()
    }

    override fun initData() {
        binding.navHostFragment.post {
            setGraph()
        }
    }

    private fun setGraph() {
        val navController = findNavController(R.id.nav_host_fragment)
        if (!NetworkUtils.isConnected()) {
            navController.setGraph(R.navigation.offline_main_graph)
        } else {
            navController.setGraph(R.navigation.main_graph)
        }
    }

    //声明一个long类型变量：用于存放上一点击“返回键”的时刻
    private var mExitTime: Long = 0
    private fun processBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = nav(binding.navHostFragment)
                val destinationId = navController.currentDestination?.id
                val isHome = destinationId == R.id.mainFragment || destinationId == R.id.offlineMainFragment

                //如果当前界面不是主页，那么直接调用返回即可
                if (!isHome && navController.navigateUp()) {
                    return
                }

                //是主页，与上次点击返回键时刻作差，大于2000ms则认为是误操作，使用Toast进行提示
                val now = System.currentTimeMillis()
                if (now - mExitTime > EXIT_INTERVAL) {
                    Toaster.show("再按一次退出程序")
                    mExitTime = now
                } else {
                    finish()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mMessenger.dispatchActivityResult(CustomActivityResult(requestCode, resultCode, data))
    }

    companion object {
        private const val EXIT_INTERVAL = 2_000L

        fun start(context: Context) {
            val intent = Intent()
            intent.setClass(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }
}
