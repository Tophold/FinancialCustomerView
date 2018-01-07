package wgyscsf.financialcustomerview;

import android.os.Bundle;
import android.view.View;

import wgyscsf.financialcustomerview.financialview.fund.FundActivity;
import wgyscsf.financialcustomerview.financialview.kview.minor.MinorActivity;
import wgyscsf.financialcustomerview.financialview.kview.master.MasterViewActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goFundView(View view) {
        go(FundActivity.class);

    }

    public void goMasterView(View view) {
        go(MasterViewActivity.class);
    }

    public void goMinor(View view) {
        go(MinorActivity.class);
    }
}
