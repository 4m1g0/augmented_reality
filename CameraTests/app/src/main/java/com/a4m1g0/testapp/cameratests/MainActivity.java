package com.a4m1g0.testapp.cameratests;

import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    Camera mCamera;
    SurfaceView mSurfaceCamera;
    SurfaceHolder mSurfaceHolder;

    @Override
    protected void onResume() {
        super.onResume();

        mSurfaceHolder = mSurfaceCamera.getHolder();
        mSurfaceHolder.addCallback(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceCamera = (SurfaceView) findViewById(R.id.surface_camera);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Camera.Parameters params = mCamera.getParameters();
        params.setEpsonCameraMode(Camera.Parameters.EPSON_CAMERA_MODE_SIDE_BY_SIDE);
        mCamera.setParameters(params);

        mCamera.setPreviewCallback(this);
        mCamera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }
}
