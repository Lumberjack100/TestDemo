package com.shmedo.mcloudapp;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.widget.Toast;

import com.hjq.toast.ToastInterceptor;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.ToastBlackStyle;
import com.pgyersdk.crash.PgyCrashManager;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.log.AppCrashHandler;
import com.shmedo.mcloudapp.log.CrashReportingTree;
import com.shmedo.mcloudapp.log.log4a.LogInit;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.Objects;

import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp
 * 文件名:   APP
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:16
 *
 */
public class MCloudApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MCloudApp.initialize(this);

        //初始化facebook.stetho
        //AppInit.init(this);

        //初始化日志输出
        initTimber();
        //初始化基于 mmap, 高性能、高可用的 Android 日志收集框架
        LogInit.init(this);

        //初始化蒲公英
        //启动 Pgyer 检测 Crash 功能
        PgyCrashManager.register();

        //异常上报和升级
        initCrashReport();

        //初始化吐司消息组件
        initToastUtil();

        //初始化蓝牙管理类
        initMdBluetoothManager();
    }


    /**
     * 初始化异常上报
     */
    private void initCrashReport() {
        //自己处理的异常
        AppCrashHandler.getInstance(this);// crash handler

        //初始化腾讯Bugly异常上报组件
        CrashReport.initCrashReport(getApplicationContext(),  BuildConfig.BUGLY_APPKEY, false);
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
        // 设置 Toast 拦截器
        ToastUtils.setToastInterceptor(new ToastInterceptor() {
            @Override
            public boolean intercept(Toast toast, CharSequence text) {
                boolean intercept = super.intercept(toast, text);
                if (intercept) {
                    Timber.e("空 Toast");
                } else {
                    Timber.i(text.toString());
                }
                return intercept;
            }
        });
        // 初始化吐司工具类
        ToastUtils.init(this, new ToastBlackStyle(this));
    }

    private void initMdBluetoothManager() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = Objects.requireNonNull(bluetoothManager).getAdapter();
        MdBluetoothManager.init(mBluetoothAdapter, bluetoothManager);
    }

}

