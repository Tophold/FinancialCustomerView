package wgyscsf.financialcustomerview.financialview;

import android.util.Log;

import java.util.List;

import wgyscsf.financialcustomerview.financialview.kview.Quotes;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/12/18 11:24
 * 描 述 ：所有金融相关算法全部在这里。算法参考【资料】包中的：常用股票指标计算公式及简单应用.pdf
 * ============================================================
 **/
// TODO: 2017/12/18 这里的算法需要核实！包括异常情况的处理和边界的处理是否合适。
public class FinancialAlgorithm {
    final static String TAG = "FinancialAlgorithm";

    public static void calculateKDJ(List<Quotes> quotesList) {
        calculateKDJ(quotesList, 9, 3, 3);
    }

    /**
     * 计算数据集合的kdj。由以下计算过程可以看出只有kPeriod有用，dPeriod、jPeriod暂时无用。
     *
     * @param quotesList 对应的数据集合
     * @param kPeriod    k所对应的周期，可以是：分钟、小时、天等
     * @param dPeriod    d所对应的周期，可以是：分钟、小时、天等
     * @param jPeriod    j所对应的周期，可以是：分钟、小时、天等
     */
    public static void calculateKDJ(List<Quotes> quotesList, int kPeriod, int dPeriod, int jPeriod) {
        //容错
        if (quotesList == null || quotesList.isEmpty()) return;
        if (kPeriod <= 0) kPeriod = 9;
        if (dPeriod <= 0) dPeriod = 3;
        if (jPeriod <= 0) jPeriod = 3;

        //转换程序员的序号
        kPeriod--;
        dPeriod--;
        jPeriod--;
        double kRsv = 0;
        //double dRsv=0;
        double k = 0;
        double d = 0;
        double j = 0;
        for (int i = 0; i < quotesList.size(); i++) {
            Quotes quotes = quotesList.get(i);

            //计算k
            if (i < kPeriod) {
                k = 50;
            } else {
                double c = 0;
                double l = 0;
                double h = 0;
                double tempL = Integer.MAX_VALUE;
                double tempH = Integer.MIN_VALUE;
                for (int i1 = i - kPeriod; i1 <= i; i1++) {
                    Quotes kQuotes = quotesList.get(i1);
                    if (kQuotes.c < tempL) {
                        tempL = kQuotes.c;
                        l = tempL;
                    }
                    if (kQuotes.c > tempH) {
                        tempH = kQuotes.c;
                        h = tempH;
                    }
                    if (i1 == i) {
                        c = kQuotes.c;
                    }
                }
                kRsv = (c - l) / (h - l) * 100;
                k = 2 / 3.0 * k + 1 / 3.0 * kRsv;
            }
            quotes.k = k;

            //计算d
            if (i < dPeriod) {
                d = 50;
            } else {
                d = 2 / 3.0 * d + 1 / 3.0 * quotes.k;
            }
            quotes.d = d;

            //计算j
            j = 3 * quotes.k - 2 * quotes.d;
            quotes.j = j;

            //计算结束
            //打印测试
            //Log.e(TAG, "calculateKDJ: k:" + quotes.k + ",d:" + quotes.d + ",j:" + quotes.j);
        }
    }


    /**
     * 计算数据集合的DIF、DEA、MACD。
     * 新股上市首日，其首日DEA为0。从次日开始，由于首日DEA为0，因此次日的DEA=0.2* DIF+0=0.2*DIF。后续日子的DEA可以套用0.2*DIF + 0.8* DEA’计算。
     *
     * @param quotesList 对应的数据集合
     */
    public static void calculateMACD(List<Quotes> quotesList) {
        //容错
        if (quotesList == null || quotesList.isEmpty()) return;

        double ema12 = 0;
        double ema26 = 0;
        double dif = 0;
        double dea = 0;
        double macd = 0;

        for (int i = 0; i < quotesList.size(); i++) {
            Quotes quotes = quotesList.get(i);
            if (i == 0) {
                ema12 = quotes.c;
                ema26 = quotes.c;
            } else {
                ema12 = ema12 * 11 / 13.0 + quotes.c * 2 / 13.0;
                ema26 = ema12 * 25 / 27.0 + quotes.c * 2 / 27.0;
            }
            //计算dif
            dif = ema12 - ema26;

            //计算dea
            if (i == 0) {
                dea = 0;
            } else {
                dea = dea * 8 / 10.0 + dif * 2 / 10.0;
            }

            //计算macd
            macd = 2 * (dif - dea);

            quotes.dif = dif;
            quotes.dea = dea;
            quotes.macd = macd;

            //计算结束
            //打印日志
            //Log.e(TAG, "calculateMACD: dif:" + quotes.dif + ",dea:" + quotes.dea + ",macd:" + macd);
        }

    }

