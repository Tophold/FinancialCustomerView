package com.tophold.trade.view.seekbar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;


import com.tophold.trade.utils.RenderUtils;
import com.tophold.trade.view.BaseView;

import java.util.Arrays;


/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 更新时间 ：2018/11/30 14:00
 * 描 述 ：
 * ============================================================
 */
public class DoubleThumbSeekBar extends BaseView {

    private DoubleTumb mDoubleThumb;

    //背景相关
    private Paint mBackgroudPaint;
    private RectF mBackgroudRectF;

    //seekbar四个顶点
    private int[] mPostionArr;

    //前景图1
    private Paint mForegroundPaintA;
    private RectF mForegroundRectFA;
    private Bitmap mThumbBitmapA;
    private Paint mTipPaintA;

    //前景图2
    private Paint mForegroundPaintB;
    private RectF mForegroundRectFB;
    private Bitmap mThumbBitmapB;
    private Paint mTipPaintB;

    //滑动监听
    private OnDoubleThumbChangeListener mOnDoubleThumbChangeListener;

    //是否拦截事件
    private boolean mDisallowIntercept = false;

    /**
     * 设置监听
     *
     * @param onDoubleThumbChangeListener
     */
    public void setOnDoubleThumbChangeListener(OnDoubleThumbChangeListener onDoubleThumbChangeListener) {
        mOnDoubleThumbChangeListener = onDoubleThumbChangeListener;
    }


    public DoubleThumbSeekBar(Context context) {
        this(context, null);
    }

    public DoubleThumbSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoubleThumbSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(DoubleTumb doubleThumb) {
        this.mDoubleThumb = doubleThumb;
        initArrts();
    }

    private void initArrts() {
        if (mDoubleThumb == null) {
            throw new IllegalArgumentException("请先初始化:init(DoubleThumbSeekBar doubleThumbSeekBar)");
        }
        initPaint();
    }

