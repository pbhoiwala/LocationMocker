package com.bhoiwala.locationmocker;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;

public class MockLocationProvider {

    private String mProviderName;
    private LocationManager mLocationManager;
    private static float sMockAccuracy = 5;

    public MockLocationProvider(String name, Context ctx) {
        this.mProviderName = name;

        mLocationManager = (LocationManager) ctx
                .getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.addTestProvider(mProviderName, false, false, false,
                false, true, true, true, 0, 5);
        mLocationManager.setTestProviderEnabled(mProviderName, true);
    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void pushLocation(double lat, double lon) {
        Location mockLocation = new Location(mProviderName);
        mockLocation.setLatitude(lat);
        mockLocation.setLongitude(lon);
        mockLocation.setAltitude(0);
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        mockLocation.setAccuracy(sMockAccuracy);
        mLocationManager.setTestProviderLocation(mProviderName, mockLocation);
    }

    public void shutdown() {
        mLocationManager.removeTestProvider(mProviderName);
    }
}
