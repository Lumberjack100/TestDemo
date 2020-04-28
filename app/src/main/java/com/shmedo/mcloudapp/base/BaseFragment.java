package com.shmedo.mcloudapp.base;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.mcloudapp.util.XPermissionUtils;
import com.shmedo.mcloudapp.interfaces.HandleBackInterface;
import com.shmedo.mcloudapp.util.common.HandleBackUtil;

import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * 项目名：  eMeasApp
 * 包名：    com.shmedo.emeas.base
 * 文件名:   BaseFragment
 * 创建者:   dpc
 * 创建时间:  2017/8/28 17:33
 * 描述：
 */

public abstract class BaseFragment extends Fragment implements HandleBackInterface {

    protected MaterialDialog loadingDialog = null;

    protected abstract int initContentView();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(initContentView(), container, false);
        ButterKnife.bind(this, view);

        return view;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull
            int[] grantResults) {
        XPermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onResume() {
        super.onResume();
        String name = getClass().getName();
        Timber.i("startPage,Fragment=%s", name);
    }


    @Override
    public void onPause() {
        super.onPause();
        String name = getClass().getName();
        Timber.i("endPage,Fragment=%s", name);
    }


    protected void showTipDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getActivity());
        mBuilder.title("温馨提示：")
                .content(content)
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定");
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    protected void showLoadingDialog(String tip) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            return;
        }

        if (loadingDialog == null) {
            loadingDialog = new MaterialDialog.Builder(getActivity())
                    .content(TextUtils.isEmpty(tip) ? "正在加载..." : tip)
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .build();
            loadingDialog.setCancelable(false);
            loadingDialog.setCanceledOnTouchOutside(false);
        }

        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    protected void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }


    @Override
    public boolean onBackPressed() {
        return HandleBackUtil.handleBackPress(this);
    }
}
