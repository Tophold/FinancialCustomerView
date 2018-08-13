package com.tophold.trade.view.kview;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 更新时间 ：2018/04/04 18:12
 * 描 述 ：
 * ============================================================
 */
public class KViewListener {
    /**
     * 主图的长按、加载更多回调
     */
    public interface MasterTouchListener {
        void onLongTouch(Quotes preQuotes, Quotes currentQuotes);

        void onUnLongTouch();

        void needLoadMore();
    }

    /**
     * 主图的监听，主要供副图使用，把这些数据回调给副图，避免副图再做复杂重复的操作。
     */
    public interface MinorListener {
        /**
         * 长按操作
         *
         * @param pressIndex 按下所对应的索引
         * @param currQuotes 按下所对应的点
         */
        void masterLongPressListener(int pressIndex, Quotes currQuotes);

        /**
         * 不再长按回调
         */
        void masterNoLongPressListener();

        /**
         * 缩放
         *
         * @param beginIndex    缩放后的起始位置索引
         * @param endIndex      缩放后的结束索引
         * @param shownMaxCount 可见数据总条数
         */
        void masteZoomlNewIndex(int beginIndex, int endIndex, int shownMaxCount);

        /**
         * 左右滑动
         *
         * @param endIndex      滑动后的结束索引
         * @param currPullType  当前PullType类型
         * @param shownMaxCount
         * @param shownMaxCount 可见数据总条数
         */
        void mastelPullmNewIndex(int beginIndex, int endIndex, KViewType.PullType currPullType, int shownMaxCount);

    }

    /**
     * 关键点的监听：可视范围内最大值点、可视范围内最小值点、最后一个点监听（数据集合的最后一个点）
     */
    public interface PostionListner {
        void postion(Quotes quotes, float x, float y);
    }
}
