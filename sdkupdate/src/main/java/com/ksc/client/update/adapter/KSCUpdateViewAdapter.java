package com.ksc.client.update.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ksc.client.update.entity.KSCUpdateInfo;

import java.util.ArrayList;

/**
 * Created by Alamusi on 2016/8/2.
 */
public class KSCUpdateViewAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<KSCUpdateInfo> mList;
    private onUpdateItemChangeListener mListener;

    public KSCUpdateViewAdapter(Context context, ArrayList<KSCUpdateInfo> updateInfoList, onUpdateItemChangeListener listener) {
        mContext = context;
        mList = updateInfoList;
        mListener = listener;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            SingleUpdateView singleView = new SingleUpdateView(mContext);
            singleView.setData(i);
            view = singleView;
        }
        return view;
    }

    public interface onUpdateItemChangeListener {
        void onChanged(int index, boolean isUpdate);
    }

    private class SingleUpdateView extends LinearLayout {
        private TextView mTvUpdateMsg;
        private CheckBox mCbForceState;
        private int mIndex;

        public SingleUpdateView(Context context) {
            super(context);
            setOrientation(LinearLayout.HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            mTvUpdateMsg = new TextView(context);
            mTvUpdateMsg.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            mCbForceState = new CheckBox(context);
            mCbForceState.setChecked(true);
            mCbForceState.setGravity(Gravity.CENTER);

            LayoutParams stateParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            mCbForceState.setLayoutParams(stateParams);
            addView(mCbForceState, stateParams);

            LayoutParams msgParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            msgParams.leftMargin = 10;
            msgParams.rightMargin = 10;
            msgParams.topMargin = 5;
            msgParams.bottomMargin = 5;
            addView(mTvUpdateMsg, msgParams);
        }

        public void setData(int index) {
            mIndex = index;
            mTvUpdateMsg.setText(mList.get(index).getUpdateMsg());
            mTvUpdateMsg.setVisibility(VISIBLE);
            if (mList.get(index).getIsForce()) {
                mCbForceState.setVisibility(INVISIBLE);
            }
            mCbForceState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    mListener.onChanged(mIndex, b);
                }
            });
        }
    }
}
