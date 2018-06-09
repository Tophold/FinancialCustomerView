package com.tophold.example.demo.btc.ui;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.tophold.example.BaseActivity;
import com.tophold.example.NoScrollViewPager;
import com.tophold.example.R;
import com.tophold.example.demo.btc.model.HuobiSymbol;
import com.tophold.example.demo.btc.model.HuobiTab;
import com.tophold.trade.utils.FormatUtil;
import com.tophold.trade.utils.TimeUtils;
import com.tophold.trade.view.kview.Quotes;

public class HuobiActivity extends BaseActivity {
    public static final String KEY_FOREX = "KEY_FOREX";
    public static final String KEY_HORIZONTAL = "KEY_HORIZONTAL";
    private TabLayout mAfTlTablayout;
    private NoScrollViewPager mAfVpViewpager;

    private LinearLayout mAkvLlContainer;
    private TextView mAkvTvH;
    private TextView mAkvTvO;
    private TextView mAkvTvTime;
    private TextView mAkvTvL;
    private TextView mAkvTvC;
    private TextView mAkvTvP;
    private TextView af_tv_symbol;
    private TextView mAkvTvBlank;

    //是否是横屏
    boolean mIsHorizontal = false;

    private List<HuobiTab> mForexTabList;
    private List<Fragment> mKViewFragmentList;

    HuobiSymbol mForex;

    @Override
    protected void getBundleExtras(Bundle extras) {
        super.getBundleExtras(extras);
        mForex = (HuobiSymbol) extras.getSerializable(KEY_FOREX);
        mIsHorizontal = extras.getBoolean(KEY_HORIZONTAL);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mIsHorizontal) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,

                    WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        }
        setContentView(R.layout.activity_huobi);
        initView();
        initData();
        initAdapter();
        loadData();
        initListener();
    }

    @Override
    protected void onResume() {
        if (mIsHorizontal) {
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
        super.onResume();
    }

    private void initView() {
        mAfTlTablayout = (TabLayout) findViewById(R.id.af_tl_tablayout);
        mAfVpViewpager = (NoScrollViewPager) findViewById(R.id.af_vp_viewpager);

        mAkvLlContainer = (LinearLayout) findViewById(R.id.akv_ll_container);
        mAkvTvH = (TextView) findViewById(R.id.akv_tv_h);
        mAkvTvO = (TextView) findViewById(R.id.akv_tv_o);
        mAkvTvTime = (TextView) findViewById(R.id.akv_tv_time);
        mAkvTvL = (TextView) findViewById(R.id.akv_tv_l);
        mAkvTvC = (TextView) findViewById(R.id.akv_tv_c);
        mAkvTvP = (TextView) findViewById(R.id.akv_tv_p);
        mAkvTvBlank = (TextView) findViewById(R.id.af_tv_blank);
        af_tv_symbol = (TextView) findViewById(R.id.af_tv_symbol);

        if (mIsHorizontal) {
            af_tv_symbol.setVisibility(View.GONE);
            mAkvTvBlank.setVisibility(View.GONE);
        } else {
            af_tv_symbol.setVisibility(View.VISIBLE);
            mAkvTvBlank.setVisibility(View.VISIBLE);
            af_tv_symbol.setText(mForex.base_currency + "/" + mForex.quote_currency);
        }

    }

    private void initData() {
        mForexTabList = new ArrayList<>();
        mForexTabList.add(new HuobiTab(HuobiTab.HuobiType._timeShring, "分时"));
        mForexTabList.add(new HuobiTab(HuobiTab.HuobiType._1m, "1分"));
        mForexTabList.add(new HuobiTab(HuobiTab.HuobiType._5m, "5分"));
        mForexTabList.add(new HuobiTab(HuobiTab.HuobiType._15m, "15分"));
        mForexTabList.add(new HuobiTab(HuobiTab.HuobiType._30m, "30分"));
        mForexTabList.add(new HuobiTab(HuobiTab.HuobiType._60m, "1小时"));
        mForexTabList.add(new HuobiTab(HuobiTab.HuobiType._1d, "日K"));
        mForexTabList.add(new HuobiTab(HuobiTab.HuobiType._1w, "周K"));
        mForexTabList.add(new HuobiTab(HuobiTab.HuobiType._1mon, "月K"));
        mForexTabList.add(new HuobiTab(HuobiTab.HuobiType._1y, "年K"));

        mAfTlTablayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        mKViewFragmentList = new ArrayList<>();
        for (HuobiTab forexTab : mForexTabList) {
            mKViewFragmentList.add(HuobiFragment.newInstance(forexTab, mForex));
        }
    }

    private void initAdapter() {
        mAfVpViewpager.setAdapter(new HuobiAdapter(getSupportFragmentManager(), mKViewFragmentList, mForexTabList));
        mAfTlTablayout.setupWithViewPager(mAfVpViewpager);
    }

    private void loadData() {

    }

    private void initListener() {

    }


    public void hidenContainer() {
        mAfTlTablayout.setVisibility(View.VISIBLE);
        mAkvLlContainer.setVisibility(View.GONE);
    }

    public void showContanier(Quotes preQuotes, Quotes currentQuotes) {
        mAfTlTablayout.setVisibility(View.GONE);
        mAkvLlContainer.setVisibility(View.VISIBLE);
        int digits = 5;
        boolean isPositive;
        String precent;
        double dis = (currentQuotes.c - preQuotes.c) / currentQuotes.c * 100;
        isPositive = dis >= 0;
        precent = FormatUtil.formatBySubString(dis, 2);
        precent += "%";

        //
        mAkvTvH.setText(FormatUtil.numFormat(currentQuotes.h, digits));
        mAkvTvO.setText(FormatUtil.numFormat(currentQuotes.o, digits));
        mAkvTvL.setText(FormatUtil.numFormat(currentQuotes.l, digits));
        mAkvTvC.setText(FormatUtil.numFormat(currentQuotes.c, digits));
        mAkvTvP.setText(precent);
        mAkvTvTime.setText(TimeUtils.millis2String(currentQuotes.t, new SimpleDateFormat("MM-dd HH:mm")));

        if (isPositive) {
            mAkvTvH.setTextColor(getMyColor(R.color.color_timeSharing_callBackRed));
            mAkvTvO.setTextColor(getMyColor(R.color.color_timeSharing_callBackRed));
            mAkvTvL.setTextColor(getMyColor(R.color.color_timeSharing_callBackRed));
            mAkvTvC.setTextColor(getMyColor(R.color.color_timeSharing_callBackRed));
            mAkvTvP.setTextColor(getMyColor(R.color.color_timeSharing_callBackRed));
        } else {
            mAkvTvH.setTextColor(getMyColor(R.color.color_timeSharing_callBackGreen));
            mAkvTvO.setTextColor(getMyColor(R.color.color_timeSharing_callBackGreen));
            mAkvTvL.setTextColor(getMyColor(R.color.color_timeSharing_callBackGreen));
            mAkvTvC.setTextColor(getMyColor(R.color.color_timeSharing_callBackGreen));
            mAkvTvP.setTextColor(getMyColor(R.color.color_timeSharing_callBackGreen));
        }
    }

    private int getMyColor(@ColorRes int colorId) {
        return getResources().getColor(colorId);
    }

    public void showHorizontal(View view) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_HORIZONTAL, true);
        bundle.putSerializable(KEY_FOREX, mForex);
        go(HuobiActivity.class, bundle);
    }

    public boolean isHorizontal() {
        return mIsHorizontal;
    }

}
