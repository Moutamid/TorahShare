package com.moutamid.torahshare.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Constants {
    public static final String IS_LOGGED_IN = "isloggedin";
    public static final String NULL = "null";
    public static final String GENDER_MALE = "male";
    public static final String GENDER_FEMALE = "female";
    public static final String USERS = "users";
    public static final String FILTER_USER = "User";
    public static final String FILTER_VIDEOS = "Videos";
    public static final String CURRENT_USER_MODEL = "currentusermodel";
    public static final String ADMIN = "admin";
    public static final String VIDEO_APPROVAL_REQUEST = "video_approval_requests";
    public static final String IS_APPROVED = "is_approved";

    public static FirebaseAuth auth() {
        return FirebaseAuth.getInstance();
    }

    public static DatabaseReference databaseReference() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("TorahShareApp");
        db.keepSynced(true);
        return db;
    }
}
