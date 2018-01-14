# 金融类自定义View（四）--股票蜡烛图(包含MA、BOLL指标)以及代码重构

## 前言
* 本文只描述蜡烛图单独的绘制逻辑，至于和分时图相同的逻辑不再阐述，感兴趣的可以查看之前的文章，会有详细的阐述。
* 首先会介绍蜡烛图的绘制思路、MA/BOLL指标的绘制、指标y轴越界问题、x轴绘制的细节处理。
* 最后会介绍一下代码的重构处理以及对后期副图部分的绘制影响。

## 效果图

## 蜡烛图绘制
#### 命名
* 主流的交易曲线图会包含两个大部分，上面一大块是走势图，包含分时图、5分图、15分图、日k、周k、月k等；下面一小块是指标，包含成交量、MACD、KDJ、RSI等。如下图，是同花顺的交易曲线图，基本也是这样构成的：


![https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/master/img/%E5%90%8C%E8%8A%B1%E9%A1%BA%E4%BA%A4%E6%98%93%E5%9B%BE.png](https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/master/img/%E5%90%8C%E8%8A%B1%E9%A1%BA%E4%BA%A4%E6%98%93%E5%9B%BE.png)

* 因此，在本项目命名如下，整个交易曲线图称为KView,上面的部分称为MasterView(主图，包含分时图和蜡烛图)；下面的部分称为MinorView(副图，包含各种副图指标)。其中蜡烛图又包含MA(5,10,20)、BOLL(26)、MA(5,10,20)\BOLL(26)三种指标，对于副图部分，计划只绘制以下三种指标，和【天厚实盘】APP保持同步：MACD(12,26,9)、RSI(6,12,24)、KDJ(9,3,3)。

#### 蜡烛图的绘制
* 仅仅是蜡烛图的绘制很简单，之前绘制好分时图就说过这个问题。蜡烛图的矩形Y轴是根据开盘价格和闭市价格绘制对应的高度，至于哪个在上哪个在下不确定。X轴的绘制是在原来分时图目标点向左向右分别取1/2.0个单元大小。对于蜡烛间间距的处理，这里采用倍率的处理方式，间距是单个蜡烛宽度的0.1f（为什么不采用固定大小？当缩放蜡烛图时如果间距固定大小会非常丑）。至于蜡烛图Y轴中间的横线则是根据最高价和最低价对应的高度进行绘制。
* 蜡烛图的绘制如下，特别注意边界的处理，当处理可视范围内第一个Quotes时leftRectX、leftLineX必须判断和左边界的关系，取最大的，不然在界面的表现就是蜡烛图会绘制出左边界。同理，可视范围内结束点也需要考虑这个问题，同时，右侧有一个内边距需要考虑进去。


		    /**
		     * 绘制蜡烛图
		     * @param canvas
		     * @param diverWidth 蜡烛图之间的间距
		     * @param List index
		     * @param quotes 目标Quotes
		     */
		    private void drawCandleViewProcess(Canvas canvas, float diverWidth, int i, 					Quotes quotes) {
		       //异常拦截以及参数设置
		        ...
		        
		        //定位蜡烛矩形的四个点
		        topRectY = (float) (mPaddingTop + mInnerTopBlankPadding +
		                mPerY * (mCandleMaxY - quotes.o));
		        bottomRectY = (float) (mPaddingTop + mInnerTopBlankPadding +
		                mPerY * (mCandleMaxY - quotes.c));
		        leftRectX = -mPerX / 2 + quotes.floatX + diverWidth / 2;
		        rightRectX = mPerX / 2 + quotes.floatX - diverWidth / 2;
		
		        //定位单个蜡烛中间线的四个点
		        leftLineX = quotes.floatX;
		        topLineY = (float) (mPaddingTop + mInnerTopBlankPadding +
		                mPerY * (mCandleMaxY - quotes.h));
		        rightLineX = quotes.floatX;
		        bottomLineY = (float) (mPaddingTop + mInnerTopBlankPadding +
		                mPerY * (mCandleMaxY - quotes.l));
		
		        RectF rectF = new RectF();
		        //边界处理
		        if (i == mBeginIndex) {
		            leftRectX = leftRectX < mPaddingLeft ? mPaddingLeft : leftRectX;
		            leftLineX = leftLineX < mPaddingLeft ? mPaddingLeft : leftLineX;
		        } else if (i == (mEndIndex - 1)) {
		            rightRectX = rightRectX > mWidth - mPaddingRight ? mWidth - mPaddingRight : rightRectX;
		            rightLineX = rightLineX > mWidth - mPaddingRight ? mWidth - mPaddingRight : rightLineX;
		        }
		        rectF.set(leftRectX, topRectY, rightRectX, bottomRectY);
		        //设置颜色
		        mCandlePaint.setColor(quotes.c > quotes.o ? mRedCandleColor : mGreenCandleColor);
		        
		        //绘制蜡烛图
		        canvas.drawRect(rectF, mCandlePaint);
		
		        //开始画low、high线
		        canvas.drawLine(leftLineX, topLineY, rightLineX, bottomLineY, mCandlePaint);
		    }
		    
		 
