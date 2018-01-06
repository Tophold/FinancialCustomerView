# 金融类自定义View（三）--股票分时图(关于细节和实现思路)

## 前言
* 本篇文章首先会介绍上一篇文章[上一篇文章](http://blog.csdn.net/wgyscsf/article/details/78632594)遗留下来的问题：长按回调、缩放问题、加载更多等。
* 剩下的会介绍如何绘制这个分时图，怎么的思路，流程怎样。
* 建议，先看第二篇文章和对代码进行大致的浏览，不然阅读起来可能会有点吃力。

## 效果图

#### 原【天厚实盘】分时图

* 【天厚实盘】分时图_默认

![https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/timesharing/img/%E5%A4%A9%E5%8E%9A%E5%AE%9E%E7%9B%98_%E5%88%86%E6%97%B6%E5%9B%BE_%E9%BB%98%E8%AE%A4.png](https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/timesharing/img/%E5%A4%A9%E5%8E%9A%E5%AE%9E%E7%9B%98_%E5%88%86%E6%97%B6%E5%9B%BE_%E9%BB%98%E8%AE%A4.png)

* 【天厚实盘】分时图_长按

![https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/timesharing/img/%E5%A4%A9%E5%8E%9A%E5%AE%9E%E7%9B%98_%E5%88%86%E6%97%B6%E5%9B%BE_%E9%95%BF%E6%8C%89.png](https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/timesharing/img/%E5%A4%A9%E5%8E%9A%E5%AE%9E%E7%9B%98_%E5%88%86%E6%97%B6%E5%9B%BE_%E9%95%BF%E6%8C%89.png)

* 【天厚实盘】分时图_gif

![https://github.com/scsfwgy/FinancialCustomerView/blob/timesharing/img/%E5%A4%A9%E5%8E%9A%E5%AE%9E%E7%9B%98_%E6%BB%91%E5%8A%A8%E6%93%8D%E4%BD%9C.gif?raw=true](https://github.com/scsfwgy/FinancialCustomerView/blob/timesharing/img/%E5%A4%A9%E5%8E%9A%E5%AE%9E%E7%9B%98_%E6%BB%91%E5%8A%A8%E6%93%8D%E4%BD%9C.gif?raw=true)

#### 仿分时图【TimeSharingView.java】

* 第一阶段，参数准备，外边框、内虚线、折线图等的绘制

![https://github.com/scsfwgy/FinancialCustomerView/blob/timesharing/img/v1.2_%E5%88%86%E6%97%B6%E5%9B%BE_%E6%8A%98%E7%BA%BF%E5%9B%BE%E9%9B%8F%E5%BD%A2.png?raw=true](https://github.com/scsfwgy/FinancialCustomerView/blob/timesharing/img/v1.2_%E5%88%86%E6%97%B6%E5%9B%BE_%E6%8A%98%E7%BA%BF%E5%9B%BE%E9%9B%8F%E5%BD%A2.png?raw=true)

* 第二阶段，x、y文字、实时横线和实时数据、下方透明阴影

![https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/timesharing/img/v1.2_%E5%88%86%E6%97%B6%E5%9B%BE_%E6%8A%98%E7%BA%BF%E5%9B%BE%E9%9B%8F%E5%BD%A2_%E5%AE%8C%E5%96%84.png](https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/timesharing/img/v1.2_%E5%88%86%E6%97%B6%E5%9B%BE_%E6%8A%98%E7%BA%BF%E5%9B%BE%E9%9B%8F%E5%BD%A2_%E5%AE%8C%E5%96%84.png)

* 第三阶段，实时数据更新分时图

![https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/timesharing/img/%E5%88%86%E6%97%B6%E5%9B%BE_%E7%AC%AC%E4%B8%89%E9%98%B6%E6%AE%B5_%E5%AE%9E%E6%97%B6%E6%95%B0%E6%8D%AE%E6%98%BE%E7%A4%BA.gif](https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/timesharing/img/%E5%88%86%E6%97%B6%E5%9B%BE_%E7%AC%AC%E4%B8%89%E9%98%B6%E6%AE%B5_%E5%AE%9E%E6%97%B6%E6%95%B0%E6%8D%AE%E6%98%BE%E7%A4%BA.gif)

* 第四阶段，长按十字线，左右移动分时图

![https://github.com/scsfwgy/FinancialCustomerView/blob/timesharing/img/%E5%88%86%E6%97%B6%E5%9B%BE_%E5%8F%AF%E4%BB%A5%E9%95%BF%E6%8C%89_%E5%8F%AF%E4%BB%A5%E5%B7%A6%E5%8F%B3%E6%8B%96%E5%8A%A8.gif?raw=true](https://github.com/scsfwgy/FinancialCustomerView/blob/timesharing/img/%E5%88%86%E6%97%B6%E5%9B%BE_%E5%8F%AF%E4%BB%A5%E9%95%BF%E6%8C%89_%E5%8F%AF%E4%BB%A5%E5%B7%A6%E5%8F%B3%E6%8B%96%E5%8A%A8.gif?raw=true)

* 第五阶段，长按实时显示详细数据、加载更多处理、实时横线优化、滑动优化

![https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/dev/img/%E5%88%86%E6%97%B6%E5%9B%BE_%E7%AC%AC%E4%BA%94%E9%98%B6%E6%AE%B5_%E5%8A%A0%E8%BD%BD%E6%9B%B4%E5%A4%9A.gif](https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/dev/img/%E5%88%86%E6%97%B6%E5%9B%BE_%E7%AC%AC%E4%BA%94%E9%98%B6%E6%AE%B5_%E5%8A%A0%E8%BD%BD%E6%9B%B4%E5%A4%9A.gif)

* 第六阶段，缩放、代码整理、注释

![https://github.com/scsfwgy/FinancialCustomerView/blob/timesharing/img/%E5%88%86%E6%97%B6%E5%9B%BE_%E7%AC%AC%E5%85%AD%E9%98%B6%E6%AE%B5_%E7%BC%A9%E6%94%BE-%E4%BB%A3%E7%A0%81%E4%BC%98%E5%8C%96-%E6%B3%A8%E9%87%8A.gif?raw=true](https://github.com/scsfwgy/FinancialCustomerView/blob/timesharing/img/%E5%88%86%E6%97%B6%E5%9B%BE_%E7%AC%AC%E5%85%AD%E9%98%B6%E6%AE%B5_%E7%BC%A9%E6%94%BE-%E4%BB%A3%E7%A0%81%E4%BC%98%E5%8C%96-%E6%B3%A8%E9%87%8A.gif?raw=true)
			


## 长按回调、缩放问题、加载更多
#### 长按回调
* 在上一节，我们已经绘制出了长按的十字，也可以左右滑动。现在的需求是仿照【天厚实盘】长按在View上面绘制出按下点（十字中央）对应点的报价详情以及涨跌幅情况。这个地方的处理，有两种：直接在View上绘制；在布局中设置布局，然后把数据回调给使用者，再设置上对应数据。很明显，第二种方式更简单，也更灵活，这里采用第二种。特别注意需要回调除了当前点，还有上一个点，便于计算涨跌幅。套路：定义接口，设置回调，设置布局。

		//长按的绘制逻辑以及回调
	    protected void drawLongPress(Canvas canvas) {
	        if (!mDrawLongPressPaint) return;
				
			//长按的逻辑
	        ....
	
	        //在这里回调数据信息
	        if (mTimeSharingListener != null) {
	            int size = mQuotesList.size();
	            if ((0 <= finalIndex && finalIndex < size) &&
	                    (0 <= finalIndex - 1 && finalIndex - 1 < size))
	          //回调,需要两个数据，便于计算涨跌百分比
	         mTimeSharingListener.onLongTouch(mQuotesList.get(finalIndex - 1),
	                        mQuotesList.get(finalIndex));
	        }
	    }

#### 长按回调
* 对于缩放问题的处理，真是操碎了心，思考了好久好久。所谓缩放，我们要知道两手指缩放的距离占View有效宽度的百分比，然后根据百分比计算新的有效可视个数，然后重绘。真的有这么简单吗？我们一直绘制的依据是确定起始位置和结束位置。当两个手指缩小视图时，真正的中心点在两指中间，因此起始位置要变，结束位置也要变。最后采用的方案是采用系统的`ScaleGestureDetector`监听手指，根据`detector.getScaleFactor()`确定缩放因子。缩放思路：所谓缩放，也是计算新的起始位置和结束位置。这里根据缩放因子detector.getScaleFactor()计算新的可见个数（x缩放因子即可）。当放大时，可见的数据集合的个数(A)应该减少。detector.getScaleFactor()(B的范围[1,2)),这个时候可以新的可见数据集合（C）可以考虑采用C=A-A*(B-1);当然这样计算是否准确，还需要商榷。思路简单，但是这里细节比较多，具体可以参考代码。

		//缩放手势监听
	    ScaleGestureDetector.OnScaleGestureListener mOnScaleGestureListener =
	            new ScaleGestureDetector.SimpleOnScaleGestureListener() {
	                @Override
	                public boolean onScale(ScaleGestureDetector detector) {
	                    //没有缩放
	                    if (detector.getScaleFactor() == 1) return true;
	
	                    //是放大还是缩小
	                    boolean isBigger = detector.getScaleFactor() > 1;
	
	                    //变化的个数（缩小或者放大），必须向上取整，不然当mShownMaxCount过小时容易取到0。
	                    int changeNum = (int) Math.ceil(mShownMaxCount * Math.abs(detector.getScaleFactor() - 1));
							
							//容错处理，省略
	                    ...
	
	                    //计算新的开始位置。这个地方比较难以理解:拉伸了起始点变大，并且是拉伸数量的一半，结束点变小，也是原来的一半。
	                    // 收缩，相反。可以自己画一个图看看
	                    mBeginIndex = isBigger ? mBeginIndex + helfChangeNum : mBeginIndex - helfChangeNum;
	                    if (mBeginIndex < 0) {
	                        mBeginIndex = 0;
	                    } else if ((mBeginIndex + mShownMaxCount) > mQuotesList.size()) {
	                        mBeginIndex = mQuotesList.size() - mShownMaxCount;
	                    }
	
	                    mEndIndex = mBeginIndex + mShownMaxCount;
	
	                    //只要找好起始点和结束点就可以交给处理重绘的方法就好啦~
	                    seekAndCalculateCellData();
	                    return true;
	                }
	
	                @Override
	                public boolean onScaleBegin(ScaleGestureDetector detector) {
	                    Log.e(TAG, "onScaleBegin: " + detector.getFocusX());
	                    //指头数量，过滤无用手势
	                    if (mFingerPressedCount != 2) return true;
	                    return true;
	                }
	            };


#### 加载更多
* 本身，加载更多是很简单的，只要判断移动的时候到最右端就去加载就好。可是，这里牵扯出来另外几个问题
	* 滑动到最右边的时候，需要显示右侧的内间距和绘制小圆点（其它情况不需要显示）。
	* 不再最右侧的时候，滑动时需要隐藏右侧间距；同时，来新数据后，不应该实时绘制View。
	* 滑动到最左边时，需要加载更多。
	* 加载更多触发可能在最左侧，但是加载过程中（加载过程可以左右滑动）可能又在最右侧。状态不确定。

* 这里的处理是定义滑动枚举类型，确认实时状态

		enum PullType {
		        PULL_RIGHT,//向右滑动
		        PULL_LEFT,//向左滑动
		        PULL_RIGHT_STOP,//滑动到最右边
		        PULL_LEFT_STOP,//滑动到最左边
		    }

* 实时监听并记录状态，并且在触发加载更多时（最好设置一定阀值，比如剩余10个数据时就触发加载更多）

		   /**
		     * 移动K线图计算移动的单位和重新计算起始位置和结束位置
		     *
		     * @param moveLen
		     */
		    protected void moveKView(float moveLen) {
		        //移动之前将右侧的内间距值为0
		        mInnerRightBlankPadding = 0;
		
		        mPullRight = moveLen > 0;
		        int moveCount = (int) Math.ceil(Math.abs(moveLen) / mPerX);
		        if (mPullRight) {
		            int len = mBeginIndex - moveCount;
		           //阀值
		            if (len < DEF_MINLEN_LOADMORE) {
		                //加载更多
		                if (mTimeSharingListener != null && mCanLoadMore) {
		                    loadMoreIng();
		                    mTimeSharingListener.needLoadMore();
		                }
		            }
		             //向右拉逻辑
		            ...	
		            } else {
		            //向左拉逻辑
		            ...
		         }
		        //确认结束位置
		        mEndIndex = mBeginIndex + mShownMaxCount;
		        //开始位置和结束位置确认好，就可以重绘啦~
		        //Log.e(TAG, "moveKView: mPullRight：" + mPullRight + ",mBeginIndex:" + mBeginIndex + ",mEndIndex:" + mEndIndex);
		        seekAndCalculateCellData();
		    }	
			
	
	
## 分时图绘制思路
* 整个分时图到这里基本全部完成了，包括如下功能：基本的边框、内部虚线、x/y周文字标示、走势折现、加载更多、左右滑动、滑动数据回调、缩放、实时横线价格展示等。
* 总代码行数1000+行，完全继承系统View,不依赖任何第三方。虽然也不是很多，但是第一次看肯定是懵逼的。下面大致简述思路。
* 首先是数据的模拟，为了更加符合真实的使用场景，我们把拿到的数据进行了“转换处理”。可以想象，真实使用场景，不可能你直接从服务端拿到的数据就可以刚好符合View的数据类型。至于获取数据、模拟网络环境、切线程、模拟实时Socket推数据等采用了Rx进行了处理（多说一句，Rx在线程切换真是好用到爆）。
* 基本点，我们会把拿到数据集合（包括推过来的实时数据）全部存到全局的List<Quotes>中，保证单一数据集合。定义可视范围的起始点mBeginIndex和结束点mEndIndex。有大致了解代码的同学，会看到整个View的大部分操作过程中，主要是计算起始点和结束点，然后重绘View。是的，实时加载数据、左右滑动、缩放、加载更多这些核心功能，其实都是为了计算新的起始位置和结束位置。
* 核心方法，核心方法全部都在`onDraw(Canvas canvas)`中完成，但是为了逻辑清晰，我们会单独绘制每一个业务功能，保证业务逻辑的清晰，方便修改和扩展。其它的，手势监控在`onTouchEvent(MotionEvent event)`和`ScaleGestureDetector mScaleGestureDetector`中完成。加载数据直接由使用者传递。

		 @Override
		    protected void onDraw(Canvas canvas) {
		        super.onDraw(canvas);
		        //Log.e("TimeSharingView", "onDraw: ");
		        //默认加载loading界面
		        showLoadingPaint(canvas);
		        if (mQuotesList == null || mQuotesList.isEmpty()) {
		            return;
		        }
		        drawOuterLine(canvas);
		        drawInnerXy(canvas);
		        drawXyTxt(canvas);
		        drawBrokenLine(canvas);
		        //长按处理
		        drawLongPress(canvas);
		        //长按情况下的时间和数据框
		        drawLongPressTxt(canvas);
		    }


* 关于绘图，绘图全部相对于View的左上角开始，在View全局中定义了宽度和高度以及内边距等，基本上在View中绘制任何线和图全部会相对这几个值进行操作，更多的是对位置的准确把握以及对边界的准确控制。

		    //控件宽高，会在onMeasure中进行测量。
		    int mWidth;
		    int mHeight;
		    //上下左右padding，这里不再采用系统属性padding，因为用户容易忘记设置padding,直接在这里更改即可。
		    float mPaddingTop = 20;
		    float mPaddingBottom = 50;
		    float mPaddingLeft = 8;
		    float mPaddingRight = 90;
		    //默认情况下结束点距离右边边距
		    float mInnerRightBlankPadding = DEF_INNER_RIGHT_BLANK_PADDING;
		    //为了美观，容器内（边框内部的折线图距离外边框线的上下距离）上面有一定间距，下面也有一定的间距。
		    float mInnerTopBlankPadding = 60;
		    float mInnerBottomBlankPadding = 60;
		    
* 关于y轴最大最小值的确认，这个比较重要。如何保证分时图准确绘制不绘制出边界？如果数据的范围本来是[1,100],突然来了一个10000的数据怎么办？处理的手段是，每次拿到数据之后遍历，找到分时图**可视范围**内的最大值和最小值和View有效高度，算出来单位高度，然后根据每个点的值和最小值（最大值也可以）的差值计算应有的高度坐标即可。如果出现上述中的特别特别大的数据怎么办？那可能就绘制出一条直线咯（生产环境中有遇到）。

* 关于错误和调试，有阅读代码的同学可以在代码中看到大量的Log日志。写的过程中遇到了很多问题，由于有大量的数据还实时推数据并且实时刷新绘制，这个时候可能debug就很难发现问题，直接打Log是有效的手段，可以实时观察到数据的异常。当然，大部分问题直接卡断点就能定位到问题。
	

#### code
* [`https://github.com/scsfwgy/FinancialCustomerView`](https://github.com/scsfwgy/FinancialCustomerView "https://github.com/scsfwgy/FinancialCustomerView")
* 注：该项目会一直维护
	* 绘制各种金融类的自定义View。
	* 提供金融类自定义View的实现思路。
	* 收集整理相关算法、文档以及专业资料。
* 另，蜡烛图（包括主图指标）大部分功能已经绘制出来啦，代码也进行了大量的重构。在分支：feature_candleview	
	
	
	
	
	
	
	
	
	
	





