package wgyscsf.financialcustomerview;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import wgyscsf.financialcustomerview.financialview.FundActivity;

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
        Toast.makeText(mContext, "TODO:待完善", Toast.LENGTH_SHORT).show();
    }

    public String getVersionStr() {
        String version = "verisonName:";
        version += BuildConfig.VERSION_NAME;
        version += ",versionCode(git head):" + BuildConfig.VERSION_CODE;

        return version;
    }
}
