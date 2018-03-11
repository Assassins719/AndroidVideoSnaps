package com.example.abc.myapplication;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Url;

public class PreviewActivity extends AppCompatActivity {
    String strVideo = "/sdcard/mysnaps.mp4"; //Use Static Path for snap record
    VideoView vv_video;
    Button btn_back, btn_upload;
    String strStoryName = "";
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        setContentView(R.layout.activity_preview);
        //Get Story name
        Bundle b = getIntent().getExtras();
        if (b != null)
            strVideo = b.getString("url");
        initView();
    }

    public void initView() {
        strStoryName = "";
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_upload = findViewById(R.id.btn_upload);
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadVideo();
            }
        });
        vv_video = (VideoView) findViewById(R.id.vv_video);
        vv_video.setMediaController(new MediaController(this));
        vv_video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });
        playVideo();
    }

    public void playVideo() {
        vv_video.setVideoPath(strVideo);
        vv_video.start();
    }

    AlertDialog levelDialog;

    public void uploadVideo() {
        // Strings to Show In Dialog with Radio Buttons
        final CharSequence[] items = {" Easy ", " Medium ", " Hard ", " Very Hard "};
        String[] mStringArray = new String[GlobalVar.mData.size()];
        mStringArray = GlobalVar.mData.toArray(mStringArray);
        // Creating and Building the Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Story");
        final String[] finalMStringArray = mStringArray;
        builder.setSingleChoiceItems(mStringArray, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                strStoryName = finalMStringArray[item];
            }
        });
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                doUploadVideo();
                PreviewActivity.this.finish();
            }
        });
        builder.setNegativeButton("Cancel", null);
        levelDialog = builder.create();
        levelDialog.show();
    }

    public void doUploadVideo() {
        Uri file = Uri.fromFile(new File(strVideo));
        long nTime = System.currentTimeMillis();
        StorageReference riversRef = GlobalVar.mStorageRef.child("snaps/" + nTime);
        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.d("url", String.valueOf(downloadUrl));
                        NewSnap mSnap = new NewSnap();
                        mSnap.image_url = "";
                        mSnap.video_url = String.valueOf(downloadUrl);
                        mSnap.story_name = strStoryName;
                        doUploadImg(mSnap);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Log.d("err", "fail");
                    }
                });

//// Request
//        @Headers("Authorization: bearer [your_vimeo_token]")
//        @PUT
//        Call<ResponseBody> uploadVideo(@Url String url, @HeaderMap Map<String, String> headers, @Body RequestBody video);
//
//// Call
//        Call<ResponseBody> call = vimeoApi.uploadVideo(vimeoTicket.getUploadLinkSecure(), headers, video);
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                // Continue with delete upload ticket
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                // Uploading video failed
//            }
//        });
    }

    public void doUploadImg(final NewSnap mSnap) {
        Bitmap bMap = ThumbnailUtils.createVideoThumbnail(strVideo, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
        long nTime = System.currentTimeMillis();
        Uri filePath = createPngFile(bMap);
        StorageReference riversRef = GlobalVar.mStorageRef.child("images/" + nTime);
        riversRef.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        mSnap.image_url = String.valueOf(downloadUrl);
                        myRef = GlobalVar.mDatabaseRef.child("stories").child("" + mSnap.nTime);
                        myRef.setValue(mSnap);
                        File file = new File(strVideo);
                        file.delete();
                        if (file.exists()) {
                            try {
                                file.getCanonicalFile().delete();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (file.exists()) {
                                getApplicationContext().deleteFile(file.getName());
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Log.d("err", "fail");
                    }
                });
    }

    Uri createPngFile(Bitmap bitmap) {
        File f = new File(PreviewActivity.this.getCacheDir(), "thumbnail.png");
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return Uri.fromFile(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