    private void initPaint() {
        mBackgroudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroudPaint.setColor(mDoubleThumb.mBackground);
        mBackgroudPaint.setStyle(Paint.Style.FILL);
        mBackgroudRectF = new RectF();

        /**
         * thumbA相关设置
         */
        mForegroundPaintA = new Paint(Paint.ANTI_ALIAS_FLAG);
        Drawable foregroundDrawableA = mDoubleThumb.mThumbA.mForegroundDrawable;
        if (foregroundDrawableA != null) {
            // TODO: 03/12/2018
        } else {
            mForegroundPaintA.setColor(mDoubleThumb.mThumbA.mForeground);
        }
        mForegroundPaintA.setStyle(Paint.Style.FILL);

        mForegroundRectFA = new RectF();
        //tipA
        mTipPaintA = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTipPaintA.setStyle(Paint.Style.STROKE);
        mTipPaintA.setColor(mDoubleThumb.mThumbA.mForeground);
        mTipPaintA.setTextSize(sp2px(17));


        /**
         * thumbB相关设置
         */
        DoubleTumb.Thumb thumbB = mDoubleThumb.mThumbB;
        if (thumbB != null) {
            mForegroundPaintB = new Paint(Paint.ANTI_ALIAS_FLAG);
            mForegroundPaintB.setColor(thumbB.mForeground);

            Drawable foregroundDrawableB = thumbB.mForegroundDrawable;
            if (foregroundDrawableB != null) {
                //            mForegroundPaintA.
                // TODO: 03/12/2018
            } else {
                mForegroundPaintB.setColor(thumbB.mForeground);
            }

            mForegroundPaintB.setStyle(Paint.Style.FILL);
            mForegroundRectFB = new RectF();
            //tipB
            mTipPaintB = new Paint(Paint.ANTI_ALIAS_FLAG);
            mTipPaintB.setStyle(Paint.Style.STROKE);
            mTipPaintB.setColor(mDoubleThumb.mThumbB.mForeground);
            mTipPaintB.setTextSize(sp2px(17));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDoubleThumb == null) {
            throw new IllegalArgumentException("请初始化init()");
        }
        drawBg(canvas);
        //绘制前景图
        drawForegroundThumbA(canvas);
        drawForegroundThumbB(canvas);

        //绘制thumb
        drawThumbA(canvas);
        drawThumbB(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        DoubleTumb.Thumb mThumbA = mDoubleThumb.mThumbA;
        DoubleTumb.Thumb mThumbB = mDoubleThumb.mThumbB;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mDisallowIntercept) getParent().requestDisallowInterceptTouchEvent(true);
                /**
                 * 判断当前手指是否在触摸thumbA和thumb(存在时)。如果thumbA和thumbB同时存在，优先thumbA为主，且只能触摸一个。
                 */
                boolean touchThumbA = mThumbA.isTouchThumb(getContext(), x, y);
                mThumbA.mTouch = touchThumbA;
                if (!touchThumbA) {
                    if (mThumbB != null) {
                        mThumbB.mTouch = mThumbB.isTouchThumb(getContext(), x, y);
                        Log.d(TAG, "onTouchEvent: " + Arrays.toString(mThumbA.getmTumbPosArr()) + "," + x + "," + y + "," + mThumbA.mTouch);
                    }
                } else {
                    if (mThumbB != null) {
                        mThumbB.mTouch = false;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mDisallowIntercept) getParent().requestDisallowInterceptTouchEvent(true);
                if (mThumbA.mTouch) {
                    //注意左右方向
                    float len = (mPostionArr[2] - mPostionArr[0]);
                    float part = mThumbA.mFromLeft ? (x - mPostionArr[0]) : -(x - mPostionArr[2]);
                    mThumbA.progress = part / len;
                    if (mThumbA.progress <= 0) {
                        mThumbA.progress = 0;
                    }
                    if (mThumbA.progress >= 1) {
                        mThumbA.progress = 1;
                    }
                    if (mOnDoubleThumbChangeListener != null) {
                        mOnDoubleThumbChangeListener.onProgressChanged(this, mThumbA.progress, true, true);
                    }

                    //刷新
                    invalidate();

                } else if (mThumbB != null && mThumbB.mTouch) {
                    //注意左右方向
                    float len = (mPostionArr[2] - mPostionArr[0]);
                    float part = mThumbB.mFromLeft ? (x - mPostionArr[0]) : -(x - mPostionArr[2]);
                    mThumbB.progress = part / len;
                    if (mThumbB.progress <= 0) {
                        mThumbB.progress = 0;
                    }
                    if (mThumbB.progress >= 1) {
                        mThumbB.progress = 1;
                    }
                    if (mOnDoubleThumbChangeListener != null) {
                        mOnDoubleThumbChangeListener.onProgressChanged(this, mThumbB.progress, false, true);
                    }

                    //刷新
                    invalidate();
                } else {

                }
                break;
            case MotionEvent.ACTION_UP:
                if (mDisallowIntercept)
                    getParent().requestDisallowInterceptTouchEvent(false);
                mThumbA.mTouch = false;
                if (mThumbB != null) mThumbB.mTouch = false;
                break;
            default:
                break;
        }

        return true;
    }

    private void drawForegroundThumbB(Canvas canvas) {
        if (mDoubleThumb.mThumbB == null) return;

        DoubleTumb.Thumb mLeftThumb = mDoubleThumb.mThumbB;
        boolean mFromLeft = mLeftThumb.mFromLeft;
        double progress = mLeftThumb.progress;
        int[] ints = getmPostionArr();
        float l = mFromLeft ? ints[0] : ints[0] + (float) ((ints[2] - ints[0]) * (1 - progress));
        float t = ints[1];
        float r = mFromLeft ? ints[0] + (float) ((ints[2] - ints[0]) * progress) : ints[2];
        float b = ints[3];

        Log.d(TAG, "drawForegroundThumbA: " + progress);
        //绘制背景
        mForegroundRectFB.set(l, t, r, b);
        canvas.drawRoundRect(mForegroundRectFB, mDoubleThumb.mCorners, mDoubleThumb.mCorners, mForegroundPaintB);
    }

