package wgyscsf.financiallib.view;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.View;

import static android.view.View.MeasureSpec.AT_MOST;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 更细日期 ：2018/01/14 12:03
 * 描 述 ：整个自定义View共有的数据特征全部在这里处理。
 * ============================================================
 **/
public class BaseView extends View {

    protected String TAG;
    protected Context mContext;

    //长按阀值，默认多长时间算长按（ms）
    protected static final long DEF_LONGPRESS_LENGTH = 700;
    //单击阀值
    protected static final long DEF_CLICKPRESS_LENGTH = 100;
    //移动阀值。手指移动多远算移动的阀值（单位：sp）
    protected static final long DEF_PULL_LENGTH = 5;

    //控件默认宽高。当控件的宽高设置为wrap_content时会采用该参数进行默认的设置（单位：sp）。子类可以修改。
    protected float DEF_WIDTH = 650;
    protected float DEF_HIGHT = 400;

    //测量的控件宽高，会在onMeasure中进行测量。
    protected int mWidth;
    protected int mHeight;


    public BaseView(Context context) {
        this(context, null);
    }

    public BaseView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TAG = this.getClass().getSimpleName();
        mContext = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == AT_MOST && heightSpecMode == AT_MOST) {
            setMeasuredDimension((int) DEF_WIDTH, (int) DEF_HIGHT);
        } else if (widthSpecMode == AT_MOST) {
            setMeasuredDimension((int) DEF_WIDTH, heightSpecSize);
        } else if (heightSpecMode == AT_MOST) {
            setMeasuredDimension(widthSpecSize, (int) DEF_HIGHT);
        } else {
            setMeasuredDimension(widthSpecSize, heightSpecSize);
        }
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    protected int getColor(@ColorRes int colorId) {
        return getResources().getColor(colorId);
    }

    protected String getString(@StringRes int stringId) {
        return getResources().getString(stringId);
    }

    /**
     * 测量指定画笔的文字的高度
     * @param fontSize
     * @param paint
     * @return
     */
    protected float getFontHeight(float fontSize, Paint paint) {
        paint.setTextSize(fontSize);
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (float) (Math.ceil(fm.descent - fm.top) + 2f);
    }

    public void setFWidth(int width) {
        mWidth = width;
    }

    public void setFHeight(int height) {
        mHeight = height;
    }
}
