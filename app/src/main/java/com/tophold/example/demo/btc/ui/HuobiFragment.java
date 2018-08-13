package com.tophold.example.demo.btc.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import com.tophold.example.BaseFragment;
import com.tophold.example.R;
import com.tophold.example.demo.btc.api.HuobiService;
import com.tophold.example.demo.btc.api.HuobiSocketApi;
import com.tophold.example.demo.btc.api.RetrofitManager;
import com.tophold.example.demo.btc.model.HuobiData;
import com.tophold.example.demo.btc.model.HuobiQuote;
import com.tophold.example.demo.btc.model.HuobiSymbol;
import com.tophold.example.demo.btc.model.HuobiTab;
import com.tophold.example.demo.btc.model.HuobiWsQuote;
import com.tophold.trade.utils.StringUtils;
import com.tophold.trade.view.kview.KView;
import com.tophold.trade.view.kview.KViewListener;
import com.tophold.trade.view.kview.Quotes;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/26 13:36
 * 描 述 ：
 * ============================================================
 **/
public class HuobiFragment extends BaseFragment {
    public static final int DEF_PAGER_SIZE = 1000;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final int DEF_COUNT = 100;
    private View mRootView;
    private KView mFkKvKview;
    HuobiTab mForexTab;
    HuobiSymbol mForex;

    //net
    List<Quotes> mQuotesList;

    public HuobiFragment() {

    }

    public static HuobiFragment newInstance(HuobiTab mForexTab, HuobiSymbol forex) {
        HuobiFragment fragment = new HuobiFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, mForexTab);
        args.putSerializable(ARG_PARAM2, forex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mForexTab = (HuobiTab) getArguments().getSerializable(ARG_PARAM1);
            mForex = (HuobiSymbol) getArguments().getSerializable(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_huobi, container, false);
        initView();
        initData();
        initAdapter();
        loadData();
        initListener();
        return mRootView;
    }

    private void loadData() {
        loadKLineData(null, false);

        subKLineData();
    }

    private void subKLineData() {
        HuobiSocketApi.subKLine(mForex.base_currency + mForex.quote_currency, mForexTab.getTypeLength() * 1000, true);
    }

    private void initView() {
        mFkKvKview = (KView) mRootView.findViewById(R.id.fk_kv_kview);

        mFkKvKview.setDigit(5);

        if (mForexTab.mForexType == HuobiTab.HuobiType._timeShring) {
            mFkKvKview.setShowTimSharing(true);
        } else {
            mFkKvKview.setShowTimSharing(false);
        }

        if (((HuobiActivity) getActivity()).isHorizontal()) {
            mFkKvKview.setShowMinor(true);
            mFkKvKview.setShowVol(true);
        } else {
            mFkKvKview.setShowMinor(false);
            mFkKvKview.setShowVol(false);
        }
    }

    private void initData() {
        mQuotesList = new ArrayList<>();
    }

    private void initAdapter() {

    }

    private void loadKLineData(String start_time, final boolean isLoadMore) {
        Map<String, Object> map = new HashMap<>();
        String symbol = mForex.base_currency + "" + mForex.quote_currency;
        map.put("symbol", symbol);
        map.put("period", mForexTab.getType());
        map.put("size", DEF_PAGER_SIZE);
        //if (start_time != null) map.put("st", start_time);
        call(RetrofitManager.getInstance().create(HuobiService.class).chartQuotes(map), new Observer<HuobiData<List<HuobiQuote>>>() {
            @Override
            public void onSubscribe(Disposable d) {
                unSubscription(d);
            }

            @Override
            public void onNext(HuobiData<List<HuobiQuote>> huobiData) {

                String status = huobiData.status;
                if (!"ok".equals(status)) {
                    Toast.makeText(mContext, "数据获取失败...", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<HuobiQuote> data = huobiData.data;
                if (StringUtils.isEmpty(data)) {
                    Toast.makeText(mContext, "数据为空...", Toast.LENGTH_SHORT).show();
                    return;
                }
                adapterData(data);

                drawKView(isLoadMore);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void drawKView(boolean isLoadMore) {
        if (!isLoadMore) {
            mFkKvKview.setKViewData(mQuotesList, new KViewListener.MasterTouchListener() {
                @Override
                public void onLongTouch(Quotes preQuotes, Quotes currentQuotes) {
                    ((HuobiActivity) getActivity()).showContanier(preQuotes, currentQuotes);
                }

                @Override
                public void onUnLongTouch() {
                    ((HuobiActivity) getActivity()).hidenContainer();
                }

                @Override
                public void needLoadMore() {
                    if (StringUtils.isEmpty(mQuotesList)) {
                        mFkKvKview.loadMoreNoData();
                        return;
                    }
                    Quotes quotes = mQuotesList.get(0);
                    String showTime = quotes.getShowTime();
                    //loadKLineData(showTime, true);火币不支持分页
                }
            });
        } else {
            mFkKvKview.loadKViewData(mQuotesList);
        }
    }

    private void adapterData(List<HuobiQuote> huobiQuoteList) {

        Collections.reverse(huobiQuoteList);

        List<Quotes> quotesList = new ArrayList<>();
        for (HuobiQuote huobiQuote : huobiQuoteList) {
            Quotes quotes = new Quotes(huobiQuote.open, huobiQuote.high, huobiQuote.low, huobiQuote.close, huobiQuote.id * 1000, huobiQuote.amount);
            quotesList.add(quotes);
        }
        mQuotesList.addAll(0, quotesList);//注意时序问题
    }

    private void initListener() {
    }

    @Override
    public boolean isBindEventBusHere() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReviceWsQuotes(HuobiWsQuote huobiWsQuote) {
        if (huobiWsQuote == null || huobiWsQuote.tick == null) return;
        //返回的数据真实醉了。。。
        String ch = huobiWsQuote.ch;
        String[] split = ch.split("\\.");
        String symbol = split[1];
        String lineType = split[3];
        if (!symbol.equals(mForex.base_currency + mForex.quote_currency)) return;
        if (!lineType.equals(mForexTab.getType())) return;
        HuobiWsQuote.TickBean tick = huobiWsQuote.tick;
        Quotes quotes = new Quotes(tick.open, tick.high, tick.low, tick.close, huobiWsQuote.ts);

        mFkKvKview.pushKViewData(quotes, mForexTab.getTypeLength() * 1000);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        HuobiSocketApi.subKLine(mForex.base_currency + mForex.quote_currency, mForexTab.getTypeLength() * 1000, false);
    }
}
