package com.shmedo.mcloudapp.user.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.user.model.CompanyInfo;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

public class CompanyHomePageActivity extends BaseActivity {
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

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


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, CompanyHomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_company_home_page;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("企业详情");
        getCompanyInfo();
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

    @OnClick({R.id.websiteLayout})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.websiteLayout) {
            String url = mTvWebsite.getText().toString();
            if (!TextUtils.isEmpty(url)) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        }
    }

    /**
     * 查询单个公司信息
     */
    private void getCompanyInfo() {
        showLoadingDialog("加载数据中...");

        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, "");
        MDRetrofit.getInstance()
                .createService()
                .GetCompanyInfo(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<CompanyInfo>() {
                    @Override
                    public void Success(CompanyInfo companyInfo, String message) {
                        dismissLoadingDialog();
                        updateView(companyInfo);
                    }

                    @Override
                    public void Failure(String message) {
                        dismissLoadingDialog();
                        Timber.w("服务器连接失败--%s", message);
                    }
                });
    }
}
