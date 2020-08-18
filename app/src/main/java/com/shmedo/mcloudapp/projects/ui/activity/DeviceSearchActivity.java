package com.shmedo.mcloudapp.projects.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.adapter.DeviceInfoAdapter;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfoWrapper;
import com.shmedo.mcloudapp.projects.model.param.QueryProjectDevice;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

public class DeviceSearchActivity extends BaseActivity {
    @BindView(R.id.et_keywords)
    ClearEditText mEtKeyWords;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private DeviceInfoAdapter deviceInfoAdapter;
    private List<ProjectDeviceInfo> deviceInfoList = new ArrayList<>();

    private int userId;
    private int companyID = 1;
    private String keyWords;// 要输入的poi搜索关键字

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, DeviceSearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_device_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUserData();
        initView();
        initDeviceInfoAdapter();
    }

    private void initUserData() {
        UserInfo userInfo = MCloudApp.getCurrentUserInfo();
        if (userInfo != null && userInfo.getUser() != null) {
            UserInfo.UserBean user = userInfo.getUser();
            userId = user.getId();
        }
        if (userInfo != null && userInfo.getDepartments() != null && userInfo.getDepartments().size() > 0) {
            companyID = userInfo.getDepartments().get(0).getCompanyID();
        }
    }

    private void initView() {
        mEtKeyWords.setHint("搜索设备");
        mEtKeyWords.requestFocus();
        mEtKeyWords.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (TextUtils.isEmpty(textView.getText())) {
                        ToastUtils.show("请输入搜索内容");
                    } else {
                        keyWords = textView.getText().toString();
                        // 当按了搜索之后关闭软键盘
                        KeyBordUtils.hideSoftKeyboard(mEtKeyWords);
                        doSearchQuery();
                    }
                    return true;
                }
                return false;
            }
        });
    }


    private void initDeviceInfoAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(this, 15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        deviceInfoAdapter = new DeviceInfoAdapter(deviceInfoList);
        deviceInfoAdapter.setAnimationEnable(true);
        deviceInfoAdapter.setAnimationFirstOnly(false);
        deviceInfoAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                ProjectDeviceInfo deviceInfo = (ProjectDeviceInfo) deviceInfoList.get(position);
            }
        });
        mRecyclerView.setAdapter(deviceInfoAdapter);
    }

    @OnClick({R.id.iv_back, R.id.tv_search})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;

            case R.id.tv_search:
                if (TextUtils.isEmpty(mEtKeyWords.getText().toString().trim())) {
                    ToastUtils.show("请输入搜索内容");
                    return;
                }
                keyWords = mEtKeyWords.getText().toString();
                // 当按了搜索之后关闭软键盘
                KeyBordUtils.hideSoftKeyboard(mEtKeyWords);
                doSearchQuery();
                break;
        }
    }


    /**
     * 开始进行poi搜索
     */
    private void doSearchQuery() {
        // 方式一：直接传入 layout id
        deviceInfoAdapter.setEmptyView(R.layout.loading_view);

        QueryProjectDevice parameter = new QueryProjectDevice();
        parameter.setCompanyID(companyID);
        parameter.setDeviceType(-1);
        parameter.setSn(keyWords);
        parameter.setPageSize(20);
        parameter.setCurrentPage(1);

        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .QueryCompanyDevice(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ProjectDeviceInfoWrapper>() {
                    @Override
                    public void Success(ProjectDeviceInfoWrapper data, String message) {
                        if (data == null || data.getCurrentPageData() == null || data.getCurrentPageData().size() == 0) {
                            deviceInfoAdapter.setEmptyView(R.layout.empty_view);
                            return;
                        }

                        deviceInfoList.clear();
                        deviceInfoList.addAll(data.getCurrentPageData());
                        deviceInfoAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(String message) {
                        Timber.w("请求失败--%s", message);
                        // 方式二：传入View
                        deviceInfoAdapter.setEmptyView(getErrorView());
                    }
                });
    }

    private View getErrorView() {
        View errorView = getLayoutInflater().inflate(R.layout.error_view, mRecyclerView, false);
        errorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearchQuery();
            }
        });
        return errorView;
    }
}
