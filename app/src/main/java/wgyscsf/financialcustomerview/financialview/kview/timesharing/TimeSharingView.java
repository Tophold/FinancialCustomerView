package wgyscsf.financialcustomerview.financialview.kview.timesharing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import wgyscsf.financialcustomerview.R;
import wgyscsf.financialcustomerview.financialview.kview.KView;
import wgyscsf.financialcustomerview.financialview.kview.Quotes;
import wgyscsf.financialcustomerview.utils.FormatUtil;
import wgyscsf.financialcustomerview.utils.TimeUtils;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/11/21 16:26
 * 描 述 ：分时图，根据闭盘价绘制分时图。
 * 为了模拟真实环境，拿到的数据没有直接使用，而是做了适配转换处理。没有使用任何第三方框架，
 * rx的使用紧紧在模拟网络环境获取数据的时候进行了线程的切换处理，控件中并没有使用。
 * <p>
 * 现功能如下：
 * 绘制必要的各种背景、加载分时折线图、实时价横线显示、实时价更新分时图、
 * 长按显示当前（距离按下点最近的有效点）价，并回调有效点、支持拖拽、支持缩放、支持加载更多、
 * 长按下不可滑动、滑动覆盖右侧内边距。
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
public class TimeSharingView extends KView {

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
    int mAlpha = 40;

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

    //画笔:外围X、Y轴线文字
    Paint mXYTxtPaint;
    //x、y轴指示文字字体的大小
    final float mXYTxtSize = 14;
    int mXYTxtColor;
    //右侧文字距离右边线线的距离
    final float mRightTxtPadding = 4;
    //底部文字距离底部线的距离
    final float mBottomTxtPadding = 20;

    //画笔:长按的十字线
    Paint mLongPressPaint;
    int mLongPressColor;
    float mLongPressLineWidth = 1.5f;
    //滑动点的x轴坐标，滑动使用，记录当前滑动点的x坐标
    float mMovingX;
    //是否绘制长按十字，逻辑判断使用，不可更改
    boolean mDrawLongPressPaint = false;

    //画笔:长按十字的上方的时间框、右侧的数据框
    Paint mLongPressTxtPaint;
    Paint mLongPressTxtBgPaint;
    int mLongPressTxtColor;
    float mLongPressTxtSize = 18;
    int mLongPressTxtBgColor;

    //画笔:蜡烛图
    Paint mCandlePaint;
    int mRedCandleColor;
    int mGreenCandleColor;
    //单个蜡烛最大值最小值对应的y轴方向的线宽度
    int mCanldeHighLowWidth = 1;


    public TimeSharingView(Context context) {
        this(context, null);
    }

