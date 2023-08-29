package com.shmedo.mcloudapp

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.blankj.utilcode.util.DeviceUtils
import com.hjq.toast.Toaster
import com.hjq.toast.style.BlackToastStyle
import com.kongzue.dialogx.DialogX
import com.shmedo.lib.core.util.CrashReportingTree
import com.shmedo.mcloudapp.ui.activity.ErrorActivity
import com.shmedo.mcloudapp.ui.activity.SplashActivity
import com.shmedo.mcloudapp.utils.CacheUtil
import com.shmedo.mcloudapp.utils.OKHttpUpdateHttpService
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import com.xuexiang.xupdate.XUpdate
import com.xuexiang.xupdate.entity.UpdateError
import com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION
import com.xuexiang.xupdate.listener.OnUpdateFailureListener
import com.xuexiang.xupdate.utils.UpdateUtils
import com.zhy.http.okhttp.OkHttpUtils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.param.Param
import timber.log.Timber.Forest.plant
import java.util.concurrent.TimeUnit

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp
 * 文件名:   APP
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:16
 */
class MCloudApplication : Application(), ViewModelStoreOwner {
    private lateinit var mAppViewModelStore: ViewModelStore

    override fun onCreate() {
        super.onCreate()
        mAppViewModelStore = ViewModelStore()
        MMKV.initialize(this)
        //异常上报和升级
        initCrashReport()
        //初始化吐司消息组件
        initToastUtil()
        //初始化日志输出
        initTimber()
        //初始化
        DialogX.init(this)
        initRxHttp()
        initOKHttpUtils()
        initUpdate()
    }

    override val viewModelStore: ViewModelStore
        get() = mAppViewModelStore


    /**
     * 初始化异常上报
     */
    private fun initCrashReport() {
        try {
            openErrorActivity()
            //自己处理的异常
//            AppCrashHandler.Companion.getINSTANCE()
            //初始化腾讯Bugly异常上报组件
            CrashReport.initCrashReport(applicationContext)
            CrashReport.setDeviceId(this, DeviceUtils.getUniqueDeviceId())
            // 也可以通过CrashReport类设置，适合无法在初始化sdk时获取到deviceModel的场景，context和deviceModel不能为空（或空字符串）
            CrashReport.setDeviceModel(this, DeviceUtils.getModel())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * 打开错误页面
     */
    private fun openErrorActivity() {
        //防止项目崩溃，崩溃后打开错误界面
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(true)//是否启用CustomActivityOnCrash崩溃拦截机制 必须启用！不然集成这个库干啥？？？
            .showErrorDetails(false) //是否必须显示包含错误详细信息的按钮 default: true
            .showRestartButton(false) //是否必须显示“重新启动应用程序”按钮或“关闭应用程序”按钮default: true
            .logErrorOnRestart(false) //是否必须重新堆栈堆栈跟踪 default: true
            .trackActivities(true) //是否必须跟踪用户访问的活动及其生命周期调用 default: false
            .minTimeBetweenCrashesMs(2000) //应用程序崩溃之间必须经过的时间 default: 3000
            .restartActivity(SplashActivity::class.java) // 重启的activity
            .errorActivity(ErrorActivity::class.java) //发生错误跳转的activity
            .apply()
    }

    /**
     * 设置日志输出
     */
    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            plant(DebugTree())
        } else {
            plant(CrashReportingTree())
        }
    }

    /**
     * 初始化 Toast 工具类
     * https://github.com/getActivity/ToastUtils
     */
    private fun initToastUtil() {
        // 初始化 Toast 框架
        Toaster.init(this)
        Toaster.setStyle(BlackToastStyle())
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
            .setOnParamAssembly { p: Param<*> ->                  //设置公共参数，非必须
                p.addHeader("X-Access-Token", CacheUtil.getToken()) //添加公共请求头
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
    private fun initUpdate() {
        XUpdate.get()
            .debug(false) //默认设置只在wifi下检查版本更新
            .isWifiOnly(false) //默认设置使用get请求检查版本
            .isGet(true) //默认设置非自动模式，可根据具体使用配置
            .isAutoMode(false) //设置默认公共请求参数
            .param("versionCode", UpdateUtils.getVersionCode(this))
            .param("appKey", packageName) //设置版本更新出错的监听
            .setOnUpdateFailureListener { error ->
                error.printStackTrace()
                //对不同错误进行处理
                if (error.code != CHECK_NO_NEW_VERSION) {
                    Toaster.show(error.toString())
                }
            } //设置是否支持静默安装，默认是true
            .supportSilentInstall(false) //这个必须设置！实现网络请求功能。
            .setIUpdateHttpService(OKHttpUpdateHttpService()) //这个必须初始化
            .init(this)
    }
}