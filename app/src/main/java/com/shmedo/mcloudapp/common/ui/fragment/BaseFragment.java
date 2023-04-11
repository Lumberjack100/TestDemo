package com.shmedo.mcloudapp.common.ui.fragment;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.BaseDialog;
import com.kongzue.dialogx.interfaces.OnBackPressedListener;
import com.shmedo.mcloudapp.MCloudApplication;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.callback.HandleBackInterface;
import com.shmedo.mcloudapp.util.HandleBackUtil;
import com.umeng.analytics.MobclickAgent;

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

    protected AppCompatActivity mActivity;
    private ViewModelProvider mFragmentProvider;
    private ViewModelProvider mActivityProvider;
    private ViewModelProvider mApplicationProvider;

    private View rootView;
    private Unbinder unbinder;

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

    /**
     * 判断当前Fragment是否处于已恢复状态。
     */
    protected boolean isActive = false;
    protected String name;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (AppCompatActivity) context;
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
        name = getClass().getSimpleName();
        initView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null)
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
        Timber.i("onResume,Fragment=%s", name);
        if (!isExcludedFragment())
            MobclickAgent.onPageStart(getClass().getSimpleName().intern()); //统计页面

    }

    @Override
    public void onPause() {
        super.onPause();
        isActive = false;
        Timber.i("onPause,Fragment=%s", name);
        if (!isExcludedFragment())
            MobclickAgent.onPageEnd(getClass().getSimpleName().intern());
    }

    @Override
    public void onStop() {
        super.onStop();
        Timber.i("onStop,Fragment=%s", name);
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

    protected void showProgressDialog(String message) {
        WaitDialog.show(message)
                .setOnBackPressedListener(new OnBackPressedListener() {//返回按键监听
                    @Override
                    public boolean onBackPressed(BaseDialog dialog) {
                        WaitDialog.dismiss();
                        return false;
                    }
                });
    }

    protected void dismissProgressDialog() {
        WaitDialog.dismiss();
    }

    @Override
    public boolean onBackPressed() {
        return HandleBackUtil.handleBackPress(this);
    }

    protected <T extends ViewModel> T getFragmentScopeViewModel(@NonNull Class<T> modelClass) {
        if (mFragmentProvider == null) {
            mFragmentProvider = new ViewModelProvider(this);
        }
        return mFragmentProvider.get(modelClass);
    }

    protected <T extends ViewModel> T getActivityScopeViewModel(@NonNull Class<T> modelClass) {
        if (mActivityProvider == null) {
            mActivityProvider = new ViewModelProvider(mActivity);
        }
        return mActivityProvider.get(modelClass);
    }

//    protected <T extends ViewModel> T getApplicationScopeViewModel(@NonNull Class<T> modelClass) {
//        if (mApplicationProvider == null) {
//            mApplicationProvider = new ViewModelProvider((MCloudApplication) mActivity.getApplicationContext());
//        }
//        return mApplicationProvider.get(modelClass);
//    }

    protected <T extends ViewModel> T getApplicationScopeViewModel(@NonNull Class<T> modelClass) {
        if (mApplicationProvider == null) {
            mApplicationProvider = new ViewModelProvider(
                    (MCloudApplication) mActivity.getApplicationContext(), getApplicationFactory(mActivity));
        }
        return mApplicationProvider.get(modelClass);
    }

    private ViewModelProvider.Factory getApplicationFactory(Activity activity) {
        checkActivity(this);
        Application application = checkApplication(activity);
        return (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(application);
    }

    private void checkActivity(Fragment fragment) {
        Activity activity = fragment.getActivity();
        if (activity == null) {
            throw new IllegalStateException("Can't create ViewModelProvider for detached fragment");
        }
    }

    private Application checkApplication(Activity activity) {
        Application application = activity.getApplication();
        if (application == null) {
            throw new IllegalStateException("Your activity/fragment is not yet attached to "
                    + "Application. You can't request ViewModel before onCreate call.");
        }
        return application;
    }

    private boolean isExcludedFragment() {
        if (name.contains("DeviceModuleMainFragment")
                || name.contains("NetVmsHomeFragment")
                || name.contains("TcpVmsHomeFragment")) {
            return true;
        }
        return false;
    }
}
