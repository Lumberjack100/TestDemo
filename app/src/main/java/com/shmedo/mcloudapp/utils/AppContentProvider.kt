package com.shmedo.mcloudapp.utils

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.IntentFilter
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.shmedo.core.commonlib.utils.AppLifeObserver
import com.shmedo.mcloudapp.utils.network.NetworkStateReceive
import com.tencent.mmkv.MMKV

/**
 * 作者　: hegaojian
 * 时间　: 2019/12/14
 * 描述　:
 */


class AppContentProvider : ContentProvider() {
    companion object {
        private var mNetworkStateReceive: NetworkStateReceive? = null
        var watchAppLife = true
    }

    override fun onCreate(): Boolean {
        val application = context!!.applicationContext as Application
        install(application)
        return true
    }

    private fun install(application: Application) {
        mNetworkStateReceive = NetworkStateReceive()
        ContextCompat.registerReceiver(application, mNetworkStateReceive, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION), ContextCompat.RECEIVER_EXPORTED)
        if (watchAppLife)
            ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifeObserver)

        //初始化MMKV
        MMKV.initialize(application)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun getType(uri: Uri): String? = null
}