package com.moutamid.torahshare.fragments.search;

import static android.view.LayoutInflater.from;
import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.torahshare.R.color.lighterGrey;
import static com.moutamid.torahshare.utils.Stash.toast;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.khizar1556.mkvideoplayer.MKPlayerActivity;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.activity.ProfileActivity;
import com.moutamid.torahshare.activity.VideoPlayerActivity;
import com.moutamid.torahshare.databinding.FragmentSearchBinding;
import com.moutamid.torahshare.model.FollowModel;
import com.moutamid.torahshare.model.SponsoredAccountsModel;
import com.moutamid.torahshare.model.UserModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SponsoredController {

    private SearchFragment fragment;
    private FragmentSearchBinding b;

    private ArrayList<FollowModel> followersList = Stash.getArrayList(Constants.FOLLOWERS_LIST, FollowModel.class);
    private ArrayList<FollowModel> followingList = Stash.getArrayList(Constants.FOLLOWING_LIST, FollowModel.class);

    private UserModel myUserModel = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);

    public SponsoredController(SearchFragment fragment, FragmentSearchBinding b) {
        this.fragment = fragment;
        this.b = b;
    }

    private ArrayList<SponsoredAccountsModel> accountsModelArrayList = new ArrayList<>();

    private RecyclerView conversationRecyclerView;
    private RecyclerViewAdapterMessages adapter;

    private void initRecyclerView() {
        conversationRecyclerView = b.sponsoredRecyclerview;
        adapter = new RecyclerViewAdapterMessages();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(fragment.requireContext());
        conversationRecyclerView.setLayoutManager(linearLayoutManager);
        conversationRecyclerView.setHasFixedSize(true);
        conversationRecyclerView.setNestedScrollingEnabled(false);

        conversationRecyclerView.setAdapter(adapter);

        //    if (adapter.getItemCount() != 0) {
        //        noChatsLayout.setVisibility(View.GONE);
        //        chatsRecyclerView.setVisibility(View.VISIBLE);
        //    }
    }

    public void fetchSponsors() {
        Constants.databaseReference().child(Constants.SPONSORED_ACCOUNTS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            accountsModelArrayList.clear();

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                SponsoredAccountsModel accountsModel = dataSnapshot.getValue(SponsoredAccountsModel.class);
                                accountsModel.push_key = dataSnapshot.getKey();
                                accountsModelArrayList.add(accountsModel);
                            }

                            initRecyclerView();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> {

        @NonNull
        @Override
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = from(parent.getContext()).inflate(R.layout.layout_sponsored_accounts, parent, false);
            return new ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage holder, int position) {
            SponsoredAccountsModel model = accountsModelArrayList.get(position);

            with(fragment.requireActivity().getApplicationContext())
                    .asBitmap()
                    .load(model.profile_url)
                    .apply(new RequestOptions()
                            .placeholder(lighterGrey)
                            .error(R.drawable.default_profile)
                    )
                    .diskCacheStrategy(DATA)
                    .into(holder.profile);

             with(fragment.requireActivity().getApplicationContext())
                    .asBitmap()
                    .load(model.thumbnail_url_1)
                    .apply(new RequestOptions()
                            .placeholder(lighterGrey)
                            .error(lighterGrey)
                    )
                    .diskCacheStrategy(DATA)
                    .into(holder.thumbnail1);

             with(fragment.requireActivity().getApplicationContext())
                    .asBitmap()
                    .load(model.thumbnail_url_2)
                    .apply(new RequestOptions()
                            .placeholder(lighterGrey)
                            .error(lighterGrey)
                    )
                    .diskCacheStrategy(DATA)
                    .into(holder.thumbnail2);

            holder.name.setText(model.name);
            holder.bio.setText(model.bio);

            holder.caption1.setText(model.video_caption_1);
            holder.caption2.setText(model.video_caption_2);
/*
            // VIDEO VIEW 1
            Uri uri = Uri.parse(model.video_link_1);
            holder.videoView1.setVideoURI(uri);
            holder.videoView1.pause();
            holder.videoView1.seekTo(1);
            //TODO holder.videoView1.start();


            holder.videoView1.setOnClickListener(view -> {
                if (holder.playBtn1.getVisibility() == View.GONE) {
                    holder.playBtn1.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(() -> {
                        holder.playBtn1.setVisibility(View.GONE);
                    }, 3000);
                } else {
                    holder.playBtn1.setVisibility(View.GONE);
                }
            });

            holder.playBtn1.setOnClickListener(view -> {
                holder.caption1.setVisibility(View.INVISIBLE);
                if (holder.videoView1.isPlaying()) {
                    // IS PLAYING
                    holder.playBtn1.setImageResource(R.drawable.ic_play_btn);
                    holder.videoView1.pause();

                } else {
                    // PAUSED OR NOT STARTED
                    holder.playBtn1.setImageResource(R.drawable.ic_pause_btn);
                    new Handler().postDelayed(() -> {
                        holder.playBtn1.setVisibility(View.GONE);
                    }, 3000);

                    holder.videoView1.start();
                }
            });

            // VIDEO VIEW 2
            Uri uri2 = Uri.parse(model.video_link_2);
            holder.videoView2.setVideoURI(uri2);
            holder.videoView2.pause();
            holder.videoView2.seekTo(1);
            *//*TODO holder.videoView2.start();
             *//*

            holder.videoView2.setOnClickListener(view -> {
                if (holder.playBtn2.getVisibility() == View.GONE) {
                    holder.playBtn2.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(() -> {
                        holder.playBtn2.setVisibility(View.GONE);
                    }, 3000);
                } else {
                    holder.playBtn2.setVisibility(View.GONE);
                }
            });

            holder.playBtn2.setOnClickListener(view -> {
                holder.caption2.setVisibility(View.INVISIBLE);
                if (holder.videoView2.isPlaying()) {
                    // IS PLAYING
                    holder.playBtn2.setImageResource(R.drawable.ic_play_btn);
                    holder.videoView2.pause();

                } else {
                    // PAUSED OR NOT STARTED
                    holder.playBtn2.setImageResource(R.drawable.ic_pause_btn);
                    new Handler().postDelayed(() -> {
                        holder.playBtn2.setVisibility(View.GONE);
                    }, 3000);

                    holder.videoView2.start();
                }
            });
 */
            holder.unFollowBtn.setVisibility(View.GONE);
            holder.followBtn.setVisibility(View.VISIBLE);
            for (int i = 0; i < followingList.size(); i++) {
                if (followingList.get(i).uid.equals(model.uid)) {
                    holder.unFollowBtn.setVisibility(View.VISIBLE);
                    holder.followBtn.setVisibility(View.GONE);
                }
            }

            holder.unFollowBtn.setOnClickListener(view -> {
                holder.unFollowBtn.setVisibility(View.GONE);
                holder.followBtn.setVisibility(View.VISIBLE);

                Constants.databaseReference().child(Constants.USERS)
                        .child(Constants.auth().getUid())
                        .child(Constants.FOLLOWING)
                        .child(model.uid)
                        .removeValue();

                Constants.databaseReference().child(Constants.USERS)
                        .child(model.uid)
                        .child(Constants.FOLLOWERS)
                        .child(Constants.auth().getUid())
                        .removeValue();
            });

            holder.followBtn.setOnClickListener(view -> {
                holder.followBtn.setVisibility(View.GONE);
                holder.unFollowBtn.setVisibility(View.VISIBLE);
                FollowModel followModel = new FollowModel();
                followModel.name = model.name;
                followModel.uid = model.uid;
                followModel.bio = model.bio;
                followModel.profile_url = model.profile_url;

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
                        .child(model.uid)
                        .child(Constants.FOLLOWERS)
                        .child(Constants.auth().getUid())
                        .setValue(myFollowModel);
            });

            holder.parent.setOnClickListener(view -> {
                fragment.startActivity(new Intent(fragment.requireContext(), ProfileActivity.class)
                        .putExtra(Constants.PARAMS, model.uid));
            });

            holder.videoViewLayout1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    fragment.startActivity(new Intent(fragment.requireActivity(),
//                            VideoPlayerActivity.class)
//                            .putExtra(Constants.PARAMS, model.video_link_1));
                    MKPlayerActivity
                            .configPlayer(fragment.requireActivity())
                            .setFullScreenOnly(true)
                            .play(model.video_link_1);

                }
            });
            holder.videoViewLayout2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    fragment.startActivity(new Intent(fragment.requireActivity(),
//                            VideoPlayerActivity.class)
//                            .putExtra(Constants.PARAMS, model.video_link_2));
                    MKPlayerActivity
                            .configPlayer(fragment.requireActivity())
                            .setFullScreenOnly(true)
                            .play(model.video_link_2);

                }
            });

        }

        @Override
        public int getItemCount() {
            if (accountsModelArrayList == null)
                return 0;
            return accountsModelArrayList.size();
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {

            CircleImageView profile;
            TextView name, bio, caption1, caption2, unFollowBtn;
            MaterialButton followBtn;
            //            VideoView videoView1, videoView2;
            ImageView playBtn1, playBtn2, thumbnail1, thumbnail2;
            RelativeLayout parent;
            MaterialCardView videoViewLayout1, videoViewLayout2;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                parent = v.findViewById(R.id.iiii);
                unFollowBtn = v.findViewById(R.id.unFollowBtn_sponsored_layout);
                profile = v.findViewById(R.id.profileImage_sponsored);
                name = v.findViewById(R.id.name_sponsored_layout);
                bio = v.findViewById(R.id.bio_sponsored_layout);
                caption1 = v.findViewById(R.id.caption1_sponsored_layout);
                caption2 = v.findViewById(R.id.caption2_sponsored_layout);
                followBtn = v.findViewById(R.id.followBtn_sponsored_layout);
//                videoView1 = v.findViewById(R.id.videoView1_sponsored_layout);
//                videoView2 = v.findViewById(R.id.videoView2_sponsored_layout);
                playBtn1 = v.findViewById(R.id.videoPlayBtn1_sponsored_layout);
                playBtn2 = v.findViewById(R.id.videoPlayBtn2_sponsored_layout);
                thumbnail1 = v.findViewById(R.id.thumbnail_1);
                thumbnail2 = v.findViewById(R.id.thumbnail_2);
                videoViewLayout1 = v.findViewById(R.id.videoView1Layout);
                videoViewLayout2 = v.findViewById(R.id.videoView2Layout);

            }
        }

    }

}
