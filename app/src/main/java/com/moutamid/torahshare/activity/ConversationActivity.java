package com.moutamid.torahshare.activity;

import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.torahshare.R.color.lighterGrey;
import static com.moutamid.torahshare.utils.Stash.toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.moutamid.torahshare.model.MessageReportModel;
import com.moutamid.torahshare.model.UserModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;
import com.tonyodev.fetch2core.Func;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationActivity extends AppCompatActivity {

    ChatModel chatModel = (ChatModel) Stash.getObject(Constants.CHAT_MODEL, ChatModel.class);
    private ActivityConversationBinding b;
    UserModel myUserModel = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);

    MessageModel currentSelectedMessageModel = new MessageModel();
    String currentVideoUrl;
    private Fetch fetch;
    TextView progressTv;
    TextView downloadedDataTv;
    TextView cancelBtn;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityConversationBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

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
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
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
        view.findViewById(R.id.reshare_btn_popup).setOnClickListener(view1 -> {
            if (mypopupWindow.isShowing()) mypopupWindow.dismiss();

            Dialog dialog = new Dialog(ConversationActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_rehare);
            dialog.setCancelable(true);

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
        dialog.findViewById(R.id.sendRequestBtn_report).setOnClickListener(view -> {
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
                .child(Constants.auth().getUid()).child(chatModel.chat_id)
                .setValue(chatModel);

        // // THIS WILL UPDATE LAST MESSAGE TO OTHER USER MODEL
        ChatModel other_chatModel = new ChatModel();
        other_chatModel.other_uid = Constants.auth().getUid();
        other_chatModel.last_message = mcg;
        other_chatModel.time = chatModel.time;
        other_chatModel.chat_id = chatModel.chat_id;
        other_chatModel.other_name = myUserModel.name;
        other_chatModel.is_contact = chatModel.is_contact;
        other_chatModel.other_profile = myUserModel.profile_url;
        Constants.databaseReference().child(Constants.CHATS)
                .child(chatModel.other_uid).child(chatModel.chat_id)
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
                    viewHolderVideo.videoView.seekTo(100);
                    viewHolderVideo.videoView.pause();

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