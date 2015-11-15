package com.peter.roadtip.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Peter on 11/14/15.
 *
 */
public class JSONOperator {

    private static JSONOperator instance;
    private static final double RESULT_NULL = -1;
    private Context context;

    public static JSONOperator getInstance(Context context) {
        if (instance == null)
            instance = new JSONOperator(context);

        return instance;
    }

    private JSONOperator(Context context) {
        this.context = context;
    }

    public double[] getLat(JSONObject data) throws JSONException {

        JSONArray JSONArr = data.getJSONArray("data");
        double[] latArr = new double[JSONArr.length()];

        for (int i=0; i<JSONArr.length();i++) {
            latArr[i] = JSONArr.getJSONObject(i).getDouble("latitude");
        }
//        for (int i=0; i<latArr.length;i++){
//            Log.v("latArr", String.valueOf(latArr[i]));
//        }
        return latArr;
    }

    public double[] getLng(JSONObject data) throws JSONException {

        JSONArray JSONArr = data.getJSONArray("data");
        double[] lngArr = new double[JSONArr.length()];

        for (int i=0; i<JSONArr.length();i++) {
            lngArr[i] = JSONArr.getJSONObject(i).getDouble("longitude");
        }
//        for (int i=0; i<lngArr.length;i++){
//            Log.v("lngArr", String.valueOf(lngArr[i]));
//        }
        return lngArr;
    }

    public double[] getRating(JSONObject data) throws JSONException {

        JSONArray JSONArr = data.getJSONArray("data");
        double[] ratingArr = new double[JSONArr.length()];

        for (int i=0; i<JSONArr.length();i++) {
            try {
                ratingArr[i] = JSONArr.getJSONObject(i).getDouble("rating");
            } catch (Exception e) {
                ratingArr[i] = RESULT_NULL;
            }

        }
//        for (int i=0; i<ratingArr.length;i++){
//            Log.v("ratingArr", String.valueOf(ratingArr[i]));
//        }
        return ratingArr;
    }

}
