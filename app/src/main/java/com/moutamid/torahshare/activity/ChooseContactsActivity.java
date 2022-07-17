package com.moutamid.torahshare.activity;

import static android.view.LayoutInflater.from;
import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.torahshare.R.color.lighterGrey;
import static com.moutamid.torahshare.utils.Stash.toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.databinding.ActivityChooseContactsBinding;
import com.moutamid.torahshare.fragments.MessagesFragment;
import com.moutamid.torahshare.model.ChatModel;
import com.moutamid.torahshare.model.UserModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChooseContactsActivity extends AppCompatActivity {

    private ActivityChooseContactsBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityChooseContactsBinding.inflate(getLayoutInflater());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(b.getRoot());

        b.backbtn.setOnClickListener(view -> {
            finish();
        });

        b.doneBtn.setOnClickListener(view -> {
            finish();
        });

        b.selectedAllBtn.setOnClickListener(view -> {
            isSelectedAll = true;
            adapter.notifyDataSetChanged();
            ArrayList<ChatModel> contactsList = Stash.getArrayList(Constants.CHOSEN_CONTACTS_LIST, ChatModel.class);
            contactsList.clear();
            contactsList.addAll(contactsChatArrayList);
            Stash.put(Constants.CHOSEN_CONTACTS_LIST, contactsList);
        });

        Constants.databaseReference().child(Constants.CHATS)
                .child(Constants.auth().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            contactsChatArrayList.clear();

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                ChatModel chatModel = dataSnapshot.getValue(ChatModel.class);
                                chatModel.push_key = dataSnapshot.getKey();

                                if (chatModel.is_contact) {
                                    contactsChatArrayList.add(chatModel);
                                }
                            }

                            initRecyclerView();

                        } else toast("No data!");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private ArrayList<ChatModel> contactsChatArrayList = new ArrayList<>();

    private RecyclerView conversationRecyclerView;
    private RecyclerViewAdapterMessages adapter;
    private boolean isSelectedAll = false;

    private void initRecyclerView() {

        conversationRecyclerView = b.contactsRecyclerView;
        //conversationRecyclerView.addItemDecoration(new DividerItemDecoration(conversationRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        adapter = new RecyclerViewAdapterMessages();
        //        LinearLayoutManager layoutManagerUserFriends = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        //    int numberOfColumns = 3;
        //int mNoOfColumns = calculateNoOfColumns(getApplicationContext(), 50);
        //  recyclerView.setLayoutManager(new GridLayoutManager(this, mNoOfColumns));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChooseContactsActivity.this);
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
        public RecyclerViewAdapterMessages.ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = from(parent.getContext()).inflate(R.layout.layout_choose_contacts, parent, false);
            return new RecyclerViewAdapterMessages.ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerViewAdapterMessages.ViewHolderRightMessage holder, int position) {
            ChatModel chatModel;
            chatModel = contactsChatArrayList.get(position);

            /*if (chatModel.other_name.equals(Constants.NULL)) {
                getUserDetails(chatModel, holder);
            }*/

            if (isSelectedAll){
                holder.checkBox.setChecked(true);
            }

            with(getApplicationContext())
                    .asBitmap()
                    .load(chatModel.other_profile)
                    .apply(new RequestOptions()
                            .placeholder(lighterGrey)
                            .error(R.drawable.default_profile)
                    )
                    .diskCacheStrategy(DATA)
                    .into(holder.profile);

            holder.name.setText(chatModel.other_name);
            holder.bio.setText(chatModel.last_message);

            holder.parent.setOnClickListener(view -> {
                // TODO: 7/4/2022
            });

            holder.checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    ArrayList<ChatModel> contactsList = Stash.getArrayList(Constants.CHOSEN_CONTACTS_LIST, ChatModel.class);

                    for (int i = 0; i < contactsList.size(); i++) {
                        if (contactsList.get(i).push_key.equals(chatModel.push_key)){
                            contactsList.remove(i);
                        }
                    }

                    contactsList.add(chatModel);
                    Stash.put(Constants.CHOSEN_CONTACTS_LIST, contactsList);
                } else {
                    ArrayList<ChatModel> contactsList = Stash.getArrayList(Constants.CHOSEN_CONTACTS_LIST, ChatModel.class);

                    for (int i = 0; i < contactsList.size(); i++) {
                        if (contactsList.get(i).push_key.equals(chatModel.push_key)){
                            contactsList.remove(i);
                        }
                    }

                    Stash.put(Constants.CHOSEN_CONTACTS_LIST, contactsList);
                }
            });

        }

        /*private void getUserDetails(ChatModel chatModel, RecyclerViewAdapterMessages.ViewHolderRightMessage holder) {
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
                                with(getApplicationContext())
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
        }*/

        @Override
        public int getItemCount() {
            if (contactsChatArrayList == null)
                return 0;
            return contactsChatArrayList.size();
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {
            TextView name, bio;
            RelativeLayout parent;
            CircleImageView profile;
            CheckBox checkBox;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                checkBox = v.findViewById(R.id.contacts_checkBox);
                name = v.findViewById(R.id.name);
                parent = v.findViewById(R.id.parentLayout);
                profile = v.findViewById(R.id.profile_mcg_left);
                bio = v.findViewById(R.id.last_mcg_contacts);

            }
        }

    }
}