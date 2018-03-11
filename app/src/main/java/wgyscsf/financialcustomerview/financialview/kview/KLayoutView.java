package wgyscsf.financialcustomerview.financialview.kview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/03 15:38
 * 描 述 ：KView,包含主图和副图，包括手势、加载数据等
 * ============================================================
 **/
public class KLayoutView extends LinearLayout {
    protected static  String TAG ;
    protected MasterView mMasterView;
    protected MinorView mMinorView;
    //副图高度占全部高度比
    float mMinorHRatio = 0.25f;

    //是否展示副图
    boolean isShowMinor = true;

    public KLayoutView(Context context) {
        this(context, null);
    }

    public KLayoutView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KLayoutView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TAG=this.getClass().getSimpleName();
        layoutViews();
        initDefAttrs();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

    }



    private void initDefAttrs() {
        setShowMinor(true);
    }

    private void layoutViews() {
        setOrientation(VERTICAL);

        mMasterView = new MasterView(getContext());
       // mMasterView.setBackgroundColor(getResources().getColor(R.color.color_fundView_brokenLineColor));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, 0);
        params.weight = 1 - mMinorHRatio;
        mMasterView.setLayoutParams(params);
        addView(mMasterView);

        mMinorView = new MinorView(getContext());
       // mMinorView.setBackgroundColor(getResources().getColor(R.color.color_fundView_xLineColor));
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, 0);
        params2.weight = mMinorHRatio;
        mMinorView.setLayoutParams(params2);
        addView(mMinorView);
    }

    public float getMinorHRatio() {
        return mMinorHRatio;
    }

    public void setMinorHRatio(float minorHRatio) {
        mMinorHRatio = minorHRatio;
    }

    public boolean isShowMinor() {
        return isShowMinor;
    }

    public void setShowMinor(boolean showMinor) {
        isShowMinor = showMinor;
        if (!isShowMinor) {
            mMinorHRatio = 0;
            mMinorView.setVisibility(GONE);
        } else {
            mMinorView.setVisibility(VISIBLE);
        }
    }

    /**
     * 数据设置入口
     *
     * @param quotesList
     */
    public void setTimeSharingData(List<Quotes> quotesList, KBaseView.TimeSharingListener timeSharingListener) {
        if (quotesList == null || quotesList.isEmpty()) {
            Toast.makeText(getContext(), "数据异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "setTimeSharingData: 数据异常");
            return;
        }
        mMasterView.setTimeSharingData(quotesList,timeSharingListener);
        mMinorView.setTimeSharingData(quotesList,timeSharingListener);
    }

    /**
     * 实时推送过来的数据，实时更新
     *
     * @param quotes
     */
    public void pushingTimeSharingData(Quotes quotes) {
        if (quotes == null) {
            Toast.makeText(getContext(), "数据异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "setTimeSharingData: 数据异常");
            return;
        }
        mMasterView.pushingTimeSharingData(quotes);
        mMinorView.pushingTimeSharingData(quotes);
    }

    /**
     * 加载更多数据
     *
     * @param quotesList
     */
    public void loadMoreTimeSharingData(List<Quotes> quotesList) {
        if (quotesList == null || quotesList.isEmpty()) {
            Toast.makeText(getContext(), "数据异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "setTimeSharingData: 数据异常");
            return;
        }
        mMasterView.loadMoreTimeSharingData(quotesList);
        mMinorView.loadMoreTimeSharingData(quotesList);
    }

}
