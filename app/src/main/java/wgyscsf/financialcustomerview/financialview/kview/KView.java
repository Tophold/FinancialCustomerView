package wgyscsf.financialcustomerview.financialview.kview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/04 11:06
 * 描 述 ：
 * ============================================================
 **/
public class KView extends KLayoutView {
    //长按阀值，默认多长时间算长按（ms）
    protected static final long DEF_LONGPRESS_LENGTH = 700;
    //单击阀值
    protected static final long DEF_CLICKPRESS_LENGTH = 100;
    //移动阀值。手指移动多远算移动的阀值（单位：sp）
    protected static final long DEF_PULL_LENGTH = 5;

    //手指按下的个数
    protected int mFingerPressedCount;

    //按下的x轴坐标
    protected float mPressedX;
    //按下的时刻
    protected long mPressTime;
    //滑动点的x轴坐标，滑动使用，记录当前滑动点的x坐标
    float mMovingX;
    //是否绘制长按十字，逻辑判断使用，不可更改
    boolean mDrawLongPress = false;



    KViewInnerListener mKViewInnerListener;

    public KView(Context context) {
        this(context,null);
    }

    public KView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public KView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initListener();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //按下的手指个数
        mFingerPressedCount = event.getPointerCount();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPressedX = event.getX();
                mPressTime = event.getDownTime();
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getEventTime() - mPressTime > DEF_LONGPRESS_LENGTH) {
                    mMovingX = event.getX();
                    mKViewInnerListener.showLongPressView();
                }
                //判断是否是手指移动
                float currentPressedX = event.getX();
                float moveLen = currentPressedX - mPressedX;
                //重置当前按下的位置
                mPressedX = currentPressedX;
                if (Math.abs(moveLen) > DEF_PULL_LENGTH &&
                        mFingerPressedCount == 1 &&
                        !mDrawLongPress) {
                    //移动k线图
                    mKViewInnerListener.moveKView(moveLen);
                }
                break;
            case MotionEvent.ACTION_UP:
                //单击事件
                if (event.getEventTime() - mPressTime < DEF_CLICKPRESS_LENGTH) {
                    //单击并且是在绘制十字
                    if (mDrawLongPress) {
                        //取消掉长按十字
                       mKViewInnerListener.hiddenLongPressView();
                    } else {
                        //响应单击事件
                        mKViewInnerListener.onKViewInnerClickListener();
                    }
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }
    private void initListener() {
        mKViewInnerListener=new KViewInnerListener() {
            @Override
            public void showLongPressView() {
                mMasterView.showLongPressView();
            }

            @Override
            public void hiddenLongPressView() {
                mMasterView.hiddenLongPressView();
            }

            @Override
            public void moveKView(float moveLen) {
                mMasterView.moveKView(moveLen);
            }

            @Override
            public void onKViewInnerClickListener() {
                mMasterView.onKViewInnerClickListener();
            }
        };
    }
}
