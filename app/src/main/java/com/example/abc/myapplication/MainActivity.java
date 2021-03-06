package com.example.abc.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private String TAG = "LOG";
    ListView listView;
    DatabaseReference myRef;
    DataListAdapter mDataListAdapter;
    FloatingActionButton fb_add;
    private ArrayList<StoryData> mData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        setContentView(R.layout.activity_main);
        initView();
    }

    public void initView() {
        fb_add = (FloatingActionButton) findViewById(R.id.fab);
        fb_add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent newIntent = new Intent(MainActivity.this, VideoCapture.class);
                startActivity(newIntent);
            }
        });
        listView = (ListView) findViewById(R.id.list_view);
        mDataListAdapter = new DataListAdapter();
        listView.setAdapter(mDataListAdapter);
        listView.setOnItemClickListener(MainActivity.this);
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        readFirebase();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{ android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.RECORD_AUDIO},
                    1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{ android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.RECORD_AUDIO},
                    1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{ android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.RECORD_AUDIO},
                    1);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CLEAR_APP_CACHE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((MainActivity) this,
                    Manifest.permission.CLEAR_APP_CACHE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CLEAR_APP_CACHE},
                        1);

            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CLEAR_APP_CACHE},
                        1);
            }
        }
    }
    public void clearCache(){
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> installedApplications = pm.getInstalledApplications(0);
        for (ApplicationInfo applicationInfo : installedApplications) {
            try {
                Context packageContext = createPackageContext(applicationInfo.packageName, 0);
                List<File> directories = new ArrayList<>();
                directories.add(packageContext.getCacheDir());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Collections.addAll(directories, packageContext.getExternalCacheDirs());
                } else {
                    directories.add(packageContext.getExternalCacheDir());
                }

                StringBuilder command = new StringBuilder("rm -rf");
                for (File directory : directories) {
                    command.append(" \"" + directory.getAbsolutePath() + "\"");
                }
                FileUtils.deleteQuietly(getApplicationContext().getCacheDir());
            } catch (PackageManager.NameNotFoundException wtf) {
            }
        }
    }
    public void readFirebase() {
        GlobalVar.mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        GlobalVar.mStorageRef = FirebaseStorage.getInstance().getReference();
        myRef = GlobalVar.mDatabaseRef.child("storynames");
        myRef.addValueEventListener(new ValueEventListener() {
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
                    mData.clear();
                    GlobalVar.mData.clear();
                    mDataListAdapter.clear();
                    for (Map.Entry<String, Object> entry : dataList.entrySet()) {
                        StoryData mtemp = _analysisRecord(entry);
                        mData.add(mtemp);
                        GlobalVar.mData.add(mtemp.strStory);
                        mDataListAdapter.add(mtemp);
                    }
                    mDataListAdapter.notifyDataSetChanged();
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
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {

            Utils.cleanVideoCacheDir(this);
        } catch (IOException e) {
            Log.e(null, "Error cleaning cache", e);
            Toast.makeText(this, "Error cleaning cache", Toast.LENGTH_LONG).show();
        }
    }
    public StoryData _analysisRecord(Map.Entry<String, Object> entry) {
        StoryData dataItem = new StoryData();
        try {
            Map<String, Object> item = (Map<String, Object>) entry.getValue();
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
//            Logger.e(TAG, e.toString());
        }
        return res;
    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case 1: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted, yay! Do the
//
//                    // contacts-related task you need to do.
//
//
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//
//                    Toast.makeText(this,"You need to accept the permission",Toast.LENGTH_SHORT).show();
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
//    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent newIntent = new Intent(MainActivity.this, VideoplayActivity.class);
        Bundle b = new Bundle();
        b.putString("url", mData.get(i).strStory); //Your id
        newIntent.putExtras(b); //Put your id to your next Intent
        startActivity(newIntent);
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

    private static class ViewHolder {
        TextView tx_story;
        TextView tx_url;
    }

    private class DataListAdapter extends BaseAdapter {
        private LayoutInflater mInflator;
        private ArrayList<StoryData> mEventData;
        private ArrayList<Boolean> mCheckStates;
        private Boolean isRemovingOrAdding = true;
        private Boolean isUpdatingTime = false;

        DataListAdapter() {
            super();
            mEventData = new ArrayList<>();
            mInflator = MainActivity.this.getLayoutInflater();
            mCheckStates = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return mEventData.size();
        }

        @Override
        public Object getItem(int i) {
            return mEventData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public void add(StoryData temp) {
            mEventData.add(temp);
            mCheckStates.add(false);
        }

        public void clear() {
            mEventData.clear();
            mCheckStates.clear();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            viewHolder = new ViewHolder();

            if (view == null) {
                view = mInflator.inflate(R.layout.eventdataitem, null);
                viewHolder.tx_story = (TextView) view.findViewById(R.id.tx_story);
                viewHolder.tx_url = (TextView) view.findViewById(R.id.tx_url);
                viewHolder.tx_story.setText(mEventData.get(i).strStory);
                viewHolder.tx_url.setText(mEventData.get(i).strImage);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            return view;
        }
    }
}
