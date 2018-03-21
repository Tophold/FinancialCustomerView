package wgyscsf.financialcustomerview.financialview.kview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import wgyscsf.financialcustomerview.R;
import wgyscsf.financialcustomerview.financialview.FinancialAlgorithm;
import wgyscsf.financialcustomerview.utils.FormatUtil;

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
    MinorType mMinorType = MinorType.MACD;

    //y轴上最大值和最小值
    protected double mMinY;
    protected double mMaxY;
    //蜡烛图间隙，大小以单个蜡烛图的宽度的比例算。可修改。
    protected float mCandleDiverWidthRatio = 0.1f;

    //监听主图的长按事件
    private MinorView.MasterListener mMasterListener;

    public MasterListener getMasterListener() {
        return mMasterListener;
    }

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

    private void initListener() {
        mMasterListener = new MasterListener() {
            @Override
            public void masterLongPressListener(float currX, Quotes currQuotes) {
                mDrawLongPress = true;
                mCurrLongPressQuotes = currQuotes;
                invalidate();
            }

            @Override
            public void masterNoLongPressListener() {
                mDrawLongPress = false;
                invalidate();
            }

            @Override
            public void masteZoomlNewIndex(int beginIndex, int endIndex,int shownMaxCount) {
                mBeginIndex=beginIndex;
                mEndIndex=endIndex;
                mShownMaxCount=shownMaxCount;
                seekAndCalculateCellData();
            }

            @Override
            public void mastelPullmNewIndex(int beginIndex, int endIndex, PullType currPullType, int shownMaxCount) {
                mBeginIndex=beginIndex;
                mEndIndex=endIndex;
                mShownMaxCount=shownMaxCount;
                mPullType=currPullType;

                //处理右侧内边距
                if(currPullType==PullType.PULL_RIGHT_STOP){
                    //重置到之前的状态
                    mInnerRightBlankPadding = DEF_INNER_RIGHT_BLANK_PADDING;
                }else{
                    mInnerRightBlankPadding=0;
                }

                seekAndCalculateCellData();
            }
        };
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
        Log.d(TAG, "onDraw: " + mHeight + "," + mWidth);
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

    private void drawLegend(Canvas canvas) {
        //绘制非按下情况下图例
        drawNoPressLegend(canvas);
        //绘制按下的图例
        drawPressLegend(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    private void drawMinorIndicatrix(Canvas canvas) {
        mMacdPaint.setStyle(Paint.Style.STROKE);
        mRsiPaint.setStyle(Paint.Style.STROKE);
        mKdjPaint.setStyle(Paint.Style.STROKE);
        if (mMinorType == MinorType.MACD) {
            drawMACD(canvas);
        } else if (mMinorType == MinorType.RSI) {
            drawRSI(canvas);
        } else if (mMinorType == MinorType.KDJ) {
            drawKDJ(canvas);
        }
    }

    private void drawLongPress(Canvas canvas) {
        if (!mDrawLongPress) return;
        if (mCurrLongPressQuotes == null) return;

        //y轴线
        canvas.drawLine(mCurrLongPressQuotes.floatX, mPaddingTop, mCurrLongPressQuotes.floatX,
                mHeight - mPaddingBottom, mLongPressPaint);
    }

    private void drawPressLegend(Canvas canvas) {
        if (!mDrawLongPress) return;

        mMacdPaint.setStyle(Paint.Style.FILL);
        mRsiPaint.setStyle(Paint.Style.FILL);
        mKdjPaint.setStyle(Paint.Style.FILL);

        float x= (float) (mLegendPaddingLeft + mPaddingLeft);
        float y= (float) (mLegendPaddingTop + mPaddingTop)+ getFontHeight(mLegendTxtSize, mMacdPaint);

        String showTxt;
        switch (mMinorType) {
            case MACD:
                showTxt = "DIFF:" + FormatUtil.formatBySubString(mCurrLongPressQuotes.dif, mDigits) + " ";
                mMacdPaint.setColor(mMacdDifColor);
                canvas.drawText(showTxt, x,
                        y, mMacdPaint);

                float leftWidth11 = mMacdPaint.measureText(showTxt);
                showTxt = "DEA:" + FormatUtil.formatBySubString(mCurrLongPressQuotes.dea, mDigits) + " ";
                mMacdPaint.setColor(mMacdDeaColor);
                canvas.drawText(showTxt, x + leftWidth11, y, mMacdPaint);

                float leftWidth12 = mMacdPaint.measureText(showTxt);
                showTxt = "MACD:" + FormatUtil.formatBySubString(mCurrLongPressQuotes.macd, mDigits) + " ";
                mMacdPaint.setColor(mMacdMacdColor);
                canvas.drawText(showTxt,  (x + leftWidth11 + leftWidth12),
                        y, mMacdPaint);
                break;
            case RSI:
                showTxt = "RSI6:" + FormatUtil.formatBySubString(mCurrLongPressQuotes.rsi6, mDigits) + " ";
                mRsiPaint.setColor(mRsi6Color);
                canvas.drawText(showTxt,  (x),
                        y, mRsiPaint);

                float leftWidth21 = mRsiPaint.measureText(showTxt);
                showTxt = "RSI12:" + FormatUtil.formatBySubString(mCurrLongPressQuotes.rsi12, mDigits) + " ";
                mRsiPaint.setColor(mRsi12Color);
                canvas.drawText(showTxt,  (x + leftWidth21),
                        y, mRsiPaint);

                float leftWidth22 = mRsiPaint.measureText(showTxt);
                showTxt = "RSI24:" + FormatUtil.formatBySubString(mCurrLongPressQuotes.rsi24, mDigits) + " ";
                mRsiPaint.setColor(mRsi24Color);
                canvas.drawText(showTxt,  (x + leftWidth21 + leftWidth22),
                        y, mRsiPaint);
                break;
            case KDJ:
                showTxt = "K:" + FormatUtil.formatBySubString(mCurrLongPressQuotes.k, mDigits) + " ";
                mKdjPaint.setColor(mKColor);
                canvas.drawText(showTxt,  (x),
                        y, mKdjPaint);

                float leftWidth = mKdjPaint.measureText(showTxt);
                showTxt = "D:" + FormatUtil.formatBySubString(mCurrLongPressQuotes.d, mDigits) + " ";
                mKdjPaint.setColor(mDColor);
                canvas.drawText(showTxt,  (x + leftWidth),
                        y, mKdjPaint);

                float leftWidth2 = mKdjPaint.measureText(showTxt);
                showTxt = "J:" + FormatUtil.formatBySubString(mCurrLongPressQuotes.j, mDigits) + " ";
                mKdjPaint.setColor(mJColor);
                canvas.drawText(showTxt,  (x + leftWidth + leftWidth2),
                        y, mKdjPaint);
                break;
            default:
                break;
        }

    }

    private void drawNoPressLegend(Canvas canvas) {
        if (mDrawLongPress) return;
        String showTxt = "";
        if (mMinorType == MinorType.MACD) {
            showTxt = "MACD(12,26,9)";
        } else if (mMinorType == MinorType.RSI) {
            showTxt = "RSI(6,12,24)";
        } else if (mMinorType == MinorType.KDJ) {
            showTxt = "KDJ(9,3,3)";
        }
        canvas.drawText(showTxt,
                (float) (mWidth - mLegendPaddingRight - mPaddingRight - mLegendPaint.measureText(showTxt)),
                (float) (mLegendPaddingTop + mPaddingTop + getFontHeight(mLegendTxtSize, mLegendPaint)), mLegendPaint);
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

        float v = mHeight - mPaddingBottom - mInnerBottomBlankPadding;
        for (int i = mBeginIndex; i < mEndIndex; i++) {
            Quotes quotes = mQuotesList.get(i);

            /*rsi6*/
            rsiX = mPaddingLeft + (i - mBeginIndex) * mPerX + mPerX / 2;
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
            rsiX = mPaddingLeft + (i - mBeginIndex) * mPerX + mPerX / 2;
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
            rsiX = mPaddingLeft + (i - mBeginIndex) * mPerX + mPerX / 2;
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

        float v = mHeight - mPaddingBottom - mInnerBottomBlankPadding;
        for (int i = mBeginIndex; i < mEndIndex; i++) {
            Quotes quotes = mQuotesList.get(i);

            /*k*/
            kdjX = mPaddingLeft + (i - mBeginIndex) * mPerX + mPerX / 2;
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
            kdjX = mPaddingLeft + (i - mBeginIndex) * mPerX + mPerX / 2;
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
            kdjX = mPaddingLeft + (i - mBeginIndex) * mPerX + mPerX / 2;
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

    private void initAttrs() {
        initDefAttrs();
        initColorRes();

        //手势
        mScaleGestureDetector = new ScaleGestureDetector(mContext, mOnScaleGestureListener);


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

    @Override
    protected void seekAndCalculateCellData() {
        if (mMinorType == MinorType.MACD) {
            FinancialAlgorithm.calculateMACD(mQuotesList);
        }
        if (mMinorType == MinorType.RSI) {
            FinancialAlgorithm.calculateRSI(mQuotesList);
        }
        if (mMinorType == MinorType.KDJ) {
            FinancialAlgorithm.calculateKDJ(mQuotesList);
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
            double min = FinancialAlgorithm.getMasterMinY(quotes, mMinorType);
            double max = FinancialAlgorithm.getMasterMaxY(quotes, mMinorType);

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

    public void setMinorType(MinorType minorType) {
        mMinorType = minorType;

        seekAndCalculateCellData();
    }

    //副图正在展示的类型
    public enum MinorType {
        MACD,
        RSI,
        KDJ
    }

    @Override
    protected void innerClickListener() {
        super.innerClickListener();
        if (mMinorType == MinorType.MACD) {
            mMinorType = MinorType.RSI;
        } else if (mMinorType == MinorType.RSI) {
            mMinorType = MinorType.KDJ;
        } else if (mMinorType == MinorType.KDJ) {
            mMinorType = MinorType.MACD;
        }
        setMinorType(mMinorType);
    }


    //缩放手势监听
    ScaleGestureDetector.OnScaleGestureListener mOnScaleGestureListener =
            new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    //没有缩放
                    if (detector.getScaleFactor() == 1) return true;

                    //是放大还是缩小
                    boolean isBigger = detector.getScaleFactor() > 1;

                    //变化的个数（缩小或者放大），必须向上取整，不然当mShownMaxCount过小时容易取到0。
                    int changeNum = (int) Math.ceil(mShownMaxCount * Math.abs(detector.getScaleFactor() - 1));

                    //一半
                    int helfChangeNum = (int) Math.ceil(changeNum / 2f);

                    //缩放个数太少，直接return
                    if (changeNum == 0 || helfChangeNum == 0) return true;

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

                    //只要找好起始点和结束点就可以交给处理重绘的方法就好啦~
                    seekAndCalculateCellData();
                    return true;
                }

                @Override
                public boolean onScaleBegin(ScaleGestureDetector detector) {
                    //指头数量
                    if (mFingerPressedCount != 2) return true;
                    return true;
                }
            };

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

    public interface MasterListener {
        /**
         * 长按操作
         * @param x  按下所对应的x坐标
         * @param currQuotes 按下所对应的点
         */
        void masterLongPressListener(float x, Quotes currQuotes);

        /**
         * 不再长按回调
         */
        void masterNoLongPressListener();

        /**
         * 缩放
         * @param beginIndex 缩放后的起始位置索引
         * @param endIndex 缩放后的结束索引
         * @param shownMaxCount 可见数据总条数
         */
        void masteZoomlNewIndex(int beginIndex, int endIndex,int shownMaxCount);

        /**
         * 左右滑动
         * @param endIndex 滑动后的结束索引
         * @param currPullType 当前PullType类型
         * @param shownMaxCount
         * @param shownMaxCount 可见数据总条数
         */
        void mastelPullmNewIndex(int beginIndex, int endIndex, PullType currPullType, int shownMaxCount);

    }
}
