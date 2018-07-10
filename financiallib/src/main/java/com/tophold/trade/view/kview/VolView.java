package com.tophold.trade.view.kview;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import java.util.List;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 更新时间 ：2018/07/08 21:18
 * 描 述 ：
 * ============================================================
 */
public class VolView extends KBaseView {

    //数据源
    private List<VolModel> mVolModelList;
    private boolean mIsShow=false;



    public VolView(Context context) {
        this(context,null);
    }

    public VolView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public VolView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr();
    }

    private void initAttr() {
        initDefAttrs();
    }

    private void initDefAttrs() {
        //重写内边距大小
        mInnerTopBlankPadding = 8;
        mInnerBottomBlankPadding = 8;

        //重写Legend padding
        mLegendPaddingTop = 0;
        mLegendPaddingRight = 4;
        mLegendPaddingLeft = 4;


        setShowInnerX(false);
        setShowInnerY(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void seekAndCalculateCellData() {

    }

    public void setVolDataList(@Nullable List<VolModel> mDataList) {

    }
}
