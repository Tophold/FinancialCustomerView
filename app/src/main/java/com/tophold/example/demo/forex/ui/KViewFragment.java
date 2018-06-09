package com.tophold.example.demo.forex.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import com.tophold.example.BaseFragment;
import com.tophold.example.R;
import com.tophold.example.demo.forex.api.RetrofitManager;
import com.tophold.example.demo.forex.api.XcfdService;
import com.tophold.example.demo.forex.model.Forex;
import com.tophold.example.demo.forex.model.WsPrice;
import com.tophold.example.demo.forex.model.XcfdQuotes;
import com.tophold.trade.utils.StringUtils;
import com.tophold.example.ForexTab;
import com.tophold.trade.view.kview.KView;
import com.tophold.trade.view.kview.KViewListener;
import com.tophold.trade.view.kview.Quotes;

public class KViewFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final int DEF_COUNT = 100;
    private View mRootView;
    private KView mFkKvKview;
    ForexTab mForexTab;
    Forex mForex;

    //net
    List<Quotes> mQuotesList;

    public KViewFragment() {

    }

    public static KViewFragment newInstance(ForexTab mForexTab, Forex forex) {
        KViewFragment fragment = new KViewFragment();
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
            mForexTab = (ForexTab) getArguments().getSerializable(ARG_PARAM1);
            mForex = (Forex) getArguments().getSerializable(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_kview, container, false);
        initView();
        initData();
        initAdapter();
        loadData(null, false);
        initListener();
        return mRootView;
    }

    private void initView() {
        mFkKvKview = (KView) mRootView.findViewById(R.id.fk_kv_kview);

        mFkKvKview.setDigit(5);

        if (mForexTab.mForexType == ForexTab.ForexType._timeShring) {
            mFkKvKview.setShowTimSharing(true);
        } else {
            mFkKvKview.setShowTimSharing(false);
        }

        if (((ForexActivity) getActivity()).isHorizontal()) {
            mFkKvKview.setShowMinor(true);
        } else {
            mFkKvKview.setShowMinor(false);
        }
    }

    private void initData() {
        mQuotesList = new ArrayList<>();
    }

    private void initAdapter() {

    }

    private void loadData(String start_time, final boolean isLoadMore) {
        Map<String, Object> map = new HashMap<>();
        map.put("count", DEF_COUNT);
        if (start_time != null) map.put("st", start_time);
        call(RetrofitManager.getInstance().create(XcfdService.class).chartQuotes(mForex.enName, mForexTab.getType(), map), new Observer<List<XcfdQuotes>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<XcfdQuotes> xcfdQuotesList) {
                if (StringUtils.isEmpty(xcfdQuotesList)) {
                    Log.d(TAG, "onNext: 数据为空");
                    return;
                }
                adapterData(xcfdQuotesList);

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
                    ((ForexActivity) getActivity()).showContanier(preQuotes, currentQuotes);
                }

                @Override
                public void onUnLongTouch() {
                    ((ForexActivity) getActivity()).hidenContainer();
                }

                @Override
                public void needLoadMore() {
                    if (StringUtils.isEmpty(mQuotesList)) {
                        mFkKvKview.loadMoreNoData();
                        return;
                    }
                    Quotes quotes = mQuotesList.get(0);
                    String showTime = quotes.getShowTime();
                    loadData(showTime, true);
                }
            });
        } else {
            mFkKvKview.loadKViewData(mQuotesList);
        }
    }

    private void adapterData(List<XcfdQuotes> xcfdQuotesList) {
        List<Quotes> quotesList = new ArrayList<>();
        for (XcfdQuotes xcfdQuotes : xcfdQuotesList) {
            Quotes quotes = new Quotes(xcfdQuotes.o, xcfdQuotes.h, xcfdQuotes.l, xcfdQuotes.c, xcfdQuotes.s, xcfdQuotes.e);
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
    public void onRevicePrice(WsPrice wsPrice) {
        if (wsPrice == null || !wsPrice.productName.equals(mForex.enName)) return;
        double currPrice = (wsPrice.askPrice + wsPrice.bidPrice) / 2.0;//均价
        String currPriceStr = String.valueOf(currPrice);

        Quotes quotes = new Quotes(currPriceStr, currPriceStr, currPriceStr, currPriceStr, wsPrice.lastUpdateTime, wsPrice.lastUpdateTime);

        mFkKvKview.pushKViewData(quotes, mForexTab.getTypeLength() * 1000);
    }
}
