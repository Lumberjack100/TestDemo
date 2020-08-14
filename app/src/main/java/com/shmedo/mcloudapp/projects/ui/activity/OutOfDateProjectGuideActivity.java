package com.shmedo.mcloudapp.projects.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;

import butterknife.BindView;

public class OutOfDateProjectGuideActivity extends BaseActivity {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.tv_project_name)
    TextView mTvProName;

    @BindView(R.id.tv_valid_period)
    TextView mTvValidPeriod;


    public static void startActivity(Context context, String name, String validPeriod) {
        Intent intent = new Intent(context, OutOfDateProjectGuideActivity.class);
        intent.putExtra(ARG_PARAM1, name);
        intent.putExtra(ARG_PARAM2, validPeriod);

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_out_of_date_project_guide;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("项目过期提示");
        parseIntent();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            String name = intent.getStringExtra(ARG_PARAM1);
            String validPeriod = intent.getStringExtra(ARG_PARAM2);

            if (!TextUtils.isEmpty(name)) {
                mTvProName.setText(name);
            }

            if (!TextUtils.isEmpty(validPeriod)) {
                mTvValidPeriod.setText(validPeriod);
            }
        }
    }
}
