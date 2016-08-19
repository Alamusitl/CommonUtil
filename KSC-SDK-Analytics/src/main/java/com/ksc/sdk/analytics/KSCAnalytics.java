package com.ksc.sdk.analytics;

import java.util.Map;

/**
 * Created by Alamusi on 2016/7/5.
 */
public class KSCAnalytics {

    private static KSCAnalytics mInstance = null;

    public static KSCAnalytics getInstance() {
        if (mInstance == null) {
            synchronized (KSCAnalytics.class) {
                if (mInstance == null) {
                    mInstance = new KSCAnalytics();
                }
            }
        }
        return mInstance;
    }

    public void init() {

    }

    public void trackEvent(String eventName, String eventValue) {
        trackEvent(eventName, eventValue, false);
    }

    public void trackEvent(String eventName, String eventValue, boolean realTime) {

    }

    public void trackEvent(String eventName, Map<String, String> value) {
        trackEvent(eventName, value, false);
    }

    public void trackEvent(String eventName, Map<String, String> value, boolean realTime) {

    }
}
