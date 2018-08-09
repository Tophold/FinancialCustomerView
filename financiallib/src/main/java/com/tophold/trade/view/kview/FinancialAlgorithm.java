package com.tophold.trade.view.kview;

import android.util.Log;

import java.util.List;


/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/12/18 11:24
 * 描 述 ：所有金融相关算法全部在这里。算法参考【资料】包中的：常用股票指标计算公式及简单应用.pdf
 * ============================================================
 **/
// TODO: 2017/12/18 这里的算法需要核实！包括异常情况的处理和边界的处理是否合适。
public class FinancialAlgorithm {
    final static String TAG = FinancialAlgorithm.class.getSimpleName();

    public static void calculateKDJ(List<Quotes> quotesList) {
        calculateKDJ(quotesList, 9, 3, 3);
    }

    /**
     * 【该算法核对过，但是和线上的APP有细微的差距，不知道是否有错！】计算数据集合的kdj。
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

    public static void calculateMACD(List<Quotes> quotesList) {
        calculateMACD(quotesList, 12, 26, 9);
    }

    /**
     * MACD(x,y,z)，一般取MACD(12,26,9)。
     * MACD(x,y,z)，x、y为平滑指数。z暂时不知道用处（不影响算法）。
     * `EMAx=((x-1)/(x+1.0)*前一日EMA)+2.0/(x+1)*今日收盘价`;其中第一日的EMA是当日的收盘价。
     * `EMA12=(11/13.0)*[前一日EMA12]+2.0/13*[今日quotes.c]`
     * `EMA26=(25/27.0)*[前一日EMA26]+2.0/27*[今日quotes.c]`
     * DIF:`DIF=EMA12-EMA26`
     * DEA:`DEA=8/10.0*(前一日的DEA)+2/10.0*今日DIF`
     * MACD:`2*(DIF-DEA)`
     *
     * @param quotesList 数据集合
     * @param d1         平滑指数，一般为12
     * @param d2         平滑指数，一般为26
     * @param z          暂时未知
     */
    public static void calculateMACD(List<Quotes> quotesList, int d1, int d2, int z) {
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
                ema12 = (d1 - 1) / (d1 + 1.0) * ema12 + 2.0 / (d1 + 1) * quotes.c;
                ema26 = (d2 - 1) / (d2 + 1.0) * ema26 + 2.0 / (d2 + 1) * quotes.c;
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
        calculateRSI(quotesList, 6);
        calculateRSI(quotesList, 12);
        calculateRSI(quotesList, 24);
    }

    /**
     * 【该算法已核实】计算RSI。RSI(x,y,z)，一般取RSI(6,12,24)。
     * RSI(x,y,z)，x、y、z均为周期单位，计算算法一致，只是周期不同。
     * RSIx,在周期x内，upSum="在周期x内的上涨总点数"，downSum="在周期x内的下跌总点数"；`RSIx=upSum/(upSum+downSum)*100`;
     * 注意：RSIx对于最开始的x+1周期内，不存在对应RSI,在图像上表示就是不显示对应RSIx即可。
     *
     * @param quotes 对应的数据集合
     * @param period 周期
     */
    public static void calculateRSI(List<Quotes> quotes, int period) {
        //容错
        if (quotes == null || quotes.isEmpty()) return;
        if (period <= 0) period = 6;
        //period单位的上涨点数
        double upSum = 0f;
        //period单位的下跌点数
        double downSum = 0f;
        //差值
        double dis;
        //最后计算的值
        double rsi;
        for (int i = 0; i < quotes.size(); i++) {
            Quotes q = quotes.get(i);
            if (i > 0) {
                dis = q.c - quotes.get(i - 1).c;
                if (dis >= 0) {
                    upSum += dis;
                } else {
                    downSum -= dis;
                }

                //上面加，这里减。要保证累计的和周期为：period
                if (i + 1 > period) {
                    dis = quotes.get(i - period + 1).c - quotes.get(i - period).c;
                    if (dis >= 0) {
                        upSum -= dis;
                    } else {
                        downSum += dis;
                    }
                    rsi = upSum / (upSum + downSum) * 100;
                    if (period == 6) q.rsi6 = rsi;
                    else if (period == 12) q.rsi12 = rsi;
                    else if (period == 24) q.rsi24 = rsi;
                    else Log.e(TAG, "calculateRSI: 不存在该周期：" + period);
                }
            }
        }

    }


