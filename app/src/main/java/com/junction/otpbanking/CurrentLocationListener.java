package com.junction.otpbanking;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import static com.junction.otpbanking.AtmDB.atmDB;


public class CurrentLocationListener implements LocationListener {

    private Context context;

    public CurrentLocationListener(Context context) {
        this.context = context;
    }

    @Override
    public void onLocationChanged(Location location) {
        atmDB.setLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
