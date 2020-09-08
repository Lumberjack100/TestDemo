package com.shmedo.mcloudapp.projects.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.bumptech.glide.Glide;
import com.gyf.immersionbar.ImmersionBar;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.model.CenterPoint;
import com.shmedo.mcloudapp.projects.model.ProjectInfoEx;
import com.shmedo.mcloudapp.util.DateUtil;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.ResponseHandler;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.config.IndicatorConfig;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.indicator.CircleIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

public class ProjectIntroductionActivity extends BaseActivity {
    private static final String PROJECT_ID = "project_id";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.banner)
    Banner banner;

    @BindView(R.id.tv_project_type)
    TextView tvProjectType;

    @BindView(R.id.tv_project_level)
    TextView tvProjectLevel;

    @BindView(R.id.tv_project_create_time)
    TextView tvProjectCreateTime;

    @BindView(R.id.tv_project_valid_period)
    TextView tvProjectValidPeriod;

    @BindView(R.id.tv_project_address)
    TextView tvProjectAddress;

    @BindView(R.id.tv_project_location)
    TextView tvProjectLoction;

    private List<String> imgUrlList = new ArrayList<>();

    private int projectID;


    public static void startActivity(Context context, int projectID) {
        Intent intent = new Intent(context, ProjectIntroductionActivity.class);
        intent.putExtra(PROJECT_ID, projectID);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_project_introduction;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("项目简介");
        parseIntent();
        setBanner();
        QueryProjectInfo();
    }

    /**
     * 初始化系统栏
     */
    @Override
    protected void initImmersionBar() {
        mToolbar.setBackgroundColor(ColorUtils.blendARGB(Color.TRANSPARENT, ContextCompat.getColor(this, R.color.white), 0));
        ImmersionBar.with(this)
                .titleBar(mToolbar)
                .statusBarColor(R.color.transparent, 0)
                .statusBarDarkFont(false)
                .navigationBarDarkIcon(true)
                .navigationBarColor(R.color.white)
                .init();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(PROJECT_ID)) {
            projectID = intent.getIntExtra(PROJECT_ID, 0);
        }
    }

    private void setBanner() {
//        imgUrlList.add("https://img.zcool.cn/community/013de756fb63036ac7257948747896.jpg");
//        imgUrlList.add("https://img.zcool.cn/community/01639a56fb62ff6ac725794891960d.jpg");
//        imgUrlList.add("https://img.zcool.cn/community/01270156fb62fd6ac72579485aa893.jpg");
//        imgUrlList.add("https://img.zcool.cn/community/01233056fb62fe32f875a9447400e1.jpg");

        banner.setAdapter(new BannerImageAdapter<String>(imgUrlList) {
            @Override
            public void onBindView(BannerImageHolder holder, String imageUrl, int position, int size) {
                //图片加载自己实现
                Glide.with(holder.itemView)
                        .load(imageUrl)
                        .thumbnail(Glide.with(holder.itemView).load(R.drawable.loading))
                        .into(holder.imageView);
            }
        });
        banner.addBannerLifecycleObserver(this);
        banner.setIndicator(new CircleIndicator(this));
        banner.setIndicatorGravity(IndicatorConfig.Direction.CENTER);
    }

    private void updateView(ProjectInfoEx projectInfoEx) {
        if (projectInfoEx == null || projectInfoEx.getProjInfo() == null)
            return;

        ProjectInfoEx.ProjInfoBean projInfoBean = projectInfoEx.getProjInfo();
        tvProjectType.setText(TextUtils.isEmpty(projectInfoEx.getProjTypeAlias()) ? "" : projectInfoEx.getProjTypeAlias());
        tvProjectLevel.setText("");
        tvProjectAddress.setText(TextUtils.isEmpty(projInfoBean.getLocation()) ? "" : projInfoBean.getLocation());

        if (!TextUtils.isEmpty(projInfoBean.getCreateTime())) {
            try {
                String createTime = DateUtil.StrToStrFormat(projInfoBean.getCreateTime(), "yyyy-MM-dd HH:mm:ss", "yyyy.MM.dd");
                tvProjectCreateTime.setText(createTime);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (!TextUtils.isEmpty(projInfoBean.getRegisterValidTime())) {
            try {
                String validTime = DateUtil.StrToStrFormat(projInfoBean.getRegisterValidTime(), "yyyy-MM-dd HH:mm:ss", "yyyy.MM.dd");
                tvProjectValidPeriod.setText(validTime);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(projInfoBean.getCenterPoint())) {
            try {
                CenterPoint centerPoint = GsonFactory.getGson().fromJson(projInfoBean.getCenterPoint(), CenterPoint.class);
                if (centerPoint != null) {
                    tvProjectLoction.setText(String.format("%s\n%s", centerPoint.getLat(), centerPoint.getLng()));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (projInfoBean.getImagePath() == null) {
            imgUrlList.add("");
        }
//        banner.getAdapter().notifyDataSetChanged();
    }

    /**
     * 查询警报阈值列表
     */
    private void QueryProjectInfo() {
        showLoadingDialog("加载数据中...");

        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, String.valueOf(projectID));
        MDRetrofit.getInstance()
                .createService()
                .GetProjectByIDEx(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ProjectInfoEx>() {
                    @Override
                    protected void onResponse(ProjectInfoEx data, ErrCode errCode) {
                        dismissLoadingDialog();
                        if (!ResponseHandler.getInstance().handleResponse(errCode)) {
                            if (errCode.getCode() == 0) {
                                updateView(data);
                            }else{
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
