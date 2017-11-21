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
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import wgyscsf.financialcustomerview.R;
import wgyscsf.financialcustomerview.fund.FundMode;

import static android.view.View.MeasureSpec.AT_MOST;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/11/21 16:26
 * 描 述 ：分时图，根据闭盘价绘制折线图
 * ============================================================
 **/
public class TimeSharingView extends View {
    private static final String TAG = "FundView";
    //控件默认宽高
    private static final float DEF_WIDTH = 650;
    private static final float DEF_HIGHT = 400;

    //控件宽高
    int mWidth;
    int mHeight;
    //上下左右padding
    float mPaddingTop = 20;
    float mPaddingBottom = 50;
    float mPaddingLeft = 8;
    float mPaddingRight = 90;

    /**
     * 最外面的上下左右的框
     */
    Paint mOuterPaint;
    float mOuterLineWidth = 1;
    int mOuterLineColor;

    /**
     * 内部xy轴虚线
     */
    Paint mInnerXyPaint;
    float mInnerXyLineWidth = 1;
    int mInnerXyLineColor;
    //是否是虚线
    boolean isInnerXyLineDashed = true;

    /**
     * 折线图
     */
    Paint mBrokenLinePaint;
    float mBrokenLineWidth = 1;
    int mBrokenLineColor;
    //是否是虚线
    boolean isBrokenLineDashed = false;

    /**
     * 实时横线
     */
    Paint mTimingLinePaint;
    float mTimingLineWidth = 1;
    int mTimingLineColor;
    //是否是虚线
    boolean isTimingLineDashed = true;

    /**
     * 其它属性
     */
    Context mContext;
    //折线下面的浅蓝色
    int mblowBlueColor;
    //可见的显示的条数(屏幕上显示的并不是所有的数据，只是部分)
    int mShownMaxCount = 100;
    //开始位置，数据集合的起始位置
    int beginIndex = 0;
    //数据集合
    List<Quotes> mQuotesList;
    //默认情况下结束点距离右边边距
    float mRightBlankPadding = 60;
    //为了美观，容器内（边框内部的折线图距离外边框线的上下距离）上面有一定间距，下面也有一定的间距。
    float mInnerTopBlankPadding = 60;
    float mInnerBottomBlankPadding = 60;


