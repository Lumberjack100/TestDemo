package com.shmedo.mcloudapp.deviceconfig.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/18 <br/>
 * 描述：     TODO #gh#
 */
public class PageAdapter extends FragmentStateAdapter {
    private List<Fragment> mFragments;

    public PageAdapter(@NonNull FragmentActivity fragmentActivity, List<Fragment> fragments) {
        super(fragmentActivity);
        this.mFragments = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getItemCount() {
        return mFragments.size();
    }
}
