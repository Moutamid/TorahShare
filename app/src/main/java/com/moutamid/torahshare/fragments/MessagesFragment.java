package com.moutamid.torahshare.fragments;

import static android.view.LayoutInflater.from;

import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.torahshare.R.color.lighterGrey;
import static com.moutamid.torahshare.utils.Stash.toast;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.activity.ConversationActivity;
import com.moutamid.torahshare.activity.HomeActivity;
import com.moutamid.torahshare.databinding.FragmentMessagesBinding;
import com.moutamid.torahshare.model.ChatModel;
import com.moutamid.torahshare.model.UserModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesFragment extends Fragment {
    public FragmentMessagesBinding b;
    UserModel userModel;

    public MessagesFragment() {
    }

    private boolean is_contact = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentMessagesBinding.inflate(inflater, container, false);

        if (!isAdded())
            return b.getRoot();

        if (Constants.auth().getCurrentUser() == null)
            return b.getRoot();

        userModel = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);

        if (userModel != null)
            if (userModel.gender != null)
                if (userModel.gender.equals(Constants.GENDER_FEMALE)) {
                    b.maleHeader.setVisibility(View.GONE);
                    b.femaleTopHeader.setVisibility(View.VISIBLE);
                    is_contact = false;
                }

        b.maleHeader.setOnPositionChangedListener(new SegmentedButtonGroup.OnPositionChangedListener() {
            @Override
            public void onPositionChanged(int position) {
                if (position == 0) {
                    is_contact = true;
                    if (adapter != null)
                        adapter.notifyDataSetChanged();

                    b.addContactLayout.setVisibility(View.VISIBLE);
                } else {
                    is_contact = false;

                    if (adapter != null)
                        adapter.notifyDataSetChanged();

                    b.addContactLayout.setVisibility(View.GONE);
                }
            }
        });

        /*b.cardContacts.setOnClickListener(view -> {
            b.cardContacts.setCardBackgroundColor(getResources().getColor(R.color.default_purple));
            b.textViewContacts.setTextColor(getResources().getColor(R.color.white));

            b.cardShare.setCardBackgroundColor(getResources().getColor(R.color.white));
            b.textViewShare.setTextColor(getResources().getColor(R.color.default_purple));

            is_contact = true;
            if (adapter != null)
                adapter.notifyDataSetChanged();
        });

        b.cardShare.setOnClickListener(view -> {
            b.cardShare.setCardBackgroundColor(getResources().getColor(R.color.default_purple));
            b.textViewShare.setTextColor(getResources().getColor(R.color.white));

            b.cardContacts.setCardBackgroundColor(getResources().getColor(R.color.white));
            b.textViewContacts.setTextColor(getResources().getColor(R.color.default_purple));

            is_contact = false;

            if (adapter != null)
                adapter.notifyDataSetChanged();

        });*/

        Constants.databaseReference().child(Constants.CHATS)
                .child(Objects.requireNonNull(Constants.auth().getUid()))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && isAdded()) {
                            followersChatArrayList.clear();
                            contactsChatArrayList.clear();

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                ChatModel chatModel = dataSnapshot.getValue(ChatModel.class);
                                chatModel.push_key = dataSnapshot.getKey();

                                if (!chatModel.other_uid.equals(userModel.uid)) {
                                    // FILTERING OWN MODEL
                                    if (chatModel.is_contact) {
                                        contactsChatArrayList.add(chatModel);
                                        contactsChatArrayListAll.add(chatModel);
                                    } else {
                                        followersChatArrayList.add(chatModel);
                                        followersChatArrayListAll.add(chatModel);
                                    }
                                }
                            }

                            initRecyclerView();

                        }
