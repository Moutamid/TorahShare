package com.moutamid.torahshare.fragments;

import static android.app.Activity.RESULT_OK;
import static android.view.LayoutInflater.from;
import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.torahshare.R.color.lighterGrey;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.activity.FollowListActivity;
import com.moutamid.torahshare.activity.settings.SettingsActivity;
import com.moutamid.torahshare.model.FollowModel;
import com.moutamid.torahshare.model.PostModel;
import com.moutamid.torahshare.model.UserModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    public com.moutamid.torahshare.databinding.FragmentProfileBinding b;

    public String profileImageUrl;
    UserModel userModel;

    private ArrayList<FollowModel> followingList = Stash.getArrayList(Constants.FOLLOWING_LIST, FollowModel.class);

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = com.moutamid.torahshare.databinding.FragmentProfileBinding.inflate(inflater, container, false);
        if (!isAdded()) return b.getRoot();
        userModel = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);

        if (userModel == null)
            return b.getRoot();

        if (userModel.profile_url.isEmpty() || userModel.profile_url.equals(Constants.NULL)) {
        } else {
            b.cameraIcon.setVisibility(View.GONE);
            b.profileImageView.setVisibility(View.VISIBLE);
        }
        with(requireActivity().getApplicationContext())
                .load(userModel.profile_url)
                .diskCacheStrategy(DATA)
                .into(b.profileImageView);

        b.poiu.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, 1001);
        });

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        b.settingsBtn.setOnClickListener(view -> {
            startActivity(new Intent(requireActivity(), SettingsActivity.class));
        });

        if (userModel.gender.equals(Constants.GENDER_FEMALE)) {
            b.maleLayoutProfile.setVisibility(View.GONE);
            b.femaleLayoutProfile.setVisibility(View.VISIBLE);
            initRecyclerVieww();
            return b.getRoot();
        }

        b.followersBtn.setOnClickListener(view -> {
            startActivity(new Intent(requireActivity(), FollowListActivity.class)
                    .putExtra(Constants.PARAMS, Constants.FOLLOWERS));
        });

        b.followingBtn.setOnClickListener(view -> {
            startActivity(new Intent(requireActivity(), FollowListActivity.class)
                    .putExtra(Constants.PARAMS, Constants.FOLLOWING));
        });

        b.bioTextview.setOnClickListener(view -> {
            Dialog dialog = new Dialog(requireActivity());
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

        Constants.databaseReference().child(Constants.PUBLIC_POSTS)
                .orderByChild("my_uid").equalTo(Constants.auth().getUid())
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

        /*TODO:
           // THIS IS THE POST WHEN SOMEONE TRIES TO POST SOMETHING
        PostModel postModel = new PostModel();
        postModel.name = "Name";
        postModel.date = Stash.getDate();
        postModel.caption = "So the caption is here residing with u all along";
        postModel.share_count = 0;
        postModel.comment_count = 0;
        postModel.video_link = "https://firebasestorage.googleapis.com/v0/b/sweet-nutrition.appspot.com/o/huitxbozybaymildmqwi.mp4?alt=media&token=e6900e45-6987-4a86-a913-8fa50e453aff";
        postModel.profile_link = "https://static.remove.bg/remove-bg-web/f68d607e3305b1c23820eab456f9a63968772cfc/assets/start-1abfb4fe2980eabfbbaaa4365a0692539f7cd2725f324f904565a9a744f8e214.jpg";
        postModel.my_uid = Constants.auth().getUid();
        postModel.push_key = Constants.databaseReference().child(Constants.PUBLIC_POSTS).push().getKey();

        Constants.databaseReference().child(Constants.PUBLIC_POSTS).child(postModel.push_key)
                .setValue(postModel);

        // THIS WILL CREATE A CHAT ENTRY IN THE DATABASE
        ChatModel chatModel = new ChatModel();
        chatModel.other_uid = "tRnOyVr8YnbauO8cv7UW1THPgT52";
        chatModel.last_message = "Sent a video";
        chatModel.time = Stash.getDate();
        chatModel.chat_id = Constants.databaseReference().push().getKey();
        chatModel.other_name = Constants.NULL;
        chatModel.is_contact = false;
        chatModel.other_profile = Constants.NULL;
        Constants.databaseReference().child(Constants.CHATS)
                .child(Constants.auth().getUid()).child(chatModel.chat_id)
                .setValue(chatModel);

        // THIS WILL ADD A ENTRY OF MESSAGE TO CONVERSATION OF THAT USER AND MINE AS WELL
        MessageModel messageModel = new MessageModel();
        messageModel.time = Stash.getDate();
        messageModel.sent_by = Constants.auth().getUid();
        messageModel.message = "Hi, hello test 1";
        Constants.databaseReference().child(Constants.CONVERSATIONS)
                .child(chatModel.chat_id).push().setValue(messageModel);

        MessageModel messageModel2 = new MessageModel();
        messageModel2.time = Stash.getDate();
        messageModel2.sent_by = Constants.auth().getUid();
        messageModel2.message = "Here is a new video for you! (TEST MCG)" + Constants.SEPARATOR + postModel.video_link;
        Constants.databaseReference().child(Constants.CONVERSATIONS)
                .child(chatModel.chat_id).push().setValue(messageModel2);

        ChatModel chatModel2 = new ChatModel();
        chatModel2.other_uid = "01pFAFCCZMbZrnpdNw1Lnsr3wjW2";
        chatModel2.last_message = "Sent a video";
        chatModel2.time = Stash.getDate();
        chatModel2.chat_id = Constants.databaseReference().push().getKey();
        chatModel2.other_name = Constants.NULL;
        chatModel2.is_contact = false;
        chatModel2.other_profile = Constants.NULL;

        Constants.databaseReference().child(Constants.CHATS)
                .child(Constants.auth().getUid()).child(chatModel2.chat_id)
                .setValue(chatModel2);

        // THIS WILL ADD A ENTRY OF MESSAGE TO CONVERSATION OF THAT USER AND MINE AS WELL
        MessageModel messageModel3 = new MessageModel();
        messageModel3.time = Stash.getDate();
        messageModel3.sent_by = Constants.auth().getUid();
        messageModel3.message = "Hi, hello test 2";

        Constants.databaseReference().child(Constants.CONVERSATIONS)
                .child(chatModel2.chat_id).push().setValue(messageModel3);

        MessageModel messageModel4 = new MessageModel();
        messageModel4.time = Stash.getDate();
        messageModel4.sent_by = Constants.auth().getUid();
        messageModel4.message = "Here is a new video for you! (TEST MCG)" + Constants.SEPARATOR + postModel.video_link;

        Constants.databaseReference().child(Constants.CONVERSATIONS)
                .child(chatModel.chat_id).push().setValue(messageModel4);
*/

//        TODO: REMOVE THESE BELOW MODELS AND MODELS
        /*UserModel userModel3 = new UserModel();
        userModel3.name = "Test user 1";
        userModel3.gender = Constants.GENDER_MALE;
        userModel3.email = "test1@gmail.com";
        userModel3.uid = "01pFAFCCZMbZrnpdNw1Lnsr3wjW2";
        userModel3.number = "0123456789";
        userModel3.bio = "My bio is only an example of how others bio will look like";
        userModel3.profile_url = "https://static.remove.bg/remove-bg-web/f68d607e3305b1c23820eab456f9a63968772cfc/assets/start-1abfb4fe2980eabfbbaaa4365a0692539f7cd2725f324f904565a9a744f8e214.jpg";
        userModel3.followers_count = 0;
        userModel3.following_count = 0;

        UserModel userModel34 = new UserModel();
        userModel34.name = "Test user 2";
        userModel34.gender = Constants.GENDER_FEMALE;
        userModel34.email = "test2@gmail.com";
        userModel34.uid = "tRnOyVr8YnbauO8cv7UW1THPgT52";
        userModel34.number = "0123456789";
        userModel34.bio = "This is the second bio example for the display";
        userModel34.profile_url = "https://static.remove.bg/remove-bg-web/f68d607e3305b1c23820eab456f9a63968772cfc/assets/start-1abfb4fe2980eabfbbaaa4365a0692539f7cd2725f324f904565a9a744f8e214.jpg";
        userModel34.followers_count = 0;
        userModel34.following_count = 0;

        Constants.databaseReference().child(Constants.USERS)
                .child(userModel3.uid)
                .setValue(userModel3);

        Constants.databaseReference().child(Constants.USERS)
                .child(userModel34.uid)
                .setValue(userModel34);*/

        return b.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
//        Constants.checkLanguage(requireActivity());

        userModel = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);
        if (userModel != null) {
            if (userModel.name != null)
                b.nameTextview.setText(userModel.name);
            b.bioTextview.setText(userModel.bio);

            b.followersTextview.setText(userModel.followers_count + "");
            b.totalCountTextView.setText(userModel.followers_count + "");
            b.followingTextview.setText(userModel.following_count + "");
        }

    }

    private ProgressDialog progressDialog;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profileImages");

            progressDialog.show();

            final StorageReference filePath = storageReference
                    .child(Constants.auth().getCurrentUser().getUid() + imageUri.getLastPathSegment());
            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri photoUrl) {
                            profileImageUrl = photoUrl.toString();

                            Constants.databaseReference().child(Constants.USERS)
                                    .child(Constants.auth().getCurrentUser().getUid())
                                    .child("profile_url").setValue(profileImageUrl)
                                    .addOnCompleteListener(
                                            new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        userModel.profile_url = profileImageUrl;
                                                        Stash.put(Constants.CURRENT_USER_MODEL, userModel);

                                                        b.profileImageView.setImageURI(data.getData());
                                                        b.cameraIcon.setVisibility(View.GONE);
                                                        b.profileImageView.setVisibility(View.VISIBLE);
                                                        progressDialog.dismiss();
                                                        Toast.makeText(getActivity(), "Upload done!", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private ArrayList<PostModel> postsArraylist = new ArrayList<>();

    private RecyclerView conversationRecyclerView;
    private RecyclerViewAdapterMessages adapter;

    private void initRecyclerView() {
        conversationRecyclerView = b.profileRecyclerView;
        //conversationRecyclerView.addItemDecoration(new DividerItemDecoration(conversationRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        adapter = new RecyclerViewAdapterMessages();
        //        LinearLayoutManager layoutManagerUserFriends = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        //    int numberOfColumns = 3;
        //int mNoOfColumns = calculateNoOfColumns(getApplicationContext(), 50);
        //  recyclerView.setLayoutManager(new GridLayoutManager(this, mNoOfColumns));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setReverseLayout(true);
        conversationRecyclerView.setLayoutManager(linearLayoutManager);
        conversationRecyclerView.setHasFixedSize(true);
        conversationRecyclerView.setNestedScrollingEnabled(false);

        conversationRecyclerView.setAdapter(adapter);

        //    if (adapter.getItemCount() != 0) {

        //        noChatsLayout.setVisibility(View.GONE);
        //        chatsRecyclerView.setVisibility(View.VISIBLE);

        //    }

    }

    /*public static int calculateNoOfColumns(Context context, float columnWidthDp) { // For example columnWidthdp=180
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for correct rounding to int.
        return noOfColumns;
    }*/

    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> {

        @NonNull
        @Override
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = from(parent.getContext()).inflate(R.layout.layout_my_post, parent, false);
            return new ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage holder, int position) {
            PostModel postModel = postsArraylist.get(position);

            with(requireActivity().getApplicationContext())
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

            Uri uri = Uri.parse(postModel.video_link);

            holder.videoView.setVideoURI(uri);
            /*TODO holder.videoView.start();
            holder.videoView.seekTo(100);
            holder.videoView.pause();*/

            holder.videoView.setOnClickListener(view -> {
                if (holder.playBtn.getVisibility() == View.GONE) {
                    holder.playBtn.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(() -> {
                        holder.playBtn.setVisibility(View.GONE);
                    }, 3000);
                } else {
                    holder.playBtn.setVisibility(View.GONE);
                }
            });

            holder.playBtn.setOnClickListener(view -> {
                if (holder.videoView.isPlaying()) {
                    // IS PLAYING
                    holder.playBtn.setImageResource(R.drawable.ic_play_btn);
                    holder.videoView.pause();

                } else {
                    // PAUSED OR NOT STARTED
                    holder.playBtn.setImageResource(R.drawable.ic_pause_btn);
                    new Handler().postDelayed(() -> {
                        holder.playBtn.setVisibility(View.GONE);
                    }, 3000);

                    holder.videoView.start();
                }
            });

        }

        @Override
        public int getItemCount() {
            if (postsArraylist == null)
                return 0;
            return postsArraylist.size();
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {
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

    private RecyclerView conversationRecyclerVieww;
    private RecyclerViewAdapterMessagess adapterr;

    private void initRecyclerVieww() {
        conversationRecyclerVieww = b.followersrecyclerView;
        adapterr = new RecyclerViewAdapterMessagess();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        conversationRecyclerVieww.setLayoutManager(linearLayoutManager);
        conversationRecyclerVieww.setHasFixedSize(true);
        conversationRecyclerVieww.setNestedScrollingEnabled(false);
        conversationRecyclerVieww.setAdapter(adapterr);

        //    if (adapter.getItemCount() != 0) {
        //        noChatsLayout.setVisibility(View.GONE);
        //        chatsRecyclerView.setVisibility(View.VISIBLE);
        //    }
    }

    private class RecyclerViewAdapterMessagess extends RecyclerView.Adapter
            <RecyclerViewAdapterMessagess.ViewHolderRightMessage> {

        @NonNull
        @Override
        public RecyclerViewAdapterMessagess.ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_follow_item, parent, false);
            return new RecyclerViewAdapterMessagess.ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerViewAdapterMessagess.ViewHolderRightMessage holder, int position) {
            FollowModel followModel = followingList.get(position);

            with(requireActivity().getApplicationContext())
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
//                if (isFollowers)
//                    return;
                holder.unFollowBtn.setVisibility(View.GONE);
                holder.followBtn.setVisibility(View.VISIBLE);
                followingList.get(position).value = false;

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
//                if (isFollowers)
//                    return;
                holder.followBtn.setVisibility(View.GONE);
                holder.unFollowBtn.setVisibility(View.VISIBLE);

                followingList.get(position).value = true;

                FollowModel myFollowModel = new FollowModel();
                myFollowModel.name = userModel.name;
                myFollowModel.uid = userModel.uid;
                myFollowModel.bio = userModel.bio;
                myFollowModel.profile_url = userModel.profile_url;

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
            if (followingList == null)
                return 0;
            return followingList.size();
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