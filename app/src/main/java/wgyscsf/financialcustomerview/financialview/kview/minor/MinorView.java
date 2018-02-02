package wgyscsf.financialcustomerview.financialview.kview.minor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import wgyscsf.financialcustomerview.R;
import wgyscsf.financialcustomerview.financialview.FinancialAlgorithm;
import wgyscsf.financialcustomerview.financialview.kview.KView;
import wgyscsf.financialcustomerview.financialview.kview.Quotes;
import wgyscsf.financialcustomerview.utils.FormatUtil;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/12/14 17:56
 * 描 述 ：
 * ============================================================
 **/
public class MinorView extends KView {


    /**
     * 初始化所有需要的颜色资源
     */
    //共有的
    int mOuterStrokeColor;
    int mInnerXyDashColor;
    int mXyTxtColor;
    int mLegendTxtColor;
    int mLongPressTxtColor;

    //macd
    int mMacdBuyColor;
    int mMacdSellColor;
    int mMacdDifColor;
    int mAcdDeaColor;
    int mAcdMacdColor;
    Paint mMacdPaint;
    float mMacdLineWidth = 1;


    //rsi
    int mRsi16Color;
    int mRsi12Color;
    int mRsi24Color;
    //kdj
    int mKColor;
    int mDColor;
    int mJColor;

    //MinorModel聚合的数据
    MinorModel mMinorModel;

    //y轴上最大值和最小值
    protected double mMinY;
    protected double mMaxY;
    //蜡烛图间隙，大小以单个蜡烛图的宽度的比例算。可修改。
    protected float mCandleDiverWidthRatio = 0.1f;


    public MinorView(Context context) {
        this(context, null);
    }

    public MinorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MinorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        if (mQuotesList == null || mQuotesList.isEmpty()) {
            return;
        }
        //绘制右侧文字
        drawYRightTxt(canvas);
        //绘制非按下情况下图例
        drawNoPressLegend(canvas);

