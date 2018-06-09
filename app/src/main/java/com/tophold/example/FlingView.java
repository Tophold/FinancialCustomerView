package com.tophold.example;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.tophold.trade.view.BaseView;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 更新时间 ：2018/05/06 12:50
 * 描 述 ：
 * ============================================================
 */
public class FlingView extends BaseView {

    private GestureDetectorCompat mGestureDetector;

    public FlingView(Context context) {
        this(context,null);
    }

    public FlingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FlingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGestureDetector = new GestureDetectorCompat(mContext, mSimpleOnGestureListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener =
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    Log.d(TAG, "onFling0: " + velocityX);
                    //x方向
                    int dx = (int) (e2.getX() - e1.getX());
                    //降噪处理
                    if (Math.abs(dx) > def_onfling) {
                        //x轴幅度大于y轴
                        if (Math.abs(velocityX) > Math.abs(velocityY)) {
                            //向右边
                            if (velocityX > 0) {
                                //移动k线图
                                //innerMoveViewListener(dx);
                                Log.d(TAG, "onFling1: " + dx);
                                return true;
                            } else {
                                //向左边
                                //移动k线图
                                //innerMoveViewListener(dx);
                                Log.d(TAG, "onFling2: " + dx);
                                return true;
                            }
                        }
                    }

                    //y轴类似

                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            };
}
