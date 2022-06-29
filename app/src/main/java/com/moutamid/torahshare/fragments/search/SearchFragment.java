package com.moutamid.torahshare.fragments.search;

import static android.view.LayoutInflater.from;
import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.torahshare.R.color.lighterGrey;
import static com.moutamid.torahshare.utils.Stash.toast;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.databinding.FragmentSearchBinding;
import com.moutamid.torahshare.model.PostModel;
import com.moutamid.torahshare.model.SearchModel;
import com.moutamid.torahshare.utils.Constants;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchFragment extends Fragment {

    public SearchFragment() {
    }

    public FragmentSearchBinding b;

    SponsoredController sponsoredController;
    SearchUsersController searchUsersController;
    SearchVideosController searchVideosController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentSearchBinding.inflate(inflater, container, false);
        if (!isAdded()) return b.getRoot();
        sponsoredController = new SponsoredController(this, b);
        searchUsersController = new SearchUsersController(this, b);
        searchVideosController = new SearchVideosController(this, b);

        b.filterCardView.setOnClickListener(view -> {
            if (b.filterTextview.getVisibility() == View.GONE) {
                // SHOW FILTER OPTIONS
                if (b.filterSelectedTextview.getText().toString().equals(Constants.FILTER_USER))
                    b.filterTextview.setText(Constants.FILTER_VIDEOS);
                else b.filterTextview.setText(Constants.FILTER_USER);

                b.filterTextview.setVisibility(View.VISIBLE);
                b.filterArrow.setRotation(180);
            } else {
                // HIDE FILTER OPTIONS
                b.filterTextview.setVisibility(View.GONE);
                b.filterArrow.setRotation(0);
            }
        });

        b.filterTextview.setOnClickListener(view -> {
            b.filterSelectedTextview.setText(b.filterTextview.getText().toString());
            b.filterTextview.setVisibility(View.GONE);
            b.filterArrow.setRotation(0);
        });

        b.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                b.crossBtnSearch.setVisibility(View.VISIBLE);
                b.topTextView.setText("Search Results");
                if (b.filterTextview.getText().toString().equals(Constants.FILTER_USER)) {
                    if (searchUsersController.isLoaded) {
                        b.sponsoredRecyclerview.setVisibility(View.GONE);
                        b.searchVideosRecyclerview.setVisibility(View.GONE);
                        b.searchUserRecyclerview.setVisibility(View.VISIBLE);
                        searchUsersController.searchUserAdapter.getFilter().filter(charSequence);
                    } else toast("Data is being loaded! Please wait...");
                } else {
                    if (searchVideosController.isLoaded) {
                        b.sponsoredRecyclerview.setVisibility(View.GONE);
                        b.searchUserRecyclerview.setVisibility(View.GONE);
                        b.searchVideosRecyclerview.setVisibility(View.VISIBLE);
                        searchVideosController.searchVideosAdapter.getFilter().filter(charSequence);
                    } else toast("Data is being loaded! Please wait...");
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        b.crossBtnSearch.setOnClickListener(view -> {
            b.searchEt.setText("");
            b.topTextView.setText("Sponsored accounts");
            b.crossBtnSearch.setVisibility(View.GONE);
            b.searchUserRecyclerview.setVisibility(View.GONE);
            b.searchVideosRecyclerview.setVisibility(View.GONE);
            b.sponsoredRecyclerview.setVisibility(View.VISIBLE);
        });

        /*SponsoredAccountsModel model = new SponsoredAccountsModel();
        model.name = "Test user 1";
        model.bio = "Video creator at TorahShare";

        model.profile_url = "https://firebasestorage.googleapis.com/v0/b/sweet-nutrition.appspot.com/o/profileImages%2FJeUgraaok9XsDzaEy8keaS89Hfg1image%3A598658?alt=media&token=cc1a8ff6-9762-4bea-98a0-033e971e0376";
        model.video_link_1 = "https://firebasestorage.googleapis.com/v0/b/sweet-nutrition.appspot.com/o/huitxbozybaymildmqwi.mp4?alt=media&token=e6900e45-6987-4a86-a913-8fa50e453aff";
        model.video_caption_1 = "So here it is our best video";
        model.video_link_2 = "https://firebasestorage.googleapis.com/v0/b/sweet-nutrition.appspot.com/o/huitxbozybaymildmqwi.mp4?alt=media&token=e6900e45-6987-4a86-a913-8fa50e453aff";
        model.video_caption_2 = "We are glad to share it";

        model.uid = "tRnOyVr8YnbauO8cv7UW1THPgT52";

        SponsoredAccountsModel model2 = new SponsoredAccountsModel();
        model2.name = "Test user 2";
        model2.bio = "Video creator at TorahShare";

        model2.profile_url = "https://firebasestorage.googleapis.com/v0/b/sweet-nutrition.appspot.com/o/profileImages%2FJeUgraaok9XsDzaEy8keaS89Hfg1image%3A598658?alt=media&token=cc1a8ff6-9762-4bea-98a0-033e971e0376";
        model2.uid = "";
        model2.video_link_1 = "https://firebasestorage.googleapis.com/v0/b/sweet-nutrition.appspot.com/o/huitxbozybaymildmqwi.mp4?alt=media&token=e6900e45-6987-4a86-a913-8fa50e453aff";
        model2.video_caption_1 = "So here it is our best video";
        model2.video_link_2 = "https://firebasestorage.googleapis.com/v0/b/sweet-nutrition.appspot.com/o/huitxbozybaymildmqwi.mp4?alt=media&token=e6900e45-6987-4a86-a913-8fa50e453aff";
        model2.video_caption_2 = "We are glad to share it";

        model2.uid = "01pFAFCCZMbZrnpdNw1Lnsr3wjW2";

        Constants.databaseReference().child(Constants.SPONSORED_ACCOUNTS).push()
                .setValue(model);
        Constants.databaseReference().child(Constants.SPONSORED_ACCOUNTS).push()
                .setValue(model2);*/

        sponsoredController.fetchSponsors();

        searchUsersController.fetchUsersList();

        searchVideosController.fetchVideosList();

        return b.getRoot();
    }
}