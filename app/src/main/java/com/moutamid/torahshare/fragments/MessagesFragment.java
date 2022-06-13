package com.moutamid.torahshare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.moutamid.torahshare.R;
import com.moutamid.torahshare.databinding.FragmentMessagesBinding;


public class MessagesFragment extends Fragment {
    public FragmentMessagesBinding b;

    public MessagesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentMessagesBinding.inflate(inflater, container, false);

        b.cardContacts.setOnClickListener(view -> {
            b.cardContacts.setCardBackgroundColor(getResources().getColor(R.color.default_purple));
            b.textViewContacts.setTextColor(getResources().getColor(R.color.white));

            b.cardShare.setCardBackgroundColor(getResources().getColor(R.color.white));
            b.textViewShare.setTextColor(getResources().getColor(R.color.default_purple));

        });

        b.cardShare.setOnClickListener(view -> {
            b.cardShare.setCardBackgroundColor(getResources().getColor(R.color.default_purple));
            b.textViewShare.setTextColor(getResources().getColor(R.color.white));

            b.cardContacts.setCardBackgroundColor(getResources().getColor(R.color.white));
            b.textViewContacts.setTextColor(getResources().getColor(R.color.default_purple));

        });


        return b.getRoot();
    }
}