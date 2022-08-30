package com.moutamid.torahshare.fragments.search;

import static android.view.LayoutInflater.from;
import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.torahshare.R.color.lighterGrey;
import static com.moutamid.torahshare.utils.Stash.toast;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.activity.ProfileActivity;
import com.moutamid.torahshare.databinding.FragmentSearchBinding;
import com.moutamid.torahshare.model.SearchModel;
import com.moutamid.torahshare.utils.Constants;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchUsersController {
    private SearchFragment fragment;
    private FragmentSearchBinding b;

    public boolean isLoaded = false;

    public SearchUsersController(SearchFragment fragment, FragmentSearchBinding b) {
        this.fragment = fragment;
        this.b = b;
    }

    public void fetchUsersList() {
        Constants.databaseReference().child(Constants.USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            searchUserArrayList.clear();
                            searchUserArrayListAll.clear();

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                SearchModel model1 = dataSnapshot.getValue(SearchModel.class);
                                if (model1 != null) {
                                    if (model1.bio.equals(Constants.NULL)) {

                                    } else {
                                        model1.uid = dataSnapshot.getKey();
                                        searchUserArrayList.add(model1);
                                        searchUserArrayListAll.add(model1);
                                    }
                                }
                            }

                            initRecyclerView();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //    SEARCH USER RECYCLER VIEW AND ADAPTER
    private ArrayList<SearchModel> searchUserArrayList = new ArrayList<>();
    private ArrayList<SearchModel> searchUserArrayListAll = new ArrayList<>();

    private RecyclerView searchUserRecyclerView;
    public RecyclerViewAdapterSearchUser searchUserAdapter;

    private void initRecyclerView() {
        searchUserRecyclerView = b.searchUserRecyclerview;
        //conversationRecyclerView.addItemDecoration(new DividerItemDecoration(conversationRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        searchUserAdapter = new RecyclerViewAdapterSearchUser();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(fragment.requireContext());
        searchUserRecyclerView.setLayoutManager(linearLayoutManager);
        searchUserRecyclerView.setHasFixedSize(true);
        searchUserRecyclerView.setNestedScrollingEnabled(false);
        searchUserRecyclerView.setAdapter(searchUserAdapter);
        isLoaded = true;
    }

    public class RecyclerViewAdapterSearchUser extends RecyclerView.Adapter
            <RecyclerViewAdapterSearchUser.ViewHolderRightMessage> implements Filterable {

        @NonNull
        @Override
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = from(parent.getContext()).inflate(R.layout.layout_search_item, parent, false);
            return new ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage holder, int position) {
            SearchModel model = searchUserArrayList.get(position);

            with(fragment.requireActivity().getApplicationContext())
                    .asBitmap()
                    .load(model.profile_url)
                    .apply(new RequestOptions()
                            .placeholder(lighterGrey)
                            .error(R.drawable.default_profile)
                    )
                    .diskCacheStrategy(DATA)
                    .into(holder.profile);

            holder.name.setText(model.name);
            holder.bio.setText(model.bio);

            holder.parent.setOnClickListener(view -> {
//                toast(model.uid);
                fragment.startActivity(new Intent(fragment.requireContext(), ProfileActivity.class)
                        .putExtra(Constants.PARAMS, model.uid));
            });

        }

        @Override
        public int getItemCount() {
            if (searchUserArrayList == null)
                return 0;
            return searchUserArrayList.size();
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    ArrayList<SearchModel> filteredList = new ArrayList<>();

                    if (constraint == null
                            || constraint.length() == 0
                            || constraint.toString().trim().equals("")
                            || constraint.toString() == null) {

                        filteredList.addAll(searchUserArrayListAll);
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();

                        for (SearchModel item : searchUserArrayListAll) {
                            if (item.name != null)
                                if (item.name.toLowerCase().contains(filterPattern)) {
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
                    searchUserArrayList.clear();

                    searchUserArrayList.addAll((ArrayList<SearchModel>) filterResults.values);
                    notifyDataSetChanged();
                }
            };
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {

            RelativeLayout parent;
            CircleImageView profile;
            TextView name, bio;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                name = v.findViewById(R.id.name_search_item);
                bio = v.findViewById(R.id.bio_search_item);
                profile = v.findViewById(R.id.profile_search_item);
                parent = v.findViewById(R.id.parent_search_item);
            }
        }

    }


}
