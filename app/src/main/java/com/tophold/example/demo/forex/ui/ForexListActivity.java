package com.tophold.example.demo.forex.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import com.tophold.example.R;
import com.tophold.example.demo.forex.model.Forex;
import com.tophold.example.BaseActivity;

public class ForexListActivity extends BaseActivity {
    RecyclerView mAflRlListview;
    BaseQuickAdapter<Forex, BaseViewHolder> mQuickAdapter;
    List<Forex> mForexList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forex_list);
        initView();
        initData();
        initAdapter();
        loadData();
        initListener();
    }

    private void initView() {
        mAflRlListview = (RecyclerView) findViewById(R.id.afl_rl_listview);
    }

    private void initData() {
        mForexList = new ArrayList<>();
        mForexList.add(new Forex("1", "EURUSD", "欧元/美元"));
        mForexList.add(new Forex("2", "USDJPY", "美元/日元"));
        mForexList.add(new Forex("3", "AUDUSD", "澳元/美元"));
        mForexList.add(new Forex("4", "GBPUSD", "英镑/美元"));
        mForexList.add(new Forex("5", "USDCHF", "美元/瑞郎"));
        mForexList.add(new Forex("6", "USDSGD", "美元/新加坡元"));
        mForexList.add(new Forex("7", "USDCAD", "美元/加元"));
        mForexList.add(new Forex("8", "EURJPY", "欧元/日元"));
        mForexList.add(new Forex("9", "EURGBP", "欧元/英镑"));
        mForexList.add(new Forex("10", "NZDUSD", "纽元/美元"));
        mForexList.add(new Forex("11", "GBPJYP", "英镑/日元"));
        mForexList.add(new Forex("12", "USDMXN", "美元/墨西哥比索"));
        mForexList.add(new Forex("13", "GOLD", "黄金"));
        mForexList.add(new Forex("14", "SILVER", "白银"));
    }

    private void initAdapter() {
        mQuickAdapter = new BaseQuickAdapter<Forex, BaseViewHolder>(R.layout.item_activity_forex_list, mForexList) {
            @Override
            protected void convert(BaseViewHolder helper, Forex item) {
                helper.setText(R.id.iafl_tv_enName, item.enName);
                helper.setText(R.id.iafl_tv_znName, item.cnName);
            }
        };
        mAflRlListview.setLayoutManager(new LinearLayoutManager(mContext));
        mAflRlListview.setAdapter(mQuickAdapter);
    }

    private void loadData() {

    }

    private void initListener() {
        mQuickAdapter.setOnItemClickListener((adapter, v, p) -> {
            Forex forex = mForexList.get(p);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ForexActivity.KEY_FOREX, forex);
            go(ForexActivity.class, bundle);
        });
    }
}
