package com.ksc.client.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import java.util.List;

/**
 * Created by ALAMUSI on 2016/8/28.
 */
public class KSCLocationUtils {

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
            KSCLog.d("gps service is closed!");
            return null;
        }
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            KSCLog.e("get Location: no useless location provider");
            return null;
        }
        if (KSCPermissionUtils.checkRequestPermission(context, Manifest.permission_group.LOCATION, KSCPermissionUtils.REQUEST_PERMISSION_CODE)) {
            return lm.getLastKnownLocation(locationProvider);
        } else {
            return null;
        }
    }
}
