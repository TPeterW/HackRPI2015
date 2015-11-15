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
    //for double and int, null is represented as -1
    //for String, null is represented as ""
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

    public double[] getDistance(JSONObject data) throws JSONException {

        JSONArray JSONArr = data.getJSONArray("data");
        double[] distanceArr = new double[JSONArr.length()];

        for (int i=0; i<JSONArr.length();i++) {
            try {
                distanceArr[i] = JSONArr.getJSONObject(i).getDouble("distance");
            } catch (Exception e) {
                distanceArr[i] = RESULT_NULL;
            }
        }
//        for (int i=0; i<distanceArr.length;i++){
//            Log.v("ratingArr", String.valueOf(distanceArr[i]));
//        }
        return distanceArr;
    }

    public int[] getNumReview(JSONObject data) throws JSONException {

        JSONArray JSONArr = data.getJSONArray("data");
        int[] numReviewArr = new int[JSONArr.length()];

        for (int i=0; i<JSONArr.length();i++) {
            try {
                numReviewArr[i] = JSONArr.getJSONObject(i).getInt("num_reviews");
            } catch (Exception e) {
                numReviewArr[i] = (int) RESULT_NULL;
            }
        }
//        for (int i=0; i<numReviewArr.length;i++){
//            Log.v("ratingArr", String.valueOf(numReviewArr[i]));
//        }
        return numReviewArr;
    }

    public String[] getName(JSONObject data) throws JSONException {

        JSONArray JSONArr = data.getJSONArray("data");
        String[] nameArr = new String[JSONArr.length()];

        for (int i=0; i<JSONArr.length();i++) {
            try {
                nameArr[i] = JSONArr.getJSONObject(i).getString("name");
            } catch (Exception e) {
                nameArr[i] = "";
            }
        }
//        for (int i=0; i<nameArr.length;i++){
//            Log.v("ratingArr", nameArr[i]);
//        }
        return nameArr;
    }

    public String[] getPriceLvl(JSONObject data) throws JSONException {

        JSONArray JSONArr = data.getJSONArray("data");
        String[] priceLvlArr = new String[JSONArr.length()];

        for (int i=0; i<JSONArr.length();i++) {
            try {
                priceLvlArr[i] = JSONArr.getJSONObject(i).getString("price_level");
            } catch (Exception e) {
                priceLvlArr[i] = "";
            }
        }
//        for (int i=0; i<priceLvlArr.length;i++){
//            Log.v("ratingArr", priceLvlArr[i]);
//        }
        return priceLvlArr;
    }

    public String[] getBearing(JSONObject data) throws JSONException {

        JSONArray JSONArr = data.getJSONArray("data");
        String[] bearingArr = new String[JSONArr.length()];

        for (int i=0; i<JSONArr.length();i++) {
            try {
                bearingArr[i] = JSONArr.getJSONObject(i).getString("bearing");
            } catch (Exception e) {
                bearingArr[i] = "";
            }
        }
//        for (int i=0; i<bearingArr.length;i++){
//            Log.v("ratingArr", bearingArr[i]);
//        }
        return bearingArr;
    }


}
