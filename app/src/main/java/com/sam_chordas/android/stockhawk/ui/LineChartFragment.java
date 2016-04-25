package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.models.DateValue;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class LineChartFragment extends Fragment implements OnChartValueSelectedListener{

//    @Bind(R.id.detail_stock_label) TextView stockLabelTextView;
//    @Bind(R.id.detail_date_label) TextView dateTextView;
//    @Bind(R.id.detail_price_label) TextView priceTextView;

    TextView stockLabelTextView;
    TextView dateTextView;
    TextView priceTextView;

    public static String LOG_TAG = "LineChrtFragment";

    public double upperLimit;
    public double lowerLimit;

    private static final String VALUE_KEY = "date_value_key";
    private ArrayList<DateValue> valueList;

    public LineChartFragment() {
    }

    public static LineChartFragment newInstance(ArrayList<DateValue> list) {
        LineChartFragment fragment = new LineChartFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(VALUE_KEY, list);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_line_chart, container, false);

        valueList = (ArrayList<DateValue>) getArguments().getSerializable(
                VALUE_KEY);
        LineChart mChart = (LineChart) rootView.findViewById(R.id.chart);
        LineData data = new LineData(getXAxisValues(),getDataSet());

        setUpChart(mChart, data);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LineChart mChart = (LineChart) view.findViewById(R.id.chart);

        stockLabelTextView = (TextView)view.findViewById(R.id.detail_stock_label);
        stockLabelTextView.setText(mChart.getLegend().getLabel(0));
        dateTextView = (TextView)view.findViewById(R.id.detail_date_label);
        dateTextView.setText(valueList.get(valueList.size() - 1).date);
        priceTextView = (TextView)view.findViewById(R.id.detail_price_label);
        priceTextView.setText(roundPrice(valueList.get(valueList.size() - 1).closeValue));
    }

    private void setUpChart(LineChart mChart, LineData data) {
        mChart.setData(data);
        mChart.setDescription(getString(R.string.quote_history_text));
        mChart.setNoDataTextDescription(getString(R.string.quote_history_error));
        mChart.setDrawGridBackground(false);

        ArrayList<ILineDataSet> sets = (ArrayList<ILineDataSet>) mChart.getData()
                .getDataSets();

        for (ILineDataSet set : sets) {
            set.setDrawFilled(true);
        }


        mChart.setTouchEnabled(true);
        data.setDrawValues(false);
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setPinchZoom(false);


        //mChart.setBackgroundColor(Color.BLACK);

        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextSize(12f);
        l.setTextColor(Color.WHITE);
        l.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);


        XAxis xAxis = mChart.getXAxis();
        xAxis.setEnabled(false);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setSpaceBetweenLabels(1);
        xAxis.setEnabled(false);



        YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.setTypeface(tf);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaxValue((float) upperLimit);
        leftAxis.setDrawGridLines(true);

        //setting up the upper line
        LimitLine ll1 = new LimitLine((float)upperLimit);
        ll1.setLineWidth(2f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setLineColor(Color.GREEN);

        //setting up the lower line
        LimitLine ll2 = new LimitLine((float)lowerLimit);
        ll2.setLineWidth(2f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setLineColor(Color.RED);


        YAxis rightAxis = mChart.getAxisRight();

        rightAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        rightAxis.addLimitLine(ll1);
        rightAxis.addLimitLine(ll2);
        rightAxis.setDrawLimitLinesBehindData(true);
        rightAxis.setTextColor(Color.WHITE);
        rightAxis.setAxisMaxValue((float) upperLimit);
        rightAxis.enableGridDashedLine(10f, 10f, 0f);
        mChart.animateXY(1000, 1000);
        mChart.invalidate();


        mChart.setOnChartValueSelectedListener(this);
    }

    public String roundPrice(String originalPrice){

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_EVEN);
        Double number = Double.parseDouble(originalPrice);

        return ""+df.format(number);
    }

    private ArrayList<ILineDataSet> getDataSet() {
        ArrayList<ILineDataSet> dataSets = null;

        upperLimit = Double.MIN_VALUE;
        lowerLimit = Double.MAX_VALUE;

        ArrayList<Entry> valueSet = new ArrayList<>();
        int i=0;
        for (DateValue value:valueList
                ) {
            valueSet.add(new Entry(Float.parseFloat(value.closeValue), i++));
            upperLimit = Math.max(Float.parseFloat(value.closeValue), upperLimit);
            lowerLimit = Math.min(Float.parseFloat(value.closeValue), lowerLimit);

        }

        LineDataSet lineDataSet = new LineDataSet(valueSet, DetailActivity.symbol);
        lineDataSet.setColor(Color.rgb(0, 133, 202));

        dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);
        return dataSets;
    }

    private ArrayList<String> getXAxisValues() {
        ArrayList<String> xAxis = new ArrayList<>();

        for (DateValue dateValue:valueList
                ) {
            xAxis.add(dateValue.date);
        }
        return xAxis;
    }


    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

        DateValue value = valueList.get(e.getXIndex());

        Log.d(LOG_TAG, "onSelected Entry: " + value.closeValue);
        Log.d(LOG_TAG, "onSelected Index: " + value.date);


        dateTextView.setText(value.date);
        priceTextView.setText(roundPrice(value.closeValue));

    }

    @Override
    public void onNothingSelected() {

    }
}
