package com.tophold.trade.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;


import java.lang.reflect.Method;

/**
 * 获取屏幕宽高等 相关工具类
 */
@SuppressWarnings("WeakerAccess")
public class ScreenUtils {
    public static int screenWidth;
    public static int screenHeight;
    public static float density;
    public static float scaledDensity;
    public static int statusbarheight;
    public static int navbarheight;
    public static int realScreenHeight;
    public static int realScreenWidth;

    static {
        init();
    }

    private static void init() {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        density = displayMetrics.density;
        scaledDensity = displayMetrics.scaledDensity;
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth() {
        return screenWidth;
    }

    /**
     * 获取屏幕高度(不包含底部虚拟按键)
     */
    public static int getScreenHeight() {
        return screenHeight;
    }

    public static int px2dip(float pxValue) {
        return (int) (pxValue / density + 0.5f);
    }

    public static int dip2px(float dpValue) {
        return (int) (dpValue * density + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     */
    public static float px2sp(float pxValue) {
        return pxValue / scaledDensity;
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     */
    public static float sp2px(float spValue) {
        return spValue * scaledDensity;
    }

    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHight() {
        if (statusbarheight != 0) {
            return statusbarheight;
        }
        int result = 0;
        int resourceId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusbarheight = result = Resources.getSystem().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取底部导航栏高度(不显示则为0)
     */
    public static int getNavigationBarHeight(Context context) {
        if (navbarheight != 0) {
            return navbarheight;
        }
        return navbarheight = getRealScreenHeight(context) - getScreenHeight();
    }

    /**
     * 获取底部导航栏高度(无论显示与否)
     */
    public static int getRealNavigationBarHeight() {
        int result = 0;
        int resourceId = Resources.getSystem().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = Resources.getSystem().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 底部导航栏高度是否显示
     */
    public static boolean hasNavigationBar(Context context) {
        return getNavigationBarHeight(context) > 0;
    }

    /**
     * 获取真实屏幕高度(带虚拟按键)
     */
    public static int getRealScreenHeight(Context context) {
        if (realScreenHeight != 0) {
            return realScreenHeight;
        }
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        assert wm != null;
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= 17) {//17以上可以直接获取 以下反射获取
            display.getRealMetrics(dm);
        } else {
            @SuppressWarnings("rawtypes")
            Class c;
            try {
                c = Class.forName("android.view.Display");
                @SuppressWarnings("unchecked")
                Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
                method.invoke(display, dm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return realScreenHeight = dm.heightPixels;
    }

    /*用于横屏时获取真实宽度*/
    public static int getRealScreenWidth(Context context) {
        if (realScreenWidth != 0) {
            return realScreenWidth;
        }
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        assert wm != null;
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= 17) {//17以上可以直接获取 以下反射获取
            display.getRealMetrics(dm);
        } else {
            @SuppressWarnings("rawtypes")
            Class c;
            try {
                c = Class.forName("android.view.Display");
                @SuppressWarnings("unchecked")
                Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
                method.invoke(display, dm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return realScreenWidth = dm.widthPixels;
    }

    /**
     * 获取view的矩形坐标
     */
    public RectF getViewRectF(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new RectF(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
    }

    /**
     * 判断是否触摸在view上
     */
    public boolean isViewTouched(View view, float rawX, float rawY) {
        return getViewRectF(view).contains(rawX, rawY);
    }

    public boolean isViewTouched(View view, MotionEvent event) {
        // event.getX(); 获取相对于控件自身左上角的 x 坐标值
        // event.getY(); 获取相对于控件自身左上角的 y 坐标值
        float rawX = event.getRawX(); // 获取相对于屏幕左上角的 x 坐标值
        float rawY = event.getRawY();// 获取相对于屏幕左上角的 y 坐标值
        return isViewTouched(view, rawX, rawY);
    }

    /**
     * 判断一个view的矩形是否包含另一view的矩形
     */
    public boolean containsRectF(View view1, View view2) {
        return getViewRectF(view1).contains(getViewRectF(view2));
    }

    public boolean isInRectF(RectF rectF, float rawX, float rawY) {
        return rectF.contains(rawX, rawY);
    }

    /**
     * 获取软键盘高度(不显示则为0)
     */
    public static int getSoftInputHeight(Activity activity) {
        if (activity == null || activity.isFinishing()) return 0;
        //获取显示区域
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        //rect.bottom为显示区域底部高度(不包含键盘) 屏幕高度减去显示区域高度 即为软键盘高度
        return screenHeight - rect.bottom;
    }

    /**
     * 软件盘是否显示
     */
    public static boolean isSoftInputShown(Activity activity) {
        return getSoftInputHeight(activity) > 0;
    }

    public static int getImageMaxEdge() {
        return (int) (165.0 / 320.0 * screenWidth);
    }

    public static int getImageMinEdge() {
        return (int) (76.0 / 320.0 * screenWidth);
    }
}