    /**
     * 【该算法已核实】计算公式：MA =(C1+C2+C3+C4+C5+...+Cn)/n,其中C为收盘价n为移动平均周期数。
     * 例如现货黄金的5日移动平均价格计算方法为：MA5=(前四天收盘价+前三天收盘价+前天收盘价+昨天收盘价+今天收盘价)/5。
     * 特殊的，假如数据集合中最开始的n个数据，是没法计算MAn的。这里的处理方式是不计算，绘制时直接不绘制对应MA即可。
     *
     * @param quotesList 数据集合
     * @param period     MAn中的n,周期，一般是：5、10、20、30、60。
     */
    public static void calculateMA(List<Quotes> quotesList, int period, KViewType.MaType maType) {
        boolean isMaster = true;
        if (maType == KViewType.MaType.volMa5 || maType == KViewType.MaType.volMa10)
            isMaster = false;

        if (quotesList == null || quotesList.isEmpty()) return;

        if (!isMaster && quotesList.get(0).vol <= 0)
            throw new IllegalArgumentException("请确保设置vol参数");

        if (period < 0 || period > quotesList.size() - 1) return;

        //包含今日的n日和
        double sum = 0;
        for (int i = 0; i < quotesList.size(); i++) {
            //计算和
            Quotes quotes = quotesList.get(i);
            sum += isMaster ? quotes.c : quotes.vol;
            if (i > period - 1) {
                Quotes q = quotesList.get(i - period);
                sum -= isMaster ? q.c : q.vol;
            }
            //边界
            if (i < period - 1) {
                continue;
            }
            double result = sum / period;
            if (period == 5) {
                if (isMaster) quotes.ma5 = result;
                else quotes.volMa5 = result;
            } else if (period == 10) {
                if (isMaster) quotes.ma10 = result;
                else quotes.volMa10 = result;
            } else if (period == 20) {
                if (isMaster) quotes.ma20 = result;
                else {
                    Log.e(TAG, "calculateMA: 没有该种period：" + period + "," + maType);
                }
            } else {
                Log.e(TAG, "calculateMA: 没有该种period：" + period + "," + maType);
                return;
            }

        }
    }


    /**
     * 【该算法已核实】BOLL(n)计算公式：
     * MA=n日内的收盘价之和÷n。
     * MD=n日的平方根（C－MA）的两次方之和除以n
     * MB=（n－1）日的MA
     * UP=MB+k×MD
     * DN=MB－k×MD
     * K为参数，可根据股票的特性来做相应的调整，一般默认为2
     *
     * @param quotesList 数据集合
     * @param period     周期，一般为26
     * @param k          参数，可根据股票的特性来做相应的调整，一般默认为2
     */
    public static void calculateBOLL(List<Quotes> quotesList, int period, int k) {
        if (quotesList == null || quotesList.isEmpty()) return;
        if (period < 0 || period > quotesList.size() - 1) return;

        double up;//上轨线
        double mb;//中轨线
        double dn;//下轨线

        //n日
        double sum = 0;
        //n-1日
        double sum2 = 0;
        for (int i = 0; i < quotesList.size(); i++) {
            Quotes quotes = quotesList.get(i);
            sum += quotes.c;
            sum2 += quotes.c;
            if (i > period - 1)
                sum -= quotesList.get(i - period).c;
            if (i > period - 2)
                sum2 -= quotesList.get(i - period + 1).c;

            //这个范围不计算，在View上的反应就是不显示这个范围的boll线
            if (i < period - 1)
                continue;

            //n日MA
            double ma = sum / period;
            //n-1日MA
            double ma2 = sum2 / (period - 1);
            double md = 0;
            for (int j = i + 1 - period; j <= i; j++) {
                //n日
                md += Math.pow(quotesList.get(j).c - ma, 2);
            }
            md = Math.sqrt(md / period);
            //(n－1）日的MA
            mb = ma2;
            up = mb + k * md;
            dn = mb - k * md;

            quotes.up = up;
            quotes.mb = mb;
            quotes.dn = dn;
        }
    }

