package wgyscsf.financiallib.view.fund;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.text.SimpleDateFormat;
import java.util.List;

import wgyscsf.financiallib.R;
import wgyscsf.financiallib.view.BaseView;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/10/25 14:47
 * 描 述 ：蚂蚁财富基金收益折线图
 * ============================================================
 **/
public class FundView extends BaseView {

    //数据源
    List<FundMode> mFundModeList;

    //上下左右padding
    float mPaddingTop = 100;
    float mPaddingBottom = 70;
    float mPaddingLeft = 50;
    float mPaddingRight = 50;

    //Y轴对应的最大值和最小值,注意，这里存的是对象
    FundMode mMinFundMode;
    FundMode mMaxFundMode;

    //X、Y轴每一个data对应的大小
    float mPerX;
    float mPerY;

    //正在加载中
    Paint mLoadingPaint;
    final float mLoadingTextSize = 20;
    final String mLoadingText = "数据加载，请稍后";
    boolean mDrawLoadingPaint = true;


    //外围X、Y轴线文字
    Paint mXYPaint;
    //x、y轴指示文字字体的大小
    final float mXYTextSize = 14;
    //左侧文字距离左边线线的距离
    final float mLeftTxtPadding = 16;
    //底部文字距离底部线的距离
    final float mBottomTxtPadding = 20;


    //内部X轴虚线
    Paint mInnerXPaint;
    float mInnerXStrokeWidth = 1;

    //折线
    Paint mBrokenPaint;
    //单位：dp
    float mBrokenStrokeWidth = 1;

    //长按的十字线
    Paint mLongPressPaint;
    boolean mDrawLongPressPaint = false;
    //长按处理
    long mPressTime;
    //默认多长时间算长按
    final long DEF_LONGPRESS_LENGTH = 700;
    float mPressX;
    float mPressY;

    //最上面默认显示累计收益金额
    Paint mDefAllIncomePaint;
    final float mDefAllIncomeTextSize = 20;


    //长按情况下x轴和y轴要显示的文字
    Paint mLongPressTxtPaint;
    final float mLongPressTextSize = 20;
    public FundView(Context context) {
        this(context, null);
    }

    public FundView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public FundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        if (mFundModeList == null || mFundModeList.size() == 0) return;

        //加载三个核心Paint
        drawInnerXPaint(canvas);
        drawBrokenPaint(canvas);
        drawXYPaint(canvas);

        drawTopTxtPaint(canvas);

