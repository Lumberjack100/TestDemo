package com.shmedo.mcloudapp.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   UserInfoActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/14 15:01
 * 描述：    TODO
 */
public class UserInfoActivity extends BaseActivity {

    @BindView(R.id.circle_image) CircleImageView mCircleImage;
    @BindView(R.id.ll_change_photo) LinearLayout mLlChangePhoto;
    @BindView(R.id.tv_account) TextView mTvAccount;
    @BindView(R.id.tv_phone) TextView mTvPhone;
    @BindView(R.id.tv_name) TextView mTvName;
    @BindView(R.id.tv_company) TextView mTvCompany;
    @BindView(R.id.tv_department) TextView mTvDepartment;
    @BindView(R.id.RL_advice) RelativeLayout mRLAdvice;


    @Override protected int initContentView() {
        return R.layout.activity_user_info;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @OnClick({ R.id.ll_change_photo, R.id.RL_advice })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_change_photo:
                break;
            case R.id.RL_advice:
                break;
        }
    }
}
