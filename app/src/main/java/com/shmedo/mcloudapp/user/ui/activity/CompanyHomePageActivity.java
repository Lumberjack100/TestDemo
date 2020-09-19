package com.shmedo.mcloudapp.user.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.model.FirmWareInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.user.model.CompanyInfo;
import com.shmedo.mcloudapp.user.model.CompanySimpleInfo;
import com.shmedo.mcloudapp.user.ui.fragment.CompanySwitchDialogFragment;
import com.shmedo.mcloudapp.util.ResponseHandler;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

public class CompanyHomePageActivity extends BaseActivity {
    private static final String ARG_PARAM1 = "param1";

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.tv_action)
    TextView mTvAction;

    @BindView(R.id.tv_companyName)
    TextView mTvCompanyName;

    @BindView(R.id.tv_companyType)
    TextView mTvCompanyType;

    @BindView(R.id.tv_IndustryName)
    TextView mTvIndustryName;

    @BindView(R.id.tv_phone)
    TextView mTvPhone;

    @BindView(R.id.tv_address)
    TextView mTvAddress;

    @BindView(R.id.tv_website)
    TextView mTvWebsite;

    @BindView(R.id.tv_companyIntro)
    TextView mTvCompanyIntro;


    public static void startActivity(Context context, CompanyInfo companyInfo) {
        Intent intent = new Intent(context, CompanyHomePageActivity.class);
        intent.putExtra(ARG_PARAM1, companyInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_company_home_page;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("企业详情");
        mTvAction.setText("切换企业");
        mTvAction.setVisibility(View.VISIBLE);
        parseIntent();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(ARG_PARAM1)) {
            CompanyInfo companyInfo = (CompanyInfo) intent.getSerializableExtra(ARG_PARAM1);
            updateView(companyInfo);
        }
    }

    private void updateView(CompanyInfo companyInfo) {
        if (companyInfo == null)
            return;

        mTvCompanyName.setText(companyInfo.getFullName() != null ? companyInfo.getFullName() : "");
        mTvCompanyType.setText("");
        mTvIndustryName.setText(companyInfo.getIndustry() != null ? companyInfo.getIndustry() : "");
        mTvPhone.setText(companyInfo.getPhone() != null ? companyInfo.getPhone() : "");
        mTvAddress.setText(companyInfo.getAddress() != null ? companyInfo.getAddress() : "");
        mTvWebsite.setText(companyInfo.getWebSite() != null ? companyInfo.getWebSite() : "");
        mTvCompanyIntro.setText(companyInfo.getDesc() != null ? companyInfo.getDesc() : "");
    }

    @OnClick({R.id.tv_action, R.id.websiteLayout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_action:
                CompanySwitchDialogFragment newFragment = new CompanySwitchDialogFragment();
                newFragment.setDialogFragmentClickListener(companySwitchListener);
                newFragment.show(getSupportFragmentManager(), "dialog");
                break;

            case R.id.websiteLayout:
                String url = mTvWebsite.getText().toString();
                if (!TextUtils.isEmpty(url)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
                break;

        }
    }

    private BaseDialogFragment.DialogFragmentClickListener companySwitchListener = new BaseDialogFragment.DialogFragmentClickListener<CompanySimpleInfo>() {
        @Override
        public boolean onPositiveClick(View view, CompanySimpleInfo companySimpleInfo) {
            if (companySimpleInfo != null) {
                MCloudApp.setCompanyID(companySimpleInfo.getCompanyID());
                getCompanyInfo(companySimpleInfo.getCompanyID());
            }
            return true;
        }


        @Override
        public void onNegativeClick(View view) {

        }
    };

    /**
     * 查询单个公司信息
     */
    private void getCompanyInfo(int companyID) {
        showLoadingDialog("加载中...");

        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, String.valueOf(companyID));
        MDRetrofit.getInstance()
                .createService()
                .GetCompanyInfo(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<CompanyInfo>() {
                    @Override
                    protected void onResponse(CompanyInfo companyInfo, ErrCode errCode) {
                        dismissLoadingDialog();

                        if (!ResponseHandler.getInstance().handleResponse(errCode)) {
                            if (errCode.getCode() == 0) {
                                updateView(companyInfo);
                            } else {
                                if (!TextUtils.isEmpty(errCode.getErrMessage())) {
                                    ToastUtils.show(errCode.getErrMessage());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }
}
