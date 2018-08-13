package com.tophold.trade.view.kview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import com.tophold.trade.Constant;
import com.tophold.trade.R;
import com.tophold.trade.utils.FormatUtil;
import com.tophold.trade.utils.ScreenUtils;
import com.tophold.trade.utils.TimeUtils;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 更细日期 ：2018/01/14 12:03
 * 描 述 ：主图。该View可以实现绘制分时图和蜡烛图。通过void setViewType(KViewType.MasterViewType viewType)方法控制，该方法暴漏给使用者。
 * 为了模拟真实环境，拿到的数据没有直接使用，而是做了适配转换处理。没有使用任何第三方框架，
 * rx的使用仅仅在模拟网络环境获取数据的时候进行了线程的切换处理，控件中并没有使用。
 * <p>
 * 现功能如下：
 * 基础部分：绘制必要的各种背景、实时价横线显示、实时价更新、
 * 长按显示当前（距离按下点最近的有效点）价，并回调有效点、支持拖拽、支持缩放、支持加载更多、
 * 长按下不可滑动、滑动覆盖右侧内边距。
 * 分时图：加载分时折线图。
 * 蜡烛图：加载蜡烛图、主图指标：MA、BOLL、MA/BOLL。
 * <p>
 * 待完成：
 * 1. 代码结构命名优化
 * 2. 以动画形式加载分时折线图（入场时的动画，可以参考mpchart的效果）
 * 3. 拖拽支持onFiling、
 * <p>
 * 疑问：
 * 1. 对于每次更新数据或者拖动必须要更新整个绘制过程，是否有更好的解决方案？
 * 2. 是否可以封装成灵活的组件供开发者使用？如何封装？（属性、功能、交互比较多）
 * <p>
 * ============================================================
 **/
public class MasterView extends KBaseView {

    /**
     * 各种画笔及其参数
     */

    //画笔:折线图
    Paint mBrokenLinePaint;
    float mBrokenLineWidth = 2;
    int mBrokenLineColor;
    //是否是虚线。可更改
    boolean mIsBrokenLineDashed = false;

    //画笔:折线图阴影,折线图阴影的处理方式采用一个画笔两个Path进行处理
    Paint mBrokenLineBgPaint;
    //折线下面的浅蓝色
    int mBrokenLineBgColor;
    //透明度，可更改
    int mBrokenLineBgAlpha = 40;

    //画笔:最后一个小圆点的半径。对于是否显示小圆点，根据PullType.PULL_RIGHT_STOP判断
    Paint mDotPaint;
    float mDotRadius = 6;
    int mDotColor;

    //画笔:实时横线，这里的处理思路：记录最后一个点的位置坐标即可，从该点开始画小圆点、横线以及右侧实时数据
    Paint mTimingLinePaint;
    float mTimingLineWidth = 2;
    int mTimingLineColor;
    //是否是虚线，可更改
    boolean mIsTimingLineDashed = true;

    //画笔:实时横线右侧的红色的框和实时数据
    Paint mTimingTxtBgPaint;//实时数据的背景
    Paint mTimingTxtPaint;//实时数据
    float mTimingTxtWidth = 18;
    int mTimingTxtColor;
    int mTimingTxtBgColor;

    //画笔:长按十字的上方的时间框、右侧的数据框
    Paint mLongPressTxtPaint;
    Paint mLongPressTxtBgPaint;
    int mLongPressTxtColor;
    float mLongPressTxtSize = 18;
    int mLongPressTxtBgColor;

    /**
     * 蜡烛图相关逻辑，思路：遍历找到可视范围内最大的high价格A（也就是可视范围内o、c、h、l四个值的最大值）和最小的low值B。
     * 这个时候要重新计算y轴的均分值、在model中的floatY也要重新计算。(因为最大最小值变了，之前计算用的是可视范围内的最大和最小close价格)。
     * <p>
     * x轴单元大小：View有效宽度C（除去左右间距）,可视范围内数据数D,x轴单元大小（单个蜡烛宽度）E=C/D。
     * y轴单元大小：View有效的高度F,y轴单元大小G=F/(A-B);
     * <p>
     * 单个蜡烛y轴的确认：蜡烛的y轴上下边的确认是根据open(H,开盘价,数据model中对应o)和close（I,收盘价，对应c）确认。
     * View下边距K,总高度L，单个蜡烛的开盘价对应的y轴坐标J=L-((H-B)*G+K);另一边亦然。
     * <p>
     * 颜色的确认：至于哪个在上哪个在下看大小。大的在上面，小的在下面。同时，收盘价大于开盘价，蜡烛图为红色。反之，亦然。
     */
    //画笔:
    Paint mCandlePaint;
    int mRedCandleColor;
    int mGreenCandleColor;
    //单个蜡烛最大值最小值对应的y轴方向的线宽度
    int mCanldeHighLowWidth = 1;
    //指标类型
    KViewType.MasterIndicatrixType mMasterType = KViewType.MasterIndicatrixType.NONE;

    //MA
    Paint mMa5Paint;
    int mMa5Color;
    Paint mMa10Paint;
    int mMa10Color;
    Paint mMa20Paint;
    int mMa20Color;


    //BOLL
    Paint mBollUpPaint;
    int mBollUpColor;
    Paint mBollMbPaint;
    int mBollMbColor;
    Paint mBollDnPaint;
    int mBollDnColor;

    //最大值和最小值
    Paint mMinMaxPaint;
    int mMinMaxColor;

    //view类型：是分时图还是蜡烛图
    protected KViewType.MasterViewType mViewType = KViewType.MasterViewType.TIMESHARING;

    /**
     * 绘制蜡烛图：y轴，可以根据可视范围内的最大high值（A）和最小low值（B）以及有效y轴高度（C）计算出
     * 单位高度mPerY(D),D=C/(A-B)。
     * x轴暂时直接先取mPerX作为宽度，不留间隙。
     * 那么，蜡烛图就可以根据当前位置的high、low两个值绘制最大最小值；
     * 然后根据open和close绘制蜡烛图的上起点和下结束点。
     * 至于颜色，当当前值为的Quote的close大于open,为红色；反之为绿色。
     */
    //根据可见范围内最大的high价格和最小的low价格计算的y单位长度。该参数父类已经定义。
    //protected float mPerY;
    /**
     * 蜡烛图：整个视图的最大值和最小值（y轴边界值），不管视图中是分时图、蜡烛图（以及蜡烛图中的指标）必须找到上下边界，
     * 然后根据该边界值会绘制，不然会出现绘制超过边界的情况。特别明显的一种情况：蜡烛图模式下，存在不存在BOLL线，
     * 蜡烛的高度显示是不一样的，因为一般情况下BOLL的上边界会比蜡烛图大。
     */
    protected double mCandleMinY;
    protected double mCandleMaxY;
    //蜡烛图间隙，大小以单个蜡烛图的宽度的比例算。可修改。
    protected float mCandleDiverWidthRatio = 0.1f;

    //持有副图长按，方便监听
    private KViewListener.MinorListener mMinorListener;
    //持有量图长按，方便监听
    private KViewListener.MinorListener mVolListener;

    //最大值监听
    private KViewListener.PostionListner mMaxPostionListener;
    //最小值监听
    private KViewListener.PostionListner mMinPostionListener;
    //最后一个数据的位置监听
    private KViewListener.PostionListner mLastPostionListener;

    public void setMinorListener(KViewListener.MinorListener minorListener) {
        mMinorListener = minorListener;
    }

    public KViewListener.MinorListener getVolListener() {
        return mVolListener;
    }

    public MasterView setVolListener(KViewListener.MinorListener volListener) {
        mVolListener = volListener;
        return this;
    }

    public KViewListener.PostionListner getMaxPostionListener() {
        return mMaxPostionListener;
    }

    public MasterView setMaxPostionListener(KViewListener.PostionListner maxPostionListener) {
        mMaxPostionListener = maxPostionListener;
        return this;
    }

    public KViewListener.PostionListner getMinPostionListener() {
        return mMinPostionListener;
    }

    public MasterView setMinPostionListener(KViewListener.PostionListner minPostionListener) {
        mMinPostionListener = minPostionListener;
        return this;
    }

    public KViewListener.PostionListner getLastPostionListener() {
        return mLastPostionListener;
    }

    public MasterView setLastPostionListener(KViewListener.PostionListner lastPostionListener) {
        mLastPostionListener = lastPostionListener;
        return this;
    }

