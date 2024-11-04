package com.moutamid.torahsharee.fragments.search;

import static com.moutamid.torahsharee.utils.Stash.toast;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.moutamid.torahsharee.R;
import com.moutamid.torahsharee.databinding.FragmentSearchBinding;
import com.moutamid.torahsharee.model.SponsoredAccountsModel;
import com.moutamid.torahsharee.utils.Constants;

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

        /*SponsoredAccountsModel model = new SponsoredAccountsModel();
        model.name = "User test name";
        model.bio = "Test bio";
        model.profile_url = Constants.DEFAULT_PROFILE_URL;
        model.uid = Constants.auth().getUid();
        model.video_link_1 = "Uploaded video download link";
        model.video_caption_1 = " test caption";
        model.video_link_2 = "Uploaded video download link";
        model.video_caption_2 = "test caption";
        model.push_key = Constants.databaseReference().child(Constants.SPONSORED_ACCOUNTS).push().getKey();

        Constants.databaseReference().child(Constants.SPONSORED_ACCOUNTS)
                .child(model.push_key)
                .setValue(model);*/

        b.filterCardView.setOnClickListener(view -> {
            mypopupWindow.setWidth(view.getMeasuredWidth());

            mypopupWindow.showAsDropDown(view, 0, 0);//-253

            /*if (b.filterTextview.getVisibility() == View.GONE) {
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
            }*/
        });

        LayoutInflater inflater1 = (LayoutInflater)
                requireActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View pop_up_view = inflater1.inflate(R.layout.popup_search, null);

        pop_up_view.findViewById(R.id.userTextViewGenderPopUp).setOnClickListener(view1 -> {
            if (mypopupWindow.isShowing()) mypopupWindow.dismiss();

            b.filterSelectedTextview.setText(Constants.FILTER_USER);

        });

        pop_up_view.findViewById(R.id.videoTextViewGenderPopUp).setOnClickListener(view1 -> {
            if (mypopupWindow.isShowing()) mypopupWindow.dismiss();

            b.filterSelectedTextview.setText(Constants.FILTER_VIDEOS);

        });

        mypopupWindow = new PopupWindow(pop_up_view,
                b.filterCardView.getMeasuredWidth(),
                RelativeLayout.LayoutParams.WRAP_CONTENT, true);

        mypopupWindow.getContentView().setOnClickListener(v -> {
            if (mypopupWindow.isShowing()) mypopupWindow.dismiss();
        });

        /*b.filterTextview.setOnClickListener(view -> {
            b.filterSelectedTextview.setText(b.filterTextview.getText().toString());
            b.filterTextview.setVisibility(View.GONE);
            b.filterArrow.setRotation(0);
        });
*/
        b.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                b.crossBtnSearch.setVisibility(View.VISIBLE);
                b.topTextView.setText("Search Results");
                if (b.filterSelectedTextview.getText().toString().equals(Constants.FILTER_USER)) {
//                if (b.filterseTextview.getText().toString().equals(Constants.FILTER_USER)) {
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

/*
        SponsoredAccountsModel model = new SponsoredAccountsModel();
        model.name = "Moutamid Waseem";
        model.bio = "Video creator at TorahShare";

        model.profile_url = "https://firebasestorage.googleapis.com/v0/b/recover-projects-2.appspot.com/o/profile_icon.jpg?alt=media&token=0b817f6f-47ea-4fd7-a584-626bc7212827";
        model.video_link_1 = "https://firebasestorage.googleapis.com/v0/b/recover-projects-2.appspot.com/o/videos%2Fprimary%3ADCIM%2FCamera%2FVID_20241023_165145.mp41729684335065?alt=media&token=f341e973-aafc-4d3c-adad-b0b2850bb82a";
        model.video_caption_1 = "So here it is our best video";
        model.video_link_2 = "https://firebasestorage.googleapis.com/v0/b/recover-projects-2.appspot.com/o/videos%2Fprimary%3ADCIM%2FCamera%2FVID_20241023_165145.mp41729684388282?alt=media&token=19e414b9-1b5d-484a-81c4-c615dee60d38";
        model.video_caption_2 = "We are glad to share it";

        model.uid = "bosnWAWIWibXOvZEsePgqqTAY5G2";

                Constants.databaseReference().child(Constants.SPONSORED_ACCOUNTS).push()
                .setValue(model);

*/
        sponsoredController.fetchSponsors();
        searchUsersController.fetchUsersList();
        searchVideosController.fetchVideosList();
        b.filterCardView.requestFocus();
        return b.getRoot();
    }
    PopupWindow mypopupWindow;

   /* @Override
    public void onBackPressed() {
        if (mypopupWindow.isShowing()) {
            mypopupWindow.dismiss();
            return;
        }
            super.onBackPressed();
    }*/
}