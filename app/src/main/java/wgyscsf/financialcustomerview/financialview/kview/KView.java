package wgyscsf.financialcustomerview.financialview.kview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/02/26 19:04
 * 描 述 ：kview,包含主图、副图、手势操作以及其他辅助view。该View是对外提供使用的View。
 * ============================================================
 **/
public class KView extends ViewGroup {

    public KView(Context context) {
        super(context);
    }

    public KView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