    public static void calculateRSI(List<Quotes> quotesList) {
        calculateRSI(quotesList, 6, 12, 24);
    }

    /**
     * 计算数据集合的RSI。对于异常的数据，比如：分子为0的，返回值全部用0表示。
     *
     * @param quotesList 对应的数据集合
     * @param xPeriod    周期
     * @param yPeriod
     * @param zPeriod
     */
    public static void calculateRSI(List<Quotes> quotesList, int xPeriod, int yPeriod, int zPeriod) {
        //容错
        if (quotesList == null || quotesList.isEmpty()) return;
        if (xPeriod <= 0) xPeriod = 6;
        if (yPeriod <= 0) yPeriod = 12;
        if (zPeriod <= 0) zPeriod = 24;
        //转化为熟悉的索引
        xPeriod--;
        yPeriod--;
        zPeriod--;

        double rs6 = 0;
        double rsi6 = 0;
        double rs12 = 0;
        double rsi12 = 0;
        double rs24 = 0;
        double rsi24 = 0;

        for (int i = 0; i < quotesList.size(); i++) {
            Quotes quotes = quotesList.get(i);
            double downSum = 0;
            double upSum = 0;
            if (i < xPeriod) {
                for (int i1 = 0; i1 <= i; i1++) {
                    if (i1 == 0) continue;
                    Quotes preQuotes = quotesList.get(i1 - 1);
                    double dis = quotes.c - preQuotes.c;
                    if (dis < 0) {
                        downSum -= dis;
                    } else {
                        upSum += dis;
                    }
                }
                //异常情况
                if (i == 0 || downSum / i == 0) {
                    rs6 = 0;
                } else {
                    rs6 = (upSum / i) / (downSum / i);
                }
            } else {
                for (int i1 = i - xPeriod; i1 <= i; i1++) {
                    if (i1 == 0) continue;
                    Quotes preQuotes = quotesList.get(i1 - 1);
                    double dis = quotes.c - preQuotes.c;
                    if (dis < 0) {
                        downSum -= dis;
                    } else {
                        upSum += dis;
                    }
                }
                //异常情况
                if (downSum == 0 || upSum == 0) {
                    rs6 = 0;
                } else {
                    rs6 = (upSum / xPeriod) / (downSum / xPeriod);
                }
            }
            //计算
            rsi6 = 100 * rs6 / (1 + rs6);


            if (i < yPeriod) {
                for (int i1 = 0; i1 <= i; i1++) {
                    if (i1 == 0) continue;
                    Quotes preQuotes = quotesList.get(i1 - 1);
                    double dis = quotes.c - preQuotes.c;
                    if (dis < 0) {
                        downSum -= dis;
                    } else {
                        upSum += dis;
                    }
                }
                //异常情况
                if (i == 0 || downSum / i == 0) {
                    rs12 = 0;
                } else {
                    rs12 = (upSum / i) / (downSum / i);
                }
            } else {
                for (int i1 = i - yPeriod; i1 <= i; i1++) {
                    if (i1 == 0) continue;
                    Quotes preQuotes = quotesList.get(i1 - 1);
                    double dis = quotes.c - preQuotes.c;
                    if (dis < 0) {
                        downSum -= dis;
                    } else {
                        upSum += dis;
                    }
                }
                //异常情况
                if (downSum == 0 || upSum == 0) {
                    rs12 = 0;
                } else {
                    rs12 = (upSum / yPeriod) / (downSum / yPeriod);
                }
            }
            //计算
            rsi12 = 100 * rs12 / (1 + rs12);


            if (i < zPeriod) {
                for (int i1 = 0; i1 <= i; i1++) {
                    if (i1 == 0) continue;
                    Quotes preQuotes = quotesList.get(i1 - 1);
                    double dis = quotes.c - preQuotes.c;
                    if (dis < 0) {
                        downSum -= dis;
                    } else {
                        upSum += dis;
                    }
                }
                //异常情况
                if (i == 0 || downSum / i == 0) {
                    rs24 = 0;
                } else {
                    rs24 = (upSum / i) / (downSum / i);
                }
            } else {
                for (int i1 = i - zPeriod; i1 <= i; i1++) {
                    if (i1 == 0) continue;
                    Quotes preQuotes = quotesList.get(i1 - 1);
                    double dis = quotes.c - preQuotes.c;
                    if (dis < 0) {
                        downSum -= dis;
                    } else {
                        upSum += dis;
                    }
                }

                //异常情况
                if (downSum == 0 || upSum == 0) {
                    rs24 = 0;
                } else {
                    rs24 = (upSum / zPeriod) / (downSum / zPeriod);
                }
            }
            //计算
            rsi24 = 100 * rs24 / (1 + rs24);

            //设置
            quotes.rsi6 = rsi6;
            quotes.rsi12 = rsi12;
            quotes.rsi24 = rsi24;

            //打印日志
            //            Log.e(TAG, "calculateRSI:rsi6 " + quotes.rsi6
            //                    + ",rsi12:" + quotes.rsi12 + ",rsi24:" + quotes.rsi24);
        }

    }

