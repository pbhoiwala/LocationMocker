package com.bhoiwala.locationmocker;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class MapsActivityOld extends FragmentActivity implements /*LocationListener,*/ OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MapsActivityOld.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private FloatingActionButton myLoc;
    private FloatingActionButton startFaking;
    PlaceAutocompleteFragment autocompleteFragment;
    private Location fakeLocation;
    private TextView warning;
    private Boolean isMocking = false;
    public float FAKE_ACCURACY = (float) 10.0;


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

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 18;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located.
    private Location mCurrentLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // navigation drawer code below
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private Boolean isSatellite = false;
    private ImageView addFav;
    // navigation drawer code above


    private LocationListener lis = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("~~~~~", "*** onCreate");
        super.onCreate(savedInstanceState);

       // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map.
//        setContentView(R.layout.activity_maps);
        setContentView(R.layout.activity_main);
        // Build the Play services client for use by the Fused Location Provider and the Places API.
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    public boolean isMockLocationEnabled() {
        Log.e("~~~~~", "*** isMockLocationEnabled");
        boolean isMockLocation = false;
        try {
            //if marshmallow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager opsManager = (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
                isMockLocation = (opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), BuildConfig.APPLICATION_ID) == AppOpsManager.MODE_ALLOWED);
            } else {
                // in marshmallow this will always return true
                isMockLocation = !android.provider.Settings.Secure.getString(this.getContentResolver(), "mock_location").equals("0");
            }
        } catch (Exception e) {
            return isMockLocation;
        }

        return isMockLocation;
    }


    /**
     * Get the device location and nearby places when the activity is restored after a pause.
     */
    @Override
    protected void onResume() {
        Log.e("~~~~~", "*** onResume");
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            getDeviceLocation();
        }
//        updateMarkers(); //todo not needed
    }

    /**
     * Stop location updates when the activity is no longer in focus, to reduce battery consumption.
     */
    @Override
    protected void onPause() {
        Log.e("~~~~~", "*** onPause");
        super.onPause();
//        if (mGoogleApiClient.isConnected()) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(
//                    mGoogleApiClient, this);
//        }
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.e("~~~~~", "*** onSavedInstanceState");
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mCurrentLocation);
            super.onSaveInstanceState(outState);
        }
        if (mCurrentLocation != null) {
            Log.e(">>>>> LATI ", String.valueOf(mCurrentLocation.getLatitude()));
            Log.e(">>>>> LONG ", String.valueOf(mCurrentLocation.getLongitude()));
            Log.e(">>>>> ACCU ", String.valueOf(mCurrentLocation.getAccuracy()));
        }
    }

    /**
     * Gets the device's current location and builds the map
     * when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.e("~~~~~", "*** onConnected");
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
        Log.e("~~~~~", "*** onConnectionFailed");
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

    @Override
    public void onBackPressed(){
        if(drawer.isDrawerOpen(Gravity.LEFT)){
            drawer.closeDrawer(Gravity.LEFT);
        }else {
            super.onBackPressed();
        }
    }

    /**
     * Handles the callback when location changes.
     */

//    @Override
//    public void onLocationChanged(Location location) {
//
//
//        Log.e("~~~~~", "*** onLocationChanged - faking is " + isMocking.toString());
//        mCurrentLocation = location;
//        Log.e(">>>>> LATI ", String.valueOf(mCurrentLocation.getLatitude()));
//        Log.e(">>>>> LONG ", String.valueOf(mCurrentLocation.getLongitude()));
//        Log.e(">>>>> ACCU ", String.valueOf(mCurrentLocation.getAccuracy()));
//
////        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//
////        updateMarkers(); //todo not needed
//    }


    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onMapReady(GoogleMap map) {
        Log.e("~~~~~", "*** onMapReady");
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final String provider = LocationManager.GPS_PROVIDER;
        mMap = map;
        myLoc = (FloatingActionButton) findViewById(R.id.find_my_location);
        warning = (TextView)findViewById(R.id.warning);
        fakeLocation = new Location("");
        startFaking = (FloatingActionButton) findViewById(R.id.start_faking);
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextColor(Color.parseColor("#757575"));
        ImageView navDrawer = (ImageView)((LinearLayout)autocompleteFragment.getView()).getChildAt(0);
        navDrawer.setColorFilter(Color.parseColor("#616161"));
        navDrawer.setImageDrawable(getDrawable(R.mipmap.ic_menu_black_24dp));
        navDrawer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });

        addFav = (ImageView) findViewById(R.id.addToFavorite);
        addFav.setVisibility(View.INVISIBLE);
        autocompleteFragment.setHint("Search here");

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Add markers for nearby places.
//        updateMarkers(); //todo not needed

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Log.e("~~~~~", "*** onMapReady - getInfoContents");
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
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mCurrentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
            warning.setText("Warning: Enable Location services");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                // TODO Auto-generated method stub
                mMap.clear();
                myLoc.setImageResource(R.mipmap.ic_crosshairs_gps_grey600_24dp);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(point);
                markerOptions.title("Dropped Pin");