    private void drawThumbB(Canvas canvas) {
        if (mDoubleThumb.mThumbB == null) return;

        DoubleTumb.Thumb mThumbB = mDoubleThumb.mThumbB;
        boolean mFromLeft = mThumbB.mFromLeft;
        double progress = mThumbB.progress;
        int[] ints = getmPostionArr();
        float l = mFromLeft ? ints[0] : ints[0] + (float) ((ints[2] - ints[0]) * (1 - progress));
        float t = ints[1];
        float r = mFromLeft ? ints[0] + (float) ((ints[2] - ints[0]) * progress) : ints[2];
        float b = ints[3];

        //开始绘制thumb
        Bitmap thumbA = getThumbB();
        float[] tumbPosArr = mThumbB.getmTumbPosArr();
        tumbPosArr[0] = mFromLeft ? r - mThumbB.getmThumbWidth() / 2.0f : l - mThumbB.getmThumbWidth() / 2.0f;
        tumbPosArr[1] = t - dp2px(10);
        tumbPosArr[2] = tumbPosArr[0] + mThumbB.getmThumbWidth() / 2.0f;
        tumbPosArr[3] = ints[1] + mThumbB.getmThumbHight();
        canvas.drawBitmap(thumbA, tumbPosArr[0], tumbPosArr[1], mForegroundPaintB);


        //绘制tips
        if (mThumbB.mShowTips) {
            double len = mDoubleThumb.mMax - mDoubleThumb.mMin;

            /**
             * 从左边开始计算最小值还是最右边和前景图无关，由开发者手动设置
             */
            double realProcess = 0;
            if (mThumbB.mFromLeft) {
                if (mDoubleThumb.mMinLeft) {
                    realProcess = progress;
                } else {
                    realProcess = (1 - progress);
                }
            } else {
                if (mDoubleThumb.mMinLeft) {
                    realProcess = (1 - progress);
                } else {
                    realProcess = progress;
                }
            }

            double result = mDoubleThumb.mMin + len * realProcess;
            String format = "%." + mDoubleThumb.mDigit + "f";
            String showTipStr = String.format(format, result);
            float txtWidth = mTipPaintB.measureText(showTipStr);
            float x = (tumbPosArr[0] + tumbPosArr[2]) / 2.0f - txtWidth / 2.0f;
            float y = tumbPosArr[1] - getTipsTopOffset();

            //边界处理
            if (x < ints[0]) {
                x = ints[0];
            }
            if (x > ints[2] - txtWidth) {
                x = ints[2] - txtWidth;
            }

            canvas.drawText(showTipStr, x, y, mTipPaintB);
        }
    }

    /**
     * tips向上的偏移量
     *
     * @return
     */
    private int getTipsTopOffset() {
        return dp2px(6);
    }

    /**
     * 绘制thumbA前景图,左滑动和右滑动放在了一起，注意三元运算符，
     *
     * @param canvas
     */
    private void drawForegroundThumbA(Canvas canvas) {
        DoubleTumb.Thumb mLeftThumb = mDoubleThumb.mThumbA;
        boolean mFromLeft = mLeftThumb.mFromLeft;
        double progress = mLeftThumb.progress;
        int[] ints = getmPostionArr();
        float l = mFromLeft ? ints[0] : ints[0] + (float) ((ints[2] - ints[0]) * (1 - progress));
        float t = ints[1];
        float r = mFromLeft ? ints[0] + (float) ((ints[2] - ints[0]) * progress) : ints[2];
        float b = ints[3];

        Log.d(TAG, "drawForegroundThumbA: " + progress);
        //绘制背景
        mForegroundRectFA.set(l, t, r, b);
        canvas.drawRoundRect(mForegroundRectFA, mDoubleThumb.mCorners, mDoubleThumb.mCorners, mForegroundPaintA);
    }

