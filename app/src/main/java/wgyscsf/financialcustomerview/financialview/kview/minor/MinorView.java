package wgyscsf.financialcustomerview.financialview.kview.minor;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import wgyscsf.financialcustomerview.R;
import wgyscsf.financialcustomerview.financialview.kview.KView;

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

    //副图正在展示的类型
    enum MinorType {
        MACD,
        RSI,
        KDJ
    }

}
