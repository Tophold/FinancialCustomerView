package wgyscsf.financialcustomerview.timesharing;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import wgyscsf.financialcustomerview.BaseActivity;
import wgyscsf.financialcustomerview.R;
import wgyscsf.financialcustomerview.utils.GsonUtil;

public class TimeSharingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_sharing);
        loadData();
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
                List<OriginQuotes> OriginFundModeList;
                try {
                    OriginFundModeList = GsonUtil.fromJson2Object(originalFundData, new TypeToken<List<OriginQuotes>>() {
                    }.getType());
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                //开始适配图表数据
                List<Quotes> quotesList = adapterData(OriginFundModeList);
                if (quotesList != null) {
                    Log.e(TAG, "TimeSharingActivity:" + quotesList);
                } else {
                    Log.e(TAG, "run: 数据适配失败、、、、");
                }
            }
        }, 1000);
    }

    private List<Quotes> adapterData(List<OriginQuotes> originFundModeList) {
        List<Quotes> fundModeList = new ArrayList<>();//适配后的数据
        for (OriginQuotes OriginQuotes : originFundModeList) {
            Quotes Quotes = new Quotes(OriginQuotes.o, OriginQuotes.h, OriginQuotes.l,
                    OriginQuotes.c, OriginQuotes.t);
            fundModeList.add(Quotes);
        }
        return fundModeList;
    }
}
