package com.moutamid.torahsharee.activity;

import static android.view.LayoutInflater.from;
import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.torahsharee.R.color.lighterGrey;
import static com.moutamid.torahsharee.utils.Stash.toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.torahsharee.R;
import com.moutamid.torahsharee.databinding.ActivityProfileBinding;
import com.moutamid.torahsharee.model.ContactRequestModel;
import com.moutamid.torahsharee.model.FollowModel;
import com.moutamid.torahsharee.model.PostModel;
import com.moutamid.torahsharee.model.UserModel;
import com.moutamid.torahsharee.utils.Constants;
import com.moutamid.torahsharee.utils.Stash;

import java.util.ArrayList;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding b;

    private UserModel userModel = new UserModel();
    private ProgressDialog progressDialog;
    private UserModel myUserModel = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        progressDialog.show();

        String other_uid = getIntent().getStringExtra(Constants.PARAMS);
        b.contactBtn.setVisibility(View.VISIBLE);

//        if (Stash.getBoolean(other_uid + Constants.CONTACT_REQUESTS))
//            b.contactBtn.setVisibility(View.GONE);

        Constants.databaseReference().child(Constants.USERS).child(other_uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            userModel = snapshot.getValue(UserModel.class);
                            userModel.followers_count = (int) snapshot.child(Constants.FOLLOWERS).getChildrenCount();
                            userModel.following_count = (int) snapshot.child(Constants.FOLLOWING).getChildrenCount();

                            Constants.databaseReference().child(Constants.USERS)
                                    .child(Constants.auth().getUid())
                                    .child(Constants.FOLLOWING)
                                    .child(userModel.uid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                b.followBtnProfile.setVisibility(View.GONE);
                                                b.followingBtnProfile.setVisibility(View.VISIBLE);

                                                setValuesOnViews();
                                                progressDialog.dismiss();
                                            } else {
                                                b.followBtnProfile.setVisibility(View.VISIBLE);
                                                b.followingBtnProfile.setVisibility(View.GONE);

                                                setValuesOnViews();
                                                progressDialog.dismiss();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            progressDialog.dismiss();
                                            Toast.makeText(ProfileActivity.this, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        Dialog dialog = new Dialog(ProfileActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_comments);
        dialog.setCancelable(true);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        initCommentsRecyclerView(dialog);

//                        dialog.findViewById(R.id.);
//        dialog.show();
//        dialog.getWindow().setAttributes(layoutParams);

    }

    private ArrayList<String> commentsArrayList = new ArrayList<>();

    private RecyclerView commentsRecyclerView;
    private RecyclerViewAdapterComments commentsAdapter;

    private void initCommentsRecyclerView(Dialog dialog) {

        commentsRecyclerView = dialog.findViewById(R.id.commentsrecyclerView);
        //conversationRecyclerView.addItemDecoration(new DividerItemDecoration(conversationRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        commentsAdapter = new RecyclerViewAdapterComments();
        //        LinearLayoutManager layoutManagerUserFriends = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        //    int numberOfColumns = 3;
        //int mNoOfColumns = calculateNoOfColumns(getApplicationContext(), 50);
        //  recyclerView.setLayoutManager(new GridLayoutManager(this, mNoOfColumns));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setReverseLayout(true);
        commentsRecyclerView.setLayoutManager(linearLayoutManager);
//        commentsRecyclerView.setHasFixedSize(true);
        commentsRecyclerView.setNestedScrollingEnabled(false);

        commentsRecyclerView.setAdapter(commentsAdapter);

        //    if (adapter.getItemCount() != 0) {

        //        noChatsLayout.setVisibility(View.GONE);
        //        chatsRecyclerView.setVisibility(View.VISIBLE);

        //    }

    }

    private class RecyclerViewAdapterComments extends Adapter
            <RecyclerViewAdapterComments.ViewHolderRightComments> {

        @NonNull
        @Override
        public ViewHolderRightComments onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_comments, parent, false);
            return new ViewHolderRightComments(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightComments holder, int position) {

            //            holder.title.setText("");

        }

        @Override
        public int getItemCount() {
            //            if (tasksArrayList == null)
            return 0;
            //            return tasksArrayList.size();
        }

        public class ViewHolderRightComments extends ViewHolder {

            //            TextView title;

            public ViewHolderRightComments(@NonNull View v) {
                super(v);
                //                title = v.findViewById(R.id.titleTextview);

            }
        }

    }

//    --------------------------------------- OTHER RECYCLERVIEW--------------------

    private void setValuesOnViews() {
//        if (userModel.IS_CONTACT_CHECKED)
//            b.contactBtn.setVisibility(View.GONE);
/*
if (userModel.gender.equals(Constants.GENDER_FEMALE))
            b.contactBtn.setVisibility(View.GONE);
*/

        with(getApplicationContext())
                .load(userModel.profile_url)
                .diskCacheStrategy(DATA)
                .into(b.profileImageView);

        b.nameTextview.setText(userModel.name);
        b.bioTextview.setText(userModel.bio);

        b.followersTextview.setText(userModel.followers_count + "");
        b.followingTextview.setText(userModel.following_count + "");

        progressDialog.dismiss();

        b.followBtnProfile.setOnClickListener(view -> {
            b.followBtnProfile.setVisibility(View.GONE);
            b.followingBtnProfile.setVisibility(View.VISIBLE);
            FollowModel followModel = new FollowModel();
            followModel.name = userModel.name;
            followModel.uid = userModel.uid;
            followModel.bio = userModel.bio;
            followModel.profile_url = userModel.profile_url;

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
                    .child(userModel.uid)
                    .child(Constants.FOLLOWERS)
                    .child(Constants.auth().getUid())
                    .setValue(myFollowModel);
        });

        b.followingBtnProfile.setOnClickListener(view -> {
            b.followingBtnProfile.setVisibility(View.GONE);
            b.followBtnProfile.setVisibility(View.VISIBLE);

            Constants.databaseReference().child(Constants.USERS)
                    .child(Constants.auth().getUid())
                    .child(Constants.FOLLOWING)
                    .child(userModel.uid)
                    .removeValue();

            Constants.databaseReference().child(Constants.USERS)
                    .child(userModel.uid)
                    .child(Constants.FOLLOWERS)
                    .child(Constants.auth().getUid())
                    .removeValue();
        });

        b.contactBtn.setOnClickListener(view -> {
            Dialog dialog = new Dialog(ProfileActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_contact_request);
            dialog.setCancelable(true);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

            dialog.findViewById(R.id.crossBtnDialog).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // CODE HERE
                    dialog.dismiss();
                }
            });

            dialog.findViewById(R.id.sendBtn).setOnClickListener(view1 -> {
                EditText editText = dialog.findViewById(R.id.messageEt);

                String text = editText.getText().toString();

                if (text.isEmpty())
                    return;

                ContactRequestModel requestModel = new ContactRequestModel();
                requestModel.requester_uid = Constants.auth().getUid();
                requestModel.requester_mcg = text;
                requestModel.push_key = Constants.databaseReference().child(Constants.USERS)
                        .child(userModel.uid).child(Constants.CONTACT_REQUESTS).push().getKey();
                requestModel.chatID = UUID.randomUUID().toString();

                ProgressDialog progressDialog;
                progressDialog = new ProgressDialog(ProfileActivity.this);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Loading...");
                progressDialog.show();

                Constants.databaseReference().child(Constants.USERS)
                        .child(userModel.uid)
                        .child(Constants.CONTACT_REQUESTS)
                        .child(requestModel.push_key)
                        .setValue(requestModel)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    dialog.dismiss();
                                    b.contactBtn.setVisibility(View.GONE);
                                    Stash.put(userModel.uid + Constants.CONTACT_REQUESTS, true);
                                    toast("Request sent!");
                                } else {
                                    toast(task.getException().getMessage());
                                }
                            }
                        });
            });

            dialog.show();
            dialog.getWindow().setAttributes(layoutParams);
        });

        Constants.databaseReference().child(Constants.PUBLIC_POSTS)
                .orderByChild("my_uid").equalTo(userModel.uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            postsArraylist.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                                PostModel postModel = dataSnapshot.getValue(PostModel.class);
                                postsArraylist.add(postModel);
                            }

                            initRecyclerView();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        b.bioTextview.setOnClickListener(view -> {
            Dialog dialog = new Dialog(ProfileActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_bio_text);
            dialog.setCancelable(true);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

            TextView textView = dialog.findViewById(R.id.bioTextViewDialog);
            textView.setText(b.bioTextview.getText().toString());

            dialog.findViewById(R.id.crossBtnDialog).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // CODE HERE
                    dialog.dismiss();
                }
            });
            dialog.show();
            dialog.getWindow().setAttributes(layoutParams);
        });
    }

    private ArrayList<PostModel> postsArraylist = new ArrayList<>();

    private RecyclerView conversationRecyclerView;
    private RecyclerViewAdapterMessages adapter;

    private void initRecyclerView() {
        conversationRecyclerView = b.profileRecyclerView;
        adapter = new RecyclerViewAdapterMessages();
        //    int numberOfColumns = 3;
        //int mNoOfColumns = calculateNoOfColumns(getApplicationContext(), 50);
        //  recyclerView.setLayoutManager(new GridLayoutManager(this, mNoOfColumns));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ProfileActivity.this);
        linearLayoutManager.setReverseLayout(true);
        conversationRecyclerView.setLayoutManager(linearLayoutManager);
//        conversationRecyclerView.setHasFixedSize(true);
        conversationRecyclerView.setNestedScrollingEnabled(false);

        conversationRecyclerView.setAdapter(adapter);

        //    if (adapter.getItemCount() != 0) {
        //        noChatsLayout.setVisibility(View.GONE);
        //        chatsRecyclerView.setVisibility(View.VISIBLE);
        //    }
    }

    private class RecyclerViewAdapterMessages extends Adapter
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> {

        @NonNull
        @Override
        public RecyclerViewAdapterMessages.ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = from(parent.getContext()).inflate(R.layout.layout_my_post, parent, false);
            return new RecyclerViewAdapterMessages.ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerViewAdapterMessages.ViewHolderRightMessage holder, int position) {
            PostModel postModel = postsArraylist.get(position);

            with(getApplicationContext())
                    .asBitmap()
                    .load(postModel.profile_link)
                    .apply(new RequestOptions()
                            .placeholder(lighterGrey)
                            .error(R.drawable.default_profile)
                    )
                    .diskCacheStrategy(DATA)
                    .into(holder.profile);

            holder.name.setText(postModel.name);

            holder.time.setText(postModel.date);

            holder.caption.setText(postModel.caption);

            holder.share_count.setText(postModel.share_count + "");

            holder.comment_count.setText(postModel.comment_count + "");

            holder.minutes.setText(postModel.date);

//            Uri uri = Uri.parse(postModel.video_link);
//
//            holder.videoView.setVideoURI(uri);
//            holder.videoView.pause();
//            holder.videoView.seekTo(1);
//            holder.videoView.start();

//            holder.videoView.setOnClickListener(view -> {
//                if (holder.playBtn.getVisibility() == View.GONE) {
//                    holder.playBtn.setVisibility(View.VISIBLE);
//                    new Handler().postDelayed(() -> {
//                        holder.playBtn.setVisibility(View.GONE);
//                    }, 3000);
//                } else {
//                    holder.playBtn.setVisibility(View.GONE);
//                }
//            });

            holder.playBtn.setOnClickListener(view -> {
                startActivity(new Intent(ProfileActivity.this, VideoPlayerActivity.class).putExtra(Constants.PARAMS, postModel.video_link));
//                if (holder.videoView.isPlaying()) {
//                    // IS PLAYING
//                    holder.playBtn.setImageResource(R.drawable.ic_play_btn);
//                    holder.videoView.pause();
//
//                } else {
//                    // PAUSED OR NOT STARTED
//                    holder.playBtn.setImageResource(R.drawable.ic_pause_btn);
//                    new Handler().postDelayed(() -> {
//                        holder.playBtn.setVisibility(View.GONE);
//                    }, 3000);
//
//                    holder.videoView.start();
//                }
            });

        }

        @Override
        public int getItemCount() {
            if (postsArraylist == null)
                return 0;
            return postsArraylist.size();
        }

        public class ViewHolderRightMessage extends ViewHolder {
            CircleImageView profile;
            TextView name, time, caption, share_count, comment_count, minutes;
            MaterialCardView parent;
            VideoView videoView;
            ImageView playBtn;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                profile = v.findViewById(R.id.profile_image_post);
                name = v.findViewById(R.id.name_post);
                time = v.findViewById(R.id.time_post);
                caption = v.findViewById(R.id.caption_post);
                share_count = v.findViewById(R.id.shares_count_post);
                comment_count = v.findViewById(R.id.comments_count_post);
                minutes = v.findViewById(R.id.minutes_post);
                parent = v.findViewById(R.id.parent_post);
                videoView = v.findViewById(R.id.videoView);
                playBtn = v.findViewById(R.id.videoPlayBtn);

            }
        }

    }
}