package com.moutamid.torahshare.fragments;

import static android.app.Activity.RESULT_OK;
import static android.view.LayoutInflater.from;

import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.torahshare.R.color.lighterGrey;

import android.app.ProgressDialog;
import android.content.Intent;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.activity.settings.SettingsActivity;
import com.moutamid.torahshare.databinding.FragmentProfileBinding;
import com.moutamid.torahshare.model.PostModel;
import com.moutamid.torahshare.model.UserModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {
    public FragmentProfileBinding b;

    public String profileImageUrl;
    UserModel userModel = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentProfileBinding.inflate(inflater, container, false);
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

        b.nameTextview.setText(userModel.name == null ? "" : userModel.name);
        b.bioTextview.setText(userModel.bio == null ? "" : userModel.bio);

        with(requireActivity().getApplicationContext())
                .asBitmap()
                .load(userModel.profile_url == null ? "" : userModel.profile_url)
                .apply(new RequestOptions()
                        .placeholder(lighterGrey)
                        .error(lighterGrey)
                )
                .diskCacheStrategy(DATA)
                .into(b.profileImageView);

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

        /*PostModel postModel = new PostModel();
        postModel.name = "Name";
        postModel.date = "25-12-2022";
        postModel.caption = "So the caption is here ressiding with u all along";
        postModel.share_count = 0;
        postModel.comment_count = 0;
        postModel.video_link = "https://firebasestorage.googleapis.com/v0/b/sweet-nutrition.appspot.com/o/huitxbozybaymildmqwi.mp4?alt=media&token=e6900e45-6987-4a86-a913-8fa50e453aff";
        postModel.profile_link = "https://static.remove.bg/remove-bg-web/f68d607e3305b1c23820eab456f9a63968772cfc/assets/start-1abfb4fe2980eabfbbaaa4365a0692539f7cd2725f324f904565a9a744f8e214.jpg";
        postModel.my_uid = Constants.auth().getUid();

        Constants.databaseReference().child(Constants.PUBLIC_POSTS)
                .push()
                .setValue(postModel);*/

        return b.getRoot();
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