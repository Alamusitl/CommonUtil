package com.ksc.screen.record.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ShowCaptureActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnNext;
    private Button mBtnPre;
    private ImageView mIvShowCapture;
    private List<Bitmap> mList = new LinkedList<>();
    private int mCurrentImage;
    private int mImageCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_capture);
        mBtnNext = (Button) findViewById(R.id.btnNext);
        mBtnPre = (Button) findViewById(R.id.btnPre);
        mIvShowCapture = (ImageView) findViewById(R.id.iv_capture);
        mBtnNext.setOnClickListener(this);
        mBtnPre.setOnClickListener(this);
        String path = ((DemoApplication) getApplication()).getImagePath();
        File imageDir = new File(path);
        if (imageDir.exists()) {
            if (imageDir.isDirectory()) {
                File[] images = imageDir.listFiles();
                Bitmap bitmap;
                for (File image : images) {
                    if (image.getName().endsWith(".png")) {
                        bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
                        mList.add(bitmap);
                    }
                }
            }
        }
        mImageCount = mList.size();
        if (mImageCount == 0 || mImageCount == 1) {
            findViewById(R.id.ll_control).setVisibility(View.INVISIBLE);
        }
        if (mImageCount > 0) {
            mIvShowCapture.setImageBitmap(mList.get(0));
            mCurrentImage = 0;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPre:
                showPre();
                break;
            case R.id.btnNext:
                showNext();
                break;
        }
    }

    private void showNext() {
        if (mCurrentImage != mImageCount - 1) {
            mCurrentImage++;
        } else {
            mCurrentImage = 0;
        }
        mIvShowCapture.setImageBitmap(mList.get(mCurrentImage));
    }

    private void showPre() {
        if (mCurrentImage != 0) {
            mCurrentImage--;
        } else {
            mCurrentImage = mImageCount - 1;
        }
        mIvShowCapture.setImageBitmap(mList.get(mCurrentImage));
    }
}
