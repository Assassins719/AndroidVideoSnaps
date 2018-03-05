package com.example.abc.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private String TAG = "LOG";
    ListView listView;
    DatabaseReference myRef;
    DataListAdapter mDataListAdapter;

    private ArrayList<StoryData> mData = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        listView = (ListView) findViewById(R.id.list_view);
        readFirebase();
        mDataListAdapter = new DataListAdapter();
        listView.setAdapter(mDataListAdapter);
        listView.setOnItemClickListener(MainActivity.this);
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mDataListAdapter.notifyDataSetChanged();

    }

    public void readFirebase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("storynames");
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
                    for (Map.Entry<String, Object> entry : dataList.entrySet()) {
                        StoryData mtemp = _analysisRecord(entry);
                        mData.add(mtemp);
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

    public StoryData _analysisRecord(Map.Entry<String, Object> entry) {
        StoryData dataItem = new StoryData();
        try {
            Map<String, Object> item = (Map<String, Object>) entry.getValue();
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
//            Logger.e(TAG, e.toString());
        }
        return res;
    }

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
        StoryData () {
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
