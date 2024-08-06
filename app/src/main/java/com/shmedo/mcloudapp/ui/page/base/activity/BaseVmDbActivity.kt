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
package com.shmedo.mcloudapp.ui.page.base.activity

import android.content.res.Resources
import android.os.Bundle
import com.blankj.utilcode.util.AdaptScreenUtils
import com.blankj.utilcode.util.ScreenUtils
import com.kunminx.architecture.ui.page.DataBindingActivity
import timber.log.Timber

/**
 * Create by KunMinX at 19/8/1
 */
abstract class BaseVmDbActivity : DataBindingActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView(savedInstanceState)
        initData()
        createObserver()
    }

    /**
     * 初始化view
     */
    abstract fun initView(savedInstanceState: Bundle?)

    abstract fun initData()

    /**
     * 创建观察者
     */
    abstract fun createObserver()

    override fun getResources(): Resources {
        return if (ScreenUtils.isPortrait()) {
            AdaptScreenUtils.adaptWidth(super.getResources(), 360)
        } else {
            AdaptScreenUtils.adaptHeight(super.getResources(), 640)
        }
    }

    /**
     * TODO 覆盖 DataBindingActivity 中的 isDebug 方法,一直返回 false
     *
     * @return
     */
    override fun isDebug(): Boolean {
        return false
    }

    override fun onStart() {
        Timber.i("onStart,BaseVmDbActivity=%s", javaClass.simpleName)
        super.onStart()
    }

    override fun onResume() {
        Timber.i("onResume,BaseVmDbActivity=%s", javaClass.simpleName)
        super.onResume()
    }

    override fun onPause() {
        Timber.i("onPause,BaseVmDbActivity=%s", javaClass.simpleName)
        super.onPause()
    }

    override fun onStop() {
        Timber.i("onStop,BaseVmDbActivity=%s", javaClass.simpleName)
        super.onStop()
    }

    override fun onDestroy() {
        Timber.i("onDestroy,BaseVmDbActivity=%s", javaClass.simpleName)
        super.onDestroy()
    }
}
