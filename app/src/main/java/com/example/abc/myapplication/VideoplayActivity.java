package com.example.abc.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class VideoplayActivity extends AppCompatActivity implements
        GestureDetector.OnGestureListener, CacheListener {
    VideoView vv_video;
    Button btn_back;
    String strVideo = "";
    String strStory = "";
    private String TAG = "LOG";
    int nIndex = -1;
    private ArrayList<StoryData> mData = new ArrayList<>();
    int nHeight = 0;
    int nWidth = 0;
    HttpProxyCacheServer proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        setContentView(R.layout.activity_videoplay);
        //Get Story name
        Bundle b = getIntent().getExtras();
        if (b != null)
            strStory = b.getString("url");
        initView();
        proxy = App.getProxy(VideoplayActivity.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        proxy.unregisterCacheListener(this);
        proxy.shutdown();
    }

    public void initView() {
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        vv_video = findViewById(R.id.vv_video);
        vv_video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nIndex = (nIndex + 1) % mData.size();
                strVideo = mData.get(nIndex).strVideo;
                playVideo();

            }
        });
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        nHeight = displayMetrics.heightPixels;
        nWidth = displayMetrics.widthPixels;
        readFirebase();
    }

    String proxyUrl;

    public void playVideo() {
        vv_video.setVideoPath(strVideo);
        vv_video.start();
    }


    public void readFirebase() {
        GlobalVar.mDatabaseRef.child("stories/").orderByChild("story_name").equalTo(strStory).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot == null) {
                        Toast.makeText(getApplicationContext(), "No Data Available",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    Map<String, Object> dataList = (Map<String, Object>) dataSnapshot.getValue();
                    if (dataList == null) {
                    }
                    for (Map.Entry<String, Object> entry : dataList.entrySet()) {
                        StoryData mtemp = _analysisRecord(entry);
                        proxy.getProxyUrl(mtemp.strVideo);
                        mData.add(mtemp);
                    }
                    if (mData.size() > 0) {
                        nIndex = 0;
                        strVideo = mData.get(nIndex).strVideo;
                        playVideo();
                    }
                } catch (Exception e) {
                    Log.d("Ex", String.valueOf(e));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public StoryData _analysisRecord(Map.Entry<String, Object> entry) {
        StoryData dataItem = new StoryData();
        try {
            Map<String, Object> item = (Map<String, Object>) entry.getValue();
            dataItem.strImage = getString(item, "image_url");
            dataItem.strVideo = getString(item, "video_url");
            dataItem.strStory = getString(item, "story_name");
        } catch (Exception e) {
            Log.e("DFEFE", "583 " + e.toString());
        }
        return dataItem;
    }

    public String getString(Map<String, Object> item, String key) {
        String res = "";
        try {
            if (item.containsKey(key)) {
                res = (String) item.get(key);
            }
        } catch (Exception e) {
        }
        return res;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            Log.d("Position", "" + event.getX() + ":" + event.getY() + "          " + nWidth + ":" + nHeight);
            if (event.getX() < nWidth / 4) {
                nIndex -= 1;
                if (nIndex < 0)
                    nIndex = mData.size() - 1;
                strVideo = mData.get(nIndex).strVideo;
                playVideo();
            } else {
                nIndex = (nIndex + 1) % mData.size();
                strVideo = mData.get(nIndex).strVideo;
                playVideo();
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        // TODO Auto-generated method stub

        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        float sensitvity = 50;
        if ((e1.getX() - e2.getX()) > sensitvity) {
            nIndex = (nIndex + 1) % mData.size();
            strVideo = mData.get(nIndex).strVideo;
            playVideo();
            Toast.makeText(VideoplayActivity.this,
                    "Next Snap", Toast.LENGTH_SHORT).show();
        } else if ((e2.getX() - e1.getX()) > sensitvity) {
            nIndex -= 1;
            if (nIndex < 0)
                nIndex = mData.size() - 1;
            strVideo = mData.get(nIndex).strVideo;
            playVideo();
            Toast.makeText(VideoplayActivity.this,
                    "Prev Snap", Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
        Log.d("Available", "url" + ":" + percentsAvailable);
    }

    private class StoryData {
        public String strVideo;
        public String strImage;
        public String strStory;

        StoryData() {
            strVideo = "";
            strImage = "";
            strStory = "";
        }
    }

    private static abstract class Sample {
        public String[] drmKeyRequestProperties;
        public String drmLicenseUrl;
        public UUID drmSchemeUuid;
        public String name;
        public boolean preferExtensionDecoders;

        public Sample(String paramString1, UUID paramUUID, String paramString2, String[] paramArrayOfString, boolean paramBoolean) {
            this.name = paramString1;
            this.drmSchemeUuid = paramUUID;
            this.drmLicenseUrl = paramString2;
            this.drmKeyRequestProperties = paramArrayOfString;
            this.preferExtensionDecoders = paramBoolean;
        }

        public Sample(UUID paramUUID, String paramString2, String[] paramArrayOfString, boolean paramBoolean) {
        }

        public Intent buildIntent(Context paramContext, String paramString) {
            Intent localIntent = null;
            if (paramString.equals("single")) {
//                localIntent = new Intent(paramContext, PlayerActivity.class);
            }
            for (; ; ) {
                localIntent.putExtra("prefer_extension_decoders", this.preferExtensionDecoders);
                if (this.drmSchemeUuid != null) {
                    localIntent.putExtra("drm_scheme_uuid", this.drmSchemeUuid.toString());
                    localIntent.putExtra("drm_license_url", this.drmLicenseUrl);
                    localIntent.putExtra("drm_key_request_properties", this.drmKeyRequestProperties);
                }
                return localIntent;
            }
        }
    }

    private static final class UriSample
            extends VideoplayActivity.Sample {
        public final String extension;
        public final String uri;

        public UriSample(String paramString1, UUID paramUUID, String paramString2, String[] paramArrayOfString, boolean paramBoolean, String paramString3, String paramString4) {
            super(paramUUID, paramString2, paramArrayOfString, paramBoolean);
            this.uri = paramString3;
            this.extension = paramString4;
        }

        public Intent buildIntent(Context paramContext, String paramString) {
            return super.buildIntent(paramContext, paramString).setData(Uri.parse(this.uri)).putExtra("extension", this.extension).setAction("com.google.android.exoplayer.demo.action.VIEW");
        }
    }
}
