package wgyscsf.financialcustomerview.timesharing;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

import wgyscsf.financialcustomerview.R;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/12/4 16:58
 * 描 述 ：分时图mp版
 * ============================================================
 **/
public class MpTimeSharingView extends LineChart {
    private static final String TAG = "MpTimeSharingView";
    private Context mContext;
    //color
    protected int mOuterLineColor;
    protected int mInnerXyLineColor;
    protected int mBrokenLineColor;
    protected int mDotColor;
    protected int mTimingLineColor;
    protected int mBrokenLineBgColor;
    protected int mTimingTxtColor;
    protected int mTimingTxtBgColor;
    protected int mXYTxtColor;
    protected int mLongPressColor;
    protected int mLongPressTxtColor;
    protected int mLongPressTxtBgColor;
    protected int mLoadingTxtClolr;

    //数据
    private LineData lineData;
    private LineDataSet lineDataSet;
    private List<Entry> lineEntries;

    public MpTimeSharingView(Context context) {
        this(context, null);
    }

    public MpTimeSharingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public MpTimeSharingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initAttrs();
    }

    protected void initAttrs() {
        initColorAttrs();
        initMpAttrs();
    }

    protected void initColorAttrs() {
        //颜色
        mOuterLineColor = getColor(R.color.color_timeSharing_outerStrokeColor);
        mInnerXyLineColor = getColor(R.color.color_timeSharing_innerXyDashColor);
        mBrokenLineColor = getColor(R.color.color_timeSharing_brokenLineColor);
        mDotColor = getColor(R.color.color_timeSharing_dotColor);
        mTimingLineColor = getColor(R.color.color_timeSharing_timingLineColor);
        mBrokenLineBgColor = getColor(R.color.color_timeSharing_blowBlueColor);
        mTimingTxtColor = getColor(R.color.color_timeSharing_timingTxtColor);
        mTimingTxtBgColor = getColor(R.color.color_timeSharing_timingTxtBgColor);
        mXYTxtColor = getColor(R.color.color_timeSharing_xYTxtColor);
        mLongPressColor = getColor(R.color.color_timeSharing_longPressLineColor);
        mLongPressTxtColor = getColor(R.color.color_timeSharing_longPressTxtColor);
        mLongPressTxtBgColor = getColor(R.color.color_timeSharing_longPressTxtBgColor);
        mLoadingTxtClolr = getColor(R.color.color_timeSharing_xYTxtColor);
    }

    protected void initMpAttrs() {
        lineData = new LineData();
        lineEntries = new ArrayList<>();
        lineDataSet = new LineDataSet(lineEntries, "图例");

        //begin
        // no description text
        Description description = new Description();
        description.setText("我是描述文字信息");
        description.setTextColor(Color.RED);
        //description.setPosition(0,0);
        description.setEnabled(false);
        setDescription(description);

        // enable touch gestures
        setTouchEnabled(true);

        // enable scaling and dragging
        setDragEnabled(true);
        setScaleEnabled(false);
        setScaleXEnabled(false);
        setScaleYEnabled(false);

        // if disabled, scaling can be done on x- and y-axis separately
        setPinchZoom(true);

        // set an alternative background color
        //setBackgroundColor(0xFFfff0ae);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        MyMarkerView mv = new MyMarkerView(mContext, R.layout.custom_marker_view);
        mv.setChartView(this); // For bounds control
        setMarker(mv); // Set the marker to the chart

        // x-axis limit line
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(2f);
        llXAxis.enableDashedLine(4f, 4f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        llXAxis.setTextSize(10f);

        XAxis xAxis = getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        //xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());
        //xAxis.addLimitLine(llXAxis); // add x-axis limit line

        YAxis leftAxis = getAxisLeft();
        leftAxis.removeAllLimitLines();
        // reset all limit lines to avoid overlapping lines
        //leftAxis.setAxisMaximum(100f);
        //leftAxis.setAxisMinimum(-50f);
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(true);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        getAxisRight().setEnabled(true);
        getAxisLeft().setEnabled(true);


        getViewPortHandler().setMinMaxScaleX(1f,4f);
        getViewPortHandler().setMinMaxScaleY(1f,4f);

        // see:setTimeSharingData
        // setData(45, 100);

        setVisibleXRange(20, 20);
        setVisibleYRange(20f, 20, YAxis.AxisDependency.RIGHT);
        centerViewTo(20, 50, YAxis.AxisDependency.LEFT);

        animateX(2500);
        //mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = getLegend();
        l.setEnabled(false);
        l.setForm(Legend.LegendForm.CIRCLE);

        // // dont forget to refresh the drawing
        // mChart.invalidate();


        lineDataSet.setDrawIcons(false);
        // set the line to be drawn like this "- - - - - -"
        //lineDataSet.enableDashedLine(5f, 5f, 0f);
        lineDataSet.enableDashedHighlightLine(5f, 5f, 0f);
        lineDataSet.setHighLightColor(mLongPressColor);
        lineDataSet.setColor(mBrokenLineColor);
        lineDataSet.setCircleColor(Color.BLACK);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(9f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFormLineWidth(1f);
        lineDataSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        lineDataSet.setFormSize(15.f);

        //小圆点
        lineDataSet.setDrawCircles(false);
        //小圆点上方的文字
        lineDataSet.setDrawValues(false);
        //长按十字
        lineDataSet.setHighlightEnabled(true);
    }

    protected int getColor(@ColorRes int colorId) {
        return getResources().getColor(colorId);
    }

    /**
     * 数据设置入口
     *
     * @param quotesList
     */
    public void setTimeSharingData(List<Quotes> quotesList) {
        resetData();
        if (quotesList == null || quotesList.isEmpty()) {
            Toast.makeText(mContext, "数据异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "setTimeSharingData: 数据异常");
            return;
        }

        for (int i = 0; i < 50; i++) {
            lineEntries.add(new Entry(i, (float) quotesList.get(i).c));
        }
        lineDataSet.setValues(lineEntries);

        lineData.addDataSet(lineDataSet);
        setData(lineData);

        invalidate();
    }
    /**
     * 实时推送过来的数据，实时更新
     *
     * @param quotes
     */
    public void addTimeSharingData(Quotes quotes) {
        if (quotes == null) {
            Toast.makeText(mContext, "数据异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "setTimeSharingData: 数据异常");
            return;
        }

        lineEntries.add(new Entry(lineEntries.size(), (float) quotes.c));

        lineDataSet.setValues(lineEntries);

        lineData.addDataSet(lineDataSet);

        setData(lineData);

        invalidate();
    }

    private void resetData() {
        lineEntries.clear();
        lineDataSet.clear();
        lineData.clearValues();
    }
}
