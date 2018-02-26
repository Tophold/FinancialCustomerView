# 金融类自定义view([English?](https://github.com/scsfwgy/FinancialCustomerView/blob/master/README_en.md))

## tips
* 非常不建议在现在这个阶段fork项目，现在项目还不稳定，一直在开发。在这个阶段PR过来的代码一般我也不会接受。[GitHub 的 Fork 是什么意思？](https://www.zhihu.com/question/20431718)

## 介绍
> 本项目会对金融交易软件中存在的各种View进行模仿绘制，提供详细的实现思路，收集整理相关算法、文档以及专业资料。

## 存在的意义
> 可能有同学会说，有很成熟的第三方图形图啊，干嘛还去自己实现？

* 知道如何编写绘制，更方便的扩展第三库图形库。
* 了解实现原理，如果第三方实现不了，可以从容的自己撸一个。
* 金融交易中各种算法、各种指标很多，但是网上相关资料很少。希望可以给金融相关软件从业者提供一点点帮助~
* 知其然，知其所以然。

## 效果图与demo
* 效果图：[效果图](https://github.com/scsfwgy/FinancialCustomerView/blob/master/%E8%B5%84%E6%96%99%E4%B8%8E%E6%96%87%E6%A1%A3/%E6%95%88%E6%9E%9C%E5%9B%BE%E5%92%8C%E8%BF%9B%E5%BA%A6.md)
* demo:[demo](https://github.com/scsfwgy/FinancialCustomerView/tree/master/apk)

## 资料与博客
* 资料和博客：[资料和博客](https://github.com/scsfwgy/FinancialCustomerView/tree/master/%E8%B5%84%E6%96%99%E4%B8%8E%E6%96%87%E6%A1%A3)

## 进度
* 完成：基金收益图开发完毕，在master分支。
* 完成：分时图开发完毕，在master分支。
* 完成：蜡烛图（带MA、BOLL指标）开发完毕，在master分支。
* 完成：代码重构完毕，在master分支。后期会不断重构代码。
* 完成：副图部分，正在开发，在feature_minor分支。
* 正在开发：整合主图和副图，正在开发，在feature_minor分支。
* 计划：校验数据准确性（拿线上APP对比数据）。
* 计划：引入开源交易商KView API，在真实环境中测试可用性。
* 计划：模拟真实使用，分时图、5分、15分、30分、1h、日k、周k、月k切换。

## 贡献代码
* 直接建立新的分支，PR即可。

## 知名第三方图形库
* MPAndroidChart：[https://github.com/PhilJay/MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
* AndroidCharts：[https://github.com/HackPlan/AndroidCharts](https://github.com/HackPlan/AndroidCharts)
* Android-Charts：[https://github.com/limccn/Android-Charts](https://github.com/limccn/Android-Charts)


