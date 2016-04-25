package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

  public static final String DATE_FORMAT = "yyyy-MM-dd";


  public static String truncateBidPrice(String bidPrice){
    bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
            QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = jsonObject.getString("Change");
      builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
      builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
      builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
          jsonObject.getString("ChangeinPercent"), true));
      builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
      builder.withValue(QuoteColumns.ISCURRENT, 1);
      if (change.charAt(0) == '-'){
        builder.withValue(QuoteColumns.ISUP, 0);
      }else{
        builder.withValue(QuoteColumns.ISUP, 1);
      }

    } catch (JSONException e){
      e.printStackTrace();
    }
    return builder.build();
  }

  public static ArrayList quoteJsonToContentVals(String JSON) {
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try {
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0) {
        jsonObject = jsonObject.getJSONObject("query");
        int count = Integer.parseInt(jsonObject.getString("count"));
        String created = jsonObject.getString("created");
        if (count == 1) {
          jsonObject = jsonObject.getJSONObject("results")
                  .getJSONObject("quote");
          batchOperations.add(buildBatchOperation(jsonObject, created));
        } else {
          resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

          if (resultsArray != null && resultsArray.length() != 0) {
            for (int i = 0; i < resultsArray.length(); i++) {
              jsonObject = resultsArray.getJSONObject(i);
              batchOperations.add(buildBatchOperation(jsonObject, created));
            }
          }
        }
      }
    } catch (JSONException e) {
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    } catch (Exception e) {
      return null;
    }
    return batchOperations;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject, String created) {
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
            QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = jsonObject.getString("Change");
      builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
      builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
      builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
              jsonObject.getString("ChangeinPercent"), true));
      builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
      builder.withValue(QuoteColumns.ISCURRENT, 1);
      if (change.charAt(0) == '-') {
        builder.withValue(QuoteColumns.ISUP, 0);
      } else {
        builder.withValue(QuoteColumns.ISUP, 1);
      }
      builder.withValue(QuoteColumns.CREATED, created);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return builder.build();
  }

  public static boolean hasNetworkConnection(Context context) {
    boolean hasConnectedWifi = false;
    boolean hasConnectedMobile = false;

    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
    for (NetworkInfo ni : netInfo) {
      if (ni.getTypeName().equalsIgnoreCase("WIFI"))
        if (ni.isConnected())
          hasConnectedWifi = true;
      if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
        if (ni.isConnected())
          hasConnectedMobile = true;
    }
    return hasConnectedWifi || hasConnectedMobile;
  }

  public static String getFormattedDate(long dateInMillis ) {
    Locale localeUS = new Locale("en", "US");
    SimpleDateFormat queryDayFormat = new SimpleDateFormat(Utils.DATE_FORMAT,localeUS);
    return queryDayFormat.format(dateInMillis);
  }

  public static String getDateBackTo(Date date, String period){

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);

    switch (period){

      case "1W":
        calendar.add(Calendar.DATE, -7);
        break;
      case "1M":
        calendar.add(Calendar.MONTH, -1);
        break;
      case "3M":
        calendar.add(Calendar.MONTH, -3);
        break;
      case "6M":
        calendar.add(Calendar.MONTH, -6);
        break;
      case "1Y":
        calendar.add(Calendar.YEAR, -1);
        break;

    }

    return getFormattedDate(calendar.getTimeInMillis());
  }

}
