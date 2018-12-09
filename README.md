## FinancialCustomerView
* 本项目会对金融交易软件中存在的各种View进行模仿绘制
* 提供详细的实现思路
* 收集整理相关算法、文档以及专业资料
* 开源库不依赖第三方，继承系统View实现
* 提供完整Demo
*  [参考文档](https://github.com/Tophold/FinancialCustomerView/tree/master/%E8%B5%84%E6%96%99%E4%B8%8E%E6%96%87%E6%A1%A3)

#### 目录
*  ``com.tophold.trade.view.fund.FundView`` 仿蚂蚁金服基金走势图
2. ``com.tophold.trade.view.kview.KView`` k线走势图
3. ``com.tophold.trade.view.pie.PieChartView`` 饼图
4. ``com.tophold.trade.view.seekbar.DoubleThumbSeekBar``  双Thumb自定义进度条

#### com.tophold.trade.view.fund.FundView
* 仿蚂蚁金服基金走势图，支持长按查看指定数据，可以据此扩展出各种简单的资金走势图。KView的思路来自于此。

![](https://github.com/scsfwgy/FinancialCustomerView/raw/master/img/v1.1_img_nopress.png)

![](https://github.com/scsfwgy/FinancialCustomerView/raw/master/img/v1.1_img_press.png)

#### com.tophold.trade.view.kview.KView
* k线走势图，支持主图+副图+量图
* 主图支持缩放、长按十字、滑动等各种手势操作；支持显示最小、最大值；指标支持：ma、boll、ma+boll;图形类别支持分时图、蜡烛图
* 副图支持指标：macd、kdj、rsi
* 量图支持：量、ma(5,10)
*

![](https://raw.githubusercontent.com/Tophold/FinancialCustomerView/master/img/vol_huobi_eos.png)

![](https://raw.githubusercontent.com/Tophold/FinancialCustomerView/master/img/vol_huobi_eos_press.png)

#### com.tophold.trade.view.pie.PieChartView
* 饼图，加载动画、特定指示图

![](https://camo.githubusercontent.com/d345e80888d09007764b575932a7ede0ba368953/687474703a2f2f6f373175686f6b67662e626b742e636c6f7564646e2e636f6d2f31353331383135393530313831372e6a7067)

#### com.tophold.trade.view.seekbar.DoubleThumbSeekBar
* 双Thumb自定义进度条，可以高度定制化
* 支持单、双thumb
* 支持从左、右开始最小值（和滑动方向无关）
* 两个thumb可以任意设置开始方向以及起始位置
* 支持数据回调
* 具体使用参看Demo

![](https://raw.githubusercontent.com/Tophold/FinancialCustomerView/master/img/DoubleThumbSeekBar.png)

#### 其它
* [天厚投资](https://www.tophold.com/)
* [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)




