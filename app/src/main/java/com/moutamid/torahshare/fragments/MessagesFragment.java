package com.moutamid.torahshare.fragments;

import static android.view.LayoutInflater.from;

import static com.moutamid.torahshare.utils.Stash.toast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.databinding.FragmentMessagesBinding;
import com.moutamid.torahshare.model.ChatModel;
import com.moutamid.torahshare.utils.Constants;

import java.util.ArrayList;


public class MessagesFragment extends Fragment {
    public FragmentMessagesBinding b;

    public MessagesFragment() {
    }

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

        });

        b.cardShare.setOnClickListener(view -> {
            b.cardShare.setCardBackgroundColor(getResources().getColor(R.color.default_purple));
            b.textViewShare.setTextColor(getResources().getColor(R.color.white));

            b.cardContacts.setCardBackgroundColor(getResources().getColor(R.color.white));
            b.textViewContacts.setTextColor(getResources().getColor(R.color.default_purple));

        });

        Constants.databaseReference().child(Constants.CHATS)
                .child(Constants.auth().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            tasksArrayList.clear();

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                ChatModel chatModel = dataSnapshot.getValue(ChatModel.class);
                                tasksArrayList.add(chatModel);
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

    private ArrayList<ChatModel> tasksArrayList = new ArrayList<>();

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

            holder.title.setText(tasksArrayList.get(position).last_message);

            holder.title.setOnClickListener(view -> {
                toast(tasksArrayList.get(position).chat_id);
                // THIS ID WILL BE PASSED TO CONVERSATION ACTIVITY TO FETCH MESSAGES
            });

        }

        @Override
        public int getItemCount() {
            if (tasksArrayList == null)
                return 0;
            return tasksArrayList.size();
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {

            TextView title;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.text);

            }
        }

    }

}