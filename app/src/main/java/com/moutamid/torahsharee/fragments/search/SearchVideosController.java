package com.moutamid.torahsharee.fragments.search;

import static android.view.LayoutInflater.from;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.torahsharee.R;
import com.moutamid.torahsharee.activity.ProfileActivity;
import com.moutamid.torahsharee.databinding.FragmentSearchBinding;
import com.moutamid.torahsharee.model.PostModel;
import com.moutamid.torahsharee.utils.Constants;

import java.util.ArrayList;

public class SearchVideosController {
    private SearchFragment fragment;
    private FragmentSearchBinding b;
    public boolean isLoaded = false;

    public SearchVideosController(SearchFragment fragment, FragmentSearchBinding b) {
        this.fragment = fragment;
        this.b = b;
    }

    public void fetchVideosList() {
        Constants.databaseReference().child(Constants.PUBLIC_POSTS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && fragment.isAdded()) {

                            searchVideosArrayList.clear();
                            searchVideosArrayListAll.clear();

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                PostModel model2 = dataSnapshot.getValue(PostModel.class);
                                searchVideosArrayList.add(model2);
                                searchVideosArrayListAll.add(model2);
                            }

                            initRecyclerViewVideos();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    // SEARCH VIDEOS RECYCLER VIEW AND ADAPTER
    private ArrayList<PostModel> searchVideosArrayList = new ArrayList<>();
    private ArrayList<PostModel> searchVideosArrayListAll = new ArrayList<>();

    private RecyclerView searchVideosRecyclerView;
    public RecyclerViewAdapterSearchVideos searchVideosAdapter;

    private void initRecyclerViewVideos() {
        searchVideosRecyclerView = b.searchVideosRecyclerview;
        //conversationRecyclerView.addItemDecoration(new DividerItemDecoration(conversationRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        searchVideosAdapter = new RecyclerViewAdapterSearchVideos();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(fragment.requireContext());
        searchVideosRecyclerView.setLayoutManager(linearLayoutManager);
        searchVideosRecyclerView.setHasFixedSize(true);
        searchVideosRecyclerView.setNestedScrollingEnabled(false);
        searchVideosRecyclerView.setAdapter(searchVideosAdapter);
        isLoaded = true;
    }

    public class RecyclerViewAdapterSearchVideos extends RecyclerView.Adapter
            <RecyclerViewAdapterSearchVideos.ViewHolderRightMessage> implements Filterable {

        @NonNull
        @Override
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = from(parent.getContext()).inflate(R.layout.layout_chat_video, parent, false);
            return new ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage viewHolderVideo, int position) {
            PostModel model = searchVideosArrayList.get(position);

            viewHolderVideo.menuBtn.setVisibility(View.GONE);

            viewHolderVideo.caption.setText(model.caption);
            viewHolderVideo.time.setText(model.name);

            viewHolderVideo.time.setOnClickListener(view -> {
                fragment.startActivity(new Intent(fragment.requireContext(), ProfileActivity.class)
                        .putExtra(Constants.PARAMS, model.my_uid));
            });

            Uri uri = Uri.parse(model.video_link);

            viewHolderVideo.videoView.setVideoURI(uri);
            viewHolderVideo.videoView.pause();
            viewHolderVideo.videoView.seekTo(1);
            /*TODO viewHolderVideo.videoView.start();
            */

            viewHolderVideo.videoView.setOnClickListener(view -> {
                if (viewHolderVideo.playBtn.getVisibility() == View.GONE) {
                    viewHolderVideo.playBtn.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(() -> {
                        viewHolderVideo.playBtn.setVisibility(View.GONE);
                    }, 3000);
                } else {
                    viewHolderVideo.playBtn.setVisibility(View.GONE);
                }
            });

            viewHolderVideo.playBtn.setOnClickListener(view -> {
                if (viewHolderVideo.videoView.isPlaying()) {
                    // IS PLAYING
                    viewHolderVideo.playBtn.setImageResource(R.drawable.ic_play_btn);
                    viewHolderVideo.videoView.pause();

                } else {
                    // PAUSED OR NOT STARTED
                    viewHolderVideo.playBtn.setImageResource(R.drawable.ic_pause_btn);
                    new Handler().postDelayed(() -> {
                        viewHolderVideo.playBtn.setVisibility(View.GONE);
                    }, 3000);

                    viewHolderVideo.videoView.start();
                }
            });

        }

        @Override
        public int getItemCount() {
            if (searchVideosArrayList == null)
                return 0;
            return searchVideosArrayList.size();
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    ArrayList<PostModel> filteredList = new ArrayList<>();

                    if (constraint == null
                            || constraint.length() == 0
                            || constraint.toString().trim().equals("")
                            || constraint.toString() == null) {

                        filteredList.addAll(searchVideosArrayListAll);
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();

                        for (PostModel item : searchVideosArrayListAll) {
                            if (item.caption != null)
                                if (item.caption.toLowerCase().contains(filterPattern)) {
                                    filteredList.add(item);
                                }
                        }
                    }

                    FilterResults results = new FilterResults();
                    results.values = filteredList;

                    return results;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    searchVideosArrayList.clear();

                    searchVideosArrayList.addAll((ArrayList<PostModel>) filterResults.values);
                    notifyDataSetChanged();
                }
            };
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {

            ImageView menuBtn, playBtn;
            VideoView videoView;
            TextView caption, time;

            public ViewHolderRightMessage(View v) {
                super(v);
                caption = v.findViewById(R.id.timeTv_caption_video);
                time = v.findViewById(R.id.timeTv_video);
                menuBtn = v.findViewById(R.id.menuBtn_video_mcg);
                videoView = v.findViewById(R.id.videoViewMcg);
                playBtn = v.findViewById(R.id.videoPlayBtn);
            }
        }

    }

}
