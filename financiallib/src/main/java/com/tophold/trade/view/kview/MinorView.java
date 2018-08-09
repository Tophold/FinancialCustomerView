package com.tophold.trade.view.kview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import com.tophold.trade.R;
import com.tophold.trade.utils.FormatUtil;


/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/12/14 17:56
 * 描 述 ：副图。该view提供各种副图指标。
 * ============================================================
 **/
public class MinorView extends KBaseView {


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
    int mMacdDeaColor;
    int mMacdMacdColor;
    Paint mMacdPaint;
    float mMacdLineWidth = 1;


    //rsi
    int mRsi6Color;
    int mRsi12Color;
    int mRsi24Color;
    Paint mRsiPaint;
    float mRsiLineWidth = 1;

    //kdj
    int mKColor;
    int mDColor;
    int mJColor;
    Paint mKdjPaint;
    float mKdjLineWidth = 1;

    //当前显示的指标
    KViewType.MinorIndicatrixType mMinorType = KViewType.MinorIndicatrixType.MACD;

    //y轴上最大值和最小值
    protected double mMinY;
    protected double mMaxY;
    //蜡烛图间隙，大小以单个蜡烛图的宽度的比例算。可修改。
    protected float mCandleDiverWidthRatio = 0.1f;

    //监听主图的长按事件
    private KViewListener.MinorListener mMinorListener;

    public MinorView(Context context) {
        this(context, null);
    }

