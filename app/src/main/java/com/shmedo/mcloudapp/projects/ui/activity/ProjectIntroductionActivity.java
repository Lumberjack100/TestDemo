package com.shmedo.mcloudapp.projects.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.youth.banner.Banner;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class ProjectIntroductionActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;

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

    @BindView(R.id.tv_project_desc)
    TextView tvProjectDesc;

    private List<String> imgUrlList = new ArrayList<>();

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ProjectIntroductionActivity.class);
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
        mToolbarTitle.setText("关于");
        setBanner();
        initData();
    }

    private void setBanner() {

//        banner.setAdapter(new BannerImageAdapter<String>(imgUrlList) {
//            @Override
//            public void onBindView(BannerImageHolder holder, String imageUrl, int position, int size) {
//                //图片加载自己实现
//                Glide.with(holder.itemView)
//                        .load(imageUrl)
//                        .thumbnail(Glide.with(holder.itemView).load(R.drawable.loading))
//                        .into(holder.imageView);
//            }
//        });
//        banner.addBannerLifecycleObserver(this);
//        banner.setIndicator(new CircleIndicator(this));
//        banner.setIndicatorGravity(IndicatorConfig.Direction.CENTER);

    }

    private void initData() {
        tvProjectType.setText("地质灾害");
        tvProjectLevel.setText("3级");
        tvProjectCreateTime.setText("2019.10.22");
        tvProjectDesc.setText(getResources().getString(R.string.project_desc_test));
    }


}
