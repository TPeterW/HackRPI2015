package com.peter.roadtip.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.peter.roadtip.R;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Peter on 11/14/15.
 *
 */
public class TripAdvisorAgent {

    // simply singleton
    private static TripAdvisorAgent instance;

    private static String apiKey;
    private Context context;

    public static TripAdvisorAgent getInstance(Context context) {
        if (instance == null) {
            instance = new TripAdvisorAgent(context);
        }
        return instance;
    }

    private TripAdvisorAgent(Context context) {
        this.context = context;
        this.apiKey = context.getString(R.string.trip_advisor_key);
    }

    /**
     *
     * @param       request
     * @return      null if encounters error
     */
    public void getResponse(final String request) {
//        URL url;
//        HttpURLConnection urlConnection = null;
//        String response = "";
//
//        try {
//            url = new URL(request);
//            urlConnection = (HttpURLConnection) url.openConnection();
//
//            InputStream in = urlConnection.getInputStream();
//            InputStreamReader isr = new InputStreamReader(in);
//
//            int data = isr.read();
//            while (data != -1) {
//                char current = (char) data;
//                data = isr.read();
//                response += current;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(context, context.getString(R.string.problem_url), Toast.LENGTH_SHORT).show();
//        }
//
//
//        return response;




        new GetResponseTaskAsync().execute(request);
    }

    /**
     *
     * @param distance      radius from LatLng, 0 if no specification
     * @param name          name of property, null if no specification
     * @return request      string
     */
    public String createRequest(double latitude, double longitude, double distance, @Nullable String name) {
        // map is to find by LatLng, other options available
        String request = "http://api.tripadvisor.com/api/partner/2.0/map/";
        request += latitude + "," + longitude + "?key=" + apiKey;

        if (distance != 0) {
            name = null;                // we don't allow search both by distance and name
            request += "&distance=" + distance;
            //TODO: filtered map call
        }

        if (name != null) {
            request += "&q=" + name;
        }

        return request;
    }

    class GetResponseTaskAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            try {
                URL url = new URL(urls[0]);
                URLConnection urlConnection = url.openConnection();

                InputStream in = urlConnection.getInputStream();
                InputStreamReader isr = new InputStreamReader(in);

                int data = isr.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isr.read();
                    response += current;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putString(context.getString(R.string.json_returned), result)
                    .apply();
            Log.i("HttpResponse", result);
            super.onPostExecute(result);
        }
    }
}
