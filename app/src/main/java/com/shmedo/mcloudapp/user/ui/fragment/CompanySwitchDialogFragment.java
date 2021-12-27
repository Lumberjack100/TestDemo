package com.shmedo.mcloudapp.user.ui.fragment;

import static autodispose2.AutoDispose.autoDisposable;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ScreenUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.model.PageResult;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrorInfo;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.RequestHeader;
import com.shmedo.mcloudapp.deviceconfig.model.PageInfo;
import com.shmedo.mcloudapp.user.adapter.CompanySimpleInfoAdapter;
import com.shmedo.mcloudapp.user.model.BasicCompanyInfo;
import com.shmedo.mcloudapp.util.ResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 企业切换列表弹框
 */
public class CompanySwitchDialogFragment extends BaseDialogFragment implements TextWatcher {
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.search_placeholder)
    View searchPlaceholder;

    @BindView(R.id.search_container)
    View searchContainer;

    @BindView(R.id.et_keywords)
    ClearEditText mEtKeyWords;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private CompanySimpleInfoAdapter simpleInfoAdapter;

    private List<BasicCompanyInfo> tempList = new ArrayList<>();

    private BasicCompanyInfo basicCompanyInfo = null;

    private static final int PAGE_SIZE = 300;
    private PageInfo pageInfo;
    private String keyWords;// 要输入的搜索关键字

    private DialogFragmentClickListener mListener;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_company_switch_dialog;
    }

    protected void setWindowStyle(int gravity) {
        super.setWindowStyle(gravity);
        Dialog mDialog = getDialog();
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = (int) (ScreenUtils.getScreenHeight() * 0.8f);
        window.setAttributes(wlp);
    }

   @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvTitle.setText("选择企业");
        pageInfo = new PageInfo(1);
        initAdapter();
//        initLoadMore();
        setEditTextListener();
        processQueryUserInCompany();
    }

    private void initAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        simpleInfoAdapter = new CompanySimpleInfoAdapter();
        simpleInfoAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                basicCompanyInfo = simpleInfoAdapter.getItem(position);
                if (basicCompanyInfo.isChecked()) {
                    return;
                }
                for (BasicCompanyInfo info : simpleInfoAdapter.getData()) {
                    info.setChecked(false);
                }
                basicCompanyInfo.setChecked(true);
                simpleInfoAdapter.notifyDataSetChanged();
            }
        });
        mRecyclerView.setAdapter(simpleInfoAdapter);
    }

    /**
     * 初始化加载更多
     */
    private void initLoadMore() {
        simpleInfoAdapter.getLoadMoreModule().setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMore();
            }
        });
        simpleInfoAdapter.getLoadMoreModule().setEnableLoadMore(true);
        // 是否自定加载下一页（默认为true）
        simpleInfoAdapter.getLoadMoreModule().setAutoLoadMore(true);
        // 当数据不满一页时，是否继续自动加载（默认为true）
        simpleInfoAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(false);
    }

    private void setEditTextListener() {
        mEtKeyWords.setHint("搜索公司");
        mEtKeyWords.requestFocus();
        mEtKeyWords.addTextChangedListener(this);
        mEtKeyWords.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (TextUtils.isEmpty(textView.getText())) {
                        ToastUtils.show("请输入搜索内容");
                    } else {
                        com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(mEtKeyWords);
                        searchProcess();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
        if (TextUtils.isEmpty(text)) {
            com.blankj.utilcode.util.KeyboardUtils.showSoftInput(mEtKeyWords);
            simpleInfoAdapter.setList(tempList);
            return;
        }
        keyWords = text.toString().trim();
        searchProcess();
    }

    private void resetCompanyInfo() {
        for (BasicCompanyInfo simpleInfo : tempList) {
            if (simpleInfo.isChecked()) {
                simpleInfo.setChecked(false);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @OnClick({R.id.search_placeholder, R.id.tv_cancel_search, R.id.tv_cancel, R.id.tv_confirm})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.search_placeholder) {
            searchPlaceholder.setVisibility(View.GONE);
            searchContainer.setVisibility(View.VISIBLE);
            mEtKeyWords.setText("");

        } else if (id == R.id.tv_cancel_search) {// 当按了搜索之后关闭软键盘
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(mEtKeyWords);
            searchPlaceholder.setVisibility(View.VISIBLE);
            searchContainer.setVisibility(View.GONE);
            resetCompanyInfo();
            simpleInfoAdapter.setList(tempList);
            keyWords = "";
        } else if (id == R.id.tv_cancel) {
            dismiss();
        } else if (id == R.id.tv_confirm) {
            doPositiveClick(view);
        }
    }

    private void searchProcess() {
        simpleInfoAdapter.setList(new ArrayList<>());
        for (BasicCompanyInfo simpleInfo : tempList) {
            if (!TextUtils.isEmpty(simpleInfo.getCompanyName()) && simpleInfo.getCompanyName().contains(keyWords)) {
                simpleInfoAdapter.addData(simpleInfo);
            }
        }
    }

    private void doPositiveClick(View view) {
        if (basicCompanyInfo == null) {
            ToastUtils.show("您还没选中企业!");
            return;
        }
        if (mListener == null) {
            return;
        }
        if (mListener.onPositiveClick(view, basicCompanyInfo)) {
            dismiss();
        }
    }

    /**
     * 加载更多
     */
    private void loadMore() {
        processQueryUserInCompany();
    }

    /**
     * 查询用户在其中具有权限的公司，包括该公司的子公司(用于设备分配)
     */
    private void processQueryUserInCompany() {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("pageSize",PAGE_SIZE);
            jsonObjectRequest.put("currentPage", pageInfo.getPage());
            jsonObjectRequest.put("companyName", null);
            jsonObjectRequest.put("includeChild", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);

        MDRetrofit.getInstance()
                .createService()
                .queryUserInCompany(MCloudApp.getAccessToken(), body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe(new BaseObserver<PageResult<BasicCompanyInfo>>() {
                    @Override
                    protected void onResponse(PageResult<BasicCompanyInfo> data, ErrorInfo errorInfo) {
//                        adpter.getLoadMoreModule().setEnableLoadMore(true);
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                if (data == null || data.getCurrentPageData() == null) {
                                    return;
                                }
                                if (pageInfo.isFirstPage()) {
                                    //如果是加载的第一页数据，用setNew
                                    tempList.clear();
                                }
                                tempList.addAll(data.getCurrentPageData());
                                simpleInfoAdapter.setList(tempList);

//                                if (data.getCurrentPageData().size() < PAGE_SIZE) {
//                                    //如果不够一页,显示没有更多数据布局
//                                    adpter.getLoadMoreModule().loadMoreEnd();
//                                } else {
//                                    adpter.getLoadMoreModule().loadMoreComplete();
//                                }
//                                // page加一
//                                pageInfo.nextPage();
                            } else {
//                                adpter.getLoadMoreModule().loadMoreFail();
                                if (!TextUtils.isEmpty(errorInfo.getMsg())) {
                                    ToastUtils.show(errorInfo.getMsg());
                                }
                            }
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
//                        adpter.getLoadMoreModule().setEnableLoadMore(true);
//                        adpter.getLoadMoreModule().loadMoreFail();
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }

    public void setDialogFragmentClickListener(DialogFragmentClickListener listener) {
        mListener = listener;
    }
}
