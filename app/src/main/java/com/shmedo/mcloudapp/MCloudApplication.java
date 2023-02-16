package com.shmedo.mcloudapp;

import static com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.MetaDataUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.Utils;
import com.hjq.toast.ToastUtils;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.style.MaterialStyle;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.log.AppCrashHandler;
import com.shmedo.core.log.CrashReportingTree;
import com.shmedo.core.log.log4a.LogInit;
import com.shmedo.mcloudapp.network.OKHttpUpdateHttpService;
import com.shmedo.mcloudapp.util.MyToastBlackStyle;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mmkv.MMKV;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.entity.UpdateError;
import com.xuexiang.xupdate.listener.OnUpdateFailureListener;
import com.xuexiang.xupdate.utils.UpdateUtils;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
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
    public void onCreate() {
        super.onCreate();
        mAppViewModelStore = new ViewModelStore();
        MCloudApp.initialize(this);

        //友盟预初始化
        setUmeng();

        //异常上报和升级
        initCrashReport();

        //初始化吐司消息组件
        initToastUtil();

        //初始化日志输出
        initTimber();

        initMMKV();

        //初始化基于 mmap, 高性能、高可用的 Android 日志收集框架
        LogInit.init(this);

        //初始化kongzue/DialogX组件
        initDialogX();

        initOKHttpUtils();

        initUpdate();
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
        try {
            //自己处理的异常
            AppCrashHandler.getInstance(this);// crash handler
            //初始化腾讯Bugly异常上报组件
            CrashReport.initCrashReport(getApplicationContext());
            CrashReport.setDeviceId(this, DeviceUtils.getUniqueDeviceId());
            // 也可以通过CrashReport类设置，适合无法在初始化sdk时获取到deviceModel的场景，context和deviceModel不能为空（或空字符串）
            CrashReport.setDeviceModel(this, DeviceUtils.getModel());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    private void initMMKV() {
        String rootDir = MMKV.initialize(this);
        Timber.d("mmkv root: %s", rootDir);
    }

    /**
     * 初始化 Toast 工具类
     * https://github.com/getActivity/ToastUtils
     */
    private void initToastUtil() {
        // 初始化吐司工具类
        ToastUtils.init(this);
        ToastUtils.setStyle(new MyToastBlackStyle());
    }

    /**
     * 初始化kongzue/DialogX组件
     * https://github.com/kongzue/DialogX
     */
    private void initDialogX() {
        DialogX.init(this);
        DialogX.implIMPLMode = DialogX.IMPL_MODE.VIEW;
        DialogX.useHaptic = true;
        DialogX.globalStyle = new MaterialStyle();
        DialogX.globalTheme = DialogX.THEME.AUTO;
        DialogX.onlyOnePopTip = false;
    }

    private void initOKHttpUtils() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20000L, TimeUnit.MILLISECONDS)
                .readTimeout(20000L, TimeUnit.MILLISECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }

    /**
     * 初始化 APP 版本更新库
     * https://github.com/xuexiangjys/XUpdate
     */
    private void initUpdate() {
        XUpdate.get()
                .debug(false)
                //默认设置只在wifi下检查版本更新
                .isWifiOnly(false)
                //默认设置使用get请求检查版本
                .isGet(true)
                //默认设置非自动模式，可根据具体使用配置
                .isAutoMode(false)
                //设置默认公共请求参数
                .param("versionCode", UpdateUtils.getVersionCode(this))
                .param("appKey", getPackageName())
                //设置版本更新出错的监听
                .setOnUpdateFailureListener(new OnUpdateFailureListener() {
                    @Override
                    public void onFailure(UpdateError error) {
                        error.printStackTrace();
                        //对不同错误进行处理
                        if (error.getCode() != CHECK_NO_NEW_VERSION) {
                            ToastUtils.show(error.toString());
                        }
                    }
                })
                //设置是否支持静默安装，默认是true
                .supportSilentInstall(false)
                //这个必须设置！实现网络请求功能。
                .setIUpdateHttpService(new OKHttpUpdateHttpService())
                //这个必须初始化
                .init(this);

    }

    /**
     * 初始化友盟统计 SDK
     * <p>
     * 确保App首次冷启动时，在用户阅读您的《隐私政策》并取得用户授权之后，才调用正式初始化函数UMConfigure.init()初始化统计SDK，此时SDK才会真正采集设备信息并上报数据。
     * 反之，如果用户不同意《隐私政策》授权，则不能调用UMConfigure.init()初始化函数。
     * 一旦App获取到《隐私政策》的用户授权，后续的App冷启动，开发者应该保证在Applicaiton.onCreate函数中调用预初始化函数UMConfigure.preInit()。
     * 正式初始化函数UMConfigure.init可以按需调用(可以在预初始化函数之后紧接着调用，也可以放到后台线程中延迟调用，但还是必须调用，不能遗漏)。
     */
    private void setUmeng() {
        String appKey = MetaDataUtils.getMetaDataInApp("UMENG_APP_KEY");

        //设置LOG开关，默认为false
        UMConfigure.setLogEnabled(false);

        // SDK预初始化函数不会采集设备信息，也不会向友盟后台上报数据。
        // preInit预初始化函数耗时极少，不会影响App首次冷启动用户体验
        UMConfigure.preInit(this, appKey, "production");

        // 页面自动采集选择
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL);

        SPStaticUtils.setDefaultSPUtils(SPUtils.getInstance(getPackageName() + "_preferences"));
        String mPrivacy = SPStaticUtils.getString(AppContants.PRIVACY_AGREEMENT);
        //用户同意隐私政策授权
        if (!TextUtils.isEmpty(mPrivacy) && mPrivacy.equalsIgnoreCase("agree")) {
            //初始化组件化基础库, 所有友盟业务SDK都必须调用此初始化接口
            //注意: 即使您已经在AndroidManifest.xml中配置过appkey和channel值，
            //也需要在App代码中调用初始化接口（如需要使用AndroidManifest.xml中配置好的appkey和channel值，
            //UMConfigure.init调用中appkey和channel参数请置为null）
            UMConfigure.init(this, appKey, "production", UMConfigure.DEVICE_TYPE_PHONE, "");
        }
    }

}

