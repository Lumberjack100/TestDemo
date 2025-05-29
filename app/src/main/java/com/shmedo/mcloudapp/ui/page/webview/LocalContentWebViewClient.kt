package com.shmedo.mcloudapp.ui.page.webview

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebViewAssetLoader
import com.just.agentweb.MiddlewareWebClientBase
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2025/5/22
 * 描述：用于处理本地资源加载的WebViewClient
 */
class LocalContentWebViewClient(context: Context) : MiddlewareWebClientBase() {

    private val assetLoader = WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
        .build()

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val url = request.url
        Timber.d("拦截请求: $url")
        return assetLoader.shouldInterceptRequest(url) ?: super.shouldInterceptRequest(view, request)
    }

}