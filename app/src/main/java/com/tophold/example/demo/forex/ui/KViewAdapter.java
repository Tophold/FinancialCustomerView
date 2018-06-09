package com.tophold.example.demo.forex.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import com.tophold.example.ForexTab;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/22 14:38
 * 描 述 ：
 * ============================================================
 **/
public class KViewAdapter extends FragmentPagerAdapter {
    private List<Fragment> mFragmentList;
    List<ForexTab> mForexTabList;

    public KViewAdapter(FragmentManager fm, List<Fragment> fragmentList, List<ForexTab> mForexTabList) {
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
