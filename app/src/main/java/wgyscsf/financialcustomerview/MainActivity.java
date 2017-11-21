package wgyscsf.financialcustomerview;

import android.os.Bundle;
import android.view.View;

import wgyscsf.financialcustomerview.fund.FundActivity;
import wgyscsf.financialcustomerview.timesharing.TimeSharingActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goFundView(View view) {
        go(FundActivity.class);

    }

    public void goTimeShring(View view) {
        go(TimeSharingActivity.class);
    }
}
