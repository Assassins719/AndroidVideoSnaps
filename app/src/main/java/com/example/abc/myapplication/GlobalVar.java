package com.example.abc.myapplication;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Created by ABC on 3/7/2018.
 */

public class GlobalVar {
    public static ArrayList<CharSequence> mData = new ArrayList<>();
    static StorageReference mStorageRef;
    static DatabaseReference mDatabaseRef;
}
