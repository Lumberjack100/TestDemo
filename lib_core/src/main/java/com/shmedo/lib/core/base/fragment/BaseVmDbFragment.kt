/*
 * Copyright 2018-present KunMinX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shmedo.lib.core.base.fragment


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import com.kunminx.architecture.ui.page.DataBindingFragment
import com.shmedo.lib.core.network.manager.NetState
import com.shmedo.lib.core.network.manager.NetworkStateManager
import timber.log.Timber

/**
 * Create by KunMinX at 19/7/11
 */
abstract class BaseVmDbFragment : DataBindingFragment() {
    private val handler = Handler(Looper.getMainLooper())

    //是否第一次加载
    protected var isFirst = true

    override fun initViewModel() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated,Fragment=%s", javaClass.simpleName)
        super.onViewCreated(view, savedInstanceState)
        isFirst = true
        initView(savedInstanceState)
        initData()
        createObserver()
    }

    /**
     * 初始化view
     */
    abstract fun initView(savedInstanceState: Bundle?)

    /**
     * Fragment执行onCreate后触发的方法
     */
    abstract fun initData()

    /**
     * 创建观察者
     */
    abstract fun createObserver()

    override fun onResume() {
        Timber.i("onResume,Fragment=%s", javaClass.simpleName)
        super.onResume()
        onVisible()
    }

    /**
     * 是否需要懒加载
     */
    private fun onVisible() {
        if (lifecycle.currentState == Lifecycle.State.STARTED && isFirst) {
            // 延迟加载 防止 切换动画还没执行完毕时数据就已经加载好了，这时页面会有渲染卡顿
            handler.postDelayed({
                lazyLoadData()
                //在Fragment中，只有懒加载过了才能开启网络变化监听
                NetworkStateManager.instance.mNetworkStateCallback.observe(
                    viewLifecycleOwner,
                    Observer {
                        //不是首次订阅时调用方法，防止数据第一次监听错误
                        if (!isFirst) {
                            onNetworkStateChanged(it)
                        }
                    })
                isFirst = false
            }, lazyLoadTime())
        }
    }

    /**
     * 懒加载 只有当前fragment视图显示时才会触发该方法
     */
    open fun lazyLoadData() {}

    /**
     * 网络变化监听 子类重写
     */
    open fun onNetworkStateChanged(netState: NetState) {}

    /**
     * 延迟加载 防止 切换动画还没执行完毕时数据就已经加载好了，这时页面会有渲染卡顿  bug
     * 这里传入你想要延迟的时间，延迟时间可以设置比转场动画时间长一点 单位： 毫秒
     * 不传默认 300毫秒
     * @return Long
     */
    open fun lazyLoadTime(): Long {
        return 300
    }

    abstract fun showLoading(message: String = "请求网络中...")

    abstract fun dismissLoading()

    /**
     * TODO 覆盖 DataBindingActivity 中的 isDebug 方法,一直返回 false
     *
     * @return
     */
    override fun isDebug(): Boolean {
        return false
    }

    override fun onPause() {
        Timber.i("onPause,Fragment=%s", javaClass.simpleName)
        super.onPause()
    }

    override fun onStop() {
        Timber.i("onStop,Fragment=%s", javaClass.simpleName)
        super.onStop()
    }

    override fun onDestroyView() {
        Timber.i("onDestroyView,Fragment=%s", javaClass.simpleName)
        super.onDestroyView()
    }

    override fun onDestroy() {
        Timber.i("onDestroy,Fragment=%s", javaClass.simpleName)
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
