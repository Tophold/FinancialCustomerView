package wgyscsf.financialcustomerview.financialview.kview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Toast;

import java.util.List;

import wgyscsf.financialcustomerview.financialview.BaseFinancialView;
import wgyscsf.financialcustomerview.R;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/12/15 15:11
 * 描 述 ：
 * ============================================================
 **/
public class KView extends BaseFinancialView {

    /**
     * 默认参数及常量
     */

    //右侧内边距，默认情况下结束点距离右边边距（单位：sp）
    protected static final float DEF_INNER_RIGHT_BLANK_PADDING = 60;

    //加载更多阀值。当在左侧不可见范围内还剩多少数据时开始加载更多。（单位：数据个数）
    protected static final int DEF_MINLEN_LOADMORE = 10;

    //移动阀值。手指移动多远算移动的阀值（单位：sp）
    protected static final long DEF_PULL_LENGTH = 5;

    //缩放最小值，该值理论上可以最小为3。为了美观，这个值不能太小，不然就成一条线了。不能定义为final,程序可能会对该值进行修改（容错）
    protected static int DEF_SCALE_MINNUM = 10;
    //缩放最大值，该值最大理论上可为数据集合的大小
    protected static int DEF_SCALE_MAXNUM = 300;

    //上下左右padding，这里不再采用系统属性padding，因为用户容易忘记设置padding,直接在这里更改即可。
    protected float mPaddingTop = 20;
    protected float mPaddingBottom = 50;
    protected float mPaddingLeft = 8;
    protected float mPaddingRight = 90;

    //可见的显示的条数，屏幕上显示的并不是所有的数据，只是部分数据，这个数据就是“可见的条数”
    protected int mShownMaxCount = 30;
    //默认产品小数位数
    protected int mDigits = 4;

    // 注意，遵循取前不取后，因此mEndIndex这个点不应该取到,但是mBeginIndex会取到。
    //数据开始位置，数据集合的起始位置
    protected int mBeginIndex = 0;
    //数据的结束位置，这里之所以定义结束位置，因为数据可能会小于mShownMaxCount。
    protected int mEndIndex;
    //数据集合
    protected List<Quotes> mQuotesList;

    //默认情况下结束点距离右边边距
    protected float mInnerRightBlankPadding = DEF_INNER_RIGHT_BLANK_PADDING;
    //为了美观，容器内（边框内部的折线图距离外边框线的上下距离）上面有一定间距，下面也有一定的间距。
    protected float mInnerTopBlankPadding = 60;
    protected float mInnerBottomBlankPadding = 60;


    //事件监听回调
    protected TimeSharingListener mTimeSharingListener;
    //是否可以加载更多,出现这个属性的原因，防止多次加载更多，不可修改
    protected boolean mCanLoadMore = true;

    //view类型：是分时图还是蜡烛图
    protected ViewType mViewType=ViewType.TIMESHARING;

    /**
     * 绘制分时图：
     */

    //每一个x、y轴的一个单元的的宽和高
    //根据可见数量和有效宽度计算单位x大小
    protected float mPerX;
    //根据最大最小close价格计算的的单位y大小。
    protected float mClosePerY;
    //Y轴：close价格最小值和最大值对应的Model。这里最小最大是根据close价格算的，用于分时图。
    protected Quotes mMinColseQuotes;
    protected Quotes mMaxCloseQuotes;
    //X轴:起始位置的时间和结束位置的时间
    protected Quotes mBeginQuotes;
    protected Quotes mEndQuotes;


    /**
     * 绘制蜡烛图：y轴，可以根据可视范围内的最大high值（A）和最小low值（B）以及有效y轴高度（C）计算出
     * 单位高度mPerY(D),D=C/(A-B)。
     * x轴暂时直接先取mPerX作为宽度，不留间隙。
     * 那么，蜡烛图就可以根据当前位置的high、low两个值绘制最大最小值；
     * 然后根据open和close绘制蜡烛图的上起点和下结束点。
     * 至于颜色，当当前值为的Quote的close大于open,为红色；反之为绿色。
     *
     */
    //根据可见范围内最大的high价格和最小的low价格计算的y单位长度
    protected float mPerY;
    //Y轴：根据可见范围内最大的high价格和最小的low价格分别对应的model
    protected Quotes mMaxHighQuotes;
    protected Quotes mMinLowQuotes;
    //蜡烛图间隙，大小以单个蜡烛图的宽度的比例算。可修改。
    protected float mCandleDiverWidthRatio=0.1f;

