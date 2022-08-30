package com.moutamid.torahshare.activity;

import static com.moutamid.torahshare.utils.Stash.toast;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moutamid.torahshare.R;
import com.moutamid.torahshare.databinding.ActivityUploadPostPreviewBinding;
import com.moutamid.torahshare.model.ChatModel;
import com.moutamid.torahshare.model.FollowModel;
import com.moutamid.torahshare.model.MessageModel;
import com.moutamid.torahshare.model.PostModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

import java.util.ArrayList;

public class UploadPostPreviewActivity extends AppCompatActivity {

    private ActivityUploadPostPreviewBinding b;
    boolean IS_FOLLOWER_CHECKED;
    boolean IS_CONTACT_CHECKED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityUploadPostPreviewBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        PostModel postModel = (PostModel) Stash.getObject(Constants.CURRENT_POST_MODEL, PostModel.class);

        IS_FOLLOWER_CHECKED = getIntent().getBooleanExtra(Constants.IS_FOLLOWER_CHECKED, false);
        IS_CONTACT_CHECKED = getIntent().getBooleanExtra(Constants.IS_CONTACT_CHECKED, false);

        b.videoName.setText(System.currentTimeMillis() + ".mp4");
        b.captionTextView.setText(postModel.caption);

        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLayoutParams.setMargins(5, 5, 5, 5);

        for (int i = 0; i < postModel.tagsList.size(); i++) {
            final TextView tv = new TextView(getApplicationContext());
            tv.setText(postModel.tagsList.get(i));
            tv.setHeight(100);
            tv.setTextSize(14.0f);
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(getResources().getColor(R.color.default_text_black));//Color.parseColor("#000000")
            tv.setBackground(getResources().getDrawable(R.drawable.rounded_text_view));
            tv.setId(i + 1);
            tv.setLayoutParams(buttonLayoutParams);
            tv.setTag(i + 1);
            tv.setPadding(20, 10, 20, 10);
            b.tagEditTextContacts.addView(tv);
        }

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
            b.tagEditTextContacts.addView(tv);
        }

        if (!IS_CONTACT_CHECKED) {
            b.contactsTextView.setVisibility(View.GONE);
            b.tagEditTextContacts.setVisibility(View.GONE);
        }

        if (!IS_FOLLOWER_CHECKED)
            b.followersTextVieww.setVisibility(View.GONE);

        b.reshareBtn.setOnClickListener(view2 -> {
            b.reshareBtn.setText("Sharing...");
            ProgressDialog progressDialog;
            progressDialog = new ProgressDialog(UploadPostPreviewActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading...");
            progressDialog.show();

            Constants.databaseReference().child(Constants.PUBLIC_POSTS).child(postModel.push_key)
                    .setValue(postModel);

            if (IS_FOLLOWER_CHECKED) {
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
                    other_chatModel.other_name = postModel.name;
                    other_chatModel.is_contact = chatModel.is_contact;
                    other_chatModel.other_profile = postModel.profile_link;
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

            if (IS_CONTACT_CHECKED) {
//                ArrayList<ChatModel> list_text2 = Stash.getArrayList(Constants.CHOSEN_CONTACTS_LIST, ChatModel.class);
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
                    other_chatModel.other_name = postModel.name;
                    other_chatModel.is_contact = chatModel.is_contact;
                    other_chatModel.other_profile = postModel.profile_link;
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
            b.postPreviewLayout.setVisibility(View.GONE);
            b.screen7.setVisibility(View.VISIBLE);
        });

        b.backToHomeBtn.setOnClickListener(view -> {
            Intent intent = new Intent(UploadPostPreviewActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);
        });

    }


}