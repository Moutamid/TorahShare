package com.moutamid.torahsharee.activity;

import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.torahsharee.R.color.lighterGrey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.moutamid.torahsharee.R;
import com.moutamid.torahsharee.databinding.ActivityFollowListBinding;
import com.moutamid.torahsharee.model.FollowModel;
import com.moutamid.torahsharee.model.UserModel;
import com.moutamid.torahsharee.utils.Constants;
import com.moutamid.torahsharee.utils.Stash;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FollowListActivity extends AppCompatActivity {

    private ActivityFollowListBinding b;

    private ArrayList<FollowModel> followersList = Stash.getArrayList(Constants.FOLLOWERS_LIST, FollowModel.class);
    private ArrayList<FollowModel> followingList = Stash.getArrayList(Constants.FOLLOWING_LIST, FollowModel.class);

    private ArrayList<FollowModel> currentList = new ArrayList<>();

    private UserModel myUserModel = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);
    private boolean isFollowers = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityFollowListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.backBtn.setOnClickListener(view -> {
            finish();
        });

        if (getIntent().getStringExtra(Constants.PARAMS).equals(Constants.FOLLOWERS)) {
            currentList = followersList;
            b.totalCountTextView.setText(currentList.size() + " Followers");
        } else {
            isFollowers = false;
            currentList = followingList;
            b.totalCountTextView.setText(currentList.size() + " Following");
        }

        initRecyclerView();

    }

    private RecyclerView conversationRecyclerView;
    private RecyclerViewAdapterMessages adapter;

    private void initRecyclerView() {
        conversationRecyclerView = b.followRecyclerView;
        adapter = new RecyclerViewAdapterMessages();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        conversationRecyclerView.setLayoutManager(linearLayoutManager);
        conversationRecyclerView.setHasFixedSize(true);
        conversationRecyclerView.setNestedScrollingEnabled(false);
        conversationRecyclerView.setAdapter(adapter);

        //    if (adapter.getItemCount() != 0) {
        //        noChatsLayout.setVisibility(View.GONE);
        //        chatsRecyclerView.setVisibility(View.VISIBLE);
        //    }
    }

    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> {

        @NonNull
        @Override
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_follow_item, parent, false);
            return new ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage holder, int position) {
            FollowModel followModel = currentList.get(position);

            with(getApplicationContext())
                    .asBitmap()
                    .load(followModel.profile_url)
                    .apply(new RequestOptions()
                            .placeholder(lighterGrey)
                            .error(R.drawable.default_profile)
                    )
                    .diskCacheStrategy(DATA)
                    .into(holder.profile);

            holder.name.setText(followModel.name);
            holder.bio.setText(followModel.bio);

            if (!followModel.value) {
                holder.followBtn.setVisibility(View.VISIBLE);
                holder.unFollowBtn.setVisibility(View.GONE);
            }

            holder.unFollowBtn.setOnClickListener(view -> {
                if (isFollowers)
                    return;
                holder.unFollowBtn.setVisibility(View.GONE);
                holder.followBtn.setVisibility(View.VISIBLE);
                currentList.get(position).value = false;

                Constants.databaseReference().child(Constants.USERS)
                        .child(Constants.auth().getUid())
                        .child(Constants.FOLLOWING)
                        .child(followModel.uid)
                        .removeValue();

                Constants.databaseReference().child(Constants.USERS)
                        .child(followModel.uid)
                        .child(Constants.FOLLOWERS)
                        .child(Constants.auth().getUid())
                        .removeValue();
            });

            holder.followBtn.setOnClickListener(view -> {
                if (isFollowers)
                    return;
                holder.followBtn.setVisibility(View.GONE);
                holder.unFollowBtn.setVisibility(View.VISIBLE);

                currentList.get(position).value = true;

                FollowModel myFollowModel = new FollowModel();
                myFollowModel.name = myUserModel.name;
                myFollowModel.uid = myUserModel.uid;
                myFollowModel.bio = myUserModel.bio;
                myFollowModel.profile_url = myUserModel.profile_url;

                Constants.databaseReference().child(Constants.USERS)
                        .child(Constants.auth().getUid())
                        .child(Constants.FOLLOWING)
                        .child(followModel.uid)
                        .setValue(followModel);

                Constants.databaseReference().child(Constants.USERS)
                        .child(followModel.uid)
                        .child(Constants.FOLLOWERS)
                        .child(Constants.auth().getUid())
                        .setValue(myFollowModel);
            });

        }

        @Override
        public int getItemCount() {
            if (currentList == null)
                return 0;
            return currentList.size();
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {
            CircleImageView profile;
            TextView name, bio, unFollowBtn;
            MaterialButton followBtn;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                profile = v.findViewById(R.id.profile_follow_item);
                name = v.findViewById(R.id.name_follow_item);
                bio = v.findViewById(R.id.bio_follow_item);
                unFollowBtn = v.findViewById(R.id.unFollowBtn_follow_item);
                followBtn = v.findViewById(R.id.followBtn_follow_item);
            }
        }

    }

}