//                mMap.animateCamera(CameraUpdateFactory.newLatLng(point));
                autocompleteFragment.setText(String.valueOf(String.format("%.6g%n",point.latitude)) + "," + String.valueOf(String.format("%.6g%n",point.longitude)));
                mMap.addMarker(markerOptions);
                fakeLocation.setLatitude(point.latitude);
                fakeLocation.setLongitude(point.longitude);
                fakeLocation.setAccuracy(FAKE_ACCURACY);
                addFav.setVisibility(View.VISIBLE);
                addFav.setColorFilter(Color.parseColor("#616161"));
                addFav.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                       askForName();

                    }
                });
            }
        });

        //parth code
        myLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
                if (mCurrentLocation == null) {
                    toast("Please enable location services and try again");
                    updateLocationUI();
                } else {
                    toast("Current location set");
//                    mMap.clear();
                    myLoc.setImageResource(R.mipmap.ic_launcher_blue);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(mCurrentLocation.getLatitude(),
                                    mCurrentLocation.getLongitude()), DEFAULT_ZOOM));
                }
            }
        });



        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mMap.clear();
                myLoc.setImageResource(R.mipmap.ic_crosshairs_gps_grey600_24dp);
                // TODO: Get info about the selected place.
                String placeName = place.getName().toString();
                Log.i(TAG, "Place: " + placeName);
                LatLng latLng = place.getLatLng();
                mMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                fakeLocation.setLatitude(place.getLatLng().latitude);
                fakeLocation.setLongitude(place.getLatLng().longitude);
                fakeLocation.setAccuracy(FAKE_ACCURACY);
            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                toast("Press Green button to start faking location");
                return true;
            }
        });

        /*startFaking.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View view) {
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                criteria.setAccuracy( Criteria.ACCURACY_FINE );
                String mocLocationProvider = lm.getBestProvider( criteria, true );
                if ( mocLocationProvider == null ) {
                    Log.e("ERROR", "No location provider found!");
                    return;
                }
                Location mockLocation = new Location(mocLocationProvider); // a string

                mockLocation.setLatitude(fakeLocation.getLatitude());  // double
                mockLocation.setLongitude(fakeLocation.getLongitude());
                mockLocation.setAccuracy(fakeLocation.getAccuracy());
//                mockLocation.setAltitude(location.getAltitude());
                mockLocation.setTime(System.currentTimeMillis());
                mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                lm.setTestProviderLocation( LocationManager.GPS_PROVIDER, mockLocation);
//                lm.setTestProviderLocation( mocLocationProvider, mockLocation);
            }
        });*/

        startFaking.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) throws SecurityException { // SecurityException will be thrown when MOCK Location is disabled
                if(!isMocking){
                    startFakingLocationNew(lm, provider);
                    showButton();
                }else{
                    stopFakingLocation(lm, provider);
                    showButton();
                }

            }
        });
        warningCheck();
    }

    public void askForName(){
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.favorite_name, null);
        final EditText favPlaceName = (EditText) promptsView.findViewById(R.id.placeName);
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
                            saveNameToDB(placeName);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        forceCloseKeyboard(favPlaceName);
                        dialog.cancel();
                    }
                });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void saveNameToDB(String placeName){
        toast(placeName + " saved to Favorites");
        addFav.setImageDrawable(getDrawable(R.mipmap.ic_favorite_black_24dp));
        addFav.setColorFilter(Color.parseColor("#FF0000"));
    }

    public void forceCloseKeyboard(EditText editText) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public void showButton(){
        if(isMocking){
            //show red button to stop
            startFaking.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MapsActivityOld.this, R.color.red_tint )));
            startFaking.setImageResource(R.mipmap.ic_stop_white_24dp);

        }else{
            //show green button to start
            startFaking.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MapsActivityOld.this, R.color.green_tint )));
            startFaking.setImageResource(R.mipmap.ic_play_arrow_white_24dp);
        }
    }

    public void stopFakingLocation(LocationManager lm, String provider){
        Log.e("<<<<", "going to stop Mocking");
        lm.removeTestProvider(provider);
        isMocking = false;
        updateLocationUI();
    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void startFakingLocationNew(final LocationManager lm, final String provider){
        try {
            isMocking = true;

            toast("Mocking begins");
            Log.e("<<<<", "going to start Mocking");
//            String provider = "fused";
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
//            lm.addTestProvider(LocationManager.GPS_PROVIDER, false, true, false, false, true, true, true, 0, 5);

            final Location newLocation = new Location(provider);
            Log.e("<<<<<", newLocation.getProvider());
            newLocation.setLatitude(fakeLocation.getLatitude());
            newLocation.setLongitude(fakeLocation.getLongitude());
            newLocation.setAccuracy(3.0f);

            Log.e(">>>>> FAKE LATI = ", String.valueOf(newLocation.getLatitude()));
            Log.e(">>>>> FAKE LONG = ", String.valueOf(newLocation.getLongitude()));

            newLocation.setTime(System.currentTimeMillis());
            newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            mCurrentLocation = newLocation;
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

//            lm.setTestProviderEnabled(provider, true);
//            lm.setTestProviderStatus(provider,
//                    LocationProvider.AVAILABLE,
//                    null, System.currentTimeMillis());
//            lm.setTestProviderLocation(provider, newLocation);


            Log.e("~~~~~", " **** Starting to mock now");
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            toast("Please enable MOCK Location for this app in settings");
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void startFakingLocation() {
        try {
            Log.e(">>>>> CURR LATI = ", String.valueOf(mCurrentLocation.getLatitude()));
            Log.e(">>>>> CURR LONG = ", String.valueOf(mCurrentLocation.getLongitude()));

//            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//            lm.addTestProvider(LocationManager.GPS_PROVIDER,
//                    Objects.equals("requiresNetwork", ""),
//                    Objects.equals("requiresSatellite", ""),
//                    Objects.equals("requiresCell", ""),
//                    Objects.equals("hasMonetaryCost", ""),
//                    Objects.equals("supportsAltitude", ""),
//                    Objects.equals("supportsSpeed", ""),
//                    Objects.equals("supportsBearing", ""),
//                    Criteria.POWER_LOW,
//                    Criteria.ACCURACY_FINE);

//            Location newLocation = new Location(LocationManager.GPS_PROVIDER);
            Location newLocation = new Location("fused");
            Log.e("<<<<<", newLocation.getProvider());
            newLocation.setLatitude(fakeLocation.getLatitude());
            newLocation.setLongitude(fakeLocation.getLongitude());
            newLocation.setAccuracy(3.0f);

            Log.e(">>>>> FAKE LATI = ", String.valueOf(newLocation.getLatitude()));
            Log.e(">>>>> FAKE LONG = ", String.valueOf(newLocation.getLongitude()));

            newLocation.setTime(System.currentTimeMillis());
            newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

//            lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
//            lm.setTestProviderStatus(LocationManager.GPS_PROVIDER,
//                    LocationProvider.AVAILABLE,
//                    null, System.currentTimeMillis());
//            lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation);

            /*LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);*/
           LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true);

            LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient, newLocation);
//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
//                    mLocationRequest, this);
            isMocking = true;
            mCurrentLocation = newLocation;
            toast("Mocking begins");
            Log.e("~~~~~", " **** Starting to mock now");
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            toast("Please enable MOCK Location for this app in settings");
        }
    }

    void warningCheck() {
        Log.e("~~~~~", "*** warningCheck");
        Boolean mockingOn = isMockLocationEnabled();
        String war1 = "", war2 = "";
        if (!mLocationPermissionGranted) {
            war1 = "Please allow permission to use location services.";
        } else if (mCurrentLocation == null) {
            war1 = "Please enable location services.";
        }
        if (!mockingOn) {
            war2 = " Please turn on Mock Location.";
//            warning.setText("Warning: Mocking is off");
        }
        if (!war1.equals("") || !war2.equals("")) {
            warning.setVisibility(View.VISIBLE);
            warning.setText(war1 + war2);
            startFaking.setVisibility(View.INVISIBLE);
        } else {
            warning.setVisibility(View.INVISIBLE);
            startFaking.setVisibility(View.VISIBLE);
        }
        warning.setVisibility(View.INVISIBLE); //todo
        startFaking.setVisibility(View.VISIBLE);
    }

    /**
     * Builds a GoogleApiClient.
     * Uses the addApi() method to request the Google Places API and the Fused Location Provider.
     */
    private synchronized void buildGoogleApiClient() {
        Log.e("~~~~~", "*** buildGoogleApiClient");
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
        Log.e("~~~~~", "createLocationRequest");
        mLocationRequest = new LocationRequest();

        /*
         * Sets the desired interval for active location updates. This interval is
         * inexact. You may not receive updates at all if no location sources are available, or
         * you may receive them slower than requested. You may also receive updates faster than
         * requested if other applications are requesting location at a faster interval.
         */
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
//        mLocationRequest.setInterval(3600000);
        /*
         * Sets the fastest rate for active location updates. This interval is exact, and your
         * application will never receive updates faster than this value.
         */
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
//        mLocationRequest.setFastestInterval(3600000);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    /**
     * Gets the current location of the device and starts the location update notifications.
     */

   /* private void getDeviceLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Get Current Location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mCurrentLocation= locationManager.getLastKnownLocation(provider);

    }*/




   private void getDeviceLocation() {
        Log.e("~~~~~", "getDeviceLocation");

        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         * Also request regular updates about the device location.
         */
        if (mLocationPermissionGranted) {
            Log.e("~~~~~", "getDeviceLocation - update");

            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
//                    mLocationRequest, this);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Log.e("~~~~~", "onRequestPermissionsResult");
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
     * Adds markers for places nearby the device and turns the My Location feature on or off,
     * provided location permission has been granted.
     */
//    private void updateMarkers() {
//        if (mMap == null) {
//            return;
//        }
//
//        if (mLocationPermissionGranted) {
//            // Get the businesses and other points of interest located
//            // nearest to the device's current location.
//            @SuppressWarnings("MissingPermission")
//            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
//                    .getCurrentPlace(mGoogleApiClient, null);
//            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
//                @Override
//                public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
//                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
//                        // Add a marker for each place near the device's current location, with an
//                        // info window showing place information.
//                        String attributions = (String) placeLikelihood.getPlace().getAttributions();
//                        String snippet = (String) placeLikelihood.getPlace().getAddress();
//                        if (attributions != null) {
//                            snippet = snippet + "\n" + attributions;
//                        }
//
//                        mMap.addMarker(new MarkerOptions()
//                                .position(placeLikelihood.getPlace().getLatLng())
//                                .title((String) placeLikelihood.getPlace().getName())
//                                .snippet(snippet));
//                    }
//                    // Release the place likelihood buffer.
//                    likelyPlaces.release();
//                }
//            });
//        } else {
//            mMap.addMarker(new MarkerOptions()
//                    .position(mDefaultLocation)
//                    .title("hey title")
//                    .snippet("hey snippet"));
////            mMap.addMarker(new MarkerOptions()
////                    .position(mDefaultLocation)
////                    .title(getString(R.string.default_info_title))
////                    .snippet(getString(R.string.default_info_snippet)));
//        }
//    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    @SuppressWarnings("MissingPermission")
    private void updateLocationUI() {
        Log.e("~~~~~", "updateLocationUI");
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true); //stackoverflow todo
//            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
//            mMap.getUiSettings().setMyLocationButtonEnabled(true);  // un-comment this to display default my location button
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mCurrentLocation = null;
//            warning.setText("Warning: Location permission denied");
        }
    }

    public void toast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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
            toast("Home");
        } else if (id == R.id.nav_howto) {
            toast("How To");
        } else if (id == R.id.nav_satellite) {
            toast("Satellite");
            toggleMapType();
        } else if (id == R.id.nav_favorites) {
            toast("Favorites");
        } else if (id == R.id.nav_recent) {
            toast("Recent");
        } else if (id == R.id.nav_rate) {
            toast("Rate");
        } else if (id == R.id.nav_share) {
            toast("Sharing");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

// // TODO: 12/13/2016 - add ability to change map type from "regular" to "satellite"
//  // TODO: 12/14/2016 - detect when "Location", "Allow mock location" etc. are on or off