package com.shmedo.mcloudapp

import cat.ereza.customactivityoncrash.config.CaocConfig
import com.drake.brv.PageRefreshLayout
import com.drake.brv.utils.BRV
import com.drake.statelayout.StateConfig
import com.hjq.toast.Toaster
import com.hjq.toast.style.BlackToastStyle
import com.kongzue.dialogx.DialogX
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.shmedo.lib.network.RxHttpManager
import com.shmedo.mcloudapp.koin.appKoinModule
import com.shmedo.mcloudapp.ui.page.base.activity.ErrorActivity
import com.shmedo.mcloudapp.ui.page.welcome.SplashActivity
import com.shmedo.mcloudapp.utils.CrashReportingTree
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import timber.log.Timber
import timber.log.Timber.Forest.plant

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp
 * 文件名:   APP
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:16
 */
class MCloudApplication : BaseApp() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            // Log Koin into Android logger
            androidLogger()
            // Reference Android context
            androidContext(this@MCloudApplication)
            // Load modules
            modules(appKoinModule)
        }

        //初始化MMKV
//        MMKV.initialize(this)

        //初始化日志输出
        initTimber()

        //异常上报和升级
        initCrashReport()

        //初始化吐司消息组件
        initToastUtil()

        DialogX.init(this)

        RxHttpManager.initial(this)

        //Android 快速构建 RecyclerView
        initBrv()
    }

    /**
     * 设置日志输出
     */
    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            plant(Timber.DebugTree())
        } else {
            plant(CrashReportingTree())
        }
    }

    /**
     * 初始化异常上报
     */
    private fun initCrashReport() {
        try {
            openErrorActivity()
            //自己处理的异常
//            AppCrashHandler.Companion.getINSTANCE()

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
     * 初始化 Toast 工具类
     * https://github.com/getActivity/ToastUtils
     */
    private fun initToastUtil() {
        // 初始化 Toast 框架
        Toaster.init(this)
        Toaster.setStyle(BlackToastStyle())
    }

    private fun initBrv() {
        BRV.modelId = BR.m
        // 禁止错误缺省页启用下拉刷新
        PageRefreshLayout.refreshEnableWhenError = false
        /**
         *  推荐在Application中进行全局配置缺省页, 当然同样每个页面可以单独指定缺省页.
         *  具体查看 https://github.com/liangjingkanji/StateLayout
         */
        StateConfig.apply {
            emptyLayout = R.layout.layout_empty
            errorLayout = R.layout.layout_error
            loadingLayout = R.layout.layout_loading
            setRetryIds(R.id.msg, R.id.iv)
            onLoading {
                // 此生命周期可以拿到LoadingLayout创建的视图对象, 可以进行动画设置或点击事件.
            }
        }
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
            MaterialHeader(context).setColorSchemeResources(R.color.colorPrimary)
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
            ClassicsFooter(context)
        }
    }
}