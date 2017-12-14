package wgyscsf.financialcustomerview.timesharing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import wgyscsf.financialcustomerview.BaseFinancialView;
import wgyscsf.financialcustomerview.R;
import wgyscsf.financialcustomerview.utils.FormatUtil;
import wgyscsf.financialcustomerview.utils.TimeUtils;

import static android.view.View.MeasureSpec.AT_MOST;

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
public class TimeSharingView extends BaseFinancialView {
    /**
     * 默认参数及常量
     */

    //右侧内边距，默认情况下结束点距离右边边距（单位：sp）
    public static final float DEF_INNER_RIGHT_BLANK_PADDING = 60;

    //加载更多阀值。当在左侧不可见范围内还剩多少数据时开始加载更多。（单位：数据个数）
    public static final int DEF_MINLEN_LOADMORE = 10;

    //长按阀值，默认多长时间算长按（ms）
    public static final long DEF_LONGPRESS_LENGTH = 700;
    //单击阀值
    public static final long DEF_CLICKPRESS_LENGTH = 300;

    //移动阀值。手指移动多远算移动的阀值（单位：sp）
    public static final long DEF_PULL_LENGTH = 5;

    //缩放最小值，该值理论上可以最小为3。为了美观，这个值不能太小，不然就成一条线了。不能定义为final,程序可能会对该值进行修改（容错）
    public static int DEF_SCALE_MINNUM = 30;
    //缩放最大值，该值最大理论上可为数据集合的大小
    public static int DEF_SCALE_MAXNUM = 300;

    /**
     * 各种画笔及其参数
     */

    //画笔:正在加载中
    Paint mLoadingPaint;
    final float mLoadingTextSize = 20;
    final String mLoadingText = "数据加载，请稍后";
    //是否显示loading,在入场的时候，数据还没有加载时进行显示。逻辑判断使用，不可更改
    boolean mDrawLoadingPaint = true;

    //画笔:最外面的上下左右的框
    Paint mOuterPaint;
    float mOuterLineWidth = 1;
    int mOuterLineColor;

    //画笔:内部xy轴虚线
    Paint mInnerXyPaint;
    float mInnerXyLineWidth = 1;
    int mInnerXyLineColor;
    //是否是虚线，可更改
    boolean mIsInnerXyLineDashed = true;

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


    /**
     * 其它属性
     */
    //上下左右padding，这里不再采用系统属性padding，因为用户容易忘记设置padding,直接在这里更改即可。
    float mPaddingTop = 20;
    float mPaddingBottom = 50;
    float mPaddingLeft = 8;
    float mPaddingRight = 90;

    //可见的显示的条数，屏幕上显示的并不是所有的数据，只是部分数据，这个数据就是“可见的条数”
    int mShownMaxCount = 30;


    // 注意，遵循取前不取后，因此mEndIndex这个点不应该取到,但是mBeginIndex会取到。
    //数据开始位置，数据集合的起始位置
    int mBeginIndex = 0;
    //数据的结束位置，这里之所以定义结束位置，因为数据可能会小于mShownMaxCount。
    int mEndIndex;
    //数据集合
    List<Quotes> mQuotesList;

    //默认情况下结束点距离右边边距
    float mInnerRightBlankPadding = DEF_INNER_RIGHT_BLANK_PADDING;
    //为了美观，容器内（边框内部的折线图距离外边框线的上下距离）上面有一定间距，下面也有一定的间距。
    float mInnerTopBlankPadding = 60;
    float mInnerBottomBlankPadding = 60;

    //默认产品小数位数
    int mDigits = 4;
    //每一个x、y轴的一个单元的的宽和高
    float mPerX;
    float mPerY;
    //Y轴：最小值和最大值对应的Model
    Quotes mMinQuotes;
    Quotes mMaxQuotes;
    //X轴:起始位置的时间和结束位置的时间
    Quotes mBeginQuotes;
    Quotes mEndQuotes;

    //事件监听回调
    TimeSharingListener mTimeSharingListener;
    //是否可以加载更多,出现这个属性的原因，防止多次加载更多，不可修改
    boolean mCanLoadMore = true;

