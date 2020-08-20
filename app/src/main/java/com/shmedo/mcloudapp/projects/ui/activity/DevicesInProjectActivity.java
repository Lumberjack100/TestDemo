package com.shmedo.mcloudapp.projects.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.projects.adapter.ProjectPageAdapter;
import com.shmedo.mcloudapp.projects.ui.fragment.ConstructionFragment;
import com.shmedo.mcloudapp.projects.ui.fragment.DevicesInProjectFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class DevicesInProjectActivity extends BaseActivity implements TabLayout.OnTabSelectedListener {
    private static final String PROJECT_ID = "project_id";

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager2 viewPager;

    private FragmentStateAdapter pagerAdapter;
    private TabLayoutMediator tabLayoutMediator;

    private int projectID;

    public static void startActivity(Context context, int projectID) {
        Intent intent = new Intent(context, DevicesInProjectActivity.class);
        intent.putExtra(PROJECT_ID, projectID);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_devices_in_project;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        initView();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(PROJECT_ID)) {
            projectID = intent.getIntExtra(PROJECT_ID, 0);
        }
    }


    private void setTabs() {
        View tabView = LayoutInflater.from(this).inflate(R.layout.custom_tab_text, null);
        TextView textView = tabView.findViewById(R.id.tabText);
        textView.setText("设备");
        textView.setTextColor(ContextCompat.getColor(this, R.color.title_text_color));
        textView.setTextSize(18);
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setCustomView(textView);
        tabLayout.addTab(tab);

        View tabView2 = LayoutInflater.from(this).inflate(R.layout.custom_tab_text, null);
        TextView textView2 = tabView2.findViewById(R.id.tabText);
        textView2.setText("施工");
        textView2.setTextColor(ContextCompat.getColor(this, R.color.sub_title_text_color));
        textView2.setTextSize(17);
        TabLayout.Tab tab2 = tabLayout.newTab();
        tab2.setCustomView(textView2);
        tabLayout.addTab(tab2);
        tabLayout.addOnTabSelectedListener(this);
    }

    private void initView() {
        List<Fragment> mFragments = new ArrayList<>();
        mFragments.add(new DevicesInProjectFragment());
        mFragments.add(new ConstructionFragment());
        pagerAdapter = new ProjectPageAdapter(this, mFragments);
        viewPager.setAdapter(pagerAdapter);
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position == 0) {
                    View tabView = LayoutInflater.from(DevicesInProjectActivity.this).inflate(R.layout.custom_tab_text, null);
                    TextView textView = tabView.findViewById(R.id.tabText);
                    textView.setText("设备");
                    textView.setTextColor(ContextCompat.getColor(DevicesInProjectActivity.this, R.color.title_text_color));
                    textView.setTextSize(18);
                    tab.setCustomView(textView);
                } else {
                    View tabView = LayoutInflater.from(DevicesInProjectActivity.this).inflate(R.layout.custom_tab_text, null);
                    TextView textView = tabView.findViewById(R.id.tabText);
                    textView.setText("施工");
                    textView.setTextColor(ContextCompat.getColor(DevicesInProjectActivity.this, R.color.sub_title_text_color));
                    textView.setTextSize(17);
                    tab.setCustomView(textView);
                }
            }
        });
        tabLayoutMediator.attach();
        tabLayout.addOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        TextView textView = (TextView) tab.getCustomView();
        textView.setTextColor(ContextCompat.getColor(this, R.color.title_text_color));
        textView.setTextSize(18);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        TextView textView = (TextView) tab.getCustomView();
        textView.setTextColor(ContextCompat.getColor(this, R.color.sub_title_text_color));
        textView.setTextSize(17);
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @OnClick({R.id.iv_back, R.id.view_icon})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;

            case R.id.view_icon:
                ProjectIntroductionActivity.startActivity(this, projectID);
                break;
        }
    }


}
