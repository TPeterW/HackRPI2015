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
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
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
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.peter.roadtip.utils.JSONOperator;
import com.peter.roadtip.utils.LocationService;
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
        OnNavigationItemSelectedListener, ResponseListener {

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
    private boolean googleServiceAvailable;

    // The Preferences
    private boolean giveSuggestions;

    // Request Codes
    private final static int FINE_LOCATION_PERMISSION_REQUEST_CODE = 0x001;
    private final static int VOICE_SEARCH_REQUEST_CODE = 1234;

    private final static int PLACE_PICKER_REQUEST_CODE = 0x201;

//    private final static int JSON_GET_LAT_REQUEST_CODE = 0x101;
//    private final static int JSON_GET_LNG_REQUEST_CODE = 0x102;
//    private final static int JSON_GET_POSTAL_REQUEST_CODE = 0x103;
//    private final static int JSON_GET_NAME_REQUEST_CODE = 0x104;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        initData();
        
        initDrawer();

        setUpMap();

        setUpSearchBox();

        Log.i("LifeCycle", "onStart");
    }

    private void initData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        giveSuggestions = sharedPreferences.getBoolean(getString(R.string.pref_give_suggestions), true);


        switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext())) {
            case ConnectionResult.SUCCESS:
                googleServiceAvailable = true;
                break;

            case ConnectionResult.API_UNAVAILABLE:
                //TODO: only show this message once
                googleServiceAvailable = false;
                Toast.makeText(MainScreen.this, getString(R.string.svcs_unavail), Toast.LENGTH_SHORT).show();
                break;

            case ConnectionResult.SERVICE_DISABLED:
                googleServiceAvailable = false;
                Toast.makeText(MainScreen.this, getString(R.string.svcs_disabled), Toast.LENGTH_SHORT).show();
                break;

            case ConnectionResult.SERVICE_MISSING:
                googleServiceAvailable = false;
                Toast.makeText(MainScreen.this, getString(R.string.svcs_missing), Toast.LENGTH_SHORT).show();
                break;

            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                googleServiceAvailable = false;
                Toast.makeText(MainScreen.this, getString(R.string.svcs_req_update), Toast.LENGTH_SHORT).show();
                break;

            default:
                googleServiceAvailable = false;
                Toast.makeText(MainScreen.this, getString(R.string.svcs_other), Toast.LENGTH_SHORT).show();
                break;
        }

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

        for (int i = 0; i < 4; i++) {
            SearchResult option = new SearchResult("Result " + Integer.toString(i),
                    ContextCompat.getDrawable(this, R.mipmap.ic_launcher));
            searchBox.addSearchable(option);
        }
        // the last item is always the place picker
        searchBox.addSearchable(new SearchResult(getString(R.string.use_place_picker), ContextCompat.getDrawable(this, R.drawable.ic_google_2015)));

        searchBox.setSearchListener(new SearchBox.SearchListener() {

            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
                Log.i("Search", "SearchOpened");
            }

            @Override
            public void onSearchClosed() {
                //Use this to un-tint the screen
                Log.i("Search", "SearchClosed");
            }

            @Override
            public void onSearchTermChanged(String term) {
                //React to the search term changing
                //Called after it has updated results
                Log.i("Search", "SearchTermChanged");
            }

            @Override
            public void onSearch(String searchTerm) {
                Toast.makeText(MainScreen.this, searchTerm + " Searched", Toast.LENGTH_LONG).show();
                Log.i("Search", searchTerm + " Searched");
            }

            @Override
            public void onResultClick(SearchResult result) {
                //React to a result being clicked
                Log.i("Search", "ResultClick");

                // for place picker
                if (result.title.equals(getString(R.string.use_place_picker))) {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                    try {
                        startActivityForResult(builder.build(MainScreen.this), PLACE_PICKER_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                        Toast.makeText(MainScreen.this, getString(R.string.svcs_repairable), Toast.LENGTH_SHORT).show();
                    }

                    searchBox.populateEditText("");         // clear search box

                    View view = getCurrentFocus();          // hide input method
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (view != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }

            @Override
            public void onSearchCleared() {
                //Called when the clear button is clicked
                Log.i("Search", "SearchCleared");
            }

        });
    }

    @Override
    public void onMapReady(GoogleMap inputMap) {
        Log.i("GoogleMap", "MapReady");

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

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions().draggable(true).flat(false).position(latLng));
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 500, null);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 500);

                return false;
            }
        });

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

        if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
            Log.i("GoogleMap", "LastKnown is not null");
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng
                    (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude(),
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude()), 10), 1000, null);
        }
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
            return;
        }

        if (key.equals(getString(R.string.json_returned))) {
            Log.i("SharedPref", "JSON changed");
            return;
        }

        if (key.equals(getString(R.string.pref_give_suggestions))) {
            Log.i("SharedPref", "Give Suggestion: " + sharedPreferences.getBoolean(getString(R.string.pref_give_suggestions), true));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case VOICE_SEARCH_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> matches = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    searchBox.populateEditText(matches.get(0));
                }
                break;

            case PLACE_PICKER_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(data, this);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                    googleMap.clear();          // clears all markers
                    googleMap.addMarker(new MarkerOptions().draggable(true).flat(false).position(place.getLatLng()));
                }
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

    // initialise background service
    @Override
    protected void onStop() {
        super.onStop();
        Log.i("LifeCycle", "onStop");

        //TODO:
        String dataUrl = "";

        Intent locationService = new Intent(this, LocationService.class);
        locationService.setData(Uri.parse(dataUrl));

        startService(locationService);
    }

    @Override
    public void onReceiveResponse(String result) {
        JSONObject data = null;

        int maxLogStringSize = 1000;
        for(int i = 0; i <= result.length() / maxLogStringSize; i++) {
            int start = i * maxLogStringSize;
            int end = (i+1) * maxLogStringSize;
            end = end > result.length() ? result.length() : end;
            Log.i("HttpResponse", result.substring(start, end));
        }

        try {
            data = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainScreen.this, getString(R.string.problem_stream), Toast.LENGTH_SHORT).show();
        }



        Toast.makeText(MainScreen.this, "Here", Toast.LENGTH_SHORT).show();
    }





    // Methods below are for debugging


    @Override
    protected void onStart() {
        super.onStart();
        Log.i("LifeCycle", "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("LifeCycle", "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("LifeCycle", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("LifeCycle", "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("LifeCycle", "onDestroy");
    }
}
