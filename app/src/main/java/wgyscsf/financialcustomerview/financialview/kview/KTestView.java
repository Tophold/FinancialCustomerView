package wgyscsf.financialcustomerview.financialview.kview;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import wgyscsf.financialcustomerview.R;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/02/26 19:04
 * 描 述 ：kview,包含主图、副图、手势操作以及其他辅助view。该View是对外提供使用的View。
 * ============================================================
 **/
public class KTestView extends ViewGroup {
    public static final String TAG= KTestView.class.getSimpleName();

    //tips
    List<TextView> mTipsTxtList;
    MasterView mMasterView;
    MinorView mMinorView;


    int mBuyColor;
    int mSellColor;


    int mTipsTxtHeight = 60;

    public KTestView(Context context) {
        this(context, null);
    }

    public KTestView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KTestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initDefAttrs();
        addAailableView();
    }

    private void initDefAttrs() {
        mBuyColor=getColor(R.color.color_timeSharing_candleRed);
        mSellColor=getColor(R.color.color_timeSharing_candleGreen);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //第一步，测量子控件

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        //第二步，测量当前控件。所有的任务就是确认maxWidth、maxHeight的值。
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        setMeasuredDimension(resolveSizeAndState(measuredWidth, widthMeasureSpec, 0),
                resolveSizeAndState(measuredHeight, heightMeasureSpec, 0));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        /**
         *
         */
        int effectiveWidth = getMeasuredWidth()-getPaddingRight()-getPaddingLeft();
        int effectiveHeight = getMeasuredHeight()-getPaddingBottom()-getPaddingTop();
        //容器的四个顶点
        int left = 0, top = 0, right = 0, bottom = 0;
        left = getPaddingLeft();
        top = getPaddingTop();
        right = r - l - getPaddingRight();
        bottom = effectiveHeight + top;
        //定位所需要的四个点
        int perTipsTxtWidth = effectiveWidth / 3;
        int childLeft = left, childTop = top, childRight =childLeft+ perTipsTxtWidth, childBottom =childTop+ mTipsTxtHeight;
        //已经测量的高度
        int usedHeight=0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (i < 3) {
                //第一行
                child.layout(childLeft, childTop, childRight, childBottom);
                childLeft += perTipsTxtWidth;
                childRight += perTipsTxtWidth;
                //最后一个
                if(i==2){
                    usedHeight+=mTipsTxtHeight;

                    //第二行
                    childTop += mTipsTxtHeight;
                    childBottom += mTipsTxtHeight;
                    childLeft = getPaddingLeft();
                    childRight = childLeft+perTipsTxtWidth;
                }
            } else if (i < 6) {
                //第二行
                child.layout(childLeft, childTop, childRight, childBottom);
                childLeft += perTipsTxtWidth;
                childRight += perTipsTxtWidth;
                //最后一个
                if(i==5){
                    usedHeight+=mTipsTxtHeight;

                    //第三行
                    int height=(effectiveHeight-usedHeight)/4*3;
                    childTop += mTipsTxtHeight;
                    childBottom += height;
                    childLeft = getPaddingLeft();
                    childRight = r - l - getPaddingRight();
                }
            } else if (i < 7) {
                //主图,主图的高度取可用高度的3/4,副图去1/4
                mMasterView.setFWidth(childRight-childLeft);
                mMasterView.setFHeight(childBottom-childTop);
                child.layout(childLeft, childTop, childRight, childBottom);

                if(i==6){
                    //副图
                    int height=(effectiveHeight-usedHeight)/4;
                    childTop += height*3;
                    childBottom += height;
                    childLeft = getPaddingLeft();
                    childRight = r - l - getPaddingRight();
                }
            } else if (i < 8) {
                //副图
                mMinorView.setFWidth(childRight-childLeft);
                mMinorView.setFHeight(childBottom-childTop);
                child.layout(childLeft, childTop, childRight, childBottom);
            }
        }
    }

    private  void addAailableView(){
        mTipsTxtList=new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            TextView textView = new TextView(getContext());
            textView.setTextSize(10);
            textView.setTextColor(mBuyColor);
            textView.setText("--");
            textView.setBackgroundColor(i%2==0
                    ?getResources().getColor(R.color.color_kview_outerStrokeColor)
                    :getResources().getColor(R.color.color_fundView_pressIncomeTxtBg));
            textView.setGravity(Gravity.CENTER);
            addView(textView);
            mTipsTxtList.add(textView);
        }

        mMasterView=new MasterView(getContext());
        addView(mMasterView);

        mMinorView=new MinorView(getContext());
        addView(mMinorView);

    }
    protected int getColor(@ColorRes int colorId) {
        return getResources().getColor(colorId);
    }

    public MasterView getMasterView() {
        return mMasterView;
    }

    public MinorView getMinorView() {
        return mMinorView;
    }
    /**
     * 数据设置入口
     *
     * @param quotesList
     */
    public void setTimeSharingData(List<Quotes> quotesList) {
        if (quotesList == null || quotesList.isEmpty()) {
            Toast.makeText(getContext(), "数据异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "setTimeSharingData: 数据异常");
            return;
        }
        mMasterView.setTimeSharingData(quotesList);
        mMinorView.setTimeSharingData(quotesList);
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
