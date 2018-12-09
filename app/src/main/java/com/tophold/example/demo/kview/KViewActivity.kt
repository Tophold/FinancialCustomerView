package com.tophold.example.demo.kview

import android.os.Bundle
import android.view.View
import com.tophold.example.BuildConfig
import com.tophold.example.R
import com.tophold.example.base.BaseActivity
import com.tophold.example.demo.kview.beginner.ui.KViewHorizontalActivityActivity
import com.tophold.example.demo.kview.beginner.ui.KViewVerticalActivity
import com.tophold.example.demo.kview.btc.ui.HuobiListActivity
import com.tophold.example.demo.kview.forex.ui.ForexListActivity

class KViewActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kview)
    }

    fun kViewVertical(view: View) {
        go(KViewVerticalActivity::class.java)
    }

    fun kViewHorizontal(view: View) {
        go(KViewHorizontalActivityActivity::class.java)
    }

    fun kViewEvaluation(view: View) {
        go(ForexListActivity::class.java)
    }

    fun btnDemo(view: View) {
        go(HuobiListActivity::class.java)
    }
}