        drawLongPress(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPressTime = event.getDownTime();
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getEventTime() - mPressTime > DEF_LONGPRESS_LENGTH) {
                    Log.e(TAG, "onTouchEvent: 长按了。。。");
                    mPressX = event.getX();
                    mPressY = event.getY();
                    //处理长按后的逻辑
                    showLongPressView();
                }
                break;
            case MotionEvent.ACTION_UP:
                //处理松手后的逻辑
                hiddenLongPressView();
                break;
            default:
                break;
        }

        return true;
    }

    private void initAttrs() {
        initLoadingPaint();
        initInnerXPaint();
        initXYPaint();
        initBrokenPaint();
        initLongPressPaint();
        initTopTxt();
    }

    private void initLoadingPaint() {
        mLoadingPaint = new Paint();
        mLoadingPaint.setColor(getColor(R.color.color_fundView_xyTxtColor));
        mLoadingPaint.setTextSize(mLoadingTextSize);
        mLoadingPaint.setAntiAlias(true);
    }

    //初始化绘制虚线的画笔
    private void initInnerXPaint() {
        mInnerXPaint = new Paint();
        mInnerXPaint.setColor(getColor(R.color.color_fundView_xLineColor));
        mInnerXPaint.setStrokeWidth(convertDp2Px(mInnerXStrokeWidth));
        mInnerXPaint.setStyle(Paint.Style.STROKE);
        setLayerType(LAYER_TYPE_SOFTWARE, null);//禁用硬件加速
        PathEffect effects = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);
        mInnerXPaint.setPathEffect(effects);
    }

    private void initXYPaint() {
        mXYPaint = new Paint();
        mXYPaint.setColor(getColor(R.color.color_fundView_xyTxtColor));
        mXYPaint.setTextSize(mXYTextSize);
        mXYPaint.setAntiAlias(true);
    }

    private void initBrokenPaint() {
        mBrokenPaint = new Paint();
        mBrokenPaint.setColor(getColor(R.color.color_fundView_brokenLineColor));
        mBrokenPaint.setStyle(Paint.Style.STROKE);
        mBrokenPaint.setAntiAlias(true);
        mBrokenPaint.setStrokeWidth(convertDp2Px(mBrokenStrokeWidth));
    }

    private void initLongPressPaint() {
        mLongPressPaint = new Paint();
        mLongPressPaint.setColor(getColor(R.color.color_fundView_longPressLineColor));
        mLongPressPaint.setStyle(Paint.Style.FILL);
        mLongPressPaint.setAntiAlias(true);
        mLongPressPaint.setTextSize(mLongPressTextSize);
    }

    //折线上面显示文字信息
    private void initTopTxt() {
        mDefAllIncomePaint = new Paint();
        mDefAllIncomePaint.setColor(getColor(R.color.color_fundView_defIncomeTxt));
        mDefAllIncomePaint.setTextSize(mLongPressTextSize);
        mDefAllIncomePaint.setAntiAlias(true);

        mLongPressTxtPaint = new Paint();
        mLongPressTxtPaint.setColor(getColor(R.color.color_fundView_longPressLineColor));
        mLongPressTxtPaint.setTextSize(mLongPressTextSize);
        mLongPressTxtPaint.setAntiAlias(true);
    }


    private void showLoadingPaint(Canvas canvas) {
        if (!mDrawLoadingPaint) return;
        //这里特别注意，x轴的起始点要减去文字宽度的一半
        canvas.drawText(mLoadingText, mWidth / 2 - mLoadingPaint.measureText(mLoadingText) / 2, mHeight / 2, mLoadingPaint);
    }

    private void drawInnerXPaint(Canvas canvas) {
        //画5条横轴的虚线
        //首先确定最大值和最小值的位置
        float perHight = (mHeight - mPaddingBottom - mPaddingTop) / 4;

        canvas.drawLine(0 + mPaddingLeft, mPaddingTop,
                mWidth - mPaddingRight, mPaddingTop, mInnerXPaint);//最上面的那一条

        canvas.drawLine(0 + mPaddingLeft, mPaddingTop + perHight * 1,
                mWidth - mPaddingRight, mPaddingTop + perHight * 1, mInnerXPaint);//2

        canvas.drawLine(0 + mPaddingLeft, mPaddingTop + perHight * 2,
                mWidth - mPaddingRight, mPaddingTop + perHight * 2, mInnerXPaint);//3

        canvas.drawLine(0 + mPaddingLeft, mPaddingTop + perHight * 3,
                mWidth - mPaddingRight, mPaddingTop + perHight * 3, mInnerXPaint);//4

        canvas.drawLine(0 + mPaddingLeft, mHeight - mPaddingBottom,
                mWidth - mPaddingRight, mHeight - mPaddingBottom, mInnerXPaint);//最下面的那一条

    }

    private void drawBrokenPaint(Canvas canvas) {
        //先画第一个点
        FundMode fundMode = mFundModeList.get(0);
        Path path = new Path();
        //这里需要说明一下，x轴的起始点，其实需要加上mPerX，但是加上之后不是从起始位置开始，不好看。
        // 同理，for循环内x轴其实需要(i+1)。现在这样处理，最后会留一点空隙，其实挺好看的。
        float floatY = mHeight - mPaddingBottom - mPerY * (fundMode.dataY - mMinFundMode.dataY);
        fundMode.floatX = mPaddingLeft;
        fundMode.floatY = floatY;
        path.moveTo(mPaddingLeft, floatY);
        for (int i = 1; i < mFundModeList.size(); i++) {
            FundMode fm = mFundModeList.get(i);
            float floatX2 = mPaddingLeft + mPerX * i;
            float floatY2 = mHeight - mPaddingBottom - mPerY * (fm.dataY - mMinFundMode.dataY);
            fm.floatX = floatX2;
            fm.floatY = floatY2;
            path.lineTo(floatX2, floatY2);
            //Log.e(TAG, "drawBrokenPaint: " + mPaddingLeft + mPerX * i + "-----" + (mHeight - mClosePerY * (mFundModeList.get(i).dataY - mMinFundMode.dataY) - mPaddingBottom));
        }

        canvas.drawPath(path, mBrokenPaint);


    }

    private void drawXYPaint(Canvas canvas) {
        //先处理y轴方向文字
        drawYPaint(canvas);

        //处理x轴方向文字
        drawXPaint(canvas);
    }

    private void drawTopTxtPaint(Canvas canvas) {
        //先画默认情况下的top文字
        drawDefTopTxtpaint(canvas);
        //按下的文字信息在按下之后处理，see:drawLongPress(Canvas canvas)
    }

    /**
     * 这里处理画十字的逻辑:这里的十字不是手指按下的位置，这样没有意义。
     * 而是当前按下的距离x轴最近的时间（注意：并不一定按下对应的x轴就是有时间的，如果没有取最近的）。
     * 当取到x轴的值，之后算出来对应的y轴的值，这个才是十字对应的位置坐标。
     * 如何获取x轴最近的时间？我们可以在FundMode中定义x\y的位置参数，遍历对比找到最小即可。
     * (see: drawBrokenPaint(canvas);)
     *
     * @param canvas
     */
    private void drawLongPress(Canvas canvas) {
        if (!mDrawLongPressPaint) return;

        //获取距离最近按下的位置的model
        float pressX = mPressX;
        //循环遍历，找到距离最短的x轴的mode
        FundMode finalFundMode = mFundModeList.get(0);
        float minXLen = Integer.MAX_VALUE;
        for (int i = 0; i < mFundModeList.size(); i++) {
            FundMode currFunMode = mFundModeList.get(i);
            float abs = Math.abs(pressX - currFunMode.floatX);
            if (abs < minXLen) {
                finalFundMode = currFunMode;
                minXLen = abs;
            }
        }

        //x
        canvas.drawLine(mPaddingLeft, finalFundMode.floatY, mWidth - mPaddingRight, finalFundMode.floatY, mLongPressPaint);
        //y
        canvas.drawLine(finalFundMode.floatX, mPaddingTop, finalFundMode.floatX, mHeight - mPaddingBottom, mLongPressPaint);

        //开始处理按下之后top的文字信息
        //先画背景
        float hight = mPaddingTop - 30;
        Paint bgColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgColor.setColor(getColor(R.color.color_fundView_pressIncomeTxtBg));
        canvas.drawRect(0, 0, mWidth, hight, bgColor);

        //开始画按下之后左边的日期文字
        Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timePaint.setTextSize(mLongPressTextSize);
        timePaint.setColor(getColor(R.color.color_fundView_xyTxtColor));
        canvas.drawText(processDateTime(finalFundMode.datetime) + "",
                10, hight / 2 + getFontHeight(mLoadingTextSize, timePaint) / 2, timePaint);

        //右边红色收益文字
        canvas.drawText(finalFundMode.dataY + "",
                mWidth - mPaddingRight - mLongPressPaint.measureText(finalFundMode.dataY + ""),
                hight / 2 + getFontHeight(mLoadingTextSize, timePaint) / 2, mLongPressPaint);

        //右边的左边的提示文字
        Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hintPaint.setTextSize(mLongPressTextSize);
        hintPaint.setColor(getColor(R.color.color_fundView_xyTxtColor));
        canvas.drawText(getString(R.string.string_fundView_pressHintTxt),
                mWidth - mPaddingRight - mLongPressPaint.measureText(finalFundMode.dataY + "")
                        - hintPaint.measureText(getString(R.string.string_fundView_pressHintTxt)),
                hight / 2 + getFontHeight(mLoadingTextSize, timePaint) / 2, hintPaint);


    }

    //找到最大时间、最小时间和中间时间显示即可
    private void drawXPaint(Canvas canvas) {
        long beginTime = mFundModeList.get(0).datetime;
        long midTime = mFundModeList.get((mFundModeList.size() - 1) / 2).datetime;
        long endTime = mFundModeList.get(mFundModeList.size() - 1).datetime;
        String bengin = processDateTime(beginTime);
        String mid = processDateTime(midTime);
        String end = processDateTime(endTime);

        //x轴文字的高度
        float hight = mHeight - mPaddingBottom + mBottomTxtPadding;

        canvas.drawText(bengin,
                mPaddingLeft,
                hight, mXYPaint);

        canvas.drawText(mid,
                mPaddingLeft + (mWidth - mPaddingLeft - mPaddingRight) / 2,
                hight, mXYPaint);

        canvas.drawText(end,
                mWidth - mPaddingRight - mXYPaint.measureText(end),
                hight, mXYPaint);//特别注意x轴的处理：- mXYPaint.measureText(end)

    }

    private void drawYPaint(Canvas canvas) {
        //现将最小值、最大值画好
        //draw min
        float txtWigth = mXYPaint.measureText(mMinFundMode.originDataY) + mLeftTxtPadding;
        canvas.drawText(mMinFundMode.originDataY + "",
                mPaddingLeft - txtWigth,
                mHeight - mPaddingBottom, mXYPaint);
        //draw max
        canvas.drawText(mMaxFundMode.dataY + "",
                mPaddingLeft - txtWigth,
                mPaddingTop, mXYPaint);
        //因为横线是均分的，所以只要取到最大值最小值的差值，均分即可。
        float perYValues = (mMaxFundMode.dataY - mMinFundMode.dataY) / 4;
        float perYWidth = (mHeight - mPaddingBottom - mPaddingTop) / 4;
        //从下到上依次画
        for (int i = 1; i <= 3; i++) {
            canvas.drawText(mMinFundMode.dataY + perYValues * i + "",
                    mPaddingLeft - txtWigth,
                    mHeight - mPaddingBottom - perYWidth * i, mXYPaint);
        }
    }

    private void drawDefTopTxtpaint(Canvas canvas) {
        //画默认情况下前面的蓝色小圆点
        Paint buleDotPaint = new Paint();
        buleDotPaint.setColor(getColor(R.color.color_fundView_brokenLineColor));
        buleDotPaint.setAntiAlias(true);
        float r = 6;
        buleDotPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mPaddingLeft + r / 2, mPaddingTop / 2 + r, r, buleDotPaint);

        float txtHight = getFontHeight(mDefAllIncomeTextSize, mDefAllIncomePaint);

        //先画hint文字
        Paint hintPaint = new Paint();
        hintPaint.setColor(getColor(R.color.color_fundView_xyTxtColor));
        hintPaint.setAntiAlias(true);
        hintPaint.setTextSize(mDefAllIncomeTextSize);
        String hintTxt = getString(R.string.string_fundView_defHintTxt);
        canvas.drawText(hintTxt, mPaddingLeft + r + 10, mPaddingTop / 2 + txtHight / 2,
                mDefAllIncomePaint);


        if (mFundModeList == null || mFundModeList.isEmpty()) return;
        canvas.drawText(mFundModeList.get(mFundModeList.size() - 1).dataY + "",
                mPaddingLeft + r + 10 + hintPaint.measureText(getString(R.string.string_fundView_defHintTxt)) + 5,
                mPaddingTop / 2 + txtHight / 2, mDefAllIncomePaint);
    }

    private void showLongPressView() {
        mDrawLongPressPaint = true;
        invalidate();
    }

    private void hiddenLongPressView() {
        //实现蚂蚁金服延迟消失十字线
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawLongPressPaint = false;
                invalidate();
            }
        }, 1000);
    }

    // 只需要把画笔颜色置为透明即可
    private void hiddenLoadingPaint() {
        mLoadingPaint.setColor(0x00000000);
        mDrawLoadingPaint = false;
    }

    private String processDateTime(long beginTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(beginTime);
    }

    private float convertDp2Px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (dpValue * scale + 0.5f);
    }

    public float getFontHeight(float fontSize, Paint paint) {
        paint.setTextSize(fontSize);
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (float) (Math.ceil(fm.descent - fm.top) + 2);
    }

    /**
     * 程序入口，设置数据
     */
    public void setDataList(List<FundMode> fundModeList) {
        if (fundModeList == null || fundModeList.size() == 0) return;
        this.mFundModeList = fundModeList;

        //开始获取最大值最小值；单个数据尺寸等
        mMinFundMode = mFundModeList.get(0);
        mMaxFundMode = mFundModeList.get(0);
        for (FundMode fundMode : mFundModeList) {
            if (fundMode.dataY < mMinFundMode.dataY) {
                mMinFundMode = fundMode;
            }
            if (fundMode.dataY > mMaxFundMode.dataY) {
                mMaxFundMode = fundMode;
            }
        }
        //获取单个数据X/y轴的大小
        mPerX = (mWidth - mPaddingLeft - mPaddingRight) / mFundModeList.size();
        mPerY = ((mHeight - mPaddingTop - mPaddingBottom) / (mMaxFundMode.dataY - mMinFundMode.dataY));
        Log.e(TAG, "setDataList: " + mMinFundMode + "," + mMaxFundMode + "..." + mPerX + "," + mPerY);

        //数据过来，隐藏加载更多
        hiddenLoadingPaint();

        //刷新界面
        invalidate();
    }
}
