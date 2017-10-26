package wgyscsf.financialcustomerview.fund;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

import wgyscsf.financialcustomerview.R;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/10/25 14:47
 * 描 述 ：
 * ============================================================
 **/
public class FundView extends View {

    private static final String TAG = "FundView";
    //数据源
    List<FundMode> mFundModeList;
    //控件宽高
    int mWidth;
    int mHeight;
    //上下左右padding
    float mPaddingTop = 50;
    float mPaddingBottom = 70;
    float mPaddingLeft = 50;
    float mPaddingRight = 50;

    //正在加载中
    Paint mLoadingPaint;
    final float mLoadingTextSize = 16;
    final String mLoadingText = "正在加载中...";


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


    //X、Y轴每一个data对应的大小
    float mPerX;
    float mPerY;

    //Y轴对应的最大值和最小值,注意，这里存的是对象
    FundMode mMinFundMode;
    FundMode mMaxFundMode;


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
        //默认加载loading界面
        showLoadingPaint(canvas);
        if (mFundModeList == null || mFundModeList.size() == 0) return;
        //hiddenLoadingPaint(canvas);

        drawInnerXPaint(canvas);
        drawBrokenPaint(canvas);
        drawXYPaint(canvas);
    }

    private void drawXYPaint(Canvas canvas) {
        //先处理y轴方向文字
        drawYPaint(canvas);

        //处理x轴方向文字
        drawXPaint(canvas);

    }

    //找到最大时间、最小时间和中间时间显示即可
    private void drawXPaint(Canvas canvas) {
        long beginTime = mFundModeList.get(0).datetime;
        long midTime = mFundModeList.get((mFundModeList.size() - 1) / 2).datetime;
        long endTime = mFundModeList.get(mFundModeList.size() - 1).datetime;
        String bengin = processDateTime(beginTime);
        String mid = processDateTime(midTime);
        String end = processDateTime(endTime);

        float hight = mHeight - mPaddingBottom + mBottomTxtPadding;

        canvas.drawText(bengin,
                mPaddingLeft,
                hight, mXYPaint);

        canvas.drawText(mid,
                mPaddingLeft + (mWidth - mPaddingLeft - mPaddingRight) / 2,
                hight, mXYPaint);

        canvas.drawText(end,
                mWidth - mPaddingRight - mXYPaint.measureText(end),
                hight, mXYPaint);

    }

    // TODO: 2017/10/26 完成时间格式化
    private String processDateTime(long beginTime) {
        return "10-26";
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

    private void drawBrokenPaint(Canvas canvas) {
        //先画第一个点
        FundMode fundMode = mFundModeList.get(0);
        Path path = new Path();
        //这里需要说明一下，x周的起始点，其实需要加上mPerX，但是加上之后不是从起始位置开始，不好看。
        // 同理，for循环内x周其实需要(i+1)。现在这样处理，最后会留一点空隙，其实挺好看的。
        path.moveTo(mPaddingLeft, (mHeight - mPaddingBottom - mPerY * (fundMode.dataY - mMinFundMode.dataY)));
        for (int i = 1; i < mFundModeList.size(); i++) {
            path.lineTo(mPaddingLeft + mPerX * i, (mHeight - mPaddingBottom - mPerY * (mFundModeList.get(i).dataY - mMinFundMode.dataY)));
            Log.e(TAG, "drawBrokenPaint: " + mPaddingLeft + mPerX * i + "-----" + (mHeight - mPerY * (mFundModeList.get(i).dataY - mMinFundMode.dataY) - mPaddingBottom));
        }

        canvas.drawPath(path, mBrokenPaint);
    }

    private void showLoadingPaint(Canvas canvas) {
        canvas.drawText(mLoadingText, mPaddingLeft +
                        (mWidth - mPaddingLeft - mPaddingRight) / 2,
                mPaddingTop + (mHeight - mPaddingTop - mPaddingBottom) / 2,
                mLoadingPaint);
    }

    private void hiddenLoadingPaint(Canvas canvas) {
        mLoadingPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(mLoadingPaint);
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

    private void initAttrs() {
        initLoadingPaint();
        initInnerXPaint();
        initXYPaint();
        initBrokenPaint();
    }

    private void initLoadingPaint() {
        mLoadingPaint = new Paint();
        mLoadingPaint.setColor(getColor(R.color.color_fundView_xyTxtColor));
        mLoadingPaint.setTextSize(mLoadingTextSize);
        mLoadingPaint.setAntiAlias(true);
    }

    private void initXYPaint() {
        mXYPaint = new Paint();
        mXYPaint.setColor(getColor(R.color.color_fundView_xyTxtColor));
        mXYPaint.setTextSize(mXYTextSize);
        mXYPaint.setAntiAlias(true);
    }


    private void initInnerXPaint() {
        mInnerXPaint = new Paint();
        mInnerXPaint.setColor(getColor(R.color.color_fundView_xLineColor));
        mInnerXPaint.setStrokeWidth(convertDp2Px(mInnerXStrokeWidth));
        mInnerXPaint.setStyle(Paint.Style.STROKE);
        setLayerType(LAYER_TYPE_SOFTWARE, null);//禁用硬件加速
        PathEffect effects = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);
        mInnerXPaint.setPathEffect(effects);
    }


    private void initBrokenPaint() {
        mBrokenPaint = new Paint();
        mBrokenPaint.setColor(getColor(R.color.color_fundView_brokenLineColor));
        mBrokenPaint.setStyle(Paint.Style.STROKE);
        mBrokenPaint.setAntiAlias(true);
        mBrokenPaint.setStrokeWidth(convertDp2Px(mBrokenStrokeWidth));
    }

    private int getColor(@ColorRes int colorId) {
        return getResources().getColor(colorId);
    }

    private float convertDp2Px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (dpValue * scale + 0.5f);
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
            FundMode temp = fundMode;
            if (temp.dataY < mMinFundMode.dataY) {
                mMinFundMode = temp;
            }
            if (temp.dataY > mMaxFundMode.dataY) {
                mMaxFundMode = temp;
            }
        }
        //获取单个数据X轴的大小
        mPerX = (mWidth - mPaddingLeft - mPaddingRight) / mFundModeList.size();
        mPerY = ((mHeight - mPaddingTop - mPaddingBottom) / (mMaxFundMode.dataY - mMinFundMode.dataY));
        Log.e(TAG, "setDataList: " + mMinFundMode + "," + mMaxFundMode + "..." + mPerX + "," + mPerY);


        //刷新界面
        postInvalidate();
    }


}
