package wgyscsf.financiallib.view.kview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/03 15:38
 * 描 述 ：KView,包含主图和副图，包括手势、加载数据等
 * ============================================================
 **/
public class KLayoutView extends LinearLayout {
    protected static String TAG;
    protected MasterView mMasterView;
    protected MinorView mMinorView;
    //副图高度占全部高度比
    protected float mMinorHRatio = 0.25f;
    //是否展示副图
    protected boolean isShowMinor = true;

    public KLayoutView(Context context) {
        this(context, null);
    }

    public KLayoutView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KLayoutView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TAG = this.getClass().getSimpleName();
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
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, 0);
        params.weight = 1 - mMinorHRatio;
        mMasterView.setLayoutParams(params);
        addView(mMasterView);

        mMinorView = new MinorView(getContext());
        // mMinorView.setBackgroundColor(getResources().getColor(R.color.color_fundView_xLineColor));
        LayoutParams params2 = new LayoutParams(
                LayoutParams.MATCH_PARENT, 0);
        params2.weight = mMinorHRatio;
        mMinorView.setLayoutParams(params2);
        addView(mMinorView);

        mMasterView.setMasterListener(mMinorView.getMasterListener());
    }


    //-----------------------对开发者暴露可以修改的参数-------
    public MasterView getMasterView() {
        return mMasterView;
    }

    public KLayoutView setMasterView(MasterView masterView) {
        mMasterView = masterView;
        return this;
    }

    public MinorView getMinorView() {
        return mMinorView;
    }

    public KLayoutView setMinorView(MinorView minorView) {
        mMinorView = minorView;
        return this;
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
}
