package wgyscsf.financialcustomerview.financialview;

import android.util.Log;

import java.util.List;

import wgyscsf.financialcustomerview.financialview.kview.Quotes;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/12/18 11:24
 * 描 述 ：所有金融相关算法全部在这里
 * ============================================================
 **/
public class FinancialAlgorithm {
    final static String TAG="FinancialAlgorithm";
    /**
     * 计算数据集合的kdj。由以下计算过程可以看出只有kPeriod有用，dPeriod、jPeriod暂时无用。
     * @param quotesList 对应的数据集合
     * @param kPeriod    k所对应的周期，可以是：分钟、小时、天等
     * @param dPeriod    d所对应的周期，可以是：分钟、小时、天等
     * @param jPeriod    j所对应的周期，可以是：分钟、小时、天等
     */
    // FIXME: 2017/12/18
    public static void calculateKDJ(List<Quotes> quotesList, int kPeriod, int dPeriod, int jPeriod) {
        //容错
        if(quotesList==null||quotesList.isEmpty())return;
        if(kPeriod<=0)kPeriod=9;
        if(dPeriod<=0)dPeriod=3;
        if(jPeriod<=0)jPeriod=3;

        //转换程序员的序号
        kPeriod--;
        dPeriod--;
        jPeriod--;
        double kRsv=0;
        //double dRsv=0;
        double k=0;
        double d=0;
        double j=0;
        for (int i = 0; i < quotesList.size(); i++) {
            Quotes quotes = quotesList.get(i);

            //计算k
            if (i < kPeriod) {
                k = 50;
            } else {
                double c=0;
                double l=0;
                double h=0;
                double tempL = Double.MAX_VALUE;
                double tempH = Double.MIN_VALUE;
                for (int i1 = i - kPeriod; i1 <= i; i1++) {
                    Quotes kQuotes = quotesList.get(i1);
                    if (kQuotes.c < tempL) {
                        l = kQuotes.c;
                    }
                    if (kQuotes.c > tempH) {
                        h = kQuotes.c;
                    }
                    if (i1 == i) {
                        c = kQuotes.c;
                    }
                }
                kRsv = (c - l) / (h - l) * 100;
                k=2/3.0*k+1/3.0*kRsv;
            }
            quotes.k=k;

            //计算d
            if (i < dPeriod) {
                d = 50;
            } else {
                //以下注释掉的是因为d的计算用不上
//                double c=0;
//                double l=0;
//                double h=0;
//                double tempL = Double.MAX_VALUE;
//                double tempH = Double.MIN_VALUE;
//                for (int i1 = i - dPeriod; i1 <= i; i1++) {
//                    Quotes kQuotes = quotesList.get(i1);
//                    if (kQuotes.c < tempL) {
//                        l = kQuotes.c;
//                    }
//                    if (kQuotes.c > tempH) {
//                        h = kQuotes.c;
//                    }
//                    if (i1 == i) {
//                        c = kQuotes.c;
//                    }
//                }
//                dRsv = (c - l) / (h - l) * 100;
                d=2/3.0*d+1/3.0*quotes.k;
            }
            quotes.d=d;

            //计算j
            j=3*quotes.k-2*quotes.d;
            quotes.j=j;

            //计算结束
            //打印测试
            Log.e(TAG, "calculateKDJ: k:"+quotes.k+",d:"+quotes.d+",j:"+quotes.j );
        }
    }

}
