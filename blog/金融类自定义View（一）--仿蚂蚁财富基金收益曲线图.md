## 金融类自定义View（一）--仿蚂蚁财富基金收益曲线图
> 该自定义View模仿了蚂蚁金服的基金收益曲线图走势。大致包含以下功能：x轴绘制等分时间坐标；y轴绘制等分收益值；中间绘制等分虚线；收益变化折线图；上方默认显示最后收益值；长按显示当前距离最近按下点的时间坐标以及对应收益值的十字线，同时上方显示对应时间与收益。看似简单的一个收益曲线图，包含很多细节技术点需要处理。虽然该自定义View比较简单，但是和金融类复杂的K线图、蜡烛图等以及相应复杂的交互本质上是一样的，会提供很好的解决思路。本文不会讲解自定义View入门知识。enjoy it~

#### 效果图
* 原蚂蚁金服收益View操作GIF

![](https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/master/img/ant.gif)

* 仿操作GIF

![](https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/master/img/fang.gif)

* 截图

![](https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/master/img/ant_nopress.PNG)

![](https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/master/img/ant_press.PNG)

![](https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/master/img/fang_nopress.png)

![](https://raw.githubusercontent.com/scsfwgy/FinancialCustomerView/master/img/fang_press.png)

#### 中间虚线的绘制
* 要绘制横轴那五条虚线，我们首先需要知道该控件的大小和位置，由调用者在使用时设置，控件在onMeasure()的时候获取，特别注意处理一下`AT_MOST`的情况。另外，我们需要设置上下左右的起始位置，也就是给该控件设置好Padding值，如下：

		//上下左右padding
		float mPaddingTop = 100;
		float mPaddingBottom = 70;
		float mPaddingLeft = 50;
		float mPaddingRight = 50;

* 知道了绘制的起始和结束位置之后，我们需要初始化一个绘制虚线的画笔，用来在Canves上绘制虚线

		//初始化绘制虚线的画笔
	    private void initInnerXPaint() {
	        mInnerXPaint = new Paint();
	        mInnerXPaint.setColor(getColor(R.color.color_fundView_xLineColor));
	        mInnerXPaint.setStrokeWidth(convertDp2Px(mInnerXStrokeWidth));
	        mInnerXPaint.setStyle(Paint.Style.STROKE);
	        setLayerType(LAYER_TYPE_SOFTWARE, null);//禁用硬件加速
	        PathEffect effects = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);
	        mInnerXPaint.setPathEffect(effects);
	    }

* 其实知道位置和初始化画笔之后，就可以直接绘制了。我们很容易知道最上面的那一条的起始点和结束点是`(mPaddingLeft, mPaddingTop)`,`(mWidth - mPaddingRight, mPaddingTop)`,就是左右上边界减去左右和上的Padding值。同理，下方的位置也很好确定。至于中间那三条线的确定，因为是上下平移，所以主要是确定y周的坐标。我们可以根据最下面线的y周坐标减去最上面的y轴值取均分值即可：

		 private void drawInnerXPaint(Canvas canvas) {
		        //画5条横轴的虚线
		        //首先确定最大值和最小值的位置
		        float perHight = (mHeight - mPaddingBottom - mPaddingTop) / 4;
		
		        canvas.drawLine(0 + mPaddingLeft, mPaddingTop,
		                mWidth - mPaddingRight, mPaddingTop, mInnerXPaint);//最上面的那一条
		
		        canvas.drawLine(0 + mPaddingLeft, mPaddingTop + perHight * 1,
		                mWidth - mPaddingRight, mPaddingTop + perHight * 1, mInnerXPaint);//2
		
		        canvas.drawLine(0 + mPaddingLeft, mPaddingTop + perHight * 2,
		                mWidth - mPaddingRight, mPaddingTop + perHight * 2, mInnerXPaint);//3
		
		        canvas.drawLine(0 + mPaddingLeft, mPaddingTop + perHight * 3,
		                mWidth - mPaddingRight, mPaddingTop + perHight * 3, mInnerXPaint);//4
		
		        canvas.drawLine(0 + mPaddingLeft, mHeight - mPaddingBottom,
		                mWidth - mPaddingRight, mHeight - mPaddingBottom, mInnerXPaint);//最下面的那一条
		
		    }

