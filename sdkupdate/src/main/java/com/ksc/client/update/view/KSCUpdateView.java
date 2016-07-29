package com.ksc.client.update.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;

import com.ksc.client.update.KSCUpdate;
import com.ksc.client.update.entity.KSCUpdateInfo;

import java.util.ArrayList;

/**
 * Created by Alamusi on 2016/7/27.
 */
public class KSCUpdateView {

    private static ProgressDialog mProgressDialog;

    public static void showUpdatePrompt(final Context context, final boolean isForceUpdate, String msg, final Handler handler) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).setMessage(msg).create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                handler.handleMessage(handler.obtainMessage(KSCUpdate.EVENT_UPDATE_DOWNLOADING));
            }
        });
        if (!isForceUpdate) {
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    alertDialog.cancel();
                    handler.handleMessage(handler.obtainMessage(KSCUpdate.EVENT_UPDATE_CANCEL));
                }
            });
        }
        alertDialog.show();
    }

    public static void showUpdatePrompt(Context context, ArrayList<KSCUpdateInfo> updateInfoList, Handler handler) {

    }

    public static void showUpdateProgress(Context context, boolean isForceUpdate, final Handler handler) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "后台下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mProgressDialog.hide();
                handler.handleMessage(handler.obtainMessage(KSCUpdate.EVENT_UPDATE_BACKGROUND));
            }
        });
        if (!isForceUpdate) {
            mProgressDialog.setCancelable(true);
            mProgressDialog.setCanceledOnTouchOutside(true);
            mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "取消更新", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mProgressDialog.cancel();
                    handler.handleMessage(handler.obtainMessage(KSCUpdate.EVENT_UPDATE_STOP_DOWNLOAD));
                }
            });
        } else {
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    public static void updateProcessHide() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }

    public static void updateProgress(int current, int max) {
        if (current == max) {
            mProgressDialog.cancel();
        } else {
            mProgressDialog.setMax(max);
            mProgressDialog.setProgress(current);
        }
    }
}
