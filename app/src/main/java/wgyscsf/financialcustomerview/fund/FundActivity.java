package wgyscsf.financialcustomerview.fund;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import wgyscsf.financialcustomerview.BaseActivity;
import wgyscsf.financialcustomerview.R;
import wgyscsf.financialcustomerview.utils.GsonUtil;

public class FundActivity extends BaseActivity {
    private FundView mAfFvFundview;
    List<OriginFundMode> mOriginFundModeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fund);
        initView();
        initData();
        loadData();

    }

    private void initView() {
        mAfFvFundview = (FundView) findViewById(R.id.af_fv_fundview);

    }

    private void initData() {
        mOriginFundModeList = new ArrayList<>();

    }

    private void loadData() {
        //模拟网络环境
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String originalFundData = SimulateNetAPI.getOriginalFundData(mContext);
                if (originalFundData == null) {
                    Log.e(TAG, "loadData: 从网络获取到的数据为空");
                    return;
                }
                OriginFundMode[] originFunModes;
                try {
                    originFunModes = GsonUtil.fromJson2Object(originalFundData, OriginFundMode[].class);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                List<OriginFundMode> OriginFundModeList = Arrays.asList(originFunModes);
                //开始适配图表数据
                List<FundMode> funModeList = adapterData(OriginFundModeList);
                if (funModeList != null) {
                    mAfFvFundview.setDataList(funModeList);
                } else {
                    Log.e(TAG, "run: 数据适配失败、、、、");
                }
            }
        }, 1000);


    }

    private List<FundMode> adapterData(List<OriginFundMode> originFundModeList) {
        List<FundMode> fundModeList = new ArrayList<>();//适配后的数据
        for (OriginFundMode originFundMode : originFundModeList) {
            FundMode fundMode = new FundMode(originFundMode.timestamp*1000, originFundMode.actual);
            fundModeList.add(fundMode);
            Log.e(TAG, "adapterData: 适配之前："+originFundMode.actual+"----->>"+fundMode.dataY );
        }
        return fundModeList;
    }
}
