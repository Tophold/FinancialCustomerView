package com.tophold.example.demo.btc.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import com.tophold.example.BaseActivity;
import com.tophold.example.R;
import com.tophold.example.demo.btc.api.HuobiService;
import com.tophold.example.demo.btc.api.RetrofitManager;
import com.tophold.example.demo.btc.model.HuobiData;
import com.tophold.example.demo.btc.model.HuobiSymbol;
import com.tophold.example.demo.forex.ui.ForexActivity;
import com.tophold.trade.utils.StringUtils;

public class HuobiListActivity extends BaseActivity {

    RecyclerView mAflRlListview;
    BaseQuickAdapter<HuobiSymbol, BaseViewHolder> mQuickAdapter;
    List<HuobiSymbol> mForexList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_huobi_list);
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
    }

    private void initAdapter() {
        mQuickAdapter = new BaseQuickAdapter<HuobiSymbol, BaseViewHolder>(R.layout.item_activity_huobi_list, mForexList) {
            @Override
            protected void convert(BaseViewHolder helper, HuobiSymbol item) {
                helper.setText(R.id.iafl_tv_znName, item.base_currency + "/" + item.quote_currency);
                helper.setText(R.id.iafl_tv_enName, item.quote_currency);
                helper.setText(R.id.iafl_tv_part, item.symbol_partition);
            }
        };
        mAflRlListview.setLayoutManager(new LinearLayoutManager(mContext));
        mAflRlListview.setAdapter(mQuickAdapter);
    }

    private void loadData() {
        Map<String, Object> map = new HashMap<>();
        call(RetrofitManager.getInstance().create(HuobiService.class).getSymbolList(map), new Observer<HuobiData<List<HuobiSymbol>>>() {
            @Override
            public void onSubscribe(Disposable d) {
                unSubscription(d);
            }

            @Override
            public void onNext(HuobiData<List<HuobiSymbol>> huobiData) {
                String status = huobiData.status;
                if (!"ok".equals(status)) {
                    Toast.makeText(mContext, "数据获取失败...", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<HuobiSymbol> data = huobiData.data;
                if (StringUtils.isEmpty(data)) {
                    Toast.makeText(mContext, "数据为空...", Toast.LENGTH_SHORT).show();
                    return;
                }
                mForexList.addAll(data);

                //排序操作
                Collections.sort(mForexList, (o1, o2) -> {
                    if (!o1.symbol_partition.equals(o2.symbol_partition))
                        return -o1.symbol_partition.compareTo(o2.symbol_partition);
                    if (!o1.quote_currency.equals(o2.quote_currency))
                        return -o1.quote_currency.compareTo(o2.quote_currency);
                    return 0;
                });

                mQuickAdapter.notifyLoadMoreToLoading();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: ");
            }
        });
    }

    private void initListener() {
        mQuickAdapter.setOnItemClickListener((adapter, v, p) -> {
            HuobiSymbol forex = mForexList.get(p);
            Bundle bundle = new Bundle();
            bundle.putSerializable(HuobiActivity.KEY_FOREX, forex);
            go(HuobiActivity.class, bundle);
        });
    }
}
