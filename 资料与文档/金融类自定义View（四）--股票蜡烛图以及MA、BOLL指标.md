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
#### 命名的声明
* 主流的交易曲线图会包含两个大部分，上面一大块是走势图，包含分时图、5分图、15分图、日k、周k、月k等；下面一小块是指标，包含成交量、MACD、KDJ、RSI等。如下图，是同花顺的交易曲线图，基本也是这样构成的：

## 代码重构
#### 长按回调

#### code
* [`https://github.com/scsfwgy/FinancialCustomerView`](https://github.com/scsfwgy/FinancialCustomerView "https://github.com/scsfwgy/FinancialCustomerView")
* 注：该项目会一直维护
	* 绘制各种金融类的自定义View。
	* 提供金融类自定义View的实现思路。
	* 收集整理相关算法、文档以及专业资料。
* 另，蜡烛图（包括主图指标）大部分功能已经绘制出来啦，代码也进行了大量的重构。在分支：feature_candleview	
	
	
	
	
	
	
	
	
	
	





