package com.shmedo.mcloudapp.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseFragment;
import com.shmedo.mcloudapp.views.MyWebView;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.fragment
 * 文件名:   QueryDataFragment
 * 创建者:   dpc
 * 创建时间:  2019/3/12 17:39
 * 描述：    查询数据
 */
public class QueryDeviceDataFragment extends BaseFragment {






    private Runnable dismssDialogRunnable = new Runnable() {
        @Override
        public void run() {
            Timber.e("加载查询设备网页失败");
        }
    };

    @Override
    protected int initContentView() {
        return R.layout.fragment_query_device_data;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, view);


        return view;
    }






    @Override
    public boolean onBackPressed() {
        return false;


    }
}
