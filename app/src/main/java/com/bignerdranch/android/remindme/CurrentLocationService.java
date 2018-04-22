package com.bignerdranch.android.remindme;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;

/**
 * Created by annika on 2017-08-11.
 */

/**
 * A background service that has a LocationListener. It provides the AppActivity
 * with updates about the position of the device.
 * This Service only runs when there are reminders in store.
 */
public class CurrentLocationService extends Service {

    private MyLocationListener locationListener;
    private LocationManager locationManager;

    /**
     * Sets up the LocationListener and it's LocationManager.
     * It will update every 5 seconds. Location permission is already
     * granted.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        locationListener = new MyLocationListener();
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
    }

    /**
     * Called when the service is started in the Activity.
     * @param intent the intent supplied to the "startServie(intent)".
     * @param flags some additional data.
     * @param startId an id to differ this service from other.
     * @return the semantics the system should use or the current state of the service.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent,flags,startId);
    }

    /**
     * Can return a communication channel for the service,
     * but it is not needed here.
     * @param intent intent containing a caller.
     * @return a communication channel, null here.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Makes sure that the service is stopped when the calling Activity is.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            //noinspection MissingPermission
            locationManager.removeUpdates(locationListener);
        }
    }


    /**
     * A customixed LocationListener. When called by the CurrentLocationService,
     * it will create an Intent, put the current location inside it.
     * The intent will be received by AppActivity using sendBroadcast
     * (AppActivity has got a inner BroadcastReceiver).
     */
    private class MyLocationListener implements LocationListener {

        /**
         * Called when the location has changed.
         * Creates the intent with the current location. Sending it to AppActivity.
         * @param location the current location of the device.
         */
        @Override
        public void onLocationChanged(Location location) {
            Intent intent = new Intent(AppActivity.LOCATION_UPDATE); // nyckelsträng för att hitta rätt intent

            intent.putExtra(AppActivity.LOCATION, location);
            sendBroadcast(intent);
        }

        /**
         *
         * @param s
         */
        @Override
        public void onProviderEnabled(String s) { }

        /**
         *
         * @param provider
         */
        @Override
        public void onProviderDisabled(String provider) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        /**
         *
         * @param s
         * @param i
         * @param b
         */
        @Override
        public void onStatusChanged(String s, int i, Bundle b) { }
    }
}
