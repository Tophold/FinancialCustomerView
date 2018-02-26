package wgyscsf.financialcustomerview;

import android.nfc.Tag;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/01/09 17:27
 * 描 述 ：
 * ============================================================
 **/
public class Test {
    boolean isLoadComty = false;

    void callBack(int process) {
        isLoadComty = false;
        if (process == 100) {
            isLoadComty = true;
        }

    }

    public static void test() {
        Subscription subscribe = Observable.interval(5000, TimeUnit.MILLISECONDS)
                .doOnNext(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        //这里处理子线程
                        Log.e("TAG", "call1: " + Thread.currentThread());

                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        //这里处理主线程
                        Log.e("TAG", "call2: " + Thread.currentThread());
                    }
                });

        //需要的时候用，不一定是退出页面。不需要上面轮训就可以取消
        //subscribe.unsubscribe();

        Observable.interval(5000, TimeUnit.MILLISECONDS)
                .doOnNext(x -> Log.e("TAG", "test: "))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(x -> Log.e("TAG", "test: "));

    }

}
