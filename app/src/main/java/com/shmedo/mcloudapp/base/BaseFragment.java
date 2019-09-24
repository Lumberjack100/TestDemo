package com.shmedo.mcloudapp.base;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.shmedo.mcloudapp.util.XPermissionUtils;
import com.shmedo.mcloudapp.util.common.HandleBackInterface;
import com.shmedo.mcloudapp.util.common.HandleBackUtil;

import butterknife.ButterKnife;

/**
 * 项目名：  eMeasApp
 * 包名：    com.shmedo.emeas.base
 * 文件名:   BaseFragment
 * 创建者:   dpc
 * 创建时间:  2017/8/28 17:33
 * 描述：    TODO
 */

public abstract class BaseFragment extends Fragment implements HandleBackInterface {

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(initContentView(), container, false);
        ButterKnife.bind(this, view);
        //initState();
        return view;
    }


    protected abstract int initContentView();

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull
        int[] grantResults) {
        XPermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 沉浸式状态栏
     */
    private void initState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            //getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }


    @Override public void onResume() {
        super.onResume();

    }


    @Override public void onPause() {
        super.onPause();

    }


    @Override public boolean onBackPressed() {
        return HandleBackUtil.handleBackPress(this);
    }
}
