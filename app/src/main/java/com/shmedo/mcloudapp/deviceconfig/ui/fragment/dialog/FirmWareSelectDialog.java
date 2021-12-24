package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog;

import static autodispose2.AutoDispose.autoDisposable;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.ScreenUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.model.PageResult;
import com.shmedo.mcloudapp.deviceconfig.adapter.DeviceFirmWareAdpter;
import com.shmedo.mcloudapp.deviceconfig.model.FirmWareInfo;
import com.shmedo.mcloudapp.deviceconfig.model.PageInfo;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrorInfo;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.RequestHeader;
import com.shmedo.mcloudapp.network.ServiceAddressType;
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
 * 固件选择列表弹框
 */
public class FirmWareSelectDialog extends BaseDialogFragment {
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private DeviceFirmWareAdpter adpter;

    private List<FirmWareInfo> firmWareInfoList = new ArrayList<>();
    private FirmWareInfo firmWareInfo = null;

    private static final int PAGE_SIZE = 10;
    private PageInfo pageInfo;
    private int productID = -1;

    private DialogFragmentClickListener mListener;

    public FirmWareSelectDialog() {
    }

    public FirmWareSelectDialog(int productID) {
        this.productID = productID;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_firm_ware_select_dialog;
    }

    @Override
    protected void setWindowStyle(int gravity) {
        super.setWindowStyle(gravity);
        Dialog mDialog = getDialog();
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = (int) (ScreenUtils.getScreenHeight() * 0.6f);
        window.setAttributes(wlp);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvTitle.setText("固件升级");
        pageInfo = new PageInfo(1);
        initAdapter();
        initLoadMore();
        processQueryFirmwareList();
    }

    private void initAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        adpter = new DeviceFirmWareAdpter(firmWareInfoList);
        adpter.setAnimationEnable(true);
        adpter.setAnimationFirstOnly(false);
        adpter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                firmWareInfo = firmWareInfoList.get(position);
                if (firmWareInfo.isChecked()) {
                    return;
                }
                for (FirmWareInfo info : firmWareInfoList) {
                    info.setChecked(false);
                }
                firmWareInfo.setChecked(true);
                adpter.notifyDataSetChanged();
            }
        });
        adpter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                firmWareInfo = firmWareInfoList.get(position);
                showTip();
            }
        });
        mRecyclerView.setAdapter(adpter);
    }

    /**
     * 初始化加载更多
     */
    private void initLoadMore() {
        adpter.getLoadMoreModule().setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMore();
            }
        });
        adpter.getLoadMoreModule().setEnableLoadMore(true);
        // 是否自定加载下一页（默认为true）
        adpter.getLoadMoreModule().setAutoLoadMore(true);
        // 当数据不满一页时，是否继续自动加载（默认为true）
        adpter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(false);
    }

    private void showTip() {
        if (firmWareInfo == null || TextUtils.isEmpty(firmWareInfo.getFwNote()))
            return;

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("固件说明")
                .content(firmWareInfo.getFwNote())
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(true)
                .positiveText("确定")
                .positiveColorRes(R.color.blue_52B4F8);
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @OnClick({R.id.tv_cancel, R.id.tv_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                dismiss();
                break;

            case R.id.tv_confirm:
                doPositiveClick(view);
                break;
        }
    }

    private void doPositiveClick(View view) {
        if (firmWareInfo == null) {
            ToastUtils.show("您还没选中固件!");
            return;
        }
        if (mListener == null) {
            return;
        }
        if (mListener.onPositiveClick(view, firmWareInfo)) {
            dismiss();
        }
    }

    /**
     * 加载更多
     */
    private void loadMore() {
        processQueryFirmwareList();
    }

    /**
     * 查询公司固件列表
     */
    private void processQueryFirmwareList() {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("companyID", MCloudApp.getCompanyID());
            if (productID != -1)
                jsonObjectRequest.put("productID", productID);//198
            jsonObjectRequest.put("nameAndVersion", false);
            jsonObjectRequest.put("pageSize", PAGE_SIZE);
            jsonObjectRequest.put("currentPage", pageInfo.getPage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);

        MDRetrofit.getInstance()
                .createService(ServiceAddressType.IOT_MANAGER_SERVICE_ADDRESS)
                .getFirmwareList(MCloudApp.getAccessToken(), body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe(new BaseObserver<PageResult<FirmWareInfo>>() {
                    @Override
                    protected void onResponse(PageResult<FirmWareInfo> data, ErrorInfo errorInfo) {
                        adpter.getLoadMoreModule().setEnableLoadMore(true);
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                if (data == null || data.getCurrentPageData() == null) {
                                    if (firmWareInfoList.size() == 0) {
                                        adpter.setEmptyView(R.layout.empty_view);
                                    } else {
                                        //显示没有更多数据布局
                                        if (isFullScreen())
                                            adpter.getLoadMoreModule().loadMoreEnd();
                                    }
                                    return;
                                }
                                if (pageInfo.isFirstPage()) {
                                    //如果是加载的第一页数据，用setNew
                                    firmWareInfoList.clear();
                                }
                                firmWareInfoList.addAll(data.getCurrentPageData());
                                adpter.notifyDataSetChanged();
                                if (data.getCurrentPageData().size() < PAGE_SIZE) {
                                    //如果不够一页,显示没有更多数据布局
                                    if (isFullScreen())
                                        adpter.getLoadMoreModule().loadMoreEnd();
                                } else {
                                    adpter.getLoadMoreModule().loadMoreComplete();
                                }
                                // page加一
                                pageInfo.nextPage();
                            } else {
                                adpter.getLoadMoreModule().loadMoreFail();
                                if (!TextUtils.isEmpty(errorInfo.getMsg())) {
                                    ToastUtils.show(errorInfo.getMsg());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        adpter.getLoadMoreModule().setEnableLoadMore(true);
                        adpter.getLoadMoreModule().loadMoreFail();
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }

    public void setDialogFragmentClickListener(DialogFragmentClickListener listener) {
        mListener = listener;
    }

    private boolean isFullScreen() {
        LinearLayoutManager llm = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        if (llm == null)
            return false;

        int lastCompletelyVisibleItemPosition = llm.findLastCompletelyVisibleItemPosition();

        return lastCompletelyVisibleItemPosition < firmWareInfoList.size() - 1;
    }
}
