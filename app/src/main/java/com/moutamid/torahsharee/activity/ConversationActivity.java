package com.moutamid.torahsharee.activity;

import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.torahsharee.R.color.lighterGrey;
import static com.moutamid.torahsharee.utils.Stash.toast;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.torahsharee.R;
import com.moutamid.torahsharee.databinding.ActivityConversationBinding;
import com.moutamid.torahsharee.model.ChatModel;
import com.moutamid.torahsharee.model.FollowModel;
import com.moutamid.torahsharee.model.MessageModel;
import com.moutamid.torahsharee.model.MessageReportModel;
import com.moutamid.torahsharee.model.PostModel;
import com.moutamid.torahsharee.model.UserModel;
import com.moutamid.torahsharee.utils.Constants;
import com.moutamid.torahsharee.utils.Stash;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationActivity extends AppCompatActivity {

    ChatModel chatModel;
    private ActivityConversationBinding b;
    UserModel myUserModel;

    MessageModel currentSelectedMessageModel = new MessageModel();
    String currentVideoUrl;
    private Fetch fetch;
    TextView progressTv;
    TextView downloadedDataTv;
    TextView cancelBtn;
    ProgressBar progressBar;
    FlexboxLayout container;
    boolean is_selected = false;

    @Override
    protected void onResume() {
        super.onResume();
        getChosenContactsList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityConversationBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        myUserModel = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);
        chatModel = (ChatModel) Stash.getObject(Constants.CHAT_MODEL, ChatModel.class);

        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(this)
                .setDownloadConcurrentLimit(3)
                .build();

        fetch = Fetch.Impl.getInstance(fetchConfiguration);

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

        Dialog downloadDialog = new Dialog(ConversationActivity.this);
        downloadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        downloadDialog.setContentView(R.layout.dialog_download_video);
        downloadDialog.setCancelable(true);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(downloadDialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        progressTv = downloadDialog.findViewById(R.id.downloadProgressTv);
        downloadedDataTv = downloadDialog.findViewById(R.id.downloadedDataTv);
        cancelBtn = downloadDialog.findViewById(R.id.doanloadCancelBtn);
        progressBar = downloadDialog.findViewById(R.id.downloadProgressBar);

        Constants.databaseReference().child(Constants.CONVERSATIONS)
                .child(chatModel.chat_id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            messagesModelArrayList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                                MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                                messageModel.push_key = dataSnapshot.getKey();
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
        LayoutInflater inflater = (LayoutInflater)
                getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.popup_conversation, null);

        view.findViewById(R.id.report_btn_popup).setOnClickListener(view1 -> {
            if (mypopupWindow.isShowing()) mypopupWindow.dismiss();

            showReportDialog("Report this video?");

        });

        view.findViewById(R.id.comment_btn_popup).setOnClickListener(view1 -> {
            if (mypopupWindow.isShowing()) mypopupWindow.dismiss();

            // TODO: 9/10/2022 ShowCommentDialog
        });

        view.findViewById(R.id.reshare_btn_popup).setOnClickListener(view1 -> {
            if (mypopupWindow.isShowing()) mypopupWindow.dismiss();

            Dialog dialog = new Dialog(ConversationActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_rehare);
            dialog.setCancelable(true);

            VideoView videoView = dialog.findViewById(R.id.videoView_reshare_dialog);
            CheckBox followersCheckBox = dialog.findViewById(R.id.followersCheckBox_reshare);
            CheckBox contactCheckBox = dialog.findViewById(R.id.contactsCheckBox_reshare);
            MaterialButton chooseContactBtn = dialog.findViewById(R.id.chooseContactBtn);
            MaterialButton reShareBtn = dialog.findViewById(R.id.reshareBtn);
            container = dialog.findViewById(R.id.containerFlexBox);
            ImageView playBtn = dialog.findViewById(R.id.videoPlayBtn_reshare);

            reShareBtn.setOnClickListener(view2 -> {
                if (!contactCheckBox.isChecked() || !followersCheckBox.isChecked())
                    return;

                ProgressDialog progressDialog;
                progressDialog = new ProgressDialog(ConversationActivity.this);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Loading...");
                progressDialog.show();

                PostModel postModel = new PostModel();
                postModel.name = myUserModel.name;
                postModel.date = Stash.getDate();
                postModel.caption = currentSelectedMessageModel.message;
                postModel.share_count = 0;
                postModel.comment_count = 0;
                postModel.video_link = currentVideoUrl;
                postModel.profile_link = myUserModel.profile_url;
                postModel.my_uid = Constants.auth().getUid();
                postModel.push_key = Constants.databaseReference().child(Constants.PUBLIC_POSTS).push().getKey();

                Constants.databaseReference().child(Constants.PUBLIC_POSTS).child(postModel.push_key)
                        .setValue(postModel);

                if (followersCheckBox.isChecked()) {
                    ArrayList<FollowModel> followingList = Stash.getArrayList(Constants.FOLLOWING_LIST, FollowModel.class);
                    for (FollowModel followModel : followingList) {
                        // THIS WILL CREATE A CHAT ENTRY IN MY DATABASE CHILD
                        ChatModel chatModel = new ChatModel();
                        chatModel.other_uid = followModel.uid;
                        chatModel.last_message = "Sent a video";
                        chatModel.time = Stash.getDate();
                        chatModel.chat_id = Constants.databaseReference().push().getKey();
                        chatModel.other_name = followModel.name;
                        chatModel.is_contact = false;
                        chatModel.other_profile = followModel.profile_url;
                        Constants.databaseReference().child(Constants.CHATS)
                                .child(Constants.auth().getUid()).child(chatModel.chat_id)
                                .setValue(chatModel);

                        // // THIS WILL UPDATE LAST MESSAGE TO OTHER USER MODEL
                        ChatModel other_chatModel = new ChatModel();
                        other_chatModel.other_uid = Constants.auth().getUid();
                        other_chatModel.last_message = "Sent a video";
                        other_chatModel.time = chatModel.time;
                        other_chatModel.chat_id = chatModel.chat_id;
                        other_chatModel.other_name = myUserModel.name;
                        other_chatModel.is_contact = chatModel.is_contact;
                        other_chatModel.other_profile = myUserModel.profile_url;
                        Constants.databaseReference().child(Constants.CHATS)
                                .child(chatModel.other_uid).child(chatModel.chat_id)
                                .setValue(chatModel);

                        // THIS WILL ADD A ENTRY OF MESSAGE TO CONVERSATION OF THAT USER AND MINE AS WELL
                        MessageModel messageModel2 = new MessageModel();
                        messageModel2.time = Stash.getDate();
                        messageModel2.sent_by = Constants.auth().getUid();
                        messageModel2.message = "Here is a new video for you!" + Constants.SEPARATOR + postModel.video_link;
                        Constants.databaseReference().child(Constants.CONVERSATIONS)
                                .child(chatModel.chat_id).push().setValue(messageModel2);

                    }
                }

                if (contactCheckBox.isChecked()) {
                    ArrayList<ChatModel> list_text = Stash.getArrayList(Constants.CHOSEN_CONTACTS_LIST, ChatModel.class);
                    for (ChatModel followModel : list_text) {
                        // THIS WILL CREATE A CHAT ENTRY IN MY DATABASE CHILD
                        ChatModel chatModel = new ChatModel();
                        chatModel.other_uid = followModel.other_uid;
                        chatModel.last_message = "Sent a video";
                        chatModel.time = Stash.getDate();
                        chatModel.chat_id = Constants.databaseReference().push().getKey();
                        chatModel.other_name = followModel.other_name;
                        chatModel.is_contact = false;
                        chatModel.other_profile = followModel.other_profile;
                        Constants.databaseReference().child(Constants.CHATS)
                                .child(Constants.auth().getUid()).child(chatModel.chat_id)
                                .setValue(chatModel);

                        // // THIS WILL UPDATE LAST MESSAGE TO OTHER USER MODEL
                        ChatModel other_chatModel = new ChatModel();
                        other_chatModel.other_uid = Constants.auth().getUid();
                        other_chatModel.last_message = "Sent a video";
                        other_chatModel.time = chatModel.time;
                        other_chatModel.chat_id = chatModel.chat_id;
                        other_chatModel.other_name = myUserModel.name;
                        other_chatModel.is_contact = chatModel.is_contact;
                        other_chatModel.other_profile = myUserModel.profile_url;
                        Constants.databaseReference().child(Constants.CHATS)
                                .child(chatModel.other_uid).child(chatModel.chat_id)
                                .setValue(chatModel);

                        // THIS WILL ADD A ENTRY OF MESSAGE TO CONVERSATION OF THAT USER AND MINE AS WELL
                        MessageModel messageModel2 = new MessageModel();
                        messageModel2.time = Stash.getDate();
                        messageModel2.sent_by = Constants.auth().getUid();
                        messageModel2.message = "Here is a new video for you!" + Constants.SEPARATOR + postModel.video_link;
                        Constants.databaseReference().child(Constants.CONVERSATIONS)
                                .child(chatModel.chat_id).push().setValue(messageModel2);

                    }
                }

                progressDialog.dismiss();
                toast("Done");
                dialog.dismiss();
            });

            Uri uri = Uri.parse(currentVideoUrl);

            videoView.setVideoURI(uri);
            // TODO: 7/12/2022  videoView.start();
            videoView.pause();
            videoView.seekTo(1);

            videoView.setOnClickListener(view5 -> {
                if (playBtn.getVisibility() == View.GONE) {
                    playBtn.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(() -> {
                        playBtn.setVisibility(View.GONE);
                    }, 3000);
                } else {
                    playBtn.setVisibility(View.GONE);
                }
            });

            playBtn.setOnClickListener(view6 -> {
                if (videoView.isPlaying()) {
                    // IS PLAYING
                    playBtn.setImageResource(R.drawable.ic_play_btn);
                    videoView.pause();

                } else {
                    // PAUSED OR NOT STARTED
                    playBtn.setImageResource(R.drawable.ic_pause_btn);
                    new Handler().postDelayed(() -> {
                        playBtn.setVisibility(View.GONE);
                    }, 3000);

                    videoView.start();
                }
            });
            getChosenContactsList();
            contactCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    chooseContactBtn.setVisibility(View.VISIBLE);
                    container.setVisibility(View.VISIBLE);
                } else {
                    chooseContactBtn.setVisibility(View.GONE);
                    container.setVisibility(View.GONE);
                }
            });

            chooseContactBtn.setOnClickListener(view2 -> {
                startActivity(new Intent(ConversationActivity.this, ChooseContactsActivity.class));
            });

            dialog.findViewById(R.id.crossBtnDialog_reshare).setOnClickListener(view22 -> {
                dialog.dismiss();
            });
            dialog.show();
            dialog.getWindow().setAttributes(layoutParams);

        });
        view.findViewById(R.id.download_btn_popup).setOnClickListener(view1 -> {
            if (mypopupWindow.isShowing()) mypopupWindow.dismiss();

            String file = "/downloads/" +
                    currentSelectedMessageModel.push_key +
                    ".mp4";

            final Request request = new Request(currentVideoUrl, file);
            request.setPriority(Priority.HIGH);
            request.setNetworkType(NetworkType.ALL);
            request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG");

            fetch.enqueue(request, updatedRequest -> {
                //Request was successfully enqueued for download.
                downloadDialog.show();
                downloadDialog.getWindow().setAttributes(layoutParams);
            }, error -> {
                //An error occurred enqueuing the request.
                toast(error.toString());
            });

            cancelBtn.setOnClickListener(view2 -> {
                fetch.cancelAll();
                downloadDialog.dismiss();
            });

            FetchListener fetchListener = new FetchListener() {
                @Override
                public void onAdded(@NonNull Download download) {

                }

                @Override
                public void onQueued(@NonNull Download download, boolean b) {
                    if (request.getId() == download.getId()) {
                    }
                }

                @Override
                public void onWaitingNetwork(@NonNull Download download) {

                }

                @Override
                public void onCompleted(@NonNull Download download) {
                    toast("Downloaded");
                    downloadDialog.dismiss();
                }

                @Override
                public void onError(@NonNull Download download, @NonNull Error error, @Nullable Throwable throwable) {
                    toast(error.toString());
                }

                @Override
                public void onDownloadBlockUpdated(@NonNull Download download, @NonNull DownloadBlock downloadBlock, int i) {

                }

                @Override
                public void onStarted(@NonNull Download download, @NonNull List<? extends DownloadBlock> list, int i) {

                }

                @Override
                public void onProgress(@NonNull Download download, long etaInMilliSeconds, long l1) {
                    if (request.getId() == download.getId()) {

                        int progress = download.getProgress();
                        if (progressTv != null)
                            progressTv.setText(progress + "%");

                        if (progressBar != null)
                            progressBar.setProgress(progress);

                        if (downloadedDataTv != null)
                            downloadedDataTv.setText(download.getDownloaded() + " MB of " + download.getTotal() + " MB");
                    }
                }

                @Override
                public void onPaused(@NonNull Download download) {

                }

                @Override
                public void onResumed(@NonNull Download download) {

                }

                @Override
                public void onCancelled(@NonNull Download download) {
                    downloadDialog.dismiss();
                    toast("Cancelled");
                }

                @Override
                public void onRemoved(@NonNull Download download) {
                    downloadDialog.dismiss();
                }

                @Override
                public void onDeleted(@NonNull Download download) {
                    downloadDialog.dismiss();
                }
            };

            fetch.addListener(fetchListener);

        });

        mypopupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        mypopupWindow.getContentView().setOnClickListener(v -> {
            if (mypopupWindow.isShowing()) mypopupWindow.dismiss();
        });

        b.deleteIconConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deleteArrayList.size() != 0) {

                    for (MessageModel model : deleteArrayList) {
                        Constants.databaseReference().child(Constants.CONVERSATIONS)
                                .child(chatModel.chat_id)
                                .child(model.push_key)
                                .removeValue();
                    }
//                    b.menuIcon.setVisibility(View.VISIBLE);
                    b.deleteIconConversation.setVisibility(View.GONE);
//                    b.searchButton.setVisibility(View.VISIBLE);
                    Toast.makeText(ConversationActivity.this, "Done", Toast.LENGTH_SHORT).show();

                    deleteArrayList.clear();
                }
            }
        });

    }

    private void getChosenContactsList() {
        if (container == null)
            return;
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLayoutParams.setMargins(5, 5, 5, 5);
//        ArrayList<String> list_text = Stash.getArrayList(Constants.CHOSEN_CONTACTS_LIST, String.class);
        ArrayList<ChatModel> list_text = Stash.getArrayList(Constants.CHOSEN_CONTACTS_LIST, ChatModel.class);
        for (int i = 0; i < list_text.size(); i++) {
            final TextView tv = new TextView(getApplicationContext());
            tv.setText(list_text.get(i).other_name);
            tv.setHeight(100);
            tv.setTextSize(14.0f);
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(getResources().getColor(R.color.default_text_black));//Color.parseColor("#000000")
            tv.setBackground(getResources().getDrawable(R.drawable.rounded_text_view));
            tv.setId(i + 1);
            tv.setLayoutParams(buttonLayoutParams);
            tv.setTag(i + 1);
            tv.setPadding(20, 10, 20, 10);
            container.addView(tv);
        }
    }

    private void showReportDialog(String mcg) {
        Dialog dialog = new Dialog(ConversationActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_report);
        dialog.setCancelable(true);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        TextView topTextView = dialog.findViewById(R.id.topi_report);
        topTextView.setText(mcg);

        dialog.findViewById(R.id.crossBtnDialog_report).setOnClickListener(view -> {
            dialog.dismiss();
        });
        dialog.findViewById(R.id.cancelBtn_report).setOnClickListener(view -> {
            dialog.dismiss();
        });
        dialog.findViewById(R.id.sendBtn_report).setOnClickListener(view -> {
            EditText reasonEt = dialog.findViewById(R.id.messageEt_report);
            String reasonStr = reasonEt.getText().toString();

            if (reasonStr.isEmpty())
                return;

            MessageReportModel reportModel = new MessageReportModel();
            reportModel.reason = reasonStr;
            reportModel.messageModel = currentSelectedMessageModel;

            Constants.databaseReference().child(Constants.REPORTED_MESSAGES)
                    .push()
                    .setValue(reportModel);

            dialog.dismiss();
            toast("Done!");

        });
        dialog.show();
        dialog.getWindow().setAttributes(layoutParams);
    }

    @Override
    public void onBackPressed() {
        if (mypopupWindow.isShowing()) {
            mypopupWindow.dismiss();
            return;
        }
        super.onBackPressed();
    }

    PopupWindow mypopupWindow;

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
        // THIS WILL UPDATE LAST MESSAGE TO MY CHAT MODEL
        chatModel.last_message = mcg;
        chatModel.time = Stash.getDate();
        Constants.databaseReference().child(Constants.CHATS)
                .child(Constants.auth().getCurrentUser().getUid()).child(chatModel.chat_id)
                .setValue(chatModel);

        // // THIS WILL UPDATE LAST MESSAGE TO OTHER USER MODEL
        ChatModel other_chatModel = new ChatModel();
        other_chatModel.other_uid = Constants.auth().getCurrentUser().getUid();
        other_chatModel.last_message = mcg;
        other_chatModel.time = chatModel.time;
        other_chatModel.chat_id = chatModel.chat_id;
        other_chatModel.other_name = myUserModel.name;
        other_chatModel.is_contact = chatModel.is_contact;
        other_chatModel.other_profile = myUserModel.profile_url;
        Constants.databaseReference().child(Constants.CHATS)
                .child(chatModel.other_uid).child(other_chatModel.chat_id)
                .setValue(other_chatModel);

        // THIS WILL ADD A ENTRY OF MESSAGE TO CONVERSATION OF THAT USER AND MINE AS WELL
        MessageModel messageModel = new MessageModel();
        messageModel.time = Stash.getDate();
        messageModel.sent_by = Constants.auth().getUid();
        messageModel.message = mcg;
        Constants.databaseReference().child(Constants.CONVERSATIONS)
                .child(chatModel.chat_id).push().setValue(messageModel);
    }

    private ArrayList<MessageModel> messagesModelArrayList = new ArrayList<>();
    private ArrayList<MessageModel> deleteArrayList = new ArrayList<>();

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

                    implementOnDelete(viewHolderLeftMcg.parentLeftMessage, messageModel, holder.getAdapterPosition());

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

                    implementOnDelete(viewHolderRightMcg.parentRightMessage, messageModel, holder.getAdapterPosition());

                    break;
                case VIDEO_LAYOUT:
                    ViewHolderVideo viewHolderVideo = (ViewHolderVideo) holder;

                    String[] parts = messageModel.message.split(Constants.SEPARATOR);

                    viewHolderVideo.caption.setText(parts[0]);
                    viewHolderVideo.time.setText(messageModel.time);

                    Uri uri = Uri.parse(parts[1]);

                    viewHolderVideo.videoView.setVideoURI(uri);
                    viewHolderVideo.videoView.pause();
                    viewHolderVideo.videoView.seekTo(1);
                    /*TODO: viewHolderVideo.videoView.start();
                   */

                    viewHolderVideo.videoView.setOnClickListener(view -> {
                        if (viewHolderVideo.playBtn.getVisibility() == View.GONE) {
                            viewHolderVideo.playBtn.setVisibility(View.VISIBLE);
                            new Handler().postDelayed(() -> {
                                viewHolderVideo.playBtn.setVisibility(View.GONE);
                            }, 3000);
                        } else {
                            viewHolderVideo.playBtn.setVisibility(View.GONE);
                        }
                    });

                    viewHolderVideo.playBtn.setOnClickListener(view -> {
                        if (viewHolderVideo.videoView.isPlaying()) {
                            // IS PLAYING
                            viewHolderVideo.playBtn.setImageResource(R.drawable.ic_play_btn);
                            viewHolderVideo.videoView.pause();

                        } else {
                            // PAUSED OR NOT STARTED
                            viewHolderVideo.playBtn.setImageResource(R.drawable.ic_pause_btn);
                            new Handler().postDelayed(() -> {
                                viewHolderVideo.playBtn.setVisibility(View.GONE);
                            }, 3000);

                            viewHolderVideo.videoView.start();
                        }
                    });

                    viewHolderVideo.menuBtn.setOnClickListener(view -> {
                        currentSelectedMessageModel = messageModel;
                        currentVideoUrl = parts[1];
                        mypopupWindow.showAsDropDown(view, -253, 0);
                    });

                    break;
            }
        }

        private void implementOnDelete(RelativeLayout messageLayout, MessageModel messageModel, int adapterPosition) {
            if (messageModel.is_selected) {
                messageLayout.setBackgroundResource(R.color.darkGrey);
            } else {
                messageLayout.setBackgroundResource(R.color.white);
            }

            messageLayout.setOnLongClickListener(view -> {
                messageLayout.setBackgroundResource(R.color.darkGrey);
                deleteArrayList.add(messageModel);
                messagesModelArrayList.get(adapterPosition).is_selected = true;

                b.deleteIconConversation.setVisibility(View.VISIBLE);
//                        b.deleteCountText.setVisibility(View.VISIBLE);
//                        b.profileimage.setVisibility(View.INVISIBLE);
//                        b.nametext.setVisibility(View.INVISIBLE);
//                        b.deleteCountText.setText(deleteArrayList.size() + " Selected");
                return false;
            });

            if (messageModel.is_selected) {
                messageLayout.setOnClickListener(view -> {
                    messageLayout.setBackgroundResource(R.color.white);
                    messagesModelArrayList.get(adapterPosition).is_selected = false;
                    for (int i = 0; i < deleteArrayList.size(); i++) {
                        if (deleteArrayList.get(i).push_key
                                .equals(chatModel.push_key)) {
                            // DELETE FROM DELETE ARRAYLIST
                            deleteArrayList.remove(i);
                        }
                    }
                    if (deleteArrayList.size() == 0) {
                        b.deleteIconConversation.setVisibility(View.GONE);
//                                b.deleteCountText.setVisibility(View.GONE);
//                                b.profileimage.setVisibility(View.VISIBLE);
//                                b.nametext.setVisibility(View.VISIBLE);
                    }
//                            else {
//                                b.deleteCountText.setText(deleteArrayList.size() + " Selected");
//                            }
                });
            } else {
                messageLayout.setOnClickListener(null);
            }
        }

        public class ViewHolderLeftMcg extends RecyclerView.ViewHolder {
            CircleImageView profile;
            TextView mcg, time;
            RelativeLayout parentLeftMessage;

            public ViewHolderLeftMcg(View v) {
                super(v);
                profile = v.findViewById(R.id.profile_mcg_left);
                mcg = v.findViewById(R.id.mcg_left);
                time = v.findViewById(R.id.timeTv_mcg_left);
                parentLeftMessage = v.findViewById(R.id.parent_left_message);
            }
        }

        public class ViewHolderRightMcg extends RecyclerView.ViewHolder {
            CircleImageView profile;
            TextView mcg, time;
            RelativeLayout parentRightMessage;

            public ViewHolderRightMcg(View v) {
                super(v);
                profile = v.findViewById(R.id.profile_mcg_right);
                mcg = v.findViewById(R.id.mcg_right);
                time = v.findViewById(R.id.timeTv_mcg_right);
                parentRightMessage = v.findViewById(R.id.parent_right_message);
            }
        }

        public class ViewHolderVideo extends RecyclerView.ViewHolder {
            ImageView menuBtn, playBtn;
            VideoView videoView;
            TextView caption, time;

            public ViewHolderVideo(View v) {
                super(v);
                caption = v.findViewById(R.id.timeTv_caption_video);
                time = v.findViewById(R.id.timeTv_video);
                menuBtn = v.findViewById(R.id.menuBtn_video_mcg);
                videoView = v.findViewById(R.id.videoViewMcg);
                playBtn = v.findViewById(R.id.videoPlayBtn);
            }
        }
    }

}