    //每一个x、y轴的一个单元的的宽和高
    float mPerX;
    float mPerY;
    //最小值和最大值对应的Model
    Quotes mMinQuotes;
    Quotes mMaxQuotes;


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
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == AT_MOST && heightSpecMode == AT_MOST) {
            setMeasuredDimension((int) DEF_WIDTH, (int) DEF_HIGHT);
        } else if (widthSpecMode == AT_MOST) {
            setMeasuredDimension((int) DEF_WIDTH, heightSpecSize);
        } else if (heightSpecMode == AT_MOST) {
            setMeasuredDimension(widthSpecSize, (int) DEF_HIGHT);
        } else {
            setMeasuredDimension(widthSpecSize, heightSpecSize);
        }
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e("TimeSharingView", "onDraw: ");
        if (mQuotesList.isEmpty()) {
            return;
        }
        drawOuterLine(canvas);
        drawInnerXy(canvas);
        drawBrokenLine(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    private void initAttrs() {
        //加载颜色和字符串资源
        loadDefAttrs();

        //初始化画笔
        initOuterPaint();
        initInnerXyPaint();
        initBrokenLinePaint();
        initTimingLinePaint();
    }

    private void loadDefAttrs() {
        //数据源
        mQuotesList = new ArrayList<>(mShownMaxCount);
        //颜色
        mOuterLineColor = getColor(R.color.color_timeSharing_outerStrokeColor);
        mInnerXyLineColor = getColor(R.color.color_timeSharing_innerXyDashColor);
        mBrokenLineColor = getColor(R.color.color_timeSharing_brokenLineColor);
        mTimingLineColor = getColor(R.color.color_timeSharing_timingLineColor);
        mblowBlueColor = getColor(R.color.color_timeSharing_blowBlueColor);
    }

    private void initOuterPaint() {
        mOuterPaint = new Paint();
        mOuterPaint.setColor(mOuterLineColor);
        mOuterPaint.setStrokeWidth(mOuterLineWidth);
    }

    private void initInnerXyPaint() {
        mInnerXyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerXyPaint.setColor(mInnerXyLineColor);
        mInnerXyPaint.setStrokeWidth(mInnerXyLineWidth);
        mInnerXyPaint.setStyle(Paint.Style.STROKE);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        mInnerXyPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));
    }

    private void initBrokenLinePaint() {
        mBrokenLinePaint = new Paint();
        mBrokenLinePaint.setColor(mBrokenLineColor);
        mBrokenLinePaint.setStrokeWidth(mBrokenLineWidth);
        mBrokenLinePaint.setStyle(Paint.Style.STROKE);
        mBrokenLinePaint.setAntiAlias(true);
    }

    private void initTimingLinePaint() {

    }

    private void drawOuterLine(Canvas canvas) {
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

    private void drawInnerXy(Canvas canvas) {
        //先绘制x轴
        //计算每一段x的高度
        double perhight = (mHeight - mPaddingTop - mPaddingBottom) / 4;
        for (int i = 1; i <= 3; i++) {
            canvas.drawLine(mPaddingLeft, (float) (mPaddingTop + perhight * i),
                    mWidth - mPaddingRight, (float) (mPaddingTop + perhight * i), mInnerXyPaint);
        }

        //绘制y轴
        double perWidth = (mWidth - mPaddingLeft - mPaddingRight) / 4;
        for (int i = 1; i <= 3; i++) {
            canvas.drawLine((float) (mPaddingLeft + perWidth * i), mPaddingTop,
                    (float) (mPaddingLeft + perWidth * i), mHeight - mPaddingBottom, mInnerXyPaint);
        }
    }

    private void drawBrokenLine(Canvas canvas) {
        //先画第一个点
        Quotes quotes = mQuotesList.get(beginIndex);
        Path path = new Path();
        //这里需要说明一下，x轴的起始点，其实需要加上mPerX，但是加上之后不是从起始位置开始，不好看。
        // 同理，for循环内x轴其实需要(i+1)。现在这样处理，最后会留一点空隙，其实挺好看的。
        float floatY = (float) (mHeight - mPaddingBottom - mInnerBottomBlankPadding - mPerY * (quotes.c - mMinQuotes.c));
        path.moveTo(mPaddingLeft, floatY);
        for (int i = beginIndex + 1; i < beginIndex + 1 + mShownMaxCount; i++) {
            Quotes q = mQuotesList.get(i);
            float floatX2 = mPaddingLeft + mPerX * i;
            float floatY2 = (float) (mHeight - mPaddingBottom - mInnerBottomBlankPadding - mPerY * (q.c - mMinQuotes.c));
            Log.e(TAG, "drawBrokenLine: floatX2:" + floatX2 + ",floatY2:" + floatY2 +
                    ",q.c:" + q.c + ",mMinQuotes.c:" + mMinQuotes.c +
                    ",---->mPerY * (q.c - mMinQuotes.c):" + mPerY * (q.c - mMinQuotes.c));
            path.lineTo(floatX2, floatY2);
        }
        canvas.drawPath(path, mBrokenLinePaint);
    }

    private int getColor(@ColorRes int colorId) {
        return getResources().getColor(colorId);
    }

    private String getString(@StringRes int stringId) {
        return getResources().getString(stringId);
    }

    public void setTimeSharingData(List<Quotes> quotesList) {
        if (quotesList == null || quotesList.isEmpty()) {
            Toast.makeText(mContext, "数据异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "setTimeSharingData: 数据异常");
            return;
        }

        mQuotesList = quotesList;

        //开始处理数据
        processData();
    }

    private void processData() {
        //找到最大值和最小值
        double tempMinClosePrice = Double.MAX_VALUE;
        double tempMaxClosePrice = Double.MIN_VALUE;

        for (int i = beginIndex; i < beginIndex + mShownMaxCount; i++) {
            Quotes quotes = mQuotesList.get(i);
            if (quotes.c <= tempMinClosePrice) {
                tempMinClosePrice = quotes.c;
                mMinQuotes = quotes;
            }
            if (quotes.c >= tempMaxClosePrice) {
                tempMaxClosePrice = quotes.c;
                mMaxQuotes = quotes;
            }
        }
        mPerX = (mWidth - mPaddingLeft - mPaddingRight - mRightBlankPadding) / mShownMaxCount;
        //不要忘了减去内部的上下Padding
        mPerY = (float) ((mHeight - mPaddingTop - mPaddingBottom - mInnerTopBlankPadding - mInnerBottomBlankPadding) / (mMaxQuotes.c - mMinQuotes.c));

        invalidate();

        Log.e(TAG, "processData: " + mMinQuotes.toString());
        Log.e(TAG, "processData: " + mMaxQuotes.toString());
    }
}
