package com.moutamid.torahshare.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.moutamid.torahshare.databinding.ActivityFollowListBinding;
import com.moutamid.torahshare.model.FollowModel;
import com.moutamid.torahshare.model.UserModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

import java.util.ArrayList;

public class FollowListActivity extends AppCompatActivity {

    private ActivityFollowListBinding b;

    private ArrayList<FollowModel> followersList = Stash.getArrayList(Constants.FOLLOWERS_LIST, FollowModel.class);
    private ArrayList<FollowModel> followingList = Stash.getArrayList(Constants.FOLLOWING_LIST, FollowModel.class);

    private UserModel myUserModel = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityFollowListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

    }

}