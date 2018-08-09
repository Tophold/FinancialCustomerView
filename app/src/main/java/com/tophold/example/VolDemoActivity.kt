package com.tophold.example

import android.os.Bundle
import android.support.annotation.NonNull
import android.util.Log
import com.tophold.trade.utils.StringUtils
import com.tophold.trade.view.kview.VolModel
import kotlinx.android.synthetic.main.activity_vol_demo.*

class VolDemoActivity : BaseActivity() {

    val mDataList: MutableList<VolModel> by lazy {
        ArrayList<VolModel>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vol_demo)
        loadData()
        avd_btn_click.setOnClickListener { test(StringUtils.getString())}
    }

    private fun loadData() {
        for (i in 0..99) {
            var volModel = VolModel(StringUtils.getRadomNum(0, 1) == 0,
                    StringUtils.getRadomNum(100, 1000).toDouble(),
                    StringUtils.getRadomNum(10, 50).toDouble(),
                    StringUtils.getRadomNum(10, 50).toDouble())
            mDataList.add(volModel)
        }

//        avd_vv_vol.setVolDataList(mDataList)
    }

    /**
     * @NonNull 只是辅助检查，并不能保证传递过来的一定不为空
     */
    fun test(@NonNull str: String) {
        Log.e(TAG, ": " + str.length);
    }
}