        if (mMinorModel.getMinorType() == MinorModel.MinorType.MACD) {
            drawMACD(canvas);
        } else if (mMinorModel.getMinorType() == MinorModel.MinorType.RSI) {
            drawRSI(canvas);
        } else if (mMinorModel.getMinorType() == MinorModel.MinorType.KDJ) {
            drawKDJ(canvas);
        }
    }

    private void drawNoPressLegend(Canvas canvas) {
        // FIXME: 2018/2/2 按下情况下则不显示

        String showTxt="";
        if (mMinorModel.getMinorType() == MinorModel.MinorType.MACD) {
            showTxt="MACD(12,26,9)";
        } else if (mMinorModel.getMinorType() == MinorModel.MinorType.RSI) {
            showTxt="RSI(6,12,24)";
        } else if (mMinorModel.getMinorType() == MinorModel.MinorType.KDJ) {
            showTxt="KDJ(9,3,3)";
        }
        canvas.drawText(showTxt,
                (float) (mWidth - mLegendPaddingRight - mPaddingRight - mLegendPaint.measureText(showTxt)),
                (float) (mLegendPaddingTop + mPaddingTop+getFontHeight(mLegendTxtSize,mLegendPaint)), mLegendPaint);
    }

    private void drawYRightTxt(Canvas canvas) {
        //绘制右侧的y轴文字
        //现将最小值、最大值画好
        float halfTxtHight = getFontHeight(mXYTxtSize, mXYTxtPaint) / 2;//应该/2的，但是不准确，原因不明
        float x = mWidth - mPaddingRight + mRightTxtPadding;
        float maxY = mPaddingTop + halfTxtHight + mInnerTopBlankPadding;
        float minY = mHeight - mPaddingBottom - halfTxtHight - mInnerBottomBlankPadding;
        //draw min
        canvas.drawText(FormatUtil.numFormat(mMinY, mDigits),
                x,
                minY, mXYTxtPaint);
        //draw max
        canvas.drawText(FormatUtil.numFormat(mMaxY, mDigits),
                x,
                maxY, mXYTxtPaint);
        //draw middle
        canvas.drawText(FormatUtil.numFormat((mMaxY + mMinY) / 2.0, mDigits),
                x, (minY + maxY) / 2.0f,
                mXYTxtPaint);
    }

    private void drawMACD(Canvas canvas) {

        //macd
        //首先寻找"0"点，这个点是正负macd的分界点
        float v = mHeight - mPaddingBottom - mInnerBottomBlankPadding;
        double zeroY = v - mPerY * (0 - mMinY);
        float startX, startY, stopX, stopY;

        //dif
        float difX, difY;
        Path difPath = new Path();

        //dea
        float deaX, deaY;
        Path deaPath = new Path();

        for (int i = mBeginIndex; i < mEndIndex; i++) {
            Quotes quotes = mQuotesList.get(i);

            /*macd*/
            //找另外一个y点
            double y = v - mPerY * (quotes.macd - mMinY);
            startX = mPaddingLeft + (i - mBeginIndex) * mPerX + mCandleDiverWidthRatio * mPerX / 2;
            stopX = mPaddingLeft + (i - mBeginIndex + 1) * mPerX - mCandleDiverWidthRatio * mPerX / 2;
            startY = (float) zeroY;
            stopY = (float) y;
            if (quotes.macd > 0) {
                mMacdPaint.setColor(mMacdBuyColor);
            } else {
                mMacdPaint.setColor(mMacdSellColor);
            }
            //            Log.e(TAG, "drawMACD: "+startY+","+stopY +
            //                    ","+(mPaddingTop+mInnerTopBlankPadding)+","+
            //                    (mHeight-mPaddingBottom-mInnerBottomBlankPadding));
            mMacdPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(startX, startY, stopX, stopY, mMacdPaint);


            /*dif*/
            difX = mPaddingLeft + (i - mBeginIndex) * mPerX + mPerX / 2;
            difY = (float) (v - mPerY * (quotes.dif - mMinY));
            if (i == mBeginIndex) {
                difPath.moveTo(difX - mPerX / 2, difY);//第一个点特殊处理
            } else {
                if (i == mEndIndex - 1) {
                    difX += mPerX / 2;//最后一个点特殊处理
                }
                difPath.lineTo(difX, difY);
            }
            mMacdPaint.setStyle(Paint.Style.STROKE);
            mMacdPaint.setColor(mMacdDifColor);
            canvas.drawPath(difPath, mMacdPaint);


            /*dea*/
            deaX = mPaddingLeft + (i - mBeginIndex) * mPerX + mPerX / 2;
            deaY = (float) (v - mPerY * (quotes.dea - mMinY));
            if (i == mBeginIndex) {
                deaPath.moveTo(deaX - mPerX / 2, deaY);//第一个点特殊处理
            } else {
                if (i == mEndIndex - 1) {
                    deaX += mPerX / 2;//最后一个点特殊处理
                }
                deaPath.lineTo(deaX, deaY);
            }
            mMacdPaint.setStyle(Paint.Style.STROKE);
            mMacdPaint.setColor(mAcdDeaColor);
            canvas.drawPath(deaPath, mMacdPaint);
        }

    }

    private void drawRSI(Canvas canvas) {

    }

    private void drawKDJ(Canvas canvas) {

    }

    private void initAttrs() {
        initDefAttrs();
        initColorRes();

        initMacdPaint();

    }

    private void initMacdPaint() {
        mMacdPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMacdPaint.setColor(mMacdBuyColor);
        mMacdPaint.setAntiAlias(true);
        mMacdPaint.setStrokeWidth(mMacdLineWidth);
    }

    private void initDefAttrs() {
        mMinorModel = new MinorModel();
        mMinorModel.setMinorType(MinorModel.MinorType.MACD);

        //重写内边距大小
        mInnerTopBlankPadding = 8;
        mInnerBottomBlankPadding = 8;

        //重写Legend padding
        mLegendPaddingTop=0;
        mLegendPaddingRight=4;
        mLegendPaddingLeft=4;


        setShowInnerX(false);
        setShowInnerY(true);
    }

    private void initColorRes() {
        //颜色
        mOuterStrokeColor = getColor(R.color.color_minorView_outerStrokeColor);
        mInnerXyDashColor = getColor(R.color.color_minorView_innerXyDashColor);
        mXyTxtColor = getColor(R.color.color_minorView_xYTxtColor);
        mLegendTxtColor = getColor(R.color.color_minorView_legendTxtColor);
        mLongPressTxtColor = getColor(R.color.color_minorView_longPressTxtColor);
        mMacdBuyColor = getColor(R.color.color_minorView_macdBuyColor);
        mMacdSellColor = getColor(R.color.color_minorView_macdSellColor);
        mMacdDifColor = getColor(R.color.color_minorView_macdDifColor);
        mAcdDeaColor = getColor(R.color.color_minorView_macdDeaColor);
        mAcdMacdColor = getColor(R.color.color_minorView_macdMacdColor);
        mRsi16Color = getColor(R.color.color_minorView_rsi16Color);
        mRsi12Color = getColor(R.color.color_minorView_rsi12Color);
        mRsi24Color = getColor(R.color.color_minorView_rsi24Color);
        mKColor = getColor(R.color.color_minorView_kColor);
        mDColor = getColor(R.color.color_minorView_dColor);
        mJColor = getColor(R.color.color_minorView_jColor);
    }

    @Override
    protected void seekAndCalculateCellData() {
        //设置数据
        mMinorModel.setQuotesList(mQuotesList);

        if (mMinorModel.getMinorType() == MinorModel.MinorType.MACD) {
            FinancialAlgorithm.calculateMACD(mMinorModel.getQuotesList());
        }
        if (mMinorModel.getMinorType() == MinorModel.MinorType.RSI) {

        }
        if (mMinorModel.getMinorType() == MinorModel.MinorType.KDJ) {

        }

        //找到close最大值和最小值
        double tempMinClosePrice = Integer.MAX_VALUE;
        double tempMaxClosePrice = Integer.MIN_VALUE;


        for (int i = mBeginIndex; i < mEndIndex; i++) {
            Quotes quotes = mQuotesList.get(i);
            if (i == mBeginIndex) {
                mBeginQuotes = quotes;
            }
            if (i == mEndIndex - 1) {
                mEndQuotes = quotes;
            }
            double min = FinancialAlgorithm.getMasterMinY(quotes, mMinorModel.getMinorType());
            double max = FinancialAlgorithm.getMasterMaxY(quotes, mMinorModel.getMinorType());

            if (min <= tempMinClosePrice) {
                tempMinClosePrice = min;
                mMinY = tempMinClosePrice;
            }
            if (max >= tempMaxClosePrice) {
                tempMaxClosePrice = max;
                mMaxY = tempMaxClosePrice;
            }

        }


        mPerX = (mWidth - mPaddingLeft - mPaddingRight - mInnerRightBlankPadding)
                / (mShownMaxCount);
        //不要忘了减去内部的上下Padding
        mPerY = (float) ((mHeight - mPaddingTop - mPaddingBottom - mInnerTopBlankPadding
                - mInnerBottomBlankPadding) / (mMaxY - mMinY));
        Log.e(TAG, "seekAndCalculateCellData: mMinY：" + mMinY + ",mMaxY:" + mMaxY);
        //重绘
        invalidate();
    }

}