    private void drawThumbA(Canvas canvas) {
        DoubleTumb.Thumb mThumbA = mDoubleThumb.mThumbA;
        boolean mFromLeft = mThumbA.mFromLeft;
        double progress = mThumbA.progress;
        //seekbar四个顶点
        int[] ints = getmPostionArr();
        float l = mFromLeft ? ints[0] : ints[0] + (float) ((ints[2] - ints[0]) * (1 - progress));
        float t = ints[1];
        float r = mFromLeft ? ints[0] + (float) ((ints[2] - ints[0]) * progress) : ints[2];
        float b = ints[3];

        //开始绘制thumb
        Bitmap thumbA = getThumbA();
        float[] tumbPosArr = mThumbA.getmTumbPosArr();
        tumbPosArr[0] = mFromLeft ? r - mThumbA.getmThumbWidth() / 2.0f : l - mThumbA.getmThumbWidth() / 2.0f;
        tumbPosArr[1] = t - dp2px(10);
        tumbPosArr[2] = tumbPosArr[0] + mThumbA.getmThumbWidth() / 2.0f;
        tumbPosArr[3] = ints[1] + mThumbA.getmThumbHight();
        canvas.drawBitmap(thumbA, tumbPosArr[0], tumbPosArr[1], mForegroundPaintA);

        //绘制tips
        if (mThumbA.mShowTips) {
            double len = mDoubleThumb.mMax - mDoubleThumb.mMin;

            /**
             * 从左边开始计算最小值还是最右边和前景图无关，由开发者手动设置
             */
            double realProcess = 0;
            if (mThumbA.mFromLeft) {
                if (mDoubleThumb.mMinLeft) {
                    realProcess = progress;
                } else {
                    realProcess = (1 - progress);
                }
            } else {
                if (mDoubleThumb.mMinLeft) {
                    realProcess = (1 - progress);
                } else {
                    realProcess = progress;
                }
            }

            double result = mDoubleThumb.mMin + len * realProcess;
            String format = "%." + mDoubleThumb.mDigit + "f";
            String showTipStr = String.format(format, result);
            float txtWidth = mTipPaintA.measureText(showTipStr);
            float x = (tumbPosArr[0] + tumbPosArr[2]) / 2.0f - txtWidth / 2.0f;
            float y = tumbPosArr[1] - getTipsTopOffset();

            //边界处理
            if (x < ints[0]) {
                x = ints[0];
            }
            if (x > ints[2] - txtWidth) {
                x = ints[2] - txtWidth;
            }

            canvas.drawText(showTipStr, x, y, mTipPaintA);
        }

    }

    private void drawBg(Canvas canvas) {
        int[] ints = getmPostionArr();
        mBackgroudRectF.set(ints[0], ints[1], ints[2], ints[3]);
        canvas.drawRoundRect(mBackgroudRectF, mDoubleThumb.mCorners, mDoubleThumb.mCorners, mBackgroudPaint);
    }

    /**
     * 获取进度条四个角的位置坐标
     *
     * @return
     */
    private int[] getmPostionArr() {
        if (mPostionArr == null) {
            mPostionArr = new int[4];
            mPostionArr[0] = 0 + mDoubleThumb.mLineLeft;
            mPostionArr[1] = 0 + mDoubleThumb.mLineTop;
            mPostionArr[2] = getWidth() - mDoubleThumb.mLineRight;
            mPostionArr[3] = getHeight() - mDoubleThumb.mLineBottom;
        }
        return mPostionArr;
    }

    public boolean isDisallowIntercept() {
        return mDisallowIntercept;
    }

    public DoubleThumbSeekBar setDisallowIntercept(boolean disallowIntercept) {
        mDisallowIntercept = disallowIntercept;
        return this;
    }

    /**
     * 获取thumbA
     *
     * @return
     */
    @NonNull
    private Bitmap getThumbA() {
        if (mThumbBitmapA == null) {
            DoubleTumb.Thumb mThumbA = mDoubleThumb.mThumbA;
            Drawable drawable = mThumbA.getmThumb();
            if (drawable == null) {
                throw new IllegalArgumentException("请提供mThumbA的Drawable");
            }
            mThumbBitmapA = RenderUtils.drawableToBitmap(drawable, mThumbA.getmThumbWidth(), mThumbA.getmThumbHight());
        }
        return mThumbBitmapA;
    }

    /**
     * 获取thumbB
     *
     * @return
     */
    @NonNull
    private Bitmap getThumbB() {
        if (mDoubleThumb.mThumbB == null) {
            throw new IllegalArgumentException("请先初始化ThumbB");
        }

        if (mThumbBitmapB == null) {
            Drawable drawable = null;
            DoubleTumb.Thumb mThumbB = mDoubleThumb.mThumbB;
            drawable = mThumbB.getmThumb();
            if (drawable == null) {
                throw new IllegalArgumentException("请提供mThumbB的Drawable");
            }
            mThumbBitmapB = RenderUtils.drawableToBitmap(drawable, mThumbB.getmThumbWidth(), mThumbB.getmThumbHight());
        }
        return mThumbBitmapB;
    }

    public interface OnDoubleThumbChangeListener {
        /**
         * 监听滑动
         *
         * @param seekBar  当前seekbar对象
         * @param progress 进度百分比，[0,1]
         * @param thumbA   是否是thumbA,true:thumbA,false:thumbB。同时只允许滑动一个进度thumb.
         * @param fromUser 是否是用户自己手动触发的
         */
        void onProgressChanged(DoubleThumbSeekBar seekBar, double progress, boolean thumbA, boolean fromUser);
    }
}
