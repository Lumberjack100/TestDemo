package com.shmedo.lib.network

import android.app.Application
import com.blankj.utilcode.util.ToastUtils
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.network.converter.MyMoshiConverter
import com.shmedo.lib.network.util.OKHttpUpdateHttpService
import com.xuexiang.xupdate.XUpdate
import com.xuexiang.xupdate.entity.UpdateError
import com.xuexiang.xupdate.utils.UpdateUtils
import com.zhy.http.okhttp.OkHttpUtils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.param.Param
import java.util.concurrent.TimeUnit

/**
 * 创建者：gonghe <br/>
 *
 * 创建时间：2023/8/29 <br/>
 *
 * 描述：初始化网络请求配置
 */
object RxHttpManager {

    fun initial(application: Application) {
        initRxHttp()
        initOKHttpUtils()
        initUpdate(application)
    }

    private fun initRxHttp() {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        //设置读、写、连接超时时间为15s
        val client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(logging) // 日志拦截器
            .build()

        RxHttpPlugins.init(client)
            .setConverter(MyMoshiConverter.create()) //设置数据解析器，非必须
            .setOnParamAssembly { p: Param<*> ->                  //设置公共参数，非必须
                p.addHeader("access_type", "android")
                p.addHeader("access_service", "mcloud")
                p.addHeader("Authorization", MmkvCacheUtil.getToken()) //添加公共请求头
            }
    }

    private fun initOKHttpUtils() {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20000L, TimeUnit.MILLISECONDS)
            .readTimeout(20000L, TimeUnit.MILLISECONDS)
            .build()
        OkHttpUtils.initClient(okHttpClient)
    }

    /**
     * 初始化 APP 版本更新库
     * https://github.com/xuexiangjys/XUpdate
     */
    private fun initUpdate(application: Application) {
        XUpdate.get()
            .debug(false) //默认设置只在wifi下检查版本更新
            .isWifiOnly(false) //默认设置使用get请求检查版本
            .isGet(true) //默认设置非自动模式，可根据具体使用配置
            .isAutoMode(false) //设置默认公共请求参数
            .param("versionCode", UpdateUtils.getVersionCode(application))
            .param("appKey", application.packageName) //设置版本更新出错的监听
            .setOnUpdateFailureListener { error ->
                error.printStackTrace()
                //对不同错误进行处理
                if (error.code != UpdateError.ERROR.CHECK_NO_NEW_VERSION) {
                    ToastUtils.showShort(error.toString())
                }
            } //设置是否支持静默安装，默认是true
            .supportSilentInstall(false) //这个必须设置！实现网络请求功能。
            .setIUpdateHttpService(OKHttpUpdateHttpService()) //这个必须初始化
            .init(application)
    }
}