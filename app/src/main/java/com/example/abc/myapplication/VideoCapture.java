package com.example.abc.myapplication;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;

public class VideoCapture extends AppCompatActivity implements Camera.AutoFocusCallback {
    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mediaRecorder;
    private Context myContext;
    private LinearLayout cameraPreview;
    private boolean cameraFront = false;
    Button btn_record;
    Button btn_back, btn_change;
    ProgressBar pb_record;
    int nProgress = 0;
    String strVideoPath = "/sdcard/";
    String strVideo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_capture);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;
        initialize();
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the back facing camera
        // get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        // for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    public void onResume() {
        super.onResume();
        nProgress = 0;
        pb_record.setProgress(nProgress);
        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            // if the front facing camera does not exist
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                btn_change.setVisibility(View.GONE);
            }
            mCamera = Camera.open(findBackFacingCamera());
            nCameraId = findBackFacingCamera();
            mPreview.refreshCamera(mCamera, nCameraId);
            mCamera.cancelAutoFocus();

            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(parameters);
            mCamera.autoFocus(this);
        }
    }

    public void initialize() {
        cameraPreview = (LinearLayout) findViewById(R.id.CameraView);
        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.addView(mPreview);
        btn_record =  findViewById(R.id.btn_record);
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_change = findViewById(R.id.btn_change);
        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        btn_record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        doRecord();
                        if(recording) {
                            btn_record.setBackgroundResource(R.drawable.record);
                        }else{
                            btn_record.setBackgroundResource(R.drawable.recordoff);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        Log.d("Down","up");
                        doRecord();
                        if(recording) {
                            btn_record.setBackgroundResource(R.drawable.record);
                        }else{
                            btn_record.setBackgroundResource(R.drawable.recordoff);
                        }
                        return false;
                }
                return false;
            }
        });
        pb_record = (ProgressBar) findViewById(R.id.progressBar);
        pb_record.setProgress(nProgress);
    }

    public void switchCamera() {
        if (!recording) {
            int camerasNumber = Camera.getNumberOfCameras();
            if (camerasNumber > 1) {
                // release the old camera instance
                // switch camera, from the front and the back and vice versa

                releaseCamera();
                chooseCamera();
            } else {
                Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    public int nCameraId;

    public void chooseCamera() {
        // if the camera preview is the front

        if (cameraFront) {
            nCameraId = findBackFacingCamera();
            if (nCameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview

                mCamera = Camera.open(findBackFacingCamera());
                // mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera, nCameraId);
            }
        } else {
            nCameraId = findFrontFacingCamera();
            if (nCameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview

                mCamera = Camera.open(findFrontFacingCamera());
                // mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera, nCameraId);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // when on Pause, release camera in order to be used from other
        // applications
        releaseCamera();
    }

    private boolean hasCamera(Context context) {
        // check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    public void gotoPreview() {
        Intent newIntent = new Intent(VideoCapture.this, PreviewActivity.class);
        Bundle b = new Bundle();
        b.putString("url", strVideoPath + strVideo + ".mp4"); //Your id
        newIntent.putExtras(b); //Put your id to your next Intent
        startActivity(newIntent);
        releaseCamera();
    }


    boolean recording = false;
    Handler handler;

    public void doRecord() {
        if (recording) {
            // stop recording and release camera
            try {
                mediaRecorder.stop(); // stop the recording
                releaseMediaRecorder(); // release the MediaRecorder object
                recording = false;
                stopProgressAnimation();
                gotoPreview();
            }catch (Exception e){
                releaseMediaRecorder(); // release the MediaRecorder object
                recording = false;
                stopProgressAnimation();
            }

        } else {
            if (!prepareMediaRecorder()) {
                Toast.makeText(VideoCapture.this, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
                finish();
            }
            // work on UiThread for better performance
            runOnUiThread(new Runnable() {
                public void run() {
                    // If there are stories, add them to the table
                    try {
                        mediaRecorder.start();
                    } catch (final Exception ex) {
                        // Log.i("---","Exception in thread");
                    }
                }
            });
            recording = true;
            setProgressAnimate();
        }
    }

    ObjectAnimator animation;
    private void setProgressAnimate()
    {
        animation = ObjectAnimator.ofInt(pb_record, "progress", 0, 1000);
        animation.setDuration(10000);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }
    private void stopProgressAnimation(){
        animation.cancel();
        pb_record.setProgress(0);
    }
    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset(); // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock(); // lock camera for later use
        }
    }

    private boolean prepareMediaRecorder() {



        //Log.v(TAG, "MediaRecorder initialized");


        mediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);
        if (nCameraId == 0) {
            mediaRecorder.setOrientationHint(90);
        } else {
            mediaRecorder.setOrientationHint(270);
        }
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        profile.videoFrameRate = 30;
        profile.videoBitRate = 1200000;
        profile.audioBitRate = 90000;
        long nTime = System.currentTimeMillis();
        strVideo = "" + nTime;
        mediaRecorder.setProfile(profile);
        mediaRecorder.setAudioChannels(1);
//        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(strVideoPath + strVideo + ".mp4");
        mediaRecorder.setMaxDuration(10000); // Set max duration 60 sec.
        mediaRecorder.setMaxFileSize(50000000); // Set max file size 50M
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onAutoFocus(boolean b, Camera camera) {

    }
}
