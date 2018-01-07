package wgyscsf.financialcustomerview.financialview.kview.master;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import wgyscsf.financialcustomerview.R;
import wgyscsf.financialcustomerview.financialview.FinancialAlgorithm;
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
public class MasterView extends KView {

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
    boolean mDrawLongPress = false;
    //长按对应的对象
    Quotes mCurrentQuotes;

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
    MasterType mMasterType = MasterType.NONE;

    //画笔:ma5、ma10、ma20
    int mMaLineWidth = 1;

    //非长按下主图指标图例
    Paint mLegendPaint;
    int mLegendColor;
    //距离上方border的距离(单位：px)
    double mLegendPaddingTop = 30;
    //这个是文字框的结束位置距离右侧的距离
    double mLegendPaddingRight = 10;
    float mLegendTxtSize = 15;

    //长按下主图指标图例
    //这个是文字框的结束位置距离右侧的距离
    double mLegendPaddingLeft = 10;
    //同时显示ma和boll上下的间距
    double mLegendTxtTopPadding = 0;

    //MA
    Paint mMa5Paint;
    int mMa5Color;
    Paint mMa10Paint;
    int mMa10Color;
    Paint mMa20Paint;
    int mMa20Color;

    //BOLL
    Paint mBollMbPaint;
    int mBollMbColor;
    Paint mBollUpPaint;
    int mBollUpColor;
    Paint mBollDnPaint;
    int mBollDnColor;


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
        //Log.e("TimeSharingView", "onDraw: ");
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
        drawMasterndIcatrix(canvas);
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
                        !mDrawLongPress) {
                    //Log.e(TAG, "onTouchEvent: 正在移动分时图");
                    //移动k线图
                    moveKView(moveLen);
                }
                break;
            case MotionEvent.ACTION_UP:
                //单击事件
                if (event.getEventTime() - mPressTime < DEF_CLICKPRESS_LENGTH) {
                    //单击并且是在绘制十字
                    if (mDrawLongPress) {
                        //取消掉长按十字
                        hiddenLongPressView();
                    } else {
                        //响应单击事件
                        onMClickListener();
                    }
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 单击事件
     */
    private void onMClickListener() {
        if (mViewType == ViewType.CANDLE) {
            if (mMasterType == MasterType.NONE) {
                mMasterType = MasterType.MA;
            } else if (mMasterType == MasterType.MA) {
                mMasterType = MasterType.BOLL;
            } else if (mMasterType == MasterType.BOLL) {
                mMasterType = MasterType.MA_BOLL;
            } else if (mMasterType == MasterType.MA_BOLL) {
                mMasterType = MasterType.NONE;
            }
            //刷新界面
            invalidate();
        }
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
        mDrawLongPress = true;
        invalidate();
    }

    protected void hiddenLongPressView() {
        mDrawLongPress = false;
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
        initLegendPaint();
        initMaPaint();
        initBollPaint();

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

        mLegendColor = getColor(R.color.color_masterView_legendColor);

        mMa5Color = getColor(R.color.color_masterView_ma5Color);
        mMa10Color = getColor(R.color.color_masterView_ma10Color);
        mMa20Color = getColor(R.color.color_masterView_ma20Color);

        mBollMbColor = getColor(R.color.color_masterView_bollMbColor);
        mBollUpColor = getColor(R.color.color_masterView_bollUpColor);
        mBollDnColor = getColor(R.color.color_masterView_bollDnColor);
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

    private void initLegendPaint() {
        mLegendPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLegendPaint.setColor(mLegendColor);
        mLegendPaint.setStrokeWidth(mMaLineWidth);
        mLegendPaint.setTextSize(mLegendTxtSize);
    }

    private void initMaPaint() {
        mMa5Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMa5Paint.setColor(mMa5Color);
        mMa5Paint.setStyle(Paint.Style.STROKE);
        mMa5Paint.setStrokeWidth(mMaLineWidth);
        mMa5Paint.setTextSize(mLegendTxtSize);

        mMa10Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMa10Paint.setColor(mMa10Color);
        mMa10Paint.setStyle(Paint.Style.STROKE);
        mMa10Paint.setStrokeWidth(mMaLineWidth);
        mMa10Paint.setTextSize(mLegendTxtSize);

        mMa20Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMa20Paint.setColor(mMa20Color);
        mMa20Paint.setStyle(Paint.Style.STROKE);
        mMa20Paint.setStrokeWidth(mMaLineWidth);
        mMa20Paint.setTextSize(mLegendTxtSize);
    }

    private void initBollPaint() {
        mBollMbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBollMbPaint.setColor(mBollMbColor);
        mBollMbPaint.setStyle(Paint.Style.STROKE);
        mBollMbPaint.setStrokeWidth(mMaLineWidth);
        mBollMbPaint.setTextSize(mLegendTxtSize);


        mBollUpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBollUpPaint.setColor(mBollUpColor);
        mBollUpPaint.setStyle(Paint.Style.STROKE);
        mBollUpPaint.setStrokeWidth(mMaLineWidth);
        mBollUpPaint.setTextSize(mLegendTxtSize);


        mBollDnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBollDnPaint.setColor(mBollDnColor);
        mBollDnPaint.setStyle(Paint.Style.STROKE);
        mBollDnPaint.setStrokeWidth(mMaLineWidth);
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

        for (int i = mBeginIndex; i < mEndIndex; i++) {
            Quotes quotes = mQuotesList.get(i);
            //mPerX/2.0f：为了让取点为单个单元的x的中间位置
            float floatX = mPaddingLeft + mPerX / 2.0f + mPerX * (i - mBeginIndex);
            float floatY = 0;

            //分时图和蜡烛图要分开对待，测量标准不一致。只要有测量的，全部要区分对待。
            if (mViewType == ViewType.TIMESHARING) {
                floatY = (float) (mHeight - mPaddingBottom - mInnerBottomBlankPadding -
                        mClosePerY * (quotes.c - mMinColseQuotes.c));
            } else if (mViewType == ViewType.CANDLE) {
                floatY = (float) (mHeight - mPaddingBottom - mInnerBottomBlankPadding -
                        mPerY * (quotes.c - mMinLowQuotes.l));
            }


            //记录下位置信息
            quotes.floatX = floatX;
            quotes.floatY = floatY;

            //边界位置修正
            //不是在最新的位置（其实也就是最新点靠近右侧边框），并且是最后一个点则向右加一个mPerX/2.0f。
            if (mPullType != PullType.PULL_RIGHT_STOP) {
                if (i == mEndIndex - 1) {
                    quotes.floatX += mPerX / 2.0f;
                }
            }
            //如果是开始位置，则减去一个mPerX/2.0f
            if (i == mBeginIndex) {
                quotes.floatX -= mPerX / 2.0f;
            }


            /**分时图折现的绘制*/
            drawTimSharingProcess(quotes, i, brokenLinePath, brokenLineBgPath);

            /**实时横线的绘制*/
            drawTimingLineProcess(canvas, quotes, i);

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

    }

    private void drawMasterLegend(Canvas canvas) {
        if (mViewType != ViewType.CANDLE) return;

        //长按
        if (mDrawLongPress) {
            if (mCurrentQuotes == null) return;
            mMa5Paint.setStyle(Paint.Style.FILL);
            mMa10Paint.setStyle(Paint.Style.FILL);
            mMa20Paint.setStyle(Paint.Style.FILL);
            mBollDnPaint.setStyle(Paint.Style.FILL);
            mBollUpPaint.setStyle(Paint.Style.FILL);
            mBollMbPaint.setStyle(Paint.Style.FILL);
            //非长按
            String showTxt = "";
            if (mMasterType == MasterType.MA) {
                showTxt = "• MA5 " + FormatUtil.formatBySubString(mCurrentQuotes.ma5, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mPaddingLeft),
                        (float) (mLegendPaddingTop + mPaddingTop), mMa5Paint);

                float leftWidth = mMa5Paint.measureText(showTxt);
                showTxt = "• MA10 " + FormatUtil.formatBySubString(mCurrentQuotes.ma10, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mPaddingLeft + leftWidth),
                        (float) (mLegendPaddingTop + mPaddingTop), mMa10Paint);

                float leftWidth2 = mMa10Paint.measureText(showTxt);
                showTxt = "• MA20 " + FormatUtil.formatBySubString(mCurrentQuotes.ma20, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mPaddingLeft + leftWidth + leftWidth2),
                        (float) (mLegendPaddingTop + mPaddingTop), mMa20Paint);
            } else if (mMasterType == MasterType.BOLL) {
                showTxt = "• UPPER " + FormatUtil.formatBySubString(mCurrentQuotes.mb, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mPaddingLeft),
                        (float) (mLegendPaddingTop + mPaddingTop), mBollMbPaint);

                float leftWidth = mBollMbPaint.measureText(showTxt);
                showTxt = "• MID " + FormatUtil.formatBySubString(mCurrentQuotes.up, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mPaddingLeft + leftWidth),
                        (float) (mLegendPaddingTop + mPaddingTop), mBollUpPaint);

                float leftWidth2 = mBollUpPaint.measureText(showTxt);
                showTxt = "• LOWER " + FormatUtil.formatBySubString(mCurrentQuotes.dn, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mPaddingLeft + leftWidth + leftWidth2),
                        (float) (mLegendPaddingTop + mPaddingTop), mBollDnPaint);

            } else if (mMasterType == MasterType.MA_BOLL) {
                showTxt = "• MA5 " + FormatUtil.formatBySubString(mCurrentQuotes.ma5, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mPaddingLeft),
                        (float) (mLegendPaddingTop + mPaddingTop), mMa5Paint);

                float leftWidth = mMa5Paint.measureText(showTxt);
                showTxt = "• MA10 " + FormatUtil.formatBySubString(mCurrentQuotes.ma10, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mPaddingLeft + leftWidth),
                        (float) (mLegendPaddingTop + mPaddingTop), mMa10Paint);

                float leftWidth2 = mMa10Paint.measureText(showTxt);
                showTxt = "• MA20 " + FormatUtil.formatBySubString(mCurrentQuotes.ma20, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mPaddingLeft + leftWidth + leftWidth2),
                        (float) (mLegendPaddingTop + mPaddingTop), mMa20Paint);


                double high = getFontHeight(mLegendTxtSize, mMa5Paint);
                high += mLegendTxtTopPadding;
                showTxt = "• UPPER " + FormatUtil.formatBySubString(mCurrentQuotes.mb, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mPaddingLeft),
                        (float) (mLegendPaddingTop + mPaddingTop + high), mBollMbPaint);

                float leftWidth3 = mBollMbPaint.measureText(showTxt);
                showTxt = "• MID " + FormatUtil.formatBySubString(mCurrentQuotes.up, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mPaddingLeft + leftWidth3),
                        (float) (mLegendPaddingTop + mPaddingTop + high), mBollUpPaint);

                float leftWidth23 = mBollUpPaint.measureText(showTxt);
                showTxt = "• LOWER " + FormatUtil.formatBySubString(mCurrentQuotes.dn, mDigits) + " ";
                canvas.drawText(showTxt, (float) (mLegendPaddingLeft + mPaddingLeft + leftWidth23 + leftWidth3),
                        (float) (mLegendPaddingTop + mPaddingTop + high), mBollDnPaint);

            }
        } else {
            //非长按
            String showTxt = "";
            if (mMasterType == MasterType.MA) {
                showTxt = "MA(5,10,20)";
            } else if (mMasterType == MasterType.BOLL) {
                showTxt = "BOLL(26)";

            } else if (mMasterType == MasterType.MA_BOLL) {
                showTxt = "BOLL(26)&MA(5,10,20)";
            }
            canvas.drawText(showTxt,
                    (float) (mWidth - mLegendPaddingRight - mPaddingRight - mLegendPaint.measureText(showTxt)),
                    (float) (mLegendPaddingTop + mPaddingTop), mLegendPaint);
        }
    }

    private void drawMasterndIcatrix(Canvas canvas) {
        //指标展示的前提是蜡烛图
        if (mViewType != ViewType.CANDLE) return;

        if (mMasterType == MasterType.NONE) {

        } else if (mMasterType == MasterType.MA) {
            drawMa(canvas);
        } else if (mMasterType == MasterType.BOLL) {
            drawBoll(canvas);
        } else if (mMasterType == MasterType.MA_BOLL) {
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
            float floatX = quotes.floatX;//在绘制蜡烛图的时候已经计算了

            float floatY = getMasterDetailFloatY(quotes, MasterDetailType.MA5);

            //异常,在View的行为就是不显示而已，影响不大。一般都是数据的开头部分。
            if (floatY == -1) continue;

            if (isFirstMa5) {
                isFirstMa5 = false;
                path5.moveTo(floatX, floatY);
            } else {
                path5.lineTo(floatX, floatY);
            }

            floatY = getMasterDetailFloatY(quotes, MasterDetailType.MA10);

            //异常
            if (floatY == -1) continue;

            if (isFirstMa10) {
                isFirstMa10 = false;
                path10.moveTo(floatX, floatY);
            } else {
                path10.lineTo(floatX, floatY);
            }

            floatY = getMasterDetailFloatY(quotes, MasterDetailType.MA20);

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

    private float getMasterDetailFloatY(Quotes quotes, MasterDetailType maType) {
        double v = 0;
        //ma
        if (maType == MasterDetailType.MA5) {
            v = quotes.ma5 - mMinLowQuotes.l;
        } else if (maType == MasterDetailType.MA10) {
            v = quotes.ma10 - mMinLowQuotes.l;
        } else if (maType == MasterDetailType.MA20) {
            v = quotes.ma20 - mMinLowQuotes.l;
        }
        //boll
        else if (maType == MasterDetailType.BOLLMB) {
            v = quotes.mb - mMinLowQuotes.l;
        } else if (maType == MasterDetailType.BOLLUP) {
            v = quotes.up - mMinLowQuotes.l;
        } else if (maType == MasterDetailType.BOLLDN) {
            v = quotes.dn - mMinLowQuotes.l;
        }
        //异常，当不存在ma值时的处理
        if (v + mMinLowQuotes.l == 0) return -1;

        double h = v * mPerY;
        float y = (float) (mHeight - h - mPaddingBottom - mInnerBottomBlankPadding);

        //这里的y,存在一种情况，y超过了View的上边界或者超过了下边界，当出现这一种情况时，不显示，当作异常情况
        if (y < mPaddingTop || y > mHeight - mPaddingBottom)
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

            float floatY = getMasterDetailFloatY(quotes, MasterDetailType.BOLLMB);

            //异常,在View的行为就是不显示而已，影响不大。一般都是数据的开头部分。
            if (floatY == -1) continue;

            if (isFirstMB) {
                isFirstMB = false;
                mbPath.moveTo(floatX, floatY);
            } else {
                mbPath.lineTo(floatX, floatY);
            }

            floatY = getMasterDetailFloatY(quotes, MasterDetailType.BOLLUP);

            //异常
            if (floatY == -1) continue;

            if (isFirstUP) {
                isFirstUP = false;
                upPath.moveTo(floatX, floatY);
            } else {
                upPath.lineTo(floatX, floatY);
            }

            floatY = getMasterDetailFloatY(quotes, MasterDetailType.BOLLDN);

            //异常
            if (floatY == -1) continue;

            if (isFirstDN) {
                isFirstDN = false;
                dnPath.moveTo(floatX, floatY);
            } else {
                dnPath.lineTo(floatX, floatY);
            }
        }

        canvas.drawPath(mbPath, mBollMbPaint);
        canvas.drawPath(upPath, mBollUpPaint);
        canvas.drawPath(dnPath, mBollDnPaint);
    }

    private void drawCandleViewProcess(Canvas canvas, float diverWidth, int i, Quotes quotes) {
        if (mViewType != ViewType.CANDLE) return;

        float topRectY;
        float bottomRectY;
        float leftRectX;
        float rightRectX;
        float leftLineX;
        float topLineY;
        float rightLineX;
        float bottomLineY;//定位蜡烛矩形的四个点
        topRectY = (float) (mPaddingTop + mInnerTopBlankPadding +
                mPerY * (mMaxHighQuotes.h - quotes.o));
        bottomRectY = (float) (mPaddingTop + mInnerTopBlankPadding +
                mPerY * (mMaxHighQuotes.h - quotes.c));
        leftRectX = -mPerX / 2 + quotes.floatX + diverWidth / 2;
        rightRectX = mPerX / 2 + quotes.floatX - diverWidth / 2;

        //定位单个蜡烛中间线的四个点
        leftLineX = quotes.floatX;
        topLineY = (float) (mPaddingTop + mInnerTopBlankPadding +
                mPerY * (mMaxHighQuotes.h - quotes.h));
        rightLineX = quotes.floatX;
        bottomLineY = (float) (mPaddingTop + mInnerTopBlankPadding +
                mPerY * (mMaxHighQuotes.h - quotes.l));

        RectF rectF = new RectF();
        //Log.e(TAG, "drawCandleView: leftX:"+leftRectX+",topY:"+topRectY+",rightX:"+rightRectX+",bottomY:"+bottomRectY );
        //边界处理
        if (i == mBeginIndex) {
            leftRectX = leftRectX < mPaddingLeft ? mPaddingLeft : leftRectX;
            leftLineX = leftLineX < mPaddingLeft ? mPaddingLeft : leftLineX;
        } else if (i == (mEndIndex - 1)) {
            rightRectX = rightRectX > mWidth - mPaddingRight ? mWidth - mPaddingRight : rightRectX;
            rightLineX = rightLineX > mWidth - mPaddingRight ? mWidth - mPaddingRight : rightLineX;
        }
        rectF.set(leftRectX, topRectY, rightRectX, bottomRectY);
        //设置颜色
        mCandlePaint.setColor(quotes.c > quotes.o ? mRedCandleColor : mGreenCandleColor);
        canvas.drawRect(rectF, mCandlePaint);

        //Log.e(TAG, "drawCandleView: leftLineX:"+leftLineX+",topLineY:"+topLineY+",rightLineX:"+rightLineX+",bottomLineY:"+bottomLineY );
        //开始画low、high线
        canvas.drawLine(leftLineX, topLineY, rightLineX, bottomLineY, mCandlePaint);
    }

    private void drawLongPress(Canvas canvas, int finalIndex, Quotes finalFundMode) {
        if (!mDrawLongPress) return;

        //        Log.e(TAG, "drawLongPress: " + mPaddingLeft + "，"
        //                + finalFundMode.floatY + "，" + (mWidth - mPaddingRight) + "," + finalFundMode.floatY);
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
                //记录当前Quotes
                mCurrentQuotes = mQuotesList.get(finalIndex);

            //回调,需要两个数据，便于计算涨跌百分比
            mTimeSharingListener.onLongTouch(mQuotesList.get(finalIndex - 1),
                    mQuotesList.get(finalIndex));
        }
    }

    private void drawTimSharing(Canvas canvas, Path brokenLinePath, Path brokenLineBgPath) {
        if (mViewType != ViewType.TIMESHARING) return;

        canvas.drawPath(brokenLinePath, mBrokenLinePaint);
        canvas.drawPath(brokenLineBgPath, mBrokenLineBgPaint);
    }

    private void drawTimingLineProcess(Canvas canvas, Quotes quotes, int i) {
        if (i == mEndIndex - 1) {
            //这里滑动到最右端
            //绘制小圆点
            if (mPullType == PullType.PULL_RIGHT_STOP) {
                //对于蜡烛图不需要绘制小圆点，但是需要绘制实时横线
                if (mViewType == ViewType.TIMESHARING) {
                    canvas.drawCircle(quotes.floatX, quotes.floatY, mDotRadius, mDotPaint);
                }
            } else {
                //这里隐藏小圆点并且重新计算Y值。这里这样处理，对应现象的问题：横线划出界面。
                Quotes endQuotes = mQuotesList.get(mQuotesList.size() - 1);
                //分时图
                if (mViewType == ViewType.TIMESHARING) {
                    quotes.floatY = (float) (mHeight - mPaddingBottom - mInnerBottomBlankPadding -
                            mClosePerY * (endQuotes.c - mMinColseQuotes.c));
                } else {
                    //蜡烛图
                    quotes.floatY = (float) (mHeight - mPaddingBottom - mInnerBottomBlankPadding -
                            mPerY * (endQuotes.c - mMinLowQuotes.l));
                }
            }

            //实时数据展示的前提是在指定范围内。不处理对应的异常：实时横线显示在底部横线的下面...
            if (mPaddingTop < quotes.floatY && quotes.floatY < mHeight - mPaddingBottom) {
                //接着画实时横线
                canvas.drawLine(mPaddingLeft, quotes.floatY, mWidth - mPaddingRight, quotes.floatY,
                        mTimingLinePaint);

                //接着绘制实时横线的右侧数据与背景
                //文字高度
                float txtHight = getFontHeight(mTimingTxtWidth, mTimingTxtBgPaint);
                //绘制背景
                canvas.drawRect(mWidth - mPaddingRight, quotes.floatY - txtHight / 2, mWidth,
                        quotes.floatY + txtHight / 2, mTimingTxtBgPaint);

                //绘制实时数据
                //距离左边的距离
                float leftDis = 8;
                canvas.drawText(FormatUtil.numFormat(quotes.c, mDigits),
                        mWidth - mPaddingRight + leftDis, quotes.floatY + txtHight / 4,
                        mTimingTxtPaint);
            }
        }
    }

    private void drawTimSharingProcess(Quotes quotes, int i, Path path, Path path2) {
        if (mViewType != ViewType.TIMESHARING) return;

        if (i == mBeginIndex) {
            path.moveTo(quotes.floatX, quotes.floatY);
            path2.moveTo(quotes.floatX, quotes.floatY);
        } else {
            //Log.e(TAG, "drawTimSharingProcess: "+quotes.floatX );
            path.lineTo(quotes.floatX, quotes.floatY);
            //开始绘制path
            path2.lineTo(quotes.floatX, quotes.floatY);
        }

        //最后一个点
        if (i == mEndIndex - 1) {
            //在这里把path圈起来，添加阴影。特别注意，这里处理下方阴影和折线边框。采用两个画笔和两个Path处理的，
            // 貌似没有一个Paint可以同时绘制边框和填充色。
            path2.lineTo(quotes.floatX, mHeight - mPaddingBottom);
            path2.lineTo(mPaddingLeft, mHeight - mPaddingBottom);
            path2.close();
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

        //对于分时图和蜡烛图的最大值和最小值计算是不一样的，因此测量的时候也要分开对待
        if (mViewType == ViewType.TIMESHARING) {
            dataDis = mMaxCloseQuotes.c - mMinColseQuotes.c;
        } else if (mViewType == ViewType.CANDLE) {
            dataDis = mMaxHighQuotes.h - mMinLowQuotes.l;
        }

        double yDis = (mHeight - mPaddingTop - mPaddingBottom - mInnerTopBlankPadding -
                mInnerBottomBlankPadding);
        //double yDis = mHeight;
        double perY = dataDis / yDis;

        if (mViewType == ViewType.TIMESHARING) {
            minBorderData = mMinColseQuotes.c - mInnerBottomBlankPadding * perY;
            maxBorderData = mMaxCloseQuotes.c + mInnerTopBlankPadding * perY;
        } else {
            minBorderData = mMinLowQuotes.l - mInnerBottomBlankPadding * perY;
            maxBorderData = mMaxHighQuotes.h + mInnerTopBlankPadding * perY;
        }


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


    @Override
    protected void seekAndCalculateCellData() {
        super.seekAndCalculateCellData();
        //ma
        FinancialAlgorithm.calculateMA(mQuotesList, 5);
        FinancialAlgorithm.calculateMA(mQuotesList, 10);
        FinancialAlgorithm.calculateMA(mQuotesList, 20);

        //boll
        FinancialAlgorithm.calculateBOLL(mQuotesList);
        invalidate();
    }


    //缩放手势监听
    ScaleGestureDetector.OnScaleGestureListener mOnScaleGestureListener =
            new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    //                    Log.e(TAG, "onScale: mFingerPressedCount:" + mFingerPressedCount +
                    //                            ",mShownMaxCount == mQuotesList.size():" + (mShownMaxCount == mQuotesList.size()) +
                    //                            ",mShownMaxCount:" + mShownMaxCount);
                    //没有缩放
                    if (detector.getScaleFactor() == 1) return true;

                    //是放大还是缩小
                    boolean isBigger = detector.getScaleFactor() > 1;

                    //变化的个数（缩小或者放大），必须向上取整，不然当mShownMaxCount过小时容易取到0。
                    int changeNum = (int) Math.ceil(mShownMaxCount * Math.abs(detector.getScaleFactor() - 1));

                    //一半
                    int helfChangeNum = (int) Math.ceil(changeNum / 2f);

                    //Log.e(TAG, "onScale:changeNum: " + changeNum + ",helfChangeNum:" + helfChangeNum);

                    //缩放个数太少，直接return
                    if (changeNum == 0 || helfChangeNum == 0) return true;

                    //Log.e(TAG, "onScale:mShownMaxCount： " + mShownMaxCount);

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

                    //                    Log.e(TAG, "onScaleBegin:mBeginIndex: " + mBeginIndex + ",mEndIndex:"
                    //                            + mEndIndex + ",changeNum:" + changeNum + ",mShownMaxCount:" + mShownMaxCount);

                    //只要找好起始点和结束点就可以交给处理重绘的方法就好啦~
                    seekAndCalculateCellData();
                    return true;
                }

                @Override
                public boolean onScaleBegin(ScaleGestureDetector detector) {
                    // Log.e(TAG, "onScaleBegin: " + detector.getFocusX());
                    //指头数量
                    if (mFingerPressedCount != 2) return true;
                    return true;
                }
            };

    /**
     * 主图上面的技术指标类型
     */
    public enum MasterType {
        NONE,//无
        MA,//MA5、10、20
        BOLL,//BOLL(26)
        MA_BOLL//MA5、10、20和BOLL(26)同时展示
    }

    /**
     * 主图上面的详细技术指标类型，主要用于判断何种具体的线，进行相应的处理
     */
    public enum MasterDetailType {
        MA5,
        MA10,
        MA20,
        BOLLMB,
        BOLLUP,
        BOLLDN
    }
}