    public TimeSharingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeSharingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        //Log.e("TimeSharingView", "onDraw: ");
        if (mQuotesList == null || mQuotesList.isEmpty()) {
            return;
        }
        //绘制x周和y周的文字
        drawXyTxt(canvas);
        //绘制分时图折现
        drawBrokenLine(canvas);
        //绘制蜡烛图
        drawCandleView(canvas);
        //长按处理
        drawLongPress(canvas);
        //长按情况下的时间和数据框
        drawLongPressTxt(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //按下的手指个数
        mFingerPressedCount = event.getPointerCount();
        //手势监听
        //Log.e(TAG, "onTouchEvent: " + event.getPointerCount());
        mScaleGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPressedX = event.getX();
                mPressTime = event.getDownTime();
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getEventTime() - mPressTime > DEF_LONGPRESS_LENGTH) {
                    //Log.e(TAG, "onTouchEvent: 长按了。。。");
                    mMovingX = event.getX();
                    showLongPressView();
                }
                //判断是否是手指移动
                float currentPressedX = event.getX();
                float moveLen = currentPressedX - mPressedX;
                //重置当前按下的位置
                mPressedX = currentPressedX;
                if (Math.abs(moveLen) > DEF_PULL_LENGTH &&
                        mFingerPressedCount == 1 &&
                        !mDrawLongPressPaint) {
                    //Log.e(TAG, "onTouchEvent: 正在移动分时图");
                    //移动k线图
                    moveKView(moveLen);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (event.getEventTime() - mPressTime < DEF_CLICKPRESS_LENGTH && mDrawLongPressPaint) {
                    //取消掉长按十字
                    hiddenLongPressView();
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

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
        if (mPullRight) {
            int len = mBeginIndex - moveCount;
            if (len < DEF_MINLEN_LOADMORE) {
                //加载更多
                if (mTimeSharingListener != null && mCanLoadMore) {
                    loadMoreIng();
                    mTimeSharingListener.needLoadMore();
                }
            }
            if (len < 0) {
                mBeginIndex = 0;
                mPullType = PullType.PULL_LEFT_STOP;
            } else {
                mBeginIndex = len;
                mPullType = PullType.PULL_LEFT;
            }
        } else {
            int len = mBeginIndex + moveCount;
            if (len + mShownMaxCount > mQuotesList.size()) {
                mBeginIndex = mQuotesList.size() - mShownMaxCount;
                //到最左边啦
                mPullType = PullType.PULL_RIGHT_STOP;
                //重置到之前的状态
                mInnerRightBlankPadding = DEF_INNER_RIGHT_BLANK_PADDING;
            } else {
                mBeginIndex = len;
                mPullType = PullType.PULL_RIGHT;
            }
        }
        mEndIndex = mBeginIndex + mShownMaxCount;
        //开始位置和结束位置确认好，就可以重绘啦~
        //Log.e(TAG, "moveKView: mPullRight：" + mPullRight + ",mBeginIndex:" + mBeginIndex + ",mEndIndex:" + mEndIndex);
        seekAndCalculateCellData();
    }

    protected void showLongPressView() {
        mDrawLongPressPaint = true;
        invalidate();
    }

    protected void hiddenLongPressView() {
        mDrawLongPressPaint = false;
        invalidate();
        mTimeSharingListener.onUnLongTouch();
    }

    protected void initAttrs() {
        //加载颜色和字符串资源
        loadDefAttrs();

        //初始化画笔
        initXyTxtPaint();
        initDotPaint();
        initTimingTxtPaint();
        initTimingLinePaint();
        initLongPressPaint();
        initLongPressTxtPaint();
        initBrokenLinePaint();
        initBrokenLineBgPaint();
        initCandlePaint();

        //手势
        mScaleGestureDetector = new ScaleGestureDetector(mContext, mOnScaleGestureListener);

        //是分时图还是蜡烛图
        setViewType(ViewType.CANDLE);
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
        mXYTxtColor = getColor(R.color.color_timeSharing_xYTxtColor);
        mLongPressColor = getColor(R.color.color_timeSharing_longPressLineColor);
        mLongPressTxtColor = getColor(R.color.color_timeSharing_longPressTxtColor);
        mLongPressTxtBgColor = getColor(R.color.color_timeSharing_longPressTxtBgColor);
        mRedCandleColor = getColor(R.color.color_timeSharing_candleRed);
        mGreenCandleColor = getColor(R.color.color_timeSharing_candleGreen);
    }

    protected void initXyTxtPaint() {
        mXYTxtPaint = new Paint();
        mXYTxtPaint.setColor(mXYTxtColor);
        mXYTxtPaint.setTextSize(mXYTxtSize);
        mXYTxtPaint.setAntiAlias(true);
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

    protected void initLongPressPaint() {
        mLongPressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLongPressPaint.setColor(mLongPressColor);
        mLongPressPaint.setStrokeWidth(mLongPressLineWidth);
        mLongPressPaint.setStyle(Paint.Style.STROKE);
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
        mBrokenLineBgPaint.setAlpha(mAlpha);
    }

    private void initCandlePaint() {
        mCandlePaint = new Paint();
        mCandlePaint.setColor(mRedCandleColor);
        mCandlePaint.setStyle(Paint.Style.FILL);
        mCandlePaint.setAntiAlias(true);
        mCandlePaint.setStrokeWidth(mCanldeHighLowWidth);
    }

    protected void drawXyTxt(Canvas canvas) {
        //先处理y轴方向文字
        drawYPaint(canvas);

        //处理x轴方向文字
        drawXPaint(canvas);
    }

    protected void drawBrokenLine(Canvas canvas) {
        //先画第一个点
        Quotes quotes = mQuotesList.get(mBeginIndex);
        Path path = new Path();
        Path path2 = new Path();
        //这里需要说明一下，x轴的起始点，其实需要加上mPerX，但是加上之后不是从起始位置开始，不好看。
        // 同理，for循环内x轴其实需要(i+1)。现在这样处理，最后会留一点空隙，其实挺好看的。
        float floatY = (float) (mHeight - mPaddingBottom - mInnerBottomBlankPadding -
                mClosePerY * (quotes.c - mMinColseQuotes.c));
        //在自定义view:FundView中的位置坐标
        //记录下位置信息
        quotes.floatX = mPaddingLeft;
        quotes.floatY = floatY;
        path.moveTo(mPaddingLeft, floatY);
        path2.moveTo(mPaddingLeft, floatY);
        for (int i = mBeginIndex + 1; i < mEndIndex; i++) {
            Quotes q = mQuotesList.get(i);
            //注意这个 mPerX * (i-mBeginIndex)，而不是mPerX * (i)
            float floatX2 = mPaddingLeft + mPerX * (i - mBeginIndex);
            float floatY2 = (float) (mHeight - mPaddingBottom - mInnerBottomBlankPadding -
                    mClosePerY * (q.c - mMinColseQuotes.c));
            //记录下位置信息
            q.floatX = floatX2;
            q.floatY = floatY2;
            path.lineTo(floatX2, floatY2);
            path2.lineTo(floatX2, floatY2);
            //最后一个点，画一个小圆点；实时横线；横线的右侧数据与背景；折线下方阴影
            if (i == mEndIndex - 1) {
                //这里滑动到最右端
                if (mPullType == PullType.PULL_RIGHT_STOP) {
                    //绘制小圆点
                    canvas.drawCircle(floatX2, floatY2, mDotRadius, mDotPaint);
                } else {
                    //这里隐藏小圆点并且重新计算Y值。这里这样处理，对应现象的问题：横线划出界面。
                    Quotes endQuotes = mQuotesList.get(mQuotesList.size() - 1);
                    floatY2 = (float) (mHeight - mPaddingBottom - mInnerBottomBlankPadding -
                            mClosePerY * (endQuotes.c - mMinColseQuotes.c));
                }

                //实时数据展示的前提是在指定范围内。不处理对应的异常：实时横线显示在底部横线的下面...
                if (mPaddingTop < floatY2 && floatY2 < mHeight - mPaddingBottom) {
                    //接着画实时横线
                    canvas.drawLine(mPaddingLeft, floatY2, mWidth - mPaddingRight, floatY2,
                            mTimingLinePaint);

                    //接着绘制实时横线的右侧数据与背景
                    //文字高度
                    float txtHight = getFontHeight(mTimingTxtWidth, mTimingTxtBgPaint);
                    //绘制背景
                    canvas.drawRect(mWidth - mPaddingRight, floatY2 - txtHight / 2, mWidth,
                            floatY2 + txtHight / 2, mTimingTxtBgPaint);

                    //绘制实时数据
                    //距离左边的距离
                    float leftDis = 8;
                    canvas.drawText(FormatUtil.numFormat(q.c, mDigits),
                            mWidth - mPaddingRight + leftDis, floatY2 + txtHight / 4,
                            mTimingTxtPaint);
                }

                //在这里把path圈起来，添加阴影。特别注意，这里处理下方阴影和折线边框。采用两个画笔和两个Path处理的，
                // 貌似没有一个Paint可以同时绘制边框和填充色。
                path2.lineTo(floatX2, mHeight - mPaddingBottom);
                path2.lineTo(mPaddingLeft, mHeight - mPaddingBottom);
                path2.close();
            }
        }
        canvas.drawPath(path, mBrokenLinePaint);
        canvas.drawPath(path2, mBrokenLineBgPaint);
    }

    private void drawCandleView(Canvas canvas) {
        if (mViewType != ViewType.CANDLE) return;

        float topRectY;
        float bottomRectY;
        float leftRectX;
        float rightRectX;

        float topLineY;
        float bottomLineY;
        float leftLineX;
        float rightLineX;
        //蜡烛图单个之间的间隙
        float diverWidth=mCandleDiverWidthRatio*mPerX;

        for (int i = mBeginIndex; i < mEndIndex; i++) {
            Quotes quotes = mQuotesList.get(i);
            //定位蜡烛矩形的四个点
            topRectY = (float) ( mPaddingTop+mInnerTopBlankPadding+
                    mPerY * (quotes.o-mMinLowQuotes.l));
            bottomRectY = (float) (mPaddingTop+mInnerTopBlankPadding+
                    mPerY * (quotes.c-mMinLowQuotes.l));
            leftRectX = mPaddingLeft + mPerX * (i - mBeginIndex)+diverWidth/2;
            rightRectX = mPaddingLeft + mPerX * (i - mBeginIndex + 1)-diverWidth/2;

            //定位单个蜡烛中间线的四个点
            leftLineX= (float) (mPaddingLeft+mPerX/2.0+mPerX * (i - mBeginIndex));
            topLineY=(float) ( mPaddingTop+mInnerTopBlankPadding+
                    mPerY* (quotes.h-mMinLowQuotes.l));
            rightLineX= (float) (mPaddingLeft+mPerX/2.0+mPerX * (i - mBeginIndex));
            bottomLineY=(float) (mPaddingTop+mInnerTopBlankPadding+
                    mPerY* (quotes.l-mMinLowQuotes.l));

            RectF rectF = new RectF();
            //Log.e(TAG, "drawCandleView: leftX:"+leftRectX+",topY:"+topRectY+",rightX:"+rightRectX+",bottomY:"+bottomRectY );
            rectF.set(leftRectX, topRectY, rightRectX, bottomRectY);
            //设置颜色
            mCandlePaint.setColor(quotes.c > quotes.o ? mRedCandleColor : mGreenCandleColor);
            canvas.drawRect(rectF, mCandlePaint);

            //Log.e(TAG, "drawCandleView: leftLineX:"+leftLineX+",topLineY:"+topLineY+",rightLineX:"+rightLineX+",bottomLineY:"+bottomLineY );
            //开始画low、high线
            canvas.drawLine(leftLineX, topLineY,rightLineX,bottomLineY,mCandlePaint);
        }

    }

    protected void drawLongPress(Canvas canvas) {
        if (!mDrawLongPressPaint) return;

        //最后的最近的按下的位置
        int finalIndex;
        //获取距离最近按下的位置的model
        float pressX = mMovingX;
        //循环遍历，找到距离最短的x轴的mode
        Quotes finalFundMode = mQuotesList.get(mBeginIndex);
        finalIndex = mBeginIndex;
        float minXLen = Integer.MAX_VALUE;
        for (int i = mBeginIndex; i < mEndIndex; i++) {
            Quotes currFunMode = mQuotesList.get(i);
            float abs = Math.abs(pressX - currFunMode.floatX);
            if (abs < minXLen) {
                finalFundMode = currFunMode;
                minXLen = abs;
                finalIndex = i;
            }
        }

        //x轴线
        canvas.drawLine(mPaddingLeft, finalFundMode.floatY, mWidth - mPaddingRight,
                finalFundMode.floatY, mLongPressPaint);
        //y轴线
        canvas.drawLine(finalFundMode.floatX, mPaddingTop, finalFundMode.floatX,
                mHeight - mPaddingBottom, mLongPressPaint);


        //接着绘制长按上方和右侧文字信息背景
        float txtBgHight = getFontHeight(mLongPressTxtSize, mLongPressTxtBgPaint);
        //y
        float longPressTxtYPadding = 10;
        canvas.drawRect(finalFundMode.floatX -
                        mLongPressTxtBgPaint.measureText(finalFundMode.showTime) / 2 -
                        longPressTxtYPadding,
                mPaddingTop,
                finalFundMode.floatX +
                        mLongPressTxtBgPaint.measureText(finalFundMode.showTime) / 2 +
                        longPressTxtYPadding,
                mPaddingTop + txtBgHight, mLongPressTxtBgPaint);
        //x
        //加一个左右边距
        canvas.drawRect(mWidth - mPaddingRight, finalFundMode.floatY - txtBgHight / 2,
                mWidth,
                finalFundMode.floatY + txtBgHight / 2,
                mLongPressTxtBgPaint);

        //绘制长按上方和右侧文字信息
        float txtHight = getFontHeight(mLongPressTxtSize, mLongPressTxtPaint);
        //x
        //距离左边的距离
        float leftDis = 8;
        canvas.drawText(FormatUtil.numFormat(finalFundMode.c, mDigits),
                mWidth - mPaddingRight + leftDis,
                finalFundMode.floatY + txtHight / 4//这特么的又是需要+/4,理论应该是-/2,原因不明
                , mLongPressTxtPaint);
        //y
        canvas.drawText(finalFundMode.showTime,
                finalFundMode.floatX - mLongPressTxtPaint.measureText(finalFundMode.showTime) / 2,
                mPaddingTop + txtHight - 6
                , mLongPressTxtPaint);

        //在这里回调数据信息
        if (mTimeSharingListener != null) {
            int size = mQuotesList.size();
            if ((0 <= finalIndex && finalIndex < size) &&
                    (0 <= finalIndex - 1 && finalIndex - 1 < size))
                //回调,需要两个数据，便于计算涨跌百分比
                mTimeSharingListener.onLongTouch(mQuotesList.get(finalIndex - 1),
                        mQuotesList.get(finalIndex));
        }
    }

    protected void drawLongPressTxt(Canvas canvas) {
        //see:drawLongPress(Canvas canvas)

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
        double dataDis = mMaxCloseQuotes.c - mMinColseQuotes.c;
        double yDis = (mHeight - mPaddingTop - mPaddingBottom - mInnerTopBlankPadding -
                mInnerBottomBlankPadding);
        double perY = dataDis / yDis;
        minBorderData = mMinColseQuotes.c - mInnerBottomBlankPadding * perY;
        maxBorderData = mMinColseQuotes.c + mInnerTopBlankPadding * perY;

        halfTxtHight = getFontHeight(mXYTxtSize, mXYTxtPaint) / 4;//应该/2的，但是不准确，原因不明
        //halfTxtHight = 0;

        //现将最小值、最大值画好
        float rightBorderPadding = mRightTxtPadding;
        canvas.drawText(FormatUtil.numFormat(minBorderData, mDigits),
                mWidth - mPaddingRight + rightBorderPadding,
                mHeight - mPaddingBottom + halfTxtHight, mXYTxtPaint);
        //draw max
        canvas.drawText(FormatUtil.numFormat(maxBorderData, mDigits),
                mWidth - mPaddingRight + rightBorderPadding,
                mPaddingTop + halfTxtHight, mXYTxtPaint);
        //因为横线是均分的，所以只要取到最大值最小值的差值，均分即可。
        float perYValues = (float) ((maxBorderData - minBorderData) / 4);
        float perYWidth = (mHeight - mPaddingBottom - mPaddingTop) / 4;
        //从下到上依次画
        for (int i = 1; i <= 3; i++) {
            canvas.drawText(FormatUtil.numFormat(minBorderData + perYValues * i, mDigits),
                    mWidth - mPaddingRight + rightBorderPadding,
                    mHeight - mPaddingBottom - perYWidth * i + halfTxtHight, mXYTxtPaint);
        }
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
        double perXWith = (mWidth - mPaddingLeft - mPaddingRight) / 4;
        double xDis = (mWidth - mPaddingLeft - mPaddingRight - mInnerRightBlankPadding);
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
                    (float) (mPaddingLeft + perXWith * i - finalHalfTxtWidth),
                    mHeight - mPaddingBottom + mBottomTxtPadding, mXYTxtPaint);
        }
    }

