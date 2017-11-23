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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import wgyscsf.financialcustomerview.R;
import wgyscsf.financialcustomerview.utils.FormatUtil;
import wgyscsf.financialcustomerview.utils.TimeUtils;

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
    float mBrokenLineWidth = 2;
    int mBrokenLineColor;
    //是否是虚线
    boolean isBrokenLineDashed = false;

    /**
     * 折线图阴影
     */
    Paint mBrokenLineBgPaint;
    //折线下面的浅蓝色
    int mBrokenLineBgColor;
    int mAlpha = 40;
    /**
     * 最后一个小圆点的半径
     */
    Paint mDotPaint;
    float mDotRadius = 6;
    int mDotColor;

    /**
     * 实时横线
     */
    Paint mTimingLinePaint;
    float mTimingLineWidth = 2;
    int mTimingLineColor;
    //是否是虚线
    boolean isTimingLineDashed = true;

    /**
     * 实时横线右侧的红色的框和实时数据
     */
    Paint mTimingTxtBgPaint;//实时数据的背景
    Paint mTimingTxtPaint;//实时数据
    float mTimingTxtWidth = 18;
    int mTimingTxtColor;
    int mTimingTxtBgColor;

    //外围X、Y轴线文字
    Paint mXYTxtPaint;
    //x、y轴指示文字字体的大小
    final float mXYTxtSize = 14;
    int mXYTxtColor;
    //右侧文字距离右边线线的距离
    final float mRightTxtPadding = 4;
    //底部文字距离底部线的距离
    final float mBottomTxtPadding = 20;

    /**
     * 其它属性
     */
    Context mContext;
    //可见的显示的条数(屏幕上显示的并不是所有的数据，只是部分)
    int mShownMaxCount = 50;
    //开始位置，数据集合的起始位置
    int beginIndex = 0;
    //数据集合
    List<Quotes> mQuotesList;
    //默认情况下结束点距离右边边距
    float mRightBlankPadding = 60;
    //为了美观，容器内（边框内部的折线图距离外边框线的上下距离）上面有一定间距，下面也有一定的间距。
    float mInnerTopBlankPadding = 60;
    float mInnerBottomBlankPadding = 60;
    //y轴数据的小数位数，这个本来数据产品属性，但是模拟数据中没有，就在这里定义了
    int digits = 2;
    //每一个x、y轴的一个单元的的宽和高
    float mPerX;
    float mPerY;
    //最小值和最大值对应的Model
    Quotes mMinQuotes;
    Quotes mMaxQuotes;
    //x轴起始位置的时间和结束位置的时间
    Quotes mBeginQuotes;
    Quotes mEndQuotes;


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
        drawXyTxt(canvas);
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
        initXyTxtPaint();
        initBrokenLinePaint();
        initBrokenLineBgPaint();
        initDotPaint();
        initTimingTxtPaint();
        initTimingLinePaint();
    }

    private void loadDefAttrs() {
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
        if (isInnerXyLineDashed)
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        mInnerXyPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));
    }

    private void initXyTxtPaint() {
        mXYTxtPaint = new Paint();
        mXYTxtPaint.setColor(mXYTxtColor);
        mXYTxtPaint.setTextSize(mXYTxtSize);
        mXYTxtPaint.setAntiAlias(true);
    }

    private void initBrokenLinePaint() {
        mBrokenLinePaint = new Paint();
        mBrokenLinePaint.setColor(mBrokenLineColor);
        mBrokenLinePaint.setStrokeWidth(mBrokenLineWidth);
        mBrokenLinePaint.setStyle(Paint.Style.STROKE);
        mBrokenLinePaint.setAntiAlias(true);
    }

    private void initBrokenLineBgPaint() {
        mBrokenLineBgPaint = new Paint();
        mBrokenLineBgPaint.setColor(mBrokenLineBgColor);
        mBrokenLineBgPaint.setStyle(Paint.Style.FILL);
        mBrokenLineBgPaint.setAntiAlias(true);
        mBrokenLineBgPaint.setAlpha(mAlpha);
    }

    private void initDotPaint() {
        mDotPaint = new Paint();
        mDotPaint.setColor(mDotColor);
        mDotPaint.setStyle(Paint.Style.FILL);
        mDotPaint.setAntiAlias(true);
    }

    private void initTimingTxtPaint() {
        mTimingTxtBgPaint = new Paint();
        mTimingTxtBgPaint.setColor(mTimingTxtBgColor);
        mTimingTxtBgPaint.setAntiAlias(true);

        mTimingTxtPaint = new Paint();
        mTimingTxtPaint.setTextSize(mTimingTxtWidth);
        mTimingTxtPaint.setColor(mTimingTxtColor);
        mTimingTxtPaint.setAntiAlias(true);
    }

    private void initTimingLinePaint() {
        mTimingLinePaint = new Paint();
        mTimingLinePaint.setColor(mTimingLineColor);
        mTimingLinePaint.setStrokeWidth(mTimingLineWidth);
        mTimingLinePaint.setStyle(Paint.Style.STROKE);
        mTimingLinePaint.setAntiAlias(true);
        if (isTimingLineDashed)
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        mTimingLinePaint.setPathEffect(new DashPathEffect(new float[]{8, 8}, 0));
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

    private void drawXyTxt(Canvas canvas) {
        //先处理y轴方向文字
        drawYPaint(canvas);

        //处理x轴方向文字
        drawXPaint(canvas);
    }

    private void drawBrokenLine(Canvas canvas) {
        //先画第一个点
        Quotes quotes = mQuotesList.get(beginIndex);
        Path path = new Path();
        Path path2 = new Path();
        //这里需要说明一下，x轴的起始点，其实需要加上mPerX，但是加上之后不是从起始位置开始，不好看。
        // 同理，for循环内x轴其实需要(i+1)。现在这样处理，最后会留一点空隙，其实挺好看的。
        float floatY = (float) (mHeight - mPaddingBottom - mInnerBottomBlankPadding - mPerY * (quotes.c - mMinQuotes.c));
        path.moveTo(mPaddingLeft, floatY);
        path2.moveTo(mPaddingLeft, floatY);
        for (int i = beginIndex + 1; i < beginIndex + mShownMaxCount; i++) {
            Quotes q = mQuotesList.get(i);
            float floatX2 = mPaddingLeft + mPerX * (i - beginIndex);//注意这个 mPerX * (i-beginIndex)，而不是mPerX * (i)
            float floatY2 = (float) (mHeight - mPaddingBottom - mInnerBottomBlankPadding - mPerY * (q.c - mMinQuotes.c));
            path.lineTo(floatX2, floatY2);
            path2.lineTo(floatX2, floatY2);
            //最后一个点，画一个小圆点；实时横线；横线的右侧数据与背景；折线下方阴影
            if (i == beginIndex + mShownMaxCount - 1) {
                //绘制小圆点
                canvas.drawCircle(floatX2, floatY2, mDotRadius, mDotPaint);

                //接着画实时横线
                canvas.drawLine(mPaddingLeft, floatY2, mWidth - mPaddingRight, floatY2, mTimingLinePaint);

                //接着绘制实时横线的右侧数据与背景
                //文字高度
                float txtHight = getFontHeight(mTimingTxtWidth, mTimingTxtBgPaint);
                //绘制背景
                canvas.drawRect(mWidth - mPaddingRight, floatY2 - txtHight / 2, mWidth, floatY2 + txtHight / 2, mTimingTxtBgPaint);

                //绘制实时数据
                //距离左边的距离
                float leftDis = 8;
                canvas.drawText(FormatUtil.numFormat(q.c, digits), mWidth - mPaddingRight + leftDis, floatY2 + txtHight / 4, mTimingTxtPaint);

                //在这里把path圈起来，添加阴影。特别注意，这里处理下方阴影和折线边框。采用两个画笔和两个Path处理的，貌似没有一个Paint可以同时绘制边框和填充色
                path2.lineTo(floatX2, mHeight - mPaddingBottom);
                path2.lineTo(mPaddingLeft, mHeight - mPaddingBottom);
                path2.close();
            }
        }
        canvas.drawPath(path, mBrokenLinePaint);
        canvas.drawPath(path2, mBrokenLineBgPaint);
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
    private void drawYPaint(Canvas canvas) {
        //细节处理，文字高度居中
        float halfTxtHight;
        double minBorderData;
        double maxBorderData;
        double dataDis = mMaxQuotes.c - mMinQuotes.c;
        double yDis = (mHeight - mPaddingTop - mPaddingBottom - mInnerTopBlankPadding - mInnerBottomBlankPadding);
        double perY = dataDis / yDis;
        minBorderData = mMinQuotes.c - mInnerBottomBlankPadding * perY;
        maxBorderData = mMinQuotes.c + mInnerTopBlankPadding * perY;

        halfTxtHight = getFontHeight(mXYTxtSize, mXYTxtPaint) / 4;//应该/2的，但是不准确，原因不明
        //halfTxtHight = 0;

        //现将最小值、最大值画好
        float rightBorderPadding = mRightTxtPadding;
        canvas.drawText(FormatUtil.numFormat(minBorderData, digits),
                mWidth - mPaddingRight + rightBorderPadding,
                mHeight - mPaddingBottom + halfTxtHight, mXYTxtPaint);
        //draw max
        canvas.drawText(FormatUtil.numFormat(maxBorderData, digits),
                mWidth - mPaddingRight + rightBorderPadding,
                mPaddingTop + halfTxtHight, mXYTxtPaint);
        //因为横线是均分的，所以只要取到最大值最小值的差值，均分即可。
        float perYValues = (float) ((maxBorderData - minBorderData) / 4);
        float perYWidth = (mHeight - mPaddingBottom - mPaddingTop) / 4;
        //从下到上依次画
        for (int i = 1; i <= 3; i++) {
            canvas.drawText(FormatUtil.numFormat(minBorderData + perYValues * i, digits),
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
    private void drawXPaint(Canvas canvas) {
        //细节，让中间虚线对应的文字居中
        float halfTxtWidth = mXYTxtPaint.measureText("00:00") / 2;

        //单位间距，注意这里需要加上右边内边距
        double perXWith = (mWidth - mPaddingLeft - mPaddingRight) / 4;
        double xDis = (mWidth - mPaddingLeft - mPaddingRight - mRightBlankPadding);
        long timeDis = mEndQuotes.t - mBeginQuotes.t;
        long perXTime = (long) (timeDis / xDis);
        String showTime;
        float finalHalfTxtWidth;
        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                finalHalfTxtWidth = 0;
                showTime = TimeUtils.millis2String((long) (mBeginQuotes.t + perXWith * perXTime * i));//不要忘了*perXWith
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

    public float getFontHeight(float fontSize, Paint paint) {
        paint.setTextSize(fontSize);
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (float) (Math.ceil(fm.descent - fm.top) + 2);
    }

    private int getColor(@ColorRes int colorId) {
        return getResources().getColor(colorId);
    }

    private String getString(@StringRes int stringId) {
        return getResources().getString(stringId);
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

        //开始处理数据
        processData();
    }

    /**
     * 实时推送过来的数据，实时更新
     *
     * @param quotes
     */
    public void addTimeSharingData(Quotes quotes) {
        if (quotes == null) {
            Toast.makeText(mContext, "数据异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "setTimeSharingData: 数据异常");
            return;
        }
        mQuotesList.add(quotes);
        processData();
    }

    private void processData() {
        digits = 4;
        //找到最大值和最小值
        double tempMinClosePrice = Double.MAX_VALUE;
        double tempMaxClosePrice = Double.MIN_VALUE;

        //特别注意，最新的数据在最后面，所以数据范围应该是[size-mShownMaxCount~size)
        int size = mQuotesList.size();
        beginIndex = size - mShownMaxCount;
        for (int i = beginIndex; i < beginIndex + mShownMaxCount; i++) {
            Quotes quotes = mQuotesList.get(i);
            if (i == beginIndex) {
                mBeginQuotes = quotes;
            }
            if (i == beginIndex + mShownMaxCount - 1) {
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
        mPerX = (mWidth - mPaddingLeft - mPaddingRight - mRightBlankPadding) / mShownMaxCount;
        //不要忘了减去内部的上下Padding
        mPerY = (float) ((mHeight - mPaddingTop - mPaddingBottom - mInnerTopBlankPadding - mInnerBottomBlankPadding) / (mMaxQuotes.c - mMinQuotes.c));
        Log.e(TAG, "processData: mPerX:" + mPerX + ",mPerY:" + mPerY);
        invalidate();
    }

    interface TimeSharingListener {
        void success();

        void error(Exception e);
    }
}
