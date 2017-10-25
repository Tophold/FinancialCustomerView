package wgyscsf.financialcustomerview.fund;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
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

    //外围X、Y轴线文字
    Paint mXYPaint;
    //x、y轴指示文字字体的大小
    final float mXYTextSize = 14;


    //内部X轴虚线
    Paint mInnerXPaint;
    //折线
    Paint mBrokenPaint;

    //X、Y轴每一个data对应的大小
    float mPerX;
    float mPerY;

    //Y轴对应的最大值和最小值
    double mMinY;
    double mMaxY;


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
        if (mFundModeList == null || mFundModeList.size() == 0) return;

        drawInnerXPaint(canvas);
    }

    private void drawInnerXPaint(Canvas canvas) {
        //画5条横轴的虚线
        //首先确定最大值和最小值的位置

        float perHight=(mHeight-mPaddingBottom-mPaddingTop)/4;

        canvas.drawLine(0+mPaddingLeft,mPaddingTop,
                mWidth-mPaddingRight,mPaddingTop,mInnerXPaint);//最上面的那一条

        canvas.drawLine(0+mPaddingLeft,mPaddingTop+perHight*1,
                mWidth-mPaddingRight,mPaddingTop+perHight*1,mInnerXPaint);//2

        canvas.drawLine(0+mPaddingLeft,mPaddingTop+perHight*2,
                mWidth-mPaddingRight,mPaddingTop+perHight*2,mInnerXPaint);//3

        canvas.drawLine(0+mPaddingLeft,mPaddingTop+perHight*3,
                mWidth-mPaddingRight,mPaddingTop+perHight*3,mInnerXPaint);//4

        canvas.drawLine(0+mPaddingLeft,mHeight-mPaddingBottom,
                mWidth-mPaddingRight,mHeight-mPaddingBottom,mInnerXPaint);//最下面的那一条

    }

    private void initAttrs() {
        initInnerXPaint();
        initXYPaint();
        initBrokenPaint();
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
        mInnerXPaint.setStrokeWidth(convertDp2Px(1));
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
    }

    private int getColor(@ColorRes int colorId) {
        return getResources().getColor(colorId);
    }

    private float convertDp2Px(int dpValue) {
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
        mMinY = mFundModeList.get(0).dataY;
        mMaxY = mFundModeList.get(0).dataY;
        for (FundMode fundMode : mFundModeList) {
            double temp = fundMode.dataY;
            if (temp < mMinY) {
                mMinY = temp;
            }
            if (temp > mMaxY) {
                mMaxY = temp;
            }
        }
        //获取单个数据X轴的大小
        mPerX = mWidth / mFundModeList.size();
        mPerY = mHeight / mFundModeList.size();


        //刷新界面
        postInvalidate();
    }


}