    public static void calculateBOLL(List<Quotes> quotesList) {
        calculateBOLL(quotesList, 26, 2);
    }

    /**
     * 找到单个报价中的最小值
     *
     * @param quotes
     * @param masterType
     * @return
     */
    public static double getMasterMinY(Quotes quotes, KViewType.MasterIndicatrixType masterType) {
        double min = Integer.MAX_VALUE;
        //ma
        if (masterType == KViewType.MasterIndicatrixType.MA || masterType == KViewType.MasterIndicatrixType.MA_BOLL) {
            if (quotes.ma5 != 0 && quotes.ma5 < min) {
                min = quotes.ma5;
            }
            if (quotes.ma10 != 0 && quotes.ma10 < min) {
                min = quotes.ma10;
            }
            if (quotes.ma20 != 0 && quotes.ma20 < min) {
                min = quotes.ma20;
            }
        }
        //boll
        if (masterType == KViewType.MasterIndicatrixType.BOLL || masterType == KViewType.MasterIndicatrixType.MA_BOLL) {
            //boll
            if (quotes.mb != 0 && quotes.mb < min) {
                min = quotes.mb;
            }
            if (quotes.up != 0 && quotes.up < min) {
                min = quotes.up;
            }
            if (quotes.dn != 0 && quotes.dn < min) {
                min = quotes.dn;
            }
        }
        //quotes
        if (quotes.l != 0 && quotes.l < min) {
            min = quotes.l;
        }
        //没有找到
        if (min == Integer.MAX_VALUE) {
            min = 0;
        }
        return min;

    }

    /**
     * 找到单个报价中的最大值
     *
     * @param quotes
     * @param masterType
     * @return
     */
    public static double getMasterMaxY(Quotes quotes, KViewType.MasterIndicatrixType masterType) {
        double max = Integer.MIN_VALUE;
        //ma
        //只有在存在ma的情况下才计算
        if (masterType == KViewType.MasterIndicatrixType.MA || masterType == KViewType.MasterIndicatrixType.MA_BOLL) {
            if (quotes.ma5 != 0 && quotes.ma5 > max) {
                max = quotes.ma5;
            }
            if (quotes.ma10 != 0 && quotes.ma10 > max) {
                max = quotes.ma10;
            }
            if (quotes.ma20 != 0 && quotes.ma20 > max) {
                max = quotes.ma20;
            }
        }

        //boll
        if (masterType == KViewType.MasterIndicatrixType.BOLL || masterType == KViewType.MasterIndicatrixType.MA_BOLL) {
            if (quotes.mb != 0 && quotes.mb > max) {
                max = quotes.mb;
            }
            if (quotes.up != 0 && quotes.up > max) {
                max = quotes.up;
            }
            if (quotes.dn != 0 && quotes.dn > max) {
                max = quotes.dn;
            }
        }
        //quotes
        if (quotes.h != 0 && quotes.h > max) {
            max = quotes.h;
        }
        //没有找到
        if (max == Integer.MIN_VALUE) {
            max = 0;
        }
        return max;

    }

