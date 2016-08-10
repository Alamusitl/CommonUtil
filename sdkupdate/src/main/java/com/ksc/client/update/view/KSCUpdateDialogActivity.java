package com.ksc.client.update.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.ksc.client.update.KSCUpdateKeyCode;
import com.ksc.client.update.adapter.KSCUpdateViewAdapter;
import com.ksc.client.update.entity.KSCUpdateInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class KSCUpdateDialogActivity extends AppCompatActivity {

    private AlertDialog mDialog;
    private ArrayList<KSCUpdateInfo> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mDialog = new AlertDialog.Builder(this).create();
        mDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        mDialog.setCanceledOnTouchOutside(false);
        Bundle extras = getIntent().getBundleExtra(KSCUpdateKeyCode.KEY_BUNDLE);
        extras.setClassLoader(KSCUpdateInfo.class.getClassLoader());
        mList = extras.getParcelableArrayList(KSCUpdateKeyCode.KEY_LIST);
        showUpdatePromptDialog();
    }

    /**
     * 显示更新提示Dialog
     */
    private void showUpdatePromptDialog() {
        if (mList == null || mList.size() == 0) {
            setResult(RESULT_CANCELED);
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.cancel();
                mDialog = null;
            }
            close();
            return;
        }
        KSCUpdateInfo updateInfo = mList.get(0);
        if (updateInfo.getType().equals(KSCUpdateKeyCode.KEY_FILE_TYPE_FULL) || updateInfo.getType().equals(KSCUpdateKeyCode.KEY_FILE_TYPE_DIFF)) {
            showDialogWithOne(mDialog, updateInfo);
            mList.remove(0);
        } else {
            showDialogWithMore(mDialog, mList);
        }
    }

    /**
     * 显示一个更新请求的Dialog
     *
     * @param dialog 目标Dialog
     * @param info   更新请求信息
     */
    private void showDialogWithOne(final AlertDialog dialog, final KSCUpdateInfo info) {
        dialog.setMessage(info.getUpdateMsg());
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, KSCUpdateKeyCode.KEY_DIALOG_TEXT_CONFIRM, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                ArrayList<KSCUpdateInfo> list = new ArrayList<>();
                list.add(info);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(KSCUpdateKeyCode.KEY_LIST, list);
                intent.putExtra(KSCUpdateKeyCode.KEY_BUNDLE, bundle);
                setResult(RESULT_OK, intent);
                dialog.cancel();
                close();
            }
        });
        if (!info.getIsForce()) {
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, KSCUpdateKeyCode.KEY_DIALOG_TEXT_CANCEL, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    showUpdatePromptDialog();
                }
            });
        } else {
            dialog.setCancelable(false);
        }
        dialog.show();
    }

    /**
     * 显示多个更新请求的Dialog
     *
     * @param dialog 目标Dialog
     * @param list   更新请求的List
     */
    private void showDialogWithMore(final AlertDialog dialog, final ArrayList<KSCUpdateInfo> list) {
        final Map<KSCUpdateInfo, Boolean> updateInfoState = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            updateInfoState.put(list.get(i), true);
        }

        final LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ListView listView = new ListView(this);
        listView.setFadingEdgeLength(0);
        KSCUpdateViewAdapter adapter = new KSCUpdateViewAdapter(this, list, new KSCUpdateViewAdapter.onUpdateItemChangeListener() {

            @Override
            public void onChanged(int index, boolean isUpdate) {
                updateInfoState.put(list.get(index), isUpdate);
            }
        });
        listView.setAdapter(adapter);
        mainLayout.addView(listView);
        dialog.setCancelable(false);
        dialog.setView(mainLayout);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, KSCUpdateKeyCode.KEY_DIALOG_TEXT_CONFIRM, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                ArrayList<KSCUpdateInfo> readList = new ArrayList<>();
                for (KSCUpdateInfo info : updateInfoState.keySet()) {
                    if (updateInfoState.get(info)) {
                        readList.add(info);
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(KSCUpdateKeyCode.KEY_LIST, readList);
                intent.putExtra(KSCUpdateKeyCode.KEY_BUNDLE, bundle);
                setResult(RESULT_OK, intent);
                dialog.cancel();
                close();
            }
        });
    }

    private void close() {
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
