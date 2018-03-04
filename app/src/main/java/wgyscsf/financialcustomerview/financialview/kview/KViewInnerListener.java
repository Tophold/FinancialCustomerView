package wgyscsf.financialcustomerview.financialview.kview;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/04 10:50
 * 描 述 ：
 * ============================================================
 **/
public interface KViewInnerListener {
    void showLongPressView();
    void hiddenLongPressView();
    void moveKView(float moveLen);
    void onKViewInnerClickListener();
}
