package com.ksc.client.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Process;

import java.util.List;

/**
 * Created by ALAMUSI on 2016/8/28.
 */
public class KSCLocationUtils {

    public static double getLongitude(Context context) {
        Location location = getLocation(context);
        if (location == null) {
            return 0;
        } else {
            return location.getLongitude();
        }
    }

    public static double getLatitude(Context context) {
        Location location = getLocation(context);
        if (location == null) {
            return 0;
        } else {
            return location.getLatitude();
        }
    }

    private synchronized static Location getLocation(Context context) {
        String locationProvider;
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            KSCLog.e("get Location: no useless location provider");
            return null;
        }
        if (context.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, Process.myPid(), Process.myUid()) != PackageManager.PERMISSION_GRANTED && context.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Process.myPid(), Process.myUid()) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(context, new String[], );
            return null;
        }
        return lm.getLastKnownLocation(locationProvider);
    }
}