//                        else toast("No data!");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        b.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deleteModelsArrayList.size() != 0) {

                    for (ChatModel model : deleteModelsArrayList) {
                        Constants.databaseReference().child(Constants.CHATS)
                                .child(Objects.requireNonNull(Constants.auth().getUid()))
                                .child(model.push_key)
                                .removeValue();
                    }
                    b.menuIcon.setVisibility(View.VISIBLE);
                    b.deleteIcon.setVisibility(View.GONE);
                    b.searchButton.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), "Done", Toast.LENGTH_SHORT).show();

                    deleteModelsArrayList.clear();
                }
            }
        });

        b.searchEditTextMessages.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (adapter != null)
                    adapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        b.closeSearchBar.setOnClickListener(view -> {
            b.searchEditTextMessages.setText("");
            b.searchLayout.setVisibility(View.GONE);

            if (userModel.gender.equals(Constants.GENDER_FEMALE)) {
                b.femaleTopHeader.setVisibility(View.VISIBLE);
            }else {
                b.maleHeader.setVisibility(View.VISIBLE);
            }
        });

        b.addContactLayout.setOnClickListener(view -> {
            ((HomeActivity)requireActivity()).showFirst();
            /*b.searchLayout.setVisibility(View.VISIBLE);

            b.maleHeader.setVisibility(View.GONE);
            b.femaleTopHeader.setVisibility(View.GONE);*/
        });

        return b.getRoot();
    }

    private ArrayList<ChatModel> followersChatArrayList = new ArrayList<>();
    private ArrayList<ChatModel> followersChatArrayListAll = new ArrayList<>();
    private ArrayList<ChatModel> contactsChatArrayList = new ArrayList<>();
    private ArrayList<ChatModel> contactsChatArrayListAll = new ArrayList<>();

    private ArrayList<ChatModel> deleteModelsArrayList = new ArrayList<>();

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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireActivity().getApplicationContext());
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
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> implements Filterable {

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    ArrayList<ChatModel> filteredList = new ArrayList<>();

                    if (constraint == null
                            || constraint.length() == 0
                            || constraint.toString().trim().equals("")
                            || constraint.toString() == null) {

                        if (is_contact) {
                            filteredList.addAll(contactsChatArrayListAll);
                        } else {
                            filteredList.addAll(followersChatArrayListAll);
                        }
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();

                        if (is_contact) {
                            for (ChatModel item : contactsChatArrayListAll) {
                                if (item.other_name != null)
                                    if (item.other_name.toLowerCase().contains(filterPattern)) {
                                        filteredList.add(item);
                                    }
                            }
                        } else {
                            for (ChatModel item : followersChatArrayListAll) {
                                if (item.other_name != null)
                                    if (item.other_name.toLowerCase().contains(filterPattern)) {
                                        filteredList.add(item);
                                    }
                            }
                        }
                    }

                    FilterResults results = new FilterResults();
                    results.values = filteredList;

                    return results;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    if (is_contact) {
                        contactsChatArrayListAll.clear();
                        contactsChatArrayListAll.addAll((ArrayList<ChatModel>) filterResults.values);
                        notifyDataSetChanged();
                    } else {
                        followersChatArrayListAll.clear();
                        followersChatArrayListAll.addAll((ArrayList<ChatModel>) filterResults.values);
                        notifyDataSetChanged();
                    }
                }
            };

        }

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

                if (chatModel.is_selected) {
                    holder.parent.setBackgroundResource(R.color.white);

                    if (is_contact) {
                        contactsChatArrayList.get(holder.getAdapterPosition())
                                .is_selected = false;
                    } else {
                        followersChatArrayList.get(holder.getAdapterPosition())
                                .is_selected = false;
                    }
                    for (int i = 0; i < deleteModelsArrayList.size(); i++) {
                        if (deleteModelsArrayList.get(i).push_key
                                .equals(chatModel.push_key)) {
                            // DELETE FROM DELETE ARRAYLIST
                            deleteModelsArrayList.remove(i);
                        }
                    }
                    if (deleteModelsArrayList.size() == 0) {
                        b.menuIcon.setVisibility(View.VISIBLE);
                        b.deleteIcon.setVisibility(View.GONE);
                        b.searchButton.setVisibility(View.VISIBLE);
                    }
                    return;
                }

                Stash.put(Constants.CHAT_MODEL, chatModel);
                startActivity(new Intent(requireActivity(), ConversationActivity.class));

            });

            if (chatModel.is_selected) {
                holder.parent.setBackgroundResource(R.color.darkGrey);
            } else {
                holder.parent.setBackgroundResource(R.color.white);
            }

            holder.parent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if (chatModel.is_contact) {
                        contactsChatArrayList.get(holder.getAdapterPosition())
                                .is_selected = true;
                    } else {
                        followersChatArrayList.get(holder.getAdapterPosition())
                                .is_selected = true;
                    }
                    holder.parent.setBackgroundResource(R.color.darkGrey);

                    deleteModelsArrayList.add(chatModel);

                    /*if (userModel.gender.equals(Constants.GENDER_FEMALE)) {
                        b.femaleTopHeader.setVisibility(View.GONE);
                    }else {
                        b.maleHeader.setVisibility(View.GONE);
                    }*/

                    b.menuIcon.setVisibility(View.GONE);
                    b.deleteIcon.setVisibility(View.VISIBLE);
                    b.searchButton.setVisibility(View.GONE);

                    return false;
                }
            });

        }

        private void getUserDetails(ChatModel chatModel, ViewHolderRightMessage holder) {
            Constants.databaseReference().child(Constants.USERS).child(chatModel.other_uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && isAdded()) {
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