    //缩放手势监听
    ScaleGestureDetector.OnScaleGestureListener mOnScaleGestureListener =
            new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    Log.e(TAG, "onScale: mFingerPressedCount:" + mFingerPressedCount +
                            ",mShownMaxCount == mQuotesList.size():" + (mShownMaxCount == mQuotesList.size()) +
                            ",mShownMaxCount:" + mShownMaxCount);
                    //没有缩放
                    if (detector.getScaleFactor() == 1) return true;

                    //是放大还是缩小
                    boolean isBigger = detector.getScaleFactor() > 1;

                    //变化的个数（缩小或者放大），必须向上取整，不然当mShownMaxCount过小时容易取到0。
                    int changeNum = (int) Math.ceil(mShownMaxCount * Math.abs(detector.getScaleFactor() - 1));

                    //一半
                    int helfChangeNum = (int) Math.ceil(changeNum / 2f);

                    Log.e(TAG, "onScale:changeNum: " + changeNum + ",helfChangeNum:" + helfChangeNum);

                    //缩放个数太少，直接return
                    if (changeNum == 0 || helfChangeNum == 0) return true;

                    Log.e(TAG, "onScale:mShownMaxCount： " + mShownMaxCount);

                    //容错处理,获取最大最小值
                    if (DEF_SCALE_MINNUM < 3) {
                        DEF_SCALE_MINNUM = 3;
                    }
                    if (DEF_SCALE_MAXNUM > mQuotesList.size()) {
                        DEF_SCALE_MAXNUM = mQuotesList.size();
                    }

                    //变大了(拉伸了)，数量变少了
                    int tempCount = isBigger ? mShownMaxCount - changeNum : mShownMaxCount + changeNum;

                    //缩小大到最小了或者放大到很大了
                    if (tempCount > DEF_SCALE_MAXNUM || tempCount < DEF_SCALE_MINNUM) return true;

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

                    Log.e(TAG, "onScaleBegin:mBeginIndex: " + mBeginIndex + ",mEndIndex:"
                            + mEndIndex + ",changeNum:" + changeNum + ",mShownMaxCount:" + mShownMaxCount);

                    //只要找好起始点和结束点就可以交给处理重绘的方法就好啦~
                    seekAndCalculateCellData();
                    return true;
                }

                @Override
                public boolean onScaleBegin(ScaleGestureDetector detector) {
                    Log.e(TAG, "onScaleBegin: " + detector.getFocusX());
                    //指头数量
                    if (mFingerPressedCount != 2) return true;
                    return true;
                }
            };
}
