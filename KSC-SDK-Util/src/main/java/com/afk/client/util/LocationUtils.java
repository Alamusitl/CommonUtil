package com.afk.client.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.afk.permission.PermissionManager;

import java.util.List;

/**
 * Created by ALAMUSI on 2016/8/28.
 */
public class LocationUtils {

    public static double getLongitude(Activity context) {
        Location location = getLocation(context);
        if (location == null) {
            return 0;
        } else {
            return location.getLongitude();
        }
    }

    public static double getLatitude(Activity context) {
        Location location = getLocation(context);
        if (location == null) {
            return 0;
        } else {
            return location.getLatitude();
        }
    }

    private synchronized static Location getLocation(Activity context) {
        String locationProvider;
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) {
            return null;
        }
        List<String> providers = lm.getProviders(true);
        if (providers == null || providers.size() == 0) {
            Logger.d("gps service is closed!");
            return null;
        }
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            Logger.e("get Location: no useless location provider");
            return null;
        }
        if (PermissionManager.getInstance().hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            try {
                return lm.getLastKnownLocation(locationProvider);
            } catch (SecurityException e) {
                return null;
            }
        } else {
            PermissionManager.getInstance().requestPermission(context, null, Manifest.permission.ACCESS_FINE_LOCATION);
        }
        return null;
    }
}
