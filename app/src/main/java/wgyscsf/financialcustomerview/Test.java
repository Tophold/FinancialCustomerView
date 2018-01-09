package wgyscsf.financialcustomerview;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/01/09 17:27
 * 描 述 ：
 * ============================================================
 **/
public class Test {
    boolean isLoadComty = false;

    void callBack(int process) {
        isLoadComty = false;
        if (process == 100) {
            isLoadComty = true;
        }

    }
}
