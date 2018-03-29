package com.example.abc.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.DynamicConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class VideoplayActivity extends AppCompatActivity implements
        GestureDetector.OnGestureListener, CacheListener, Player.EventListener {
    VideoView vv_video;
    VideoView vv_temp[] = new VideoView[6];
    Button btn_back;
    String strVideo = "";
    String strStory = "";
    private String TAG = "LOG";
    int nIndex = -1;
    private ArrayList<StoryData> mData = new ArrayList<>();
    int nHeight = 0;
    int nWidth = 0;
    HttpProxyCacheServer proxy;
    ArrayList<Integer> mIndexArray = new ArrayList<>();

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
        initializePlayer();
        proxy = App.getProxy(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        vv_video.stopPlayback();
        for (int i = 0; i < 6; i++) {
            vv_temp[i].stopPlayback();
        }
        player.stop();
//        proxy.shutdown();
//        deleteCache(this);

    }

    ProgressDialog dialog;

    public void initView() {
        dialog = ProgressDialog.show(this, "",
                "Loading...", true);
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
                Log.d("Buffering", "Complete");
                nIndex = (nIndex + 1) % mData.size();
                strVideo = mData.get(nIndex).strVideo;
                playVideo();
            }
        });
        vv_temp[0] = findViewById(R.id.vv_temp1);
        vv_temp[1] = findViewById(R.id.vv_temp2);
        vv_temp[2] = findViewById(R.id.vv_temp3);
        vv_temp[3] = findViewById(R.id.vv_temp4);
        vv_temp[4] = findViewById(R.id.vv_temp5);
        vv_temp[5] = findViewById(R.id.vv_temp6);
        vv_video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                dialog.dismiss();
            }
        });
        vv_temp[0].setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d("Buffering", "VV1");
                mp.setVolume(0, 0);
            }
        });
        vv_temp[1].setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d("Buffering", "VV2");
                mp.setVolume(0, 0);
            }
        });
        vv_temp[2].setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                Log.d("Buffering", "VV3");
                mp.setVolume(0, 0);
            }
        });
        vv_temp[3].setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d("Buffering", "VV4");
                mp.setVolume(0, 0);
            }
        });
        vv_temp[4].setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d("Buffering", "VV5");
                mp.setVolume(0, 0);
            }
        });
        vv_temp[5].setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d("Buffering", "VV6");
                mp.setVolume(0, 0);
            }
        });

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        nHeight = displayMetrics.heightPixels;
        nWidth = displayMetrics.widthPixels;
        readFirebase();
