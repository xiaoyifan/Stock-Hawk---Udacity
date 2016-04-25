package com.sam_chordas.android.stockhawk.rest;

import com.sam_chordas.android.stockhawk.models.QuoteInfo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Yifan on 4/23/16.
 */
public interface QuoteService {

    @GET("public/yql")
    Call<QuoteInfo> getObjectWithNestedArraysAndObject(@Query("q") String q, @Query("diagnostics") String diagnostics,
                                                       @Query("env") String env, @Query("format") String format);
}
