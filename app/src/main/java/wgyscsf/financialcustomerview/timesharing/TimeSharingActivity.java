package wgyscsf.financialcustomerview.timesharing;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import wgyscsf.financialcustomerview.BaseActivity;
import wgyscsf.financialcustomerview.R;
import wgyscsf.financialcustomerview.utils.GsonUtil;
import wgyscsf.financialcustomerview.utils.StringUtils;

public class TimeSharingActivity extends BaseActivity {
    TimeSharingView tsv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_sharing);
        tsv = (TimeSharingView) findViewById(R.id.tsv);
        loadData();
        pushData();
    }


    private void loadData() {
        //模拟网络环境
        Observable.create(new Observable.OnSubscribe<List<Quotes>>() {
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
                            tsv.setTimeSharingData(o);
                        } else {
                            Log.e(TAG, "run: 数据适配失败、、、、");
                        }
                    }
                });
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

    //模拟推送实时数据
    private void pushData() {
        Observable.create(new Observable.OnSubscribe<Quotes>() {
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
                        tsv.addTimeSharingData(o);
                    }
                });
    }
}
