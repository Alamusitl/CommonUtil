package com.sensorsdata.analytics.android.sdk;

import org.json.JSONArray;

// Will be called from both customer threads and the SensorsDataAPI worker thread.
public class DecideMessages {

    private static final String LOGTAG = "SA.DecideMessages";
    private final VTrack mVTrack;

    public DecideMessages(VTrack vTrack) {
        mVTrack = vTrack;
    }

    public synchronized void reportResults(JSONArray eventBindings) {
        mVTrack.setEventBindings(eventBindings);
    }
}
