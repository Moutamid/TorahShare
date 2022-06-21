package com.moutamid.torahshare.startup;

import android.app.Application;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.torahshare.model.UserModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

// HERE WE ARE ATTACHING THIS CLASS TO OUR MANIFEST AND EXTENDING IT TO APPLICATION CLASS
// SO THAT THIS CLASS WILL EXECUTE IN THE FIRST PLACE AND WE GET OUR CONTEXT THROUGH THIS
// ONLY ONCE AND THEN USE IT MANY TIMES
public class AppContext extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stash.init(this);

        if (Constants.auth().getCurrentUser() == null)
            return;

        Constants.databaseReference().child(Constants.USERS)
                .child(Constants.auth().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Stash.put(Constants.CURRENT_USER_MODEL, snapshot.getValue(UserModel.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}