* 按照上述步骤执行完毕之后，`invalidate()`刷新界面即可。其实虚线的绘制还是比较简单的，主要是定位、定分y周轴。

#### x轴时间文字和y轴收益文字的定位和绘制
* 首先定义绘制x\y轴文字的画笔、字体大小颜色等属性。当然，如果要细分x轴和y轴可以定义两套Paint,这里简单处理，只用了一个Paint:

	 	//外围X、Y轴线文字
	    Paint mXYPaint;
	    //x、y轴指示文字字体的大小
	    final float mXYTextSize = 14;
	    //左侧文字距离左边线线的距离
	    final float mLeftTxtPadding = 16;
	    //底部文字距离底部线的距离
	    final float mBottomTxtPadding = 20;

* 对于x轴的处理，我们只需要获取列表中第一个Model的时间值、结束位置的时间值和中间值寄存下来，然后在固定位置显示即可。对于第一个时间的位置，和绘制最下面一条虚线定位一样。中间时间文字的显示，x轴的坐标是：总宽度减去左右pading值，然后取半再加上左边pdding值。当然，你如果仔细观察，这个时候x并不是居中显示的，因为是从**中间点**开始显示的，所有会错一个一半的时间文字宽度的值，这个时候应该再减去txt.lnegth()/2。同理，最后一个时间点的x轴开始位置也是需要减去文字的宽度的，不然就会绘制出范围。特别注意，为了好看，我们让文字向下偏移了一定的距离：`float hight = mHeight - mPaddingBottom + mBottomTxtPadding;`。

		  //找到最大时间、最小时间和中间时间显示即可
		    private void drawXPaint(Canvas canvas) {
		        long beginTime = mFundModeList.get(0).datetime;
		        long midTime = mFundModeList.get((mFundModeList.size() - 1) / 2).datetime;
		        long endTime = mFundModeList.get(mFundModeList.size() - 1).datetime;
		        String bengin = processDateTime(beginTime);
		        String mid = processDateTime(midTime);
		        String end = processDateTime(endTime);
		
		        //x轴文字的高度
		        float hight = mHeight - mPaddingBottom + mBottomTxtPadding;
		
		        canvas.drawText(bengin,
		                mPaddingLeft,
		                hight, mXYPaint);
		
		        canvas.drawText(mid,
		                mPaddingLeft + (mWidth - mPaddingLeft - mPaddingRight) / 2,
		                hight, mXYPaint);
		
		        canvas.drawText(end,
		                mWidth - mPaddingRight - mXYPaint.measureText(end),
		                hight, mXYPaint);//特别注意x轴的处理：- mXYPaint.measureText(end)
		
		    }


* 对于y轴文字的处理，我们并不知道最小值和最大值，所以我们需要遍历记录下最小值和最大值。然后均分取间距，在左侧绘制即可。也需要特别注意文字绘制起始点的问题。

#### 折线的绘制
* 对于折线的处理就比较麻烦了。按照上述思路，x轴可以直接取有效总宽度除以数据总个数，计算出每一个点的宽度即可。对于y轴，我们可以考虑一下，y轴的收益可能小到1分，或者大到100元，怎么确定每一点的y轴位置，如何保证绘制的曲线不出有效范围？我们可以用y轴有效的总高度除以收益的最大最小区间，获取每个单位的高度。当绘制时，根据对应时间点的收益乘以单位收益，即可以计算出需要的高度：

		//获取单个数据X/y轴的大小
        mPerX = (mWidth - mPaddingLeft - mPaddingRight) / mFundModeList.size();
        mPerY = ((mHeight - mPaddingTop - mPaddingBottom) / (mMaxFundMode.dataY - mMinFundMode.dataY));
        Log.e(TAG, "setDataList: " + mMinFundMode + "," + mMaxFundMode + "..." + mPerX + "," + mPerY);

