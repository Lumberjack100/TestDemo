package com.shmedo.mcloudapp.ui.activity.device.config.senior;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import butterknife.BindView;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import java.util.Objects;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.config
 * 文件名:   InstructionDebugActivity
 * 创建者:   dpc
 * 创建时间:  2019/2/19 13:59
 * 描述：    指令交互调试模式
 */
public class InstructionDebugActivity extends BaseActivity {

    @BindView(R.id.toolbar_title) TextView mToolbarTitle;
    @BindView(R.id.toolbar) Toolbar mToolbar;


    @Override protected int initContentView() {
        return R.layout.activity_instruction_debug;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mToolbarTitle.setText("指令交互调试模式");
    }

}