    public MasterView(Context context) {
        this(context, null);
    }

    public MasterView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MasterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs();
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
        if (mQuotesList == null || mQuotesList.isEmpty()) {
            return;
        }
        //绘制x周和y周的文字
        drawXyTxt(canvas);

        /**
         * 将需要遍历可视范围内的数据的操作全部在这里处理，尽可能少的遍历数据次数。
         * 现在处理的如下：分时图折现、蜡烛图、实时横线、长按十字。
         */
        drawLooper(canvas);

        //绘制主图上的指标
        drawMasterLegend(canvas);
        drawMasterIndicatrix(canvas);
    }

    protected void initAttrs() {
        //加载颜色和字符串资源
        loadDefAttrs();

        //初始化画笔
        initDotPaint();
        initTimingTxtPaint();
        initTimingLinePaint();
        initLongPressTxtPaint();
        initBrokenLinePaint();
        initBrokenLineBgPaint();
        initCandlePaint();
        initMaPaint();
        initBollPaint();
        initMinMaxPaint();

        //手势
        mScaleGestureDetector = new ScaleGestureDetector(mContext, mOnScaleGestureListener);

        mFlingDetector = new GestureDetectorCompat(mContext, mSimpleOnScaleGestureListener);
        mFlingDetector.setIsLongpressEnabled(false);


        //是分时图还是蜡烛图,def
        setViewType(KViewType.MasterViewType.TIMESHARING);
        //底部距离
        mBasePaddingBottom = 35;

        setMoveType(KViewType.MoveType.STEP);
    }

    private void initMinMaxPaint() {
        mMinMaxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMinMaxPaint.setColor(mMinMaxColor);
        mMinMaxPaint.setStyle(Paint.Style.FILL);
        mMinMaxPaint.setTextSize(16);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        if (mMoveType == KViewType.MoveType.ONFLING) {
            mFlingDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    protected void loadDefAttrs() {
        //数据源
        mQuotesList = new ArrayList<>(mShownMaxCount);
        //颜色
        mBrokenLineColor = getColor(R.color.color_timeSharing_brokenLineColor);
        mDotColor = getColor(R.color.color_timeSharing_dotColor);
        mTimingLineColor = getColor(R.color.color_timeSharing_timingLineColor);
        mBrokenLineBgColor = getColor(R.color.color_timeSharing_blowBlueColor);
        mTimingTxtColor = getColor(R.color.color_timeSharing_timingTxtColor);
        mTimingTxtBgColor = getColor(R.color.color_timeSharing_timingTxtBgColor);
        mLongPressTxtColor = getColor(R.color.color_timeSharing_longPressTxtColor);
        mLongPressTxtBgColor = getColor(R.color.color_timeSharing_longPressTxtBgColor);
        mRedCandleColor = getColor(R.color.color_timeSharing_candleRed);
        mGreenCandleColor = getColor(R.color.color_timeSharing_candleGreen);

        mMa5Color = getColor(R.color.color_masterView_ma5Color);
        mMa10Color = getColor(R.color.color_masterView_ma10Color);
        mMa20Color = getColor(R.color.color_masterView_ma20Color);

        mBollUpColor = getColor(R.color.color_masterView_bollUpColor);
        mBollMbColor = getColor(R.color.color_masterView_bollMbColor);
        mBollDnColor = getColor(R.color.color_masterView_bollDnColor);

        mMinMaxColor = getColor(R.color.color_minmax);
    }

    protected void initDotPaint() {
        mDotPaint = new Paint();
        mDotPaint.setColor(mDotColor);
        mDotPaint.setStyle(Paint.Style.FILL);
        mDotPaint.setAntiAlias(true);
    }

    protected void initTimingTxtPaint() {
        mTimingTxtBgPaint = new Paint();
        mTimingTxtBgPaint.setColor(mTimingTxtBgColor);
        mTimingTxtBgPaint.setAntiAlias(true);

        mTimingTxtPaint = new Paint();
        mTimingTxtPaint.setTextSize(mTimingTxtWidth);
        mTimingTxtPaint.setColor(mTimingTxtColor);
        mTimingTxtPaint.setAntiAlias(true);
    }

    protected void initTimingLinePaint() {
        mTimingLinePaint = new Paint();
        mTimingLinePaint.setColor(mTimingLineColor);
        mTimingLinePaint.setStrokeWidth(mTimingLineWidth);
        mTimingLinePaint.setStyle(Paint.Style.STROKE);
        mTimingLinePaint.setAntiAlias(true);
        if (mIsTimingLineDashed) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            mTimingLinePaint.setPathEffect(new DashPathEffect(new float[]{8, 8}, 0));
        }
    }

    protected void initLongPressTxtPaint() {
        mLongPressTxtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLongPressTxtPaint.setTextSize(mLongPressTxtSize);
        mLongPressTxtPaint.setColor(mLongPressTxtColor);

        mLongPressTxtBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLongPressTxtBgPaint.setColor(mLongPressTxtBgColor);
    }

    protected void initBrokenLinePaint() {
        mBrokenLinePaint = new Paint();
        mBrokenLinePaint.setColor(mBrokenLineColor);
        mBrokenLinePaint.setStrokeWidth(mBrokenLineWidth);
        mBrokenLinePaint.setStyle(Paint.Style.STROKE);
        mBrokenLinePaint.setAntiAlias(true);

        if (mIsBrokenLineDashed) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            mBrokenLinePaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));
        }
    }

    protected void initBrokenLineBgPaint() {
        mBrokenLineBgPaint = new Paint();
        mBrokenLineBgPaint.setColor(mBrokenLineBgColor);
        mBrokenLineBgPaint.setStyle(Paint.Style.FILL);
        mBrokenLineBgPaint.setAntiAlias(true);
        mBrokenLineBgPaint.setAlpha(mBrokenLineBgAlpha);
    }

    private void initCandlePaint() {
        mCandlePaint = new Paint();
        mCandlePaint.setColor(mRedCandleColor);
        mCandlePaint.setStyle(Paint.Style.FILL);
        mCandlePaint.setAntiAlias(true);
        mCandlePaint.setStrokeWidth(mCanldeHighLowWidth);
    }

    private void initMaPaint() {
        mMa5Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMa5Paint.setColor(mMa5Color);
        mMa5Paint.setStyle(Paint.Style.STROKE);
        mMa5Paint.setStrokeWidth(mLineWidth);
        mMa5Paint.setTextSize(mLegendTxtSize);

        mMa10Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMa10Paint.setColor(mMa10Color);
        mMa10Paint.setStyle(Paint.Style.STROKE);
        mMa10Paint.setStrokeWidth(mLineWidth);
        mMa10Paint.setTextSize(mLegendTxtSize);

        mMa20Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMa20Paint.setColor(mMa20Color);
        mMa20Paint.setStyle(Paint.Style.STROKE);
        mMa20Paint.setStrokeWidth(mLineWidth);
        mMa20Paint.setTextSize(mLegendTxtSize);
    }

    private void initBollPaint() {
        mBollMbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBollMbPaint.setColor(mBollMbColor);
        mBollMbPaint.setStyle(Paint.Style.STROKE);
        mBollMbPaint.setStrokeWidth(mLineWidth);
        mBollMbPaint.setTextSize(mLegendTxtSize);


        mBollUpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBollUpPaint.setColor(mBollUpColor);
        mBollUpPaint.setStyle(Paint.Style.STROKE);
        mBollUpPaint.setStrokeWidth(mLineWidth);
        mBollUpPaint.setTextSize(mLegendTxtSize);


        mBollDnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBollDnPaint.setColor(mBollDnColor);
        mBollDnPaint.setStyle(Paint.Style.STROKE);
        mBollDnPaint.setStrokeWidth(mLineWidth);
        mBollDnPaint.setTextSize(mLegendTxtSize);

    }

    protected void drawXyTxt(Canvas canvas) {
        //先处理y轴方向文字
        drawYPaint(canvas);

        //处理x轴方向文字
        drawXPaint(canvas);
    }

    private void drawLooper(Canvas canvas) {
        Quotes firstQ = mQuotesList.get(mBeginIndex);


        /**分时图折现的绘制*/
        Path brokenLinePath = new Path();
        Path brokenLineBgPath = new Path();

        /**实时横线的绘制*/
        drawTimingLineProcess(canvas, mQuotesList.get(mQuotesList.size() - 1));

        /**蜡烛图的绘制*/
        //蜡烛图单个之间的间隙
        float diverWidth = mCandleDiverWidthRatio * mPerX;

        /**长按的绘制*/
        //最后的最近的按下的位置
        int finalIndex = mBeginIndex;
        //获取距离最近按下的位置的model
        float pressX = mMovingX;
        //循环遍历，找到距离最短的x轴的mode
        Quotes finalFundMode = firstQ;
        //遍历的点距离按下的距离
        float minXLen = Integer.MAX_VALUE;

        //寻找可视范围内的最大值和最小值
        double maxClose = Integer.MIN_VALUE;
        double minClose = Integer.MAX_VALUE;
        int maxIndex = -1;
        int minIndex = -1;
        for (int i = mBeginIndex; i < mEndIndex; i++) {
            Quotes quotes = mQuotesList.get(i);
            //mPerX/2.0f：为了让取点为单个单元的x的中间位置
            float floatX = getFloatX(i);
            float floatY = getFloatY((float) quotes.c);


            //记录下位置信息
            quotes.floatX = floatX;
            quotes.floatY = floatY;


            /**确认最大值和最小值*/
            if (quotes.h > maxClose) {
                maxClose = quotes.h;
                maxIndex = i;
            }
            if (quotes.l < minClose) {
                minClose = quotes.l;
                minIndex = i;
            }


            /**分时图折现的绘制*/
            drawTimSharingProcess(quotes, i, brokenLinePath, brokenLineBgPath);

            /**蜡烛图的绘制*/
            drawCandleViewProcess(canvas, diverWidth, i, quotes);

            /**长按的绘制*/
            if (mDrawLongPress) {
                float abs = Math.abs(pressX - floatX);
                if (abs < minXLen) {
                    finalFundMode = quotes;
                    minXLen = abs;
                    finalIndex = i;
                }
            }

        }
        /**分时图折现的绘制*/
        drawTimSharing(canvas, brokenLinePath, brokenLineBgPath);

        /**长按的绘制*/
        drawLongPress(canvas, finalIndex, finalFundMode);

        /**回调最小值和最大值。注意：在分时图上不显示（为什么？）。*/
        if (maxIndex != -1 && mViewType == KViewType.MasterViewType.CANDLE) {
            Quotes quotes = mQuotesList.get(maxIndex);
            float x = getFloatX(maxIndex);
            float y = getFloatY((float) quotes.h);
            if (mMaxPostionListener != null) mMaxPostionListener.postion(quotes, x, y);


            y -= ScreenUtils.dip2px(10);
            float xHeight = ScreenUtils.dip2px(15);
            float posHalfX = getBaseWidth() / 2.0f;
            String txt = FormatUtil.numFormat(quotes.h, mDigits);
            float stopX = x > posHalfX ? x - xHeight : x + xHeight;
            canvas.drawLine(x, y, stopX, y, mMinMaxPaint);
            x = stopX;
            if (x > posHalfX) {
                x -= mMinMaxPaint.measureText(txt);
            }
            y += getFontHeight(mMinMaxPaint.getTextSize(), mMinMaxPaint) / 4.0f;
            canvas.drawText(txt, x, y, mMinMaxPaint);
        }
        if (minIndex != -1 && mViewType == KViewType.MasterViewType.CANDLE) {
            Quotes quotes = mQuotesList.get(minIndex);
            float x = getFloatX(minIndex);
            float y = getFloatY((float) quotes.l);
            if (mMinPostionListener != null) mMinPostionListener.postion(quotes, x, y);

            y += ScreenUtils.dip2px(10);
            float xHeight = ScreenUtils.dip2px(15);
            float posHalfX = getBaseWidth() / 2.0f;
            String txt = FormatUtil.numFormat(quotes.l, mDigits);
            float stopX = x > posHalfX ? x - xHeight : x + xHeight;
            canvas.drawLine(x, y, stopX, y, mMinMaxPaint);
            x = stopX;
            if (x > posHalfX) {
                x -= mMinMaxPaint.measureText(txt);
            }
            y += getFontHeight(mMinMaxPaint.getTextSize(), mMinMaxPaint) / 4.0f;
            canvas.drawText(txt, x, y, mMinMaxPaint);
        }

    }

    private float getFloatY(float price) {
        return (float) (mBaseHeight - mBasePaddingBottom - mInnerBottomBlankPadding -
                mPerY * (price - mCandleMinY));
    }

    private float getFloatX(int i) {
        return mBasePaddingLeft + mPerX / 2.0f + mPerX * (i - mBeginIndex);
    }

    private void drawMasterLegend(Canvas canvas) {
        if (mViewType != KViewType.MasterViewType.CANDLE) return;

        //长按
        if (mDrawLongPress) {
            if (mCurrLongPressQuotes == null) return;
            mMa5Paint.setStyle(Paint.Style.FILL);
            mMa10Paint.setStyle(Paint.Style.FILL);
            mMa20Paint.setStyle(Paint.Style.FILL);
            mBollDnPaint.setStyle(Paint.Style.FILL);
            mBollUpPaint.setStyle(Paint.Style.FILL);
            mBollMbPaint.setStyle(Paint.Style.FILL);
            String showTxt = "";
            if (mMasterType == KViewType.MasterIndicatrixType.MA) {
                showTxt = "• MA5 " + FormatUtil.formatBySubString(mCurrLongPressQuotes.ma5, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mBasePaddingLeft),
                        (float) (mLegendPaddingTop + mBasePaddingTop), mMa5Paint);

                float leftWidth = mMa5Paint.measureText(showTxt);
                showTxt = "• MA10 " + FormatUtil.formatBySubString(mCurrLongPressQuotes.ma10, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mBasePaddingLeft + leftWidth),
                        (float) (mLegendPaddingTop + mBasePaddingTop), mMa10Paint);

                float leftWidth2 = mMa10Paint.measureText(showTxt);
                showTxt = "• MA20 " + FormatUtil.formatBySubString(mCurrLongPressQuotes.ma20, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mBasePaddingLeft + leftWidth + leftWidth2),
                        (float) (mLegendPaddingTop + mBasePaddingTop), mMa20Paint);
            } else if (mMasterType == KViewType.MasterIndicatrixType.BOLL) {
                showTxt = "• UPPER " + FormatUtil.formatBySubString(mCurrLongPressQuotes.up, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mBasePaddingLeft),
                        (float) (mLegendPaddingTop + mBasePaddingTop), mBollMbPaint);

                float leftWidth = mBollMbPaint.measureText(showTxt);
                showTxt = "• MID " + FormatUtil.formatBySubString(mCurrLongPressQuotes.mb, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mBasePaddingLeft + leftWidth),
                        (float) (mLegendPaddingTop + mBasePaddingTop), mBollUpPaint);

                float leftWidth2 = mBollUpPaint.measureText(showTxt);
                showTxt = "• LOWER " + FormatUtil.formatBySubString(mCurrLongPressQuotes.dn, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mBasePaddingLeft + leftWidth + leftWidth2),
                        (float) (mLegendPaddingTop + mBasePaddingTop), mBollDnPaint);

            } else if (mMasterType == KViewType.MasterIndicatrixType.MA_BOLL) {
                showTxt = "• MA5 " + FormatUtil.formatBySubString(mCurrLongPressQuotes.ma5, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mBasePaddingLeft),
                        (float) (mLegendPaddingTop + mBasePaddingTop), mMa5Paint);

                float leftWidth = mMa5Paint.measureText(showTxt);
                showTxt = "• MA10 " + FormatUtil.formatBySubString(mCurrLongPressQuotes.ma10, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mBasePaddingLeft + leftWidth),
                        (float) (mLegendPaddingTop + mBasePaddingTop), mMa10Paint);

                float leftWidth2 = mMa10Paint.measureText(showTxt);
                showTxt = "• MA20 " + FormatUtil.formatBySubString(mCurrLongPressQuotes.ma20, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mBasePaddingLeft + leftWidth + leftWidth2),
                        (float) (mLegendPaddingTop + mBasePaddingTop), mMa20Paint);


                double high = getFontHeight(mLegendTxtSize, mMa5Paint);
                high += mLegendTxtTopPadding;
                showTxt = "• UPPER " + FormatUtil.formatBySubString(mCurrLongPressQuotes.mb, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mBasePaddingLeft),
                        (float) (mLegendPaddingTop + mBasePaddingTop + high), mBollMbPaint);

                float leftWidth3 = mBollMbPaint.measureText(showTxt);
                showTxt = "• MID " + FormatUtil.formatBySubString(mCurrLongPressQuotes.up, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mBasePaddingLeft + leftWidth3),
                        (float) (mLegendPaddingTop + mBasePaddingTop + high), mBollUpPaint);

                float leftWidth23 = mBollUpPaint.measureText(showTxt);
                showTxt = "• LOWER " + FormatUtil.formatBySubString(mCurrLongPressQuotes.dn, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mBasePaddingLeft + leftWidth23 + leftWidth3),
                        (float) (mLegendPaddingTop + mBasePaddingTop + high), mBollDnPaint);

            }
        } else {
            //非长按
            String showTxt = "";
            if (mMasterType == KViewType.MasterIndicatrixType.MA) {
                showTxt = "MA(5,10,20)";
            } else if (mMasterType == KViewType.MasterIndicatrixType.BOLL) {
                showTxt = "BOLL(26)";

            } else if (mMasterType == KViewType.MasterIndicatrixType.MA_BOLL) {
                showTxt = "BOLL(26)&MA(5,10,20)";
            }
            canvas.drawText(showTxt,
                    (float) (mBaseWidth - mLegendPaddingRight - mBasePaddingRight - mLegendPaint.measureText(showTxt)),
                    (float) (mLegendPaddingTop + mBasePaddingTop), mLegendPaint);
        }
    }

    private void drawMasterIndicatrix(Canvas canvas) {
        //指标展示的前提是蜡烛图
        if (mViewType != KViewType.MasterViewType.CANDLE) return;

        if (mMasterType == KViewType.MasterIndicatrixType.NONE) {

        } else if (mMasterType == KViewType.MasterIndicatrixType.MA) {
            drawMa(canvas);
        } else if (mMasterType == KViewType.MasterIndicatrixType.BOLL) {
            drawBoll(canvas);
        } else if (mMasterType == KViewType.MasterIndicatrixType.MA_BOLL) {
            drawMa(canvas);
            drawBoll(canvas);
        }

    }

    private void drawMa(Canvas canvas) {
        mMa5Paint.setStyle(Paint.Style.STROKE);
        mMa10Paint.setStyle(Paint.Style.STROKE);
        mMa20Paint.setStyle(Paint.Style.STROKE);

        Path path5 = new Path();
        Path path10 = new Path();
        Path path20 = new Path();
        boolean isFirstMa5 = true;
        boolean isFirstMa10 = true;
        boolean isFirstMa20 = true;
        for (int i = mBeginIndex; i < mEndIndex; i++) {
            Quotes quotes = mQuotesList.get(i);
            //在绘制蜡烛图的时候已经计算了
            float floatX = quotes.floatX;

            float floatY = getMasterDetailFloatY(quotes, KViewType.MasterIndicatrixDetailType.MA5);

            //异常,在View的行为就是不显示而已，影响不大。一般都是数据的开头部分。
            if (floatY == -1) continue;

            if (isFirstMa5) {
                isFirstMa5 = false;
                path5.moveTo(floatX, floatY);
            } else {
                path5.lineTo(floatX, floatY);
            }

            floatY = getMasterDetailFloatY(quotes, KViewType.MasterIndicatrixDetailType.MA10);

            //异常
            if (floatY == -1) continue;

            if (isFirstMa10) {
                isFirstMa10 = false;
                path10.moveTo(floatX, floatY);
            } else {
                path10.lineTo(floatX, floatY);
            }

            floatY = getMasterDetailFloatY(quotes, KViewType.MasterIndicatrixDetailType.MA20);

            //异常
            if (floatY == -1) continue;

            if (isFirstMa20) {
                isFirstMa20 = false;
                path20.moveTo(floatX, floatY);
            } else {
                path20.lineTo(floatX, floatY);
            }
        }

        canvas.drawPath(path5, mMa5Paint);
        canvas.drawPath(path10, mMa10Paint);
        canvas.drawPath(path20, mMa20Paint);
    }

    private float getMasterDetailFloatY(Quotes quotes, KViewType.MasterIndicatrixDetailType maType) {
        double v = 0;
        //ma
        if (maType == KViewType.MasterIndicatrixDetailType.MA5) {
            v = quotes.ma5 - mCandleMinY;
        } else if (maType == KViewType.MasterIndicatrixDetailType.MA10) {
            v = quotes.ma10 - mCandleMinY;
        } else if (maType == KViewType.MasterIndicatrixDetailType.MA20) {
            v = quotes.ma20 - mCandleMinY;
        }
        //boll
        else if (maType == KViewType.MasterIndicatrixDetailType.BOLLUP) {
            v = quotes.up - mCandleMinY;
        } else if (maType == KViewType.MasterIndicatrixDetailType.BOLLMB) {
            v = quotes.mb - mCandleMinY;
        } else if (maType == KViewType.MasterIndicatrixDetailType.BOLLDN) {
            v = quotes.dn - mCandleMinY;
        }
        //异常，当不存在ma值时的处理.也就是up、mb、dn为0时，这样判断其实有问题，比如算出来的值就是0？？？
        if (v + mCandleMinY == 0) return -1;

        double h = v * mPerY;
        float y = (float) (mBaseHeight - h - mBasePaddingBottom - mInnerBottomBlankPadding);

        //这里的y,存在一种情况，y超过了View的上边界或者超过了下边界，当出现这一种情况时，不显示，当作异常情况
        if (y < mBasePaddingTop || y > mBaseHeight - mBasePaddingBottom)
            return -1;

        return y;
    }

    private void drawBoll(Canvas canvas) {
        mBollDnPaint.setStyle(Paint.Style.STROKE);
        mBollUpPaint.setStyle(Paint.Style.STROKE);
        mBollMbPaint.setStyle(Paint.Style.STROKE);

        Path mbPath = new Path();
        Path upPath = new Path();
        Path dnPath = new Path();
        boolean isFirstMB = true;
        boolean isFirstUP = true;
        boolean isFirstDN = true;
        for (int i = mBeginIndex; i < mEndIndex; i++) {
            Quotes quotes = mQuotesList.get(i);
            float floatX = quotes.floatX;//在绘制蜡烛图的时候已经计算了

            //上轨线
            float floatY = getMasterDetailFloatY(quotes, KViewType.MasterIndicatrixDetailType.BOLLUP);
            //异常
            if (floatY == -1) continue;

            if (isFirstUP) {
                isFirstUP = false;
                upPath.moveTo(floatX, floatY);
            } else {
                upPath.lineTo(floatX, floatY);
            }

            //中轨线
            floatY = getMasterDetailFloatY(quotes, KViewType.MasterIndicatrixDetailType.BOLLMB);
            //异常,在View的行为就是不显示而已，影响不大。一般都是数据的开头部分。
            if (floatY == -1) continue;

            if (isFirstMB) {
                isFirstMB = false;
                mbPath.moveTo(floatX, floatY);
            } else {
                mbPath.lineTo(floatX, floatY);
            }


            //下轨线
            floatY = getMasterDetailFloatY(quotes, KViewType.MasterIndicatrixDetailType.BOLLDN);
            //异常
            if (floatY == -1) continue;

            if (isFirstDN) {
                isFirstDN = false;
                dnPath.moveTo(floatX, floatY);
            } else {
                dnPath.lineTo(floatX, floatY);
            }
        }

        canvas.drawPath(upPath, mBollUpPaint);
        canvas.drawPath(mbPath, mBollMbPaint);
        canvas.drawPath(dnPath, mBollDnPaint);
    }

    /**
     * 绘制蜡烛图
     *
     * @param canvas
     * @param diverWidth 蜡烛图之间的间距
     * @param i          List index
     * @param quotes     目标Quotes
     */
    private void drawCandleViewProcess(Canvas canvas, float diverWidth, int i, Quotes quotes) {
        if (mViewType != KViewType.MasterViewType.CANDLE) return;

        float topRectY;
        float bottomRectY;
        float leftRectX;
        float rightRectX;
        float leftLineX;
        float topLineY;
        float rightLineX;
        float bottomLineY;
        //定位蜡烛矩形的四个点
        topRectY = (float) (mBasePaddingTop + mInnerTopBlankPadding +
                mPerY * (mCandleMaxY - quotes.o));
        bottomRectY = (float) (mBasePaddingTop + mInnerTopBlankPadding +
                mPerY * (mCandleMaxY - quotes.c));
        leftRectX = -mPerX / 2 + quotes.floatX + diverWidth / 2;
        rightRectX = mPerX / 2 + quotes.floatX - diverWidth / 2;

        //定位单个蜡烛中间线的四个点
        leftLineX = quotes.floatX;
        topLineY = (float) (mBasePaddingTop + mInnerTopBlankPadding +
                mPerY * (mCandleMaxY - quotes.h));
        rightLineX = quotes.floatX;
        bottomLineY = (float) (mBasePaddingTop + mInnerTopBlankPadding +
                mPerY * (mCandleMaxY - quotes.l));

        RectF rectF = new RectF();

        //上下边界一样，设置一个偏移值
        if (topRectY == bottomRectY) bottomRectY += 1;

        rectF.set(leftRectX, topRectY, rightRectX, bottomRectY);
        //设置颜色
        mCandlePaint.setColor(quotes.c > quotes.o ? mRedCandleColor : mGreenCandleColor);
        canvas.drawRect(rectF, mCandlePaint);

        //开始画low、high线
        canvas.drawLine(leftLineX, topLineY, rightLineX, bottomLineY, mCandlePaint);
    }

    private void drawLongPress(Canvas canvas, int finalIndex, Quotes finalFundMode) {
        if (!mDrawLongPress) return;

        //x轴线
        canvas.drawLine(mBasePaddingLeft, finalFundMode.floatY, mBaseWidth - mBasePaddingRight,
                finalFundMode.floatY, mLongPressPaint);
        //y轴线
        canvas.drawLine(finalFundMode.floatX, mBasePaddingTop, finalFundMode.floatX,
                mBaseHeight - mBasePaddingBottom, mLongPressPaint);

        //将长按信息回调给副图，方便副图处理长按信息
        if (mMinorListener != null)
            mMinorListener.masterLongPressListener(finalIndex, finalFundMode);

        if (mVolListener != null)
            mVolListener.masterLongPressListener(finalIndex, finalFundMode);


        //接着绘制长按上方和右侧文字信息背景
        float txtBgHight = getFontHeight(mLongPressTxtSize, mLongPressTxtBgPaint);
        //y
        float longPressTxtYPadding = 10;
        canvas.drawRect(finalFundMode.floatX -
                        mLongPressTxtBgPaint.measureText(finalFundMode.getShowTime()) / 2 -
                        longPressTxtYPadding,
                mBasePaddingTop,
                finalFundMode.floatX +
                        mLongPressTxtBgPaint.measureText(finalFundMode.getShowTime()) / 2 +
                        longPressTxtYPadding,
                mBasePaddingTop + txtBgHight, mLongPressTxtBgPaint);
        //x
        //加一个左右边距
        canvas.drawRect(mBaseWidth - mBasePaddingRight, finalFundMode.floatY - txtBgHight / 2,
                mBaseWidth,
                finalFundMode.floatY + txtBgHight / 2,
                mLongPressTxtBgPaint);

        //绘制长按上方和右侧文字信息
        float txtHight = getFontHeight(mLongPressTxtSize, mLongPressTxtPaint);
        //x
        //距离左边的距离
        float leftDis = 8;
        canvas.drawText(FormatUtil.numFormat(finalFundMode.c, mDigits),
                mBaseWidth - mBasePaddingRight + leftDis,
                finalFundMode.floatY + txtHight / 4//这特么的又是需要+/4,理论应该是-/2,原因不明
                , mLongPressTxtPaint);
        //y
        canvas.drawText(finalFundMode.getShowTime(),
                finalFundMode.floatX - mLongPressTxtPaint.measureText(finalFundMode.getShowTime()) / 2,
                mBasePaddingTop + txtHight - 6
                , mLongPressTxtPaint);

        //在这里回调数据信息
        int size = mQuotesList.size();
        if ((0 <= finalIndex && finalIndex < size) &&
                (0 <= finalIndex - 1 && finalIndex - 1 < size))
            //记录当前Quotes
            mCurrLongPressQuotes = mQuotesList.get(finalIndex);
        if (mMasterTouchListener != null) {
            //回调,需要两个数据，便于计算涨跌百分比
            mMasterTouchListener.onLongTouch(mQuotesList.get(finalIndex - 1),
                    mQuotesList.get(finalIndex));
        }
    }

    private void drawTimSharing(Canvas canvas, Path brokenLinePath, Path brokenLineBgPath) {
        if (mViewType != KViewType.MasterViewType.TIMESHARING) return;

        canvas.drawPath(brokenLinePath, mBrokenLinePaint);
        canvas.drawPath(brokenLineBgPath, mBrokenLineBgPaint);
    }

    private void drawTimingLineProcess(Canvas canvas, Quotes quotes) {
        //这里滑动到最右端
        //绘制小圆点
        if (mPullType == KViewType.PullType.PULL_RIGHT_STOP) {
            //这里记录最后一个点的位置
            float floatX = mBaseWidth - mInnerRightBlankPadding - mBasePaddingRight - mPerX / 2.0f;
            float floatY = getFloatY((float) quotes.c);
            quotes.floatX = floatX;
            quotes.floatY = floatY;

            //对于蜡烛图不需要绘制小圆点，但是需要绘制实时横线
            if (mViewType == KViewType.MasterViewType.TIMESHARING) {
                canvas.drawCircle(quotes.floatX, quotes.floatY, mDotRadius, mDotPaint);
            }
        } else {
            //这里隐藏小圆点并且重新计算Y值。这里这样处理，对应现象的问题：横线划出界面。
            Quotes endQuotes = mQuotesList.get(mQuotesList.size() - 1);

            //蜡烛图
            quotes.floatY = getFloatY((float) endQuotes.c);
        }

        /**
         * 回调后注意处理边界问题
         */
        if (mLastPostionListener != null)
            mLastPostionListener.postion(quotes, quotes.floatX, quotes.floatY);

        //实时数据展示的前提是在指定范围内。不处理对应的异常：实时横线显示在底部横线的下面...
        if (mBasePaddingTop < quotes.floatY && quotes.floatY < mBaseHeight - mBasePaddingBottom) {
            //接着画实时横线
            canvas.drawLine(mBasePaddingLeft, quotes.floatY, mBaseWidth - mBasePaddingRight, quotes.floatY,
                    mTimingLinePaint);

            //接着绘制实时横线的右侧数据与背景
            //文字高度
            float txtHight = getFontHeight(mTimingTxtWidth, mTimingTxtBgPaint);
            //绘制背景
            canvas.drawRect(mBaseWidth - mBasePaddingRight, quotes.floatY - txtHight / 2, mBaseWidth,
                    quotes.floatY + txtHight / 2, mTimingTxtBgPaint);

            //绘制实时数据
            //距离左边的距离
            float leftDis = 8;
            canvas.drawText(FormatUtil.numFormat(quotes.c, mDigits),
                    mBaseWidth - mBasePaddingRight + leftDis, quotes.floatY + txtHight / 4,
                    mTimingTxtPaint);
        }
    }

    private void drawTimSharingProcess(Quotes quotes, int i, Path path, Path path2) {
        if (mViewType != KViewType.MasterViewType.TIMESHARING) return;

        if (i == mBeginIndex) {
            path.moveTo((float) (quotes.floatX - mPerX / 2.0), quotes.floatY);
            path2.moveTo((float) (quotes.floatX - mPerX / 2.0), quotes.floatY);
        } else if (i == mEndIndex - 1) {
            //在这里把path圈起来，添加阴影。特别注意，这里处理下方阴影和折线边框。采用两个画笔和两个Path处理的，
            // 貌似没有一个Paint可以同时绘制边框和填充色。
            float bootomY = mBaseHeight - mBasePaddingBottom;
            if (mPullType == KViewType.PullType.PULL_RIGHT_STOP) {
                path.lineTo(quotes.floatX, quotes.floatY);
                //开始绘制path
                path2.lineTo(quotes.floatX, quotes.floatY);
                path2.lineTo(quotes.floatX, bootomY);
                path2.lineTo(mBasePaddingLeft, bootomY);
            } else {
                float x = (float) (quotes.floatX + mPerX / 2.0);
                path.lineTo(x, quotes.floatY);
                //开始绘制path
                path2.lineTo(x, quotes.floatY);
                path2.lineTo(x, bootomY);
                path2.lineTo(mBasePaddingLeft, bootomY);
            }
            path2.close();
        } else {
            path.lineTo(quotes.floatX, quotes.floatY);
            //开始绘制path
            path2.lineTo(quotes.floatX, quotes.floatY);
        }
    }

    /**
     * 这里的均值如何处理：只知道最小值和最大值（mMinColseQuotes,mMaxCloseQuotes）,但是不是容器的上border和下border。
     * 这里的处理方式是根据mMinQuotes和mMaxQuotes的差值与y轴高度的距离计算单位距离对应的值。
     * 然后根据mMinQuotes的值(A)和底部内边距的（折线底部距离底边线的距离mInnerBottomBlankPadding）（B）
     * 和单位对应的值(D)就可以计算出最下面border对应的值：(A-B*D)。
     * 同理，可以计算最高对应的值。最后，均分标记即可。
     *
     * @param canvas
     */
    protected void drawYPaint(Canvas canvas) {
        //细节处理，文字高度居中
        float halfTxtHight;
        double minBorderData;
        double maxBorderData;
        double dataDis = 0;

        dataDis = mCandleMaxY - mCandleMinY;

        double yDis = (mBaseHeight - mBasePaddingTop - mBasePaddingBottom - mInnerTopBlankPadding -
                mInnerBottomBlankPadding);
        double perY = dataDis / yDis;


        minBorderData = mCandleMinY - mInnerBottomBlankPadding * perY;
        maxBorderData = mCandleMaxY + mInnerTopBlankPadding * perY;

        halfTxtHight = getFontHeight(mXYTxtSize, mXYTxtPaint) / 4;//应该/2的，但是不准确，原因不明

        //现将最小值、最大值画好
        float rightBorderPadding = mRightTxtPadding;
        canvas.drawText(FormatUtil.numFormat(minBorderData, mDigits),
                mBaseWidth - mBasePaddingRight + rightBorderPadding,
                mBaseHeight - mBasePaddingBottom + halfTxtHight, mXYTxtPaint);
        //draw max
        canvas.drawText(FormatUtil.numFormat(maxBorderData, mDigits),
                mBaseWidth - mBasePaddingRight + rightBorderPadding,
                mBasePaddingTop + halfTxtHight, mXYTxtPaint);
        //因为横线是均分的，所以只要取到最大值最小值的差值，均分即可。
        float perYValues = (float) ((maxBorderData - minBorderData) / 4);
        float perYWidth = (mBaseHeight - mBasePaddingBottom - mBasePaddingTop) / 4;
        //从下到上依次画
        for (int i = 1; i <= 3; i++) {
            canvas.drawText(FormatUtil.numFormat(minBorderData + perYValues * i, mDigits),
                    mBaseWidth - mBasePaddingRight + rightBorderPadding,
                    mBaseHeight - mBasePaddingBottom - perYWidth * i + halfTxtHight, mXYTxtPaint);
        }
    }

    public boolean isDrawLongPress() {
        return mDrawLongPress;
    }

    /**
     * 对于x轴文字的处理：其实位置已经知道，结束位置知道，x轴有效间距也知道，
     * 计算均值即可，处理思路和y轴文字基本一致。特别注意：右边有一段间距。
     * 这里测量直接使用最原始的时间（long类型更加方便）
     *
     * @param canvas
     */
    protected void drawXPaint(Canvas canvas) {
        //细节，让中间虚线对应的文字居中
        float halfTxtWidth = mXYTxtPaint.measureText("00:00") / 2;

        //单位间距，注意这里需要加上右边内边距
        double perXWith = (mBaseWidth - mBasePaddingLeft - mBasePaddingRight) / 4;
        double xDis = (mBaseWidth - mBasePaddingLeft - mBasePaddingRight - mInnerRightBlankPadding);
        long timeDis = mEndQuotes.t - mBeginQuotes.t;
        long perXTime = (long) (timeDis / xDis);
        String showTime;
        float finalHalfTxtWidth;
        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                finalHalfTxtWidth = 0;
                //不要忘了*perXWith
                showTime = TimeUtils.millis2String((long) (mBeginQuotes.t + perXWith * perXTime * i));
            } else {
                finalHalfTxtWidth = halfTxtWidth;
                showTime = TimeUtils.millis2String((long) (mBeginQuotes.t + perXWith * perXTime * i),
                        new SimpleDateFormat("HH:mm", Locale.getDefault()));
            }
            canvas.drawText(showTime,
                    (float) (mBasePaddingLeft + perXWith * i - finalHalfTxtWidth),
                    mBaseHeight - mBasePaddingBottom + mBottomTxtPadding, mXYTxtPaint);
        }
    }


    @Override
    protected void seekAndCalculateCellData() {
        if (mQuotesList == null || mQuotesList.isEmpty()) return;


        //找到close最大值和最小值
        mCandleMinY = Integer.MAX_VALUE;
        mCandleMaxY = Integer.MIN_VALUE;


        //对于蜡烛图，需要计算以下指标。
        if (mViewType == KViewType.MasterViewType.CANDLE) {
            //ma
            FinancialAlgorithm.calculateMA(mQuotesList, 5, KViewType.MaType.ma5);
            FinancialAlgorithm.calculateMA(mQuotesList, 10, KViewType.MaType.ma10);
            FinancialAlgorithm.calculateMA(mQuotesList, 20, KViewType.MaType.ma20);
            //boll
            FinancialAlgorithm.calculateBOLL(mQuotesList);

            //最终确定的最大high值和最小low值
            mCandleMaxY = mQuotesList.get(mBeginIndex).h;
            mCandleMinY = mQuotesList.get(mBeginIndex).l;
        }

        for (int i = mBeginIndex; i < mEndIndex; i++) {
            Quotes quotes = mQuotesList.get(i);
            if (i == mBeginIndex) {
                mBeginQuotes = quotes;
            }
            if (i == mEndIndex - 1) {
                mEndQuotes = quotes;
            }

            if (mViewType == KViewType.MasterViewType.CANDLE) {
                if (quotes.l <= mCandleMinY) {
                    mCandleMinY = quotes.l;
                }
                if (quotes.h >= mCandleMaxY) {
                    mCandleMaxY = quotes.h;
                }
            } else {
                if (quotes.c <= mCandleMinY) {
                    mCandleMinY = quotes.c;
                }
                if (quotes.c >= mCandleMaxY) {
                    mCandleMaxY = quotes.c;
                }
            }

            //蜡烛图
            if (mViewType == KViewType.MasterViewType.CANDLE) {
                double max = FinancialAlgorithm.getMasterMaxY(quotes, mMasterType);
                if (max > mCandleMaxY) {
                    mCandleMaxY = max;
                }
                double min = FinancialAlgorithm.getMasterMinY(quotes, mMasterType);
                if (min < mCandleMinY) {
                    mCandleMinY = min;
                }
            }
        }

        mPerX = (mBaseWidth - mBasePaddingLeft - mBasePaddingRight - mInnerRightBlankPadding)
                / (mShownMaxCount);
        //不要忘了减去内部的上下Padding
        mPerY = (float) ((mBaseHeight - mBasePaddingTop - mBasePaddingBottom - mInnerTopBlankPadding
                - mInnerBottomBlankPadding) / (mCandleMaxY - mCandleMinY));

        Log.d(Constant.ESPECIAL_TAG, "seekAndCalculateCellData,mPerY:" + mPerY);

        //重绘
        invalidate();
    }


    //缩放手势监听
    ScaleGestureDetector.OnScaleGestureListener mOnScaleGestureListener =
            new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    //没有缩放
                    if (detector.getScaleFactor() == 1) return true;

                    //是放大还是缩小
                    boolean isBigger = detector.getScaleFactor() > 1;

                    //变化的个数（缩小或者放大），必须向上取整，不然当mShownMaxCount过小时容易取到0。
                    int changeNum = (int) Math.ceil(mShownMaxCount * Math.abs(detector.getScaleFactor() - 1));

                    //一半
                    int helfChangeNum = (int) Math.ceil(changeNum / 2f);

                    //缩放个数太少，直接return
                    if (changeNum == 0 || helfChangeNum == 0) return true;

                    //容错处理,获取最大最小值
                    if (def_scale_minnum < 3) {
                        def_scale_minnum = 3;
                    }
                    if (def_scale_maxnum > mQuotesList.size()) {
                        def_scale_maxnum = mQuotesList.size();
                    }

                    //变大了(拉伸了)，数量变少了
                    int tempCount = isBigger ? mShownMaxCount - changeNum : mShownMaxCount + changeNum;

                    //缩小大到最小了或者放大到很大了
                    if (tempCount > def_scale_maxnum || tempCount < def_scale_minnum)
                        return true;

                    mShownMaxCount = tempCount;

                    //计算新的开始位置。这个地方比较难以理解:拉伸了起始点变大，并且是拉伸数量的一半，结束点变小，也是原来的一半。
                    // 收缩，相反。可以自己画一个图看看
                    mBeginIndex = isBigger ? mBeginIndex + helfChangeNum : mBeginIndex - helfChangeNum;
                    if (mBeginIndex < 0) {
                        mBeginIndex = 0;
                    } else if ((mBeginIndex + mShownMaxCount) > mQuotesList.size()) {
                        mBeginIndex = mQuotesList.size() - mShownMaxCount;
                    }

                    mEndIndex = mBeginIndex + mShownMaxCount;

                    //将新的索引回调给副图
                    if (mMinorListener != null)
                        mMinorListener.masteZoomlNewIndex(mBeginIndex, mEndIndex, mShownMaxCount);
                    if (mVolListener != null)
                        mVolListener.masteZoomlNewIndex(mBeginIndex, mEndIndex, mShownMaxCount);

                    //只要找好起始点和结束点就可以交给处理重绘的方法就好啦~
                    seekAndCalculateCellData();
                    return true;
                }

                @Override
                public boolean onScaleBegin(ScaleGestureDetector detector) {
                    //指头数量
                    if (mFingerPressedCount != 2) return true;
                    return true;
                }
            };

    /**
     * 滑动手势
     */
    GestureDetector.SimpleOnGestureListener mSimpleOnScaleGestureListener
            = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, final float distanceX, float distanceY) {
            Log.d(TAG, "onFling0: " + distanceX + "，" + distanceY);
            //降噪处理
            if (Math.abs(distanceX) > def_onfling) {
                //x轴幅度大于y轴
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    /**
                     * 为什么加延迟？这个取决于onFling的时机。如果不加的话，手指离开就立马结束了onFling效果。
                     * TODO:其实这样实现效果并不是太好，是否有其他更好的解决方案？
                     */
                    postDelayed(() -> innerMoveViewListener(-distanceX), 200);
                    return true;
                }
            }

            //y轴类似

            return true;
        }

        /**
         * 这个方法是最终滑动到的位置，onScroll()才是onFling的效果回调
         * @param e1
         * @param e2
         * @param distanceX
         * @param distanceY
         * @return
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return super.onFling(e1, e2, distanceX, distanceY);
        }
    };


    /**
     * 移动K线图计算移动的单位和重新计算起始位置和结束位置
     *
     * @param moveLen
     */
    protected void moveKView(float moveLen) {
        //移动之前将右侧的内间距值为0
        mInnerRightBlankPadding = 0;

        mPullRight = moveLen > 0;
        int moveCount = (int) Math.ceil(Math.abs(moveLen) / mPerX);
        //向右拉
        if (mPullRight) {
            int len = mBeginIndex - moveCount;
            if (len < def_minlen_loadmore) {
                //加载更多
                if (mMasterTouchListener != null && mCanLoadMore) {
                    loadMoreIng();
                    mMasterTouchListener.needLoadMore();
                }
            }
            if (len <= 0) {
                mBeginIndex = 0;
                mPullType = KViewType.PullType.PULL_LEFT_STOP;
            } else {
                mBeginIndex = len;
                mPullType = KViewType.PullType.PULL_LEFT;
            }

            mEndIndex = mBeginIndex + mShownMaxCount;
            mEndIndex = mEndIndex > mQuotesList.size() ? mQuotesList.size() : mEndIndex;
        } else {
            int len = mEndIndex + moveCount;
            if (len >= mQuotesList.size()) {
                mEndIndex = mQuotesList.size();
                mPullType = KViewType.PullType.PULL_RIGHT_STOP;
                //重置到之前的状态
                mInnerRightBlankPadding = DEF_INNER_RIGHT_BLANK_PADDING;
            } else {
                mEndIndex += moveCount;
                mPullType = KViewType.PullType.PULL_RIGHT;
            }
            mBeginIndex = mEndIndex - mShownMaxCount;
            mBeginIndex = mBeginIndex < 0 ? 0 : mBeginIndex;
        }

        //回调给副图
        if (mMinorListener != null)
            mMinorListener.mastelPullmNewIndex(mBeginIndex, mEndIndex, mPullType, mShownMaxCount);

        if (mVolListener != null)
            mVolListener.mastelPullmNewIndex(mBeginIndex, mEndIndex, mPullType, mShownMaxCount);

        //开始位置和结束位置确认好，就可以重绘啦~
        Log.d(TAG, "moveKView: mBeginIndex:" + mBeginIndex + ",mEndIndex:" + mEndIndex);
        seekAndCalculateCellData();
    }

    @Override
    protected void innerClickListener() {
        super.innerClickListener();
        if (mViewType == KViewType.MasterViewType.CANDLE) {
            if (mMasterType == KViewType.MasterIndicatrixType.NONE) {
                mMasterType = KViewType.MasterIndicatrixType.MA;
            } else if (mMasterType == KViewType.MasterIndicatrixType.MA) {
                mMasterType = KViewType.MasterIndicatrixType.BOLL;
            } else if (mMasterType == KViewType.MasterIndicatrixType.BOLL) {
                mMasterType = KViewType.MasterIndicatrixType.MA_BOLL;
            } else if (mMasterType == KViewType.MasterIndicatrixType.MA_BOLL) {
                mMasterType = KViewType.MasterIndicatrixType.NONE;
            }
            //刷新界面,重绘前需要么重新计算y的边界
            seekAndCalculateCellData();
        }
    }

    @Override
    protected void innerLongClickListener(float x, float y) {
        super.innerLongClickListener(x, y);
        mDrawLongPress = true;
        mMovingX = x;
        invalidate();
    }

    @Override
    protected void innerHiddenLongClick() {
        super.innerHiddenLongClick();
        mDrawLongPress = false;
        invalidate();
        if (mMasterTouchListener != null) mMasterTouchListener.onUnLongTouch();
        if (mMinorListener != null) mMinorListener.masterNoLongPressListener();
        if (mVolListener != null) mVolListener.masterNoLongPressListener();
    }

    @Override
    protected void innerMoveViewListener(float moveXLen) {
        super.innerMoveViewListener(moveXLen);
        moveKView(moveXLen);
    }


    //-----------------------对开发者暴露可以修改的参数-------

    public KViewType.MasterViewType getViewType() {
        return mViewType;
    }

    public void setViewType(KViewType.MasterViewType viewType) {
        if (viewType == mViewType) return;
        mViewType = viewType;

        //重绘
        seekAndCalculateCellData();
    }


    public Paint getBrokenLinePaint() {
        return mBrokenLinePaint;
    }

    public MasterView setBrokenLinePaint(Paint brokenLinePaint) {
        mBrokenLinePaint = brokenLinePaint;
        return this;
    }

    public float getBrokenLineWidth() {
        return mBrokenLineWidth;
    }

    public MasterView setBrokenLineWidth(float brokenLineWidth) {
        mBrokenLineWidth = brokenLineWidth;
        return this;
    }

    public int getBrokenLineColor() {
        return mBrokenLineColor;
    }

    public MasterView setBrokenLineColor(int brokenLineColor) {
        mBrokenLineColor = brokenLineColor;
        return this;
    }

    public boolean isBrokenLineDashed() {
        return mIsBrokenLineDashed;
    }

    public MasterView setBrokenLineDashed(boolean brokenLineDashed) {
        mIsBrokenLineDashed = brokenLineDashed;
        return this;
    }

    public Paint getBrokenLineBgPaint() {
        return mBrokenLineBgPaint;
    }

    public MasterView setBrokenLineBgPaint(Paint brokenLineBgPaint) {
        mBrokenLineBgPaint = brokenLineBgPaint;
        return this;
    }

    public int getBrokenLineBgColor() {
        return mBrokenLineBgColor;
    }

    public MasterView setBrokenLineBgColor(int brokenLineBgColor) {
        mBrokenLineBgColor = brokenLineBgColor;
        return this;
    }

    public Paint getDotPaint() {
        return mDotPaint;
    }

    public MasterView setDotPaint(Paint dotPaint) {
        mDotPaint = dotPaint;
        return this;
    }

    public float getDotRadius() {
        return mDotRadius;
    }

    public MasterView setDotRadius(float dotRadius) {
        mDotRadius = dotRadius;
        return this;
    }

    public int getDotColor() {
        return mDotColor;
    }

    public MasterView setDotColor(int dotColor) {
        mDotColor = dotColor;
        return this;
    }

    public Paint getTimingLinePaint() {
        return mTimingLinePaint;
    }

    public MasterView setTimingLinePaint(Paint timingLinePaint) {
        mTimingLinePaint = timingLinePaint;
        return this;
    }

    public float getTimingLineWidth() {
        return mTimingLineWidth;
    }

    public MasterView setTimingLineWidth(float timingLineWidth) {
        mTimingLineWidth = timingLineWidth;
        return this;
    }

    public int getTimingLineColor() {
        return mTimingLineColor;
    }

    public MasterView setTimingLineColor(int timingLineColor) {
        mTimingLineColor = timingLineColor;
        return this;
    }

    public boolean isTimingLineDashed() {
        return mIsTimingLineDashed;
    }

    public MasterView setTimingLineDashed(boolean timingLineDashed) {
        mIsTimingLineDashed = timingLineDashed;
        return this;
    }

    public Paint getTimingTxtBgPaint() {
        return mTimingTxtBgPaint;
    }

    public MasterView setTimingTxtBgPaint(Paint timingTxtBgPaint) {
        mTimingTxtBgPaint = timingTxtBgPaint;
        return this;
    }

    public Paint getTimingTxtPaint() {
        return mTimingTxtPaint;
    }

    public MasterView setTimingTxtPaint(Paint timingTxtPaint) {
        mTimingTxtPaint = timingTxtPaint;
        return this;
    }

    public float getTimingTxtWidth() {
        return mTimingTxtWidth;
    }

    public MasterView setTimingTxtWidth(float timingTxtWidth) {
        mTimingTxtWidth = timingTxtWidth;
        return this;
    }

    public int getTimingTxtColor() {
        return mTimingTxtColor;
    }

    public MasterView setTimingTxtColor(int timingTxtColor) {
        mTimingTxtColor = timingTxtColor;
        return this;
    }

    public int getTimingTxtBgColor() {
        return mTimingTxtBgColor;
    }

    public MasterView setTimingTxtBgColor(int timingTxtBgColor) {
        mTimingTxtBgColor = timingTxtBgColor;
        return this;
    }

    public Paint getLongPressTxtPaint() {
        return mLongPressTxtPaint;
    }

    public MasterView setLongPressTxtPaint(Paint longPressTxtPaint) {
        mLongPressTxtPaint = longPressTxtPaint;
        return this;
    }

    public Paint getLongPressTxtBgPaint() {
        return mLongPressTxtBgPaint;
    }

    public MasterView setLongPressTxtBgPaint(Paint longPressTxtBgPaint) {
        mLongPressTxtBgPaint = longPressTxtBgPaint;
        return this;
    }

    public int getLongPressTxtColor() {
        return mLongPressTxtColor;
    }

    public MasterView setLongPressTxtColor(int longPressTxtColor) {
        mLongPressTxtColor = longPressTxtColor;
        return this;
    }

    public float getLongPressTxtSize() {
        return mLongPressTxtSize;
    }

    public MasterView setLongPressTxtSize(float longPressTxtSize) {
        mLongPressTxtSize = longPressTxtSize;
        return this;
    }

    public int getLongPressTxtBgColor() {
        return mLongPressTxtBgColor;
    }

    public MasterView setLongPressTxtBgColor(int longPressTxtBgColor) {
        mLongPressTxtBgColor = longPressTxtBgColor;
        return this;
    }

    public Paint getCandlePaint() {
        return mCandlePaint;
    }

    public MasterView setCandlePaint(Paint candlePaint) {
        mCandlePaint = candlePaint;
        return this;
    }

    public int getRedCandleColor() {
        return mRedCandleColor;
    }

    public MasterView setRedCandleColor(int redCandleColor) {
        mRedCandleColor = redCandleColor;
        return this;
    }

    public int getGreenCandleColor() {
        return mGreenCandleColor;
    }

    public MasterView setGreenCandleColor(int greenCandleColor) {
        mGreenCandleColor = greenCandleColor;
        return this;
    }

    public int getCanldeHighLowWidth() {
        return mCanldeHighLowWidth;
    }

    public MasterView setCanldeHighLowWidth(int canldeHighLowWidth) {
        mCanldeHighLowWidth = canldeHighLowWidth;
        return this;
    }

    public KViewType.MasterIndicatrixType getMasterType() {
        return mMasterType;
    }

    public MasterView setMasterType(KViewType.MasterIndicatrixType masterType) {
        mMasterType = masterType;
        return this;
    }

    public Paint getMa5Paint() {
        return mMa5Paint;
    }

    public MasterView setMa5Paint(Paint ma5Paint) {
        mMa5Paint = ma5Paint;
        return this;
    }

    public int getMa5Color() {
        return mMa5Color;
    }

    public MasterView setMa5Color(int ma5Color) {
        mMa5Color = ma5Color;
        return this;
    }

    public Paint getMa10Paint() {
        return mMa10Paint;
    }

    public MasterView setMa10Paint(Paint ma10Paint) {
        mMa10Paint = ma10Paint;
        return this;
    }

    public int getMa10Color() {
        return mMa10Color;
    }

    public MasterView setMa10Color(int ma10Color) {
        mMa10Color = ma10Color;
        return this;
    }

    public Paint getMa20Paint() {
        return mMa20Paint;
    }

    public MasterView setMa20Paint(Paint ma20Paint) {
        mMa20Paint = ma20Paint;
        return this;
    }

    public int getMa20Color() {
        return mMa20Color;
    }

    public MasterView setMa20Color(int ma20Color) {
        mMa20Color = ma20Color;
        return this;
    }

    public Paint getBollUpPaint() {
        return mBollUpPaint;
    }

    public MasterView setBollUpPaint(Paint bollUpPaint) {
        mBollUpPaint = bollUpPaint;
        return this;
    }

    public int getBollUpColor() {
        return mBollUpColor;
    }

    public MasterView setBollUpColor(int bollUpColor) {
        mBollUpColor = bollUpColor;
        return this;
    }

    public Paint getBollMbPaint() {
        return mBollMbPaint;
    }

    public MasterView setBollMbPaint(Paint bollMbPaint) {
        mBollMbPaint = bollMbPaint;
        return this;
    }

    public int getBollMbColor() {
        return mBollMbColor;
    }

    public MasterView setBollMbColor(int bollMbColor) {
        mBollMbColor = bollMbColor;
        return this;
    }

    public Paint getBollDnPaint() {
        return mBollDnPaint;
    }

    public MasterView setBollDnPaint(Paint bollDnPaint) {
        mBollDnPaint = bollDnPaint;
        return this;
    }

    public int getBollDnColor() {
        return mBollDnColor;
    }

    public MasterView setBollDnColor(int bollDnColor) {
        mBollDnColor = bollDnColor;
        return this;
    }

    public double getCandleMinY() {
        return mCandleMinY;
    }

    public MasterView setCandleMinY(double candleMinY) {
        mCandleMinY = candleMinY;
        return this;
    }

    public double getCandleMaxY() {
        return mCandleMaxY;
    }

    public MasterView setCandleMaxY(double candleMaxY) {
        mCandleMaxY = candleMaxY;
        return this;
    }

    public float getCandleDiverWidthRatio() {
        return mCandleDiverWidthRatio;
    }

    public MasterView setCandleDiverWidthRatio(float candleDiverWidthRatio) {
        mCandleDiverWidthRatio = candleDiverWidthRatio;
        return this;
    }

    public KViewListener.MinorListener getMinorListener() {
        return mMinorListener;
    }

    public ScaleGestureDetector.OnScaleGestureListener getOnScaleGestureListener() {
        return mOnScaleGestureListener;
    }

    public MasterView setOnScaleGestureListener(ScaleGestureDetector.OnScaleGestureListener
                                                        onScaleGestureListener) {
        mOnScaleGestureListener = onScaleGestureListener;
        return this;
    }

    public int getBrokenLineBgAlpha() {
        return mBrokenLineBgAlpha;
    }

    public MasterView setBrokenLineBgAlpha(int brokenLineBgAlpha) {
        mBrokenLineBgAlpha = brokenLineBgAlpha;
        return this;
    }

}
