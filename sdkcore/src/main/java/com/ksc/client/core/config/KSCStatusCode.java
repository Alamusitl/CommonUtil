package com.ksc.client.core.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alamusi on 2016/6/22.
 */
public class KSCStatusCode {

    /********************
     * 错误码
     ********************/
    public static final int SUCCESS = 200;// 成功
    public static final int INIT_FAIL = 1000;// 初始化失败
    public static final int LOGIN_FAIL = 1100;// 登录失败
    public static final int LOGIN_FAIL_NOT_INIT = 1200;// 登录失败，渠道未初始化或正在初始化
    public static final int LOGIN_CANCEL = 1300;// 取消登录
    public static final int LOGOUT_FAIL = 1400;// 注销失败
    public static final int EXIT_FAIL = 1500;// 退出失败
    public static final int SWITCH_ACCOUNT_FAIL = 1600;// 切换账号失败
    public static final int PAY_FAILED = 2000;// 支付失败
    public static final int PAY_FAILED_CREATE_ORDER_FAILED = 2010;// 创建订单失败
    public static final int PAY_FAILED_PRODUCT_TOTAL_PRICE_INVALID = 2020;// 订单金额错误
    public static final int PAY_FAILED_UID_INVALID = 2030;// UID错误
    public static final int PAY_FAILED_PRODUCT_COUNT_INVALID = 2040;// 产品数量错误
    public static final int PAY_FAILED_EXT_INVALID = 2050;// 扩展信息错误
    public static final int PAY_FAILED_CHANNEL_RESPONSE = 2060;// 渠道反馈的支付失败
    public static final int PAY_FAILED_REPEAT = 2070;//短时间内重复支付(2秒内)
    public static final int PAY_PROGRESS = 2080;//正在支付中
    public static final int PAY_RESULT_UNKNOWN = 2090;//支付结果未知，需要服务端进一步确认
    public static final int PAY_CANCELED = 2100;//支付取消

    /********************
     * 错误码对应的错误信息
     ********************/
    private static final String MSG_SUCCESS = "Success.";
    private static final String MSG_INIT_FAILED = "Init failed";
    private static final String MSG_LOGIN_FAILED = "Login failed";
    private static final String MSG_LOGIN_FAILED_NOT_INIT = "Login failed with not init";
    private static final String MSG_LOGIN_CANCEL = "Login cancel";
    private static final String MSG_LOGOUT_FAILED = "Logout failed";
    private static final String MSG_EXIT_FAILED = "Exit failed";
    private static final String MSG_SWITCH_ACCOUNT_FAIL = "Switch Account failed";
    private static final String MSG_PAY_FAILED = "Pay failed";
    private static final String MSG_PAY_FAILED_CREATE_ORDER_FAILED = "Pay failed : Create order failed";
    private static final String MSG_PAY_FAILED_PRODUCT_TOTAL_PRICE_INVALID = "Pay failed : Product total price invalid";
    private static final String MSG_PAY_FAILED_UID_INVALID = "Pay failed : Uid invalid";
    private static final String MSG_PAY_FAILED_PRODUCT_COUNT_INVALID = "Pay failed : Product count invalid";
    private static final String MSG_PAY_FAILED_EXT_INVALID = "Pay failed : custom info invalid";
    private static final String MSG_PAY_FAILED_CHANNEL_RESPONSE = "Pay failed : channel response";
    private static final String MSG_PAY_FAILED_REPEAT = "Pay failed : order create too frequently";
    private static final String MSG_PAY_PROGRESS = "Pay progress";
    private static final String MSG_PAY_RESULT_UNKOWN = "Pay result unkown";
    private static final String MSG_PAY_CANCELED = "Pay canceled";

    private static Map<String, String> mErrorInfoMap = new HashMap<>();

    static {
        mErrorInfoMap.put(String.valueOf(SUCCESS), MSG_SUCCESS);
        mErrorInfoMap.put(String.valueOf(INIT_FAIL), MSG_INIT_FAILED);
        mErrorInfoMap.put(String.valueOf(LOGIN_FAIL), MSG_LOGIN_FAILED);
        mErrorInfoMap.put(String.valueOf(LOGIN_FAIL_NOT_INIT), MSG_LOGIN_FAILED_NOT_INIT);
        mErrorInfoMap.put(String.valueOf(LOGIN_CANCEL), MSG_LOGIN_CANCEL);
        mErrorInfoMap.put(String.valueOf(LOGOUT_FAIL), MSG_LOGOUT_FAILED);
        mErrorInfoMap.put(String.valueOf(EXIT_FAIL), MSG_EXIT_FAILED);
        mErrorInfoMap.put(String.valueOf(SWITCH_ACCOUNT_FAIL), MSG_SWITCH_ACCOUNT_FAIL);
        mErrorInfoMap.put(String.valueOf(PAY_FAILED), MSG_PAY_FAILED);
        mErrorInfoMap.put(String.valueOf(PAY_FAILED_CREATE_ORDER_FAILED), MSG_PAY_FAILED_CREATE_ORDER_FAILED);
        mErrorInfoMap.put(String.valueOf(PAY_FAILED_PRODUCT_TOTAL_PRICE_INVALID), MSG_PAY_FAILED_PRODUCT_TOTAL_PRICE_INVALID);
        mErrorInfoMap.put(String.valueOf(PAY_FAILED_UID_INVALID), MSG_PAY_FAILED_UID_INVALID);
        mErrorInfoMap.put(String.valueOf(PAY_FAILED_PRODUCT_COUNT_INVALID), MSG_PAY_FAILED_PRODUCT_COUNT_INVALID);
        mErrorInfoMap.put(String.valueOf(PAY_FAILED_EXT_INVALID), MSG_PAY_FAILED_EXT_INVALID);
        mErrorInfoMap.put(String.valueOf(PAY_FAILED_CHANNEL_RESPONSE), MSG_PAY_FAILED_CHANNEL_RESPONSE);
        mErrorInfoMap.put(String.valueOf(PAY_FAILED_REPEAT), MSG_PAY_FAILED_REPEAT);
        mErrorInfoMap.put(String.valueOf(PAY_PROGRESS), MSG_PAY_PROGRESS);
        mErrorInfoMap.put(String.valueOf(PAY_RESULT_UNKNOWN), MSG_PAY_RESULT_UNKOWN);
        mErrorInfoMap.put(String.valueOf(PAY_CANCELED), MSG_PAY_CANCELED);
    }


    public static String getErrorMsg(int code) {
        return mErrorInfoMap.get(String.valueOf(code));
    }
}
