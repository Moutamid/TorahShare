package com.moutamid.torahshare.activity;

import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.torahshare.R.color.lighterGrey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.databinding.ActivityConversationBinding;
import com.moutamid.torahshare.model.ChatModel;
import com.moutamid.torahshare.model.MessageModel;
import com.moutamid.torahshare.model.UserModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationActivity extends AppCompatActivity {

    ChatModel chatModel = (ChatModel) Stash.getObject(Constants.CHAT_MODEL, ChatModel.class);
    private ActivityConversationBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityConversationBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        with(getApplicationContext())
                .asBitmap()
                .load(chatModel.other_profile)
                .apply(new RequestOptions()
                        .placeholder(lighterGrey)
                        .error(lighterGrey)
                )
                .diskCacheStrategy(DATA)
                .into(b.profileimage);

        b.nametext.setText(chatModel.other_name);

        b.backbtn.setOnClickListener(view -> {
            finish();
        });

        Constants.databaseReference().child(Constants.CONVERSATIONS)
                .child(chatModel.chat_id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            messagesModelArrayList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                                MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                                messagesModelArrayList.add(messageModel);

                            }

                            initRecyclerView();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        b.sendBtn.setOnClickListener(view -> {
            String message = b.messageEt.getText().toString();

            if (message.isEmpty())
                return;

            sendMessage(message);
            b.messageEt.setText("");
        });

        b.messageEt.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (keyboardShown(b.messageEt.getRootView())) {
                    Log.d("keyboard", "keyboard UP");

                    if (keyboardUp == false) {
                        if (messagesModelArrayList.size() > 0)
                            conversationRecyclerView.smoothScrollToPosition(messagesModelArrayList.size() + 1);
                        keyboardUp = true;
                    }

                } else {
                    Log.d("keyboard", "keyboard Down");
                    keyboardUp = false;
                }
            }
        });

    }

    private boolean keyboardUp = false;

    private boolean keyboardShown(View rootView) {
        final int softKeyboardHeight = 100;
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        int heightDiff = rootView.getBottom() - r.bottom;
        return heightDiff > softKeyboardHeight * dm.density;
    }

    private void sendMessage(String mcg) {
        // THIS WILL UPDATE LAST MESSAGE
        chatModel.last_message = mcg;
        chatModel.time = Stash.getDate();
        Constants.databaseReference().child(Constants.CHATS)
                .child(Constants.auth().getUid()).child(chatModel.chat_id)
                .setValue(chatModel);

        // THIS WILL ADD A ENTRY OF MESSAGE TO CONVERSATION OF THAT USER AND MINE AS WELL
        MessageModel messageModel = new MessageModel();
        messageModel.time = Stash.getDate();
        messageModel.sent_by = Constants.auth().getUid();
        messageModel.message = mcg;
        Constants.databaseReference().child(Constants.CONVERSATIONS)
                .child(chatModel.chat_id).push().setValue(messageModel);
    }

    private ArrayList<MessageModel> messagesModelArrayList = new ArrayList<>();

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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setStackFromEnd(true);
        //linearLayoutManager.setReverseLayout(true);
        conversationRecyclerView.setLayoutManager(linearLayoutManager);
        conversationRecyclerView.setHasFixedSize(true);
        conversationRecyclerView.setNestedScrollingEnabled(false);

        conversationRecyclerView.setAdapter(adapter);
        conversationRecyclerView.scrollToPosition(conversationRecyclerView.getAdapter().getItemCount() - 1);
        //    if (adapter.getItemCount() != 0) {
        //        noChatsLayout.setVisibility(View.GONE);
        //        chatsRecyclerView.setVisibility(View.VISIBLE);
        //    }

    }

    /*private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> {

        @NonNull
        @Override
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_messages_left, parent, false);
            return new ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage holder, int position) {
            MessageModel messageModel = tasksArrayList.get(position);

            with(getApplicationContext())
                    .asBitmap()
                    .load("")
                    .apply(new RequestOptions()
                            .placeholder(lighterGrey)
                            .error(R.drawable.default_profile)
                    )
                    .diskCacheStrategy(DATA)
                    .into(holder.profile);

            holder.mcg.setText(messageModel.message);
            holder.time.setText(messageModel.time);
        }

        @Override
        public int getItemCount() {
            if (tasksArrayList == null)
                return 0;
            return tasksArrayList.size();
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {
            CircleImageView profile;
            TextView mcg, time;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                profile = v.findViewById(R.id.profile_mcg_left);
                mcg = v.findViewById(R.id.mcg_left);
                time = v.findViewById(R.id.timeTv_mcg_left);

            }
        }

    }*/

    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int LEFT_MESSAGE_LAYOUT = 1;
        private static final int RIGHT_MESSAGE_LAYOUT = 2;
        private static final int VIDEO_LAYOUT = 3;

        @Override
        public int getItemViewType(int position) {
            if (messagesModelArrayList.get(position).message.contains(Constants.SEPARATOR)) {
                return VIDEO_LAYOUT;

            } else if (messagesModelArrayList.get(position).sent_by.equals(Constants.auth().getUid())) {
                return RIGHT_MESSAGE_LAYOUT;

            } else
                return LEFT_MESSAGE_LAYOUT;
        }

        @Override
        public int getItemCount() {
            if (messagesModelArrayList == null)
                return 0;
            return messagesModelArrayList.size();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIDEO_LAYOUT:
                    View view3 = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_video, parent, false);
                    return new ViewHolderVideo(view3);
                case LEFT_MESSAGE_LAYOUT:
                    View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_messages_left, parent, false);
                    return new ViewHolderLeftMcg(view1);
                default: //RIGHT_MESSAGE_LAYOUT
                    View view2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_messages_right, parent, false);
                    return new ViewHolderRightMcg(view2);
            }
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            MessageModel messageModel = messagesModelArrayList.get(position);

            switch (holder.getItemViewType()) {
                case LEFT_MESSAGE_LAYOUT:
                    ViewHolderLeftMcg viewHolderLeftMcg = (ViewHolderLeftMcg) holder;

                    with(getApplicationContext())
                            .asBitmap()
                            .load("")
                            .apply(new RequestOptions()
                                    .placeholder(lighterGrey)
                                    .error(R.drawable.default_profile)
                            )
                            .diskCacheStrategy(DATA)
                            .into(viewHolderLeftMcg.profile);

                    viewHolderLeftMcg.mcg.setText(messageModel.message);
                    viewHolderLeftMcg.time.setText(messageModel.time);
                    break;

                case RIGHT_MESSAGE_LAYOUT:
                    ViewHolderRightMcg viewHolderRightMcg = (ViewHolderRightMcg) holder;
                    UserModel userModel = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);
                    with(getApplicationContext())
                            .asBitmap()
                            .load(userModel.profile_url)
                            .apply(new RequestOptions()
                                    .placeholder(lighterGrey)
                                    .error(R.drawable.default_profile)
                            )
                            .diskCacheStrategy(DATA)
                            .into(viewHolderRightMcg.profile);

                    viewHolderRightMcg.mcg.setText(messageModel.message);
                    viewHolderRightMcg.time.setText(messageModel.time);
                    break;
                case VIDEO_LAYOUT:
                    ViewHolderVideo viewHolderVideo = (ViewHolderVideo) holder;

                    String[] parts = messageModel.message.split(Constants.SEPARATOR);

                    viewHolderVideo.caption.setText(parts[0]);
                    viewHolderVideo.time.setText(messageModel.time);

                    Uri uri = Uri.parse(parts[1]);

                    viewHolderVideo.videoView.setVideoURI(uri);
                    viewHolderVideo.videoView.start();
