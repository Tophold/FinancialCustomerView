package wgyscsf.financialcustomerview;

import android.os.Bundle;
import android.view.View;

import wgyscsf.financialcustomerview.financialview.fund.FundActivity;
import wgyscsf.financialcustomerview.financialview.kview.master.MasterView;
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


    public void goMinor(View view) {
        go(MinorActivity.class);
    }

    public void goTimeShringView(View view) {
        Bundle bundle=new Bundle();
        bundle.putBoolean(MasterViewActivity.KEY_INTENT,true);
        go(MasterViewActivity.class,bundle);
    }

    public void goCandleView(View view) {
        Bundle bundle=new Bundle();
        bundle.putBoolean(MasterViewActivity.KEY_INTENT,false);
        go(MasterViewActivity.class,bundle);
    }
}
