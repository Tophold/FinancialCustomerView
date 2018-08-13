## FinancialCustomerView
* 本项目会对金融交易软件中存在的各种View进行模仿绘制
* 提供详细的实现思路
* 收集整理相关算法、文档以及专业资料
* 开源实现的金融自定View，已经核对过指标算法数据
* 开源库已成功接入外汇、数字货币KView
* 开源库不依赖第三方，继承系统View实现

#### 实现思路及资料
* https://github.com/scsfwgy/FinancialCustomerView/tree/master/%E8%B5%84%E6%96%99%E4%B8%8E%E6%96%87%E6%A1%A3

#### 算法以及Java版实现
* https://github.com/scsfwgy/FinancialCustomerView/tree/master/%E8%B5%84%E6%96%99%E4%B8%8E%E6%96%87%E6%A1%A3
* Java版实现：FinancialAlgorithm.java

#### Usage

1. Add the following to your project level build.gradle:

        allprojects {
        	repositories {
        		jcenter()
        	}
        }
        
2. Add this to your app build.gradle:

        implementation 'com.wgyscsf:financialLib:0.0.2'

3. 在布局文件中引入需要使用的自定义View(`com.tophold.trade.view.fund.FundView`、`com.tophold.trade.view.kview.KView`)
4. 个性化设置，like this

          /**
                 * 定制,所有的画笔以及其它属性都已经暴露出来，有了更加大的定时灵活性。更多参数可以直接查看源码...
                 */
                //常规set、get...
                mFundView.getBrokenPaint().setColor(getResources().getColor(R.color.colorAccent));//设置折现颜色
                mFundView.getInnerXPaint().setStrokeWidth(1);//设置内部x轴虚线的宽度,px
                mFundView.getBrokenPaint().setStrokeWidth(1);
                //链式调用
                mFundView
                        .setBasePaddingTop(140)
                        .setBasePaddingLeft(50)
                        .setBasePaddingRight(40)
                        .setBasePaddingBottom(30)
                        .setLoadingText("正在加载，马上就来...");


5. 适配数据:需要将原始数据转换为本库需要的数据格式
6. 加载数据进行显示


#### Demo
>  已成功模拟基金走势图、模拟K线走势、接入火币数字货币API（科学上网）、接入外汇API

* https://github.com/scsfwgy/FinancialCustomerView/tree/feature_testlib

#### 效果图

###### 基金收益折线图`com.tophold.trade.view.fund.FundView`

![](https://github.com/scsfwgy/FinancialCustomerView/blob/master/img/v1.1_img_nopress.png?raw=true)

![](https://github.com/scsfwgy/FinancialCustomerView/blob/master/img/v1.1_img_press.png?raw=true)

###### KView`com.tophold.trade.view.kview.KView`

![2-w180](http://o71uhokgf.bkt.clouddn.com/1.png?imageMogr2/thumbnail/!25p)
![2-w180](http://o71uhokgf.bkt.clouddn.com/2.png?imageMogr2/thumbnail/!25p)
![2-w180](http://o71uhokgf.bkt.clouddn.com/3.png?imageMogr2/thumbnail/!25p)
![2-w180](http://o71uhokgf.bkt.clouddn.com/4.png?imageMogr2/thumbnail/!25p)

![2-w180](http://o71uhokgf.bkt.clouddn.com/5.png?imageMogr2/thumbnail/!25p)
![2-w180](http://o71uhokgf.bkt.clouddn.com/6.png?imageMogr2/thumbnail/!25p)
![2-w180](http://o71uhokgf.bkt.clouddn.com/7.png?imageMogr2/thumbnail/!25p)
![2-w180](http://o71uhokgf.bkt.clouddn.com/8.png?imageMogr2/thumbnail/!25p)

![2-w180](http://o71uhokgf.bkt.clouddn.com/15338110152858.jpg)
![2-w180](http://o71uhokgf.bkt.clouddn.com/15341533160499.jpg)



外汇类Demo gif:[http://o71uhokgf.bkt.clouddn.com/forex.gif](http://o71uhokgf.bkt.clouddn.com/forex.gif)

数字货币类Demo gif:[http://o71uhokgf.bkt.clouddn.com/huobi.gif](http://o71uhokgf.bkt.clouddn.com/huobi.gif)

###### PieChartView`com.tophold.trade.view.pie.PieChartView`

![](http://o71uhokgf.bkt.clouddn.com/15318159501817.jpg)

#### 计划
1. onFling滑动效果
2. view上箭头标记最大值最小值（✅）
3. 入场动画
4. 成交量（✅）

