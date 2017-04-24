package org.artoolkit.ar.samples.ARSimpleInteraction;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.app.AlertDialog.Builder;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.AndroidUtils;
import org.artoolkit.ar.base.rendering.ARRenderer;

import java.io.IOException;

public abstract class ARBTActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback{

    protected FrameLayout mainLayout;
    protected ARRenderer renderer;
    protected GLSurfaceView glView;
    protected SurfaceHolder mSurfaceHolder;
    protected Camera mCamera;
    protected SurfaceView preview;

    private boolean firstUpdate = false;
    private int captureWidth;
    private int captureHeight;
    private int captureRate;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidUtils.reportDisplayInformation(this);
    }

    protected void onStart() {
        super.onStart();
        Log.i("ARBTActivity", "Activity starting.");
        if(!ARToolKit.getInstance().initialiseNative(this.getCacheDir().getAbsolutePath())) {
            (new Builder(this)).setMessage("The native library is not loaded. The application cannot continue.").setTitle("Error").setCancelable(true).setNeutralButton(DialogInterface.BUTTON_POSITIVE, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    ARBTActivity.this.finish();
                }
            }).show();
        } else {
            this.mainLayout = this.supplyFrameLayout();
            if(this.mainLayout == null) {
                Log.e("ARActivity", "Error: supplyFrameLayout did not return a layout.");
            } else {
                this.renderer = this.supplyRenderer();
                if(this.renderer == null) {
                    Log.e("ARActivity", "Error: supplyRenderer did not return a renderer.");
                    this.renderer = new ARRenderer();
                }

            }
        }
    }

    protected abstract FrameLayout supplyFrameLayout();
    protected abstract ARRenderer supplyRenderer();

    public void onResume() {
        super.onResume();

        preview = new SurfaceView(this);
        mSurfaceHolder = preview.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(3);

        Log.i("ARBTActivity", "CaptureCameraPreview created");
        this.glView = new GLSurfaceView(this);
        this.glView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        this.glView.getHolder().setFormat(-3);
        this.glView.setRenderer(this.renderer);
        this.glView.setRenderMode(0);
        this.glView.setZOrderMediaOverlay(true);
        Log.i("ARBTActivity", "GLSurfaceView created");
        this.mainLayout.addView(this.preview, new ViewGroup.LayoutParams(-1, -1));
        this.mainLayout.addView(this.glView, new ViewGroup.LayoutParams(-1, -1));
        Log.i("ARBTActivity", "Views added to main layout.");
        if(this.glView != null) {
            Log.i("ARBTActivity", "Views added to main layout.");
            this.glView.onResume();
        }

    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        Log.d("ARBTActivity", "Received new frame");
        if(this.firstUpdate) {
            if(this.renderer.configureARScene()) {
                Log.i("ARActivity", "Scene configured successfully");
            } else {
                Log.e("ARActivity", "Error configuring scene. Cannot continue.");
                this.finish();
            }

            this.firstUpdate = false;
        }

        if(ARToolKit.getInstance().convertAndDetect(bytes)) {
            if(this.glView != null) {
                Log.d("ARBTActivity", "Call render");
                this.glView.requestRender();
            }

            this.onFrameProcessed();
        }
    }

    public void onFrameProcessed() {
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d("ARBTActivity", "Surface created");
        mCamera = Camera.open();

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if(mCamera == null) {
            Log.e("ARBTActivity", "No camera in surfaceChanged");
        } else {
            Log.i("ARBTActivity", "Surfaced changed, setting up camera and starting preview");
            mCamera.stopPreview();

            Camera.Parameters parameters = mCamera.getParameters();

            this.captureWidth = parameters.getPreviewSize().width;
            this.captureHeight = parameters.getPreviewSize().height;
            this.captureRate = parameters.getPreviewFrameRate();
            int pixelformat = parameters.getPreviewFormat();
            PixelFormat pixelinfo = new PixelFormat();
            PixelFormat.getPixelFormatInfo(pixelformat, pixelinfo);
            int cameraIndex = 0;
            boolean cameraIsFrontFacing = false;
            if(Build.VERSION.SDK_INT >= 9) {
                Camera.CameraInfo bufSize = new Camera.CameraInfo();
                cameraIndex = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("pref_cameraIndex", "0"));
                Camera.getCameraInfo(cameraIndex, bufSize);
                if (bufSize.facing == 1) {
                    cameraIsFrontFacing = true;
                }
            }

            if(ARToolKit.getInstance().initialiseAR(captureWidth, captureHeight, "Data/camera_para.dat", cameraIndex, cameraIsFrontFacing)) {
                Log.i("ARActivity", "Camera initialised");
            } else {
                Log.e("ARActivity", "Error initialising camera. Cannot continue.");
                this.finish();
            }

            mCamera.startPreview();
            mCamera.setPreviewCallback(this);

            Toast.makeText(this, "Camera settings: " + captureWidth + "x" + captureHeight + "@" + captureRate + "fps", Toast.LENGTH_LONG).show();
            this.firstUpdate = true;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
