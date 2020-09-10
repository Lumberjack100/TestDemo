package com.shmedo.mcloudapp.common.ui.fragment;

import android.app.Activity;
import android.content.Context;
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
import com.shmedo.mcloudapp.common.callback.HandleBackInterface;
import com.shmedo.mcloudapp.util.common.HandleBackUtil;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;

import butterknife.ButterKnife;
import butterknife.Unbinder;
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
    private Unbinder unbinder;

    protected Activity mActivity;

    protected View mRootView;

    protected MaterialDialog loadingDialog = null;

    protected boolean isActive = false;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(getLayoutId(), container, false);
        } else {
            ViewGroup viewGroup = (ViewGroup) mRootView.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(mRootView);
            }
        }
        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        initView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }


    /**
     * Gets layout id.
     *
     * @return the layout id
     */
    protected abstract int getLayoutId();

    /**
     * view与数据绑定
     */
    protected void initView() {

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
        isActive = true;
        String name = getClass().getName();
        Timber.i("startPage,Fragment=%s", name);
    }


    @Override
    public void onPause() {
        super.onPause();
        isActive = false;
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
            //Sets whether this dialog is cancelable with the BACK key.
//            loadingDialog.setCancelable(true);
            //Sets whether this dialog is canceled when touched outside the window's bounds.
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