//                    viewHolderVideo.videoView.pause();
                    break;
            }
        }

        public class ViewHolderLeftMcg extends RecyclerView.ViewHolder {
            CircleImageView profile;
            TextView mcg, time;

            public ViewHolderLeftMcg(View v) {
                super(v);
                profile = v.findViewById(R.id.profile_mcg_left);
                mcg = v.findViewById(R.id.mcg_left);
                time = v.findViewById(R.id.timeTv_mcg_left);
            }
        }

        public class ViewHolderRightMcg extends RecyclerView.ViewHolder {
            CircleImageView profile;
            TextView mcg, time;

            public ViewHolderRightMcg(View v) {
                super(v);
                profile = v.findViewById(R.id.profile_mcg_right);
                mcg = v.findViewById(R.id.mcg_right);
                time = v.findViewById(R.id.timeTv_mcg_right);
            }
        }

        public class ViewHolderVideo extends RecyclerView.ViewHolder {
            ImageView menuBtn;
            VideoView videoView;
            TextView caption, time;

            public ViewHolderVideo(View v) {
                super(v);
                caption = v.findViewById(R.id.timeTv_caption_video);
                time = v.findViewById(R.id.timeTv_video);
                menuBtn = v.findViewById(R.id.menuBtn_video_mcg);
                videoView = v.findViewById(R.id.videoViewMcg);
            }
        }
    }

}