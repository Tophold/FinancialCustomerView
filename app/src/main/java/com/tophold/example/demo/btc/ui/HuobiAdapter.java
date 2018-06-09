package com.tophold.example.demo.btc.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import com.tophold.example.demo.btc.model.HuobiTab;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/26 13:37
 * 描 述 ：
 * ============================================================
 **/
public class HuobiAdapter extends FragmentPagerAdapter {
    private List<Fragment> mFragmentList;
    List<HuobiTab> mForexTabList;

    public HuobiAdapter(FragmentManager fm, List<Fragment> fragmentList, List<HuobiTab> mForexTabList) {
        super(fm);
        this.mFragmentList = fragmentList;
        this.mForexTabList = mForexTabList;
    }


    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mForexTabList.get(position).tabName;
    }
}