    /**
     * 左右拖动思路：这里开始处理分时图的左右移动问题，思路：当手指移动时，会有移动距离（A），我们又有x轴的单位距离(B)，
     * 所以可以计算出来需要移动的个数（C=A/B,注意向上取整）。
     * 这个时候，就可以确定新的开始位置（D）和新的结束位置（E）：
     * D=mBeginIndex±C,E=mEndIndex干C，正负号取决于移动方向。
     */
    //手指按下的个数
    protected int mFingerPressedCount;
    //是否是向右拉，不可修改
    protected boolean mPullRight = false;
    //按下的x轴坐标
    protected float mPressedX;
    //按下的时刻
    protected long mPressTime;
    //手指移动的类型，默认在最后边
    protected PullType mPullType = PullType.PULL_RIGHT_STOP;


    /**
     * 缩放思路：所谓缩放，也是计算新的起始位置和结束位置。这里根据缩放因子detector.getScaleFactor()计算新的可见个数（x缩放因子即可）。
     * 当放大时，可见的数据集合的个数(A)应该减少。detector.getScaleFactor()(B的范围[1,2)),
     * 这个时候可以新的可见数据集合（C）可以考虑采用C=A-A*(B-1);当然这样计算是否准确，还需要商榷。
     * 思路简单，但是这里细节比较多，具体可以参考代码。see:mOnScaleGestureListener->onScale
     */

    protected ScaleGestureDetector mScaleGestureDetector;


    /**
     * 画笔
     */
    //画笔:正在加载中
    protected Paint mLoadingPaint;
    protected float mLoadingTextSize = 20;
    protected int mLoadingTextColor;
    protected final String mLoadingText = "数据加载，请稍后";
    //是否显示loading,在入场的时候，数据还没有加载时进行显示。逻辑判断使用，不可更改
    protected boolean mDrawLoadingPaint = true;

    //画笔:最外面的上下左右的框
    protected Paint mOuterPaint;
    protected float mOuterLineWidth = 1;
    protected int mOuterLineColor;

    //画笔:内部xy轴虚线
    protected Paint mInnerXyPaint;
    protected float mInnerXyLineWidth = 1;
    protected int mInnerXyLineColor;
    //是否是虚线，可更改
    protected boolean mIsInnerXyLineDashed = true;
    //是否显示内部x虚线
    protected boolean mIsShowInnerX = true;
    //是否显示内部y虚线
    protected boolean mIsShowInnerY = true;


    public KView(Context context) {
        this(context, null);
    }