#### MA、BOLL的绘制
* MA、BOLL是在主图的指标表现,可以辅助用户在交易中做出策略指导。链接：[百度百科：移动平均线(MA)](https://baike.baidu.com/item/%E7%A7%BB%E5%8A%A8%E5%B9%B3%E5%9D%87%E7%BA%BF/217887?fr=aladdin&fromid=1511750&fromtitle=MA)，[百度百科：布林线(BOLL)](https://baike.baidu.com/item/%E5%B8%83%E6%9E%97%E7%BA%BF/3424486?fr=aladdin)
* 在本项目中MA取最常用的5单位、10单位、20单位，简称MA(5,10,20);BOLL取常用的26单位，简称BOLL(26)。当然，在本项目中到的各种指标算法全部都抽取出来，在FinancialAlgorithm.java中。一般指标算法都是比较繁琐的，要么是多日均值，要么就是加权等等，所以对算法要求尽可能的高效。下面是MA的计算，之前写的是采用双重循环，改后之后只要一层即可，尽可能避免多重循环。


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
	                sum1 -= quotesList.get(i - period).c;//减去之前算过的。
	            }
	
	            if (period == 5) {
	                quotes.ma5 = sum1 / period;
	            } else if (period == 10) {
	                quotes.ma10 = sum1 / period;
	            } else if (period == 20) {
	                quotes.ma20 = sum1 / period;
	            } else {
	                Log.e(TAG, "calculateMA: 没有该种period，TODO:完善Quotes");
	                return;
	            }
	
	        }
	    }

* 对于主图指标的切换，采用的是单击click，四种类型。对于指标的绘制其实就是绘制Path，只要能确认x轴和y轴对应坐标即可。对于x轴上文已经说了，取单个蜡烛的x方向即可。对于y轴，这里取的是对应Quotes的MA值，当然这里的MA有三种MA5、MA10、MA20,因此画出来也是三条线。有心的同学看上面计算MA的算法有一个判断`if (i < period - 1) {continue;}`，对于`i < period - 1`直接就continue，意味着就没有对应的MA值。是的，对于数据集合当开始的时候是没有MA值的，有些资料会取一些默认值进行显示，这里的处理方式是不进行计算，在View的效果就是不绘制（效果如下图）。BOLL同理。


	 	/**
	     * 主图上面的技术指标类型
	     */
	    public enum MasterType {
	        NONE,//无
	        MA,//MA5、10、20
	        BOLL,//BOLL(26)
	        MA_BOLL//MA5、10、20和BOLL(26)同时展示
	    }

    	
		private void drawMa(Canvas canvas) {
		        //参数初始化
		        ...
		        
		        for (int i = mBeginIndex; i < mEndIndex; i++) {
		            Quotes quotes = mQuotesList.get(i);
		            //在绘制蜡烛图的时候已经计算了
		            float floatX = quotes.floatX;
		
					  //绘制MA5的逻辑
		            float floatY = getMasterDetailFloatY(quotes, MasterDetailType.MA5);
		
		            //异常,在View的行为就是不显示而已，影响不大。一般都是数据的开头部分。
		            if (floatY == -1) continue;
		
		            if (isFirstMa5) {
		                isFirstMa5 = false;
		                path5.moveTo(floatX, floatY);
		            } else {
		                path5.lineTo(floatX, floatY);
		            }
					
						//这里是绘制MA10/20的逻辑，和MA5一样
						...
		           
		          }
		
		        canvas.drawPath(path5, mMa5Paint);
		        canvas.drawPath(path10, mMa10Paint);
		        canvas.drawPath(path20, mMa20Paint);
		    }    	

    	
    	
    	

#### 越界问题

## 代码重构
#### 长按回调

#### code
* [`https://github.com/scsfwgy/FinancialCustomerView`](https://github.com/scsfwgy/FinancialCustomerView "https://github.com/scsfwgy/FinancialCustomerView")
* 注：该项目会一直维护
	* 绘制各种金融类的自定义View。
	* 提供金融类自定义View的实现思路。
	* 收集整理相关算法、文档以及专业资料。
* 另，蜡烛图（包括主图指标）大部分功能已经绘制出来啦，代码也进行了大量的重构。在分支：feature_candleview	
	
	
	
	
	
	
	
	
	
	





