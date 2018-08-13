package com.tophold.trade.view.kview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tophold.trade.R;

import java.util.List;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/04 11:06
 * 描 述 ：KView入口
 * ============================================================
 **/
public final class KView extends KLayoutView {

    //主图展示的是蜡烛图还是分时图
    protected boolean isShowTimSharing = true;
    //设置数据精度
    protected int mDigit = 4;

    public KView(Context context) {
        this(context, null);
    }

    public KView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initListener();
    }


    private void initListener() {
        mMasterView.setMaxPostionListener((quotes, x, y) -> {
            Log.d(TAG, "initListener1: " + x + "," + y);
        });
        mMasterView.setMinPostionListener((quotes, x, y) -> {
            Log.d(TAG, "initListener2: " + x + "," + y);

        });
        mMasterView.setLastPostionListener((quotes, x, y) -> {
            Log.d(TAG, "initListener3: " + x + "," + y);
        });
    }

    /**
     * 数据设置入口
     *
     * @param quotesList
     */
    public void setKViewData(List<Quotes> quotesList, KViewListener.MasterTouchListener masterTouchListener) {
        if (quotesList == null || quotesList.isEmpty()) {
            Toast.makeText(getContext(), "数据异常1111", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "setKViewData: 数据异常111");
            return;
        }
        mMasterView.setTimeSharingData(quotesList, masterTouchListener);
        mMinorView.setTimeSharingData(quotesList, masterTouchListener);
        mVolView.setTimeSharingData(quotesList, masterTouchListener);
    }

    /**
     * 实时推送过来的数据，实时更新。
     * 这个地方可以优化：因为用户不知道什么时候可以Push过来数据，如果不处理。可能存在一种情况：数据还没加载完毕，push就过来了就会出现异常。
     *
     * @param quotes
     */
    public void pushKViewData(Quotes quotes, long period) {
        if (quotes == null) {
            //Toast.makeText(getContext(), "数据异常", Toast.LENGTH_SHORT).show();
            //Log.e(TAG, "setKViewData: 数据异常");
            return;
        }
        mMasterView.pushingTimeSharingData(quotes, period);
        mMinorView.pushingTimeSharingData(quotes, period);
        mVolView.pushingTimeSharingData(quotes, period);
    }

    /**
     * 加载更多数据
     *
     * @param quotesList
     */
    public void loadKViewData(List<Quotes> quotesList) {
        if (quotesList == null || quotesList.isEmpty()) {
            Toast.makeText(getContext(), "数据异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "setKViewData: 数据异常");
            return;
        }
        mMasterView.loadMoreTimeSharingData(quotesList);
        mMinorView.loadMoreTimeSharingData(quotesList);
        mVolView.loadMoreTimeSharingData(quotesList);
    }

    /**
     * 加载更多失败，在这里添加逻辑
     */
    public void loadMoreError() {
        mMasterView.loadMoreError();
    }

    /**
     * 加载更多成功，在这里添加逻辑
     */
    public void loadMoreSuccess() {
        mMasterView.loadMoreSuccess();
    }

    /**
     * 正在加载更多，在这里添加逻辑
     */
    public void loadMoreIng() {
        mMasterView.loadMoreIng();
    }

    /**
     * 没有更多数据，在这里添加逻辑
     */
    public void loadMoreNoData() {
        mMasterView.loadMoreNoData();
    }


    //-----------------------对开发者暴露可以修改的参数-------

    public boolean isShowTimSharing() {
        return isShowTimSharing;
    }

    public void setShowTimSharing(boolean showTimSharing) {
        isShowTimSharing = showTimSharing;
        mMasterView.setViewType(showTimSharing ? KViewType.MasterViewType.TIMESHARING : KViewType.MasterViewType.CANDLE);
    }

    public int getDigit() {
        return mDigit;
    }

    public void setDigit(int digit) {
        mDigit = digit;
        mMasterView.setDigits(mDigit);
        mMinorView.setDigits(mDigit);
    }
}
