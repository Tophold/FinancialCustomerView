package wgyscsf.financialcustomerview.financialview.kview.minor;

import android.util.Log;

import java.util.List;

import wgyscsf.financialcustomerview.financialview.kview.Quotes;
import wgyscsf.financialcustomerview.utils.StringUtils;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/12/22 16:42
 * 描 述 ：
 * ============================================================
 **/
public class MacdModel {
    private static final String TAG = "MacdModel";
    /**
     * macd相关属性
     */
    private Quotes mMinMacdQuotes;
    private Quotes mMaxMacdQuotes;
    private Quotes mMinDifQuotes;
    private Quotes mMaxDifQuotes;
    private Quotes mMinDeaQuotes;
    private Quotes mMaxDeaQuotes;
    //数据源
    private List<Quotes> mOriginList;

    public Quotes getMinMacdQuotes() {
        return mMinMacdQuotes;
    }

    public void setMinMacdQuotes(Quotes minMacdQuotes) {
        mMinMacdQuotes = minMacdQuotes;
    }

    public Quotes getMaxMacdQuotes() {
        return mMaxMacdQuotes;
    }

    public void setMaxMacdQuotes(Quotes maxMacdQuotes) {
        mMaxMacdQuotes = maxMacdQuotes;
    }

    public Quotes getMinDifQuotes() {
        return mMinDifQuotes;
    }

    public void setMinDifQuotes(Quotes minDifQuotes) {
        mMinDifQuotes = minDifQuotes;
    }

    public Quotes getMaxDifQuotes() {
        return mMaxDifQuotes;
    }

    public void setMaxDifQuotes(Quotes maxDifQuotes) {
        mMaxDifQuotes = maxDifQuotes;
    }

    public Quotes getMinDeaQuotes() {
        return mMinDeaQuotes;
    }

    public void setMinDeaQuotes(Quotes minDeaQuotes) {
        mMinDeaQuotes = minDeaQuotes;
    }

    public Quotes getMaxDeaQuotes() {
        return mMaxDeaQuotes;
    }

    public void setMaxDeaQuotes(Quotes maxDeaQuotes) {
        mMaxDeaQuotes = maxDeaQuotes;
    }

    /**
     * 设置数据源，然后就计算边界
     *
     * @param originList
     */
    public void setOriginList(List<Quotes> originList) {
        mOriginList = originList;
        if (originList == null || originList.isEmpty()) {
            Log.d(TAG, "setOriginList: 数据源为空");
            return;
        }
        processMinMax(originList);
    }

    private void processMinMax(List<Quotes> originList) {
        double minMacd = Integer.MAX_VALUE;
        double maxMacd = Integer.MIN_VALUE;

        double minDif = Integer.MAX_VALUE;
        double maxDif = Integer.MIN_VALUE;

        double minDea = Integer.MAX_VALUE;
        double maxDea = Integer.MIN_VALUE;
        for (Quotes quotes : originList) {
            if (quotes.macd < minMacd) {
                minMacd = quotes.macd;
                mMinMacdQuotes = quotes;
            } else if (quotes.macd > maxMacd) {
                maxMacd = quotes.macd;
                mMaxMacdQuotes = quotes;
            }

            if (quotes.dif < minDif) {
                minDif = quotes.dif;
                mMinDifQuotes = quotes;
            } else if (quotes.dif > maxDif) {
                maxDif = quotes.dif;
                mMaxDifQuotes = quotes;
            }

            if (quotes.dea < minDea) {
                minDea = quotes.dea;
                mMinDeaQuotes = quotes;
            } else if (quotes.dea > maxDea) {
                maxDea = quotes.dea;
                mMaxDeaQuotes = quotes;
            }
        }
    }
}
