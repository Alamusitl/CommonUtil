package com.ksc.client.core.api.callback;

import com.ksc.client.core.api.entity.OrderResponse;

/**
 * Created by Alamusi on 2016/6/27.
 */
public interface GetOrderCallBack {
    void onCreateOrderResult(int code, String msg, OrderResponse response);
}