    public KView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        //默认加载loading界面
        showLoadingPaint(canvas);
        if (mQuotesList == null || mQuotesList.isEmpty()) {
            return;
        }
        drawOuterLine(canvas);
        drawInnerXy(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    private void initAttrs() {
        initColorRes();
        initPaintRes();
    }

    private void initColorRes() {
        mOuterLineColor = getColor(R.color.color_kview_outerStrokeColor);
        mLoadingTextColor = getColor(R.color.color_kview_loadingTxtColor);
        mInnerXyLineColor = getColor(R.color.color_kview_innerXyDashColor);
    }

    private void initPaintRes() {
        initLoadingPaint();
        initOuterPaint();
        initInnerXyPaint();
    }

    protected void initLoadingPaint() {
        mLoadingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLoadingPaint.setColor(mLoadingTextColor);
        mLoadingPaint.setTextSize(mLoadingTextSize);
    }


    protected void initOuterPaint() {
        mOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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
        if (isShowInnerX())
            drawInnerX(canvas);
        if (isShowInnerY())
            drawInnerY(canvas);
    }

    private void drawInnerY(Canvas canvas) {
        //绘制y轴
        double perWidth = (mWidth - mPaddingLeft - mPaddingRight) / 4;
        for (int i = 1; i <= 3; i++) {
            canvas.drawLine((float) (mPaddingLeft + perWidth * i), mPaddingTop,
                    (float) (mPaddingLeft + perWidth * i), mHeight - mPaddingBottom,
                    mInnerXyPaint);
        }
    }

    private void drawInnerX(Canvas canvas) {
        //先绘制x轴
        //计算每一段x的高度
        double perhight = (mHeight - mPaddingTop - mPaddingBottom) / 4;
        for (int i = 1; i <= 3; i++) {
            canvas.drawLine(mPaddingLeft, (float) (mPaddingTop + perhight * i),
                    mWidth - mPaddingRight, (float) (mPaddingTop + perhight * i),
                    mInnerXyPaint);
        }
    }

    // 只需要把画笔颜色置为透明即可
    protected void hiddenLoadingPaint() {
        mLoadingPaint.setColor(0x00000000);
        mDrawLoadingPaint = false;
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

    //产品的小数位数
    public void setDigits(int digits) {
        mDigits = digits;
    }

    protected enum PullType {
        PULL_RIGHT,//向右滑动
        PULL_LEFT,//向左滑动
        PULL_RIGHT_STOP,//滑动到最右边
        PULL_LEFT_STOP,//滑动到最左边
    }

    public enum ViewType{
        TIMESHARING,
        CANDLE
    }

    //监听回调
    public interface TimeSharingListener {
        void onLongTouch(Quotes preQuotes, Quotes currentQuotes);

        void onUnLongTouch();

        void needLoadMore();
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
     * 寻找边界和计算单元数据大小。寻找:x轴开始位置数据和结束位置的model、y轴的最大数据和最小数据对应的model；
     * 计算x/y轴数据单元大小
     */
    protected void seekAndCalculateCellData() {
        //找到close最大值和最小值
        double tempMinClosePrice = Integer.MAX_VALUE;
        double tempMaxClosePrice = Integer.MIN_VALUE;

        //找到可见范围之内的最大的high价格和最小的low价格
        double tempMaxHighPrice=Integer.MIN_VALUE;
        double tempMinLowPrice=Integer.MAX_VALUE;

        //最终确定的最大high值和最小low值
        double finalMaxHighPrice=mQuotesList.get(mBeginIndex).h;
        double finalMinLowPrice=mQuotesList.get(mBeginIndex).l;
        mMaxHighQuotes=mQuotesList.get(mBeginIndex);
        mMinLowQuotes=mQuotesList.get(mBeginIndex);

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
                mMinColseQuotes = quotes;
            }
            if (quotes.c >= tempMaxClosePrice) {
                tempMaxClosePrice = quotes.c;
                mMaxCloseQuotes = quotes;
            }

            //蜡烛图
            if (mViewType==ViewType.CANDLE){
                if(quotes.h>tempMaxHighPrice){
                    tempMaxHighPrice=quotes.h;
                    finalMaxHighPrice=tempMaxHighPrice;
                    mMaxHighQuotes=quotes;
                }

                if(quotes.l<tempMinLowPrice){
                    tempMinLowPrice=quotes.l;
                    finalMinLowPrice=tempMinLowPrice;
                    mMinLowQuotes=quotes;
                }
            }
        }
        mPerX = (mWidth - mPaddingLeft - mPaddingRight - mInnerRightBlankPadding)
                / (mShownMaxCount - 1);//特别注意，这里-1并不代表个数减少了，因为起始点是从0开始的。
        //不要忘了减去内部的上下Padding
        mClosePerY = (float) ((mHeight - mPaddingTop - mPaddingBottom - mInnerTopBlankPadding
                - mInnerBottomBlankPadding) / (mMaxCloseQuotes.c - mMinColseQuotes.c));

        if(mViewType==ViewType.CANDLE){
            mPerY=(float) ((mHeight - mPaddingTop - mPaddingBottom - mInnerTopBlankPadding
                    - mInnerBottomBlankPadding) / (finalMaxHighPrice- finalMinLowPrice));
        }


        //刷新界面
        invalidate();
    }

    public boolean isShowInnerX() {
        return mIsShowInnerX;
    }

    public void setShowInnerX(boolean showInnerX) {
        mIsShowInnerX = showInnerX;
    }

    public boolean isShowInnerY() {
        return mIsShowInnerY;
    }

    public void setShowInnerY(boolean showInnerY) {
        mIsShowInnerY = showInnerY;
    }

    public ViewType getViewType() {
        return mViewType;
    }

    public void setViewType(ViewType viewType) {
        mViewType = viewType;
    }
}