    /**
     * 左右拖动思路：这里开始处理分时图的左右移动问题，思路：当手指移动时，会有移动距离（A），我们又有x轴的单位距离(B)，
     * 所以可以计算出来需要移动的个数（C=A/B,注意向上取整）。
     * 这个时候，就可以确定新的开始位置（D）和新的结束位置（E）：
     * D=mBeginIndex±C,E=mEndIndex干C，正负号取决于移动方向。
     */
    //手指按下的个数
    int mFingerPressedCount;
    //是否是向右拉，不可修改
    boolean mPullRight = false;
    //按下的x轴坐标
    float mPressedX;
    //按下的时刻
    long mPressTime;
    //手指移动的类型，默认在最后边
    PullType mPullType = PullType.PULL_RIGHT_STOP;


    /**
     * 缩放思路：所谓缩放，也是计算新的起始位置和结束位置。这里根据缩放因子detector.getScaleFactor()计算新的可见个数（x缩放因子即可）。
     * 当放大时，可见的数据集合的个数(A)应该减少。detector.getScaleFactor()(B的范围[1,2)),
     * 这个时候可以新的可见数据集合（C）可以考虑采用C=A-A*(B-1);当然这样计算是否准确，还需要商榷。
     * 思路简单，但是这里细节比较多，具体可以参考代码。see:mOnScaleGestureListener->onScale
     */

    ScaleGestureDetector mScaleGestureDetector;


    public TimeSharingView(Context context) {
        this(context, null);
    }

    public TimeSharingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeSharingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
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
        //默认加载loading界面
        showLoadingPaint(canvas);
        if (mQuotesList == null || mQuotesList.isEmpty()) {
            return;
        }
        drawOuterLine(canvas);
        drawInnerXy(canvas);
        drawXyTxt(canvas);
        drawBrokenLine(canvas);
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
        initLoadingPaint();
        initOuterPaint();
        initInnerXyPaint();
        initXyTxtPaint();
        initBrokenLinePaint();
        initBrokenLineBgPaint();
        initDotPaint();
        initTimingTxtPaint();
        initTimingLinePaint();
        initLongPressPaint();
        initLongPressTxtPaint();

