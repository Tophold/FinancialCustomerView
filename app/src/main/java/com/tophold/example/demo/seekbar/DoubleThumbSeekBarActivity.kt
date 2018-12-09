package com.tophold.example.demo.seekbar

import android.os.Bundle
import android.support.annotation.NonNull
import com.tophold.example.base.BaseActivity
import com.tophold.example.R
import kotlinx.android.synthetic.main.activity_double_thumb_seek_bar.*
import com.tophold.trade.utils.RenderUtils.dp2px
import com.tophold.trade.view.seekbar.DoubleTumb


class DoubleThumbSeekBarActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_double_thumb_seek_bar)
        renderView1()
    }

    private fun renderView1() {
        /**
         * def
         */
        am_dtsv_seek0.init(loadTpSeekBar())

        /**
         * 加载单个thumb,从左边开始且最小值从最左边开始
         */
        am_dtsv_seek1.init(loadTpSeekBar(
                false, -44.0, 66.0, true, 4,
                true, false, 0.2, greenA = false))
        /**
         * 单个thumb,从右边开始且从左边开始最小值
         */
        am_dtsv_seek2.init(loadTpSeekBar(
                false, -44.0, 66.0, true, 4,
                false, true, 0.2, greenA = true))
        /**
         * 双thumb同向,最小值从左边开始
         */
        am_dtsv_seek3.init(loadTpSeekBar(
                true, -44.0, 66.0, true, 4,
                true, true, 0.2, false,
                true, true, 0.33, false))
        /**
         * 双thumb同向,最小值从右边开始
         */
        am_dtsv_seek4.init(loadTpSeekBar(
                true, -44.0, 66.0, false, 4,
                false, true, 0.2, true,
                false, true, 0.33, true))

        /**
         * 交叉方式显示
         */
        am_dtsv_seek5.init(loadTpSeekBar(
                true, -44.0, 66.0, false, 4,
                true, true, 0.2, false,
                false, true, 0.33, true))
    }

    /**
     * @param doubleThumb 是否启动双thumb
     * @param min         最小值
     * @param max         最大值
     * @param minFromLeft 最小值是否从左边开始（注意和从哪边开始滑动无关）
     * @param digit       格式化的小数位数
     *
     * @param fromLeftA   是否从左边开始滑动
     * @param showTipsA   是否实时展示当前值
     * @param progressA   默认进度（0~1）
     * @param greenA      thumbA的背景色
     *
     * @param fromLeftB   是否从左边开始滑动
     * @param showTipsB   是否实时展示当前值
     * @param progressB   默认进度（0~1）
     * @param greenB      thumbA的背景色
     *
     */
    @NonNull
    fun loadTpSeekBar(doubleThumb: Boolean = false, min: Double = 0.0, max: Double = 100.0, minFromLeft: Boolean = true, digit: Int = 2,
                      fromLeftA: Boolean = true, showTipsA: Boolean = true, progressA: Double = 0.0, greenA: Boolean = false,
                      fromLeftB: Boolean = true, showTipsB: Boolean = true, progressB: Double = 0.0, greenB: Boolean = false): DoubleTumb {

        val thumbAForeground = if (!greenA) {
            getCompatColor(R.color.dtsb_thumb1Color)
        } else {
            getCompatColor(R.color.dtsb_thumb2Color)

        }

        val thumbADrawable = if (!greenA) {
            getCompatDrawable(R.drawable.layer_seekbar_thumb_red)
        } else {
            getCompatDrawable(R.drawable.layer_seekbar_thumb_green)
        }

        val thumbA = DoubleTumb.Thumb(thumbAForeground)
        thumbA.progress = progressA//默认位置，百分比值（0~1）
        thumbA.mFromLeft = fromLeftA//是否从左边开始滑动
        thumbA.mShowTips = showTipsA
        thumbA.setmThumb(thumbADrawable)
        thumbA.setmThumbWidth(dp2px(mContext, 20f))
        thumbA.setmThumbHight(dp2px(mContext, 25f))

        var thumbB: DoubleTumb.Thumb? = null
        //复杂模式显示两个thumb
        if (doubleThumb) {
            val thumbBForeground = if (!greenB) {
                getCompatColor(R.color.dtsb_thumb11Color)
            } else {
                getCompatColor(R.color.dtsb_thumb22Color)

            }
            val thumbBDrawable = if (!greenB) {
                getCompatDrawable(R.drawable.layer_seekbar_thumb_red)
            } else {
                getCompatDrawable(R.drawable.layer_seekbar_thumb_green)
            }
            thumbB = DoubleTumb.Thumb(thumbBForeground)
            thumbB.progress = progressB
            thumbB.mFromLeft = fromLeftB
            thumbB.mShowTips = showTipsB
            thumbB.setmThumb(thumbBDrawable)
            thumbB.setmThumbWidth(dp2px(mContext, 20f))
            thumbB.setmThumbHight(dp2px(mContext, 25f))
        }

        val doubleTumb = DoubleTumb(min, max,
                dp2px(mContext, 4f).toFloat(),
                getCompatColor(R.color.dtsb_bgColor),
                thumbA,
                thumbB)
        //注意这里！！！
        doubleTumb.mMinLeft = minFromLeft
        doubleTumb.mDigit = digit
        //根据总高度取设置
        doubleTumb.mLineTop = dp2px(mContext, 40f)
        doubleTumb.mLineBottom = dp2px(mContext, 10f)
        doubleTumb.mLineLeft = dp2px(mContext, 8f)
        doubleTumb.mLineRight = dp2px(mContext, 8f)

        return doubleTumb
    }
}
