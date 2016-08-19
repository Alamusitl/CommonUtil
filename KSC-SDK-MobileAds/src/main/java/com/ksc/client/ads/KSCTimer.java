package com.ksc.client.ads;

import android.os.Handler;
import android.os.Message;

import java.util.TimerTask;

/**
 * Created by Alamusi on 2016/8/19.
 */
public class KSCTimer extends TimerTask {

    private Handler mHandler;

    public KSCTimer(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void run() {
        if (mHandler != null) {
            Message message = mHandler.obtainMessage();
            message.what = 1;
            mHandler.sendMessage(message);
        }
    }
}
