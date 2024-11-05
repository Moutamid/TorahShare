package com.moutamid.torahsharee.activity;

import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.torahsharee.R.color.lighterGrey;
import static com.moutamid.torahsharee.utils.Stash.toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.torahsharee.R;
import com.moutamid.torahsharee.databinding.ActivityContactRequestsBinding;
import com.moutamid.torahsharee.model.ChatModel;
import com.moutamid.torahsharee.model.ContactRequestModel;
import com.moutamid.torahsharee.model.UserModel;
import com.moutamid.torahsharee.utils.Constants;
import com.moutamid.torahsharee.utils.Stash;

public class ContactRequestsActivity extends AppCompatActivity {

    private ActivityContactRequestsBinding b;
    ContactRequestModel contactRequestModel;
    UserModel myUserModel;

    Animation slide_down;
    Animation slide_up;

    UserModel other_userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityContactRequestsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        myUserModel = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);

        slide_up = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);
        slide_down = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down);


        b.backBtn.setOnClickListener(view -> {
            finish();
        });

        contactRequestModel = (ContactRequestModel) Stash.getObject(Constants.CURRENT_CONTACT_REQUEST, ContactRequestModel.class);

        b.message.setText(contactRequestModel.requester_mcg);

        Constants.databaseReference().child(Constants.USERS)
                .child(contactRequestModel.requester_uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            other_userModel = snapshot.getValue(UserModel.class);

                            with(getApplicationContext())
                                    .asBitmap()
                                    .load(other_userModel.profile_url)
                                    .apply(new RequestOptions()
                                            .placeholder(lighterGrey)
                                            .error(lighterGrey)
                                    )
                                    .diskCacheStrategy(DATA)
                                    .into(b.profile);

                            b.name1Tv.setText(other_userModel.name);
                            b.message.setText(contactRequestModel.requester_mcg);
                            b.name2Tv.setText(other_userModel.name);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        b.declineBtnRequest.setOnClickListener(view -> {
            Constants.databaseReference().child(Constants.USERS)
                    .child(Constants.auth().getUid())
                    .child(Constants.CONTACT_REQUESTS)
                    .child(contactRequestModel.push_key)
                    .removeValue();

            b.requestLayout.startAnimation(slide_down);
            toast("Request declined!");

        });
        b.acceptBtnRequest.setOnClickListener(view -> {
            // MY CHAT MODEL
            ChatModel chatModel = new ChatModel();
            chatModel.other_uid = contactRequestModel.requester_uid;
            chatModel.last_message = "Request accepted. Send your first message!";
            chatModel.time = Stash.getDate();
            chatModel.chat_id = contactRequestModel.chatID;
            chatModel.other_name = other_userModel.name;
            chatModel.is_contact = true;
            chatModel.other_profile = other_userModel.profile_url;

            Constants.databaseReference().child(Constants.CHATS)
                    .child(Constants.auth().getUid()).child(chatModel.chat_id)
                    .setValue(chatModel);

            // OTHER USER MODEL
            ChatModel other_chatModel = new ChatModel();
            other_chatModel.other_uid = Constants.auth().getCurrentUser().getUid();
            other_chatModel.last_message = "Request accepted. Send your first message!";
            other_chatModel.time = chatModel.time;
            other_chatModel.chat_id = contactRequestModel.chatID;
            other_chatModel.other_name = myUserModel.name;
            other_chatModel.is_contact = true;
            other_chatModel.other_profile = myUserModel.profile_url;

            Constants.databaseReference().child(Constants.CHATS)
                    .child(contactRequestModel.requester_uid).child(other_chatModel.chat_id)
                    .setValue(other_chatModel);

            Constants.databaseReference().child(Constants.USERS)
                    .child(Constants.auth().getUid())
                    .child(Constants.CONTACT_REQUESTS)
                    .child(contactRequestModel.push_key)
                    .removeValue();

            toast("Request accepted!");

            Stash.put(Constants.CHAT_MODEL, chatModel);
            finish();
            startActivity(new Intent(ContactRequestsActivity.this, ConversationActivity.class));
        });

    }
}