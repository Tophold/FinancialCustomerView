package wgyscsf.financialcustomerview.timesharing;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import wgyscsf.financialcustomerview.BaseActivity;
import wgyscsf.financialcustomerview.R;
import wgyscsf.financialcustomerview.utils.FormatUtil;
import wgyscsf.financialcustomerview.utils.GsonUtil;
import wgyscsf.financialcustomerview.utils.StringUtils;
import wgyscsf.financialcustomerview.utils.TimeUtils;

/**
 * timesharing0:模拟的是加载更多的数据，注意，会分段取，模拟的是多次加载更多
 * timesharing1：模拟的是api请求的数据集合，注意：一次加载完毕，模拟的是第一次加载的数据
 * timesharing2：模拟的是实时**推送**的数据，注意：会分段取，一次取一个。
 */
public class TimeSharingActivity extends BaseActivity {
    TimeSharingView mTimeSharingView;
    private LinearLayout ats_ll_container;
    private TextView mAtsTvH;
    private TextView mAtsTvO;
    private TextView mAtsTvL;
    private TextView mAtsTvC;
    private TextView mAtsTvP;
    private TextView ats_tv_time;

    List<Quotes> mLoadMoreList;
    int index = 0;//加载更多，加载到哪儿了。因为真实应用中，也存在加载完毕的情况。这里对应加载到list的最后

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_sharing);
        bindView();
        loadData();
        pushData();
        mTimeSharingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //这里先预加载加载更多的数据，然后加载更多的时候分段取出来，模拟加载更多
        initLoadMore();
    }

    private void initLoadMore() {
        mLoadMoreList = new ArrayList<>();
        String originalFundData = SimulateNetAPI.getOriginalFundData(mContext, 0);
        if (originalFundData == null) {
            Log.e(TAG, "loadData: 从网络获取到的数据为空");
            return;
        }
        try {
            List<OriginQuotes> quotesList = GsonUtil.fromJson2Object(originalFundData, new TypeToken<List<OriginQuotes>>() {
            }.getType());
            mLoadMoreList = adapterData(quotesList);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void bindView() {
        ats_ll_container = (LinearLayout) findViewById(R.id.ats_ll_container);
        mTimeSharingView = (TimeSharingView) findViewById(R.id.tsv);
        mAtsTvH = (TextView) findViewById(R.id.ats_tv_h);
        mAtsTvO = (TextView) findViewById(R.id.ats_tv_o);
        mAtsTvL = (TextView) findViewById(R.id.ats_tv_l);
        mAtsTvC = (TextView) findViewById(R.id.ats_tv_c);
        mAtsTvP = (TextView) findViewById(R.id.ats_tv_p);
        ats_tv_time = (TextView) findViewById(R.id.ats_tv_time);
    }


    private void loadData() {
        //模拟网络环境
        Subscription subscribeApi = Observable.create(new Observable.OnSubscribe<List<Quotes>>() {
            @Override
            public void call(Subscriber<? super List<Quotes>> subscriber) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String originalFundData = SimulateNetAPI.getOriginalFundData(mContext, 1);
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
                List<Quotes> quotesList = adapterData(OriginFundModeList);
                //发送数据
                subscriber.onNext(quotesList);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Quotes>>() {
                    @Override
                    public void call(List<Quotes> o) {
                        if (o != null) {
                            mTimeSharingView.setTimeSharingData(o, new TimeSharingView.TimeSharingListener() {

                                @Override
                                public void onLongTouch(Quotes preQuotes, Quotes currentQuotes) {
                                    showContanier(preQuotes, currentQuotes);
                                }

                                @Override
                                public void onUnLongTouch() {
                                    ats_ll_container.setVisibility(View.INVISIBLE);
                                }

                                @Override
                                public void needLoadMore() {
                                    Log.e(TAG, "needLoadMore: 需要加载更多了..");
                                    loadMoreData();
                                }
                            });
                        } else {
                            Log.e(TAG, "run: 数据适配失败、、、、");
                        }
                    }
                });
        //及时回收，防止泄露
        addGcManagerSubscription(subscribeApi);
    }

    private void showContanier(Quotes preQuotes, Quotes currentQuotes) {
        ats_ll_container.setVisibility(View.VISIBLE);
        int digits = 4;
        boolean isPositive;
        String precent;
        double dis = (currentQuotes.c - preQuotes.c) / currentQuotes.c * 100;
        isPositive = dis >= 0;
        precent = FormatUtil.formatBySubString(dis, 2);
        precent += "%";

        //
        mAtsTvH.setText(FormatUtil.numFormat(currentQuotes.h, digits));
        mAtsTvO.setText(FormatUtil.numFormat(currentQuotes.o, digits));
        mAtsTvL.setText(FormatUtil.numFormat(currentQuotes.l, digits));
        mAtsTvC.setText(FormatUtil.numFormat(currentQuotes.c, digits));
        mAtsTvP.setText(precent);
        ats_tv_time.setText(TimeUtils.millis2String(currentQuotes.t, new SimpleDateFormat("MM-dd HH:mm")));

        if (isPositive) {
            mAtsTvH.setTextColor(getMyColor(R.color.color_timeSharing_callBackRed));
            mAtsTvO.setTextColor(getMyColor(R.color.color_timeSharing_callBackRed));
            mAtsTvL.setTextColor(getMyColor(R.color.color_timeSharing_callBackRed));
            mAtsTvC.setTextColor(getMyColor(R.color.color_timeSharing_callBackRed));
            mAtsTvP.setTextColor(getMyColor(R.color.color_timeSharing_callBackRed));
        } else {
            mAtsTvH.setTextColor(getMyColor(R.color.color_timeSharing_callBackGreen));
            mAtsTvO.setTextColor(getMyColor(R.color.color_timeSharing_callBackGreen));
            mAtsTvL.setTextColor(getMyColor(R.color.color_timeSharing_callBackGreen));
            mAtsTvC.setTextColor(getMyColor(R.color.color_timeSharing_callBackGreen));
            mAtsTvP.setTextColor(getMyColor(R.color.color_timeSharing_callBackGreen));
        }
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

    private int getMyColor(@ColorRes int colorId) {
        return getResources().getColor(colorId);
    }

    //模拟推送实时数据
    private void pushData() {
        Subscription subscribeSocekt = Observable.create(new Observable.OnSubscribe<Quotes>() {
            @Override
            public void call(Subscriber<? super Quotes> subscriber) {
                //5s后获取实时推送数据
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String originalFundData = SimulateNetAPI.getOriginalFundData(mContext, 2);
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
                final List<Quotes> quotesList = adapterData(OriginFundModeList);


                for (int i = 0; i < Integer.MAX_VALUE; i++) {
                    try {
                        //模拟不定时推送数据
                        Thread.sleep(StringUtils.getRadomNum(500, 3000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (i >= quotesList.size() - 1) {
                        subscriber.onCompleted();
                        break;
                    }
                    subscriber.onNext(quotesList.get(i));

                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Quotes>() {
                    @Override
                    public void call(Quotes o) {
                       mTimeSharingView.addTimeSharingData(o);
                    }
                });

        //及时回收，防止泄露
        addGcManagerSubscription(subscribeSocekt);
    }

    private void loadMoreData() {
        if (mLoadMoreList.isEmpty()) return;
        Observable.create(new Observable.OnSubscribe<List<Quotes>>() {
            @Override
            public void call(Subscriber<? super List<Quotes>> subscriber) {
                try {
                    Thread.sleep(StringUtils.getRadomNum(1000, 5000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int size = mLoadMoreList.size();
                int min = size / 20;
                int max = size / 5;//一次最多加载多少
                int loadSize = StringUtils.getRadomNum(min, max);
                if (index == loadSize) {
                    //没有更多数据了
                    mTimeSharingView.loadMoreNoData();
                }
                if ((index + loadSize) > mLoadMoreList.size()) {
                    loadSize = mLoadMoreList.size();
                }
                List<Quotes> loadList = mLoadMoreList.subList(index, index + loadSize);
                index = index + loadSize;//重置起始位置
                subscriber.onNext(loadList);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Quotes>>() {
                    @Override
                    public void call(List<Quotes> integer) {
                        mTimeSharingView.loadMoreData(integer);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "call: 加载更多出现了异常");
                        mTimeSharingView.loadMoreError();
                    }
                });
    }
}
