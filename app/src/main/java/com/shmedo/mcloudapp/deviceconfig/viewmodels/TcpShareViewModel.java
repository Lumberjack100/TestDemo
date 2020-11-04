package com.shmedo.mcloudapp.deviceconfig.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.shmedo.mcloudapp.deviceconfig.TcpManager;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/4 <br/>
 * 描述：     TODO
 */
public class TcpShareViewModel extends AndroidViewModel {

    public final TcpManager tcpManager = new TcpManager();

    public TcpShareViewModel(@NonNull Application application) {
        super(application);
    }


}
