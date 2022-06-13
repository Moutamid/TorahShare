package com.moutamid.torahshare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.moutamid.torahshare.databinding.FragmentSearchBinding;
import com.moutamid.torahshare.utils.Constants;


public class SearchFragment extends Fragment {

    public SearchFragment() {
    }

    public FragmentSearchBinding b;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentSearchBinding.inflate(inflater, container, false);

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

        return b.getRoot();
    }
}