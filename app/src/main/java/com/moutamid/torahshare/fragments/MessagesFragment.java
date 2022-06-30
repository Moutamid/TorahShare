package com.moutamid.torahshare.fragments;

import static android.view.LayoutInflater.from;

import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.torahshare.R.color.lighterGrey;
import static com.moutamid.torahshare.utils.Stash.toast;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.activity.ConversationActivity;
import com.moutamid.torahshare.databinding.FragmentMessagesBinding;
import com.moutamid.torahshare.model.ChatModel;
import com.moutamid.torahshare.model.UserModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessagesFragment extends Fragment {
    public FragmentMessagesBinding b;

    public MessagesFragment() {
    }

    private boolean is_contact = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentMessagesBinding.inflate(inflater, container, false);

        if (!isAdded())
            return b.getRoot();

        b.cardContacts.setOnClickListener(view -> {
            b.cardContacts.setCardBackgroundColor(getResources().getColor(R.color.default_purple));
            b.textViewContacts.setTextColor(getResources().getColor(R.color.white));

            b.cardShare.setCardBackgroundColor(getResources().getColor(R.color.white));
            b.textViewShare.setTextColor(getResources().getColor(R.color.default_purple));

            is_contact = true;

        });

        b.cardShare.setOnClickListener(view -> {
            b.cardShare.setCardBackgroundColor(getResources().getColor(R.color.default_purple));
            b.textViewShare.setTextColor(getResources().getColor(R.color.white));

            b.cardContacts.setCardBackgroundColor(getResources().getColor(R.color.white));
            b.textViewContacts.setTextColor(getResources().getColor(R.color.default_purple));

            is_contact = false;

        });

        Constants.databaseReference().child(Constants.CHATS)
                .child(Constants.auth().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            followersChatArrayList.clear();
                            contactsChatArrayList.clear();

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                ChatModel chatModel = dataSnapshot.getValue(ChatModel.class);
                                chatModel.push_key = dataSnapshot.getKey();

                                if (chatModel.is_contact) {
                                    contactsChatArrayList.add(chatModel);
                                } else {
                                    followersChatArrayList.add(chatModel);
                                }
                            }

                            initRecyclerView();

                        } else toast("No data!");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        return b.getRoot();
    }

    private ArrayList<ChatModel> followersChatArrayList = new ArrayList<>();
    private ArrayList<ChatModel> contactsChatArrayList = new ArrayList<>();

    private RecyclerView conversationRecyclerView;
    private RecyclerViewAdapterMessages adapter;

    private void initRecyclerView() {

        conversationRecyclerView = b.conversationRecyclerview;
        //conversationRecyclerView.addItemDecoration(new DividerItemDecoration(conversationRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        adapter = new RecyclerViewAdapterMessages();
        //        LinearLayoutManager layoutManagerUserFriends = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        //    int numberOfColumns = 3;
        //int mNoOfColumns = calculateNoOfColumns(getApplicationContext(), 50);
        //  recyclerView.setLayoutManager(new GridLayoutManager(this, mNoOfColumns));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        //linearLayoutManager.setReverseLayout(true);
        conversationRecyclerView.setLayoutManager(linearLayoutManager);
        conversationRecyclerView.setHasFixedSize(true);
        conversationRecyclerView.setNestedScrollingEnabled(false);

        conversationRecyclerView.setAdapter(adapter);

        //    if (adapter.getItemCount() != 0) {

        //        noChatsLayout.setVisibility(View.GONE);
        //        chatsRecyclerView.setVisibility(View.VISIBLE);

        //    }

    }

    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> {

        @NonNull
        @Override
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = from(parent.getContext()).inflate(R.layout.layout_chats, parent, false);
            return new ViewHolderRightMessage(view);
        }


        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage holder, int position) {
            ChatModel chatModel;
            if (is_contact) {
                chatModel = contactsChatArrayList.get(position);
            } else {
                chatModel = followersChatArrayList.get(position);
            }

            if (chatModel.other_name.equals(Constants.NULL)) {
                getUserDetails(chatModel, holder);
            }

            with(requireActivity().getApplicationContext())
                    .asBitmap()
                    .load(chatModel.other_profile)
                    .apply(new RequestOptions()
                            .placeholder(lighterGrey)
                            .error(R.drawable.default_profile)
                    )
                    .diskCacheStrategy(DATA)
                    .into(holder.profile);

            holder.name.setText(chatModel.other_name);
            holder.lastMcg.setText(chatModel.last_message);
            holder.time.setText(chatModel.time);

            holder.parent.setOnClickListener(view -> {
                Stash.put(Constants.CHAT_MODEL, chatModel);
                startActivity(new Intent(requireActivity(), ConversationActivity.class));

            });

        }

        private void getUserDetails(ChatModel chatModel, ViewHolderRightMessage holder) {
            Constants.databaseReference().child(Constants.USERS).child(chatModel.other_uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                UserModel model = snapshot.getValue(UserModel.class);
                                chatModel.other_name = model.name;
                                chatModel.other_profile = model.profile_url;
                                Constants.databaseReference().child(Constants.CHATS)
                                        .child(Constants.auth().getUid())
                                        .child(chatModel.chat_id).setValue(chatModel);

                                holder.name.setText(model.name);
                                with(requireActivity().getApplicationContext())
                                        .asBitmap()
                                        .load(chatModel.other_profile)
                                        .apply(new RequestOptions()
                                                .placeholder(lighterGrey)
                                                .error(R.drawable.default_profile)
                                        )
                                        .diskCacheStrategy(DATA)
                                        .into(holder.profile);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        }

        @Override
        public int getItemCount() {
            if (is_contact) {
                if (contactsChatArrayList == null)
                    return 0;
                return contactsChatArrayList.size();
            } else {
                if (followersChatArrayList == null)
                    return 0;
                return followersChatArrayList.size();
            }
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {
            TextView lastMcg, name, time;
            RelativeLayout parent;
            CircleImageView profile;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                lastMcg = v.findViewById(R.id.last_mcg);
                name = v.findViewById(R.id.name);
                time = v.findViewById(R.id.date);
                parent = v.findViewById(R.id.parentLayout);
                profile = v.findViewById(R.id.profile_mcg_left);

            }
        }

    }

}