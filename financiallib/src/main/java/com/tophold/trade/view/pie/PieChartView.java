package com.tophold.trade.view.pie;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import com.tophold.trade.R;
import com.tophold.trade.utils.GsonUtil;
import com.tophold.trade.utils.ScreenUtils;
import com.tophold.trade.utils.StringUtils;
import com.tophold.trade.view.BaseView;


import java.util.List;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 更新时间 ：2018/07/10 20:40
 * 描 述 ：对饼形图实现给出一个思路。之前看MP实现如果改成这样可能比较麻烦一点，这里就直接进行是实现。
 * 饼形图的实现难点不在于图的实现，而在于图周围的指示文字的控制，如果处理不好就会出现挤压问题。这里的处理思路是：给出最小比例，
 * 如果小于这个比例直接从最大份中"借"出一点满足最小比例。
 * ============================================================
 */
public class PieChartView extends BaseView {

    public static final float DEF_PARTSTHRESHOLD = 0.1f;

    List<PieEntrys> mPieEntryList;

    /**
     * 内边距
     */
    float basePaddingTop = 45;
    float basePaddingBottom = 45;
    float basePaddingLeft;
    float basePaddingRight;

    //圆环宽度
    float roundWidth = 30;
    //高亮环的宽度
    float highLightWidth = 4;
    //高亮环距离圆环的距离
    float highLightPadding = 5;

    //指示线距离圆环的边距
    float lineLengthMargin = 10;
    //指示线延伸长度
    float lineLength = 85;
    //指示线的宽度
    float lineWidth = 1f;
    //是否延伸长度的阀值
    float lineThreshold = 10;
    //延长的宽度
    float lineThresholdLength = 50;

    //指示文字下边距
    float textPaddingBottom = 2;
    //圆点半径
    float dotRadius = 2;

    //上下两个txt之间的y轴的距离
    float txtThresholdmargin = 20;
    //上下两个txt之间的y轴的阀值
    float txtThreshold = 20;

    //文字大小
    float textSize = 12;
    //饼图item最小的占比。0~1，定义最小的item,防止积压在一起。初始值设置小一点，为了动画好看。
    float minPartsThreshold = 0.1f;


    //没有数据显示
    String mEmptyTxt = "还没有交易哦";

    //开始角度
    float mBeginAngle = -90;

    /**
     * 自定义动画
     */
    private PieChartAnimation mAnimation;
    long mAnim = 1000;

    //私有属性，不对外暴露
    private float centerX;
    private float centerY;
    private boolean firstCome = true;

    public PieChartView(Context context) {
        this(context, null);
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs();
    }

