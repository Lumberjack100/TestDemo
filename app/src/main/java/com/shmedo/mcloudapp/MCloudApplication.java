package com.shmedo.mcloudapp;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import com.hjq.toast.ToastUtils;
import com.hjq.toast.config.IToastInterceptor;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.style.MaterialStyle;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.log.AppCrashHandler;
import com.shmedo.core.log.CrashReportingTree;
import com.shmedo.core.log.log4a.LogInit;
import com.shmedo.mcloudapp.util.MyToastBlackStyle;
import com.tencent.bugly.crashreport.CrashReport;

import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp
 * 文件名:   APP
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:16
 */
public class MCloudApplication extends Application implements ViewModelStoreOwner {
    private ViewModelStore mAppViewModelStore;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAppViewModelStore = new ViewModelStore();
        MCloudApp.initialize(this);

        //异常上报和升级
        initCrashReport();

        //初始化吐司消息组件
        initToastUtil();

        //初始化kongzue/DialogX组件
        initDialogX();

        //初始化日志输出
        initTimber();

        //初始化基于 mmap, 高性能、高可用的 Android 日志收集框架
        LogInit.init(this);
    }

    @NonNull
    @Override
    public ViewModelStore getViewModelStore() {
        return mAppViewModelStore;
    }

    /**
     * 初始化异常上报
     */
    private void initCrashReport() {
        //自己处理的异常
        AppCrashHandler.getInstance(this);// crash handler
        //初始化腾讯Bugly异常上报组件
        CrashReport.initCrashReport(getApplicationContext());
    }

    /**
     * 设置日志输出
     */
    private void initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
    }

    /**
     * 初始化 Toast 工具类
     * https://github.com/getActivity/ToastUtils
     */
    private void initToastUtil() {
        // 自定义 Toast 拦截器（用于追踪 Toast 调用的位置）
        ToastUtils.setInterceptor(new IToastInterceptor() {
            @Override
            public boolean intercept(CharSequence text) {
                if (BuildConfig.DEBUG) {
                    // 获取调用的堆栈信息
                    StackTraceElement[] stackTrace = new Throwable().getStackTrace();
                    // 跳过最前面两个堆栈
                    for (int i = 2; stackTrace.length > 2 && i < stackTrace.length; i++) {
                        // 获取代码行数
                        int lineNumber = stackTrace[i].getLineNumber();
                        // 获取类的全路径
                        String className = stackTrace[i].getClassName();
                        if (lineNumber <= 0 || className.startsWith(ToastUtils.class.getName())) {
                            continue;
                        }
                        Timber.d("(" + stackTrace[i].getFileName() + ":" + lineNumber + ") " + text.toString());
                        break;
                    }
                }
                return false;
            }
        });
        // 初始化吐司工具类
        ToastUtils.init(this);
        ToastUtils.setStyle(new MyToastBlackStyle());
    }

    private void initDialogX() {
        DialogX.init(this);
        DialogX.implIMPLMode = DialogX.IMPL_MODE.VIEW;
        DialogX.useHaptic = true;
        DialogX.globalStyle = new MaterialStyle();
        DialogX.globalTheme = DialogX.THEME.AUTO;
        DialogX.onlyOnePopTip = false;
    }
}

