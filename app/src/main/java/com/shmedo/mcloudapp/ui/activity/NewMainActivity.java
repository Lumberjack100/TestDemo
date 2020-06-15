package com.shmedo.mcloudapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.maps.ui.activity.MapActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class NewMainActivity extends BaseActivity {

    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;


    public static void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, NewMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_new_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(savedInstanceState);
    }

    private void initView(Bundle savedInstanceState) {
        initBottomNavigationItemSelectedListener();
    }

    private void initBottomNavigationItemSelectedListener() {
        //为底部导航设置条目选中监听
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_config_module:

                        break;

                    case R.id.item_project_module:

                        break;
                    case R.id.item_knowledge_module:

                        break;

                    case R.id.item_me_module:

                        break;
                }

                return true;    //这里返回true，表示事件已经被处理。如果返回false，为了达到条目选中效果，还需要下面的代码
                // item.setChecked(true);  不论点击了哪一个，都手动设置为选中状态true（该控件并没有默认实现)
                // 。如果不设置，只有第一个menu展示的时候是选中状态，其他的即便被点击选中了，图标和文字也不会做任何更改
            }
        });
    }

    @OnClick({R.id.image_map_module})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_map_module:
                MapActivity.startActivity(NewMainActivity.this);
                break;
        }
    }
}
