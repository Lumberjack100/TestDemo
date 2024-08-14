/*
 * Copyright (C) 2018 xuexiangjys(xuexiangjys@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shmedo.lib.network.interceptor

import com.xuexiang.xupdate.proxy.IUpdateHttpService
import com.xuexiang.xupdate.proxy.IUpdateHttpService.DownloadCallback
import com.xuexiang.xupdate.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import rxhttp.RxHttpPlugins
import rxhttp.toDownloadFlow
import rxhttp.wrapper.param.RxHttp
import timber.log.Timber

/**
 * 使用okhttp
 *
 * @author xuexiang
 * @since 2018/7/10 下午4:04
 */
class OKHttpUpdateHttpService : IUpdateHttpService {
    override fun asyncGet(
        url: String,
        params: Map<String, Any>,
        callBack: IUpdateHttpService.Callback
    ) {}

    override fun asyncPost(
        url: String,
        params: Map<String, Any>,
        callBack: IUpdateHttpService.Callback
    ) {}

    override fun download(url: String, path: String, fileName: String, callback: DownloadCallback) {
        //如果想使用RxJava或Await下载，更改以下代码即可
        CoroutineScope(Dispatchers.Main).launch {
            Timber.i("开始下载")
            val destPath = "${path}/${fileName}"
            callback.onStart()

            RxHttp.get(url)
                .tag(url)
                .toDownloadFlow(destPath, true)
                .onProgress {
                    val currentProgress = it.progress //当前进度 0-100
                    val currentSize = it.currentSize //当前已下载的字节大小
                    val totalSize = it.totalSize //要下载的总字节大小
                    callback.onProgress(currentProgress.toFloat() / 100, totalSize)
                }.catch {
                    //异常回调
                    callback.onError(it)
                }.collect {
                    callback.onSuccess(FileUtils.getFileByPath(destPath))
                    Timber.i("下载完成")
                }
        }
    }

    override fun cancelDownload(url: String) {
        RxHttpPlugins.cancelAll(url)
    }
}