* 根据上述计算出来单位高度，绘制y轴文字信息

		private void drawYPaint(Canvas canvas) {
		        //现将最小值、最大值画好
		        //draw min
		        float txtWigth = mXYPaint.measureText(mMinFundMode.originDataY) + mLeftTxtPadding;
		        canvas.drawText(mMinFundMode.originDataY + "",
		                mPaddingLeft - txtWigth,
		                mHeight - mPaddingBottom, mXYPaint);
		        //draw max
		        canvas.drawText(mMaxFundMode.dataY + "",
		                mPaddingLeft - txtWigth,
		                mPaddingTop, mXYPaint);
		        //因为横线是均分的，所以只要取到最大值最小值的差值，均分即可。
		        float perYValues = (mMaxFundMode.dataY - mMinFundMode.dataY) / 4;
		        float perYWidth = (mHeight - mPaddingBottom - mPaddingTop) / 4;
		        //从下到上依次画
		        for (int i = 1; i <= 3; i++) {
		            canvas.drawText(mMinFundMode.dataY + perYValues * i + "",
		                    mPaddingLeft - txtWigth,
		                    mHeight - mPaddingBottom - perYWidth * i, mXYPaint);
		        }
		    }

#### 不长按上面最终收益文字的绘制
* 我们观察可以发现，默认文字包括：绿色的小圆点、提示文字、收益金额。看似简单的几个字，也需要一个一个处理，准备不同的画笔（当然也可以共用一个画笔）。代码不再给出。

#### 长按上面最终收益文字的绘制
* 这一块因为需要监测用户的长按状态，会稍微复杂一点。首先需要在`onTouchEvent(MotionEvent event)`获取长按监听：

		 @Override
		    public boolean onTouchEvent(MotionEvent event) {
		        switch (event.getAction()) {
		            case MotionEvent.ACTION_DOWN:
		                mPressTime = event.getDownTime();
		                break;
		            case MotionEvent.ACTION_MOVE:
		                if (event.getEventTime() - mPressTime > DEF_LONGPRESS_LENGTH) {
		                    Log.e(TAG, "onTouchEvent: 长按了。。。");
		                    mPressX = event.getX();
		                    mPressY = event.getY();
		                    //处理长按后的逻辑
		                    showLongPressView();
		                }
		                break;
		            case MotionEvent.ACTION_UP:
		                //处理松手后的逻辑
		                hiddenLongPressView();
		                break;
		            default:
		                break;
		        }
		
		        return true;
		    }

* 至于长按后的处理，其实并不是在`showLongPressView();`中处理，这里只是去通知onCanves()方法去刷新界面。同时用一个标记位处理是否绘制十字：

		   private void showLongPressView() {
		        mDrawLongPressPaint = true;
		        invalidate();
		    }


