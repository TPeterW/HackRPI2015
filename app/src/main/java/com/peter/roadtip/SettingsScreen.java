package com.peter.roadtip;

import android.app.ActivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.peter.roadtip.utils.SettingsFragment;

import java.util.List;

/**
 * Created by Peter on 11/14/15.
 *
 */
public class SettingsScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        finishActivity(0);
        super.onBackPressed();
    }
}