    /**
     * 副图：找到单个报价中的最小值
     *
     * @param quotes
     * @param minorType
     * @return
     */
    public static double getMinorMinY(Quotes quotes, KViewType.MinorIndicatrixType minorType) {
        double min = Integer.MAX_VALUE;
        //macd
        if (minorType == KViewType.MinorIndicatrixType.MACD) {
            if (quotes.dif != 0 && quotes.dif < min) {
                min = quotes.dif;
            }
            if (quotes.dea != 0 && quotes.dea < min) {
                min = quotes.dea;
            }
            if (quotes.macd != 0 && quotes.macd < min) {
                min = quotes.macd;
            }
        }
        //RSI
        if (minorType == KViewType.MinorIndicatrixType.RSI) {
            if (quotes.rsi6 != 0 && quotes.rsi6 < min) {
                min = quotes.rsi6;
            }
            if (quotes.rsi12 != 0 && quotes.rsi12 < min) {
                min = quotes.rsi12;
            }
            if (quotes.rsi24 != 0 && quotes.rsi24 < min) {
                min = quotes.rsi24;
            }
        }
        //KDJ
        if (minorType == KViewType.MinorIndicatrixType.KDJ) {
            if (quotes.k != 0 && quotes.k < min) {
                min = quotes.k;
            }
            if (quotes.d != 0 && quotes.d < min) {
                min = quotes.d;
            }
            if (quotes.j != 0 && quotes.j < min) {
                min = quotes.j;
            }
        }
        //没有找到
        if (min == Integer.MAX_VALUE) {
            min = 0;
        }
        return min;

    }

    /**
     * 副图：找到单个报价中的最大值
     *
     * @param quotes
     * @param minorType
     * @return
     */
    public static double getMinorMaxY(Quotes quotes, KViewType.MinorIndicatrixType minorType) {
        double max = Integer.MIN_VALUE;
        //macd
        if (minorType == KViewType.MinorIndicatrixType.MACD) {
            if (quotes.dif != 0 && quotes.dif > max) {
                max = quotes.dif;
            }
            if (quotes.dea != 0 && quotes.dea > max) {
                max = quotes.dea;
            }
            if (quotes.macd != 0 && quotes.macd > max) {
                max = quotes.macd;
            }
        }
        //RSI
        if (minorType == KViewType.MinorIndicatrixType.RSI) {
            if (quotes.rsi6 != 0 && quotes.rsi6 > max) {
                max = quotes.rsi6;
            }
            if (quotes.rsi12 != 0 && quotes.rsi12 > max) {
                max = quotes.rsi12;
            }
            if (quotes.rsi24 != 0 && quotes.rsi24 > max) {
                max = quotes.rsi24;
            }
        }
        //KDJ
        if (minorType == KViewType.MinorIndicatrixType.KDJ) {
            if (quotes.k != 0 && quotes.k > max) {
                max = quotes.k;
            }
            if (quotes.d != 0 && quotes.d > max) {
                max = quotes.d;
            }
            if (quotes.j != 0 && quotes.j > max) {
                max = quotes.j;
            }
        }
        //没有找到
        if (max == Integer.MIN_VALUE) {
            max = 0;
        }
        return max;

    }

    /**
     * 量图：找到最大值
     *
     * @param quotes
     * @return
     */
    public static double getVolMaxY(Quotes quotes) {
        double max = Integer.MIN_VALUE;
        //vol、volma5,volma10
        if (quotes.vol != 0 && quotes.vol > max) {
            max = quotes.vol;
        }
        if (quotes.volMa5 != 0 && quotes.volMa5 > max) {
            max = quotes.volMa5;
        }
        if (quotes.volMa10 != 0 && quotes.volMa10 > max) {
            max = quotes.volMa10;
        }
        return max;
    }

    /**
     * 量图：找到最小值
     *
     * @param quotes
     * @return
     */
    public static double getVolMinY(Quotes quotes) {
        double min = Integer.MAX_VALUE;
        //vol、volma5,volma10
        if (quotes.vol != 0 && quotes.vol < min) {
            min = quotes.vol;
        }
        if (quotes.volMa5 != 0 && quotes.volMa5 < min) {
            min = quotes.volMa5;
        }
        if (quotes.volMa10 != 0 && quotes.volMa10 < min) {
            min = quotes.volMa10;
        }
        return min;
    }
}
