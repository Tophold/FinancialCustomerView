package com.tophold.example;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tophold.example.demo.beginner.ui.FundActivity;
import com.tophold.example.demo.beginner.ui.KViewHorizontalActivityActivity;
import com.tophold.example.demo.beginner.ui.KViewVerticalActivity;
import com.tophold.example.demo.btc.ui.HuobiActivity;
import com.tophold.example.demo.btc.ui.HuobiListActivity;
import com.tophold.example.demo.forex.ui.ForexListActivity;
import com.tophold.example.demo.pie.PieChartActivity;

public class MainActivity extends BaseActivity {
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.version);
        mTextView.setText(getVersionStr());
    }

    public void fundView(View view) {
        go(FundActivity.class);
    }

    public void kViewVertical(View view) {
        go(KViewVerticalActivity.class);
    }

    public void kViewHorizontal(View view) {
        go(KViewHorizontalActivityActivity.class);
    }

    public void kViewEvaluation(View view) {
        go(ForexListActivity.class);
    }

    public String getVersionStr() {
        String version = "verisonName:";
        version += BuildConfig.VERSION_NAME;
        version += ",versionCode(git head):" + BuildConfig.VERSION_CODE;

        return version;
    }

    public void btnDemo(View view) {
        go(HuobiListActivity.class);
    }


    public void onPieTest(View view) {
        go(PieChartActivity.class);
    }
}
