package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.CustomSpinnerAdapter;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.models.DateValue;
import com.sam_chordas.android.stockhawk.models.Quote;
import com.sam_chordas.android.stockhawk.models.QuoteInfo;
import com.sam_chordas.android.stockhawk.rest.QuoteService;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailActivity extends AppCompatActivity implements Callback<QuoteInfo>{

    ProgressBar pb;
    public static String symbol;

    ArrayList<DateValue> quoteList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        symbol = getIntent().getExtras().getString("symbol");
        pb = (ProgressBar)findViewById(R.id.progressBar);
        getSupportActionBar().setTitle(symbol);
    }

    private void fetchStockDetails(String symbol, String startDate, String endDate) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://query.yahooapis.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        QuoteService quoteCall = retrofit.create(QuoteService.class);
    //http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20=%20%22YHOO%22%20and%20startDate%20=%20%222014-02-11%22%20and%20endDate%20=%20%222014-02-18%22&format=json&diagnostics=true&env=store://datatables.org/alltableswithkeys&callback=

        String q = "select * from yahoo.finance.historicaldata where symbol = \""+symbol+"\" and startDate = \""+endDate+"\" and endDate = \""+startDate+"\"";
        String diagnostics = "true";
        String env = "store://datatables.org/alltableswithkeys";
        String format = "json";
        Call<QuoteInfo> call = quoteCall.getObjectWithNestedArraysAndObject(q,diagnostics,env,format);

        call.enqueue(this);
    }

    //implement call backs
    @Override
    public void onResponse(Call<QuoteInfo> call, Response<QuoteInfo> response) {
        pb.setVisibility(View.GONE);
        QuoteInfo quoteInfo = response.body();

        ArrayList<Quote> quoteArray = quoteInfo.query.results.quote;

        Collections.reverse(quoteArray);
        int i;
        quoteList.clear();
        for (i=0;i<quoteArray.size();i++ ) {
            DateValue data = new DateValue();
            data.date = quoteArray.get(i).quote_date;
            data.closeValue = quoteArray.get(i).close;
            quoteList.add(data);
        }
        //get value from the response


        LineChartFragment fragment = LineChartFragment.newInstance(quoteList);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_fg, fragment)
                .commit();
    }

    @Override
    public void onFailure(Call<QuoteInfo> call, Throwable t) {
        Toast.makeText(DetailActivity.this, "data fetch failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);

        ArrayList<String> list = new ArrayList<String>();
        list.add(getString(R.string.one_week));
        list.add(getString(R.string.one_month));
        list.add(getString(R.string.three_month));
        list.add(getString(R.string.six_month));
        list.add(getString(R.string.one_year));
        CustomSpinnerAdapter spinAdapter = new CustomSpinnerAdapter(
                getApplicationContext(), list);
        spinner.setAdapter(spinAdapter); // set the adapter to provide layout of rows and content
//        spinner.setOnItemSelectedListener(onItemSelectedListener);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,
                                       int position, long id) {
                // On selecting a spinner item
                String item = adapter.getItemAtPosition(position).toString();
                String startDate = Utils.getFormattedDate(System.currentTimeMillis());
                Date date = new Date();

                fetchStockDetails(symbol, startDate, Utils.getDateBackTo(date, item));

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        return true;
    }
}
