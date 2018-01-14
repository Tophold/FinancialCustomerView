# 金融类自定义View（四）--股票蜡烛图(包含MA、BOLL指标)以及代码重构

## 前言
* 本文只描述蜡烛图单独的绘制逻辑，至于和分时图相同的逻辑不再阐述，感兴趣的可以查看之前的文章，会有详细的阐述。
* 首先会介绍蜡烛图的绘制思路、MA/BOLL指标的绘制、指标y轴越界问题、x轴绘制的细节处理。
* 最后会介绍一下代码的重构处理以及对后期副图部分的绘制影响。

## 效果图

#### 蜡烛图【MasterView.java】

* 默认蜡烛图

![https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/feature_candleview/img/%E8%9C%A1%E7%83%9B%E5%9B%BE_%E7%AC%AC%E4%B8%89%E9%98%B6%E6%AE%B5_%E8%9C%A1%E7%83%9B%E5%9B%BE.png](https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/feature_candleview/img/%E8%9C%A1%E7%83%9B%E5%9B%BE_%E7%AC%AC%E4%B8%89%E9%98%B6%E6%AE%B5_%E8%9C%A1%E7%83%9B%E5%9B%BE.png)

* MA指标（BOLL类似）

![https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/feature_candleview/img/%E8%9C%A1%E7%83%9B%E5%9B%BE_%E7%AC%AC%E5%9B%9B%E9%98%B6%E6%AE%B5_ma_nopress.png](https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/feature_candleview/img/%E8%9C%A1%E7%83%9B%E5%9B%BE_%E7%AC%AC%E5%9B%9B%E9%98%B6%E6%AE%B5_ma_nopress.png)

* 蜡烛图长按

![https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/feature_candleview/img/%E8%9C%A1%E7%83%9B%E5%9B%BE_%E7%AC%AC%E5%9B%9B%E9%98%B6%E6%AE%B5_ma_press.png](https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/feature_candleview/img/%E8%9C%A1%E7%83%9B%E5%9B%BE_%E7%AC%AC%E5%9B%9B%E9%98%B6%E6%AE%B5_ma_press.png)

* gif

