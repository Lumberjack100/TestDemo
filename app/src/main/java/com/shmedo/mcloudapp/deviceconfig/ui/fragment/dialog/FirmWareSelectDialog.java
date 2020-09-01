package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.DeviceInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.adapter.DeviceFirmWareAdpter;
import com.shmedo.mcloudapp.deviceconfig.model.FirmWareInfo;
import com.shmedo.mcloudapp.deviceconfig.model.params.QueryFirmwareListParam;
import com.shmedo.mcloudapp.entity.PageResult;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.model.PageInfo;
import com.shmedo.mcloudapp.util.GsonFactory;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class FirmWareSelectDialog extends BaseDialogFragment {
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private DeviceFirmWareAdpter adpter;

    private List<FirmWareInfo> firmWareInfoList = new ArrayList<>();

    private static final int PAGE_SIZE = 10;
    private PageInfo pageInfo;
    private int companyID = 1;
    private int deviceTypeID = -1;

    private FirmWareInfo firmWareInfo = null;

    private DialogFragmentClickListener mListener;


    public FirmWareSelectDialog(int companyID, int deviceTypeID) {
        this.companyID = companyID;
        this.deviceTypeID = deviceTypeID;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_firm_ware_select_dialog;
    }

    protected void setWindowStyle(int gravity) {
        super.setWindowStyle(gravity);
        Dialog mDialog = getDialog();
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = (int) (DeviceInfo.getScreenHeight() * 0.5f);
        window.setAttributes(wlp);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTvTitle.setText("固件升级");
        pageInfo = new PageInfo(1);
        initAdapter();
        initLoadMore();
        processQueryFirmwareList();
    }


    private void initAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
//        DefaultItemDecoration mItemDecoration = new DefaultItemDecoration(ContextCompat.getColor(getActivity(), R.color.divider_line_bg_efefef), 0, DensityUtil.Dp2Px(getActivity(), 0.5f));
//        mRecyclerView.addItemDecoration(mItemDecoration);
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
     * 查询公司设备在线统计信息
     */
    private void processQueryFirmwareList() {
        QueryFirmwareListParam parameter = new QueryFirmwareListParam();
        parameter.setCompanyID(companyID);
        parameter.setDeviceTypeID(deviceTypeID);//deviceTypeID
        parameter.setPageSize(PAGE_SIZE);
        parameter.setCurrentPage(pageInfo.getPage());

        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .QueryFirmwareList(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<PageResult<FirmWareInfo>>() {
                    @Override
                    public void Success(PageResult<FirmWareInfo> data, String message) {
                        adpter.getLoadMoreModule().setEnableLoadMore(true);

                        if (data == null || data.getCurrentPageData() == null || data.getCurrentPageData().size() == 0) {
                            return;
                        }

                        if (pageInfo.isFirstPage()) {
                            //如果是加载的第一页数据，用setNew
                            firmWareInfoList.clear();
                            firmWareInfoList.addAll(data.getCurrentPageData());
                            adpter.notifyDataSetChanged();
                        } else {
                            //不是第一页，则用add
                            firmWareInfoList.addAll(data.getCurrentPageData());
                            adpter.notifyDataSetChanged();
                        }

                        if (data.getCurrentPageData().size() < PAGE_SIZE) {
                            //如果不够一页,显示没有更多数据布局
                            adpter.getLoadMoreModule().loadMoreEnd();

                        } else {
                            adpter.getLoadMoreModule().loadMoreComplete();
                        }
                        // page加一
                        pageInfo.nextPage();
                    }

                    @Override
                    public void Failure(String message) {
                        Timber.w("请求失败--%s", message);
                        adpter.getLoadMoreModule().setEnableLoadMore(true);
                        adpter.getLoadMoreModule().loadMoreFail();
                    }
                });
    }

    public void setDialogFragmentClickListener(DialogFragmentClickListener listener) {
        mListener = listener;
    }
}
