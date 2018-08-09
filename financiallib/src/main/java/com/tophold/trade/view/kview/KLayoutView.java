package com.tophold.trade.view.kview;

import android.content.Context;
import android.support.annotation.NonNull;
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
    public static String TAG;
    public static final float DEF_MINORHRATIO = 0.25f;
    public static final float DEF_VOLHRATIO = 0.25f;
    protected MasterView mMasterView;
    protected MinorView mMinorView;
    protected VolView mVolView;
    //副图高度占全部高度比
    protected float mMinorHRatio = DEF_MINORHRATIO;
    //量图高度占全部高度比
    protected float mVolHRatio = DEF_VOLHRATIO;
    //是否展示副图
    protected boolean isShowMinor = true;
    //是否展示量图
    protected boolean isShowVol = true;

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
        mMinorView = new MinorView(getContext());
        // mMinorView.setBackgroundColor(getResources().getColor(R.color.color_fundView_xLineColor));

        mVolView = new VolView(getContext());

        //测量高度
        measureHeight();

        addView(mMasterView);
        addView(mMinorView);
        addView(mVolView);

        mMasterView.setMinorListener(mMinorView.getMinorListener());
        mMasterView.setVolListener(mVolView.getVolListener());
    }

    private void measureHeight() {
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, 0);
        params.weight = 1 - mMinorHRatio - mVolHRatio;
        mMasterView.setLayoutParams(params);


        LayoutParams params2 = new LayoutParams(
                LayoutParams.MATCH_PARENT, 0);
        params2.weight = mMinorHRatio;
        mMinorView.setLayoutParams(params2);

        LayoutParams params3 = new LayoutParams(
                LayoutParams.MATCH_PARENT, 0);
        params3.weight = mVolHRatio;
        mVolView.setLayoutParams(params3);
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

    public VolView getVolView() {
        return mVolView;
    }

    public KLayoutView setVolView(VolView volView) {
        mVolView = volView;
        return this;
    }

    public float getVolHRatio() {
        return mVolHRatio;
    }

    public KLayoutView setVolHRatio(float volHRatio) {
        mVolHRatio = volHRatio;
        return this;
    }

    public boolean isShowVol() {
        return isShowVol;
    }

    public KLayoutView setShowVol(boolean showVol) {
        isShowVol = showVol;
        if (!isShowVol) {
            mVolHRatio = 0;
        } else {
            mMinorHRatio = DEF_VOLHRATIO;
        }
        mVolView.setShowVol(isShowVol);
        return reloadHeight();


    }

    @NonNull
    private KLayoutView reloadHeight() {
        //为什么要这样刷新？先重新测量主图和副图的高度，然后再去测量各自的seekAndCalculateCellData
        measureHeight();

        postDelayed(() -> {
            mMasterView.seekAndCalculateCellData();
            mMinorView.seekAndCalculateCellData();
            mVolView.seekAndCalculateCellData();
        }, 300);
        return this;
    }

    /**
     * 这个问题比较奇葩。主要原因是：整个View重新布局了大小，等于重新走了生命周期，导致各种问题。
     * 之前一般都是只是走onDraw()方法，所以基本不会有什么问题。
     * 现在是高度变了，因此必须重新测量。
     * 现在下面这种实现方式仍然会有视觉上的延迟，体验不太好，暂时没有好评的解决方案。
     * 另外：平时基本不会这样直接的主图副图显示不显示来回切换。如果在初始化的时候就设置好，不会有任何问题。
     *
     * @param showMinor
     */
    public KLayoutView setShowMinor(boolean showMinor) {
        isShowMinor = showMinor;
        if (!isShowMinor) {
            mMinorHRatio = 0;
        } else {
            mMinorHRatio = DEF_MINORHRATIO;
        }
        return reloadHeight();
    }
}
