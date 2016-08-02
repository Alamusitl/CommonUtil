package com.ksc.client.update.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.ksc.client.update.adapter.KSCUpdateViewAdapter;
import com.ksc.client.update.entity.KSCUpdateInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class KSCUpdateDialogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getIntent().hasExtra("updatePrompt") && getIntent().getBooleanExtra("updatePrompt", false)) {
            showUpdatePromptDialog();
        } else {
            close();
        }
    }

    /**
     * 显示更新提示Dialog
     */
    private void showUpdatePromptDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        dialog.setCanceledOnTouchOutside(false);
        ArrayList<KSCUpdateInfo> list = getIntent().getParcelableArrayListExtra("updateList");
        if (list.size() == 0) {
            setResult(RESULT_CANCELED);
            close();
        } else if (list.size() == 1) {
            showDialogWithOne(dialog, list);
        } else {
            showDialogWithMore(dialog, list);
        }
    }

    /**
     * 显示一个更新请求的Dialog
     *
     * @param dialog 目标Dialog
     * @param list   更新请求的List
     */
    private void showDialogWithOne(final AlertDialog dialog, final ArrayList<KSCUpdateInfo> list) {
        dialog.setMessage(list.get(0).getUpdateMsg());
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra("updateList", list);
                setResult(RESULT_OK, intent);
                dialog.cancel();
                close();
            }
        });
        if (!list.get(0).getIsForce()) {
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    setResult(RESULT_CANCELED);
                    dialog.cancel();
                    close();
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
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                ArrayList<KSCUpdateInfo> readList = new ArrayList<>();
                for (KSCUpdateInfo info : updateInfoState.keySet()) {
                    if (updateInfoState.get(info)) {
                        readList.add(info);
                    }
                }
                intent.putParcelableArrayListExtra("updateList", readList);
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
