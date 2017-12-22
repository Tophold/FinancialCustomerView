package wgyscsf.financialcustomerview.financialview.kview.minor;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import wgyscsf.financialcustomerview.R;
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

    //MinorModel聚合的数据`
    MinorModel mMinorModel;

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
        if (mMinorModel.getMinorType() == MinorModel.MinorType.MACD) {
            drawMACD(canvas);
        } else if (mMinorModel.getMinorType() == MinorModel.MinorType.RSI) {
            drawRSI(canvas);
        } else if (mMinorModel.getMinorType() == MinorModel.MinorType.KDJ) {
            drawKDJ(canvas);
        }
    }

    private void drawMACD(Canvas canvas) {

    }

    private void drawRSI(Canvas canvas) {

    }

    private void drawKDJ(Canvas canvas) {

    }

    private void initAttrs() {
        initDefAttrs();
        initColorRes();

    }

    private void initDefAttrs() {
        mMinorModel = new MinorModel();
        mMinorModel.setMinorType(MinorModel.MinorType.MACD);


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
    public void setTimeSharingData(List<Quotes> quotesList) {
        super.setTimeSharingData(quotesList);
        if (quotesList == null || quotesList.isEmpty()) {
            Toast.makeText(mContext, "数据异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "setTimeSharingData: 数据异常");
            return;
        }
        //设置数据
        mMinorModel.setQuotesList(quotesList);

        //执行寻找最大最小值
        proformMinMaxData();
    }

    /**
     * 寻找指定指标类型的最大最小值
     */
    private void proformMinMaxData() {
        if (mMinorModel.getMinorType() == MinorModel.MinorType.MACD) {
            MacdModel macdModel = mMinorModel.getMacdModel();
            //设置并开始寻找最小最大值
            macdModel.setOriginList(mMinorModel.getQuotesList());

            //以下开始寻找macd的单元宽度与间隔宽度。


        } else if (mMinorModel.getMinorType() == MinorModel.MinorType.RSI) {
            RsiModel rsiModel = mMinorModel.getRsiModel();
        } else if (mMinorModel.getMinorType() == MinorModel.MinorType.KDJ) {
            KdjModel kdjModel = mMinorModel.getKdjModel();
        }
    }
}
