package com.tophold.trade.view.kview;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 更新时间 ：2018/04/04 17:04
 * 描 述 ：
 * ============================================================
 */
public class KViewType {
    /**
     * 主图类型：分时图还是蜡烛图
     */
    public enum MasterViewType {
        TIMESHARING,
        CANDLE
    }

    /**
     * 拖拽类型
     */
    protected enum PullType {
        PULL_RIGHT,//向右滑动
        PULL_LEFT,//向左滑动
        PULL_RIGHT_STOP,//滑动到最右边
        PULL_LEFT_STOP,//滑动到最左边
    }

    /**
     * 主图上面的技术指标类型
     */
    public enum MasterIndicatrixType {
        NONE,//无
        MA,//MA5、10、20
        BOLL,//BOLL(26)
        MA_BOLL//MA5、10、20和BOLL(26)同时展示
    }


    /**
     * 主图上面的详细技术指标类型，主要用于判断何种具体的线，进行相应的处理
     */
    public enum MasterIndicatrixDetailType {
        MA5,
        MA10,
        MA20,
        BOLLUP,
        BOLLMB,
        BOLLDN
    }

    /**
     * 副图上面的技术指标类型
     */
    public enum MinorIndicatrixType {
        MACD,
        RSI,
        KDJ
    }

    /**
     * 滑动的类型
     */
    public enum MoveType {
        STEP,//一点一点移动
        ONFLING//具有onfling效果
    }

    /**
     * MA类型：主图：ma5,ma10,ma20;量图：ma5,ma10
     */
    public enum MaType {
        ma5,//主图ma5
        ma10,//主图ma10
        ma20,//主图ma20
        volMa5,//量图ma5
        volMa10//量图ma10
    }
}
