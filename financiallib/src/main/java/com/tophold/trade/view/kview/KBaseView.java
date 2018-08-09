package com.tophold.trade.view.kview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.tophold.trade.R;
import com.tophold.trade.utils.StringUtils;
import com.tophold.trade.view.BaseView;


/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 更细日期 ：2018/01/14 12:03
 * 描 述 ：该View是KView的父类。
 * 提供各种基础能力，包括边框、边距、内虚线以及其它公共参数的初始化操作。
 * ============================================================
 **/
public abstract class KBaseView extends BaseView {

    /**
     * 默认参数及常量
     */
    //右侧内边距，默认情况下结束点距离右边边距（单位：sp）
    protected static float DEF_INNER_RIGHT_BLANK_PADDING = 60;

    //加载更多阀值。当在左侧不可见范围内还剩多少数据时开始加载更多。（单位：数据个数）
    protected static int def_minlen_loadmore = 10;

    //缩放最小值，该值理论上可以最小为3。为了美观，这个值不能太小，不然就成一条线了。不能定义为final,程序可能会对该值进行修改（容错）
    protected static int def_scale_minnum = 10;
    //缩放最大值，该值最大理论上可为数据集合的大小
    protected static int def_scale_maxnum = 300;

    //上下左右padding，这里不再采用系统属性padding，因为用户容易忘记设置padding,直接在这里更改即可。
    protected float mBasePaddingTop = 20;
    protected float mBasePaddingBottom = 10;
    protected float mBasePaddingLeft = 8;
    protected float mBasePaddingRight = 90;

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
    protected KViewListener.MasterTouchListener mMasterTouchListener;
    //是否可以加载更多,出现这个属性的原因，防止多次加载更多，不可修改
    protected boolean mCanLoadMore = true;


    /**
     * 绘制分时图：
     */

    //每一个x、y轴的一个单元的的宽和高
    //根据可见数量和有效宽度计算单位x大小
    protected float mPerX;
    //根据最大最小close价格计算的的单位y大小。该参数已废弃，统一采用mPerY。
    //protected float mClosePerY;
    //该参数的具体逻辑由子类去实现
    protected float mPerY;

    //X轴:起始位置的时间和结束位置的时间
    protected Quotes mBeginQuotes;
    protected Quotes mEndQuotes;


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
    //按下的x、y轴坐标
    protected float mPressedX;
    protected float mPressedY;
    //按下的时刻
    protected long mPressTime;
    //手指移动的类型，默认在最后边
    protected KViewType.PullType mPullType = KViewType.PullType.PULL_RIGHT_STOP;


    /**
     * 缩放思路：所谓缩放，也是计算新的起始位置和结束位置。这里根据缩放因子detector.getScaleFactor()计算新的可见个数（x缩放因子即可）。
     * 当放大时，可见的数据集合的个数(A)应该减少。detector.getScaleFactor()(B的范围[1,2)),
     * 这个时候可以新的可见数据集合（C）可以考虑采用C=A-A*(B-1);当然这样计算是否准确，还需要商榷。
     * 思路简单，但是这里细节比较多，具体可以参考代码。see:mOnScaleGestureListener->onScale
     */

    protected ScaleGestureDetector mScaleGestureDetector;

    protected GestureDetectorCompat mFlingDetector;

    //移动的模式：是一点一点移动还是具有onFling()效果？现在onfling效果有点问题
    protected KViewType.MoveType mMoveType = KViewType.MoveType.STEP;


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

    //画笔:外围X、Y轴线文字
    protected Paint mXYTxtPaint;
    //x、y轴指示文字字体的大小
    protected final float mXYTxtSize = 14;
    protected int mXYTxtColor;
    //右侧文字距离右边线线的距离
    protected final float mRightTxtPadding = 4;
    //底部文字距离底部线的距离
    protected final float mBottomTxtPadding = 20;


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

