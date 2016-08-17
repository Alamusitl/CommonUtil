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
import com.ksc.client.util.KSCHelpUtils;
import com.ksc.client.util.KSCLog;
import com.ksc.client.util.KSCPackageUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Alamusi on 2016/7/4.
 */
public class KSCCommonService {

    private static final int RESPONSE_FAIL = 300;
    private static final int RESPONSE_SUCCESS = 200;
    private static final String AES_KEY = "5dr7WEb2fo20ZF9U";
    private static AtomicBoolean mIsCreatingOrder = new AtomicBoolean(false);

    public static void getInitParams(final Activity activity, final GetInitParamCallBack callBack) {
        String url = KSCSDKInfo.getInitUrl() + "?r=";
        String param = "gameid=" + KSCSDKInfo.getAppId() + "&gameversion=" + KSCPackageUtils.getVersionName(activity) + "&channelid=" +
                KSCSDKInfo.getChannelId() + "&channelversion=" + KSCSDKInfo.getChannelVersion() + "&appkey=" + KSCSDKInfo.getAppKey();
        url += KSCHelpUtils.encodeParam(param, AES_KEY);
        KSCLog.d(url);
        final HttpRequestParam requestParam = new HttpRequestParam(url);
        requestParam.setTimeOutMs(5 * 1000);
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(final HttpResponse response) {
                KSCLog.i("get init param from server success, code :" + response.getCode() + " , data=" + response.getBodyString());
                if (response.getCode() != RESPONSE_SUCCESS) {
                    getLocalParams(activity, callBack);
                    return;
                }
                try {
                    final JSONObject data = new JSONObject(KSCHelpUtils.decodeParam(response.getBodyString(), AES_KEY));
                    int status = data.optInt("status");
                    if (status == RESPONSE_FAIL) {
                        KSCLog.w("get channel param error, status = " + status);
                        getLocalParams(activity, callBack);
                    }
                    if (status == RESPONSE_SUCCESS) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callBack.onGetParamsResult(KSCStatusCode.SUCCESS, data.optString("data"));
                            }
                        });
                    }
                } catch (JSONException e) {
                    KSCLog.e("convert response to json fail.", e);
                    getLocalParams(activity, callBack);
                }
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                KSCLog.w("get init param from server fail, error: " + error.getMessage());
                getLocalParams(activity, callBack);
            }
        });
    }

    private static void getLocalParams(Activity activity, final GetInitParamCallBack callBack) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String param = KSCHelpUtils.decodeParam(KSCSDKInfo.getChannelParam(), AES_KEY);
                if (TextUtils.isEmpty(param)) {
                    callBack.onGetParamsResult(KSCStatusCode.INIT_FAIL, "get channel param error");
                } else {
                    callBack.onGetParamsResult(KSCStatusCode.SUCCESS, param);
                }
            }
        });
    }

    public static void createOrder(final Activity activity, final PayInfo payInfo, final GetOrderCallBack callBack) {
        if (mIsCreatingOrder.get()) {
            callBack.onCreateOrderResult(KSCStatusCode.PAY_FAILED_REPEAT, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_FAILED_REPEAT), null);
            return;
        }
        mIsCreatingOrder.compareAndSet(false, true);
        String url = KSCSDKInfo.getCreateOrderUrl();
        final HttpRequestParam requestParam = new HttpRequestParam(url, HttpRequestParam.METHOD_POST);
        JSONObject param = new JSONObject();
        try {
            param.put("orderid_cp", payInfo.getOrder());
            param.put("channelid", KSCSDKInfo.getChannelId());
            param.put("appid", KSCSDKInfo.getAppId());
            param.put("userid", payInfo.getUid());
            param.put("productid", payInfo.getProductId());
            param.put("productnum", String.valueOf(payInfo.getProductQuantity()));
            param.put("productname", payInfo.getProductName());
            param.put("description", payInfo.getProductDest());
            param.put("price", payInfo.getPrice());
        } catch (JSONException e) {
            mIsCreatingOrder.compareAndSet(true, false);
            callBack.onCreateOrderResult(KSCStatusCode.PAY_FAILED_CREATE_ORDER_FAILED, "request order fail, param format error", null);
            return;
        }
        String paramStr = KSCHelpUtils.encodeParam(param.toString(), AES_KEY);
        requestParam.setBody(paramStr);
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(final HttpResponse response) {
                mIsCreatingOrder.compareAndSet(true, false);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.getCode() != RESPONSE_SUCCESS) {
                            callBack.onCreateOrderResult(KSCStatusCode.PAY_FAILED_CREATE_ORDER_FAILED, "server response code is :" + response.getCode(), null);
                            return;
                        }
                        try {
                            JSONObject result = new JSONObject(KSCHelpUtils.decodeParam(response.getBodyString(), AES_KEY));
                            int status = result.optInt("status");
                            if (status == 200) {
                                JSONObject data = result.optJSONObject("data");
                                OrderResponse orderResponse = new OrderResponse();
                                orderResponse.setKscOrder(data.optString("orderid"));
                                orderResponse.setAmount(data.optString("price"));
                                orderResponse.setProductId(data.optString("productid"));
                                orderResponse.setProductName(data.optString("productname"));
                                orderResponse.setProductDesc(data.optString("description"));
                                orderResponse.setCustomInfo(data.optString("custominfo"));
                                orderResponse.setSubmitTime(data.optString("createtime"));
                                orderResponse.setAccessKey(data.optString("accesskey"));
                                callBack.onCreateOrderResult(KSCStatusCode.SUCCESS, "get order response success.", orderResponse);
                            } else if (status == 201) {
                                callBack.onCreateOrderResult(KSCStatusCode.PAY_FAILED_CREATE_ORDER_FAILED, "order is exist", null);
                            } else {
                                callBack.onCreateOrderResult(KSCStatusCode.PAY_FAILED_CREATE_ORDER_FAILED, "get order fail, status=" + status, null);
                            }
                        } catch (JSONException e) {
                            KSCLog.e("JSON Format order response error", e);
                            callBack.onCreateOrderResult(KSCStatusCode.PAY_FAILED_CREATE_ORDER_FAILED, "JSON Format order response error", null);
                        }
                    }
                });
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(final HttpError error) {
                KSCLog.e("get ksc order fail, error: " + error.getMessage());
                mIsCreatingOrder.compareAndSet(true, false);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onCreateOrderResult(KSCStatusCode.PAY_FAILED_CREATE_ORDER_FAILED, "get ksc order fail, error: " + error.getMessage(), null);
                    }
                });
            }
        });
    }
}
