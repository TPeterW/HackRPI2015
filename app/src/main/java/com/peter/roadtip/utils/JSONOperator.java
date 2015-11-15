package com.peter.roadtip.utils;

import android.content.Context;

/**
 * Created by Peter on 11/14/15.
 *
 */
public class JSONOperator {

    private static JSONOperator instance;

    private Context context;

    public static JSONOperator getInstance(Context context) {
        if (instance == null)
            instance = new JSONOperator(context);

        return instance;
    }

    private JSONOperator(Context context) {
        this.context = context;
    }

    
}