    private void initAttrs() {
        mAnimation = new PieChartAnimation();
        mAnimation.setDuration(mAnim);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 所有单位传递标准化参数即可，这里进行转换。
     */
    private void allDp2Px() {
        if (!firstCome) return;
        firstCome = false;

        basePaddingLeft = ScreenUtils.dip2px(basePaddingLeft);
        basePaddingRight = ScreenUtils.dip2px(basePaddingRight);
        basePaddingTop = ScreenUtils.dip2px(basePaddingTop);
        basePaddingBottom = ScreenUtils.dip2px(basePaddingBottom);
        highLightWidth = ScreenUtils.dip2px(highLightWidth);
        highLightPadding = ScreenUtils.dip2px(highLightPadding);
        roundWidth = ScreenUtils.dip2px(roundWidth);
        lineLength = ScreenUtils.dip2px(lineLength);
        lineWidth = ScreenUtils.dip2px(lineWidth);
        dotRadius = ScreenUtils.dip2px(dotRadius);
        textSize = ScreenUtils.sp2px(textSize);
        textPaddingBottom = ScreenUtils.dip2px(textPaddingBottom);
        lineThreshold = ScreenUtils.dip2px(lineThreshold);
        lineThresholdLength = ScreenUtils.dip2px(lineThresholdLength);
        txtThreshold = ScreenUtils.dip2px(txtThreshold);
        txtThresholdmargin = ScreenUtils.dip2px(txtThresholdmargin);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        allDp2Px();

        float w = getBaseWidth() - basePaddingLeft - basePaddingRight;
        float h = getBaseHeight() - basePaddingTop - basePaddingBottom;
        centerX = getBaseWidth() / 2.0f;
        centerY = getBaseHeight() / 2.0f;
        if (roundWidth <= 0) {
            roundWidth = Math.min(w, h) / 2.0f;
        }

        drawCircle(canvas);
    }


    private void initPieData() {
        if (StringUtils.isEmptyList(mPieEntryList)) {
            return;
        }
        float sumValue = 0;
        int maxIndex = 0;
        float maxData = -1;
        //数据预处理:对占比比较小的值多分配一点防止太小。
        for (int i = 0; i < mPieEntryList.size(); i++) {
            PieEntrys pieEntry = mPieEntryList.get(i);
            sumValue += pieEntry.value;
            if (pieEntry.value > maxData) {
                maxData = pieEntry.value;
                maxIndex = i;
            }
        }
        mAnimation.setPieChartData(mPieEntryList, sumValue, this);
        /**
         * 边界问题处理：防止item太小,出现文字积压问题。
         */
        PieEntrys maxPieEntry = mPieEntryList.get(maxIndex);
        for (int i = 0; i < mPieEntryList.size(); i++) {
            PieEntrys pieEntry = mPieEntryList.get(i);
            float tempValue = pieEntry.value;
            if (pieEntry.value / sumValue < minPartsThreshold) {
                //该块补全最小值
                pieEntry.value = sumValue * minPartsThreshold;
                float disLen = pieEntry.value - tempValue;
                //最大块减去被减去的
                maxPieEntry.value -= disLen;
            }
        }

        //startAnimation的时候，如果这个View是不可见的，或者是gone的，就会导致传进去的Animation对象不执行
        postDelayed(() -> startAnimation(mAnimation), 300);
    }

    private void drawCircle(Canvas canvas) {
        if (StringUtils.isEmptyList(mPieEntryList)) {
            drawEmptyView(canvas);
            return;
        }

        //正式开始处理
        float valueW = getBaseWidth() - basePaddingLeft - basePaddingRight;
        float valueH = getBaseHeight() - basePaddingTop - basePaddingBottom;
        float radius = Math.min(valueW,
                valueH) / 2.0f - roundWidth / 2.0f;//这个地方比较难理解，定义圆环的外边界。边框其实是在中间。
        float lX = centerX - radius;
        float tY = centerY - radius;
        float rX = centerX + radius;
        float bY = centerY + radius;
        //圆环外边界，但是如果圆环成整个圆之后，就是内边界了。
        RectF rectF = new RectF(lX, tY, rX, bY);
        float beginAngle = mBeginAngle;
        float preYpos = 0;
        for (PieEntrys pieEntry : mPieEntryList) {

            //无动画
            //float sweepAngle = 360 * pieEntry.value / mSumValue;
            //在这里实现动画
            float sweepAngle = pieEntry.mSweepAngle;

            //防止item过小
            if (sweepAngle < 360 * minPartsThreshold) {
                sweepAngle = 360 * minPartsThreshold;
            }
            //防止绘制过度
            if (beginAngle + sweepAngle > 270) {
                sweepAngle = 270 - beginAngle;
            }

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(roundWidth);
            paint.setColor(pieEntry.colorBg);
            canvas.drawArc(rectF, beginAngle, sweepAngle, false, paint);
            //高亮逻辑
            if (pieEntry.highLight) {
                float radius2 = Math.min(
                        valueW,
                        valueH) / 2.0f + highLightPadding + highLightWidth / 2.0f;

                float lX2 = centerX - radius2;
                float tY2 = centerY - radius2;
                float rX2 = centerX + radius2;
                float bY2 = centerY + radius2;

                RectF rectF2 = new RectF(lX2, tY2, rX2, bY2);
                Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint2.setStyle(Paint.Style.STROKE);
                paint2.setStrokeWidth(highLightWidth);
                paint2.setColor(pieEntry.colorBg);
                canvas.drawArc(rectF2, beginAngle, sweepAngle, false, paint2);
            }

            /**
             * 指示文字，重要逻辑
             * 思路：取该模块的度数的中间值向外延伸一定距离。如果该度数在[-90,90]之间，在右边显示；否则在左边显示。
             */
            //真实的角度
            float coreAngle = beginAngle + sweepAngle / 2.0f;//因为从-90开始的
            float r = radius + roundWidth / 2.0f;//圆的半径

            Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            linePaint.setColor(pieEntry.colorBg);
            linePaint.setStrokeWidth(lineWidth);

            Paint ratePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ratePaint.setColor(pieEntry.highLight ? getColor(R.color.color_pie_chart_red) : getColor(R.color.color_pie_chart_gray));
            ratePaint.setTextSize(textSize);

            Paint symbolPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            symbolPaint.setColor(getColor(R.color.color_pie_chart_gray));
            symbolPaint.setTextSize(textSize);


            float yPos;
            //右边
            Log.d(TAG, "drawCircle: " + coreAngle);
            float textPdding = getFontHeight(textSize, ratePaint);
            if (coreAngle >= -90 && coreAngle <= 90) {
                float circleX = centerX + radius + roundWidth / 2.0f + highLightPadding + highLightWidth;
                float xPos = circleX + lineLengthMargin;//x的坐标
                boolean isThreshold = false;
                if (coreAngle <= 0) {
                    yPos = (float) (centerY - Math.cos(getAreaAngle(90 + coreAngle)) * r);//y的左边
                    //在上边界或者下边界x方向延伸长一点
                    if (r - (centerY - yPos) < lineThreshold) {
                        xPos -= lineThresholdLength;
                        isThreshold = true;
                    }
                } else {
                    yPos = (float) (centerY + Math.cos(getAreaAngle(90 - coreAngle)) * r);//y的左边
                    //在上边界或者下边界x方向延伸长一点
                    if (r - (yPos - centerY) < lineThreshold) {
                        xPos -= lineThresholdLength;
                        isThreshold = true;
                    }
                }

                //又一个边界问题
                if (preYpos != 0 && yPos - preYpos < txtThreshold) {
                    yPos += txtThresholdmargin;
                }
                preYpos = yPos;

                Log.d(TAG, "drawCircle: " + xPos + "," + yPos);
                float lineLeft = !isThreshold ? xPos + lineLength : xPos + lineLength + lineThresholdLength;
                float txtLeft = !isThreshold ? xPos : xPos + lineThresholdLength;
                canvas.drawLine(xPos, yPos, lineLeft, yPos, linePaint);
                canvas.drawCircle(xPos, yPos, dotRadius, linePaint);
                float labelLen = lineLength - ratePaint.measureText(pieEntry.label);
                canvas.drawText(pieEntry.label, txtLeft + labelLen, yPos - textPaddingBottom, ratePaint);
                float symbolLen = lineLength - symbolPaint.measureText(pieEntry.symbol);
                canvas.drawText(pieEntry.symbol, txtLeft + symbolLen, yPos + textPaddingBottom + textPdding / 2.0f, symbolPaint);
            } else {
                float circleX = centerX - radius - roundWidth / 2.0f - highLightPadding - highLightWidth;
                float xPos = circleX + lineLengthMargin;//x的坐标
                boolean isThreshold = false;
                //左边
                if (coreAngle <= 180) {
                    yPos = (float) (centerY + Math.cos(getAreaAngle(coreAngle - 90)) * r);//y的左边
                    //在上边界或者下边界x方向延伸长一点
                    if (r - (yPos - centerY) < lineThreshold) {
                        xPos += lineThresholdLength;
                        isThreshold = true;
                    }
                } else {
                    yPos = (float) (centerY - Math.cos(getAreaAngle(270 - coreAngle)) * r);//y的左边
                    if (r - (centerY - yPos) < lineThreshold) {
                        xPos += lineThresholdLength;
                        isThreshold = true;
                    }
                }

                //又一个边界问题
                if (preYpos != 0 && preYpos - yPos < txtThreshold) {
                    yPos -= txtThresholdmargin;
                }
                preYpos = yPos;

                Log.d(TAG, "drawCircle: " + xPos + "," + yPos);
                float lineLeft = !isThreshold ? xPos - lineLength : xPos - lineLength - lineThresholdLength;
                float txtLeft = !isThreshold ? xPos - lineLength : xPos - lineLength - lineThresholdLength;

                canvas.drawLine(xPos, yPos, lineLeft, yPos, linePaint);
                canvas.drawCircle(xPos, yPos, dotRadius, linePaint);
                canvas.drawText(pieEntry.label, txtLeft, yPos - textPaddingBottom, ratePaint);
                canvas.drawText(pieEntry.symbol, txtLeft, yPos + textPaddingBottom + textPdding / 2.0f, symbolPaint);
            }

            beginAngle += sweepAngle;
        }
    }

    private float getAreaAngle(float angle) {
        return (float) (Math.PI * angle / 180);
    }

    private void drawEmptyView(Canvas canvas) {
        String txt = mEmptyTxt;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(ScreenUtils.sp2px(15));
        paint.setColor(getColor(R.color.color_pie_chart_gray));
        canvas.drawText(txt, getBaseWidth() / 2.0f - paint.measureText(txt) / 2.0f, getBaseHeight() / 2.0f, paint);
    }

    public List<PieEntrys> getPieEntryList() {
        return mPieEntryList;
    }

    public PieChartView setPieEntryList(List<PieEntrys> pieEntryList) {
        mPieEntryList = pieEntryList;
        initPieData();
        return this;
    }

    public float getBasePaddingTop() {
        return basePaddingTop;
    }

    public PieChartView setBasePaddingTop(float basePaddingTop) {
        this.basePaddingTop = basePaddingTop;
        return this;
    }

    public float getBasePaddingBottom() {
        return basePaddingBottom;
    }

    public PieChartView setBasePaddingBottom(float basePaddingBottom) {
        this.basePaddingBottom = basePaddingBottom;
        return this;
    }

    public float getBasePaddingLeft() {
        return basePaddingLeft;
    }

    public PieChartView setBasePaddingLeft(float basePaddingLeft) {
        this.basePaddingLeft = basePaddingLeft;
        return this;
    }

    public float getBasePaddingRight() {
        return basePaddingRight;
    }

    public PieChartView setBasePaddingRight(float basePaddingRight) {
        this.basePaddingRight = basePaddingRight;
        return this;
    }

    public float getRoundWidth() {
        return roundWidth;
    }

    public PieChartView setRoundWidth(float roundWidth) {
        this.roundWidth = roundWidth;
        return this;
    }

    public float getHighLightWidth() {
        return highLightWidth;
    }

    public PieChartView setHighLightWidth(float highLightWidth) {
        this.highLightWidth = highLightWidth;
        return this;
    }

    public float getHighLightPadding() {
        return highLightPadding;
    }

    public PieChartView setHighLightPadding(float highLightPadding) {
        this.highLightPadding = highLightPadding;
        return this;
    }

    public float getLineLengthMargin() {
        return lineLengthMargin;
    }

    public PieChartView setLineLengthMargin(float lineLengthMargin) {
        this.lineLengthMargin = lineLengthMargin;
        return this;
    }

    public float getLineLength() {
        return lineLength;
    }

    public PieChartView setLineLength(float lineLength) {
        this.lineLength = lineLength;
        return this;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public PieChartView setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
        return this;
    }

    public float getLineThreshold() {
        return lineThreshold;
    }

    public PieChartView setLineThreshold(float lineThreshold) {
        this.lineThreshold = lineThreshold;
        return this;
    }

    public float getLineThresholdLength() {
        return lineThresholdLength;
    }

    public PieChartView setLineThresholdLength(float lineThresholdLength) {
        this.lineThresholdLength = lineThresholdLength;
        return this;
    }

    public float getTextPaddingBottom() {
        return textPaddingBottom;
    }

    public PieChartView setTextPaddingBottom(float textPaddingBottom) {
        this.textPaddingBottom = textPaddingBottom;
        return this;
    }

    public float getDotRadius() {
        return dotRadius;
    }

    public PieChartView setDotRadius(float dotRadius) {
        this.dotRadius = dotRadius;
        return this;
    }

    public float getTxtThresholdmargin() {
        return txtThresholdmargin;
    }

    public PieChartView setTxtThresholdmargin(float txtThresholdmargin) {
        this.txtThresholdmargin = txtThresholdmargin;
        return this;
    }

    public float getTxtThreshold() {
        return txtThreshold;
    }

    public PieChartView setTxtThreshold(float txtThreshold) {
        this.txtThreshold = txtThreshold;
        return this;
    }

    public float getTextSize() {
        return textSize;
    }

    public PieChartView setTextSize(float textSize) {
        this.textSize = textSize;
        return this;
    }

    public float getMinPartsThreshold() {
        return minPartsThreshold;
    }

    public PieChartView setMinPartsThreshold(float minPartsThreshold) {
        this.minPartsThreshold = minPartsThreshold;
        return this;
    }

    public String getEmptyTxt() {
        return mEmptyTxt;
    }

    public PieChartView setEmptyTxt(String emptyTxt) {
        mEmptyTxt = emptyTxt;
        return this;
    }
}
