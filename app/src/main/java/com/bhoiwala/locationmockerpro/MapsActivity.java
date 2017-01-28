package com.bhoiwala.locationmockerpro;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.bhoiwala.locationmocker.realm.Favorites;
import com.bhoiwala.locationmockerpro.realm.MyLocation;
//import com.bhoiwala.locationmocker.realm.Recent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import android.location.LocationListener;

import com.google.android.gms.common.api.Status;
//import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static com.bhoiwala.locationmockerpro.R.id.placeName;

public class MapsActivity extends FragmentActivity implements /*LocationListener,*/ OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private FloatingActionButton myLocationButton;
    private FloatingActionButton startFakingButton;
    PlaceAutocompleteFragment autocompleteFragment;
    private Location droppedMarker = null;
    private Boolean isMocking = false;
    public float FAKE_ACCURACY = (float) 3.0f;
    private Realm realm;

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;
    // A request object to store parameters for requests to the FusedLocationProviderApi.
    private LocationRequest mLocationRequest;
    // The desired interval for location updates. Inexact. Updates may be more or less frequent.
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    // The fastest rate for active location updates. Exact. Updates will never be more frequent
    // than this value.
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // A default location (New York City) and default zoom to use when location permission is
    // not granted.
    private CameraPosition oldCameraPosition;
    private final LatLng mDefaultLocation = new LatLng(40.730610, -73.935242);
    private static final int DEFAULT_ZOOM = 18;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located.
    private Location mCurrentLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_SEARCH_BAR = "search";
    private static final String KEY_DROPPED_PIN = "dropped";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    // Tools for navigation drawer
    protected DrawerLayout drawer;
    protected Toolbar toolbar;
    protected ActionBarDrawerToggle toggle;
    protected NavigationView navigationView;
    private Boolean isSatellite = false;
    private ImageView addFav;
    private EditText searchBar = null;
    private String searchBarText = "";
    // Tools for navigation drawer


    // Location Listener when mocking location (basically useless)
    private LocationListener lis = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {}
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}
        @Override
        public void onProviderEnabled(String s) {}
        @Override
        public void onProviderDisabled(String s) {}
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // Retrieve location and camera position from saved instance state.

        if (savedInstanceState != null) {
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
            searchBarText = savedInstanceState.getString(KEY_SEARCH_BAR);
            droppedMarker = savedInstanceState.getParcelable(KEY_DROPPED_PIN);
        }

        // Retrieve the content view that renders the map.
        // setContentView(R.layout.activity_maps);  use this when not using navigation drawer
        // activity_main implements navigation drawer
        setContentView(R.layout.activity_main);
        ViewStub stub = (ViewStub)findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.activity_maps);
        View inflated = stub.inflate();

        // Build the Play services client for use by the Fused Location Provider and the Places API.
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        // Initialize DB
        Realm.init(this);
        realm = Realm.getDefaultInstance();

        // Initialize tools for navigation drawer
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        addFav = (ImageView)findViewById(R.id.addToFavorite);
        refreshFavoriteButton();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void goToLocation(MyLocation goToLocation) {
        clearMap();
        myLocationButton.setImageResource(R.mipmap.ic_crosshairs_gps_grey600_24dp);
        searchBarText = goToLocation.placeName;
        autocompleteFragment.setText(searchBarText);
        LatLng latLng = new LatLng(goToLocation.latitude, goToLocation.longitude);
        mMap.addMarker(new MarkerOptions().position(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        prepareFakeLocation(latLng);
        refreshFavoriteButton();
    }

    /**
     * Checks if "Mock Locations" is enabled(true) or disabled(false) in Developer Settings
     * TODO: If it is disabled, ask user if they want to enable it
     */
    public boolean isMockLocationEnabled() {
        boolean isMockEnabled = false;
        try {
            // if marshmallow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager opsManager = (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
                isMockEnabled = (opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), BuildConfig.APPLICATION_ID) == AppOpsManager.MODE_ALLOWED);
            } else {
                // in marshmallow this will always return true
                isMockEnabled = !android.provider.Settings.Secure.getString(this.getContentResolver(), "mock_location").equals("0");
            }
        } catch (Exception e) {
            return isMockEnabled;
        }
        return isMockEnabled;
    }


    /**
     * Get the device location and nearby places when the activity is restored after a pause.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            getDeviceLocation();
        }
    }

    /**
     * Pause activity
     * Stop location updates when the activity is no longer in focus, to reduce battery consumption.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(mMap != null) {
            mCameraPosition = mMap.getCameraPosition();
        }
        /*if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }*/
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mCameraPosition);
            outState.putParcelable(KEY_LOCATION, mCurrentLocation);
            outState.putString(KEY_SEARCH_BAR, searchBarText);
            outState.putParcelable(KEY_DROPPED_PIN, droppedMarker);
            setCameraPosition();
        }
    }

    /**
     * Gets the device's current location and builds the map
     * when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        getDeviceLocation();
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Handles failure to connect to the Google Play services client.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    /**
     * Handles suspension of the connection to the Google Play services client.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.e("~~~~~", "*** onConnectionSuspended");
        Log.d(TAG, "Play services connection suspended");
    }

    /**
     * Handles back button press. If navigation drawer is open, then close the app
     * otherwise do regular back press function.
     */
    @Override
    public void onBackPressed(){
        if(drawer.isDrawerOpen(Gravity.LEFT)){
            drawer.closeDrawer(Gravity.LEFT);
        }else {
//            super.onBackPressed();
            this.finish();
        }
    }


    /**
     * Handles the callback when location changes.
     */
    /*@Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        // LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }*/

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onMapReady(GoogleMap map) {
        // Checks to see if Call is made from a different activity
        // Initialize location manager and location provider
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final String provider = LocationManager.GPS_PROVIDER;

        // Initialize main display elements of the screen including map, buttons and search bar
        mMap = map;
        myLocationButton = (FloatingActionButton) findViewById(R.id.find_my_location);
        startFakingButton = (FloatingActionButton) findViewById(R.id.start_faking);

        addFav = (ImageView) findViewById(R.id.addToFavorite);
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        //-----------

        // Modify search bar
        autocompleteFragment.setHint("Search here");
        searchBar = ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input));
        searchBar.setTextColor(Color.parseColor(MyStrings.GRAY));


        // Initialize Navigation Drawer
        ImageView navDrawer = (ImageView)((LinearLayout)autocompleteFragment.getView()).getChildAt(0);
        navDrawer.setColorFilter(Color.parseColor(MyStrings.DARK_GRAY));
        navDrawer.setImageDrawable(getDrawable(R.mipmap.ic_menu_black_24dp));
        navDrawer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });

        ImageView clearButton = (ImageView)((LinearLayout)autocompleteFragment.getView()).getChildAt(2);
        clearButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clearMap();
            }
        });

        refreshFavoriteButton();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) { return null; }
            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());
                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());
                return infoWindow;
            }
        });

        /*
         * Set the map's camera position to the current location of the device.
         * If the previous state was saved, set the position to the saved state.
         * If the current location is unknown, use a default position and zoom value.
         */
        getOldCameraPosition();
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mCurrentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude()), DEFAULT_ZOOM));
        } else if (oldCameraPosition != null){
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(oldCameraPosition));
        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 10));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            toast("Using Default");
        }

        /*
         * Drops a marker when Long click is activated on the map
         */
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                mMap.clear();
                myLocationButton.setImageResource(R.mipmap.ic_crosshairs_gps_grey600_24dp);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(point);
                markerOptions.title("Dropped Pin");
                searchBarText = String.valueOf(String.format("%.6g%n",point.latitude)) + "," + String.valueOf(String.format("%.6g%n",point.longitude));
                autocompleteFragment.setText(searchBarText);
                mMap.addMarker(markerOptions);
                prepareFakeLocation(point);
                refreshFavoriteButton();
            }
        });

        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mLocationPermissionGranted) {
                    askForLocationPermission();
                }else{
                    getDeviceLocation();
                    if (mCurrentLocation == null) {
                        snackBarForGPS();
                        updateLocationUI();
                    } else {
                        if(isMocking) {
                            toast("Mocked current location");
                        }else{
                            toast("Real current location");
                        }
                        myLocationButton.setImageResource(R.mipmap.ic_launcher_blue);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mCurrentLocation.getLatitude(),
                                        mCurrentLocation.getLongitude()), DEFAULT_ZOOM));
                    }
                }

            }
        });
        addFav.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(checkIfMarkerExistsInFavorites()){
                    removePlaceFromFavorites();
                }else{
                    askForName();
                }
            }
        });


      autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mMap.clear();
                myLocationButton.setImageResource(R.mipmap.ic_crosshairs_gps_grey600_24dp);
                searchBarText = place.getName().toString();
                LatLng point = place.getLatLng();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(point);
                markerOptions.title(searchBarText);
                mMap.addMarker(markerOptions);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, DEFAULT_ZOOM));
                prepareFakeLocation(point);
                refreshFavoriteButton();
            }
            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), DEFAULT_ZOOM));
                toast("Press Green button to start faking location");
                return true;
            }
        });

        startFakingButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            // SecurityException will be thrown when MOCK Location is disabled
            public void onClick(View view) throws SecurityException {
                if(!isMocking && droppedMarker != null){
                    startFakingLocation(lm, provider);
                }else if(isMocking){
                    stopFakingLocation(lm, provider);
                }else{
                    toast("Please choose a location to mock");
                }
                setupStartStopButton();
            }
        });
        snackBarForMockSetting();
        isCallFromDifferentActivity();

    }

    private void setCameraPosition(){
        CameraPosition cameraPosition = mMap.getCameraPosition();
        SharedPreferences.Editor editor = getSharedPreferences(MyStrings.cpFile, MODE_PRIVATE).edit();
        editor.putString(KEY_CAMERA_POSITION, cameraPosition.toString());
        editor.putFloat(MyStrings.cpLATI, (float) cameraPosition.target.latitude);
        editor.putFloat(MyStrings.cpLONG, (float) cameraPosition.target.longitude);
        editor.putFloat(MyStrings.cpZOOM, cameraPosition.zoom);
        editor.putFloat(MyStrings.cpTILT, cameraPosition.tilt);
        editor.putFloat(MyStrings.cpBEAR, cameraPosition.bearing);
        editor.apply();

    }

    private void getOldCameraPosition(){
        SharedPreferences prefs = getSharedPreferences(MyStrings.cpFile, MODE_PRIVATE);
            float lati = prefs.getFloat(MyStrings.cpLATI, 0);
            float longi = prefs.getFloat(MyStrings.cpLONG, 0);
            LatLng target = new LatLng(lati, longi);
            float zoom = prefs.getFloat(MyStrings.cpZOOM, 0);
            float tilt = prefs.getFloat(MyStrings.cpTILT, 0);
            float bearing = prefs.getFloat(MyStrings.cpBEAR, 0);
            oldCameraPosition = new CameraPosition.Builder()
                    .target(target)
                    .zoom(zoom)
                    .tilt(tilt)
                    .bearing(bearing)
                    .build();

    }

    private void snackBarForGPS() {
        Snackbar.make(findViewById(android.R.id.content), "Location services is disabled", Snackbar.LENGTH_LONG)
                .setAction("SETTINGS", new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        Toast.makeText(getApplicationContext(), "Turn on GPS", Toast.LENGTH_LONG).show();
                    }
                })
                .setActionTextColor(Color.RED)
                .show();
    }

    /**
     * Checks Whether this activity is called from a different activity.
     * If so, then 'from_id' is checked which determines what to do.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void isCallFromDifferentActivity(){
        Intent intent = this.getIntent();
        if(intent.hasExtra("mock_status")){
            if(intent.getBooleanExtra("mock_status", false)){
                isMocking = true;
                setupStartStopButton();
            }
        }else if(intent.hasExtra("from_id")){
            if (intent.getStringExtra("from_id").equals("ListViewActivity")) {
                MyLocation goToPlace = new MyLocation();
                goToPlace.placeName = intent.getStringExtra("place_name");
                goToPlace.latitude = intent.getDoubleExtra("place_lati", 0.0);
                goToPlace.longitude = intent.getDoubleExtra("place_long", 0.0);
                goToLocation(goToPlace);
                intent.removeExtra("from_id");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void clearMap() {
        mMap.clear();
        droppedMarker = null;
        searchBarText = "";
        refreshFavoriteButton();
        autocompleteFragment.setText(searchBarText);
    }

    /**
     * If marker is place, it makes 'favorite' button visible
     * If marker location is in DB, button is filled red, else gray border
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void refreshFavoriteButton(){
        if(droppedMarker == null){
            addFav.setVisibility(View.INVISIBLE);
        }else{
            addFav.setVisibility(View.VISIBLE);
            if(checkIfMarkerExistsInFavorites()){
                addFav.setImageDrawable(getDrawable(R.mipmap.ic_favorite_black_24dp));
                addFav.setColorFilter(Color.parseColor(MyStrings.RED));
            }else{
                addFav.setImageDrawable(getDrawable(R.mipmap.ic_favorite_border_black_24dp));
                addFav.setColorFilter(Color.parseColor(MyStrings.DARK_GRAY));
            }
        }
    }

    /**
     * Set's dropped marker's latitude and longitude
     */
    public void prepareFakeLocation(LatLng point){
        droppedMarker = new Location("");
        droppedMarker.setLatitude(point.latitude);
        droppedMarker.setLongitude(point.longitude);
    }

    /**
     * Gets called when user clicks 'heart' button.
     * Asks for placeName and saves the dropped marker location to favorites
     */
    public void askForName(){
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog_favorite_name, null);
        final EditText favPlaceName = (EditText) promptsView.findViewById(placeName);
        favPlaceName.setText(searchBarText);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    public void onClick(DialogInterface dialog, int id) {
                        String placeName = favPlaceName.getText().toString();
                        forceCloseKeyboard(favPlaceName);
                        if(placeName.equals("")){toast("Name cannot be blank");}
                        else{
                            addPlaceToFavoritesDB(placeName);
                            refreshFavoriteButton();
                        }
                    }})
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        forceCloseKeyboard(favPlaceName);
                        dialog.cancel();
                    }});
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Removes favorite place from favorites DB
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void removePlaceFromFavorites(){
//        RealmResults<Favorites> favorite = realm.where(Favorites.class).equalTo("latitude", droppedMarker.getLatitude()).equalTo("longitude", droppedMarker.getLongitude()).findAll();
        RealmResults<MyLocation> favorite = realm.where(MyLocation.class).equalTo("id", MyStrings.favID).equalTo("latitude", droppedMarker.getLatitude()).equalTo("longitude", droppedMarker.getLongitude()).findAll();
        realm.beginTransaction();
        favorite.deleteAllFromRealm();
        realm.commitTransaction();
        refreshFavoriteButton();
    }


    /**
     * Checks if 'dropped marker' exists in Favorites DB
     * return True if exists and False otherwise
     * NOTE: There can only be ONE dropped marker at any given time
     */
    public Boolean checkIfMarkerExistsInFavorites(){
//        RealmQuery<Favorites> courses = realm.where(Favorites.class).equalTo("latitude", droppedMarker.getLatitude()).equalTo("longitude", droppedMarker.getLongitude());
        RealmQuery<MyLocation> favorites = realm.where(MyLocation.class).equalTo("id", MyStrings.favID).equalTo("latitude", droppedMarker.getLatitude()).equalTo("longitude", droppedMarker.getLongitude());
        return favorites.count() != 0;
    }

    public Boolean checkIfMarkerExistsInRecent(){
//        RealmQuery<Favorites> courses = realm.where(Favorites.class).equalTo("latitude", droppedMarker.getLatitude()).equalTo("longitude", droppedMarker.getLongitude());
        RealmQuery<MyLocation> recent = realm.where(MyLocation.class).equalTo("id", MyStrings.recID).equalTo("latitude", droppedMarker.getLatitude()).equalTo("longitude", droppedMarker.getLongitude());
        return recent.count() != 0;
    }


    public void addPlaceToFavoritesDB(final String placeName){
        /*realm.beginTransaction();
        final Favorites favPlace = realm.createObject(Favorites.class);
        favPlace.placeName = placeName;
        favPlace.latitude = droppedMarker.getLatitude();
        favPlace.longitude = droppedMarker.getLongitude();
        realm.commitTransaction();*/
        realm.beginTransaction();
        final MyLocation favPlace = realm.createObject(MyLocation.class);
        favPlace.id = MyStrings.favID;
        favPlace.placeName = placeName;
        favPlace.latitude = droppedMarker.getLatitude();
        favPlace.longitude = droppedMarker.getLongitude();
        realm.commitTransaction();
    }

    public void setupStartStopButton(){
        if(isMocking){
            //show red button to stop
            startFakingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MapsActivity.this, R.color.red_tint )));
            startFakingButton.setImageResource(R.mipmap.ic_stop_white_24dp);

        }else{
            //show green button to start
            startFakingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MapsActivity.this, R.color.green_tint )));
            startFakingButton.setImageResource(R.mipmap.ic_play_arrow_white_24dp);
        }
    }

    public void stopFakingLocation(LocationManager lm, String provider){
        lm.removeTestProvider(provider);
        isMocking = false;
        updateLocationUI();
        toast("Location Mocking stopped");
    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void startFakingLocation(final LocationManager lm, final String provider){
        try {
            isMocking = true;
            toast("Location Mocking started");
            lm.requestLocationUpdates(provider, 50, 0, lis);
            lm.addTestProvider(provider,
                    Objects.equals("requiresNetwork", ""),
                    Objects.equals("requiresSatellite", ""),
                    Objects.equals("requiresCell", ""),
                    Objects.equals("hasMonetaryCost", ""),
                    Objects.equals("supportsAltitude", ""),
                    Objects.equals("supportsSpeed", ""),
                    Objects.equals("supportsBearing", ""),
                    Criteria.POWER_LOW,
                    Criteria.ACCURACY_FINE);

            final Location newLocation = new Location(provider);
            newLocation.setLatitude(droppedMarker.getLatitude());
            newLocation.setLongitude(droppedMarker.getLongitude());
            newLocation.setAccuracy(FAKE_ACCURACY);
            newLocation.setTime(System.currentTimeMillis());
            newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            mCurrentLocation = newLocation;
            if(!checkIfMarkerExistsInRecent()) {
                addDroppedMarkerToRecent();
            }
            final Timer t = new Timer();
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(isMocking) {
                        newLocation.setTime(System.currentTimeMillis());
                        newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                        lm.setTestProviderLocation(provider, newLocation);
                    }else{
                        t.cancel();
                    }
                }
            },0,2000);
            /*lm.setTestProviderEnabled(provider, true);
            lm.setTestProviderStatus(provider,
                    LocationProvider.AVAILABLE,
                    null, System.currentTimeMillis());
            lm.setTestProviderLocation(provider, newLocation);*/
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds location details to recent when user starts a Mock
     */
    public void addDroppedMarkerToRecent(){

        realm.beginTransaction();
        final MyLocation mockedLocation = realm.createObject(MyLocation.class);
        mockedLocation.id = MyStrings.recID;
        mockedLocation.placeName = searchBarText;
        mockedLocation.latitude = droppedMarker.getLatitude();
        mockedLocation.longitude = droppedMarker.getLongitude();
        realm.commitTransaction();
    }



    public void snackBarForMockSetting(){
        startFakingButton.setVisibility(View.INVISIBLE);
        if(!isMockLocationEnabled()){
            Snackbar.make(findViewById(android.R.id.content), "Mock location setting is disabled", Snackbar.LENGTH_INDEFINITE)
                    .setAction("SETTINGS", new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                            Toast.makeText(getApplicationContext(), "Allow this app to mock your location", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setActionTextColor(Color.RED)
                    .show();
        }else{
            startFakingButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Builds a GoogleApiClient.
     * Uses the addApi() method to request the Google Places API and the Fused Location Provider.
     */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        createLocationRequest();
    }

    /**
     * Sets up the location request.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Gets the current location of the device
     */
   private void getDeviceLocation() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } /*else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }*/
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         * Also request regular updates about the device location.
         */
        if (mLocationPermissionGranted) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
    }

    /**
     * Asks permission to use user's location.
     */
    public void askForLocationPermission(){
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            if(!mLocationPermissionGranted){
                toast("Allow permission to use location");
            }
    }


    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    @SuppressWarnings("MissingPermission")
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        if (mLocationPermissionGranted) {
            //set this to 'false' to get rid of "blue dot"
            mMap.setMyLocationEnabled(true);
            //set this to 'true' to show default myLocation Button
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mCurrentLocation = null;
        }
    }

    public void toggleMapType(){
        if(!isSatellite){
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }else{
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        isSatellite = !isSatellite;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
//            toast("Home");
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_enterCoordinates){
//            toast("Enter Coordinates");
            searchByCoordinates();
        } else if (id == R.id.nav_howto) {
//            toast("How To");
            Intent intent = new Intent(MapsActivity.this, HowToActivity.class);
            intent.putExtra("from_id", MyStrings.howID);
            intent.putExtra("mock_status", isMocking);
            startActivity(intent);
        } else if (id == R.id.nav_satellite) {
//            toast("Satellite");
            toggleMapType();
        } else if (id == R.id.nav_favorites) {
//            toast("Favorites");
            Intent intent = new Intent(MapsActivity.this, MyListViewActivity.class);
            intent.putExtra("from_id", MyStrings.favID);
            intent.putExtra("mock_status", isMocking);
            startActivity(intent);
        } else if (id == R.id.nav_recent) {
//            toast("Recent");
            Intent intent = new Intent(MapsActivity.this, MyListViewActivity.class);
            intent.putExtra("from_id", MyStrings.recID);
            intent.putExtra("mock_status", isMocking);
            startActivity(intent);
        } else if (id == R.id.nav_rate) {
//            toast("Rate");
            Intent rate = new Intent(Intent.ACTION_VIEW, Uri.parse(MyStrings.RATE_URL));
            startActivity(rate);
        } else if (id == R.id.nav_share) {
//            toast("Sharing");
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Location Mocker");
            i.putExtra(Intent.EXTRA_TEXT, MyStrings.SHARE_MSG);
            startActivity(Intent.createChooser(i, "Share via"));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void searchByCoordinates() {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog_coordinates, null);
        final EditText coordinates = (EditText) promptsView.findViewById(R.id.coordinateValue);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            MyLocation corLocation = new MyLocation();
                            corLocation.placeName = coordinates.getText().toString();
                            String[] latlng = corLocation.placeName.split(",");
                            corLocation.latitude = Double.parseDouble(latlng[0]);
                            corLocation.longitude = Double.parseDouble(latlng[1]);
                            goToLocation(corLocation);
                        }catch (Exception e){
                            e.printStackTrace();
                            toast("Invalid Coordinates");
                        }
                    }})
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        forceCloseKeyboard(coordinates);
                        dialog.cancel();
                    }});
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void toast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void startMockUsingFusedProvider() {
        try {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            lm.addTestProvider(LocationManager.GPS_PROVIDER,
                    Objects.equals("requiresNetwork", ""),
                    Objects.equals("requiresSatellite", ""),
                    Objects.equals("requiresCell", ""),
                    Objects.equals("hasMonetaryCost", ""),
                    Objects.equals("supportsAltitude", ""),
                    Objects.equals("supportsSpeed", ""),
                    Objects.equals("supportsBearing", ""),
                    Criteria.POWER_LOW,
                    Criteria.ACCURACY_FINE);

            Location newLocation = new Location("fused");
            newLocation.setLatitude(droppedMarker.getLatitude());
            newLocation.setLongitude(droppedMarker.getLongitude());
            newLocation.setAccuracy(FAKE_ACCURACY);
            newLocation.setTime(System.currentTimeMillis());
            newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true);
            LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient, newLocation);
            isMocking = true;
            mCurrentLocation = newLocation;
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            toast("Please enable MOCK Location for this app in settings");
        }
    }

    public void forceCloseKeyboard(EditText editText) {
        editText.setCursorVisible(false);
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}