package com.ksc.client.core.api;

import android.app.Activity;
import android.text.TextUtils;

import com.ksc.client.core.api.callback.GetInitParamCallBack;
import com.ksc.client.core.api.callback.GetOrderCallBack;
import com.ksc.client.core.api.entity.OrderResponse;
import com.ksc.client.core.base.entity.PayInfo;
import com.ksc.client.core.config.KSCSDKInfo;
import com.ksc.client.core.config.KSCStatusCode;
import com.ksc.client.toolbox.HttpError;
import com.ksc.client.toolbox.HttpErrorListener;
import com.ksc.client.toolbox.HttpListener;
import com.ksc.client.toolbox.HttpRequestManager;
import com.ksc.client.toolbox.HttpRequestParam;
import com.ksc.client.toolbox.HttpResponse;
import com.ksc.client.util.KSCLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Alamusi on 2016/7/4.
 */
public class KSCCommonService {

    public static final int K_RESPONSE_OK = 0;
    public static final int K_RESPONSE_FAIL = -1;
    private static AtomicBoolean mIsCreatingOrder = new AtomicBoolean(false);
    private static long mLastTime = System.currentTimeMillis();

    public static void getInitParams(final Activity activity, final String channel, final GetInitParamCallBack callBack) {
        String url = KSCSDKInfo.getInitUrl();
        HttpRequestParam requestParam = new HttpRequestParam(url);
        requestParam.setTimeOutMs(5 * 1000);
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(final HttpResponse response) {
                KSCLog.i("get init param from server success, code :" + response.getCode() + " , data" + response.getBodyString());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onGetParamsResult(K_RESPONSE_OK, response.getBodyString());
                    }
                });
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                KSCLog.w("get init param from server fail, error: " + error.getMessage());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String param = KSCSDKInfo.getChannelParam();
                        if (TextUtils.isEmpty(param)) {
                            callBack.onGetParamsResult(K_RESPONSE_FAIL, "get channel param error");
                        } else {
                            callBack.onGetParamsResult(K_RESPONSE_OK, param);
                        }
                    }
                });
            }
        });
    }

    public static void createOrder(final Activity activity, final String channel, final PayInfo payInfo, final GetOrderCallBack callBack) {
        long currentTime = System.currentTimeMillis();
        if (mIsCreatingOrder.get() && (currentTime - mLastTime <= 2000)) {
            callBack.onCreateOrderResult(KSCStatusCode.PAY_FAILED_REPEAT, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_FAILED_REPEAT), null);
            return;
        }
        mIsCreatingOrder.compareAndSet(false, true);
        mLastTime = System.currentTimeMillis();
        String url = KSCSDKInfo.getCreateOrderUrl();
        final HttpRequestParam requestParam = new HttpRequestParam(url);
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(final HttpResponse response) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.getCode() != HttpURLConnection.HTTP_OK) {
                            callBack.onCreateOrderResult(K_RESPONSE_FAIL, "server response code is :" + response.getCode(), null);
                            return;
                        }
                        try {
                            JSONObject data = new JSONObject(response.getBodyString());
                            OrderResponse orderResponse = new OrderResponse();
                            orderResponse.setKscOrder(data.optString("order"));
                            orderResponse.setAmount(data.optString("amount"));
                            orderResponse.setProductId(data.optString("productId"));
                            orderResponse.setProductName(data.optString("productName"));
                            orderResponse.setProductDesc(data.optString("productDesc"));
                            orderResponse.setCustomInfo(data.optString("customInfo"));
                            orderResponse.setSubmitTime(data.optString("submitTime"));
                            orderResponse.setSign(data.optString("sign"));
                            callBack.onCreateOrderResult(K_RESPONSE_OK, "get order response success.", orderResponse);
                        } catch (JSONException e) {
                            KSCLog.e("JSON Format order response error", e);
                        }
                    }
                });
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                KSCLog.e("get ksc order fail, error: " + error.getMessage());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onCreateOrderResult(K_RESPONSE_FAIL, "get ksc order fail", null);
                    }
                });
            }
        });
    }

    public static void getUpdateInfo(Activity activity, String channel) {

    }
}
