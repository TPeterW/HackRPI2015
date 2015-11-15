package com.peter.roadtip.utils;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Peter on 11/15/15.
 *
 */
public class LocationService extends Service {


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public class BackgroundLocationListener implements LocationListener {
        Location lastLocation;

        public BackgroundLocationListener(String provider) {
            Log.d("LocationListener", "LocationListener " + provider);
            lastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d("LocationListener", "LocationChanged " + location);
            lastLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("LocationListener", "StatusChanged " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("LocationListener", "ProviderEnabled " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("LocationListener", "ProviderDisabled " + provider);
        }
    }
}
