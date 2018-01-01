package wgyscsf.financialcustomerview.financialview.kview.minor;

import java.util.List;

import wgyscsf.financialcustomerview.financialview.kview.Quotes;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/12/22 16:30
 * 描 述 ：副图的属性在这里包装成对象。改为组合的方式。
 * ============================================================
 **/
public class MinorModel {

    private MinorType mMinorType;

    private MacdModel mMacdModel;
    private RsiModel mRsiModel;
    private KdjModel mKdjModel;

    //目标数据源
    private List<Quotes> mQuotesList;

    public List<Quotes> getQuotesList() {
        return mQuotesList;
    }

    public void setQuotesList(List<Quotes> quotesList) {
        mQuotesList = quotesList;
    }

    public MinorType getMinorType() {
        return mMinorType;
    }

    public void setMinorType(MinorType minorType) {
        mMinorType = minorType;
    }

    public MacdModel getMacdModel() {
        if (mMacdModel == null)
            mMacdModel = new MacdModel();
        return mMacdModel;
    }

    public void setMacdModel(MacdModel macdModel) {
        mMacdModel = macdModel;
    }

    public RsiModel getRsiModel() {
        return mRsiModel;
    }

    public void setRsiModel(RsiModel rsiModel) {
        mRsiModel = rsiModel;
    }

    public KdjModel getKdjModel() {
        return mKdjModel;
    }

    public void setKdjModel(KdjModel kdjModel) {
        mKdjModel = kdjModel;
    }

    //副图正在展示的类型
    public enum MinorType {
        MACD,
        RSI,
        KDJ
    }
}
