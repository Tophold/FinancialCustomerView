package wgyscsf.financialcustomerview;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import wgyscsf.financialcustomerview.financialview.fund.FundActivity;
import wgyscsf.financialcustomerview.financialview.kview.minor.MinorActivity;

public class MainActivity extends BaseActivity {
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.version);
        mTextView.setText(getVersionStr());
    }

    public void goFundView(View view) {
        go(FundActivity.class);

    }


    public void goMinor(View view) {
         go(MinorActivity.class);
        //Test.test();
    }

    public void goTimeShringView(View view) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(MasterViewActivity.KEY_INTENT, true);
        go(MasterViewActivity.class, bundle);
    }

    public void goCandleView(View view) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(MasterViewActivity.KEY_INTENT, false);
        go(MasterViewActivity.class, bundle);
    }

    public String getVersionStr() {
        String version = "verisonName:";
        version += BuildConfig.VERSION_NAME;
        version += ",versionCode(git head):" + BuildConfig.VERSION_CODE;

        return version;
    }
}
