package com.moutamid.torahshare.startup;

import android.app.Application;

import com.moutamid.torahshare.utils.Stash;

// HERE WE ARE ATTACHING THIS CLASS TO OUR MANIFEST AND EXTENDING IT TO APPLICATION CLASS
// SO THAT THIS CLASS WILL EXECUTE IN THE FIRST PLACE AND WE GET OUR CONTEXT THROUGH THIS
// ONLY ONCE AND THEN USE IT MANY TIMES
public class AppContext extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stash.init(this);
    }
}
