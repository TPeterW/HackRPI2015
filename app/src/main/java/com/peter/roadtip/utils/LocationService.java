package com.peter.roadtip.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.peter.roadtip.MainScreen;
import com.peter.roadtip.R;

import org.json.JSONException;
import org.json.JSONObject;

import static com.peter.roadtip.utils.TripAdvisorAgent.*;

/**
 * Created by Peter on 11/15/15.
 *
 */
public class LocationService extends Service implements ResponseListener {

    private static int notificationId;

    private JSONOperator jsonOperator;

    private boolean pushNotifs;

    private LocationManager locationManager = null;
    private static final int LOCATION_INTERVAL = 1000;          // that's one sec mate
    private static final float LOCATION_DISTANCE = 0f;

    BackgroundLocationListener[] listLocationListener = new BackgroundLocationListener[]{
            new BackgroundLocationListener(LocationManager.GPS_PROVIDER),
            new BackgroundLocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LocationListener", "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    @SuppressWarnings("all")
    public void onCreate() {
        Log.d("LocationListener", "onCreate");
        initialiseLocationManager();

        pushNotifs = true;

        jsonOperator = JSONOperator.getInstance(this);


        try {
            if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getBoolean(getString(R.string.can_access_location), true)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        listLocationListener[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onCreate();
    }

    private void initialiseLocationManager() {
        Log.d("LocationListener", "initialiseLocationManager");
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        }
    }

    private void pushNotification(String title, String msg, double latitude, double longitude) {
        // TODO: depending on type of notification, we give different icons
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_normal_notification)
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000});

        Intent toDisplayScreen = new Intent(LocationService.this, MainScreen.class);  //TODO: change to specific page later
        toDisplayScreen.putExtra("latitude", latitude);
        toDisplayScreen.putExtra("longitude", longitude);
        toDisplayScreen.putExtra("hasIntent", true);
        toDisplayScreen.putExtra("name", title);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainScreen.class);
        stackBuilder.addNextIntent(toDisplayScreen);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(5221, builder.build());
    }

    @Override
    @SuppressWarnings("all")
    public void onDestroy() {
        Log.d("LocationListener", "onDestroy");
        if (locationManager != null) {
            for (int i = 0; i < listLocationListener.length; i++) {
                try {
                    locationManager.removeUpdates(listLocationListener[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.onDestroy();
    }

    @Override
    public void onReceiveResponse(String result) {
        try {
            JSONObject data = new JSONObject(result);
            JSONOperator jsonOperator = JSONOperator.getInstance(LocationService.this);

            double[] listDistance = jsonOperator.getDistance(data);
            double[] listLat = jsonOperator.getLat(data);
            double[] listLng = jsonOperator.getLng(data);
            int minIndex = getMinIndex(listDistance);

            if (pushNotifs)
                pushNotification(jsonOperator.getName(data)[minIndex],
                        getString(R.string.notif_msg),
                        listLat[minIndex], listLng[minIndex]);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private double getDistanceInKilo(Location A, Location B) {
        return A.distanceTo(B);
    }

    private int getMinIndex(double[] input) {
        double min = Double.MAX_VALUE;
        int minIndex = 0;
        for (int i = 0; i < input.length; i++) {
            if (input[i] < min) {
                min = input[i];
                minIndex = i;
            }
        }
        return minIndex;
    }

    @Override
    public void drawMarkers(String[] listName, double[] listLat, double[] listLng, double[] listDistance) {

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

            TripAdvisorAgent tripAdvisorAgent = getInstance(LocationService.this);
            String request = tripAdvisorAgent.createRequest(lastLocation.getLatitude(), lastLocation.getLongitude(), 0, null);
            tripAdvisorAgent.getResponse(request);
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
