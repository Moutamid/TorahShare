package com.moutamid.torahshare.fragments;

import static android.app.Activity.RESULT_OK;
import static android.view.LayoutInflater.from;
import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.torahshare.R.color.lighterGrey;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.activity.settings.SettingsActivity;
import com.moutamid.torahshare.model.PostModel;
import com.moutamid.torahshare.model.UserModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {
    public com.moutamid.torahshare.databinding.FragmentProfileBinding b;

    public String profileImageUrl;
    UserModel userModel = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = com.moutamid.torahshare.databinding.FragmentProfileBinding.inflate(inflater, container, false);
        if (!isAdded()) return b.getRoot();

        // TODO: REMOVE AFTER INSTALL
        if (Stash.getBoolean("aa", false)) {
            Stash.put("aa", false);
            userModel.uid = Constants.auth().getUid();
            userModel.number = Constants.NULL;
            userModel.bio = Constants.NULL;
            userModel.profile_url = Constants.NULL;
            userModel.followers_count = 0;
            userModel.following_count = 0;
            Stash.put(Constants.CURRENT_USER_MODEL, userModel);
        }

        b.profileImageView.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, 1001);
        });

        initRecyclerView();

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        b.settingsBtn.setOnClickListener(view -> {
            startActivity(new Intent(requireActivity(), SettingsActivity.class));
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
        return b.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            b.nameTextview.setText(userModel.name == null ? "Null" : userModel.name);
            b.bioTextview.setText(userModel.bio == null ? "Null" : userModel.bio);

            with(requireActivity().getApplicationContext())
                    .asBitmap()
                    .load(userModel.profile_url == null ? "" : userModel.profile_url)
                    .addListener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            b.cameraIcon.setVisibility(View.GONE);
                            b.profileImageView.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .apply(new RequestOptions()
                            .placeholder(lighterGrey)
                            .error(lighterGrey)
                    )
                    .diskCacheStrategy(DATA)
                    .into(b.profileImageView);
        } catch (Exception e) {
            e.printStackTrace();
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

            //            holder.title.setText("");

        }

        @Override
        public int getItemCount() {
            //            if (tasksArrayList == null)
            return 10;
            //            return tasksArrayList.size();
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {

            //            TextView title;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                //                title = v.findViewById(R.id.titleTextview);

            }
        }

    }

}