* 这里有一个问题，仔细观看GIF图，十字的中间位置并不一定是用户手指长按的位置，而是距离按下最近的有效时间点和对应的收益坐标的位置。这样描述可能比较难以理解，这么说吧，比如你点击的是（4,5），但是这一点并没有刚好对应的那一天（因为一天是一个区间，而坐标是一个点），而最近的一天的坐标是（4.1,5.1）。不仅仅x轴不能根据按下的位置确定，并且y轴也不能确认！需要确认到距离按下的最近的有效位置。这里的处理思路是：获取按下的x轴的位置坐标，然后遍历集合中的x轴的时间点所对应的坐标，找到间距最小的有效点，该点的x轴即有效的x轴点。为了简单处理y轴的值，这里保存的是对应点的Model,不用再去处理y轴的有效点。至于绘制十字就可以根据该有效点的x、y轴的坐标去处理：

		 /**
		     * 这里处理画十字的逻辑:这里的十字不是手指按下的位置，这样没有意义。
		     * 而是当前按下的距离x轴最近的时间（注意：并不一定按下对应的x轴就是有时间的，如果没有取最近的）。
		     * 当取到x轴的值，之后算出来对应的y轴的值，这个才是十字对应的位置坐标。
		     * 如何获取x轴最近的时间？我们可以在FundMode中定义x\y的位置参数，遍历对比找到最小即可。
		     * (see: drawBrokenPaint(canvas);)
		     *
		     * @param canvas
		     */
		    private void drawLongPress(Canvas canvas) {
		        if (!mDrawLongPressPaint) return;
		
		        //获取距离最近按下的位置的model
		        float pressX = mPressX;
		        //循环遍历，找到距离最短的x轴的mode
		        FundMode finalFundMode = mFundModeList.get(0);
		        float minXLen = Integer.MAX_VALUE;
		        for (int i = 0; i < mFundModeList.size(); i++) {
		            FundMode currFunMode = mFundModeList.get(i);
		            float abs = Math.abs(pressX - currFunMode.floatX);
		            if (abs < minXLen) {
		                finalFundMode = currFunMode;
		                minXLen = abs;
		            }
		        }
		
		        //x
		        canvas.drawLine(mPaddingLeft, finalFundMode.floatY, mWidth - mPaddingRight, finalFundMode.floatY, mLongPressPaint);
		        //y
		        canvas.drawLine(finalFundMode.floatX, mPaddingTop, finalFundMode.floatX, mWidth - mPaddingBottom, mLongPressPaint);
		
		        //开始处理按下之后top的文字信息
		        //先画背景
		        float hight = mPaddingTop - 30;
		        Paint bgColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		        bgColor.setColor(getColor(R.color.color_fundView_pressIncomeTxtBg));
		        canvas.drawRect(0, 0, mWidth, hight, bgColor);
		
		        //开始画按下之后左边的日期文字
		        Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		        timePaint.setTextSize(mLongPressTextSize);
		        timePaint.setColor(getColor(R.color.color_fundView_xyTxtColor));
		        canvas.drawText(processDateTime(finalFundMode.datetime) + "",
		                10, hight / 2 + getFontHeight(mLoadingTextSize, timePaint) / 2, timePaint);
		
		        //右边红色收益文字
		        canvas.drawText(finalFundMode.dataY + "",
		                mWidth - mPaddingRight - mLongPressPaint.measureText(finalFundMode.dataY + ""),
		                hight / 2 + getFontHeight(mLoadingTextSize, timePaint) / 2, mLongPressPaint);
		
		        //右边的左边的提示文字
		        Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		        hintPaint.setTextSize(mLongPressTextSize);
		        hintPaint.setColor(getColor(R.color.color_fundView_xyTxtColor));
		        canvas.drawText(getString(R.string.string_fundView_pressHintTxt),
		                mWidth - mPaddingRight - mLongPressPaint.measureText(finalFundMode.dataY + "")
		                        - hintPaint.measureText(getString(R.string.string_fundView_pressHintTxt)),
		                hight / 2 + getFontHeight(mLoadingTextSize, timePaint) / 2, hintPaint);
		
		
		    }


* 如何实现蚂蚁金服基金收益的长按后十字延迟消失？这里延迟消失，其实是很好的体验，省的按下的位置不能及时看不到。其实只需要设置一个标记位，当标记为为false时，不执行绘制十字的操作即可：`if (!mDrawLongPressPaint) return;`

		 private void hiddenLongPressView() {
		        //实现蚂蚁金服延迟消失十字线
		        postDelayed(new Runnable() {
		            @Override
		            public void run() {
		                mDrawLongPressPaint = false;
		                invalidate();
		            }
		        }, 1000);
		    }

#### 正在加载中文字处理
* 这里只需要在数据过来之前居中显示一个Text即可：`canvas.drawText(mLoadingText, mWidth / 2 - mLoadingPaint.measureText(mLoadingText) / 2, mHeight / 2, mLoadingPaint);`
* 那么如何在数据来之后取消这个显示呢？其实所谓取消，直接不执行这个绘制即可（因为重新刷新了界面）。其实和上面十字消失是一样的道理，加一个标记位。

#### code
* [`https://github.com/scsfwgy/FinancialCustomerView`](https://github.com/scsfwgy/FinancialCustomerView "https://github.com/scsfwgy/FinancialCustomerView")
* 注：该项目会一直维护，不断加入新的关于金融类的各种自定义View。最终的目标是绘制出复杂多变的K线图~