![https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/feature_candleview/img/%E8%9C%A1%E7%83%9B%E5%9B%BE_%E7%AC%AC%E5%9B%9B%E9%98%B6%E6%AE%B5_gif.gif](https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/feature_candleview/img/%E8%9C%A1%E7%83%9B%E5%9B%BE_%E7%AC%AC%E5%9B%9B%E9%98%B6%E6%AE%B5_gif.gif)


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

    	
 ![https://github.com/scsfwgy/FinancialCustomerView/blob/master/img/MA%E6%8C%87%E6%A0%87%E8%BE%B9%E7%95%8C.png?raw=true](https://github.com/scsfwgy/FinancialCustomerView/blob/master/img/MA%E6%8C%87%E6%A0%87%E8%BE%B9%E7%95%8C.png?raw=true)
    	

#### 越界问题
* 在开始之前看以下两个截图，对于同一个时刻一个不展示BOLL线，一个展示BOLL线，绘制出来的蜡烛图折线图是“不一样”的。

 ![https://github.com/scsfwgy/FinancialCustomerView/blob/master/img/%E6%9C%89boll%E5%92%8C%E6%B2%A1%E6%9C%89boll%E5%AF%B9%E6%AF%94%E5%9B%BE.png?raw=true](https://github.com/scsfwgy/FinancialCustomerView/blob/master/img/%E6%9C%89boll%E5%92%8C%E6%B2%A1%E6%9C%89boll%E5%AF%B9%E6%AF%94%E5%9B%BE.png?raw=true)
 
 * 这并不是绘制错误，而是特意的处理。在绘制分时图的时候，我们对于Y轴的mPerY的计算是根据可视范围内数据集合的收盘价最大值和最小值以及Y轴有效高度计算的。这样的处理可以保证无论分时图浮动多大，可以保证分时图不会绘制出边界。分时图“值”比较单一，只需要考虑一个收盘价即可。可是，对于蜡烛图需要考虑的就比较多了，如果还是仅仅通过收盘价计算最小值和最大值，就会导致如果计算出来的“待显示的点”如果远远大于收盘价，就会导致“越界”！并且，这种情况，确实在开发过程中遇到了。因此，对于蜡烛图最大值和最小值的判断，需要遍历单个Quotes,寻找到最大和最小值。通过观察线上应用【天厚实盘】以及同花顺基本上都是这样处理的：随着主图上指标的变化，蜡烛图显示的高度并不一致，为的就是保证“不越界”。以下是寻找单个Quotes中最大值的过程，特别注意，if判断逻辑不能用`else if`，这个错误调试了几个小时才找到！

 
			  /**
			     * 找到单个报价中的最大值
			     *
			     * @param quotes
			     * @param masterType
			     * @return
			     */
			    public static double getMasterMaxY(Quotes quotes, MasterView.MasterType masterType) {
			        double max = Integer.MIN_VALUE;
			        //ma
			        //只有在存在ma的情况下才计算
			        if (masterType == MasterView.MasterType.MA || masterType == MasterView.MasterType.MA_BOLL) {
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
			        if (masterType == MasterView.MasterType.BOLL || masterType == MasterView.MasterType.MA_BOLL) {
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


## 代码重构
* 其实在分时图绘制完毕之后打算先整副图部分的，后来发现各种指标算法比较烦人，就直接上手蜡烛图了。
* 随着View功能、交互的的增加，代码量也大量激增，在分时图功能完成时整个View的代码行数已经过1000行了，感觉多的都没法维护了=。=，因此考虑对代码进行重构。
* 在开始绘制绘制蜡烛图之前，对代码进行了大量的重构，将一些公共的逻辑抽离到了父类，尽可能减少子类中的业务逻辑。什么是公共的逻辑？比如边距、单机长按阀值、背景、边框等等很多属性其实主图和副图都是一样的，也有部分一样的，可以分开对待，比如主图需要绘制x/y内虚线，而副图只需要绘制y轴的虚线即可。当然，还有一些父类没法实现的，可以考虑将方法抽象化，让子类去实现业务逻辑。
* 整个继承关系如下

![https://github.com/scsfwgy/FinancialCustomerView/blob/master/img/view%E7%BB%A7%E6%89%BF%E5%85%B3%E7%B3%BB.png?raw=true](https://github.com/scsfwgy/FinancialCustomerView/blob/master/img/view%E7%BB%A7%E6%89%BF%E5%85%B3%E7%B3%BB.png?raw=true)

* 在处理的时候，把和业务没有关系的View逻辑放到了BaseFinancialView中，比如各种常量的获取、View宽高的测量、以及View的工具类等。而在KView.java中则是处理主图和副图全部都有（或者部分有）的共有属性。对于部分有的特性，比如副图没有x轴的虚线绘制，可以用一个开关在父类中处理是否绘制，开关可以由子类控制。

			    /**
			     * 绘制内部x/y轴虚线
			     * @param canvas
			     */
			    protected void drawInnerXy(Canvas canvas) {
			        if (isShowInnerX())
			            drawInnerX(canvas);
			        if (isShowInnerY())
			            drawInnerY(canvas);
			    }
			    
* 代码继承关系整好之后，一些线基本就OK了，下面是副图的截图，基本没有做任何处理，就可以直接显示了

![https://github.com/scsfwgy/FinancialCustomerView/blob/master/img/%E5%89%AF%E5%9B%BE_def.png?raw=true](https://github.com/scsfwgy/FinancialCustomerView/blob/master/img/%E5%89%AF%E5%9B%BE_def.png?raw=true)
			    
* 而一些父类没法实现具体逻辑的，可以直接声明为抽象类，让子类去实现即可。

		   /**
		     * 父类：寻找边界和计算单元数据大小。寻找:x轴开始位置数据和结束位置的model、y轴的最大数据和最小数据对应的model；
		     * 计算x/y轴数据单元大小。这个交给子类去实现，一般情况下寻找边界需要遍历，在父类中遍历没有意义，
		     * 因为不知道子类还有什么遍历需求。因此改为抽象方法，子类实现。子类必须完成寻找边界的任务。
		     */
		    protected abstract void seekAndCalculateCellData();
    
 			-------------------------------------------------------   
    
    		//MasterView实现具体逻辑。
		 	@Override
		    protected void seekAndCalculateCellData() {
		        if (mQuotesList.isEmpty()) return;
		
		        //对于蜡烛图，需要计算以下指标。
		        if (mViewType == ViewType.CANDLE) {
		           //指标算法
		           ...
		        }
		
		
		        //寻找边界和计算单元数据大小
		        ...
		        
		        //计算mPerX、mPerY		
		       ...
		       		
		        //重绘
		        invalidate();
	    		}

* 通过代码重构的手段，实现了在分时图、蜡烛图以及蜡烛图对应指标、其它各种交互共存的情况下，MasterView的代码行数还是维持在1000多行，MasterView基本没有增加代码量。现在存在的问题是，仍然存在各种各样的属性散列在View中，感觉难以控制。MP作者PhilJay在处理这个问题是将所有属性聚合成一个对象，在使用时可能会有很多层调用，但是在使用者（开发者）层面，看到的只有一个对象，然后控制着View的各种表现。在下一阶段重构的过程中，会考虑属性的处理方式是否也采用这种方式。


#### code
* [`https://github.com/scsfwgy/FinancialCustomerView`](https://github.com/scsfwgy/FinancialCustomerView "https://github.com/scsfwgy/FinancialCustomerView")
* 注：该项目会一直维护
	* 绘制各种金融类的自定义View。
	* 提供金融类自定义View的实现思路。
	* 收集整理相关算法、文档以及专业资料。
	* 代码进行了大量重构。
	* 蜡烛图绘制完毕。	
	
	
	
	
	
	
	
	
	
	





