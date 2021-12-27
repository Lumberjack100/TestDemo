package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shmedo.mcloudapp.deviceconfig.ui.fragment.tcpcommon.BaseTcpIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.VmsViewModel;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/11 <br/>
 * 描述：     TODO #gh#
 */
public abstract class BaseVmsTcpCommunicateFragment extends BaseTcpIotCommunicateFragment {
    protected VmsViewModel vmsViewModel;


   @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vmsViewModel = getApplicationScopeViewModel(VmsViewModel.class);
    }
}
