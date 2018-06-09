package com.tophold.example;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import com.tophold.example.demo.forex.api.RetrofitManager;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/22 14:06
 * 描 述 ：
 * ============================================================
 **/
public class BaseFragment extends android.support.v4.app.Fragment {
    CompositeDisposable disposables;

    protected String TAG;
    protected Context mContext;
    protected Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = this.getClass().getSimpleName();
        mContext = this.getActivity();
        mActivity = getActivity();
        if (isBindEventBusHere()) {
            EventBus.getDefault().register(this);
        }
    }
    public boolean isBindEventBusHere() {
        return false;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBindEventBusHere()) {
            EventBus.getDefault().unregister(this);
        }
        if (disposables != null) disposables.clear();
    }

    public void unSubscription(Disposable disposable) {
        if (disposables == null) {
            synchronized (CompositeDisposable.class) {
                if (disposables == null) {
                    disposables = new CompositeDisposable();
                }
            }
        }
        disposables.add(disposable);
    }
    /**
     * @param observable
     * @param observer
     */
    public void call(Observable observable, Observer<?> observer) {
        RetrofitManager.call(observable, observer);
    }
}
