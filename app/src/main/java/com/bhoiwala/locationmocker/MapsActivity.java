package com.bhoiwala.locationmocker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
//import com.google.android.gms.location.LocationListener;
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

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MapsActivityOld.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private FloatingActionButton myLoc;
    private FloatingActionButton startFaking;
    PlaceAutocompleteFragment autocompleteFragment;
    private Location fakeLocation;
    private TextView warning;
    private Boolean isMocking = false;
    public float FAKE_ACCURACY = (float) 3.0f;
    private Location mCurrentLocation;
    private GoogleApiClient mGoogleApiClient;
    private final LatLng mDefaultLocation = new LatLng(40.1663282, -75.3901519);
    private static final int DEFAULT_ZOOM = 18;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
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
        setContentView(R.layout.activity_maps);
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        // Build the Play services client for use by the Fused Location Provider and the Places API.

    }

    private synchronized void buildGoogleApiClient() {
        Log.e("~~~~~", "*** buildGoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();


//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this /* FragmentActivity */,
//                        this /* OnConnectionFailedListener */)
//                .addConnectionCallbacks(this)
//                .addApi(LocationServices.API)
//                .addApi(Places.GEO_DATA_API)
//                .addApi(Places.PLACE_DETECTION_API)
//                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.e("~~~~~", "*** onConnected");
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("~~~~~", "*** onConnectionSuspended");
        Log.d(TAG, "Play services connection suspended");
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


    @Override
    protected void onResume() {
        Log.e("~~~~~", "*** onResume");
        super.onResume();

    }

    @Override
    protected void onPause() {
        Log.e("~~~~~", "*** onPause");
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.e("~~~~~", "*** onSavedInstanceState");
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mCurrentLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.e("~~~~~", "*** onMapReady");
        mMap = map;
        myLoc = (FloatingActionButton) findViewById(R.id.find_my_location);
        fakeLocation = new Location("");
        startFaking = (FloatingActionButton) findViewById(R.id.start_faking);
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

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

        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mCurrentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
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
                mMap.addMarker(markerOptions);
                fakeLocation.setLatitude(point.latitude);
                fakeLocation.setLongitude(point.longitude);
                fakeLocation.setAccuracy(FAKE_ACCURACY);
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


        startFaking.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) throws SecurityException { // SecurityException will be thrown when MOCK Location is disabled
                startFakingLocation();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void startFakingLocation2() {
        MockLocationProvider mock;
        mock = new MockLocationProvider(LocationManager.GPS_PROVIDER, this);
        mock.pushLocation(fakeLocation.getLatitude(), fakeLocation.getLongitude());
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        LocationListener lis = new LocationListener() {
//            @Override
//            public void onLocationChanged(Location location) {
//                Log.e(">>>>>", "new location - " + location.getLatitude() + ", " + location.getLongitude());
//            }
//        };
        toast("Mocking begins");
        mCurrentLocation = fakeLocation;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, (android.location.LocationListener) lis);
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
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void startFakingLocation3() {
        LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true);
        Location newLocation = new Location("network");
        newLocation.setLatitude(fakeLocation.getLatitude());
        newLocation.setLongitude(fakeLocation.getLongitude());
        newLocation.setAccuracy(FAKE_ACCURACY);
        newLocation.setTime(System.currentTimeMillis());
        newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient, newLocation);
        toast("mocking begins");
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


    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void startFakingLocation() {
        try {

            final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            final String provider = LocationManager.GPS_PROVIDER;
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

            Timer t = new Timer();
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    newLocation.setTime(System.currentTimeMillis());
                    newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                    lm.setTestProviderLocation(provider, newLocation);
                }
            },0,2000);

//            lm.setTestProviderEnabled(provider, true);
//            lm.setTestProviderStatus(provider,
//                    LocationProvider.AVAILABLE,
//                    null, System.currentTimeMillis());
//            lm.setTestProviderLocation(provider, newLocation);

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
    public void toast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

//    @Override
//    public void onStop(){
//        super.onStop();
//        final Handler h = new Handler();
//        final int delay = 2500;
//        h.postDelayed(new Runnable(){
//            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
//            public void run(){
////                toast("doing something");
//                startFakingLocation3();
//                h.postDelayed(this, delay);
//            }
//        },delay);
//    }



}