    //画笔：非长按下主图指标图例
    protected Paint mLegendPaint;
    protected int mLegendColor;
    //距离上方border的距离(单位：px)
    protected double mLegendPaddingTop = 30;
    //这个是文字框的结束位置距离右侧的距离
    protected double mLegendPaddingRight = 10;
    protected float mLegendTxtSize = 15;
    protected int mLineWidth = 1;

    //长按下主图指标图例
    //这个是文字框的结束位置距离右侧的距离
    protected double mLegendPaddingLeft = 10;
    //同时显示ma和boll上下的间距
    protected double mLegendTxtTopPadding = 0;


    //是否绘制长按十字，逻辑判断使用，不可更改
    protected boolean mDrawLongPress = false;

    //滑动点的x轴坐标，滑动使用，记录当前滑动点的x坐标
    protected float mMovingX;

    //画笔:长按的十字线
    protected Paint mLongPressPaint;
    protected int mLongPressColor;
    protected float mLongPressLineWidth = 1.5f;
    //长按对应的对象
    protected Quotes mCurrLongPressQuotes;


    public KBaseView(Context context) {
        this(context, null);
    }

    public KBaseView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KBaseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
//        showLoadingPaint(canvas);
//        if (mQuotesList == null || mQuotesList.isEmpty()) {
//            return;
//        }
        drawOuterLine(canvas);
        drawInnerXy(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //按下的手指个数
        mFingerPressedCount = event.getPointerCount();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPressedX = event.getX();
                mPressTime = event.getDownTime();
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getEventTime() - mPressTime > def_longpress_length) {
                    innerLongClickListener(event.getX(), event.getY());
                }
                //判断是否是手指移动
                float currentPressedX = event.getX();
                float moveLen = currentPressedX - mPressedX;
                //重置当前按下的位置
                mPressedX = currentPressedX;
                if (Math.abs(moveLen) > def_pull_length &&
                        mFingerPressedCount == 1 &&
                        !mDrawLongPress &&
                        mMoveType == KViewType.MoveType.STEP) {
                    //移动k线图
                    innerMoveViewListener(moveLen);
                }
                break;
            case MotionEvent.ACTION_UP:
                //单击事件
                if (event.getEventTime() - mPressTime < def_clickpress_length) {
                    //单击并且是在绘制十字
                    if (mDrawLongPress) {
                        //取消掉长按十字
                        innerHiddenLongClick();
                    } else {
                        //响应单击事件
                        innerClickListener();//这个事件传递下去
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void initAttrs() {
        initColorRes();
        initPaintRes();
        initData();
    }

    private void initData() {
        mQuotesList=new ArrayList<>();
    }

    private void initColorRes() {
        mOuterLineColor = getColor(R.color.color_kview_outerStrokeColor);
        mLoadingTextColor = getColor(R.color.color_kview_loadingTxtColor);
        mInnerXyLineColor = getColor(R.color.color_kview_innerXyDashColor);
        mXYTxtColor = getColor(R.color.color_timeSharing_xYTxtColor);
        mLegendColor = getColor(R.color.color_masterView_legendColor);
        mLongPressColor = getColor(R.color.color_timeSharing_longPressLineColor);
    }

    private void initPaintRes() {
        initLoadingPaint();
        initOuterPaint();
        initInnerXyPaint();
        initXyTxtPaint();
        initLegendPaint();
        initLongPressPaint();
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

    protected void initXyTxtPaint() {
        mXYTxtPaint = new Paint();
        mXYTxtPaint.setColor(mXYTxtColor);
        mXYTxtPaint.setTextSize(mXYTxtSize);
        mXYTxtPaint.setAntiAlias(true);
    }

    private void initLegendPaint() {
        mLegendPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLegendPaint.setColor(mLegendColor);
        mLegendPaint.setStrokeWidth(mLineWidth);
        mLegendPaint.setTextSize(mLegendTxtSize);
    }

    protected void initLongPressPaint() {
        mLongPressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLongPressPaint.setColor(mLongPressColor);
        mLongPressPaint.setStrokeWidth(mLongPressLineWidth);
        mLongPressPaint.setStyle(Paint.Style.STROKE);
    }

    protected void showLoadingPaint(Canvas canvas) {
        if (!mDrawLoadingPaint) return;
        //这里特别注意，x轴的起始点要减去文字宽度的一半
        canvas.drawText(mLoadingText, mBaseWidth / 2 - mLoadingPaint.measureText(mLoadingText) / 2,
                mBaseHeight / 2, mLoadingPaint);
    }

    protected void drawOuterLine(Canvas canvas) {
        //先绘制x轴
        canvas.drawLine(mBasePaddingLeft, mBasePaddingTop,
                mBaseWidth - mBasePaddingRight, mBasePaddingTop, mOuterPaint);
        canvas.drawLine(mBasePaddingLeft, mBaseHeight - mBasePaddingBottom,
                mBaseWidth - mBasePaddingRight, mBaseHeight - mBasePaddingBottom, mOuterPaint);

        //绘制y轴
        canvas.drawLine(mBasePaddingLeft, mBasePaddingTop,
                mBasePaddingLeft, mBaseHeight - mBasePaddingBottom, mOuterPaint);
        canvas.drawLine(mBaseWidth - mBasePaddingRight, mBasePaddingTop,
                mBaseWidth - mBasePaddingRight, mBaseHeight - mBasePaddingBottom, mOuterPaint);
    }

    /**
     * 绘制内部x/y轴虚线
     *
     * @param canvas
     */
    protected void drawInnerXy(Canvas canvas) {
        if (isShowInnerX())
            drawInnerX(canvas);
        if (isShowInnerY())
            drawInnerY(canvas);
    }

    private void drawInnerY(Canvas canvas) {
        //绘制y轴
        double perWidth = (mBaseWidth - mBasePaddingLeft - mBasePaddingRight) / 4;
        for (int i = 1; i <= 3; i++) {
            canvas.drawLine((float) (mBasePaddingLeft + perWidth * i), mBasePaddingTop,
                    (float) (mBasePaddingLeft + perWidth * i), mBaseHeight - mBasePaddingBottom,
                    mInnerXyPaint);
        }
    }

    private void drawInnerX(Canvas canvas) {
        //先绘制x轴
        //计算每一段x的高度
        double perhight = (mBaseHeight - mBasePaddingTop - mBasePaddingBottom) / 4;
        for (int i = 1; i <= 3; i++) {
            canvas.drawLine(mBasePaddingLeft, (float) (mBasePaddingTop + perhight * i),
                    mBaseWidth - mBasePaddingRight, (float) (mBasePaddingTop + perhight * i),
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
     * 没有更多数据，在这里添加逻辑
     */
    public void loadMoreNoData() {
        mCanLoadMore = false;
        Toast.makeText(mContext, "加载更多，没有数据了...", Toast.LENGTH_SHORT).show();
    }


    /**
     * 数据设置入口
     *
     * @param quotesList
     * @param masterTouchListener
     */
    public void setTimeSharingData(List<Quotes> quotesList, KViewListener.MasterTouchListener masterTouchListener) {
        //绑定监听
        mMasterTouchListener = masterTouchListener;
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
            Log.e(TAG, "setKViewData: 数据异常");
            return;
        }
        mQuotesList.addAll(quotesList);
        //数据过来，隐藏加载更多
        hiddenLoadingPaint();

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
     * @param period 对应的数据的类型 ms
     */
    public void pushingTimeSharingData(Quotes quotes, long period) {
        if (quotes == null) {
            Toast.makeText(mContext, "数据异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "setKViewData: 数据异常");
            return;
        }
        if (StringUtils.isEmpty(mQuotesList)) return;


        /**
         * 对于新推送过来的数据，需要判断和列表中最新的数据的时间差进行对比。对比和View的类型有关，比如1min的
         * 时间差是60s，如果小于60s,就更新最新一条的最大值和最小值；否则添加进去。
         */
        Quotes q = mQuotesList.get(mQuotesList.size() - 1);
        if (quotes.t < q.t) return;
        if (period != 0 && ((quotes.t - q.t) / 1000.0) <= period / 1000) {
            //这个时候替换最后一个原有数据
            q.c = quotes.c;//绘制分时图是根据闭市价格算的

            //判断最大值和最小值，及时更新
            if (quotes.c > q.h) q.h = quotes.c;
            if (quotes.c < q.l) q.l = quotes.c;


            mQuotesList.set(mQuotesList.size() - 1, q);
        } else {
            //new ?Add it!
            mQuotesList.add(quotes);
        }


        //如果是在左右移动，则不去实时更新K线图，但是要把数据加进去
        if (mPullType == KViewType.PullType.PULL_RIGHT_STOP) {
            //Log.e(TAG, "pushKViewData: 处理实时更新操作...");
            seekBeginAndEndByNewer();
            seekAndCalculateCellData();
        }
    }

    /**
     * 加载更多数据。所谓加载更多数据，加载的是历史数据。而整个数据是从左到右依次为旧的->新的，所以新过来的数据是旧的，
     * 要插入到最左边。而这个时候，应该要保持mBeginIndex和mEndIndx和插入之前一致（不然k线会突然闪动一下）。
     *
     *
     * @param quotesList
     */
    public void loadMoreTimeSharingData(List<Quotes> quotesList) {
        if (quotesList == null || quotesList.isEmpty()) {
            Toast.makeText(mContext, "数据异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "setKViewData: 数据异常");
            return;
        }
        mQuotesList.addAll(0, quotesList);

        //到这里就可以判断，加载更对成功了
        loadMoreSuccess();

        //特别特别注意，加载更多之后，不应该更新起始位置和结束位置，
        //因为可能在加载的过程中，原来的意图是在最左边，但是加载完毕后，又不在最左边了。
        //因此，只要保持原来的起始位置和结束位置即可。【原来：指的是视觉上的原来】
        int addSize = quotesList.size();
        mBeginIndex += addSize;
        mEndIndex += addSize;

        //重新计算perx\pery等
        seekAndCalculateCellData();
    }


    /**
     * 寻找边界和计算单元数据大小。寻找:x轴开始位置数据和结束位置的model、y轴的最大数据和最小数据对应的model；
     * 计算x/y轴数据单元大小。这个交给子类去实现，一般情况下寻找边界需要遍历，在父类中遍历没有意义，
     * 因为不知道子类还有什么遍历需求。因此改为抽象方法，子类实现。子类必须完成寻找边界的任务。
     */
    protected abstract void seekAndCalculateCellData();

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

    protected void innerClickListener() {
    }

    protected void innerHiddenLongClick() {

    }

    protected void innerMoveViewListener(float moveXLen) {
    }

    protected void innerLongClickListener(float x, float y) {
    }


    //-----------------------对开发者暴露可以修改的参数-------


    public static int getDef_minlen_loadmore() {
        return def_minlen_loadmore;
    }

    public static void setDef_minlen_loadmore(int def_minlen_loadmore) {
        KBaseView.def_minlen_loadmore = def_minlen_loadmore;
    }

    public static int getDef_scale_minnum() {
        return def_scale_minnum;
    }

    public static void setDef_scale_minnum(int def_scale_minnum) {
        KBaseView.def_scale_minnum = def_scale_minnum;
    }

    public static int getDef_scale_maxnum() {
        return def_scale_maxnum;
    }

    public static void setDef_scale_maxnum(int def_scale_maxnum) {
        KBaseView.def_scale_maxnum = def_scale_maxnum;
    }

    public float getBasePaddingTop() {
        return mBasePaddingTop;
    }

    public KBaseView setBasePaddingTop(float basePaddingTop) {
        mBasePaddingTop = basePaddingTop;
        return this;
    }

    public float getBasePaddingBottom() {
        return mBasePaddingBottom;
    }

    public KBaseView setBasePaddingBottom(float basePaddingBottom) {
        mBasePaddingBottom = basePaddingBottom;
        return this;
    }

    public float getBasePaddingLeft() {
        return mBasePaddingLeft;
    }

    public KBaseView setBasePaddingLeft(float basePaddingLeft) {
        mBasePaddingLeft = basePaddingLeft;
        return this;
    }

    public float getBasePaddingRight() {
        return mBasePaddingRight;
    }

    public KBaseView setBasePaddingRight(float basePaddingRight) {
        mBasePaddingRight = basePaddingRight;
        return this;
    }

    public int getShownMaxCount() {
        return mShownMaxCount;
    }

    public KBaseView setShownMaxCount(int shownMaxCount) {
        mShownMaxCount = shownMaxCount;
        return this;
    }

    public int getDigits() {
        return mDigits;
    }

    public KBaseView setDigits(int digits) {
        mDigits = digits;
        return this;
    }

    public int getBeginIndex() {
        return mBeginIndex;
    }

    public KBaseView setBeginIndex(int beginIndex) {
        mBeginIndex = beginIndex;
        return this;
    }

    public int getEndIndex() {
        return mEndIndex;
    }

    public KBaseView setEndIndex(int endIndex) {
        mEndIndex = endIndex;
        return this;
    }

    public List<Quotes> getQuotesList() {
        return mQuotesList;
    }

    public KBaseView setQuotesList(List<Quotes> quotesList) {
        mQuotesList = quotesList;
        return this;
    }

    public float getInnerRightBlankPadding() {
        return mInnerRightBlankPadding;
    }

    public KBaseView setInnerRightBlankPadding(float innerRightBlankPadding) {
        mInnerRightBlankPadding = innerRightBlankPadding;
        return this;
    }

    public float getInnerTopBlankPadding() {
        return mInnerTopBlankPadding;
    }

    public KBaseView setInnerTopBlankPadding(float innerTopBlankPadding) {
        mInnerTopBlankPadding = innerTopBlankPadding;
        return this;
    }

    public float getInnerBottomBlankPadding() {
        return mInnerBottomBlankPadding;
    }

    public KBaseView setInnerBottomBlankPadding(float innerBottomBlankPadding) {
        mInnerBottomBlankPadding = innerBottomBlankPadding;
        return this;
    }

    public KViewListener.MasterTouchListener getMasterTouchListener() {
        return mMasterTouchListener;
    }

    public KBaseView setMasterTouchListener(KViewListener.MasterTouchListener masterTouchListener) {
        mMasterTouchListener = masterTouchListener;
        return this;
    }

    public boolean isCanLoadMore() {
        return mCanLoadMore;
    }

    public KBaseView setCanLoadMore(boolean canLoadMore) {
        mCanLoadMore = canLoadMore;
        return this;
    }

    public float getPerX() {
        return mPerX;
    }

    public KBaseView setPerX(float perX) {
        mPerX = perX;
        return this;
    }

    public float getPerY() {
        return mPerY;
    }

    public KBaseView setPerY(float perY) {
        mPerY = perY;
        return this;
    }

    public Quotes getBeginQuotes() {
        return mBeginQuotes;
    }

    public KBaseView setBeginQuotes(Quotes beginQuotes) {
        mBeginQuotes = beginQuotes;
        return this;
    }

    public Quotes getEndQuotes() {
        return mEndQuotes;
    }

    public KBaseView setEndQuotes(Quotes endQuotes) {
        mEndQuotes = endQuotes;
        return this;
    }

    public int getFingerPressedCount() {
        return mFingerPressedCount;
    }

    public KBaseView setFingerPressedCount(int fingerPressedCount) {
        mFingerPressedCount = fingerPressedCount;
        return this;
    }

    public boolean isPullRight() {
        return mPullRight;
    }

    public KBaseView setPullRight(boolean pullRight) {
        mPullRight = pullRight;
        return this;
    }

    public float getPressedX() {
        return mPressedX;
    }

    public KBaseView setPressedX(float pressedX) {
        mPressedX = pressedX;
        return this;
    }

    public float getPressedY() {
        return mPressedY;
    }

    public KBaseView setPressedY(float pressedY) {
        mPressedY = pressedY;
        return this;
    }

    public long getPressTime() {
        return mPressTime;
    }

    public KBaseView setPressTime(long pressTime) {
        mPressTime = pressTime;
        return this;
    }

    public KViewType.PullType getPullType() {
        return mPullType;
    }

    public KBaseView setPullType(KViewType.PullType pullType) {
        mPullType = pullType;
        return this;
    }

    public ScaleGestureDetector getScaleGestureDetector() {
        return mScaleGestureDetector;
    }

    public KBaseView setScaleGestureDetector(ScaleGestureDetector scaleGestureDetector) {
        mScaleGestureDetector = scaleGestureDetector;
        return this;
    }

    public Paint getLoadingPaint() {
        return mLoadingPaint;
    }

    public KBaseView setLoadingPaint(Paint loadingPaint) {
        mLoadingPaint = loadingPaint;
        return this;
    }

    public float getLoadingTextSize() {
        return mLoadingTextSize;
    }

    public KBaseView setLoadingTextSize(float loadingTextSize) {
        mLoadingTextSize = loadingTextSize;
        return this;
    }

    public int getLoadingTextColor() {
        return mLoadingTextColor;
    }

    public KBaseView setLoadingTextColor(int loadingTextColor) {
        mLoadingTextColor = loadingTextColor;
        return this;
    }

    public String getLoadingText() {
        return mLoadingText;
    }

    public boolean isDrawLoadingPaint() {
        return mDrawLoadingPaint;
    }

    public KBaseView setDrawLoadingPaint(boolean drawLoadingPaint) {
        mDrawLoadingPaint = drawLoadingPaint;
        return this;
    }

    public Paint getOuterPaint() {
        return mOuterPaint;
    }

    public KBaseView setOuterPaint(Paint outerPaint) {
        mOuterPaint = outerPaint;
        return this;
    }

    public float getOuterLineWidth() {
        return mOuterLineWidth;
    }

    public KBaseView setOuterLineWidth(float outerLineWidth) {
        mOuterLineWidth = outerLineWidth;
        return this;
    }

    public int getOuterLineColor() {
        return mOuterLineColor;
    }

    public KBaseView setOuterLineColor(int outerLineColor) {
        mOuterLineColor = outerLineColor;
        return this;
    }

    public Paint getXYTxtPaint() {
        return mXYTxtPaint;
    }

    public KBaseView setXYTxtPaint(Paint XYTxtPaint) {
        mXYTxtPaint = XYTxtPaint;
        return this;
    }

    public float getXYTxtSize() {
        return mXYTxtSize;
    }

    public int getXYTxtColor() {
        return mXYTxtColor;
    }

    public KBaseView setXYTxtColor(int XYTxtColor) {
        mXYTxtColor = XYTxtColor;
        return this;
    }

    public float getRightTxtPadding() {
        return mRightTxtPadding;
    }

    public float getBottomTxtPadding() {
        return mBottomTxtPadding;
    }

    public Paint getInnerXyPaint() {
        return mInnerXyPaint;
    }

    public KBaseView setInnerXyPaint(Paint innerXyPaint) {
        mInnerXyPaint = innerXyPaint;
        return this;
    }

    public float getInnerXyLineWidth() {
        return mInnerXyLineWidth;
    }

    public KBaseView setInnerXyLineWidth(float innerXyLineWidth) {
        mInnerXyLineWidth = innerXyLineWidth;
        return this;
    }

    public int getInnerXyLineColor() {
        return mInnerXyLineColor;
    }

    public KBaseView setInnerXyLineColor(int innerXyLineColor) {
        mInnerXyLineColor = innerXyLineColor;
        return this;
    }

    public boolean isInnerXyLineDashed() {
        return mIsInnerXyLineDashed;
    }

    public KBaseView setInnerXyLineDashed(boolean innerXyLineDashed) {
        mIsInnerXyLineDashed = innerXyLineDashed;
        return this;
    }

    public Paint getLegendPaint() {
        return mLegendPaint;
    }

    public KBaseView setLegendPaint(Paint legendPaint) {
        mLegendPaint = legendPaint;
        return this;
    }

    public int getLegendColor() {
        return mLegendColor;
    }

    public KBaseView setLegendColor(int legendColor) {
        mLegendColor = legendColor;
        return this;
    }

    public double getLegendPaddingTop() {
        return mLegendPaddingTop;
    }

    public KBaseView setLegendPaddingTop(double legendPaddingTop) {
        mLegendPaddingTop = legendPaddingTop;
        return this;
    }

    public double getLegendPaddingRight() {
        return mLegendPaddingRight;
    }

    public KBaseView setLegendPaddingRight(double legendPaddingRight) {
        mLegendPaddingRight = legendPaddingRight;
        return this;
    }

    public float getLegendTxtSize() {
        return mLegendTxtSize;
    }

    public KBaseView setLegendTxtSize(float legendTxtSize) {
        mLegendTxtSize = legendTxtSize;
        return this;
    }

    public int getLineWidth() {
        return mLineWidth;
    }

    public KBaseView setLineWidth(int lineWidth) {
        mLineWidth = lineWidth;
        return this;
    }

    public double getLegendPaddingLeft() {
        return mLegendPaddingLeft;
    }

    public KBaseView setLegendPaddingLeft(double legendPaddingLeft) {
        mLegendPaddingLeft = legendPaddingLeft;
        return this;
    }

    public double getLegendTxtTopPadding() {
        return mLegendTxtTopPadding;
    }

    public KBaseView setLegendTxtTopPadding(double legendTxtTopPadding) {
        mLegendTxtTopPadding = legendTxtTopPadding;
        return this;
    }

    public boolean isDrawLongPress() {
        return mDrawLongPress;
    }

    public KBaseView setDrawLongPress(boolean drawLongPress) {
        mDrawLongPress = drawLongPress;
        return this;
    }

    public float getMovingX() {
        return mMovingX;
    }

    public KBaseView setMovingX(float movingX) {
        mMovingX = movingX;
        return this;
    }

    public Paint getLongPressPaint() {
        return mLongPressPaint;
    }

    public KBaseView setLongPressPaint(Paint longPressPaint) {
        mLongPressPaint = longPressPaint;
        return this;
    }

    public int getLongPressColor() {
        return mLongPressColor;
    }

    public KBaseView setLongPressColor(int longPressColor) {
        mLongPressColor = longPressColor;
        return this;
    }

    public float getLongPressLineWidth() {
        return mLongPressLineWidth;
    }

    public KBaseView setLongPressLineWidth(float longPressLineWidth) {
        mLongPressLineWidth = longPressLineWidth;
        return this;
    }

    public Quotes getCurrLongPressQuotes() {
        return mCurrLongPressQuotes;
    }

    public KBaseView setCurrLongPressQuotes(Quotes currLongPressQuotes) {
        mCurrLongPressQuotes = currLongPressQuotes;
        return this;
    }

    public KViewType.MoveType getMoveType() {
        return mMoveType;
    }

    public KBaseView setMoveType(KViewType.MoveType moveType) {
        mMoveType = moveType;
        return this;
    }
}
