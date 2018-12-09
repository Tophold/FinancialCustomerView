package com.tophold.trade.view.seekbar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 更新时间 ：2018/11/30 14:13
 * 描 述 ：
 * ============================================================
 */
public class DoubleTumb {
    public double mMin;
    public double mMax;
    public float mCorners;
    @ColorInt
    public int mBackground;
    @NonNull
    public Thumb mThumbA;
    @Nullable
    public Thumb mThumbB;//是否启用双thumb以这个参数为空不为空所解决。当这个不为空则显示双thumb.
    public int mDigit = 4;//精度
    //最小值从左边开始还是从右边开始,和前景图无关
    public boolean mMinLeft = true;

    /**
     * 内部属性
     */
    //背景的四边内间距
    public int mLineTop, mLineBottom, mLineLeft, mLineRight;


    public DoubleTumb(double mMin, double mMax, float mCorners,
                      @ColorInt int mBackground,
                      @NonNull Thumb mThumbA,
                      @Nullable Thumb mThumbB) {
        this.mMin = mMin;
        this.mMax = mMax;
        this.mCorners = mCorners;
        this.mBackground = mBackground;
        this.mThumbA = mThumbA;
        this.mThumbB = mThumbB;
    }


    public static class Thumb {
        public boolean mFromLeft = true;
        @ColorInt
        public int mForeground;
        public Drawable mForegroundDrawable;

        //进度，确认当前thumb所在的位置，值为百分比
        public double progress;
        //thumb
        private Drawable mThumb;
        private int mThumbWidth;
        private int mThumbHight;
        //是否展示当前htumb的值
        public boolean mShowTips;

        /**
         * 内部使用属性
         */
        //thumb的位置坐标，四个点的,用于判断手指是否在按压范围内。左、上、右、下
        private float[] mTumbPosArr = new float[4];
        //是否正在按压
        public boolean mTouch = false;

        public int getmThumbWidth() {
            if (mThumbWidth <= 0) {
                if (mThumb != null)
                    return mThumb.getIntrinsicWidth();
            }
            return mThumbWidth;
        }

        public Thumb setmThumbWidth(int mThumbWidth) {
            this.mThumbWidth = mThumbWidth;
            return this;
        }

        public int getmThumbHight() {
            if (mThumbHight <= 0) {
                if (mThumb != null)
                    return mThumb.getIntrinsicHeight();
            }
            return mThumbHight;
        }

        public Thumb setmThumbHight(int mThumbHight) {
            this.mThumbHight = mThumbHight;
            return this;
        }

        public Thumb(int mForeground) {
            this.mForeground = mForeground;
        }

        public Thumb(boolean mFromLeft, Drawable mForegroundDrawable) {
            this.mFromLeft = mFromLeft;
            this.mForegroundDrawable = mForegroundDrawable;
        }

        public Drawable getmThumb() {
            return mThumb;
        }

        public Thumb setmThumb(Drawable mThumb) {
            this.mThumb = mThumb;
            return this;
        }

        public float[] getmTumbPosArr() {
            return mTumbPosArr;
        }

        /**
         * 传递的x、y是否在按压范围内
         *
         * @param context
         * @param x
         * @param y
         * @return
         */
        public boolean isTouchThumb(Context context, float x, float y) {
            float touchOffset = getTouchOffset(context);
            float[] ints = getmTumbPosArr();
            ints[0] -= touchOffset;
            ints[1] -= touchOffset;
            ints[2] += touchOffset;
            ints[3] += touchOffset;
            return x >= ints[0] && x <= ints[2] && y >= ints[1] && y <= ints[3];
        }

        /**
         * 扩大按压区域
         *
         * @param context
         * @return
         */
        public float getTouchOffset(Context context) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (2 * scale + 0.5f);
        }
    }
}
