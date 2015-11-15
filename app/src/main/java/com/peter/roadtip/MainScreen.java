package com.peter.roadtip;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.peter.roadtip.utils.JSONOperator;
import com.peter.roadtip.utils.TripAdvisorAgent;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.peter.roadtip.utils.TripAdvisorAgent.*;

////////////////////////////////////////////////////////////////////
//                            _ooOoo_                             //
//                           o8888888o                            //
//                           88" . "88                            //
//                           (| ^_^ |)                            //
//                           O\  =  /O                            //
//                        ____/`---'\____                         //
//                      .'  \\|     |//  `.                       //
//                     /  \\|||  :  |||//  \                      //
//                    /  _||||| -:- |||||-  \                     //
//                    |   | \\\  -  /// |   |                     //
//                    | \_|  ''\---/''  |   |                     //
//                    \  .-\__  `-`  ___/-. /                     //
//                  ___`. .'  /--.--\  `. . ___                   //
//                ."" '<  `.___\_<|>_/___.'  >'"".                //
//              | | :  `- \`.;`\ _ /`;.`/ - ` : | |               //
//              \  \ `-.   \_ __\ /__ _/   .-` /  /               //
//        ========`-.____`-.___\_____/___.-`____.-'========       //
//                             `=---='                            //
//        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^      //
//                    Buddha Keeps Bugs Away                      //
////////////////////////////////////////////////////////////////////


public class MainScreen extends FragmentActivity implements OnMapReadyCallback, OnSharedPreferenceChangeListener,
        OnMyLocationChangeListener, OnNavigationItemSelectedListener, ResponseListener {

    private GoogleMap googleMap;

    // Locations
    private Location currentLocation;
    private LocationManager locationManager;

    // Search Box
    private SearchBox searchBox;

    // Drawer
    private DrawerLayout drawer;
    private NavigationView navigationView;

    // TripAdvisor
    private TripAdvisorAgent tripAdvisorAgent;
    private JSONOperator jsonOperator;

    private boolean hasLocationPermission;

    // Request Codes
    private final static int FINE_LOCATION_PERMISSION_REQUEST_CODE = 0x001;
    private final static int VOICE_SEARCH_REQUEST_CODE = 1234;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        initData();
        
        initDrawer();

        setUpMap();

        setUpSearchBox();
    }

    private void initData() {
        tripAdvisorAgent = getInstance(this);
        String request = tripAdvisorAgent.createRequest(42.33141, -71.099396, 0, null);
        Log.i("HttpRequest", request);
        tripAdvisorAgent.getResponse(request);

        jsonOperator = JSONOperator.getInstance(this);
    }

    private void initDrawer() {
        drawer = (DrawerLayout) findViewById(R.id.main_screen_drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setUpMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.main_screen_map);
        mapFragment.getMapAsync(this);
    }

    private void setUpSearchBox() {
        searchBox = (SearchBox) findViewById(R.id.search_box);
        searchBox.enableVoiceRecognition(this);

        searchBox.setMenuListener(new SearchBox.MenuListener() {
            @Override
            public void onMenuClick() {
                if (!drawer.isDrawerOpen(GravityCompat.START))
                    drawer.openDrawer(GravityCompat.START);
            }
        });

        searchBox.setSearchListener(new SearchBox.SearchListener() {

            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
            }

            @Override
            public void onSearchClosed() {
                //Use this to un-tint the screen
            }

            @Override
            public void onSearchTermChanged(String term) {
                //React to the search term changing
                //Called after it has updated results
            }

            @Override
            public void onSearch(String searchTerm) {
                Toast.makeText(MainScreen.this, searchTerm + " Searched", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResultClick(SearchResult result) {
                //React to a result being clicked
            }

            @Override
            public void onSearchCleared() {
                //Called when the clear button is clicked
            }

        });
    }

    @Override
    public void onMapReady(GoogleMap inputMap) {
        googleMap = inputMap;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                int fineLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

                if (fineLocation == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            FINE_LOCATION_PERMISSION_REQUEST_CODE);
                }
            }
        } else
            hasLocationPermission = true;

        if (hasLocationPermission && locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
            currentLocation = googleMap.getMyLocation();
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng
                    (currentLocation.getLatitude(), currentLocation.getLongitude())));
        }

        googleMap.setBuildingsEnabled(true);
        googleMap.setIndoorEnabled(true);
        googleMap.setMyLocationEnabled(true);
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setIndoorLevelPickerEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setZoomControlsEnabled(true);

        googleMap.setPadding(0, getResources().getDimensionPixelSize(R.dimen.compass_padding), 0, 0);

        googleMap.setOnMyLocationChangeListener(this);
    }


    private void showAlertDialog(String dialogTitle, String dialogMsg,
                                 @Nullable String positiveBtnMsg, @Nullable String negativeBtnMsg,
                                 @Nullable DialogInterface.OnClickListener positiveListener,
                                 @Nullable DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        AlertDialog dialog = builder.setTitle(dialogTitle)
                .setMessage(dialogMsg)
                .setPositiveButton(getString(R.string.positive_button), positiveListener)
                .create();

        dialog.show();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // TODO:
            case R.id.nav_map_normal:
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.nav_map_terrain:
                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.nav_map_hybrid:
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.nav_map_satellite:
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;

            case R.id.nav_settings:
                Intent toSettingsScreen = new Intent(MainScreen.this, SettingsScreen.class);
                startActivity(toSettingsScreen);
                break;
        }

        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.can_access_location))) {
            hasLocationPermission = sharedPreferences.getBoolean(getString(R.string.can_access_location), true);
        }

        if (key.equals(getString(R.string.json_returned))) {
            Log.i("SharedPref", "JSON changed");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_SEARCH_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            searchBox.populateEditText(matches.get(0));
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {      // permission granted
                    Log.i("Permission Granted: ", "ACCESS_FINE_LOCATION");
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                    editor.putBoolean(getString(R.string.can_access_location), true)
                            .apply();
                } else {
                    showAlertDialog(getString(R.string.permission_title), getString(R.string.permission_message),
                            getString(R.string.positive_button), null, null, null);
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMyLocationChange(Location location) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
    }

    @Override
    public void onReceiveResponse(String result) {
        JSONObject data = null;

//        Log.i("HttpResponse", result);
        int maxLogStringSize = 1000;
        for(int i = 0; i <= result.length() / maxLogStringSize; i++) {
            int start = i * maxLogStringSize;
            int end = (i+1) * maxLogStringSize;
            end = end > result.length() ? result.length() : end;
            Log.v("HttpResponse", result.substring(start, end));
        }

        try {
            data = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainScreen.this, getString(R.string.problem_stream), Toast.LENGTH_SHORT).show();
        }


        Toast.makeText(MainScreen.this, "Here", Toast.LENGTH_SHORT).show();
    }
}
