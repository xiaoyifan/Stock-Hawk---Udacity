package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sam_chordas.android.stockhawk.R;

public class DetailActivity extends AppCompatActivity {

    static String LOG_TAG = DetailActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        String symbol = getIntent().getExtras().getString("symbol");

        Log.d(LOG_TAG + " symbol:", symbol);

    }

}
