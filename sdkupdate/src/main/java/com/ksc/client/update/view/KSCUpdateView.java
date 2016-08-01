package com.ksc.client.update.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

import com.ksc.client.update.KSCUpdate;
import com.ksc.client.update.entity.KSCUpdateInfo;

import java.util.ArrayList;

/**
 * Created by Alamusi on 2016/7/27.
 */
public class KSCUpdateView {

    private static ProgressDialog mProgressDialog;

    public static void showUpdatePrompt(final Context context, final boolean isForceUpdate, final String msg, final Handler handler) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                final AlertDialog alertDialog = new AlertDialog.Builder(context).setMessage(msg).create();
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        handler.handleMessage(handler.obtainMessage(KSCUpdate.EVENT_UPDATE_START));
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
        });
    }

    public static void showUpdatePrompt(Context context, ArrayList<KSCUpdateInfo> updateInfoList, Handler handler) {

    }

    public static void showUpdateProgress(final Context context, final boolean isForceUpdate, final Handler handler) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.cancel();
                    mProgressDialog = null;
                }
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "后台下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mProgressDialog.hide();
                        handler.handleMessage(handler.obtainMessage(KSCUpdate.EVENT_UPDATE_DOWNLOAD_BACKGROUND));
                    }
                });
                if (!isForceUpdate) {
                    mProgressDialog.setCancelable(true);
                    mProgressDialog.setCanceledOnTouchOutside(true);
                    mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "取消更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mProgressDialog.cancel();
                            handler.handleMessage(handler.obtainMessage(KSCUpdate.EVENT_UPDATE_DOWNLOAD_STOP));
                        }
                    });
                } else {
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setCanceledOnTouchOutside(false);
                }
                mProgressDialog.show();
            }
        });
    }

    public static void updateProcessHide() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.cancel();
                }
            }
        });
    }

    public static void updateProgress(final int current, final int total) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                int present = (int) ((current * 100) / (double) total);
                if (current == total) {
                    mProgressDialog.cancel();
                } else {
                    mProgressDialog.setMax(total);
                    mProgressDialog.setProgress(current);
                }
            }
        });
    }
}
