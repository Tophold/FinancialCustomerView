package com.tophold.example.demo.pie

import android.os.Bundle
import android.support.annotation.ColorInt
import com.tophold.example.BaseActivity
import com.tophold.example.R
import com.tophold.trade.view.pie.PieEntrys
import kotlinx.android.synthetic.main.activity_pie_chart.*

class PieChartActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pie_chart)

        initPieChart()

        loadPieChart()
    }

    private fun loadPieChart() {
        val pieEntrysList: MutableList<PieEntrys> = ArrayList()
        pieEntrysList.add(PieEntrys(10f, "80%", getColorId(R.color.trade_analyze_pie_1), false, "澳元/美元"))
        pieEntrysList.add(PieEntrys(50f, "胜率 86.96%", getColorId(R.color.trade_analyze_pie_2), true, "英镑/美元"))
        pieEntrysList.add(PieEntrys(1f, "60.71%", getColorId(R.color.trade_analyze_pie_3), false, "黄金"))
        pieEntrysList.add(PieEntrys(1f, "76.47%", getColorId(R.color.trade_analyze_pie_4), false, "欧元/美元"))

        hatab_tapc_chart.pieEntryList = pieEntrysList
    }

    private fun initPieChart() {
        hatab_tapc_chart.basePaddingTop = 40f
        hatab_tapc_chart.basePaddingBottom = 40f
    }

    @ColorInt
    private fun getColorId(colorId: Int): Int {
        return resources.getColor(colorId)
    }
}
