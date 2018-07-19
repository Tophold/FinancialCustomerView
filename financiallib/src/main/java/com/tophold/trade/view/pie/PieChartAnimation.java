package com.tophold.trade.view.pie;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.tophold.trade.utils.StringUtils;

import java.util.List;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 更新时间 ：2018/07/17 16:50
 * 描 述 ：
 * ============================================================
 */
public class PieChartAnimation extends Animation {
    public static final String TAG = PieChartAnimation.class.getSimpleName();
    List<PieEntrys> mPieEntrysList;
    float mSumValue;
    PieChartView mView;

    public void setPieChartData(List<PieEntrys> pieEntrysList, float sumValue, PieChartView view) {
        this.mPieEntrysList = pieEntrysList;
        this.mSumValue = sumValue;
        this.mView = view;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        if (StringUtils.isEmpty(mPieEntrysList) || mSumValue <= 0 || mView == null) return;
        Log.d(TAG, "applyTransformation1: " + interpolatedTime);
        for (int i = 0; i < mPieEntrysList.size(); i++) {
            PieEntrys data = mPieEntrysList.get(i);
            //通过总和来计算百分比
            float percentage = data.value / mSumValue;
            //通过百分比来计算对应的角度
            float angle = percentage * 360;
            //根据插入时间来计算角度
            angle = angle * interpolatedTime;
            data.mSweepAngle = angle;
        }
        mView.invalidate();
    }
}
