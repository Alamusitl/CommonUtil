package com.ksc.client.util;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * 软键盘辅助类
 * Created by Alamusi on 2016/9/29.
 */
public class KSCKeyBoardUtils {

    /**
     * 打卡软键盘
     *
     * @param context  上下文
     * @param editText 输入框
     */
    public static void openKeybord(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * 关闭软键盘
     *
     * @param context  上下文
     * @param editText 输入框
     */
    public static void closeKeybord(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}