        //手势
        mScaleGestureDetector = new ScaleGestureDetector(mContext, mOnScaleGestureListener);
    }

    protected void loadDefAttrs() {
        //数据源
        mQuotesList = new ArrayList<>(mShownMaxCount);
        //颜色
        mOuterLineColor = getColor(R.color.color_timeSharing_outerStrokeColor);
        mInnerXyLineColor = getColor(R.color.color_timeSharing_innerXyDashColor);
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
    }

    protected void initLoadingPaint() {
        mLoadingPaint = new Paint();
        mLoadingPaint.setColor(getColor(R.color.color_timeSharing_xYTxtColor));
        mLoadingPaint.setTextSize(mLoadingTextSize);
        mLoadingPaint.setAntiAlias(true);
    }

    protected void initOuterPaint() {
        mOuterPaint = new Paint();
        mOuterPaint.setColor(mOuterLineColor);
        mOuterPaint.setStrokeWidth(mOuterLineWidth);
    }

    protected void initInnerXyPaint() {
        mInnerXyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerXyPaint.setColor(mInnerXyLineColor);
        mInnerXyPaint.setStrokeWidth(mInnerXyLineWidth);
        mInnerXyPaint.setStyle(Paint.Style.STROKE);
        if (mIsInnerXyLineDashed) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            mInnerXyPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));
        }
    }

    protected void initXyTxtPaint() {
        mXYTxtPaint = new Paint();
        mXYTxtPaint.setColor(mXYTxtColor);
        mXYTxtPaint.setTextSize(mXYTxtSize);
        mXYTxtPaint.setAntiAlias(true);
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

    protected void showLoadingPaint(Canvas canvas) {
        if (!mDrawLoadingPaint) return;
        //这里特别注意，x轴的起始点要减去文字宽度的一半
        canvas.drawText(mLoadingText, mWidth / 2 - mLoadingPaint.measureText(mLoadingText) / 2,
                mHeight / 2, mLoadingPaint);
    }

    protected void drawOuterLine(Canvas canvas) {
        //先绘制x轴
        canvas.drawLine(mPaddingLeft, mPaddingTop,
                mWidth - mPaddingRight, mPaddingTop, mOuterPaint);
        canvas.drawLine(mPaddingLeft, mHeight - mPaddingBottom,
                mWidth - mPaddingRight, mHeight - mPaddingBottom, mOuterPaint);

        //绘制y轴
        canvas.drawLine(mPaddingLeft, mPaddingTop,
                mPaddingLeft, mHeight - mPaddingBottom, mOuterPaint);
        canvas.drawLine(mWidth - mPaddingRight, mPaddingTop,
                mWidth - mPaddingRight, mHeight - mPaddingBottom, mOuterPaint);
    }

    protected void drawInnerXy(Canvas canvas) {
        //先绘制x轴
        //计算每一段x的高度
        double perhight = (mHeight - mPaddingTop - mPaddingBottom) / 4;
        for (int i = 1; i <= 3; i++) {
            canvas.drawLine(mPaddingLeft, (float) (mPaddingTop + perhight * i),
                    mWidth - mPaddingRight, (float) (mPaddingTop + perhight * i),
                    mInnerXyPaint);
        }

        //绘制y轴
        double perWidth = (mWidth - mPaddingLeft - mPaddingRight) / 4;
        for (int i = 1; i <= 3; i++) {
            canvas.drawLine((float) (mPaddingLeft + perWidth * i), mPaddingTop,
                    (float) (mPaddingLeft + perWidth * i), mHeight - mPaddingBottom,
                    mInnerXyPaint);
        }
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
                mPerY * (quotes.c - mMinQuotes.c));
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
                    mPerY * (q.c - mMinQuotes.c));
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
                            mPerY * (endQuotes.c - mMinQuotes.c));
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
     * 这里的均值如何处理：只知道最小值和最大值（mMinQuotes,mMaxQuotes）,但是不是容器的上border和下border。
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
        double dataDis = mMaxQuotes.c - mMinQuotes.c;
        double yDis = (mHeight - mPaddingTop - mPaddingBottom - mInnerTopBlankPadding -
                mInnerBottomBlankPadding);
        double perY = dataDis / yDis;
        minBorderData = mMinQuotes.c - mInnerBottomBlankPadding * perY;
        maxBorderData = mMinQuotes.c + mInnerTopBlankPadding * perY;

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

    // 只需要把画笔颜色置为透明即可
    protected void hiddenLoadingPaint() {
        mLoadingPaint.setColor(0x00000000);
        mDrawLoadingPaint = false;
    }

    /**
     * 数据设置入口
     *
     * @param quotesList
     * @param timeSharingListener
     */
    public void setTimeSharingData(List<Quotes> quotesList, TimeSharingListener timeSharingListener) {
        //绑定监听
        mTimeSharingListener = timeSharingListener;
        //添加数据
        setTimeSharingData(quotesList);
    }

    /**
     * 数据设置入口
     *
     * @param quotesList
     */
    public void setTimeSharingData(List<Quotes> quotesList) {
        if (quotesList == null || quotesList.isEmpty()) {
            Toast.makeText(mContext, "数据异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "setTimeSharingData: 数据异常");
            return;
        }
        mQuotesList = quotesList;
        //数据过来，隐藏加载更多
        hiddenLoadingPaint();
        //开始处理数据
        mDigits = 4;

        //寻找开始位置和结束位置
        seekBeginAndEndByNewer();

        //寻找边界和计算单元数据大小
        seekAndCalculateCellData();
    }

    /**
     * 获取最新数据时（包括第一次进来）获取可见数据的开始位置和结束位置。来最新数据或者刚加载的时候，计算开始位置和结束位置。
     * 特别注意，最新的数据在最后面，所以数据范围应该是[(size-mShownMaxCount)~size)
     */
    protected void seekBeginAndEndByNewer() {
        int size = mQuotesList.size();
        if (size >= mShownMaxCount) {
            mBeginIndex = size - mShownMaxCount;
            mEndIndex = mBeginIndex + mShownMaxCount;
        } else {
            mBeginIndex = 0;
            mEndIndex = mBeginIndex + mQuotesList.size();
        }
    }

    /**
     * 实时推送过来的数据，实时更新
     *
     * @param quotes
     */
    public void pushingTimeSharingData(Quotes quotes) {
        if (quotes == null) {
            Toast.makeText(mContext, "数据异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "setTimeSharingData: 数据异常");
            return;
        }
        mQuotesList.add(quotes);
        //如果实在左右移动，则不去实时更新K线图，但是要把数据加进去
        if (mPullType == PullType.PULL_RIGHT_STOP) {
            //Log.e(TAG, "pushingTimeSharingData: 处理实时更新操作...");
            seekBeginAndEndByNewer();
            seekAndCalculateCellData();
        }
    }

    /**
     * 加载更多数据
     *
     * @param quotesList
     */
    public void loadMoreTimeSharingData(List<Quotes> quotesList) {
        if (quotesList == null || quotesList.isEmpty()) {
            Toast.makeText(mContext, "数据异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "setTimeSharingData: 数据异常");
            return;
        }
        mQuotesList.addAll(0, quotesList);

        //到这里就可以判断，加载更对成功了
        loadMoreSuccess();

        //特别特别注意，加载更多之后，不应该更新起始位置和结束位置，
        //因为可能在加载的过程中，原来的意图是在最左边，但是加载完毕后，又不在最左边了。
        // 因此，只要保持原来的起始位置和结束位置即可。【原来：指的是视觉上的原来】
        int addSize = quotesList.size();
        Log.e(TAG, "loadMoreTimeSharingData: 新来的数据大小：" + addSize);
        mBeginIndex = mBeginIndex + addSize;
        if (mBeginIndex + mShownMaxCount > mQuotesList.size()) {
            mBeginIndex = mQuotesList.size() - mShownMaxCount;
        }
        mEndIndex = mBeginIndex + mShownMaxCount;
        Log.e(TAG, "loadMoreTimeSharingData: 加载更多完毕，mBeginIndex：" + mBeginIndex + ",mEndIndex:" + mEndIndex);
        //重新测量一下,这里不能重新测量。因为重新测量的逻辑是寻找最新的点。
        //seekBeginAndEndByNewer();
        seekAndCalculateCellData();
    }

    /**
     * 加载更多失败，在这里添加逻辑
     */
    public void loadMoreError() {
        mCanLoadMore = true;
        Toast.makeText(mContext, "加载更多失败", Toast.LENGTH_SHORT).show();
    }

    /**
     * 加载更多成功，在这里添加逻辑
     */
    public void loadMoreSuccess() {
        mCanLoadMore = true;
        Toast.makeText(mContext, "加载更多成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 正在加载更多，在这里添加逻辑
     */
    public void loadMoreIng() {
        mCanLoadMore = false;
        Toast.makeText(mContext, "正在加载更多", Toast.LENGTH_SHORT).show();
    }

    /**
     * 正在加载没有更多数据，在这里添加逻辑
     */
    public void loadMoreNoData() {
        mCanLoadMore = false;
        Toast.makeText(mContext, "加载更多，没有数据了...", Toast.LENGTH_SHORT).show();
    }

    /**
     * 寻找边界和计算单元数据大小。寻找:x轴开始位置数据和结束位置的model、y轴的最大数据和最小数据对应的model；
     * 计算x/y轴数据单元大小
     */
    protected void seekAndCalculateCellData() {
        //找到最大值和最小值
        double tempMinClosePrice = Double.MAX_VALUE;
        double tempMaxClosePrice = Double.MIN_VALUE;

        for (int i = mBeginIndex; i < mEndIndex; i++) {
            Quotes quotes = mQuotesList.get(i);
            if (i == mBeginIndex) {
                mBeginQuotes = quotes;
            }
            if (i == mEndIndex - 1) {
                mEndQuotes = quotes;
            }
            if (quotes.c <= tempMinClosePrice) {
                tempMinClosePrice = quotes.c;
                mMinQuotes = quotes;
            }
            if (quotes.c >= tempMaxClosePrice) {
                tempMaxClosePrice = quotes.c;
                mMaxQuotes = quotes;
            }
        }
        mPerX = (mWidth - mPaddingLeft - mPaddingRight - mInnerRightBlankPadding)
                / (mShownMaxCount - 1);//特别注意，这里-1并不代表个数减少了，因为起始点是从0开始的。
        //不要忘了减去内部的上下Padding
        mPerY = (float) ((mHeight - mPaddingTop - mPaddingBottom - mInnerTopBlankPadding
                - mInnerBottomBlankPadding) / (mMaxQuotes.c - mMinQuotes.c));

        //刷新界面
        invalidate();
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

    //产品的小数位数
    public void setDigits(int digits) {
        mDigits = digits;
    }

    //监听回调
    interface TimeSharingListener {
        void onLongTouch(Quotes preQuotes, Quotes currentQuotes);

        void onUnLongTouch();

        void needLoadMore();
    }

    enum PullType {
        PULL_RIGHT,//向右滑动
        PULL_LEFT,//向左滑动
        PULL_RIGHT_STOP,//滑动到最右边
        PULL_LEFT_STOP,//滑动到最左边
    }
}
