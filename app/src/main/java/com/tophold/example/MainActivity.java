package com.tophold.example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tophold.example.base.BaseActivity;
import com.tophold.example.demo.fund.FundActivity;
import com.tophold.example.demo.kview.KViewActivity;
import com.tophold.example.demo.kview.beginner.ui.KViewHorizontalActivityActivity;
import com.tophold.example.demo.kview.beginner.ui.KViewVerticalActivity;
import com.tophold.example.demo.kview.btc.ui.HuobiListActivity;
import com.tophold.example.demo.kview.forex.ui.ForexListActivity;
import com.tophold.example.demo.pie.PieChartActivity;
import com.tophold.example.demo.seekbar.DoubleThumbSeekBarActivity;
import com.tophold.example.utils.RxUtils;
import com.tophold.trade.utils.StringUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity {
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.version);
        mTextView.setText(getVersionStr());
    }

    public String getVersionStr() {
        String version = "verisonName:";
        version += BuildConfig.VERSION_NAME;
        version += ",versionCode(git head):" + BuildConfig.VERSION_CODE;

        return version;
    }

    public void fundView(View view) {
        go(FundActivity.class);
    }

    public void kViewDemo(View view) {
        go(KViewActivity.class);
    }

    public void onPieTest(View view) {
        go(PieChartActivity.class);
    }

    public void onSeekBarTest(View view) {
        go(DoubleThumbSeekBarActivity.class);
    }


    public void onRx(View view) {
        Disposable disposable = Observable.create((ObservableOnSubscribe<String>) emitter -> {
            while (true) {
                emitter.onNext("a");
                Thread.sleep(500);
                emitter.onNext("b");
                Thread.sleep(2000);
                emitter.onNext("c");
                Thread.sleep(9000);
                emitter.onNext("d");
            }
        }).observeOn(Schedulers.io())
                .compose(RxUtils.rxApiSchedulerHelper())
                .sample(1000, TimeUnit.MILLISECONDS)
                .subscribe(s -> Log.d(TAG, "accept: " + s));
    }
}
