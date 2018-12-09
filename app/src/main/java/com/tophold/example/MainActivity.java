package com.tophold.example;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tophold.example.base.BaseActivity;
import com.tophold.example.demo.fund.FundActivity;
import com.tophold.example.demo.kview.KViewActivity;
import com.tophold.example.demo.kview.beginner.ui.KViewHorizontalActivityActivity;
import com.tophold.example.demo.kview.beginner.ui.KViewVerticalActivity;
import com.tophold.example.demo.kview.btc.ui.HuobiListActivity;
import com.tophold.example.demo.kview.forex.ui.ForexListActivity;
import com.tophold.example.demo.pie.PieChartActivity;
import com.tophold.example.demo.seekbar.DoubleThumbSeekBarActivity;

public class MainActivity extends BaseActivity {
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.version);
        mTextView.setText(getVersionStr());
    }

    public String getVersionStr() {
        String version = "verisonName:";
        version += BuildConfig.VERSION_NAME;
        version += ",versionCode(git head):" + BuildConfig.VERSION_CODE;

        return version;
    }
    public void fundView(View view) {
        go(FundActivity.class);
    }

    public void kViewDemo(View view) {
        go(KViewActivity.class);
    }
    public void onPieTest(View view) {
        go(PieChartActivity.class);
    }

    public void onSeekBarTest(View view) {
        go(DoubleThumbSeekBarActivity.class);
    }


}
