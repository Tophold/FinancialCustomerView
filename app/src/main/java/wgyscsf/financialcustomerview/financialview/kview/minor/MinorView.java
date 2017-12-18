package wgyscsf.financialcustomerview.financialview.kview.minor;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import wgyscsf.financialcustomerview.R;
import wgyscsf.financialcustomerview.financialview.FinancialAlgorithm;
import wgyscsf.financialcustomerview.financialview.kview.KView;
import wgyscsf.financialcustomerview.financialview.kview.Quotes;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/12/14 17:56
 * 描 述 ：
 * ============================================================
 **/
public class MinorView extends KView {

    /**
     * 常量
     */
    public final static int DEF_K_PERIOD=9;
    public final static int DEF_D_PERIOD=3;
    public final static int DEF_J_PERIOD=3;

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
    //rsi
    int mRsi16Color;
    int mRsi12Color;
    int mRsi24Color;
    //kdj
    int mKColor;
    int mDColor;
    int mJColor;

    //显示的副图类型
    MinorType mMinorType = MinorType.MACD;

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
        drawInnerXy(canvas);
    }

    private void initAttrs() {
        initColorRes();

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
    protected void drawInnerXy(Canvas canvas) {
        //x轴的虚线不再绘制
        //先绘制x轴
        //计算每一段x的高度
//        double perhight = (mHeight - mPaddingTop - mPaddingBottom) / 4;
//        for (int i = 1; i <= 3; i++) {
//            canvas.drawLine(mPaddingLeft, (float) (mPaddingTop + perhight * i),
//                    mWidth - mPaddingRight, (float) (mPaddingTop + perhight * i),
//                    mInnerXyPaint);
//        }

        //绘制y轴
        double perWidth = (mWidth - mPaddingLeft - mPaddingRight) / 4;
        for (int i = 1; i <= 3; i++) {
            canvas.drawLine((float) (mPaddingLeft + perWidth * i), mPaddingTop,
                    (float) (mPaddingLeft + perWidth * i), mHeight - mPaddingBottom,
                    mInnerXyPaint);
        }
    }

    //重写获取数据的方法，计算kjd


    @Override
    public void setTimeSharingData(List<Quotes> quotesList) {
        super.setTimeSharingData(quotesList);
        if (quotesList == null || quotesList.isEmpty()) {
            Toast.makeText(mContext, "数据异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "setTimeSharingData: 数据异常");
            return;
        }

        //try {
            FinancialAlgorithm.calculateKDJ(quotesList,DEF_K_PERIOD,DEF_D_PERIOD,DEF_J_PERIOD);
//        } catch (Exception e) {
//            e.printStackTrace();
//            //这里处理计算出错
//        }
    }

    //副图正在展示的类型
    enum MinorType {
        MACD,
        RSI,
        KDJ
    }

}