    //MA算法

    /**
     * MA算法，period（周期）的MA的计算：带上今天，向前取period的收盘价之和除以period,即是今日的MA(period)。
     * 算法很简洁，但是是对的。
     *
     * @param period 周期，一般是：5、10、20、30、60
     */
    public static void calculateMA(List<Quotes> quotesList, int period) {
        if (quotesList == null || quotesList.isEmpty()) return;
        if (period < 0 || period > quotesList.size() - 1) return;

        double sum1 = 0;
        for (int i = 0; i < quotesList.size(); i++) {
            Quotes quotes = quotesList.get(i);
            sum1 += quotes.c;
            //边界
            if (i < period - 1) {
                continue;
            }

            if (i > period - 1) {
                sum1 -= quotesList.get(i - period).c;
            }

            if (period == 5) {
                quotes.ma5 = sum1 / period;
            } else if (period == 10) {
                quotes.ma10 = sum1 / period;
            } else if (period == 20) {
                quotes.ma20 = sum1 / period;
            } else {
                Log.e(TAG, "calculateMA: TODO:完善算法");
                return;
            }

        }
    }

    /**
     * 计算boll，当List最前面的值（index<period）怎么处理？？？现在的处理，不处理，显示：0
     *
     * @param quotesList
     * @param period     周期，一般取26
     * @param k          K为参数，可根据股票的特性来做相应的调整，一般默认为2
     *                   <p>
     *                   boll线
     *                   日BOLL指标的计算过程
     *                   （1）计算MA
     *                   MA=N周期之和÷N
     *                   （2）计算标准差MD
     *                   MD=平方根N日的（C－MA）的两次方之和除以N
     *                   （3）计算MB、UP、DN线
     *                   MB=（N－1）日的MA
     *                   UP=MB＋2×MD
     *                   DN=MB－2×MD
     */
    public static void calculateBOLL(List<Quotes> quotesList, int period, int k) {
        if (quotesList == null || quotesList.isEmpty()) return;
        if (period < 0 || period > quotesList.size() - 1) return;

        double mb;//上轨线
        double up;//中轨线
        double dn;//下轨线

        double ma = 0;//N-1日
        double md = 0;
        double sum = 0;

        for (int i = 0; i < quotesList.size(); i++) {
            Quotes quotes = quotesList.get(i);
            sum += quotes.c;
            //这个范围不计算，在View上的反应就是不显示这个范围的boll线
            if (i < period - 1)
                continue;

            md = 0;
            if (i > period - 1)
                sum -= quotesList.get(i - period).c;//特别特别注意，这个算的是（N-1）日的，因为把今天的减去了。
            ma = sum / period;
            for (int j = i + 1 - period; j <= i; j++) {
                md += Math.pow(quotesList.get(j).c - ma, 2);
            }
            md = Math.sqrt(md / period);

            mb = ma;
            up = mb + 2 * md;
            dn = mb - 2 * md;

            quotes.mb = mb;
            quotes.up = up;
            quotes.dn = dn;
        }
    }

    public static void calculateBOLL(List<Quotes> quotesList) {
        calculateBOLL(quotesList, 26, 2);
    }
}
