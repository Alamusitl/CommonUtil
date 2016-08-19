package com.ksc.client.ads.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Alamusi on 2016/8/19.
 */
public class KSCCountDownView extends TextView {

    public KSCCountDownView(Context context) {
        this(context, null);
    }

    public KSCCountDownView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KSCCountDownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}