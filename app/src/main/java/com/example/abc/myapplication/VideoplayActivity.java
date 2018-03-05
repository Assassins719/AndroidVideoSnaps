package com.example.abc.myapplication;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewFlipper;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class VideoplayActivity extends AppCompatActivity implements
        GestureDetector.OnGestureListener  {
    VideoView vv_video;
    ViewFlipper viewFlipper;
    Animation slide_in_left, slide_out_right;
    Animation slide_in_right, slide_out_left;

    String strVideo = "";
    String strStory = "";
    private String TAG = "LOG";
    int nIndex = -1;
    private GestureDetectorCompat mDetector;
    private ArrayList<StoryData> mData = new ArrayList<>();
    int nHeight = 0;
    int nWidth = 0;
    Button btn_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_videoplay);
        Bundle b = getIntent().getExtras();
        if(b != null)
            strStory = b.getString("url");
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        vv_video = (VideoView) findViewById(R.id.vv_video);
        vv_video.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            public void onCompletion(MediaPlayer mp)
            {
                // Do whatever u need to do here
                nIndex = (nIndex+1)%mData.size();
                strVideo = mData.get(nIndex).strVideo;
                playVideo();
            }
        });
        vv_video.setMediaController(null);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        nHeight = displayMetrics.heightPixels;
        nWidth = displayMetrics.widthPixels;
//        mDetector = new GestureDetectorCompat(this,this);
//        slide_in_left = AnimationUtils.loadAnimation(this,
//                R.anim.slide_in_left);
//        slide_out_right = AnimationUtils.loadAnimation(this,
//                R.anim.slide_out_right);
//
//        slide_in_right = AnimationUtils.loadAnimation(this,
//                R.anim.slide_in_right);
//        slide_out_left = AnimationUtils.loadAnimation(this,
//                R.anim.slide_out_left);
        readFirebase();
    }

    public void playVideo(){
        vv_video.setVideoURI(Uri.parse(strVideo));
        vv_video.start();
    }

    public void readFirebase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("stories/").orderByChild("story_name").equalTo(strStory).addValueEventListener(new ValueEventListener() {
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
                        mData.add(mtemp);
                    }
                    if(mData.size() > 0) {
                        nIndex = 0;
                        strVideo = mData.get(nIndex).strVideo;
                        playVideo();
                    }

                } catch (Exception e) {
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
            dataItem.strImage= getString(item,"image_url");
            dataItem.strVideo= getString(item,"video_url");
            dataItem.strStory = getString(item,"story_name");
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
         if(event. getActionMasked() ==  MotionEvent.ACTION_DOWN){
             Log.d("Position",""+event.getX() +":"+ event.getY() + "          "+ nWidth+ ":"+nHeight);
            if(event.getX() < nWidth/4)
            {
                nIndex -= 1 ;
                if(nIndex < 0)
                    nIndex = mData.size()-1;
                strVideo = mData.get(nIndex).strVideo;
                playVideo();
            }else{
                nIndex = (nIndex+1)%mData.size();
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
        if((e1.getX() - e2.getX()) > sensitvity){
            nIndex = (nIndex+1)%mData.size();
            strVideo = mData.get(nIndex).strVideo;
            playVideo();
            Toast.makeText(VideoplayActivity.this,
                    "Next Snap", Toast.LENGTH_SHORT).show();
        }else if((e2.getX() - e1.getX()) > sensitvity){
            nIndex -= 1 ;
            if(nIndex < 0)
                nIndex = mData.size()-1;
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


    private class StoryData {
        public String strVideo;
        public String strImage;
        public String strStory;
        StoryData () {
            strVideo = "";
            strImage = "";
            strStory = "";
        }
    }
}
