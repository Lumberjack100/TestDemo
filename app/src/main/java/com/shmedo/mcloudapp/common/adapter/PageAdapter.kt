package com.shmedo.mcloudapp.common.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/18 <br></br>
 * 描述：
 */
class PageAdapter(fragmentActivity: FragmentActivity, private val mFragments: List<Fragment>) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return mFragments[position]
    }

    override fun getItemCount(): Int {
        return mFragments.size
    }
}
