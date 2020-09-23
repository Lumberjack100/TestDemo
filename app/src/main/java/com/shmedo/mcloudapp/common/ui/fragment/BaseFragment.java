package com.shmedo.mcloudapp.common.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.callback.HandleBackInterface;
import com.shmedo.mcloudapp.util.common.HandleBackUtil;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;

import java.util.Calendar;

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
    //防止按钮重复点击设置的时间间隔
    private static final int DOUBLE_CLICK_TIME_INTERVAL = 1500;

    private Unbinder unbinder;

    protected Activity mActivity;

    private View rootView;

    /**
     * Fragment中显示加载等待的控件。
     */
    protected ProgressBar loading = null;

    /**
     * Fragment中由于服务器异常导致加载失败显示的布局。
     */
    private View loadErrorView = null;

    /**
     * Fragment中由于网络异常导致加载失败显示的布局。
     */
    private View badNetworkView = null;

    /**
     * Fragment中当界面上没有任何内容时展示的布局。
     */
    private View noContentView = null;

    protected MaterialDialog loadingDialog = null;

    /**
     * 判断当前Fragment是否处于已恢复状态。
     */
    protected boolean isActive = false;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(getLayoutId(), container, false);
        } else {
            ViewGroup viewGroup = (ViewGroup) rootView.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(rootView);
            }
        }
        return rootView;
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
     * @return the layout id
     */
    protected abstract int getLayoutId();

    /**
     * view与数据绑定
     */
    protected void initView() {
    }

    /**
     * 当Fragment中的加载内容服务器返回失败，通过此方法显示提示界面给用户。
     *
     * @param tip 界面中的提示信息
     */
    protected void showLoadErrorView(String tip) {
        if (loadErrorView != null) {
            loadErrorView.setVisibility(View.VISIBLE);
            return;
        }
        if (rootView != null) {
            ViewStub viewStub = rootView.findViewById(R.id.loadErrorView);
            if (viewStub != null) {
                loadErrorView = viewStub.inflate();
                TextView loadErrorText = loadErrorView.findViewById(R.id.loadErrorText);
                loadErrorText.setText(tip);
            }
        }
    }

    /**
     * 当Fragment中的内容因为网络原因无法显示的时候，通过此方法显示提示界面给用户。
     *
     * @param listener 重新加载点击事件回调
     */
    protected void showBadNetworkView(View.OnClickListener listener) {
        if (badNetworkView != null) {
            badNetworkView.setVisibility(View.VISIBLE);
            return;
        }
        if (rootView != null) {
            ViewStub viewStub = rootView.findViewById(R.id.badNetworkView);
            if (viewStub != null) {
                badNetworkView = viewStub.inflate();
                View badNetworkRootView = badNetworkView.findViewById(R.id.badNetworkRootView);
                badNetworkRootView.setOnClickListener(listener);
            }
        }
    }

    /**
     * 当Fragment中没有任何内容的时候，通过此方法显示提示界面给用户。
     *
     * @param tip 界面中的提示信息
     */
    protected void showNoContentView(String tip) {
        if (noContentView != null) {
            noContentView.setVisibility(View.VISIBLE);
            return;
        }
        if (rootView != null) {
            ViewStub viewStub = rootView.findViewById(R.id.noContentView);
            if (viewStub != null) {
                noContentView = viewStub.inflate();
                TextView noContentText = noContentView.findViewById(R.id.noContentText);
                noContentText.setText(tip);
            }
        }
    }

    /**
     * 将load error view进行隐藏。
     */
    protected void hideLoadErrorView() {
        if (loadErrorView != null) {
            loadErrorView.setVisibility(View.GONE);
        }
    }

    /**
     * 将no content view进行隐藏。
     */
    protected void hideNoContentView() {
        if (noContentView != null) {
            noContentView.setVisibility(View.GONE);
        }
    }

    /**
     * 将bad network view进行隐藏。
     */
    protected void hideBadNetworkView() {
        if (badNetworkView != null) {
            badNetworkView.setVisibility(View.GONE);
        }
    }

    /**
     * 开始加载，将加载等待控件显示。
     */
    protected void startLoading() {
        if (loading != null) {
            loading.setVisibility(View.VISIBLE);
        }
        hideBadNetworkView();
        hideNoContentView();
        hideLoadErrorView();
    }

    /**
     * 加载完成，将加载等待控件隐藏。
     */
    protected void loadFinished() {
        if (loading != null) {
            loading.setVisibility(View.GONE);
        }
    }

    /**
     * 加载失败，将加载等待控件隐藏。
     */
    protected void loadFailed(String msg) {
        if (loading != null) {
            loading.setVisibility(View.GONE);
        }
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
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .positiveColorRes(R.color.blue_52B4F8);
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


    protected boolean isDoubleClick(View v) {
        Object tag = v.getTag(v.getId());
        long beforeTimeMillis = tag != null ? (long) tag : 0;
        long timeInMillis = System.currentTimeMillis();
        v.setTag(v.getId(), timeInMillis);

        long interval = timeInMillis - beforeTimeMillis;
        Timber.d("isDoubleClick点击了=%s", interval);
        return interval < DOUBLE_CLICK_TIME_INTERVAL;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull
            int[] grantResults) {
        XPermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public boolean onBackPressed() {
        return HandleBackUtil.handleBackPress(this);
    }
}
