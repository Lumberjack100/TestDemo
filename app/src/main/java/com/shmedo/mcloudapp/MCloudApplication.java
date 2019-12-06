package com.shmedo.mcloudapp;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.widget.Toast;

import com.hjq.toast.ToastInterceptor;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.ToastBlackStyle;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.logging.AppCrashHandler;
import com.shmedo.mcloudapp.logging.CrashReportingTree;
import com.tencent.mmkv.MMKV;

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
        //初始化蒲公英
        //PgyCrashManager.register(this);

        AppCrashHandler.getInstance(this);// crash handler

        //基于 mmap 的高性能通用 key-value 组件
        MMKV.initialize(this);

        //日志输出
        initTimber();

        initToastUtil();

        initMdBluetoothManager();
    }


    /**
     * 设置日志输出
     */
    private void initTimber() {
        Timber.d("initTimber()------in");
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

