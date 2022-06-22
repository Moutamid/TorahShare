package com.moutamid.torahshare.model;

import java.util.ArrayList;

public class PostModel {
    public String name, date, caption,
            video_link, profile_link, my_uid, push_key;
    public int share_count, comment_count;

    public ArrayList<String> followers_list, contacts_list;

    public PostModel() {
    }
}