//        initializePlayer();
    }

    Handler mHandlerMain;
    Handler mHandlerCache;

    public void playVideo() {
        MediaSource videoSource = newVideoSource(mData.get(nIndex).strProxyUrl);
        player.prepare(videoSource);

//        player.getCurrentWindowIndex();
//        player.seekTo(player.getCurrentWindowIndex() + 1, 0);
//        String strproxy = proxy.getProxyUrl(mData.get(nIndex).strVideo);
//        vv_video.setVideoPath(mData.get(nIndex).strProxyUrl);
//        vv_video.start();
        mHandler.post(runnable);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                catchingVideos();
//            }
//        });
    }

    Handler mHandler = new Handler();
    final Runnable runnable = new Runnable() {
        public void run() {
            // TODO Auto-generated method stub
            for(int i = 0; i < 6; i ++){
                vv_temp[i].stopPlayback();
            }
            int nCount = mData.size();
            int nNext = 0;
            nNext = nCount - 1;
            Log.d("Buffering", "Index : " + nIndex + " Next : " + nNext);
            nNext = Math.min(5, nNext);
            for (int i = 0; i < nNext; i++) {
                int nTemp = (nIndex + i + 1) % nCount;
                vv_temp[i].setVideoPath(mData.get(nTemp).strProxyUrl);
                vv_temp[i].start();
            }
        }
    };

    public void catchingVideos() {

    }

    public void preCache() {
//        String proxyUrl = proxy.getProxyUrl(mData.get((nIndex + 1) % mData.size()).strVideo);
//        vv_temp.setVideoPath(proxyUrl);
//        vv_temp.start();
//        vv_temp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                mp.setVolume(0, 0);
//            }
//        });
    }

    public void cacheNext() {
//        String strTemp = proxy.getProxyUrl(mData.get((nIndex + 1) % mData.size()).strVideo);
//        URL url = null;
//        try {
//            url = new URL(strTemp);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        InputStream inputStream = null;
//        try {
//            inputStream = url.openStream();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        int bufferSize = 1024;
//        byte[] buffer = new byte[bufferSize];
//        int length = 0;
//        try {
//            while ((length = inputStream.read(buffer)) != -1) {
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        InputStream is = null;
//        try {
//            String strTemp = proxy.getProxyUrl(mData.get((nIndex + 1)%mData.size()).strVideo);
//            URL url = new URL(strTemp);
//            is = url.openStream();
//            byte[] byteChunk = IOUtils.toByteArray(is);
//        } catch (Exception e) {
//        } finally {
//            if(is != null) {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    // Long hair, don't care.
//                }
//            }
//        }
    }

    ExoPlayer player;
    DataSource.Factory dataSourceFactory;
    ExtractorsFactory extractorsFactory;
    DynamicConcatenatingMediaSource mList = new DynamicConcatenatingMediaSource();

    private MediaSource newVideoSource(String url) {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        String userAgent = Util.getUserAgent(this, "AndroidVideoCache sample");
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, userAgent, bandwidthMeter);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        return new ExtractorMediaSource(Uri.parse(url), dataSourceFactory, extractorsFactory, null, null);
    }

    private void initializePlayer() {
        // Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        //Initialize the player
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        //Initialize simpleExoPlayerView
        SimpleExoPlayerView simpleExoPlayerView = findViewById(R.id.vv_videoexo);
        simpleExoPlayerView.setPlayer((SimpleExoPlayer) player);
        player.setPlayWhenReady(true);
        player.addListener(this);

        // Produces DataSource instances through which media data is loaded.
        dataSourceFactory =
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "CloudinaryExoplayer"));
        // Produces Extractor instances for parsing the media data.
        extractorsFactory = new DefaultExtractorsFactory();
        // This is the MediaSource representing the media to be played.
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
                        mtemp.strProxyUrl = proxy.getProxyUrl(mtemp.strVideo);
                        mData.add(mtemp);
                    }
                    for (int i = 0; i < mData.size() - 1; i++) {
                        for (int j = i + 1; j < mData.size(); j++) {
                            if (mData.get(i).nKey > mData.get(j).nKey) {
                                StoryData tmp = mData.get(i);
                                mData.set(i, mData.get(j));
                                mData.set(j, tmp);
                            }
                        }
                    }
                    for (int i = 0; i < mData.size(); i++) {
                        Uri videoUri = Uri.parse(mData.get(i).strVideo);
                        MediaSource videoSource = new ExtractorMediaSource(videoUri,
                                dataSourceFactory, extractorsFactory, null, null);
                        // Prepare the player with the source.
                        mList.addMediaSource(videoSource);
                    }
                    if (mData.size() > 0) {
                        nIndex = 0;
                        playVideo();
//                        player.prepare(mList);
//                        player.setPlayWhenReady(true);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                catchingVideos();
//                            }
//                        });
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
            dataItem.nKey = Long.parseLong(entry.getKey());

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
                dialog.show();
//                if(player.getPreviousWindowIndex() == -1)
//                    player.seekTo(mList.getSize()-1, 0);
//                else {
//                    player.seekTo(player.getPreviousWindowIndex(), 0);
//                }
                nIndex -= 1;
                if (nIndex < 0)
                    nIndex = mData.size() - 1;
                strVideo = mData.get(nIndex).strVideo;
                playVideo();
            } else {
                dialog.show();
//                if(player.getNextWindowIndex() == -1)
//                    player.seekTo(0, 0);
//                else {
//                    player.seekTo(player.getNextWindowIndex(), 0);
//                }
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
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
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

    @Override
    public void onBackPressed() {
        this.finish();
//        vv_video.stopPlayback();
//        vv_video.stopPlayback();
        for (int i = 0; i < 6; i++) {
            vv_temp[i].stopPlayback();
        }
        player.stop();
//        proxy.shutdown();
//        deleteCache(this);
//        player.stop();
//        player.release();
    }

    public static void deleteCache(Context context) {
        FileUtils.deleteQuietly(context.getApplicationContext().getCacheDir());
//        try {
//            File dir = context.getCacheDir();
//            deleteDir(dir);
//        } catch (Exception e) {
//        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        vv_video.pause();
        for (int i = 0; i < 6; i++) {
            vv_temp[i].pause();
        }
        pausePlayer();
//        proxy.shutdown();
//        deleteCache(this);
//        try {
//
//            Utils.cleanVideoCacheDir(this);
//        } catch (IOException e) {
//            Log.e(null, "Error cleaning cache", e);
//            Toast.makeText(this, "Error cleaning cache", Toast.LENGTH_LONG).show();
//        }
//        player.stop();
//        player.release();
    }

    @Override
    public void onResume() {
        super.onResume();
        vv_video.resume();
        for (int i = 0; i < 6; i++) {
            vv_temp[i].resume();
        }
        startPlayer();
    }
    private void pausePlayer(){
        player.setPlayWhenReady(false);
        player.getPlaybackState();
    }
    private void startPlayer(){
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }
    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        Log.d("buffering","loading" + isLoading);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.d("buffering","loading" + playWhenReady);
        Log.d("buffering","loading" + playbackState);
        switch(playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                break;
            case ExoPlayer.STATE_ENDED:
                dialog.show();
                nIndex = (nIndex + 1) % mData.size();
                strVideo = mData.get(nIndex).strVideo;
                playVideo();
                break;
            case ExoPlayer.STATE_IDLE:
                break;
            case ExoPlayer.STATE_READY:
                Log.d("buffering","Ready");
                dialog.dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    private class StoryData {
        public String strVideo;
        public String strImage;
        public String strStory;
        public String strProxyUrl;
        public long nKey;

        StoryData() {
            nKey = 0;
            strVideo = "";
            strImage = "";
            strStory = "";
            strProxyUrl = "";
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