    public MinorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MinorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs();
        initListener();
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
        Log.d(TAG, "onDraw: " + mBaseHeight + "," + mBaseWidth);
        if (mQuotesList == null || mQuotesList.isEmpty()) {
            return;
        }
        //绘制右侧文字
        drawYRightTxt(canvas);
        //绘制图例
        drawLegend(canvas);
        //绘制核心指标线
        drawMinorIndicatrix(canvas);
        //绘制长按线
        drawLongPress(canvas);
    }

    private void initAttrs() {
        initDefAttrs();
        initColorRes();
        initMacdPaint();
        initRsiPaint();
        initKdjPaint();

    }

    private void initKdjPaint() {
        mKdjPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mKdjPaint.setColor(mKColor);
        mKdjPaint.setAntiAlias(true);
        mKdjPaint.setStrokeWidth(mKdjLineWidth);
        mKdjPaint.setStyle(Paint.Style.STROKE);
        mKdjPaint.setTextSize(mLegendTxtSize);
    }

    private void initRsiPaint() {
        mRsiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRsiPaint.setColor(mRsi6Color);
        mRsiPaint.setAntiAlias(true);
        mRsiPaint.setStrokeWidth(mRsiLineWidth);
        mRsiPaint.setStyle(Paint.Style.STROKE);
        mRsiPaint.setTextSize(mLegendTxtSize);
    }

    private void initMacdPaint() {
        mMacdPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMacdPaint.setColor(mMacdBuyColor);
        mMacdPaint.setAntiAlias(true);
        mMacdPaint.setStrokeWidth(mMacdLineWidth);
        mMacdPaint.setTextSize(mLegendTxtSize);
    }

    private void initDefAttrs() {
        //重写内边距大小
        mInnerTopBlankPadding = 8;
        mInnerBottomBlankPadding = 8;

        //重写Legend padding
        mLegendPaddingTop = 0;
        mLegendPaddingRight = 4;
        mLegendPaddingLeft = 4;


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
        mMacdDeaColor = getColor(R.color.color_minorView_macdDeaColor);
        mMacdMacdColor = getColor(R.color.color_minorView_macdMacdColor);
        mRsi6Color = getColor(R.color.color_minorView_rsi6Color);
        mRsi12Color = getColor(R.color.color_minorView_rsi12Color);
        mRsi24Color = getColor(R.color.color_minorView_rsi24Color);
        mKColor = getColor(R.color.color_minorView_kColor);
        mDColor = getColor(R.color.color_minorView_dColor);
        mJColor = getColor(R.color.color_minorView_jColor);
    }

    private void initListener() {
        mMinorListener = new KViewListener.MinorListener() {
            @Override
            public void masterLongPressListener(int pressIndex, Quotes currQuotes) {
                mDrawLongPress = true;
                mCurrLongPressQuotes = mQuotesList.get(pressIndex);
                invalidate();
            }

            @Override
            public void masterNoLongPressListener() {
                mDrawLongPress = false;
                invalidate();
            }

            @Override
            public void masteZoomlNewIndex(int beginIndex, int endIndex, int shownMaxCount) {
                mBeginIndex = beginIndex;
                mEndIndex = endIndex;
                mShownMaxCount = shownMaxCount;
                seekAndCalculateCellData();
            }

            @Override
            public void mastelPullmNewIndex(int beginIndex, int endIndex, KViewType.PullType currPullType, int shownMaxCount) {
                mBeginIndex = beginIndex;
                mEndIndex = endIndex;
                mShownMaxCount = shownMaxCount;
                mPullType = currPullType;

                //处理右侧内边距
                if (currPullType == KViewType.PullType.PULL_RIGHT_STOP) {
                    //重置到之前的状态
                    mInnerRightBlankPadding = DEF_INNER_RIGHT_BLANK_PADDING;
                } else {
                    mInnerRightBlankPadding = 0;
                }

                seekAndCalculateCellData();
            }
        };
    }

    private void drawLegend(Canvas canvas) {
        //绘制非按下情况下图例
        drawNoPressLegend(canvas);
        //绘制按下的图例
        drawPressLegend(canvas);
    }

    private void drawMinorIndicatrix(Canvas canvas) {
        mMacdPaint.setStyle(Paint.Style.STROKE);
        mRsiPaint.setStyle(Paint.Style.STROKE);
        mKdjPaint.setStyle(Paint.Style.STROKE);
        if (mMinorType == KViewType.MinorIndicatrixType.MACD) {
            drawMACD(canvas);
        } else if (mMinorType == KViewType.MinorIndicatrixType.RSI) {
            drawRSI(canvas);
        } else if (mMinorType == KViewType.MinorIndicatrixType.KDJ) {
            drawKDJ(canvas);
        }
    }

    private void drawLongPress(Canvas canvas) {
        if (!mDrawLongPress) return;
        if (mCurrLongPressQuotes == null) return;

        //y轴线
        canvas.drawLine(mCurrLongPressQuotes.floatX, mBasePaddingTop, mCurrLongPressQuotes.floatX,
                mBaseHeight - mBasePaddingBottom, mLongPressPaint);
    }

    private void drawPressLegend(Canvas canvas) {
        if (!mDrawLongPress) return;

        mMacdPaint.setStyle(Paint.Style.FILL);
        mRsiPaint.setStyle(Paint.Style.FILL);
        mKdjPaint.setStyle(Paint.Style.FILL);

        float x = (float) (mLegendPaddingLeft + mBasePaddingLeft);
        float y = (float) (mLegendPaddingTop + mBasePaddingTop) + getFontHeight(mLegendTxtSize, mMacdPaint);

        String showTxt;
        switch (mMinorType) {
            case MACD:
                showTxt = "DIFF:" + FormatUtil.numFormat(mCurrLongPressQuotes.dif, mDigits) + " ";
                mMacdPaint.setColor(mMacdDifColor);
                canvas.drawText(showTxt, x,
                        y, mMacdPaint);

                float leftWidth11 = mMacdPaint.measureText(showTxt);
                showTxt = "DEA:" + FormatUtil.numFormat(mCurrLongPressQuotes.dea, mDigits) + " ";
                mMacdPaint.setColor(mMacdDeaColor);
                canvas.drawText(showTxt, x + leftWidth11, y, mMacdPaint);

                float leftWidth12 = mMacdPaint.measureText(showTxt);
                showTxt = "MACD:" + FormatUtil.numFormat(mCurrLongPressQuotes.macd, mDigits) + " ";
                mMacdPaint.setColor(mMacdMacdColor);
                canvas.drawText(showTxt, (x + leftWidth11 + leftWidth12),
                        y, mMacdPaint);
                break;
            case RSI:
                showTxt = "RSI6:" + FormatUtil.numFormat(mCurrLongPressQuotes.rsi6, mDigits) + " ";
                mRsiPaint.setColor(mRsi6Color);
                canvas.drawText(showTxt, (x),
                        y, mRsiPaint);

                float leftWidth21 = mRsiPaint.measureText(showTxt);
                showTxt = "RSI12:" + FormatUtil.numFormat(mCurrLongPressQuotes.rsi12, mDigits) + " ";
                mRsiPaint.setColor(mRsi12Color);
                canvas.drawText(showTxt, (x + leftWidth21),
                        y, mRsiPaint);

                float leftWidth22 = mRsiPaint.measureText(showTxt);
                showTxt = "RSI24:" + FormatUtil.numFormat(mCurrLongPressQuotes.rsi24, mDigits) + " ";
                mRsiPaint.setColor(mRsi24Color);
                canvas.drawText(showTxt, (x + leftWidth21 + leftWidth22),
                        y, mRsiPaint);
                break;
            case KDJ:
                showTxt = "K:" + FormatUtil.numFormat(mCurrLongPressQuotes.k, mDigits) + " ";
                mKdjPaint.setColor(mKColor);
                canvas.drawText(showTxt, (x),
                        y, mKdjPaint);

                float leftWidth = mKdjPaint.measureText(showTxt);
                showTxt = "D:" + FormatUtil.numFormat(mCurrLongPressQuotes.d, mDigits) + " ";
                mKdjPaint.setColor(mDColor);
                canvas.drawText(showTxt, (x + leftWidth),
                        y, mKdjPaint);

                float leftWidth2 = mKdjPaint.measureText(showTxt);
                showTxt = "J:" + FormatUtil.numFormat(mCurrLongPressQuotes.j, mDigits) + " ";
                mKdjPaint.setColor(mJColor);
                canvas.drawText(showTxt, (x + leftWidth + leftWidth2),
                        y, mKdjPaint);
                break;
            default:
                break;
        }

    }

    private void drawNoPressLegend(Canvas canvas) {
        if (mDrawLongPress) return;
        String showTxt = "";
        if (mMinorType == KViewType.MinorIndicatrixType.MACD) {
            showTxt = "MACD(12,26,9)";
        } else if (mMinorType == KViewType.MinorIndicatrixType.RSI) {
            showTxt = "RSI(6,12,24)";
        } else if (mMinorType == KViewType.MinorIndicatrixType.KDJ) {
            showTxt = "KDJ(9,3,3)";
        }
        canvas.drawText(showTxt,
                (float) (mBaseWidth - mLegendPaddingRight - mBasePaddingRight - mLegendPaint.measureText(showTxt)),
                (float) (mLegendPaddingTop + mBasePaddingTop + getFontHeight(mLegendTxtSize, mLegendPaint)), mLegendPaint);
    }

    private void drawYRightTxt(Canvas canvas) {
        //绘制右侧的y轴文字
        //现将最小值、最大值画好
        float halfTxtHight = getFontHeight(mXYTxtSize, mXYTxtPaint) / 2;//应该/2的，但是不准确，原因不明
        float x = mBaseWidth - mBasePaddingRight + mRightTxtPadding;
        float maxY = mBasePaddingTop + halfTxtHight + mInnerTopBlankPadding;
        float minY = mBaseHeight - mBasePaddingBottom - halfTxtHight - mInnerBottomBlankPadding;
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
        float v = mBaseHeight - mBasePaddingBottom - mInnerBottomBlankPadding;
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
            startX = mBasePaddingLeft + (i - mBeginIndex) * mPerX + mCandleDiverWidthRatio * mPerX / 2;
            stopX = mBasePaddingLeft + (i - mBeginIndex + 1) * mPerX - mCandleDiverWidthRatio * mPerX / 2;
            startY = (float) zeroY;
            stopY = (float) y;
            if (quotes.macd > 0) {
                mMacdPaint.setColor(mMacdBuyColor);
            } else {
                mMacdPaint.setColor(mMacdSellColor);
            }
            //            Log.e(TAG, "drawMACD: "+startY+","+stopY +
            //                    ","+(mBasePaddingTop+mInnerTopBlankPadding)+","+
            //                    (mBaseHeight-mBasePaddingBottom-mInnerBottomBlankPadding));
            mMacdPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(startX, startY, stopX, stopY, mMacdPaint);


            /*dif*/
            difX = mBasePaddingLeft + (i - mBeginIndex) * mPerX + mPerX / 2;
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
            deaX = mBasePaddingLeft + (i - mBeginIndex) * mPerX + mPerX / 2;
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
            mMacdPaint.setColor(mMacdDeaColor);
            canvas.drawPath(deaPath, mMacdPaint);
        }

    }

    private void drawRSI(Canvas canvas) {

        float rsiX;

        //rsi6
        float rsi6Y;
        Path rsi6Path = new Path();

        //rsi12
        float rsi12Y;
        Path rsi12Path = new Path();

        //rsi24
        float rsi24Y;
        Path rsi24Path = new Path();

        float v = mBaseHeight - mBasePaddingBottom - mInnerBottomBlankPadding;
        for (int i = mBeginIndex; i < mEndIndex; i++) {
            Quotes quotes = mQuotesList.get(i);

            /*rsi6*/
            rsiX = mBasePaddingLeft + (i - mBeginIndex) * mPerX + mPerX / 2;
            rsi6Y = (float) (v - mPerY * (quotes.rsi6 - mMinY));
            if (i == mBeginIndex) {
                rsi6Path.moveTo(rsiX - mPerX / 2, rsi6Y);//第一个点特殊处理
            } else {
                if (i == mEndIndex - 1) {
                    rsiX += mPerX / 2;//最后一个点特殊处理
                }
                rsi6Path.lineTo(rsiX, rsi6Y);
            }
            mRsiPaint.setColor(mRsi6Color);
            canvas.drawPath(rsi6Path, mRsiPaint);

            /*rsi12*/
            //为什么这里重复再赋一遍值？因为下面有一个"rsiX +="操作
            rsiX = mBasePaddingLeft + (i - mBeginIndex) * mPerX + mPerX / 2;
            rsi12Y = (float) (v - mPerY * (quotes.rsi12 - mMinY));
            if (i == mBeginIndex) {
                rsi12Path.moveTo(rsiX - mPerX / 2, rsi12Y);//第一个点特殊处理
            } else {
                if (i == mEndIndex - 1) {
                    rsiX += mPerX / 2;//最后一个点特殊处理
                }
                rsi12Path.lineTo(rsiX, rsi12Y);
            }
            mRsiPaint.setColor(mRsi12Color);
            canvas.drawPath(rsi12Path, mRsiPaint);

             /*rsi24*/
            //为什么这里重复再赋一遍值？因为下面有一个"rsiX +="操作
            rsiX = mBasePaddingLeft + (i - mBeginIndex) * mPerX + mPerX / 2;
            rsi24Y = (float) (v - mPerY * (quotes.rsi24 - mMinY));
            if (i == mBeginIndex) {
                rsi24Path.moveTo(rsiX - mPerX / 2, rsi24Y);//第一个点特殊处理
            } else {
                if (i == mEndIndex - 1) {
                    rsiX += mPerX / 2;//最后一个点特殊处理
                }
                rsi24Path.lineTo(rsiX, rsi24Y);
            }
            mRsiPaint.setColor(mRsi24Color);
            canvas.drawPath(rsi24Path, mRsiPaint);

        }
    }

    private void drawKDJ(Canvas canvas) {

        float kdjX;

        //k
        float kY;
        Path kPath = new Path();

        //d
        float dY;
        Path dPath = new Path();

        //j
        float jY;
        Path jPath = new Path();

        float v = mBaseHeight - mBasePaddingBottom - mInnerBottomBlankPadding;
        for (int i = mBeginIndex; i < mEndIndex; i++) {
            Quotes quotes = mQuotesList.get(i);

            /*k*/
            kdjX = mBasePaddingLeft + (i - mBeginIndex) * mPerX + mPerX / 2;
            kY = (float) (v - mPerY * (quotes.k - mMinY));
            if (i == mBeginIndex) {
                kPath.moveTo(kdjX - mPerX / 2, kY);//第一个点特殊处理
            } else {
                if (i == mEndIndex - 1) {
                    kdjX += mPerX / 2;//最后一个点特殊处理
                }
                kPath.lineTo(kdjX, kY);
            }
            mKdjPaint.setColor(mKColor);
            canvas.drawPath(kPath, mKdjPaint);

            /*d*/
            kdjX = mBasePaddingLeft + (i - mBeginIndex) * mPerX + mPerX / 2;
            dY = (float) (v - mPerY * (quotes.d - mMinY));
            if (i == mBeginIndex) {
                dPath.moveTo(kdjX - mPerX / 2, dY);//第一个点特殊处理
            } else {
                if (i == mEndIndex - 1) {
                    kdjX += mPerX / 2;//最后一个点特殊处理
                }
                dPath.lineTo(kdjX, dY);
            }
            mKdjPaint.setColor(mDColor);
            canvas.drawPath(dPath, mKdjPaint);

             /*j*/
            kdjX = mBasePaddingLeft + (i - mBeginIndex) * mPerX + mPerX / 2;
            jY = (float) (v - mPerY * (quotes.j - mMinY));
            if (i == mBeginIndex) {
                jPath.moveTo(kdjX - mPerX / 2, jY);//第一个点特殊处理
            } else {
                if (i == mEndIndex - 1) {
                    kdjX += mPerX / 2;//最后一个点特殊处理
                }
                jPath.lineTo(kdjX, jY);
            }
            mKdjPaint.setColor(mJColor);
            canvas.drawPath(jPath, mKdjPaint);

        }
    }

    @Override
    protected void seekAndCalculateCellData() {
        if (mQuotesList == null || mQuotesList.isEmpty()) return;

        if (mMinorType == KViewType.MinorIndicatrixType.MACD) {
            FinancialAlgorithm.calculateMACD(mQuotesList);
        } else if (mMinorType == KViewType.MinorIndicatrixType.RSI) {
            FinancialAlgorithm.calculateRSI(mQuotesList);
        } else if (mMinorType == KViewType.MinorIndicatrixType.KDJ) {
            FinancialAlgorithm.calculateKDJ(mQuotesList);
        }

        //找到close最大值和最小值
        mMinY = Integer.MAX_VALUE;
        mMaxY = Integer.MIN_VALUE;


        for (int i = mBeginIndex; i < mEndIndex; i++) {
            Quotes quotes = mQuotesList.get(i);
            if (i == mBeginIndex) {
                mBeginQuotes = quotes;
            }
            if (i == mEndIndex - 1) {
                mEndQuotes = quotes;
            }
            double min = FinancialAlgorithm.getMinorMinY(quotes, mMinorType);
            double max = FinancialAlgorithm.getMinorMaxY(quotes, mMinorType);

            if (min <= mMinY) {
                mMinY = min;
            }
            if (max >= mMaxY) {
                mMaxY = max;
            }

        }


        mPerX = (mBaseWidth - mBasePaddingLeft - mBasePaddingRight - mInnerRightBlankPadding)
                / (mShownMaxCount);
        //不要忘了减去内部的上下Padding
        mPerY = (float) ((mBaseHeight - mBasePaddingTop - mBasePaddingBottom - mInnerTopBlankPadding
                - mInnerBottomBlankPadding) / (mMaxY - mMinY));
        Log.e(TAG, "seekAndCalculateCellData: mMinY：" + mMinY + ",mMaxY:" + mMaxY);
        //重绘
        invalidate();
    }

    @Override
    protected void innerClickListener() {
        super.innerClickListener();
        if (mMinorType == KViewType.MinorIndicatrixType.MACD) {
            mMinorType = KViewType.MinorIndicatrixType.RSI;
        } else if (mMinorType == KViewType.MinorIndicatrixType.RSI) {
            mMinorType = KViewType.MinorIndicatrixType.KDJ;
        } else if (mMinorType == KViewType.MinorIndicatrixType.KDJ) {
            mMinorType = KViewType.MinorIndicatrixType.MACD;
        }
        setMinorType(mMinorType);
    }

    @Override
    protected void innerLongClickListener(float x, float y) {
        super.innerLongClickListener(x, y);

    }

    @Override
    protected void innerHiddenLongClick() {
        super.innerHiddenLongClick();
    }

    @Override
    protected void innerMoveViewListener(float moveXLen) {
        super.innerMoveViewListener(moveXLen);

    }


    //-----------------------对开发者暴露可以修改的参数-------

    public void setMinorType(KViewType.MinorIndicatrixType minorType) {
        mMinorType = minorType;

        seekAndCalculateCellData();
    }

    public int getOuterStrokeColor() {
        return mOuterStrokeColor;
    }

    public MinorView setOuterStrokeColor(int outerStrokeColor) {
        mOuterStrokeColor = outerStrokeColor;
        return this;
    }

    public int getInnerXyDashColor() {
        return mInnerXyDashColor;
    }

    public MinorView setInnerXyDashColor(int innerXyDashColor) {
        mInnerXyDashColor = innerXyDashColor;
        return this;
    }

    public int getXyTxtColor() {
        return mXyTxtColor;
    }

    public MinorView setXyTxtColor(int xyTxtColor) {
        mXyTxtColor = xyTxtColor;
        return this;
    }

    public int getLegendTxtColor() {
        return mLegendTxtColor;
    }

    public MinorView setLegendTxtColor(int legendTxtColor) {
        mLegendTxtColor = legendTxtColor;
        return this;
    }

    public int getLongPressTxtColor() {
        return mLongPressTxtColor;
    }

    public MinorView setLongPressTxtColor(int longPressTxtColor) {
        mLongPressTxtColor = longPressTxtColor;
        return this;
    }

    public int getMacdBuyColor() {
        return mMacdBuyColor;
    }

    public MinorView setMacdBuyColor(int macdBuyColor) {
        mMacdBuyColor = macdBuyColor;
        return this;
    }

    public int getMacdSellColor() {
        return mMacdSellColor;
    }

    public MinorView setMacdSellColor(int macdSellColor) {
        mMacdSellColor = macdSellColor;
        return this;
    }

    public int getMacdDifColor() {
        return mMacdDifColor;
    }

    public MinorView setMacdDifColor(int macdDifColor) {
        mMacdDifColor = macdDifColor;
        return this;
    }

    public int getMacdDeaColor() {
        return mMacdDeaColor;
    }

    public MinorView setMacdDeaColor(int macdDeaColor) {
        mMacdDeaColor = macdDeaColor;
        return this;
    }

    public int getMacdMacdColor() {
        return mMacdMacdColor;
    }

    public MinorView setMacdMacdColor(int macdMacdColor) {
        mMacdMacdColor = macdMacdColor;
        return this;
    }

    public Paint getMacdPaint() {
        return mMacdPaint;
    }

    public MinorView setMacdPaint(Paint macdPaint) {
        mMacdPaint = macdPaint;
        return this;
    }

    public float getMacdLineWidth() {
        return mMacdLineWidth;
    }

    public MinorView setMacdLineWidth(float macdLineWidth) {
        mMacdLineWidth = macdLineWidth;
        return this;
    }

    public int getRsi6Color() {
        return mRsi6Color;
    }

    public MinorView setRsi6Color(int rsi6Color) {
        mRsi6Color = rsi6Color;
        return this;
    }

    public int getRsi12Color() {
        return mRsi12Color;
    }

    public MinorView setRsi12Color(int rsi12Color) {
        mRsi12Color = rsi12Color;
        return this;
    }

    public int getRsi24Color() {
        return mRsi24Color;
    }

    public MinorView setRsi24Color(int rsi24Color) {
        mRsi24Color = rsi24Color;
        return this;
    }

    public Paint getRsiPaint() {
        return mRsiPaint;
    }

    public MinorView setRsiPaint(Paint rsiPaint) {
        mRsiPaint = rsiPaint;
        return this;
    }

    public float getRsiLineWidth() {
        return mRsiLineWidth;
    }

    public MinorView setRsiLineWidth(float rsiLineWidth) {
        mRsiLineWidth = rsiLineWidth;
        return this;
    }

    public int getKColor() {
        return mKColor;
    }

    public MinorView setKColor(int KColor) {
        mKColor = KColor;
        return this;
    }

    public int getDColor() {
        return mDColor;
    }

    public MinorView setDColor(int DColor) {
        mDColor = DColor;
        return this;
    }

    public int getJColor() {
        return mJColor;
    }

    public MinorView setJColor(int JColor) {
        mJColor = JColor;
        return this;
    }

    public Paint getKdjPaint() {
        return mKdjPaint;
    }

    public MinorView setKdjPaint(Paint kdjPaint) {
        mKdjPaint = kdjPaint;
        return this;
    }

    public float getKdjLineWidth() {
        return mKdjLineWidth;
    }

    public MinorView setKdjLineWidth(float kdjLineWidth) {
        mKdjLineWidth = kdjLineWidth;
        return this;
    }

    public KViewType.MinorIndicatrixType getMinorType() {
        return mMinorType;
    }

    public double getMinY() {
        return mMinY;
    }

    public MinorView setMinY(double minY) {
        mMinY = minY;
        return this;
    }

    public double getMaxY() {
        return mMaxY;
    }

    public MinorView setMaxY(double maxY) {
        mMaxY = maxY;
        return this;
    }

    public float getCandleDiverWidthRatio() {
        return mCandleDiverWidthRatio;
    }

    public MinorView setCandleDiverWidthRatio(float candleDiverWidthRatio) {
        mCandleDiverWidthRatio = candleDiverWidthRatio;
        return this;
    }

    public MinorView setMinorListener(KViewListener.MinorListener minorListener) {
        mMinorListener = minorListener;
        return this;
    }

    public KViewListener.MinorListener getMinorListener() {
        return mMinorListener;
    }
}
