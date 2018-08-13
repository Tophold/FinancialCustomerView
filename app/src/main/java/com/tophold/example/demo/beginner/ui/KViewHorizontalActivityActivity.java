package com.tophold.example.demo.beginner.ui;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import com.tophold.example.R;
import com.tophold.example.demo.beginner.model.OriginQuotes;
import com.tophold.example.demo.beginner.api.KViewSimulateNetAPI;
import com.tophold.example.BaseActivity;
import com.tophold.trade.utils.FormatUtil;
import com.tophold.example.GsonUtil;
import com.tophold.trade.utils.StringUtils;
import com.tophold.trade.utils.TimeUtils;
import com.tophold.trade.view.kview.KView;
import com.tophold.trade.view.kview.KViewListener;
import com.tophold.trade.view.kview.Quotes;

/**
 * timesharing0:模拟的是加载更多的数据，注意，会分段取，模拟的是多次加载更多
 * timesharing1：模拟的是api请求的数据集合，注意：一次加载完毕，模拟的是第一次加载的数据
 * timesharing2：模拟的是实时**推送**的数据，注意：会分段取，一次取一个。
 */
public class KViewHorizontalActivityActivity extends BaseActivity {
    //bind view
    private LinearLayout mAkvLlContainer;
    private TextView mAkvTvH;
    private TextView mAkvTvO;
    private TextView mAkvTvTime;
    private TextView mAkvTvL;
    private TextView mAkvTvC;
    private TextView mAkvTvP;
    private KView mAkvKvKview;

    //模拟网络过来的列表数据
    List<Quotes> mQuotesList;
    //模拟加载更多的数据
    List<Quotes> mLoadMoreData;
    //模拟socket推送过来的单个数据
    List<Quotes> mPushData;
    //加载更多，加载到哪儿了。因为真实应用中，也存在加载完毕的情况。这里对应加载到list的最后
    int index = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kview_horizontal);
        initView();
        initData();
        loadData();
        pushData();
    }

    private void pushData() {
        Disposable disposable = Observable.interval(StringUtils.getRadomNum(300, 3000), TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            int size = mPushData.size();
                            if (data < size) {
                                mAkvKvKview.pushKViewData(mPushData.get(data.intValue()), 0);
                            } else {

                            }


                        }
                        , throwable -> throwable.printStackTrace());

        unSubscription(disposable);
    }

    private void initView() {
        mAkvLlContainer = (LinearLayout) findViewById(R.id.akv_ll_container);
        mAkvTvH = (TextView) findViewById(R.id.akv_tv_h);
        mAkvTvO = (TextView) findViewById(R.id.akv_tv_o);
        mAkvTvTime = (TextView) findViewById(R.id.akv_tv_time);
        mAkvTvL = (TextView) findViewById(R.id.akv_tv_l);
        mAkvTvC = (TextView) findViewById(R.id.akv_tv_c);
        mAkvTvP = (TextView) findViewById(R.id.akv_tv_p);
        mAkvKvKview = (KView) findViewById(R.id.akv_kv_kview);
    }

    private void initData() {
        mQuotesList = new ArrayList<>();
        mLoadMoreData = new ArrayList<>();
        mPushData = new ArrayList<>();

        //这里先预加载加载更多的数据，然后加载更多的时候分段取出来，模拟加载更多
        initLoadMoreData();

        //pushData
        initPushData();

    }

    private void initPushData() {
        String originalFundData = KViewSimulateNetAPI.getOriginalFundData(mContext, 2);
        if (originalFundData == null) {
            Log.e(TAG, "loadData: 从网络获取到的数据为空");
            return;
        }
        List<OriginQuotes> OriginFundModeList;
        try {
            OriginFundModeList = GsonUtil.fromJson2Object(originalFundData, new TypeToken<List<OriginQuotes>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //开始适配图表数据
        mPushData = adapterData(OriginFundModeList);

    }

    private void initLoadMoreData() {
        String originalFundData = KViewSimulateNetAPI.getOriginalFundData(mContext, 0);
        if (originalFundData == null) {
            Log.e(TAG, "loadData: 从网络获取到的数据为空");
            return;
        }
        try {
            List<OriginQuotes> quotesList = GsonUtil.fromJson2Object(originalFundData,
                    new TypeToken<List<OriginQuotes>>() {
                    }.getType());
            mLoadMoreData = adapterData(quotesList);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void loadData() {
        //模拟网络环境加载数据列表
        Disposable disposable = Observable.timer(StringUtils.getRadomNum(500, 2000),
                TimeUnit.MILLISECONDS)
                .doOnNext(data -> {
                    String originalData = KViewSimulateNetAPI.getOriginalFundData(mContext, 2);
                    if (originalData == null) {
                        Log.e(TAG, "loadData: 从网络获取到的数据为空");
                        return;
                    }
                    try {
                        List<OriginQuotes> originQuotes = GsonUtil
                                .fromJson2Object(originalData, new TypeToken<List<OriginQuotes>>() {
                                }.getType());
                        mQuotesList = adapterData(originQuotes);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "loadData: json转换错误");
                        return;
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> mAkvKvKview.setKViewData(mQuotesList, new KViewListener.MasterTouchListener() {
                            @Override
                            public void onLongTouch(Quotes preQuotes, Quotes currentQuotes) {
                                showContanier(preQuotes, currentQuotes);
                            }

                            @Override
                            public void onUnLongTouch() {
                                mAkvLlContainer.setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void needLoadMore() {
                                loadMore();
                            }
                        }),
                        Throwable::printStackTrace
                );
        unSubscription(disposable);
    }

    private void loadMore() {
        if (mLoadMoreData == null) return;
        Disposable disposable = Observable.timer(StringUtils.getRadomNum(1000, 5000), TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            int size = mLoadMoreData.size();
                            int min = size / 20;
                            int max = size / 5;//一次最多加载多少
                            int loadSize = StringUtils.getRadomNum(min, max);
                            if (index == loadSize) {
                                //没有更多数据了
                                mAkvKvKview.loadMoreNoData();
                            }
                            if ((index + loadSize) > mLoadMoreData.size()) {
                                loadSize = mLoadMoreData.size();
                            }
                            List<Quotes> loadList = mLoadMoreData.subList(index, index + loadSize);
                            index = index + loadSize;//重置起始位置
                            mAkvKvKview.loadKViewData(loadList);
                        }
                        , throwable -> throwable.printStackTrace());
        unSubscription(disposable);

    }

    private List<Quotes> adapterData(List<OriginQuotes> originFundModeList) {
        List<Quotes> fundModeList = new ArrayList<>();//适配后的数据
        for (OriginQuotes OriginQuotes : originFundModeList) {
            Quotes Quotes = new Quotes(OriginQuotes.o, OriginQuotes.h, OriginQuotes.l,
                    OriginQuotes.c, OriginQuotes.t);
            fundModeList.add(Quotes);
        }
        return fundModeList;
    }

    private void showContanier(Quotes preQuotes, Quotes currentQuotes) {
        mAkvLlContainer.setVisibility(View.VISIBLE);
        int digits = 4;
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

    public void showCandle(View view) {
        boolean showTimSharing = mAkvKvKview.isShowTimSharing();
        mAkvKvKview.setShowTimSharing(!showTimSharing);
        if (!showTimSharing) {
            ((Button) view).setText("点击展示蜡烛图");
        } else {
            ((Button) view).setText("点击展示分时图");
        }
    }
}